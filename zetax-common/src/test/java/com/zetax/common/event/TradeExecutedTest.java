package com.zetax.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** BF-02 decisions 13, 19, 20: one maker/taker match; impossible values can't exist. */
class TradeExecutedTest {

    private static TradeExecuted trade(long priceTicks, long qty, String makerOrderId, String takerOrderId) {
        return new TradeExecuted("2-1532-1", "ABC", 1532, "trade_2-1532-1", priceTicks, qty,
                makerOrderId, takerOrderId, "bob", "alice", 1_763_000_000_000L);
    }

    @Test
    void validTrade_keepsItsValues_andIsEqualToSameValues() {
        TradeExecuted t = trade(10000, 30, "order_bob", "order_alice");

        assertEquals("trade_2-1532-1", t.tradeId());
        assertEquals(10000, t.priceTicks());
        assertEquals(trade(10000, 30, "order_bob", "order_alice"), t);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void qtyNotPositive_throws(long qty) {
        assertThrows(IllegalArgumentException.class, () -> trade(10000, qty, "order_bob", "order_alice"));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void priceTicksNotPositive_throws(long priceTicks) {
        assertThrows(IllegalArgumentException.class, () -> trade(priceTicks, 30, "order_bob", "order_alice"));
    }

    @Test
    void negativeOffsetOrTime_throws() {
        assertThrows(IllegalArgumentException.class, () -> new TradeExecuted("2-1532-1", "ABC", -1, "trade_2-1532-1",
                10000, 30, "order_bob", "order_alice", "bob", "alice", 0));
        assertThrows(IllegalArgumentException.class, () -> new TradeExecuted("2-1532-1", "ABC", 1532, "trade_2-1532-1",
                10000, 30, "order_bob", "order_alice", "bob", "alice", -1));
    }

    @Test
    void sameMakerAndTakerOrder_throws() {
        assertThrows(IllegalArgumentException.class, () -> trade(10000, 30, "order_1", "order_1"));
    }

    @Test
    void nullFields_throwWithTheFieldName() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> trade(10000, 30, null, "order_alice"));
        assertEquals("makerOrderId", e.getMessage());
        assertThrows(NullPointerException.class, () -> trade(10000, 30, "order_bob", null));
        assertThrows(NullPointerException.class, () -> new TradeExecuted(null, "ABC", 1532, "trade_2-1532-1",
                10000, 30, "order_bob", "order_alice", "bob", "alice", 0));
        assertThrows(NullPointerException.class, () -> new TradeExecuted("2-1532-1", null, 1532, "trade_2-1532-1",
                10000, 30, "order_bob", "order_alice", "bob", "alice", 0));
        assertThrows(NullPointerException.class, () -> new TradeExecuted("2-1532-1", "ABC", 1532, null,
                10000, 30, "order_bob", "order_alice", "bob", "alice", 0));
        assertThrows(NullPointerException.class, () -> new TradeExecuted("2-1532-1", "ABC", 1532, "trade_2-1532-1",
                10000, 30, "order_bob", "order_alice", null, "alice", 0));
        assertThrows(NullPointerException.class, () -> new TradeExecuted("2-1532-1", "ABC", 1532, "trade_2-1532-1",
                10000, 30, "order_bob", "order_alice", "bob", null, 0));
    }
}
