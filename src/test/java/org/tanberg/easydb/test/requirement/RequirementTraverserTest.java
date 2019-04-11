package org.tanberg.easydb.test.requirement;

import com.google.common.collect.Lists;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.query.Operator;
import org.tanberg.easydb.query.req.QueryRequirement;
import org.tanberg.easydb.query.req.RequirementBuilder;
import org.tanberg.easydb.query.req.SimpleRequirement;
import org.tanberg.easydb.query.traverse.RequirementCase;
import org.tanberg.easydb.query.traverse.RequirementTraverser;
import org.tanberg.easydb.test.mock.item.TestItem;
import org.tanberg.easydb.util.reflection.UtilField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public class RequirementTraverserTest {

    // Fields
    private static final PersistentField<TestItem> ID_FIELD = new PersistentField<>(0, TestItem.class, UtilField.getField(TestItem.class, "id"));
    private static final PersistentField<TestItem> USERNAME_FIELD = new PersistentField<>(1, TestItem.class, UtilField.getField(TestItem.class, "username"));
    private static final PersistentField<TestItem> FIRSTNAME_FIELD = new PersistentField<>(2, TestItem.class, UtilField.getField(TestItem.class, "firstName"));
    private static final PersistentField<TestItem> LASTNAME_FIELD = new PersistentField<>(3, TestItem.class, UtilField.getField(TestItem.class, "lastName"));
    private static final PersistentField<TestItem> EMAIL_FIELD = new PersistentField<>(4, TestItem.class, UtilField.getField(TestItem.class, "email"));

    @Parameter(0)
    public QueryRequirement requirement;

    @Parameter(1)
    public RequirementCase[] cases;

    @Test
    public void requirementTraverseTest() {
        RequirementTraverser traverser = new RequirementTraverser(this.requirement);

        assertThat(traverser.getCases()).containsAllIn(this.cases);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Lists.newArrayList(
          new Object[]{
            new RequirementBuilder<TestItem>(null, null).andEquals(ID_FIELD, 0).build(),
            new RequirementCase[]{
              new RequirementCase(new SimpleRequirement(ID_FIELD, Operator.EQUALS, 0))
            }
          },
          new Object[]{
            new RequirementBuilder<TestItem>(null, null)
              .andEquals(ID_FIELD, 0)
              .orEquals(ID_FIELD, 1)
              .build(),
            new RequirementCase[]{
              new RequirementCase(new SimpleRequirement(ID_FIELD, Operator.EQUALS, 0)),
              new RequirementCase(new SimpleRequirement(ID_FIELD, Operator.EQUALS, 1))
            }
          },
          new Object[]{
            new RequirementBuilder<TestItem>(null, null)
              .andOpen()
              .andEquals(ID_FIELD, 0)
              .orEquals(USERNAME_FIELD, "test1")
              .orEquals(USERNAME_FIELD, "test2")
              .closeCurrent()
              .andEquals(LASTNAME_FIELD, "test3")
              .build(),
            new RequirementCase[]{
              new RequirementCase(
                new SimpleRequirement(ID_FIELD, Operator.EQUALS, 0),
                new SimpleRequirement(LASTNAME_FIELD, Operator.EQUALS, "test3")
              ),
              new RequirementCase(
                new SimpleRequirement(USERNAME_FIELD, Operator.EQUALS, "test1"),
                new SimpleRequirement(LASTNAME_FIELD, Operator.EQUALS, "test3")
              ),
              new RequirementCase(
                new SimpleRequirement(USERNAME_FIELD, Operator.EQUALS, "test2"),
                new SimpleRequirement(LASTNAME_FIELD, Operator.EQUALS, "test3")
              )
            }
          }
        );
    }
}
