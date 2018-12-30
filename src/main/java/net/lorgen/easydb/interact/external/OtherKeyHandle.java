package net.lorgen.easydb.interact.external;

import com.google.common.collect.Lists;
import net.lorgen.easydb.access.ListenableTypeAccessor;
import net.lorgen.easydb.field.FieldValue;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.QueryBuilder;
import net.lorgen.easydb.query.req.RequirementBuilder;
import net.lorgen.easydb.response.Response;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public abstract class OtherKeyHandle<T> extends FieldHandle<T> {

    public OtherKeyHandle(ListenableTypeAccessor<T> accessor, PersistentField<T> field) {
        super(accessor, field);
    }

    public void save(Query<T> baseQuery) {
        Validate.notNull(baseQuery);

        Object value = baseQuery.getValue(this.getField()).getValue();
        this.save(baseQuery, value);
    }

    public void save(Query<T> baseQuery, Object fieldValue) {
        this.save(baseQuery, null, fieldValue);
    }

    public void save(Query<T> baseQuery, Object keyObject, Object fieldValue) {
        QueryBuilder builder = this.getRepository().newQuery();

        if (this.useAddedKeys()) {
            for (String key : this.getKeys()) {
                builder.set(key, baseQuery.getValue(key).getValue());
            }

            if (keyObject != null) {
                if (this.isPrimitive(keyObject.getClass())) {
                    builder.set(KEY_FIELD, keyObject);
                } else {
                    for (Field field : keyObject.getClass().getDeclaredFields()) {
                        if (Modifier.isTransient(field.getModifiers())) {
                            continue;
                        }

                        try {
                            builder.set(field.getName(), field.get(keyObject));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (this.isPrimitive()) {
            builder.set(VALUE_FIELD, fieldValue);
        } else {
            builder.set(fieldValue);
        }

        builder.saveSync();
    }

    public Object extractValue(Response entity) {
        return this.isPrimitive(this.getField().getTypeParameters()[0]) ? entity.getValue(VALUE_FIELD).getValue() : entity.getInstance();
    }

    public void delete(Query<T> baseQuery) {
        RequirementBuilder builder = this.getRepository().newQuery().where();

        if (this.useAddedKeys()) {
            for (String key : this.getKeys()) {
                builder.andEquals(key, baseQuery.getValue(key).getValue());
            }
        } else {
            builder.keysAreSameAs(baseQuery.getValue(this.getField()).getValue());
        }

        builder.closeAll().deleteSync();
    }

    public Response getResponse(Response<T> entity) {
        return this.buildQuery(entity).findFirstSync();
    }

    public List<Response> getResponses(Response<T> entity) {
        return Lists.newArrayList(this.buildQuery(entity).findAllSync());
    }

    public QueryBuilder<?> buildQuery(Response<T> entity) {
        RequirementBuilder<?> builder = this.getRepository().newQuery().where();

        for (String key : this.getKeys()) {
            FieldValue<T> value = entity.getValue(key);
            if (value.isEmpty()) {
                throw new IllegalStateException("Couldn't find value for " + key + "!");
            }

            builder.andEquals(key, value.getValue());
        }

        return builder.closeAll();
    }
}
