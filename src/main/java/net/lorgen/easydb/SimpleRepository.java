package net.lorgen.easydb;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.response.Response;

import java.util.List;
import java.util.Objects;

/**
 * The simplest of repositories, and the most basic possible implementation
 * of {@link DatabaseRepository}.
 *
 * @param <T> The type
 */
public class SimpleRepository<T> implements DatabaseRepository<T> {

    private String tableName;
    private DatabaseType type;
    private ItemProfile<T> profile;
    private DatabaseTypeAccessor<T> accessor;

    @RepositoryConstructor({RepositoryOption.CONFIG, RepositoryOption.TABLE, RepositoryOption.TYPE, RepositoryOption.DATABASE})
    public SimpleRepository(ConnectionConfiguration configuration, String tableName, Class<T> typeClass, DatabaseType type) {
        this(configuration, tableName, new ItemProfile<>(typeClass), type);
    }

    @RepositoryConstructor({RepositoryOption.CONFIG, RepositoryOption.TABLE, RepositoryOption.TYPE, RepositoryOption.DATABASE})
    public SimpleRepository(ConnectionConfiguration configuration, String tableName, ItemProfile<T> profile, DatabaseType type) {
        this.tableName = tableName;
        this.type = type;

        this.profile = profile;
        this.accessor = type.newAccessor(configuration, this, this.tableName);

        Repositories.registerRepository(this);
    }

    @RepositoryConstructor({RepositoryOption.TABLE, RepositoryOption.TYPE, RepositoryOption.DATABASE})
    public SimpleRepository(String tableName, Class<T> typeClass, DatabaseType type) {
        this(tableName, new ItemProfile<>(typeClass), type);
    }

    @RepositoryConstructor({RepositoryOption.TABLE, RepositoryOption.PROFILE, RepositoryOption.DATABASE})
    public SimpleRepository(String tableName, ItemProfile<T> profile, DatabaseType type) {
        this.tableName = tableName;
        this.type = type;

        this.profile = profile;
        this.accessor = type.newAccessor(this, this.tableName);

        Repositories.registerRepository(this);
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Class<T> getTypeClass() {
        return this.getProfile().getTypeClass();
    }

    @Override
    public DatabaseTypeAccessor<T> getDatabaseAccessor() {
        return accessor;
    }

    @Override
    public DatabaseType getDatabaseType() {
        return type;
    }

    @Override
    public ItemProfile<T> getProfile() {
        return profile;
    }

    @Override
    public Response<T> findFirstSync(Query<T> query) {
        return accessor.findFirst(query);
    }

    @Override
    public List<Response<T>> findAllSync(Query<T> query) {
        return accessor.findAll(query);
    }

    @Override
    public void saveSync(Query<T> query) {
        query.getObjectInstance().ifPresent(instance -> {
            if (instance instanceof StoredItem) {
                ((StoredItem) instance).preSave();
            }
        });

        this.accessor.saveOrUpdate(query);

        query.getObjectInstance().ifPresent(instance -> {
            if (instance instanceof StoredItem) {
                ((StoredItem) instance).postSave();
            }
        });
    }

    @Override
    public void deleteSync(Query<T> query) {
        query.getObjectInstance().ifPresent(instance -> {
            if (instance instanceof StoredItem) {
                ((StoredItem) instance).preDelete();
            }
        });

        this.accessor.delete(query);

        query.getObjectInstance().ifPresent(instance -> {
            if (instance instanceof StoredItem) {
                ((StoredItem) instance).postDelete();
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleRepository<?> that = (SimpleRepository<?>) o;
        return Objects.equals(tableName, that.tableName) &&
          Objects.equals(profile, that.profile) &&
          Objects.equals(accessor, that.accessor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, profile, accessor);
    }

    @Override
    public String toString() {
        return "ItemRepository{" +
          "tableName='" + tableName + '\'' +
          ", profile=" + profile +
          ", accessor=" + accessor +
          '}';
    }
}
