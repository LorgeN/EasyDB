package net.lorgen.easydb.interact.external;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import net.lorgen.easydb.query.req.RequirementBuilder;
import net.lorgen.easydb.response.Response;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ExternalCollectionOtherKeyHandle<T> extends OtherKeyHandle<T> {

    public ExternalCollectionOtherKeyHandle(ListenableTypeAccessor<T> accessor, PersistentField<T> field) {
        super(accessor, field);

        DatabaseType type = this.getDatabaseType();
        String table = field.getExternalTable();
        Class<?> typeClass = field.getTypeParameters()[0];
        Class<? extends ItemRepository> repoClass = field.getRepository();

        if (!this.hasKeys(typeClass)) {
            ItemProfile profile = this.buildWithLocalKeys(typeClass, Integer.class);

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
        Collection collection = this.newInstance();
        if (collection == null) {
            return;
        }

        Collections.addAll(collection, this.getResponses((Response<T>) event.getEntity()).stream()
          .map(this::extractValue)
          .collect(Collectors.toList()));

        event.getEntity().getValue(this.getField().getName()).setValue(collection);
    }

    @EventHandler
    public void onSave(AccessorSaveEvent event) {
        if (this.getField().isTransient()) {
            return;
        }

        Query<T> baseQuery = (Query<T>) event.getQuery();

        this.delete(baseQuery); // Removes any elements that have been removed from the list

        Collection<?> collection = (Collection<?>) baseQuery.getValue(this.getField()).getValue();
        Iterator<?> iterator = collection.iterator();

        AtomicInteger index = new AtomicInteger(0);
        iterator.forEachRemaining(value -> this.save(baseQuery, index.getAndIncrement(), value));
    }

    @EventHandler
    public void onDelete(AccessorDeleteEvent event) {
        if (!this.getField().isTransient()) {
            return;
        }

        Query<T> baseQuery = (Query<T>) event.getQuery();
        this.delete(baseQuery);
    }

    // Internals

    public void delete(Query<T> baseQuery) {
        if (this.useAddedKeys()) {
            super.delete(baseQuery);
            return;
        }

        Collection<?> collection = (Collection<?>) baseQuery.getValue(this.getField()).getValue();
        for (Object o : collection) {
            ((RequirementBuilder) this.getRepository().newQuery().where()).keysAreSameAs(o).closeAll().deleteSync();
        }
    }

    private Collection<?> newInstance() {
        Class<? extends Collection> typeClass = (Class<? extends Collection>) this.getField().getTypeClass();
        if (!typeClass.isInterface()) {
            try {
                return typeClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

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
