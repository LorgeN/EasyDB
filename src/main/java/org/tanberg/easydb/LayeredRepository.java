package org.tanberg.easydb;

import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.profile.ItemProfile;
import org.tanberg.easydb.query.Query;
import org.tanberg.easydb.query.response.Response;

import java.util.List;

public class LayeredRepository<T> implements ItemRepository<T> {

    private String tableName;
    private Class<T> typeClass;
    private ItemProfile<T> profile;
    private ItemRepository<T>[] layers;

    @SafeVarargs
    public LayeredRepository(ItemRepository<T>... layers) {
        this.layers = layers;

        // TODO: Verify similarity and fill in arguments
    }

    public ItemRepository<T>[] getLayers() {
        return layers;
    }

    @Override
    public boolean isSearchable(PersistentField<T> field) {
        return false;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Class<T> getTypeClass() {
        return typeClass;
    }

    @Override
    public ItemProfile<T> getProfile() {
        return profile;
    }

    @Override
    public Response<T> findFirst(Query<T> query) {
        // TODO
        return null;
    }

    @Override
    public List<Response<T>> findAll(Query<T> query) {
        // TODO
        return null;
    }

    @Override
    public void save(Query<T> query) {
        // TODO
    }

    @Override
    public void delete(Query<T> query) {
        // TODO
    }
}
