package net.lorgen.easydb.test.profile;

import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.WrappedIndex;
import net.lorgen.easydb.test.TestItem;
import net.lorgen.easydb.util.reflection.UtilField;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class ProfilerTest {

    private static final PersistentField<TestItem> ID_FIELD = new PersistentField<>(0, TestItem.class, UtilField.getField(TestItem.class, "id"));
    private static final PersistentField<TestItem> USERNAME_FIELD = new PersistentField<>(1, TestItem.class, UtilField.getField(TestItem.class, "username"));
    private static final PersistentField<TestItem> FIRSTNAME_FIELD = new PersistentField<>(2, TestItem.class, UtilField.getField(TestItem.class, "firstName"));
    private static final PersistentField<TestItem> LASTNAME_FIELD = new PersistentField<>(3, TestItem.class, UtilField.getField(TestItem.class, "lastName"));
    private static final PersistentField<TestItem> EMAIL_FIELD = new PersistentField<>(4, TestItem.class, UtilField.getField(TestItem.class, "email"));

    private static final WrappedIndex<TestItem> NAME_INDEX = new WrappedIndex<>(1, true, FIRSTNAME_FIELD, LASTNAME_FIELD);
    private static final WrappedIndex<TestItem> USERNAME_INDEX = new WrappedIndex<>(2, false, USERNAME_FIELD);

    @Test
    public void profilerTest() {
        ItemProfile<TestItem> profile = new ItemProfile<>(TestItem.class);

        assertThat(profile.getTypeClass()).isEqualTo(TestItem.class);

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
