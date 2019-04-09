package org.tanberg.easydb.util;

import java.math.BigDecimal;

public class UtilCompare {

    public static int compare(Number number1, Number number2) {
        // Probably not the best performance wise
        BigDecimal decimal1 = new BigDecimal(number1.toString());
        BigDecimal decimal2 = new BigDecimal(number2.toString());
        return decimal1.compareTo(decimal2);
    }
}
