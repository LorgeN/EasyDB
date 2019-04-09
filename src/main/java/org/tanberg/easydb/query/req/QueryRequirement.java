package org.tanberg.easydb.query.req;

import org.tanberg.easydb.query.Operator;

public interface QueryRequirement {

    // Basically the only thing every requirement will have is an operator
    Operator getOperator();
}
