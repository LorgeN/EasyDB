package net.lorgen.easydb.interact.external;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.Repositories;
import net.lorgen.easydb.access.ListenableTypeAccessor;
import net.lorgen.easydb.access.event.AccessorDeleteEvent;
import net.lorgen.easydb.access.event.AccessorRespondEvent;
import net.lorgen.easydb.access.event.AccessorSaveEvent;
import net.lorgen.easydb.event.EventHandler;
import net.lorgen.easydb.event.Listener;
import net.lorgen.easydb.field.FieldValue;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.profile.ItemProfileBuilder;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.QueryBuilder;
import net.lorgen.easydb.query.req.RequirementBuilder;
import net.lorgen.easydb.response.ResponseEntity;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

public class ExternalCollectionListener<T> implements Listener {

    private static final String INDEX_FIELD = "collection_index";

    private PersistentField<T> field;

    // We use one of these
    private PersistentField<T>[] keys;
    private Map<String, String> mappedFields;

    private boolean addedKeys;
    private ItemRepository<?> repository;

    public ExternalCollectionListener(ListenableTypeAccessor<T> accessor, PersistentField<T> field) {
        this.field = field;

        DatabaseType type = DatabaseType.fromAccessor(accessor);

        try {
            new ItemProfile<>(field.getTypeClass()); // Draw up a profile, so we can see what we're working with

            this.repository = Repositories.getOrCreateRepository(null, type, field.getExternalTable(), field.getTypeClass(), field.getRepository());
        } catch (IllegalArgumentException e) { // Exception thrown if no keys, so we need to create some
            ItemProfileBuilder builder = new ItemProfileBuilder<>(this.field.getTypeClass());

            for (PersistentField<T> key : accessor.getProfile().getKeys()) {
                builder.newField().copyAttributes(key).setAsKey(true).buildAndAddField();
            }

            builder.newField().setName(INDEX_FIELD).setTypeClass(Integer.class).setAsKey(true).buildAndAddField()
              .fromTypeClass(); // Add the type class

            ItemProfile profile = builder.build();
            this.repository = Repositories.createRepository(null, type, field.getExternalTable(), field.getTypeClass(), field.getRepository(), profile);
            this.keys = accessor.getProfile().getKeys(); // Use these keys so we get the correct objects
            this.addedKeys = true;
        }

        if (this.repository == null) {
            throw new IllegalArgumentException("Couldn't find repository!");
        }

        if (this.keys != null) {
            return;
        }

        if (field.getKeyFields() != null && field.getKeyFields().length != 0) {
            this.keys = new PersistentField[field.getKeyFields().length];
            String[] keyFields = field.getKeyFields();

            for (int i = 0; i < keyFields.length; i++) {
                String keyField = keyFields[i];
                this.keys[i] = accessor.getProfile().resolveField(keyField);
            }
        } else {
            this.mappedFields = Maps.newHashMap();

            ItemProfile<T> originalProfile = accessor.getProfile();
            ItemProfile<?> localProfile = this.repository.getProfile();

            for (PersistentField<T> storedField : originalProfile.getStoredFields()) {
                PersistentField<?> similarField = localProfile.resolveField(storedField.getName());
                if (similarField == null) {
                    continue;
                }

                if (similarField.getType() != storedField.getType()) {
                    continue;
                }

                if (similarField.isExternalStore()) {
                    continue;
                }

                this.mappedFields.put(storedField.getName(), similarField.getName());
            }

            if (this.mappedFields.isEmpty()) {
                throw new IllegalArgumentException("No common fields found!");
            }
        }
    }

    public PersistentField<T> getField() {
        return field;
    }

    public ItemRepository<?> getRepository() {
        return repository;
    }

    @EventHandler
    public void onRespond(AccessorRespondEvent event) {
        ResponseEntity<T> entity = (ResponseEntity<T>) event.getEntity();
        RequirementBuilder<?> builder = this.repository.newQuery().where();

        if (this.keys != null) {
            Object[] values = new Object[this.keys.length];
            for (int i = 0; i < this.keys.length; i++) {
                PersistentField<T> field = this.keys[i];
                values[i] = entity.getValue(field.getName()).getValue();
            }

            builder.keysAre(values);
        } else {
            for (Entry<String, String> entry : this.mappedFields.entrySet()) {
                FieldValue<T> value = entity.getValue(entry.getKey());
                if (value.isEmpty()) {
                    throw new IllegalStateException("Couldn't find value for " + entry.getKey() + "!");
                }

                builder.andEquals(entry.getValue(), value.getValue());
            }
        }

        List values = builder.closeAll().findAllSync();
        Collection<?> collection = this.newInstance();
        if (collection == null) {
            return;
        }

        collection.addAll(values);
        entity.getValue(this.field.getName()).setValue(collection);
    }

    @EventHandler
    public void onSave(AccessorSaveEvent event) {
        if (!this.field.isUpdateOnSave()) {
            return;
        }

        Query<T> baseQuery = (Query<T>) event.getQuery();
        this.deleteAll(baseQuery); // I know, slow, but really the only way to handle it

        Collection<?> collection = (Collection<?>) baseQuery.getValue(this.field).getValue();
        Iterator<?> iterator = collection.iterator();
        for (int i = 0; i < collection.size(); i++) {
            Object value = iterator.next();
            QueryBuilder builder = this.repository.newQuery();

            if (this.addedKeys) {
                for (PersistentField<T> key : this.keys) {
                    builder.set(key.getName(), baseQuery.getValue(key.getName()));
                }

                builder.set(INDEX_FIELD, i);
            }

            builder.set(value).saveSync();
        }
    }

    @EventHandler
    public void onDelete(AccessorDeleteEvent event) {
        if (!this.field.isUpdateOnSave()) {
            return;
        }

        Query<T> baseQuery = (Query<T>) event.getQuery();
        this.deleteAll(baseQuery);
    }

    // Internals

    private void deleteAll(Query<T> baseQuery) {
        if (this.addedKeys) {
            RequirementBuilder builder = this.repository.newQuery().where();
            for (PersistentField<T> key : this.keys) {
                builder.andEquals(key.getName(), baseQuery.getValue(key.getName()));
            }

            builder.closeAll().deleteSync();
            return;
        }

        // Hope to god that most people don't use this
        Collection<?> collection = (Collection<?>) baseQuery.getValue(this.field).getValue();
        Iterator<?> iterator = collection.iterator();
        for (int i = 0; i < collection.size(); i++) {
            Object value = iterator.next();
            this.repository.newQuery().where().keysAre(value).closeAll().deleteSync();
        }
    }

    private Collection<?> newInstance() {
        Class<? extends Collection> typeClass = (Class<? extends Collection>) this.field.getTypeClass();
        if (List.class.isAssignableFrom(typeClass)) {
            return Lists.newLinkedList();
        }

        if (Set.class.isAssignableFrom(typeClass)) {
            return Sets.newHashSet();
        }

        if (Queue.class.isAssignableFrom(typeClass)) {
            return Lists.newLinkedList();
        }

        throw new IllegalStateException("Unknown collection " + typeClass.getSimpleName() + "!");
    }
}
