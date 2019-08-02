package org.tanberg.easydb.test.profile;

import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.profile.ItemProfile;
import org.tanberg.easydb.WrappedIndex;
import org.tanberg.easydb.test.mock.item.SimpleFieldsItem;
import org.tanberg.easydb.util.reflection.UtilField;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class ProfilerTest {

    private static final PersistentField<SimpleFieldsItem> ID_FIELD = new PersistentField<>(0, SimpleFieldsItem.class, UtilField.getField(SimpleFieldsItem.class, "id"));
    private static final PersistentField<SimpleFieldsItem> USERNAME_FIELD = new PersistentField<>(1, SimpleFieldsItem.class, UtilField.getField(SimpleFieldsItem.class, "username"));
    private static final PersistentField<SimpleFieldsItem> FIRSTNAME_FIELD = new PersistentField<>(2, SimpleFieldsItem.class, UtilField.getField(SimpleFieldsItem.class, "firstName"));
    private static final PersistentField<SimpleFieldsItem> LASTNAME_FIELD = new PersistentField<>(3, SimpleFieldsItem.class, UtilField.getField(SimpleFieldsItem.class, "lastName"));
    private static final PersistentField<SimpleFieldsItem> EMAIL_FIELD = new PersistentField<>(4, SimpleFieldsItem.class, UtilField.getField(SimpleFieldsItem.class, "email"));

    private static final WrappedIndex<SimpleFieldsItem> NAME_INDEX = new WrappedIndex<>(1, false, FIRSTNAME_FIELD, LASTNAME_FIELD);
    private static final WrappedIndex<SimpleFieldsItem> USERNAME_INDEX = new WrappedIndex<>(2, true, USERNAME_FIELD);

    @Test
    public void profilerTest() {
        ItemProfile<SimpleFieldsItem> profile = new ItemProfile<>(SimpleFieldsItem.class);

        assertThat(profile.getTypeClass()).isEqualTo(SimpleFieldsItem.class);

        assertThat(profile.getKeys()).isEqualTo(new PersistentField<?>[]{
          ID_FIELD
        });

        assertThat(profile.getFields()).isEqualTo(new PersistentField<?>[]{
          ID_FIELD,
          USERNAME_FIELD,
          FIRSTNAME_FIELD,
          LASTNAME_FIELD,
          EMAIL_FIELD
        });

        assertThat(profile.getAutoIncrementField()).isEqualTo(ID_FIELD);

        assertThat(profile.getIndices()).isEqualTo(new WrappedIndex<?>[]{
          NAME_INDEX,
          USERNAME_INDEX
        });
    }
}
