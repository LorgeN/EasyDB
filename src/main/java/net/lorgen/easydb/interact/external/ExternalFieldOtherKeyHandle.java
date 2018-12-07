package net.lorgen.easydb.interact.external;

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
import net.lorgen.easydb.response.Response;

public class ExternalFieldOtherKeyHandle<T> extends OtherKeyHandle<T> {

    public ExternalFieldOtherKeyHandle(ListenableTypeAccessor<T> accessor, PersistentField<T> field) {
        super(accessor, field);

        DatabaseType type = this.getDatabaseType();
        String table = field.getExternalTable();
        Class<?> typeClass = field.getTypeClass();
        Class<? extends ItemRepository> repoClass = field.getRepository();

        if (!this.hasKeys(typeClass)) {
            ItemProfile profile = this.buildWithLocalKeys(typeClass);

            this.setRepository(Repositories.createRepository(null, type, table, typeClass, repoClass, profile));
            this.setUseAddedKeys(true);
            this.setToLocalKeys();

            if (this.getRepository() == null) {
                throw new IllegalArgumentException("Couldn't create repository!");
            }

            return;
        }

        this.setRepository(Repositories.getOrCreateRepository(null, type, table, typeClass, repoClass));

        if (this.getRepository() == null) {
            throw new IllegalArgumentException("Couldn't find repository!");
        }

        this.computeKeys();
    }

    @EventHandler
    public void onRespond(AccessorRespondEvent event) {
        Response<T> response = (Response<T>) event.getEntity();
        Response responseEntity = this.getResponse(response);
        response.getValue(this.getField().getName()).setValue(this.extractValue(responseEntity));
    }

    @EventHandler
    public void onSave(AccessorSaveEvent event) {
        if (!this.getField().isTransient()) {
            return;
        }

        Query<T> baseQuery = (Query<T>) event.getQuery();
        this.save(baseQuery);
    }

    @EventHandler
    public void onDelete(AccessorDeleteEvent event) {
        if (!this.getField().isTransient()) {
            return;
        }

        Query<T> baseQuery = (Query<T>) event.getQuery();
        this.delete(baseQuery);
    }
}
