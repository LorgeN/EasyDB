package net.lorgen.easydb.query.req;

import net.lorgen.easydb.PersistentField;
import net.lorgen.easydb.StorageManager;
import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.Operator;
import net.lorgen.easydb.query.QueryBuilder;

public class RequirementBuilder<T extends StoredItem> {

    private StorageManager<T> manager;

    private QueryRequirement req;

    private int level;
    private Operator returnOp;
    private QueryBuilder<T> builder;
    private RequirementBuilder<T> returnTo;

    public RequirementBuilder(StorageManager<T> manager, QueryBuilder<T> builder) {
        this.manager = manager;
        this.builder = builder;
    }

    public RequirementBuilder(StorageManager<T> manager, int level, Operator returnOp, RequirementBuilder<T> returnTo) {
        this.manager = manager;
        this.level = level;
        this.returnOp = returnOp;
        this.returnTo = returnTo;
    }

    public RequirementBuilder<T> andOpen() {
        return new RequirementBuilder<>(this.manager, this.level + 1, Operator.AND, this);
    }

    public RequirementBuilder<T> orOpen() {
        return new RequirementBuilder<>(this.manager, this.level + 1, Operator.OR, this);
    }

    public RequirementBuilder<T> closeCurrent() {
        if (this.returnTo == null) {
            throw new IllegalStateException("Already at top level!");
        }

        if (this.req instanceof CombinedRequirement) {
            ((CombinedRequirement) this.req).setWrap(true);
        }

        switch (this.returnOp) {
            case AND:
                this.returnTo.and(this.req);
                break;
            case OR:
                this.returnTo.or(this.req);
        }

        return this.returnTo;
    }

    public QueryBuilder<T> closeAll() {
        if (this.builder == null) {
            return this.returnTo.closeAll();
        }

        return this.builder.setRequirement(this.req);
    }

    public RequirementBuilder<T> andGreaterThanOrEqualTo(String fieldName, Object value) {
        return this.andGreaterThanOrEqualTo(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> orGreaterThanOrEqualTo(String fieldName, Object value) {
        return this.orGreaterThanOrEqualTo(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> andGreaterThanOrEqualTo(PersistentField<T> field, Object value) {
        return this.and(new SimpleRequirement(field, Operator.GREATER_THAN_OR_EQUAL_TO, value));
    }

    public RequirementBuilder<T> orGreaterThanOrEqualTo(PersistentField<T> field, Object value) {
        return this.or(new SimpleRequirement(field, Operator.GREATER_THAN_OR_EQUAL_TO, value));
    }

    public RequirementBuilder<T> andGreaterThan(String fieldName, Object value) {
        return this.andGreaterThan(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> orGreaterThan(String fieldName, Object value) {
        return this.orGreaterThan(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> andGreaterThan(PersistentField<T> field, Object value) {
        return this.and(new SimpleRequirement(field, Operator.GREATER_THAN, value));
    }

    public RequirementBuilder<T> orGreaterThan(PersistentField<T> field, Object value) {
        return this.or(new SimpleRequirement(field, Operator.GREATER_THAN, value));
    }

    public RequirementBuilder<T> andLessThanOrEqualTo(String fieldName, Object value) {
        return this.andLessThanOrEqualTo(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> orLessThanOrEqualTo(String fieldName, Object value) {
        return this.orLessThanOrEqualTo(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> andLessThanOrEqualTo(PersistentField<T> field, Object value) {
        return this.and(new SimpleRequirement(field, Operator.LESS_THAN_OR_EQUAL_TO, value));
    }

    public RequirementBuilder<T> orLessThanOrEqualTo(PersistentField<T> field, Object value) {
        return this.or(new SimpleRequirement(field, Operator.LESS_THAN_OR_EQUAL_TO, value));
    }

    public RequirementBuilder<T> andLessThan(String fieldName, Object value) {
        return this.andLessThan(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> orLessThan(String fieldName, Object value) {
        return this.orLessThan(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> andLessThan(PersistentField<T> field, Object value) {
        return this.and(new SimpleRequirement(field, Operator.LESS_THAN, value));
    }

    public RequirementBuilder<T> orLessThan(PersistentField<T> field, Object value) {
        return this.or(new SimpleRequirement(field, Operator.LESS_THAN, value));
    }

    public RequirementBuilder<T> andNotEquals(String fieldName, Object value) {
        return this.andNotEquals(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> orNotEquals(String fieldName, Object value) {
        return this.orNotEquals(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> andNotEquals(PersistentField<T> field, Object value) {
        return this.and(new SimpleRequirement(field, Operator.NOT_EQUALS, value));
    }

    public RequirementBuilder<T> orNotEquals(PersistentField<T> field, Object value) {
        return this.or(new SimpleRequirement(field, Operator.NOT_EQUALS, value));
    }

    public RequirementBuilder<T> andEquals(String fieldName, Object value) {
        return this.andEquals(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> orEquals(String fieldName, Object value) {
        return this.orEquals(this.resolve(fieldName), value);
    }

    public RequirementBuilder<T> andEquals(PersistentField<T> field, Object value) {
        return this.and(new SimpleRequirement(field, Operator.EQUALS, value));
    }

    public RequirementBuilder<T> orEquals(PersistentField<T> field, Object value) {
        return this.or(new SimpleRequirement(field, Operator.EQUALS, value));
    }

    public RequirementBuilder<T> and(QueryRequirement req) {
        if (this.req == null) {
            this.req = req;
            return this;
        }

        this.req = new CombinedRequirement(this.req, Operator.AND, req);
        return this;
    }

    public RequirementBuilder<T> or(QueryRequirement req) {
        if (this.req == null) {
            this.req = req;
            return this;
        }

        this.req = new CombinedRequirement(this.req, Operator.OR, req);
        return this;
    }

    public RequirementBuilder<T> andKeysAre(T object) {
        PersistentField<T>[] keys = this.manager.getProfile().getKeys();
        // We have to move down 1 level since we wish to wrap it
        RequirementBuilder<T> down = this.andOpen();

        for (PersistentField<T> key : keys) {
            down.andEquals(key, key.get(object));
        }

        return down.closeCurrent(); // Returns this
    }

    public RequirementBuilder<T> orKeysAre(T object) {
        PersistentField<T>[] keys = this.manager.getProfile().getKeys();
        // We have to move down 1 level since we wish to wrap it
        RequirementBuilder<T> down = this.orOpen();

        for (PersistentField<T> key : keys) {
            down.andEquals(key, key.get(object));
        }

        return down.closeCurrent(); // Returns this
    }

    public RequirementBuilder<T> andKeysAre(Object... values) {
        PersistentField<T>[] keys = this.manager.getProfile().getKeys();
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Given values do not correspond correctly to the keys of " +
              this.manager.getTypeClass().getSimpleName() + "! Expected length " + keys.length + ", actual length " +
              values.length + "!");
        }

        // No other arguments in this builder, don't need to move down (another) level
        if (this.req == null) {
            for (int i = 0; i < keys.length; i++) {
                this.andEquals(keys[i], values[i]);
            }

            return this;
        }

        // We have to move down 1 level since we wish to wrap it
        RequirementBuilder<T> down = this.andOpen();

        for (int i = 0; i < keys.length; i++) {
            down.andEquals(keys[i], values[i]);
        }

        return down.closeCurrent(); // Returns this
    }

    public RequirementBuilder<T> orKeysAre(Object... values) {
        PersistentField<T>[] keys = this.manager.getProfile().getKeys();
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Given values do not correspond correctly to the keys of " +
              this.manager.getTypeClass().getSimpleName() + "! Expected length " + keys.length + ", actual length " +
              values.length + "!");
        }

        // No other arguments in this builder, don't need to move down (another) level
        if (this.req == null) {
            for (int i = 0; i < keys.length; i++) {
                this.andEquals(keys[i], values[i]);
            }

            return this;
        }

        // We have to move down 1 level since we wish to wrap it
        RequirementBuilder<T> down = this.orOpen();

        for (int i = 0; i < keys.length; i++) {
            down.andEquals(keys[i], values[i]);
        }

        return down.closeCurrent(); // Returns this
    }

    // Internals

    private PersistentField<T> resolve(String name) {
        PersistentField<T> field = this.manager.getProfile().resolveField(name);
        if (field == null) {
            throw new IllegalArgumentException("Couldn't find field \"" + name + "\" in class " +
              this.manager.getTypeClass().getSimpleName() + "!");
        }

        return field;
    }
}
