package net.lorgen.easydb.query.traverse;

import com.google.common.collect.Lists;
import net.lorgen.easydb.query.req.SimpleRequirement;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

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

        if (!(o instanceof RequirementCase)) {
            return false;
        }

        RequirementCase that = (RequirementCase) o;

        return new EqualsBuilder()
          .append(requirements, that.requirements)
          .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(requirements)
          .toHashCode();
    }

    @Override
    public String toString() {
        return "RequirementCase{" +
          "requirements=" + requirements +
          '}';
    }
}
