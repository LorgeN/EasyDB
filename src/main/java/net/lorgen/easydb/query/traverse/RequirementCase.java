package net.lorgen.easydb.query.traverse;

import net.lorgen.easydb.query.req.SimpleRequirement;
import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;

public class RequirementCase {

    private List<SimpleRequirement> requirements;

    public RequirementCase() {
        this(Lists.newArrayList());
    }

    public RequirementCase(RequirementCase other) {
        this(Lists.newArrayList(other.getRequirements()));
    }

    public RequirementCase(List<SimpleRequirement> requirements) {
        this.requirements = requirements;
    }

    public List<SimpleRequirement> getRequirements() {
        return requirements;
    }
}
