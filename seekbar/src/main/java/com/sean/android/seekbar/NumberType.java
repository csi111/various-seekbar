package com.sean.android.seekbar;

import java.math.BigDecimal;

/**
 * Created by Seonil on 2016-12-05.
 */

public  enum NumberType {
    LONG, DOUBLE, INTEGER, FLOAT, SHORT, BYTE, BIG_DECIMAL;

    public static <E extends Number> NumberType fromNumber(E value) throws IllegalArgumentException {
        if (value instanceof Long) {
            return LONG;
        }
        if (value instanceof Double) {
            return DOUBLE;
        }
        if (value instanceof Integer) {
            return INTEGER;
        }
        if (value instanceof Float) {
            return FLOAT;
        }
        if (value instanceof Short) {
            return SHORT;
        }
        if (value instanceof Byte) {
            return BYTE;
        }
        if (value instanceof BigDecimal) {
            return BIG_DECIMAL;
        }
        throw new IllegalArgumentException("Number class '" + value.getClass().getName() + "' is not supported");
    }

    public Number toNumber(double value) {
        switch (this) {
            case LONG:
                return (long) value;
            case DOUBLE:
                return value;
            case INTEGER:
                return (int) value;
            case FLOAT:
                return (float) value;
            case SHORT:
                return (short) value;
            case BYTE:
                return (byte) value;
            case BIG_DECIMAL:
                return BigDecimal.valueOf(value);
        }
        throw new InstantiationError("can't convert " + this + " to a Number object");
    }
}
