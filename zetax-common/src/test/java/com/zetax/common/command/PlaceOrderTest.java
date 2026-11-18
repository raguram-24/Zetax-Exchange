package com.zetax.common.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zetax.common.order.enums.OrderType;
import com.zetax.common.order.enums.Side;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** BF-02 decision 19: a PlaceOrder with impossible values can never exist. */
class PlaceOrderTest {

    private static PlaceOrder order(long priceTicks, long qty) {
        return new PlaceOrder("order_1", "alice", "ABC", Side.BUY, OrderType.LIMIT, priceTicks, qty);
    }

    @Test
    void validOrder_keepsItsValues_andIsEqualToSameValues() {
        PlaceOrder o = order(10100, 70);

        assertEquals("ABC", o.symbol());
        assertEquals(10100, o.priceTicks());
        assertEquals(order(10100, 70), o);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -70})
    void qtyNotPositive_throws(long qty) {
        assertThrows(IllegalArgumentException.class, () -> order(10100, qty));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void priceTicksNotPositive_throws(long priceTicks) {
        assertThrows(IllegalArgumentException.class, () -> order(priceTicks, 70));
    }

    @Test
    void nullFields_throwWithTheFieldName() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new PlaceOrder("order_1", null, "ABC", Side.BUY, OrderType.LIMIT, 10100, 70));
        assertEquals("userId", e.getMessage());
        assertThrows(NullPointerException.class, () -> new PlaceOrder(null, "alice", "ABC", Side.BUY, OrderType.LIMIT, 10100, 70));
        assertThrows(NullPointerException.class, () -> new PlaceOrder("order_1", "alice", null, Side.BUY, OrderType.LIMIT, 10100, 70));
        assertThrows(NullPointerException.class, () -> new PlaceOrder("order_1", "alice", "ABC", null, OrderType.LIMIT, 10100, 70));
        assertThrows(NullPointerException.class, () -> new PlaceOrder("order_1", "alice", "ABC", Side.BUY, null, 10100, 70));
    }
}
