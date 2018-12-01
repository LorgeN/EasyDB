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
import net.lorgen.easydb.event.Listener;
import net.lorgen.easydb.field.FieldValue;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.profile.ItemProfileBuilder;
import net.lorgen.easydb.query.req.RequirementBuilder;
import net.lorgen.easydb.response.ResponseEntity;

import java.util.Map;
import java.util.Map.Entry;

public class ExternalFieldListener<T> implements Listener {

    private PersistentField<T> field;

    // We use one of these
    private PersistentField<T>[] keys;
    private Map<String, String> mappedFields;

    private ItemRepository<?> repository;

    public ExternalFieldListener(ListenableTypeAccessor<T> accessor, PersistentField<T> field) {
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

            builder.fromTypeClass(); // Add the type class
            this.repository = Repositories.createRepository(null, type, field.getExternalTable(), field.getTypeClass(), field.getRepository(), builder.build());
            this.keys = accessor.getProfile().getKeys();
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

        Object value = builder.closeAll().findFirstSync();
        entity.getValue(this.field.getName()).setValue(value);
    }

    @EventHandler
    public void onSave(AccessorSaveEvent event) {
        // TODO
    }

    @EventHandler
    public void onDelete(AccessorDeleteEvent event) {
        // TODO
    }
}
