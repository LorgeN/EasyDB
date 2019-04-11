package org.tanberg.easydb.access.memory;

import org.tanberg.easydb.ItemRepository;
import org.tanberg.easydb.access.ListenableTypeAccessor;
import org.tanberg.easydb.connection.configuration.ConnectionConfiguration;
import org.tanberg.easydb.connection.memory.MemoryDatabase;
import org.tanberg.easydb.connection.memory.MemoryTable;
import org.tanberg.easydb.connection.memory.UnsafeMemoryAccessor;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.query.Query;
import org.tanberg.easydb.query.response.Response;

import java.util.List;

public class MemoryAccessor<T> extends ListenableTypeAccessor<T> {

    private final MemoryTable<T> table;

    public MemoryAccessor(String table, ItemRepository<T> repository) {
        super(repository);

        this.table = MemoryDatabase.getTable(table, this.getProfile());
    }

    public UnsafeMemoryAccessor<T> getUnsafeAccessor() {
        return this.table.getUnsafeAccessor();
    }

    @Override
    protected void setUpInternal() {
    }

    @Override
    public ConnectionConfiguration getConfiguration() {
        return null;
    }

    @Override
    protected Response<T> findFirstInternal(Query<T> query) {
        return this.table.findFirstInternal(query);
    }

    @Override
    public List<Response<T>> findAllInternal(Query<T> query) {
        return this.table.findAllInternal(query);
    }

    @Override
    public void saveOrUpdateInternal(Query<T> query) {
        this.table.saveOrUpdateInternal(query);
    }

    @Override
    public void deleteInternal(Query<T> query) {
        this.table.deleteInternal(query);
    }

    @Override
    public void dropInternal() {
        this.table.dropInternal();
    }

    @Override
    public boolean isSearchable(PersistentField<T> field) {
        return this.table.isSearchable(field);
    }
}
