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

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static org.tanberg.easydb.util.reflection.UtilField.getField;

@RunWith(Parameterized.class)
public class StrategyResolveTest {

    private static final Class<?> ITEM_CLASS = ExternalFieldItem.class;

    @Parameter(0)
    public PersistentField<?> field;

    @Parameter(1)
    public ProfilerStrategy strategy;

    @Test
    public void testStrategyResolve() {
        // We know that context resolve works
        ProfilerContext context = StrategyHelper.getContext(this.field);
        ProfilerStrategy strategy = StrategyHelper.suggestStrategy(context, this.field);

        assertThat(strategy).isEqualTo(this.strategy);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Lists.newArrayList(
          new Object[]{new PersistentField<>(0, ITEM_CLASS, getField(ITEM_CLASS, "item")), ProfilerStrategy.DIRECT_USE},
          new Object[]{new PersistentField<>(0, ITEM_CLASS, getField(ITEM_CLASS, "list")), ProfilerStrategy.DIRECT_USE},
          new Object[]{new PersistentField<>(0, ITEM_CLASS, getField(ITEM_CLASS, "list2")), ProfilerStrategy.DECLARING_KEYS_WITH_INDEX},
          new Object[]{new PersistentField<>(0, ITEM_CLASS, getField(ITEM_CLASS, "map")), ProfilerStrategy.DECLARING_KEYS_WITH_INDEX},
          new Object[]{new PersistentField<>(0, ITEM_CLASS, getField(ITEM_CLASS, "map2")), ProfilerStrategy.DECLARING_KEYS_WITH_INDEX}
        );
    }
}
