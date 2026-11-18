package com.zetax.common.order.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** BF-02 decision 23: enums are stored in the DB as fixed, gapped codes (never ordinal()). */
class EnumCodesTest {

    @ParameterizedTest
    @EnumSource(Side.class)
    void side_codeRoundTrip(Side side) {
        assertEquals(side, Side.fromCode(side.code()));
    }

    @ParameterizedTest
    @EnumSource(OrderType.class)
    void orderType_codeRoundTrip(OrderType type) {
        assertEquals(type, OrderType.fromCode(type.code()));
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void orderStatus_codeRoundTrip(OrderStatus status) {
        assertEquals(status, OrderStatus.fromCode(status.code()));
    }

    @Test
    void codes_areUniqueWithinEachEnum() {
        assertEquals(Side.values().length, Arrays.stream(Side.values()).mapToInt(Side::code).distinct().count());
        assertEquals(OrderType.values().length, Arrays.stream(OrderType.values()).mapToInt(OrderType::code).distinct().count());
        assertEquals(OrderStatus.values().length, Arrays.stream(OrderStatus.values()).mapToInt(OrderStatus::code).distinct().count());
    }

    @Test
    void fromCode_unknownCode_throws() {
        assertThrows(IllegalArgumentException.class, () -> Side.fromCode(7));
        assertThrows(IllegalArgumentException.class, () -> OrderType.fromCode(7));
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromCode(7));
    }
}
