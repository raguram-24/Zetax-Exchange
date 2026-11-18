package com.zetax.common.order.enums;

/**
 * Order types built so far. BF-02: limit orders only (good-till-cancelled); MARKET comes with BF-03.
 * <p>{@code code} is the fixed value stored in the DB (BF-02 decision 23). Never use {@code ordinal()}.
 */
public enum OrderType {
    LIMIT(5);

    private final int code;

    OrderType(int code) {
        this.code = code;
    }

    /** The fixed DB code. */
    public int code() {
        return code;
    }

    /** The value for a DB code; throws for an unknown code. */
    public static OrderType fromCode(int code) {
        for (OrderType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown OrderType code: " + code);
    }
}
