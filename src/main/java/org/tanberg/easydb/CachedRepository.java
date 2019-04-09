package org.tanberg.easydb;

import com.google.common.collect.Maps;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.profile.ItemProfile;
import org.tanberg.easydb.query.Query;
import org.tanberg.easydb.query.response.Response;
import org.tanberg.easydb.util.ValueContainer;

import java.util.List;
import java.util.Map;

public class CachedRepository<T> implements ItemRepository<T> {

    private ItemRepository<T> underlyingRepository;
    private Map<ValueContainer, T> keyToValueMap;

    public CachedRepository(ItemRepository<T> underlyingRepository) {
        this.underlyingRepository = underlyingRepository;
        this.keyToValueMap = Maps.newHashMap();
    }

    @Override
    public boolean isSearchable(PersistentField<T> field) {
        return false;
    }

    @Override
    public String getTableName() {
        return this.underlyingRepository.getTableName();
    }

    @Override
    public Class<T> getTypeClass() {
        return this.underlyingRepository.getTypeClass();
    }

    @Override
    public ItemProfile<T> getProfile() {
        return this.underlyingRepository.getProfile();
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
