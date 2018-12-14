package net.lorgen.easydb;

import com.google.common.collect.Maps;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.response.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CachedRepository<T> implements ItemRepository<T> {

    private ItemRepository<T> underlyingRepository;
    private Map<KeyContainer, T> keyToValueMap;

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
    public Response<T> findFirstSync(Query<T> query) {
        // TODO
        return null;
    }

    @Override
    public List<Response<T>> findAllSync(Query<T> query) {
        // TODO
        return null;
    }

    @Override
    public void saveSync(Query<T> query) {
        // TODO
    }

    @Override
    public void deleteSync(Query<T> query) {
        // TODO
    }

    public static class KeyContainer {

        private Object[] values;

        public KeyContainer(Object[] values) {
            this.values = values;
        }

        public Object[] getValues() {
            return values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof KeyContainer)) {
                return false;
            }

            KeyContainer that = (KeyContainer) o;
            return Arrays.equals(values, that.values);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(values);
        }

        @Override
        public String toString() {
            return "KeyContainer{" +
              "values=" + Arrays.toString(values) +
              '}';
        }
    }
}
