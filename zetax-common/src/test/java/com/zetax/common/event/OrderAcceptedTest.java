package com.zetax.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zetax.common.order.enums.OrderType;
import com.zetax.common.order.enums.Side;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** BF-02 decisions 12, 13, 19: OrderAccepted carries the full order and can't hold impossible values. */
class OrderAcceptedTest {

    private static OrderAccepted accepted(long sourceOffset, long priceTicks, long qty, long acceptedAt) {
        return new OrderAccepted("2-1532-0", "ABC", sourceOffset, "order_1", "alice",
                Side.BUY, OrderType.LIMIT, priceTicks, qty, acceptedAt);
    }

    @Test
    void validEvent_keepsItsValues_andIsEqualToSameValues() {
        OrderAccepted e = accepted(1532, 10100, 70, 1_763_000_000_000L);

        assertEquals("2-1532-0", e.eventId());
        assertEquals(70, e.qty());
        assertEquals(accepted(1532, 10100, 70, 1_763_000_000_000L), e);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void qtyNotPositive_throws(long qty) {
        assertThrows(IllegalArgumentException.class, () -> accepted(1532, 10100, qty, 0));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void priceTicksNotPositive_throws(long priceTicks) {
        assertThrows(IllegalArgumentException.class, () -> accepted(1532, priceTicks, 70, 0));
    }

    @Test
    void negativeOffsetOrTime_throws() {
        assertThrows(IllegalArgumentException.class, () -> accepted(-1, 10100, 70, 0));
        assertThrows(IllegalArgumentException.class, () -> accepted(1532, 10100, 70, -1));
    }

    @Test
    void nullFields_throwWithTheFieldName() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> new OrderAccepted(
                "2-1532-0", "ABC", 1532, "order_1", null, Side.BUY, OrderType.LIMIT, 10100, 70, 0));
        assertEquals("userId", e.getMessage());
        assertThrows(NullPointerException.class, () -> new OrderAccepted(
                null, "ABC", 1532, "order_1", "alice", Side.BUY, OrderType.LIMIT, 10100, 70, 0));
        assertThrows(NullPointerException.class, () -> new OrderAccepted(
                "2-1532-0", null, 1532, "order_1", "alice", Side.BUY, OrderType.LIMIT, 10100, 70, 0));
        assertThrows(NullPointerException.class, () -> new OrderAccepted(
                "2-1532-0", "ABC", 1532, null, "alice", Side.BUY, OrderType.LIMIT, 10100, 70, 0));
        assertThrows(NullPointerException.class, () -> new OrderAccepted(
                "2-1532-0", "ABC", 1532, "order_1", "alice", null, OrderType.LIMIT, 10100, 70, 0));
        assertThrows(NullPointerException.class, () -> new OrderAccepted(
                "2-1532-0", "ABC", 1532, "order_1", "alice", Side.BUY, null, 10100, 70, 0));
    }
}
