package net.lorgen.easydb.query.traverse;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.lorgen.easydb.query.req.CombinedRequirement;
import net.lorgen.easydb.query.req.QueryRequirement;
import net.lorgen.easydb.query.req.SimpleRequirement;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Set;

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
        Validate.notNull(requirement);

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
        // We consider these as their own, standalone requirements (unaffected by the
        // "surrounding" requirements).
        List<RequirementCase> req1Cases = new RequirementTraverser(requirement1).getCases();
        List<RequirementCase> req2Cases = new RequirementTraverser(requirement2).getCases();

        Set<RequirementCase> combined = Sets.newHashSet();
        combined.addAll(req1Cases);
        combined.addAll(req2Cases);

        // Take every newly found case, and combine it with what we have already
        // found in this instance.
        List<RequirementCase> cases = Lists.newArrayList();
        for (RequirementCase activeCase : this.activeCases) {
            for (RequirementCase otherCase : combined) {
                RequirementCase clone = new RequirementCase(activeCase);
                clone.getRequirements().addAll(otherCase.getRequirements());
                cases.add(clone);
            }
        }

        this.activeCases = cases;
    }
}
