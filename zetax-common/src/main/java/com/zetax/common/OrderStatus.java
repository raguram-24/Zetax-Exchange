package com.zetax.common;

/** Order lifecycle states (see "Business and Technical Flow" A3). */
public enum OrderStatus {
    RECEIVED,
    OPEN,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED
}
