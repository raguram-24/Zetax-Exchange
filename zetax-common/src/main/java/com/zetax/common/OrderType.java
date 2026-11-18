package com.zetax.common;

/** v1 order types (D-13). LIMIT orders are GTC; MARKET orders are immediate-or-cancel. */
public enum OrderType {
    LIMIT,
    MARKET
}
