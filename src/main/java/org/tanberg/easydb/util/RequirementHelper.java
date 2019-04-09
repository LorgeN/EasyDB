package org.tanberg.easydb.util;

import org.tanberg.easydb.field.FieldValue;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.query.req.SimpleRequirement;

public class RequirementHelper {

    public static <T> boolean matches(SimpleRequirement requirement, FieldValue<T>[] values) {
        for (FieldValue<T> value : values) {
            PersistentField<T> field = value.getField();
            if (!field.equals(requirement.getField())) {
                continue;
            }

            Object fieldValue = value.getValue();
            Object requirementValue = requirement.getValue();
            return ValueHelper.matches(fieldValue, requirementValue, requirement.getOperator());
        }

        // No value found = no match
        return false;
    }
}
