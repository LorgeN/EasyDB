package org.tanberg.easydb.query;

public enum Operator {
    // Logical Operators

    AND("AND"),
    OR("OR"),

    // Comparison Operators

    NOT_EQUALS("<>"),
    EQUALS("="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL_TO("<="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL_TO(">=");

    private String str; // This is actually just the SQL operator

    Operator(String str) {
        this.str = str;
    }

    public String getString() {
        return str;
    }

    @Override
    public String toString() {
       return this.getString();
    }
}
