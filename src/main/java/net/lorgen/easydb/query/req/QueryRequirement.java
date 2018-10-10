package net.lorgen.easydb.query.req;

import net.lorgen.easydb.query.Operator;

public interface QueryRequirement {

    // Basically the only thing every requirement will have is an operator
    Operator getOperator();
}
