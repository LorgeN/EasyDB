package net.lorgen.easydb.interact.external;

import com.google.common.collect.Maps;
import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.Repositories;
import net.lorgen.easydb.access.ListenableTypeAccessor;
import net.lorgen.easydb.access.event.AccessorDeleteEvent;
import net.lorgen.easydb.access.event.AccessorRespondEvent;
import net.lorgen.easydb.access.event.AccessorSaveEvent;
import net.lorgen.easydb.event.EventHandler;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.QueryBuilder;
import net.lorgen.easydb.response.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;

public class ExternalMapOtherKeyHandle<T> extends OtherKeyHandle<T> {

    public ExternalMapOtherKeyHandle(ListenableTypeAccessor<T> accessor, PersistentField<T> field) {
        super(accessor, field);

        DatabaseType type = this.getDatabaseType();
        String table = field.getExternalTable();
        Class<?> keyClass = field.getTypeParameters()[0];
        Class<?> valueClass = field.getTypeParameters()[1];
        Class<? extends ItemRepository> repoClass = field.getRepository();

        ItemProfile profile = this.buildWithLocalKeys(valueClass, keyClass);

        this.setRepository(Repositories.createRepository(null, type, table, valueClass, repoClass, profile));
        this.setUseAddedKeys(true);
        this.setToLocalKeys();

        if (this.getRepository() == null) {
            throw new IllegalArgumentException("Couldn't create repository!");
        }
    }

    @EventHandler
    public void onRespond(AccessorRespondEvent event) {
        Map map = this.newInstance();
        if (map == null) {
            return;
        }

        for (ResponseEntity entity : this.getResponses((ResponseEntity<T>) event.getEntity())) {
            Object key = this.extractKey(entity);
            Object value = this.extractValue(entity);

            map.put(key, value);
        }

        event.getEntity().getValue(this.getField().getName()).setValue(map);
    }

    @EventHandler
    public void onSave(AccessorSaveEvent event) {
        if (this.getField().isTransient()) {
            return;
        }

        Query<T> baseQuery = (Query<T>) event.getQuery();
        Class<?> keyClass = this.getField().getTypeParameters()[0];

        this.delete(baseQuery); // Removes any elements that have been removed from the map

        Map<?, ?> map = (Map) baseQuery.getValue(this.getField()).getValue();

        for (Entry entry : map.entrySet()) {
            QueryBuilder builder = this.getRepository().newQuery();

            for (String key : this.getKeys()) {
                builder.set(key, baseQuery.getValue(key).getValue());
            }

            for (Field field : keyClass.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                try {
                    builder.set(field.getName(), field.get(entry.getKey()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            
            builder.set(entry.getValue()).saveSync();
        }
    }

    @EventHandler
    public void onDelete(AccessorDeleteEvent event) {
        if (!this.getField().isTransient()) {
            return;
        }

        Query<T> baseQuery = (Query<T>) event.getQuery();
        this.delete(baseQuery);
    }

    // Internals

    private Object extractKey(ResponseEntity entity) {
        Class<?> keyClass = this.getField().getTypeParameters()[0];
        if (this.isPrimitive(keyClass)) {
            return entity.getValue(KEY_FIELD).getValue();
        }

        try {
            Object key = keyClass.newInstance();

            for (Field field : keyClass.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                field.set(key, entity.getValue(field.getName()).getValue());
            }

            return key;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<?, ?> newInstance() {
        Class<? extends Map> typeClass = (Class<? extends Map>) this.getField().getTypeClass();
        if (!typeClass.isInterface()) {
            try {
                return typeClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return Maps.newLinkedHashMap();
    }
}
