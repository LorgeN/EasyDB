package org.tanberg.easydb.query.traverse;

import com.google.common.collect.Lists;
import org.tanberg.easydb.query.req.SimpleRequirement;

import java.util.List;
import java.util.Objects;

public class RequirementCase {

    private List<SimpleRequirement> requirements;

    public RequirementCase() {
        this(Lists.newArrayList());
    }

    public RequirementCase(RequirementCase other) {
        this(Lists.newArrayList(other.getRequirements()));
    }

    public RequirementCase(SimpleRequirement... requirements) {
        this(Lists.newArrayList(requirements));
    }

    public RequirementCase(List<SimpleRequirement> requirements) {
        this.requirements = requirements;
    }

    public List<SimpleRequirement> getRequirements() {
        return requirements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RequirementCase that = (RequirementCase) o;
        return Objects.equals(requirements, that.requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirements);
    }

    @Override
    public String toString() {
        return "RequirementCase{" +
          "requirements=" + requirements +
          '}';
    }
}
