package org.tanberg.easydb.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.tanberg.easydb.WrappedIndex;
import org.tanberg.easydb.field.FieldValue;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.profile.ItemProfile;
import org.tanberg.easydb.query.req.SimpleRequirement;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class IndexHelper {

    public static <T> Collection<WrappedIndex<T>> combineIndices(ItemProfile<T> profile, FieldValue<T>[] values) {
        return combineFieldIndices(profile, Arrays.stream(values)
          .map(FieldValue::getField)
          .toArray(PersistentField[]::new));
    }

    public static <T> Collection<WrappedIndex<T>> combineIndices(ItemProfile<T> profile, Collection<SimpleRequirement> requirements) {
        return combineFieldIndices(profile, requirements.stream()
          .map(SimpleRequirement::getField)
          .toArray(PersistentField[]::new));
    }

    public static <T> Collection<WrappedIndex<T>> combineIndices(ItemProfile<T> profile, SimpleRequirement[] requirements) {
        return combineFieldIndices(profile, Arrays.stream(requirements)
          .map(SimpleRequirement::getField)
          .toArray(PersistentField[]::new));
    }

    public static <T> Collection<WrappedIndex<T>> combineFieldIndices(ItemProfile<T> profile, PersistentField<T>[] fields) {
        if (profile.areKeys(fields)) {
            return null;
        }

        WrappedIndex<T> potentialIndex = profile.getIndex(fields);
        if (potentialIndex != null) {
            return Lists.newArrayList(potentialIndex);
        }

        List<WrappedIndex<T>> indices = Lists.newArrayList();
        Set<PersistentField<T>> usedFields = Sets.newHashSet();
        for (WrappedIndex<T> index : profile.getIndices()) {
            List<PersistentField<T>> contains = Lists.newArrayList();
            for (PersistentField<T> field : fields) {
                if (!index.isField(field)) {
                    continue;
                }

                contains.add(field);
            }

            if (!index.areFields(contains)) {
                continue;
            }

            usedFields.addAll(contains);
            indices.add(index);
        }

        if (usedFields.size() != fields.length) {
            throw new IllegalArgumentException("Attempted query on un-indexed field!");
        }

        return indices;
    }
}
