package org.tanberg.easydb.access;

import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.profile.ItemProfile;
import org.tanberg.easydb.query.Query;
import org.tanberg.easydb.query.response.Response;

import java.util.List;

/**
 * Accessor responsible for executing a given query on a specific database
 * type (e. g. Redis or MongoDB).
 *
 * @param <T> The type this accessor handles
 */
public interface DatabaseTypeAccessor<T> {

    ItemProfile<T> getProfile();

    boolean isSearchable(PersistentField<T> field);

    /**
     * Executes the database setup. Automatically called, but can (using this
     * method) also be called manually.
     */
    void setUp();

    /**
     * Looks for the first T value to match the given parameters.
     *
     * @param query The {@link Query query}
     * @return The first T value to match the given parameters
     */
    Response<T> findFirst(Query<T> query);

    /**
     * Looks for all T values matching the given parameters
     *
     * @param query The {@link Query query}
     * @return All T values to match the given parameters
     */
    List<Response<T>> findAll(Query<T> query);

    /**
     * Saves or updates any T value matching the given parameters
     * using the given values. If no requirement is passed, it will
     * use values in the query, assuming you are saving an instance
     * of T in its entirety (i. e. using the key values as parameters).
     *
     * @param query The {@link Query query}
     */
    void saveOrUpdate(Query<T> query);

    /**
     * Deletes any T value matching the given parameters.
     *
     * @param query The {@link Query query}
     */
    void delete(Query<T> query);

    /**
     * Drops all of the stored data in this table
     */
    void drop();
}
