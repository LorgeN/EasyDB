package net.lorgen.easydb.access.redis;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.Repositories;
import net.lorgen.easydb.WrappedIndex;
import net.lorgen.easydb.access.ListenableTypeAccessor;
import net.lorgen.easydb.connection.ConnectionRegistry;
import net.lorgen.easydb.exception.DeleteQueryException;
import net.lorgen.easydb.exception.DropException;
import net.lorgen.easydb.exception.FindQueryException;
import net.lorgen.easydb.exception.SaveQueryException;
import net.lorgen.easydb.exception.SetUpException;
import net.lorgen.easydb.field.FieldValue;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.interact.join.JoinWrapper;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.query.Operator;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.req.QueryRequirement;
import net.lorgen.easydb.query.req.SimpleRequirement;
import net.lorgen.easydb.query.traverse.RequirementCase;
import net.lorgen.easydb.query.traverse.RequirementTraverser;
import net.lorgen.easydb.query.response.Response;
import net.lorgen.easydb.util.UtilLog;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.Collection;
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
public class RedisAccessor<T> extends ListenableTypeAccessor<T> {

    /**
     * The format of the key in the Redis database
     */
    private static final String AUTO_INCREMENT_FORMAT = "%s:auto_increment";

    private static final String INDEX_HASH_FORMAT = "%s:index:(fields[%s])";

    private static final String STORE_FORMAT = "%s:value(%s)";

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

    private final String table;
    private final RedisConfiguration configuration;
    private final RedisJoinWrapper[] joinWrappers;

    public RedisAccessor(RedisConfiguration config, ItemRepository<T> repository, String table) {
        super(repository);
        this.table = table;
        this.configuration = config;
        this.joinWrappers = Arrays.stream(repository.getProfile().getJoins())
          .map(wrapper -> new RedisJoinWrapper(wrapper, this))
          .toArray(RedisJoinWrapper[]::new);

        this.setUp();
    }

    private void info(String msg) {
        UtilLog.info("Redis - " + this.table + ": " + msg);
    }

    private Jedis getResource() {
        return ConnectionRegistry.getInstance().<Jedis>getPool(this.configuration).getConnection();
    }

    @Override
    public ItemProfile<T> getProfile() {
        return this.getRepository().getProfile();
    }

    @Override
    public boolean isSearchable(PersistentField<T> field) {
        boolean result = field.isIndex() || field.isStorageKey();
        this.info("Checking if " + field + " is searchable! Result: " + result);
        return result;
    }

    @Override
    public void setUpInternal() {
        this.info("Setting up internals for redis accessor!");
        if (this.getRepository().getProfile().getAutoIncrementField() == null) {
            this.info("No auto increment field, thus no setup is required!");
            return;
        }

        String key = String.format(AUTO_INCREMENT_FORMAT, this.table);
        this.info("Auto increment key is \"" + key + "\"!");

        try (Jedis jedis = this.getResource()) {
            if (jedis.exists(key)) {
                this.info("Auto increment key already exists. Completed set up.");
                return;
            }

            jedis.set(key, "0");
            this.info("Auto increment value set to 0. Completed set up.");
        } catch (Throwable t) {
            throw new SetUpException(t);
        }
    }

    @Override
    public Response<T> findFirstInternal(Query<T> query) {
        try {
            if (query.getRequirement() == null) {
                this.info("No requirement found! Getting first value.");
                return this.getFirst();
            }

            this.info("Parsing requirement and fetching first matching value!");
            return this.getFirst(query.getRequirement());
        } catch (Throwable t) {
            throw new FindQueryException(t, query);
        }
    }

    @Override
    public List<Response<T>> findAllInternal(Query<T> query) {
        try {
            if (query.getRequirement() == null) {
                this.info("No requirement found! Getting all values!");
                return this.getAll();
            }

            this.info("Parsing requirement and fetching all values matching!");
            return this.getAll(query.getRequirement());
        } catch (Throwable t) {
            throw new FindQueryException(t, query);
        }
    }

    @Override
    public void saveOrUpdateInternal(Query<T> query) {
        try {
            this.info("Saving values " + UtilLog.format(query.getValues()) + "...");

            // In this case we first have to find all the keys we wish to modify. This
            // means we have to find all matching keys.
            if (query.getRequirement() != null) {
                this.info("Requirement found! Looking for all matching keys, then modifying values!");
                FieldValue<T>[] newValues = query.getValues();
                List<String> keys = this.getKeys(query.getRequirement());

                this.info("Iterating through keys...");
                for (String key : keys) {
                    this.info("Updating values for \"" + key + "\"!");
                    FieldValue<T>[] currentValues = this.getValues(key);
                    for (FieldValue<T> newValue : newValues) {
                        this.info("Updating array value for \"" + newValue.getField().getName() + "\" to " + newValue.getValue() + "!");
                        this.getRepository().updateArrayValue(newValue.getField(), newValue.getValue(), currentValues);
                    }

                    // Current values have been updated
                    this.insertIntoHash(key, currentValues);
                }

                this.info("Query complete.");
                // Query completed
                return;
            }

            this.info("No requirement found. Using keys from query.");
            String thisKey = this.getKey(query.getValues());

            // We now have a new T instance to save
            // First; Verify all unique fields:
            this.info("Verifying that there exists no duplicate unique fields...");
            for (WrappedIndex<T> index : this.getRepository().getProfile().getUniqueIndices()) {
                FieldValue<T>[] values = Arrays.stream(index.getFields())
                  .map(field -> new FieldValue<>(field, this.getRepository().getArrayValue(field, query.getValues())))
                  .toArray(FieldValue[]::new);

                List<String> keys = this.getKeys(values);
                if (keys.isEmpty()) {
                    this.info("Checked " + index + ". No duplicates found.");
                    continue;
                }

                // This would mean we are working with the same object,
                // and we of course need to be able to update it
                if (keys.size() == 1 && keys.get(0).equals(thisKey)) {
                    this.info("Found match for " + index + " but was this object.");
                    continue;
                }

                throw new IllegalArgumentException("Unique index \"" + index + "\" is same as another entry!");
            }

            // Then; Save it
            this.insertIntoHash(query.getObjectInstance(), query.getValues());
        } catch (Throwable t) {
            throw new SaveQueryException(t, query);
        }
    }

    @Override
    public void deleteInternal(Query<T> query) {
        if (query.getRequirement() == null) {
            this.info("No requirement given.");
            this.drop();
            return;
        }

        String[] keys = this.getKeys(query.getRequirement()).toArray(new String[0]);

        if (keys.length == 0) {
            this.info("No matching keys found. Nothing to delete.");
            return;
        }

        this.info("Deleting values...");
        try (Jedis jedis = this.getResource()) {
            jedis.del(keys);
        } catch (Throwable t) {
            throw new DeleteQueryException(t, query);
        }

        this.info("Finish deleting values. Removing from indices...");
        this.removeFromIndices(keys);
        this.info("Query complete.");
    }

    @Override
    public void dropInternal() {
        this.info("Dropping all values!");
        try (Jedis jedis = this.getResource()) {
            Set<String> keys = jedis.keys(String.format(STORE_FORMAT, this.table, "*"));
            this.info("Found " + keys.size() + " matching key(s)!");
            if (!keys.isEmpty()) {
                this.info("Deleting found values!");
                jedis.del(keys.toArray(new String[0]));
            }

            this.info("Deleting all index hashes...");
            for (WrappedIndex<T> index : this.getRepository().getProfile().getIndices()) {
                this.info("Deleting " + index + " hash...");
                jedis.del(this.getIndexHashKey(index.getFields()));
            }

            this.info("Completed deleting index hashes.");
            if (this.getRepository().getProfile().getAutoIncrementField() != null) {
                this.info("Deleting auto increment field.");
                jedis.del(String.format(AUTO_INCREMENT_FORMAT, this.table));
            }

            this.info("Query complete.");
        } catch (Throwable t) {
            throw new DropException(t);
        }
    }

    public int getNextAutoIncrementValue() {
        this.info("Fetching next auto increment value...");
        String key = String.format(AUTO_INCREMENT_FORMAT, this.table);

        try (Jedis jedis = this.getResource()) {
            int autoIncr = Math.toIntExact(jedis.incr(key));
            this.info("Next value is " + autoIncr + "!");
            return autoIncr;
        }
    }

    // Internals

    private List<Response<T>> getAll() {
        this.info("Fetching all values stored...");
        try (Jedis jedis = this.getResource()) {
            Set<String> keys = jedis.keys(String.format(STORE_FORMAT, this.table, "*"));
            this.info("Found " + keys.size() + " key(s).");
            return this.getAll(keys);
        }
    }

    private Response<T> getFirst() {
        this.info("Fetching first value stored...");
        try (Jedis jedis = this.getResource()) {
            Set<String> keys = jedis.keys(String.format(STORE_FORMAT, this.table, "*"));
            this.info("Found " + keys.size() + " key(s).");
            String key = Iterables.getFirst(keys, null);
            if (key == null) {
                this.info("Returning empty result.");
                return new Response<>(this.getRepository().getProfile());
            }

            this.info("Getting value at \"" + key + "\"...");
            return this.getObject(key);
        }
    }

    private List<Response<T>> getAll(QueryRequirement requirement) {
        this.info("Fetching all for requirement " + requirement);
        List<String> keys = this.getKeys(requirement);
        return this.getAll(keys);
    }

    private List<Response<T>> getAll(Collection<String> keys) {
        List<Response<T>> list = Lists.newArrayList();
        for (String key : keys) {
            list.add(this.getObject(key));
        }

        this.info("Completed fetching " + list.size() + " element(s)!");
        return list;
    }

    private Response<T> getFirst(QueryRequirement requirement) {
        List<String> keys = this.getKeys(requirement);
        if (keys.size() == 0) {
            this.info("Returning empty response.");
            return new Response<>(this.getRepository().getProfile());
        }

        this.info("Fetching object from " + keys.get(0) + "!");
        return this.getObject(keys.get(0));
    }

    private Response<T> getObject(String key) {
        this.info("Fetching \"" + key + "\"...");
        FieldValue<T>[] values = this.getValues(key);
        if (values == null) {
            this.info("No value found!");
            return new Response<>(this.getRepository().getProfile());
        }

        this.info("Query complete.");
        return new Response<>(this.getRepository().getProfile(), values);
    }

    private FieldValue<T>[] getValues(String key) {
        this.info("Getting values at \"" + key + "\"...");
        List<FieldValue<T>> list = Lists.newArrayList();

        try (Jedis jedis = this.getResource()) {
            Map<String, String> valueMap = jedis.hgetAll(key);
            if (valueMap.isEmpty()) {
                this.info("No values found!");
                return null;
            }

            this.info("Parsing values...");
            for (Entry<String, String> entry : valueMap.entrySet()) {
                PersistentField<T> field = this.getRepository().getProfile().resolveField(entry.getKey());;
                if (field == null) {
                    throw new IllegalArgumentException("Unknown field \"" + entry.getKey() + "\"!");
                }

                Object value = field.getType().fromString(this.getRepository(), field, entry.getValue());
                list.add(new FieldValue<>(field, value));
                this.info("Value for field " + field + " is " + value + "!");
            }

            this.info("Handling joins...");
            // Handle joins
            FieldValue<T>[] values = list.toArray(new FieldValue[0]);

            // First; Get all values
            List<FieldValue> joinValues = Lists.newArrayList();
            for (RedisJoinWrapper joinWrapper : this.joinWrappers) {
                Collections.addAll(joinValues, joinWrapper.getValues(values));
            }

            // Then; Compute into usable data
            fields:
            for (PersistentField<T> field : this.getRepository().getProfile().getFields()) {
                if (!field.isJoined()) {
                    continue;
                }

                for (FieldValue value : joinValues) {
                    if (!value.getField().getName().equals(field.getName())) {
                        continue;
                    }

                    this.info("Found join value for " + field + " in " + value + "!");
                    list.add(new FieldValue<>(field, value.getValue()));
                    continue fields;
                }
            }
        }

        this.info("Query complete.");
        return list.toArray(new FieldValue[0]);
    }

    private String getKey(FieldValue<T>[] values) {
        this.info("Extracting key from " + UtilLog.format(values) + "...");
        PersistentField<T>[] keys = this.getRepository().getProfile().getKeys();
        StringBuilder builder = new StringBuilder();

        for (PersistentField<T> key : keys) {
            if (!builder.toString().isEmpty()) {
                builder.append(".");
            }

            Object value = this.getRepository().getArrayValue(key, values);
            if (value == null) {
                throw new IllegalArgumentException("Missing value for key \"" + key.getName() + "\"!");
            }

            builder.append(key.getType().toString(this.getRepository(), key, value));
        }

        String key = String.format(STORE_FORMAT, this.table, builder.toString());
        this.info("Key found as \"" + key + "\"!");
        return key;
    }

    private void insertIntoHash(Optional<T> objectInstance, FieldValue<T>[] values) {
        this.info("Inserting into hash; " + UtilLog.format(values) + "...");
        PersistentField<T> autoIncrement = this.getRepository().getProfile().getAutoIncrementField();
        if (autoIncrement != null && ((int) this.getRepository().getArrayValue(autoIncrement, values)) == 0) {
            this.info("Updating auto increment value in cache...");
            int value = this.getNextAutoIncrementValue();
            objectInstance.ifPresent(tValue -> autoIncrement.set(tValue, value));
            this.getRepository().updateArrayValue(autoIncrement, value, values);
        }

        this.insertIntoHash(values);
    }

    private void insertIntoHash(FieldValue<T>[] values) {
        PersistentField<T> autoIncrement = this.getRepository().getProfile().getAutoIncrementField();
        if (autoIncrement != null && ((int) this.getRepository().getArrayValue(autoIncrement, values)) == 0) {
            throw new IllegalArgumentException("Missing value for auto increment field \"" + autoIncrement.getName() + "\"");
        }

        this.insertIntoHash(this.getKey(values), values);
    }

    private void insertIntoHash(String key, FieldValue<T>[] values) {
        Map<String, String> map = Maps.newHashMap();
        for (FieldValue<T> value : values) {
            if (value.getField().isExternalStore()) {
                continue;
            }

            PersistentField<T> field = value.getField();
            map.put(field.getName(), field.getType().toString(this.getRepository(), field, value.getValue()));
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
        this.info("Finding all keys matching " + requirement + "...");
        Set<String> keys = Sets.newHashSet();
        RequirementTraverser traverser = new RequirementTraverser(requirement);
        this.info("Traverser found " + traverser.getCases().size() + " case(s).");
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
        this.info("Getting all keys matching " + UtilLog.format(values) + "...");
        ItemProfile<T> profile = this.getRepository().getProfile();
        PersistentField<T>[] fields = Arrays.stream(values)
          .map(FieldValue::getField)
          .toArray(PersistentField[]::new);

        if (profile.areKeys(fields)) {
            this.info("Values are keys!");
            return Lists.newArrayList(this.getKey(values));
        }

        WrappedIndex<T> potentialIndex = profile.getIndex(fields);
        if (potentialIndex != null) {
            this.info("Matching index found!");
            return this.getKeys(potentialIndex, values);
        }

        this.info("Failed to find optimal strategy to find keys. Consider making into index if this query is common.");
        this.info("Attempting to use fallback method, and combine indices to find desired keys.");

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

        this.info("Found " + indices.size() + " indices to use. Finding matching keys from each index...");
        List<List<String>> keys = Lists.newArrayList();
        for (WrappedIndex<T> index : indices) {
            keys.add(this.getKeys(index, values));
        }

        List<String> finalKeys = this.getKeys(keys);
        this.info("Successfully found " + finalKeys.size() + " key(s).");
        return finalKeys;
    }

    private List<String> getKeys(List<List<String>> lists) {
        if (lists.size() == 0) {
            throw new IllegalArgumentException("Empty list!");
        }

        if (lists.size() == 1) {
            return lists.get(0);
        }

        List<String> baseList = lists.remove(0);
        baseList.removeIf(item -> !lists.stream().allMatch(list -> list.contains(item)));
        return baseList;
    }

    private List<String> getKeys(WrappedIndex<T> index, FieldValue<T>[] values) {
        this.info("Finding key from index " + index + " to match values " + UtilLog.format(values) + "...");
        String key = this.getIndexHashKey(index.getFields());

        StringBuilder builder = new StringBuilder();
        for (PersistentField<T> field : index.getFields()) {
            if (!builder.toString().isEmpty()) {
                builder.append(":");
            }

            Object value = this.getRepository().getArrayValue(field, values);
            if (value == null) {
                throw new IllegalArgumentException("Missing value for field " + field + " in index " + index + "!");
            }

            builder.append(field.getType().toString(this.getRepository(), field, value));
        }

        List<String> keys = Lists.newArrayList();

        String matcher = builder.toString();
        this.info("Looking for keys to match \"" + matcher + "\"...");
        try (Jedis jedis = this.getResource()) {
            Map<String, String> indexMap = jedis.hgetAll(key);

            for (Entry<String, String> entry : indexMap.entrySet()) {
                if (!entry.getValue().equals(matcher)) {
                    continue;
                }

                this.info("Found match at \"" + entry.getKey() + "\".");
                keys.add(entry.getKey());
            }
        }

        this.info("Found " + keys.size() + " matching key(s)");
        return keys;
    }

    private void removeFromIndices(String... keys) {
        this.info("Removing " + Arrays.toString(keys) + " from indices...");
        try (Jedis jedis = this.getResource()) {
            for (WrappedIndex<T> index : this.getRepository().getProfile().getIndices()) {
                String indexKey = this.getIndexHashKey(index.getFields());
                jedis.hdel(indexKey, keys);
            }
        }
    }

    private void addToIndices(String key, FieldValue<T>[] values) {
        this.info("Adding value at \"" + key + "\" to indices...");
        for (WrappedIndex<T> index : this.getRepository().getProfile().getIndices()) {
            StringBuilder builder = new StringBuilder();
            for (PersistentField<T> field : index.getFields()) {
                if (!builder.toString().isEmpty()) {
                    builder.append(":");
                }

                builder.append(field.getType().toString(this.getRepository(), field, this.getRepository().getArrayValue(field, values)));
            }

            this.addToIndex(index.getFields(), key, builder.toString());
        }
    }

    private void addToIndex(PersistentField<T>[] fields, String key, String value) {
        this.info("Adding \"" + key + "\" with values \"" + value + "\" to index with fields " + Arrays.toString(fields));
        String indexKey = this.getIndexHashKey(fields);
        try (Jedis jedis = this.getResource()) {
            jedis.hmset(indexKey, ImmutableMap.of(key, value));
        }
    }

    private String getIndexHashKey(PersistentField<T>[] fields) {
        StringBuilder builder = new StringBuilder();
        for (PersistentField<T> field : fields) {
            if (!builder.toString().isEmpty()) {
                builder.append(".");
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

            if (this.getRepository() == null) {
                throw new IllegalArgumentException("Could not find repository for join " + this + "! Make sure to initialize this first!");
            }
        }

        public ItemRepository<?> getRepositoryInstance() {
            return repository;
        }

        public FieldValue<?>[] getValues(FieldValue<T>[] values) {
            Object localFieldValue = this.accessor.getRepository().getArrayValue(this.accessor.getRepository().getProfile().resolveField(this.getLocalField()), values);

            Response<?> response = this.repository.newQuery()
              .where().equals(this.getRemoteField(), localFieldValue).closeAll()
              .findFirst();

            return response.getValues();
        }
    }
}
