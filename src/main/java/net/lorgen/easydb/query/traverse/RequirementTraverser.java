package net.lorgen.easydb.query.traverse;

import net.lorgen.easydb.query.req.CombinedRequirement;
import net.lorgen.easydb.query.req.QueryRequirement;
import net.lorgen.easydb.query.req.SimpleRequirement;
import com.google.common.collect.Lists;

import java.util.List;

public class RequirementTraverser {

    private List<RequirementCase> completedCases;
    private List<RequirementCase> activeCases;

    public RequirementTraverser() {
        this.completedCases = Lists.newArrayList();
        this.activeCases = Lists.newArrayList(new RequirementCase());
    }

    public RequirementTraverser(QueryRequirement requirement) {
        this();
        this.traverse(requirement);
    }

    public RequirementTraverser(RequirementTraverser other) {
        this.completedCases = Lists.newArrayList();
        this.activeCases = Lists.newArrayList(other.activeCases);
    }

    public void traverse(QueryRequirement requirement) {
        if (requirement instanceof SimpleRequirement) {
            for (RequirementCase traversement : this.activeCases) {
                traversement.getRequirements().add((SimpleRequirement) requirement);
            }
            return;
        }

        if (!(requirement instanceof CombinedRequirement)) {
            throw new UnsupportedOperationException("Unsupported requirement \"" + requirement.getClass().getSimpleName() + "\"!");
        }

        switch (requirement.getOperator()) {
            case AND:
                this.traverse(((CombinedRequirement) requirement).getRequirement1());
                this.traverse(((CombinedRequirement) requirement).getRequirement2());
                break;
            case OR:
                if (((CombinedRequirement) requirement).isWrapped()) {
                    this.split(((CombinedRequirement) requirement).getRequirement1(), ((CombinedRequirement) requirement).getRequirement2());
                    break;
                }

                this.traverse(((CombinedRequirement) requirement).getRequirement1());
                this.complete();
                this.traverse(((CombinedRequirement) requirement).getRequirement2());
                break;
            default:
                throw new UnsupportedOperationException("Operation \"" + requirement.getOperator().name() + "\" not supported by Redis!");
        }
    }

    public List<RequirementCase> getCases() {
        List<RequirementCase> cases = Lists.newArrayList(this.activeCases);
        cases.addAll(this.completedCases);
        return cases;
    }

    // Internals

    private void complete() {
        this.completedCases.addAll(this.activeCases);
        this.activeCases = Lists.newArrayList(new RequirementCase());
    }

    private void split(QueryRequirement requirement1, QueryRequirement requirement2) {
        RequirementTraverser other = new RequirementTraverser(this);
        this.traverse(requirement1);
        other.traverse(requirement2);

        this.completedCases.addAll(other.completedCases); // Could happen
        this.activeCases.addAll(other.activeCases);
    }
}
