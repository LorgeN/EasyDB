package org.tanberg.easydb.connection.memory;

import com.google.common.collect.Lists;
import org.tanberg.easydb.WrappedIndex;
import org.tanberg.easydb.field.FieldValue;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.profile.ItemProfile;
import org.tanberg.easydb.query.Operator;
import org.tanberg.easydb.query.req.SimpleRequirement;
import org.tanberg.easydb.util.UniqueValueMultimap;
import org.tanberg.easydb.util.ValueContainer;
import org.tanberg.easydb.util.ValueHelper;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;
import java.util.List;

public class MemoryIndexMap<T> {

    private final ItemProfile<T> profile;
    private final WrappedIndex<T> index;
    private final UniqueValueMultimap<ValueContainer, ValueContainer> indexValueToKey;

    public MemoryIndexMap(ItemProfile<T> profile, WrappedIndex<T> index) {
        this.profile = profile;
        this.index = index;
        this.indexValueToKey = new UniqueValueMultimap<>();
    }

    public ItemProfile<T> getProfile() {
        return profile;
    }

    public WrappedIndex<T> getIndex() {
        return index;
    }

    public void drop() {
        this.indexValueToKey.clear();
    }

    public void remove(FieldValue<T>[] values) {
        ValueContainer value = ValueContainer.getKeys(this.getProfile(), values);
        this.removeKey(value);
    }

    public void removeKey(ValueContainer key) {
        this.indexValueToKey.removeValue(key);
    }

    public void add(FieldValue<T>[] values) {
        ValueContainer key = ValueContainer.getValues(this.getIndex().getFields(), values);
        ValueContainer value = ValueContainer.getKeys(this.getProfile(), values);
        this.indexValueToKey.put(key, value);
    }

    public Collection<ValueContainer> getKeys(FieldValue<T>[] values) {
        ValueContainer key = ValueContainer.getKeys(this.getProfile(), values);
        return this.indexValueToKey.get(key);
    }

    public Collection<ValueContainer> getKeys(Collection<SimpleRequirement> requirements) {
        // Speed up for a common case
        if (requirements.stream().allMatch(requirement -> requirement.getOperator() == Operator.EQUALS)) {
            return this.getKeys((FieldValue<T>[]) requirements.stream()
              .map(requirement -> new FieldValue<>((PersistentField<T>) requirement.getField(), requirement.getValue()))
              .toArray(FieldValue[]::new));
        }

        List<ValueContainer> indices = Lists.newArrayList(this.indexValueToKey.keySet());
        for (SimpleRequirement requirement : requirements) {
            this.eliminate(indices, requirement);
        }

        List<ValueContainer> values = Lists.newArrayList();
        for (ValueContainer index : indices) {
            values.addAll(this.indexValueToKey.get(index));
        }

        return values;
    }

    // Internals

    private void eliminate(List<ValueContainer> containers, SimpleRequirement requirement) {
        PersistentField<T> field = (PersistentField<T>) requirement.getField();
        int index = ArrayUtils.indexOf(this.getIndex().getFields(), requirement.getField());
        if (index == -1) {
            throw new IllegalArgumentException("Couldn't find key " + field + "!");
        }

        Object reqValue = requirement.getValue();
        Operator operator = requirement.getOperator();
        ValueHelper.eliminateUnmatching(containers, index, reqValue, operator);
    }
}
