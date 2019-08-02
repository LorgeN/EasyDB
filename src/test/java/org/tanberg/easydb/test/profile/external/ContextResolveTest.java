package org.tanberg.easydb.test.profile.external;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.profile.external.strategy.ProfilerContext;
import org.tanberg.easydb.profile.external.strategy.ProfilerStrategy;
import org.tanberg.easydb.profile.external.strategy.StrategyHelper;
import org.tanberg.easydb.test.mock.item.ExternalFieldItem;
import org.tanberg.easydb.util.reflection.UtilField;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ContextResolveTest {

    // TODO: Test that the StrategyHelper resolves the correct context for certain scenarios

    @Parameter(0)
    public PersistentField<?> field;

    @Parameter(1)
    public ProfilerContext context;

    @Test
    public void testResolveContext() {
        ProfilerContext resolved = StrategyHelper.getContext(this.field);
        Truth.assertThat(resolved).isEqualTo(this.context);
    }

    /*

    @External(table = "basic_external_test", immutable = false, keyFields = "itemId")
    private SimpleFieldsItem item;

    @External(table = "list_key_external_test", immutable = false, keyFields = "name")
    @Options(typeParams = SimpleFieldsItem.class)
    private List<SimpleFieldsItem> list;

    @External(table = "list_nokey_external_test", immutable = false)
    @Options(typeParams = NoKeyItem.class)
    private List<NoKeyItem> list2;

    @External(table = "map_key_external_test", immutable = false)
    @Options(typeParams = {Integer.class, SimpleFieldsItem.class})
    private Map<Integer, SimpleFieldsItem> map;

    @External(table = "map_nokey_external_test", immutable = false)
    @Options(typeParams = {Integer.class, NoKeyItem.class})
    private Map<Integer, NoKeyItem> map2;
     */

    @Parameters
    public static Collection<Object[]> data() {
        return Lists.newArrayList(
          new Object[]{new PersistentField<>(0, ExternalFieldItem.class, UtilField.getField(ExternalFieldItem.class, "item")), ProfilerStrategy.DIRECT_USE},
          new Object[]{new PersistentField<>(0, ExternalFieldItem.class, UtilField.getField(ExternalFieldItem.class, "item")), ProfilerStrategy.DIRECT_USE}
        );
    }
}
