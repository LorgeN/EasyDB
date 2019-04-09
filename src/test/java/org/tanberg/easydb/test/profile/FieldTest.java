package org.tanberg.easydb.test.profile;

import com.google.common.collect.Lists;
import org.tanberg.easydb.DataType;
import org.tanberg.easydb.field.FieldSerializer;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.test.item.TestItem;
import org.tanberg.easydb.util.reflection.UtilField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.lang.reflect.Field;
import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public class FieldTest {

    @Parameter(0)
    public String fieldName;

    @Parameter(1)
    public String name;

    @Parameter(2)
    public DataType type;

    @Parameter(3)
    public int size;

    @Parameter(4)
    public Class<?>[] typeParams;

    @Parameter(5)
    public Class<? extends FieldSerializer> serializerClass;

    @Parameter(6)
    public boolean key;

    @Parameter(7)
    public boolean autoIncr;

    @Parameter(8)
    public boolean index;

    @Parameter(9)
    public int[] indexIds;

    @Test
    public void fieldTest() {
        Field classField = UtilField.getField(TestItem.class, this.fieldName);
        PersistentField<TestItem> field = new PersistentField<>(0, TestItem.class, classField);

        assertThat(field.getName()).isEqualTo(this.name);
        assertThat(field.getSize()).isEqualTo(this.size);
        assertThat(field.getTypeParameters()).isEqualTo(this.typeParams);
        assertThat(field.getSerializerClass()).isEqualTo(this.serializerClass);
        assertThat(field.isStorageKey()).isEqualTo(this.key);
        assertThat(field.isAutoIncrement()).isEqualTo(this.autoIncr);
        assertThat(field.isIndex()).isEqualTo(this.index);
        assertThat(field.getIndexIds()).isEqualTo(this.indexIds);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Lists.newArrayList(
          // Various data for the fields in the "TestItem" class
          new Object[]{"id", "id", DataType.INTEGER, 16, new Class[0], DataType.class, true, true, false, null},
          new Object[]{"username", "name", DataType.STRING, 24, new Class[0], DataType.class, false, false, true, new int[]{-1}},
          new Object[]{"firstName", "firstName", DataType.STRING, 16, new Class[]{boolean.class}, DataType.class, false, false, true, new int[]{1}},
          new Object[]{"email", "email", DataType.STRING, 32, new Class[0], DataType.class, false, false, false, null}
        );
    }
}
