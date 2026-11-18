package com.zetax.common.order.enums;

/**
 * Which side of the book an order is on.
 * <p>{@code code} is the fixed value stored in the DB (BF-02 decision 23). Never use {@code ordinal()}.
 */
public enum Side {
    BUY(5),
    SELL(10);

    private final int code;

    Side(int code) {
        this.code = code;
    }

    /** The fixed DB code. */
    public int code() {
        return code;
    }

    /** The value for a DB code; throws for an unknown code. */
    public static Side fromCode(int code) {
        for (Side side : values()) {
            if (side.code == code) {
                return side;
            }
        }
        throw new IllegalArgumentException("unknown Side code: " + code);
    }
}
