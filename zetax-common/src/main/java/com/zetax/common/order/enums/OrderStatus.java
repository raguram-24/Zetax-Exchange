package com.zetax.common.order.enums;

/**
 * Order states built so far ({@code docs/flows/ORDER-LIFECYCLE.md} v0). CANCELLED / REJECTED come with later flows.
 * <p>{@code code} is the fixed value stored in the DB (BF-02 decision 23). Never use {@code ordinal()}.
 */
public enum OrderStatus {
    /** Gateway journaled the order and answered 202. */
    RECEIVED(5),
    /** Resting in the book, nothing traded. */
    OPEN(10),
    /** Some quantity traded, the rest still resting. */
    PARTIALLY_FILLED(15),
    /** All quantity traded (final). */
    FILLED(20);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    /** The fixed DB code. */
    public int code() {
        return code;
    }

    /** The value for a DB code; throws for an unknown code. */
    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("unknown OrderStatus code: " + code);
    }
}
