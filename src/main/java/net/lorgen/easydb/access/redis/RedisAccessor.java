package net.lorgen.easydb.access.redis;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.DatabaseTypeAccessor;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.Repositories;
import net.lorgen.easydb.WrappedIndex;
import net.lorgen.easydb.connection.ConnectionRegistry;
import net.lorgen.easydb.field.FieldValue;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.interact.JoinWrapper;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.query.Operator;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.req.QueryRequirement;
import net.lorgen.easydb.query.req.SimpleRequirement;
import net.lorgen.easydb.query.traverse.RequirementCase;
import net.lorgen.easydb.query.traverse.RequirementTraverser;
import net.lorgen.easydb.response.ResponseEntity;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * Redis database type accessor
 *
 * @param <T> The type this accessor handles
 */
public class RedisAccessor<T> implements DatabaseTypeAccessor<T> {

    /**
     * The format of the key in the Redis database
     */
    private static final String AUTO_INCREMENT_FORMAT = "auto_increment:%s";

    private static final String INDEX_HASH_FORMAT = "index:%s(fields[%s])";

    private static final String STORE_FORMAT = "%s(%s)";

    /*
     * Notes
     *
     * hashes for storage
     * 1 hash per object
     * key of hash consisting of string versions of the keys (recommend 1 key) + this table name
     * key of field in hash = field name, val ofc the val of the field (null handling?)
     *
     * indices using another hash
     * key of hash based on this table name and index fields
     * keys in hash same as key of main object hash, value being the index value
     *
     * auto increment by separate integer field
     * use "INCR" to increment, and use the returned value as ID val
     */

    private final ItemRepository<T> repository;
    private final String table;
    private final RedisConfiguration configuration;
    private final RedisJoinWrapper[] joinWrappers;

    public RedisAccessor(RedisConfiguration config, ItemRepository<T> repository, String table) {
        this.repository = repository;
        this.table = table;
        this.configuration = config;
        this.joinWrappers = Arrays.stream(repository.getProfile().getJoins())
          .map(wrapper -> new RedisJoinWrapper(wrapper, this))
          .toArray(RedisJoinWrapper[]::new);

        this.setUp();
    }

    private Jedis getResource() {
        return ConnectionRegistry.getInstance().<Jedis>getPool(this.configuration).getConnection();
    }

    @Override
    public void setUp() {
        if (this.repository.getProfile().getAutoIncrementField() == null) {
            return;
        }

        String key = String.format(AUTO_INCREMENT_FORMAT, this.table);

        try (Jedis jedis = this.getResource()) {
            if (jedis.exists(key)) {
                return;
            }

            jedis.set(key, "0");
        }
    }

    @Override
    public ResponseEntity<T> findFirst(Query<T> query) {
        if (query.getRequirement() == null) {
            return this.getFirst();
        }

        return this.getFirst(query.getRequirement());
    }

    @Override
    public List<ResponseEntity<T>> findAll(Query<T> query) {
        if (query.getRequirement() == null) {
            return this.getAll();
        }

        return this.getAll(query.getRequirement());
    }

    @Override
    public void saveOrUpdate(Query<T> query) {
        // In this case we first have to find all the keys we wish to modify. This
        // means we have to find all matching keys.
        if (query.getRequirement() != null) {
            FieldValue<T>[] newValues = query.getValues();
            List<String> keys = this.getKeys(query.getRequirement());

            for (String key : keys) {
                FieldValue<T>[] currentValues = this.getValues(key);
                for (FieldValue<T> newValue : newValues) {
                    this.repository.updateArrayValue(newValue.getField(), newValue.getValue(), currentValues);
                }

                // Current values have been updated
                this.insertIntoHash(key, currentValues);
            }

            // Query completed
            return;
        }

        String thisKey = this.getKey(query.getValues());

        // We now have a new T instance to save
        // First; Verify all unique fields:
        for (WrappedIndex<T> index : this.repository.getProfile().getUniqueIndices()) {
            FieldValue<T>[] values = Arrays.stream(index.getFields())
              .map(field -> new FieldValue<>(field, this.repository.getArrayValue(field, query.getValues())))
              .toArray(FieldValue[]::new);

            List<String> keys = this.getKeys(values);
            if (keys.isEmpty()) {
                continue;
            }

            // This would mean we are working with the same object,
            // and we of course need to be able to update it
            if (keys.size() == 1 && keys.get(0).equals(thisKey)) {
                continue;
            }

            throw new IllegalArgumentException("Unique index \"" + index + "\" is same as another entry!");
        }

        // Then; Save it
        this.insertIntoHash(query.getObjectInstance(), query.getValues());
    }

    @Override
    public void delete(Query<T> query) {
        if (query.getRequirement() == null) {
            this.drop();
            return;
        }

        String[] keys = this.getKeys(query.getRequirement()).toArray(new String[0]);

        try (Jedis jedis = this.getResource()) {
            jedis.del(keys);
        }

        this.removeFromIndices(keys);
    }

    @Override
    public void drop() {
        try (Jedis jedis = this.getResource()) {
            Set<String> keys = jedis.keys(String.format(STORE_FORMAT, this.table, "*"));
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
            }

            for (WrappedIndex<T> index : this.repository.getProfile().getIndices()) {
                jedis.del(this.getIndexHashKey(index.getFields()));
            }

            if (this.repository.getProfile().getAutoIncrementField() != null) {
                jedis.del(String.format(AUTO_INCREMENT_FORMAT, this.table));
            }
        }
    }

    public int getNextAutoIncrementValue() {
        String key = String.format(AUTO_INCREMENT_FORMAT, this.table);

        try (Jedis jedis = this.getResource()) {
            return Math.toIntExact(jedis.incr(key));
        }
    }

    // Internals

    private List<ResponseEntity<T>> getAll() {
        try (Jedis jedis = this.getResource()) {
            Set<String> keys = jedis.keys(String.format(STORE_FORMAT, this.table, "*"));
            List<ResponseEntity<T>> list = Lists.newArrayList();
            for (String key : keys) {
                list.add(this.getObject(key));
            }

            return list;
        }
    }

    private ResponseEntity<T> getFirst() {
        try (Jedis jedis = this.getResource()) {
            Set<String> keys = jedis.keys(String.format(STORE_FORMAT, this.table, "*"));
            String key = Iterables.getFirst(keys, null);
            if (key == null) {
                return new ResponseEntity<>(this.repository.getProfile());
            }

            return this.getObject(key);
        }
    }

    private List<ResponseEntity<T>> getAll(QueryRequirement requirement) {
        List<String> keys = this.getKeys(requirement);
        List<ResponseEntity<T>> list = Lists.newArrayList();
        for (String key : keys) {
            list.add(this.getObject(key));
        }

        return list;
    }

    private ResponseEntity<T> getFirst(QueryRequirement requirement) {
        List<String> keys = this.getKeys(requirement);
        if (keys.size() == 0) {
            return new ResponseEntity<>(this.repository.getProfile());
        }

        return this.getObject(keys.get(0));
    }

    private ResponseEntity<T> getObject(String key) {
        FieldValue<T>[] values = this.getValues(key);
        if (values == null) {
            return new ResponseEntity<>(this.repository.getProfile());
        }

        return new ResponseEntity<>(this.repository.getProfile(), values);
    }

    private FieldValue<T>[] getValues(String key) {
        List<FieldValue<T>> list = Lists.newArrayList();

        try (Jedis jedis = this.getResource()) {
            Map<String, String> valueMap = jedis.hgetAll(key);
            if (valueMap.isEmpty()) {
                return null;
            }

            for (Entry<String, String> entry : valueMap.entrySet()) {
                PersistentField<T> field = this.repository.getProfile().resolveField(entry.getKey());
                if (field == null) {
                    throw new IllegalArgumentException("Unknown field \"" + entry.getKey() + "\"!");
                }

                Object value = field.getType().fromString(this.repository, field, entry.getValue());
                list.add(new FieldValue<>(field, value));
            }

            // Handle joins
            FieldValue<T>[] values = list.toArray(new FieldValue[0]);

            // First; Get all values
            List<FieldValue> joinValues = Lists.newArrayList();
            for (RedisJoinWrapper joinWrapper : this.joinWrappers) {
                Collections.addAll(joinValues, joinWrapper.getValues(values));
            }

            // Then; Compute into usable data
            fields:
            for (PersistentField<T> field : this.repository.getProfile().getFields()) {
                if (!field.isJoined()) {
                    continue;
                }

                for (FieldValue value : joinValues) {
                    if (!value.getField().getName().equals(field.getName())) {
                        continue;
                    }

                    list.add(new FieldValue<>(field, value.getValue()));
                    continue fields;
                }
            }
        }

        return list.toArray(new FieldValue[0]);
    }

    private String getKey(FieldValue<T>[] values) {
        PersistentField<T>[] keys = this.repository.getProfile().getKeys();
        StringBuilder builder = new StringBuilder();

        for (PersistentField<T> key : keys) {
            if (!builder.toString().isEmpty()) {
                builder.append(":");
            }

            Object value = this.repository.getArrayValue(key, values);
            if (value == null) {
                throw new IllegalArgumentException("Missing value for key \"" + key.getName() + "\"!");
            }

            builder.append(key.getType().toString(this.repository, key, value));
        }

        return String.format(STORE_FORMAT, this.table, builder.toString());
    }

    private void insertIntoHash(Optional<T> objectInstance, FieldValue<T>[] values) {
        PersistentField<T> autoIncrement = this.repository.getProfile().getAutoIncrementField();
        if (autoIncrement != null && ((int) this.repository.getArrayValue(autoIncrement, values)) == 0) {
            int value = this.getNextAutoIncrementValue();
            objectInstance.ifPresent(tValue -> autoIncrement.set(tValue, value));
            this.repository.updateArrayValue(autoIncrement, value, values);
        }

        this.insertIntoHash(values);
    }

    private void insertIntoHash(FieldValue<T>[] values) {
        PersistentField<T> autoIncrement = this.repository.getProfile().getAutoIncrementField();
        if (autoIncrement != null && ((int) this.repository.getArrayValue(autoIncrement, values)) == 0) {
            throw new IllegalArgumentException("Missing value for auto increment field \"" + autoIncrement.getName() + "\"");
        }

        this.insertIntoHash(this.getKey(values), values);
    }

    private void insertIntoHash(String key, FieldValue<T>[] values) {
        Map<String, String> map = Maps.newHashMap();
        for (FieldValue<T> value : values) {
            PersistentField<T> field = value.getField();
            map.put(field.getName(), field.getType().toString(this.repository, field, value.getValue()));
        }

        this.addToIndices(key, values);
        this.insertIntoHash(key, map);
    }

    private void insertIntoHash(String key, Map<String, String> valuesByKey) {
        try (Jedis jedis = this.getResource()) {
            jedis.hmset(key, valuesByKey);
        }
    }

    private List<String> getKeys(QueryRequirement requirement) {
        Set<String> keys = Sets.newHashSet();
        RequirementTraverser traverser = new RequirementTraverser(requirement);
        for (RequirementCase requirementCase : traverser.getCases()) {
            List<FieldValue<T>> values = Lists.newArrayList();
            for (SimpleRequirement simpleRequirement : requirementCase.getRequirements()) {
                if (simpleRequirement.getOperator() != Operator.EQUALS) {
                    throw new IllegalArgumentException("Unsupported operation \"" + simpleRequirement.getOperator().name() + "\" by redis!");
                }

                values.add(new FieldValue<>((PersistentField<T>) simpleRequirement.getField(), simpleRequirement.getValue()));
            }

            List<String> keysFound = this.getKeys(values.toArray(new FieldValue[0]));
            keys.addAll(keysFound);
        }

        return Lists.newArrayList(keys);
    }

    @SafeVarargs
    private final List<String> getKeys(FieldValue<T>... values) {
        ItemProfile<T> profile = this.repository.getProfile();
        PersistentField<T>[] fields = Arrays.stream(values)
          .map(FieldValue::getField)
          .toArray(PersistentField[]::new);

        if (profile.areKeys(fields)) {
            return Lists.newArrayList(this.getKey(values));
        }

        WrappedIndex<T> potentialIndex = profile.getIndex(fields);
        if (potentialIndex != null) {
            return this.getKeys(potentialIndex, values);
        }

        List<WrappedIndex<T>> indices = Lists.newArrayList();
        Set<PersistentField<T>> usedFields = Sets.newHashSet();
        for (WrappedIndex<T> index : profile.getIndices()) {
            List<PersistentField<T>> contains = Lists.newArrayList();
            for (PersistentField<T> field : fields) {
                if (!index.isField(field)) {
                    continue;
                }

                contains.add(field);
            }

            if (!index.areFields(contains.toArray(new PersistentField[0]))) {
                continue;
            }

            usedFields.addAll(contains);
            indices.add(index);
        }

        if (usedFields.size() != fields.length) {
            throw new IllegalArgumentException("Attempted query on un-indexed field!");
        }

        List<List<String>> keys = Lists.newArrayList();
        for (WrappedIndex<T> index : indices) {
            keys.add(this.getKeys(index, values));
        }

        return this.getKeys(keys);
    }

    private List<String> getKeys(List<List<String>> lists) {
        if (lists.size() == 0) {
            throw new IllegalArgumentException("Empty list!");
        }

        if (lists.size() == 1) {
            return lists.get(0);
        }

        List<String> baseList = lists.remove(0);
        baseList.removeIf(item -> lists.stream().allMatch(list -> list.contains(item)));
        return baseList;
    }

    private List<String> getKeys(WrappedIndex<T> index, FieldValue<T>[] values) {
        String key = this.getIndexHashKey(index.getFields());

        StringBuilder builder = new StringBuilder();
        for (PersistentField<T> field : index.getFields()) {
            if (!builder.toString().isEmpty()) {
                builder.append(":");
            }

            Object value = this.repository.getArrayValue(field, values);
            builder.append(field.getType().toString(this.repository, field, value));
        }

        List<String> keys = Lists.newArrayList();

        String matcher = builder.toString();
        try (Jedis jedis = this.getResource()) {
            Map<String, String> indexMap = jedis.hgetAll(key);

            for (Entry<String, String> entry : indexMap.entrySet()) {
                if (!entry.getValue().equals(matcher)) {
                    continue;
                }

                keys.add(entry.getKey());
            }
        }

        return keys;
    }

    private void removeFromIndices(String... keys) {
        try (Jedis jedis = this.getResource()) {
            for (WrappedIndex<T> index : this.repository.getProfile().getIndices()) {
                String indexKey = this.getIndexHashKey(index.getFields());
                jedis.hdel(indexKey, keys);
            }
        }
    }

    private void addToIndices(String key, FieldValue<T>[] values) {
        for (WrappedIndex<T> index : this.repository.getProfile().getIndices()) {
            StringBuilder builder = new StringBuilder();
            for (PersistentField<T> field : index.getFields()) {
                if (!builder.toString().isEmpty()) {
                    builder.append(":");
                }

                builder.append(field.getType().toString(this.repository, field, this.repository.getArrayValue(field, values)));
            }

            this.addToIndex(index.getFields(), key, builder.toString());
        }
    }

    private void addToIndex(PersistentField<T>[] fields, String key, String value) {
        String indexKey = this.getIndexHashKey(fields);
        try (Jedis jedis = this.getResource()) {
            jedis.hmset(indexKey, ImmutableMap.of(key, value));
        }
    }

    private String getIndexHashKey(PersistentField<T>[] fields) {
        StringBuilder builder = new StringBuilder();
        for (PersistentField<T> field : fields) {
            if (!builder.toString().isEmpty()) {
                builder.append(":");
            }

            builder.append(field.getName());
        }

        return String.format(INDEX_HASH_FORMAT, this.table, builder.toString());
    }

    public static class RedisJoinWrapper<T> extends JoinWrapper {

        private RedisAccessor<T> accessor;
        private ItemRepository<?> repository;

        public RedisJoinWrapper(JoinWrapper wrapper, RedisAccessor<T> accessor) {
            super(wrapper);
            this.accessor = accessor;

            // This repository must already exist. We do not have sufficient data to create it. A
            // Join should be on something else that already exists either way, so it is not a major
            // flaw in the system
            this.repository = Repositories.getRepository(DatabaseType.REDIS, this.getTable(), null, this.getRepository());

            if (this.repository == null) {
                throw new IllegalArgumentException("Could not find repository for join " + this + "!");
            }
        }

        public ItemRepository<?> getRepositoryInstance() {
            return repository;
        }

        public FieldValue<?>[] getValues(FieldValue<T>[] values) {
            Object localFieldValue = this.accessor.repository.getArrayValue(this.accessor.repository.getProfile().resolveField(this.getLocalField()), values);

            ResponseEntity<?> response = this.repository.newQuery()
              .where().equals(this.getRemoteField(), localFieldValue).closeAll()
              .findFirstSync();

            return response.getValues();
        }
    }
}
