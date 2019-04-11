package org.tanberg.easydb.connection.memory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ArrayUtils;
import org.tanberg.easydb.WrappedIndex;
import org.tanberg.easydb.exception.DeleteQueryException;
import org.tanberg.easydb.exception.DropException;
import org.tanberg.easydb.exception.FindQueryException;
import org.tanberg.easydb.exception.SaveQueryException;
import org.tanberg.easydb.field.FieldValue;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.profile.ItemProfile;
import org.tanberg.easydb.query.Operator;
import org.tanberg.easydb.query.Query;
import org.tanberg.easydb.query.req.QueryRequirement;
import org.tanberg.easydb.query.req.SimpleRequirement;
import org.tanberg.easydb.query.response.Response;
import org.tanberg.easydb.query.traverse.RequirementCase;
import org.tanberg.easydb.query.traverse.RequirementTraverser;
import org.tanberg.easydb.util.IndexHelper;
import org.tanberg.easydb.util.ValueContainer;
import org.tanberg.easydb.util.ValueHelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemoryTable<T> {

    private final String name;
    private final ItemProfile<T> profile;
    private final Map<ValueContainer, ValueContainer> keyToValueMap;
    private final Map<WrappedIndex<T>, MemoryIndexMap<T>> indexMaps;
    private final UnsafeMemoryAccessor<T> unsafeAccessor;

    public MemoryTable(String name, ItemProfile<T> profile) {
        this.name = name;
        this.profile = profile;

        this.keyToValueMap = Maps.newConcurrentMap();
        this.indexMaps = Maps.newConcurrentMap();
        this.unsafeAccessor = new UnsafeMemoryAccessor<>(this.keyToValueMap, this.indexMaps);

        for (WrappedIndex<T> index : profile.getIndices()) {
            MemoryIndexMap<T> indexMap = new MemoryIndexMap<>(profile, index);
            this.indexMaps.put(index, indexMap);
        }
    }

    public String getName() {
        return name;
    }

    public ItemProfile<T> getProfile() {
        return profile;
    }


    public UnsafeMemoryAccessor<T> getUnsafeAccessor() {
        return unsafeAccessor;
    }

    public Response<T> findFirstInternal(Query<T> query) {
        try {
            if (query.getRequirement() == null) {
                ValueContainer value = Iterables.getFirst(this.keyToValueMap.values(), null);
                if (value == null) {
                    return new Response<>(this.getProfile());
                }

                return this.toResponse(value);
            }

            ValueContainer key = this.getFirstKey(query.getRequirement());
            return this.toResponseFromKey(key);
        } catch (Throwable t) {
            throw new FindQueryException(t, query);
        }
    }

    public List<Response<T>> findAllInternal(Query<T> query) {
        try {
            if (query.getRequirement() == null) {
                return this.toResponses(this.keyToValueMap.values());
            }

            return this.toResponsesFromKeys(this.getKeys(query.getRequirement()));
        } catch (Throwable t) {
            throw new FindQueryException(t, query);
        }
    }

    public void saveOrUpdateInternal(Query<T> query) {
        try {
            if (query.getRequirement() == null) {
                ValueContainer key = ValueContainer.getKeys(this.getProfile(), query.getValues());
                this.removeFromIndices(key);

                ValueContainer value = this.toValues(query.getValues());
                this.keyToValueMap.put(key, value);
                this.addToIndices(query.getValues());
                return;
            }

            FieldValue<T>[] newValues = query.getValues();
            int[] indices = new int[newValues.length];

            PersistentField<T>[] storedFields = this.getProfile().getStoredFields();
            for (int i = 0; i < newValues.length; i++) {
                indices[i] = ArrayUtils.indexOf(storedFields, newValues[i].getField());
            }

            Collection<ValueContainer> keys = this.getKeys(query.getRequirement());
            for (ValueContainer key : keys) {
                ValueContainer value = this.keyToValueMap.remove(key);
                this.removeFromIndices(key);

                for (int i = 0; i < indices.length; i++) {
                    value.setValue(indices[i], newValues[i].getValue());
                }

                this.keyToValueMap.put(key, value);
                this.addToIndices(this.toValues(value));
            }
        } catch (Throwable t) {
            throw new SaveQueryException(t, query);
        }
    }

    public void deleteInternal(Query<T> query) {
        try {
            if (query.getRequirement() == null) {
                this.dropInternal();
            }

            Collection<ValueContainer> keys = this.getKeys(query.getRequirement());
            for (ValueContainer key : keys) {
                ValueContainer remove = this.keyToValueMap.remove(key);
                this.removeFromIndices(remove);
            }
        } catch (Throwable t) {
            throw new DeleteQueryException(t, query);
        }
    }

    public void dropInternal() {
        try {
            this.keyToValueMap.clear();
            this.indexMaps.values().forEach(MemoryIndexMap::drop);
        } catch (Throwable t) {
            throw new DropException(t);
        }
    }

    public boolean isSearchable(PersistentField<T> field) {
        return field.isIndex() || field.isStorageKey();
    }

    // Internals

    private void addToIndices(FieldValue<T>[] values) {
        for (MemoryIndexMap<T> index : this.indexMaps.values()) {
            index.add(values);
        }
    }

    private void removeFromIndices(ValueContainer container) {
        for (MemoryIndexMap<T> index : this.indexMaps.values()) {
            index.removeKey(container);
        }
    }

    private void removeFromIndices(FieldValue<T>[] values) {
        for (MemoryIndexMap<T> index : this.indexMaps.values()) {
            index.remove(values);
        }
    }

    private FieldValue<T>[] toValues(ValueContainer container) {
        if (container == null) {
            return null;
        }

        PersistentField<T>[] fields = this.getProfile().getStoredFields();
        FieldValue<T>[] values = new FieldValue[fields.length];

        for (int i = 0; i < fields.length; i++) {
            PersistentField<T> field = fields[i];
            values[i] = new FieldValue<>(field, container.getValues()[i]);
        }

        return values;
    }

    private Response<T> toResponse(ValueContainer container) {
        return new Response<>(this.getProfile(), this.toValues(container));
    }

    private Response<T> toResponseFromKey(ValueContainer key) {
        return this.toResponse(this.keyToValueMap.get(key));
    }

    private ValueContainer toValues(FieldValue<T>[] values) {
        PersistentField<T>[] fields = this.getProfile().getStoredFields();
        Object[] valueArray = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            PersistentField<T> field = fields[i];
            valueArray[i] = ValueHelper.getValue(values, field);
        }

        return new ValueContainer(valueArray);
    }

    private List<Response<T>> toResponses(Collection<ValueContainer> values) {
        List<Response<T>> list = Lists.newArrayList();
        for (ValueContainer value : values) {
            list.add(this.toResponse(value));
        }

        return list;
    }

    private List<Response<T>> toResponsesFromKeys(Collection<ValueContainer> keys) {
        List<Response<T>> list = Lists.newArrayList();
        for (ValueContainer key : keys) {
            list.add(this.toResponseFromKey(key));
        }

        return list;
    }

    private ValueContainer getFirstKey(QueryRequirement requirement) {
        RequirementTraverser traverser = new RequirementTraverser(requirement);

        for (RequirementCase requirementCase : traverser.getCases()) {
            Collection<ValueContainer> keysFound = this.getKeys(requirementCase.getRequirements());
            if (keysFound.isEmpty()) {
                continue;
            }

            return Iterables.getFirst(keysFound, null);
        }

        return null;
    }

    private Collection<ValueContainer> getKeys(QueryRequirement requirement) {
        Set<ValueContainer> keys = Sets.newHashSet();
        RequirementTraverser traverser = new RequirementTraverser(requirement);

        for (RequirementCase requirementCase : traverser.getCases()) {
            Collection<ValueContainer> keysFound = this.getKeys(requirementCase.getRequirements());
            keys.addAll(keysFound);
        }

        return keys;
    }

    private Collection<ValueContainer> getKeys(List<SimpleRequirement> requirements) {
        ItemProfile<T> profile = this.getProfile();

        Collection<WrappedIndex<T>> indices = IndexHelper.combineIndices(profile, requirements);
        if (indices == null) {
            List<ValueContainer> localMappingIndices = Lists.newArrayList(this.keyToValueMap.keySet());
            for (SimpleRequirement requirement : requirements) {
                this.eliminate(localMappingIndices, requirement);
            }

            List<ValueContainer> values = Lists.newArrayList();
            for (ValueContainer index : localMappingIndices) {
                values.add(this.keyToValueMap.get(index));
            }

            return values;
        }

        List<Collection<ValueContainer>> keys = Lists.newArrayList();
        for (WrappedIndex<T> index : indices) {
            keys.add(this.getKeys(index, requirements));
        }

        return this.allMatch(keys);
    }

    private void eliminate(List<ValueContainer> containers, SimpleRequirement requirement) {
        PersistentField<T> field = (PersistentField<T>) requirement.getField();
        int index = ArrayUtils.indexOf(this.getProfile().getKeys(), requirement.getField());
        if (index == -1) {
            throw new IllegalArgumentException("Couldn't find key " + field + "!");
        }

        Object reqValue = requirement.getValue();
        Operator operator = requirement.getOperator();
        ValueHelper.eliminateUnmatching(containers, index, reqValue, operator);
    }

    private Collection<ValueContainer> allMatch(List<Collection<ValueContainer>> lists) {
        if (lists.size() == 0) {
            throw new IllegalArgumentException("Empty list!");
        }

        if (lists.size() == 1) {
            return lists.get(0);
        }

        Collection<ValueContainer> baseList = lists.remove(0);
        baseList.removeIf(item -> !lists.stream().allMatch(list -> list.contains(item)));
        return baseList;
    }

    private Collection<ValueContainer> getKeys(WrappedIndex<T> index, Collection<SimpleRequirement> values) {
        MemoryIndexMap<T> indexMap = this.indexMaps.get(index);
        return indexMap.getKeys(values);
    }
}
