package net.lorgen.easydb.query.req;

import net.lorgen.easydb.query.Operator;

import java.util.Objects;

public class CombinedRequirement implements QueryRequirement {

    private QueryRequirement requirement1;
    private QueryRequirement requirement2;
    private Operator operator;
    private boolean wrap;

    public CombinedRequirement(QueryRequirement requirement1, Operator operator, QueryRequirement requirement2) {
        this.requirement1 = requirement1;
        this.requirement2 = requirement2;
        this.operator = operator;
    }

    public QueryRequirement getRequirement1() {
        return requirement1;
    }

    public QueryRequirement getRequirement2() {
        return requirement2;
    }

    public void setRequirement1(QueryRequirement requirement1) {
        this.requirement1 = requirement1;
    }

    public void setRequirement2(QueryRequirement requirement2) {
        this.requirement2 = requirement2;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    public boolean isWrapped() {
        return wrap;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CombinedRequirement that = (CombinedRequirement) o;
        return wrap == that.wrap &&
          Objects.equals(requirement1, that.requirement1) &&
          Objects.equals(requirement2, that.requirement2) &&
          operator == that.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirement1, requirement2, operator, wrap);
    }

    @Override
    public String toString() {
        return "CombinedRequirement{" +
          "requirement1=" + requirement1 +
          ", requirement2=" + requirement2 +
          ", operator=" + operator +
          ", wrap=" + wrap +
          '}';
    }
}
