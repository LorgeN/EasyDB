package net.lorgen.easydb.access.redis;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.lorgen.easydb.DatabaseTypeAccessor;
import net.lorgen.easydb.FieldValue;
import net.lorgen.easydb.PersistentField;
import net.lorgen.easydb.StorageManager;
import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.StoredItemProfile;
import net.lorgen.easydb.WrappedIndex;
import net.lorgen.easydb.query.Operator;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.req.QueryRequirement;
import net.lorgen.easydb.query.req.SimpleRequirement;
import net.lorgen.easydb.query.traverse.RequirementCase;
import net.lorgen.easydb.query.traverse.RequirementTraverser;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Redis database type accessor
 *
 * @param <T> The type this accessor handles
 */
public class RedisAccessor<T extends StoredItem> implements DatabaseTypeAccessor<T> {

    /**
     * The format of the key in the Redis database
     */
    private static final String AUTO_INCREMENT_FORMAT = "auto_increment:%s";

    private static final String INDEX_HASH_FORMAT = "%s(index:%s)";

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

    private final JedisPool pool;
    private final StorageManager<T> manager;
    private final String table;

    public RedisAccessor(RedisConfiguration config, StorageManager<T> manager, String table) {
        this.manager = manager;
        this.table = table;

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxWaitMillis(1000);
        poolConfig.setMinIdle(5);
        poolConfig.setMaxTotal(20);

        String host = config.getHost();
        int port = config.getPort();
        String password = (config.getPassword() == null || config.getPassword().isEmpty()) ? null : config.getPassword();

        this.pool = new JedisPool(poolConfig, host, port, 2000, password);

        if (manager.getProfile().getAutoIncrementField() != null) {
            String key = String.format(AUTO_INCREMENT_FORMAT, this.table);

            try (Jedis jedis = this.pool.getResource()) {
                if (jedis.exists(key)) {
                    return;
                }

                jedis.set(key, "0");
            }
        }
    }

    @Override
    public T findFirst(Query<T> query) {
        if (query.getRequirement() == null) {
            return this.getFirst();
        }

        return this.getFirst(query.getRequirement());
    }

    @Override
    public List<T> findAll(Query<T> query) {
        if (query.getRequirement() == null) {
            return this.getAll();
        }

        return this.getAll(query.getRequirement());
    }

    @Override
    public void saveOrUpdate(Query<T> query) {
        // TODO
    }

    @Override
    public void delete(Query<T> query) {
        // TODO
    }

    public int getNextAutoIncrementValue() {
        String key = String.format(AUTO_INCREMENT_FORMAT, this.table);

        try (Jedis jedis = this.pool.getResource()) {
            return Math.toIntExact(jedis.incr(key));
        }
    }

    // Internals

    private List<T> getAll() {
        try (Jedis jedis = this.pool.getResource()) {
            Set<String> keys = jedis.keys(String.format(STORE_FORMAT, this.table, "*"));
            List<T> list = Lists.newArrayList();
            for (String key : keys) {
                list.add(this.getObject(key));
            }

            return list;
        }
    }

    private T getFirst() {
        try (Jedis jedis = this.pool.getResource()) {
            Set<String> keys = jedis.keys(String.format(STORE_FORMAT, this.table, "*"));
            String key = Iterables.getFirst(keys, null);
            if (key == null) {
                return null;
            }

            return this.getObject(key);
        }
    }

    private List<T> getAll(QueryRequirement requirement) {
        List<String> keys = this.getKeys(requirement);
        List<T> list = Lists.newArrayList();
        for (String key : keys) {
            list.add(this.getObject(key));
        }

        return list;
    }

    private T getFirst(QueryRequirement requirement) {
        List<String> keys = this.getKeys(requirement);
        if (keys.size() == 0) {
            return null;
        }

        return this.getObject(keys.get(0));
    }

    private T getObject(String key) {
        return this.manager.fromValues(this.getValues(key));
    }

    private FieldValue<T>[] getValues(String key) {
        List<FieldValue<T>> list = Lists.newArrayList();

        try (Jedis jedis = this.pool.getResource()) {
            for (Entry<String, String> entry : jedis.hgetAll(key).entrySet()) {
                PersistentField<T> field = this.manager.getProfile().resolveField(entry.getKey());
                if (field == null) {
                    throw new IllegalArgumentException("Unknown field \"" + entry.getKey() + "\"!");
                }

                Object value = field.getType().fromString(this.manager, field, entry.getValue());
                list.add(new FieldValue<>(field, value));
            }
        }

        return list.toArray(new FieldValue[0]);
    }

    private String getKey(FieldValue<T>[] values) {
        PersistentField<T>[] keys = this.manager.getProfile().getKeys();
        StringBuilder builder = new StringBuilder();

        for (PersistentField<T> key : keys) {
            if (!builder.toString().isEmpty()) {
                builder.append(":");
            }

            Object value = this.manager.getArrayValue(key, values);
            if (value == null) {
                throw new IllegalArgumentException("Missing value for key \"" + key.getName() + "\"!");
            }

            builder.append(key.getType().toString(this.manager, key, value));
        }

        return String.format(STORE_FORMAT, this.table, builder.toString());
    }

    private void insertIntoHash(T instance, FieldValue<T>[] values) {
        PersistentField<T> autoIncrement = this.manager.getProfile().getAutoIncrementField();
        if (autoIncrement != null && ((int) this.manager.getArrayValue(autoIncrement, values)) == 0) {
            int value = this.getNextAutoIncrementValue();
            autoIncrement.set(instance, value);
            this.manager.updateArrayValue(autoIncrement, value, values);
        }

        this.insertIntoHash(values);
    }

    private void insertIntoHash(FieldValue<T>[] values) {
        PersistentField<T> autoIncrement = this.manager.getProfile().getAutoIncrementField();
        if (autoIncrement != null && ((int) this.manager.getArrayValue(autoIncrement, values)) == 0) {
            throw new IllegalArgumentException("Missing value for auto increment field \"" + autoIncrement.getName() + "\"");
        }

        this.insertIntoHash(this.getKey(values), values);
    }

    private void insertIntoHash(String key, FieldValue<T>[] values) {
        Map<String, String> map = Maps.newHashMap();
        for (FieldValue<T> value : values) {
            PersistentField<T> field = value.getField();
            map.put(field.getName(), field.getType().toString(this.manager, field, value.getValue()));
        }

        this.addToIndices(key, values);
        this.insertIntoHash(key, map);
    }

    private void insertIntoHash(String key, Map<String, String> valuesByKey) {
        try (Jedis jedis = this.pool.getResource()) {
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

            keys.addAll(this.getKeys(values.toArray(new FieldValue[0])));
        }

        return Lists.newArrayList(keys);
    }

    private List<String> getKeys(FieldValue<T>[] values) {
        StoredItemProfile<T> profile = this.manager.getProfile();
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

            Object value = this.manager.getArrayValue(field, values);
            builder.append(field.getType().toString(this.manager, field, value));
        }

        ScanParams params = new ScanParams()
          .count(100)
          .match(builder.toString());

        List<String> keys = Lists.newArrayList();
        try (Jedis jedis = this.pool.getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;

            // do-while instead of while because we want it to execute at least once
            do {
                ScanResult<Entry<String, String>> result = jedis.hscan(key, cursor, params);
                for (Entry<String, String> entry : result.getResult()) {
                    keys.add(entry.getKey());
                }

                cursor = result.getStringCursor();
            } while (!cursor.equals(ScanParams.SCAN_POINTER_START)); // This means we are done
        }

        return keys;
    }

    private void removeFromIndices(String key) {
        try (Jedis jedis = this.pool.getResource()) {
            for (WrappedIndex<T> index : this.manager.getProfile().getIndices()) {
                String indexKey = this.getIndexHashKey(index.getFields());
                jedis.hdel(indexKey, key);
            }
        }
    }

    private void addToIndices(String key, FieldValue<T>[] values) {
        for (WrappedIndex<T> index : this.manager.getProfile().getIndices()) {
            StringBuilder builder = new StringBuilder();
            for (PersistentField<T> field : index.getFields()) {
                if (!builder.toString().isEmpty()) {
                    builder.append(":");
                }

                builder.append(field.getType().toString(this.manager, field, this.manager.getArrayValue(field, values)));
            }

            this.addToIndex(index.getFields(), key, builder.toString());
        }
    }

    private void addToIndex(PersistentField<T>[] fields, String key, String value) {
        String indexKey = this.getIndexHashKey(fields);
        try (Jedis jedis = this.pool.getResource()) {
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
}
