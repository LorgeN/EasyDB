package org.tanberg.easydb.access.memory;

import org.tanberg.easydb.ItemRepository;
import org.tanberg.easydb.access.ListenableTypeAccessor;
import org.tanberg.easydb.connection.configuration.ConnectionConfiguration;
import org.tanberg.easydb.connection.memory.MemoryDatabase;
import org.tanberg.easydb.connection.memory.MemoryTable;
import org.tanberg.easydb.connection.memory.UnsafeMemoryAccessor;
import org.tanberg.easydb.exception.DeleteQueryException;
import org.tanberg.easydb.exception.DropException;
import org.tanberg.easydb.exception.FindQueryException;
import org.tanberg.easydb.exception.SaveQueryException;
import org.tanberg.easydb.field.FieldValue;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.query.Query;
import org.tanberg.easydb.query.response.Response;

import java.util.List;
import java.util.stream.Collectors;

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
        try {
            FieldValue<T>[] values = this.table.findFirst(query.getRequirement());
            return new Response<>(this.getProfile(), values);
        } catch (Throwable t) {
            throw new FindQueryException(t, query);
        }
    }

    @Override
    public List<Response<T>> findAllInternal(Query<T> query) {
        try {
            List<FieldValue<T>[]> allValues = this.table.findAll(query.getRequirement());
            return allValues.stream()
              .map(values -> new Response<>(this.getProfile(), values))
              .collect(Collectors.toList());
        } catch (Throwable t) {
            throw new FindQueryException(t, query);
        }
    }

    @Override
    public void saveOrUpdateInternal(Query<T> query) {
        try {
            this.table.save(query.getObjectInstance(), query.getValues(), query.getRequirement());
        } catch (Throwable t) {
            throw new SaveQueryException(t, query);
        }
    }

    @Override
    public void deleteInternal(Query<T> query) {
        try {
            this.table.delete(query.getRequirement());
        } catch (Throwable t) {
            throw new DeleteQueryException(t, query);
        }
    }

    @Override
    public void dropInternal() {
        try {
            this.table.drop();
        } catch (Throwable t) {
            throw new DropException(t);
        }
    }

    @Override
    public boolean isSearchable(PersistentField<T> field) {
        return this.table.isSearchable(field);
    }
}
