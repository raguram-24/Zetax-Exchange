package com.zetax.common.command;

import java.util.Objects;

import com.zetax.common.order.enums.OrderType;
import com.zetax.common.order.enums.Side;

public record PlaceOrder(
        String orderId,
        String userId,
        String symbol,
        Side side,
        OrderType orderType,
        long priceTicks,
        long qty
        ) implements EngineCommand {

    public PlaceOrder {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(symbol, "symbol");
        Objects.requireNonNull(orderType, "orderType");
        Objects.requireNonNull(side, "side");

        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be greater than zero");
        }
        if (priceTicks <= 0) {
            throw new IllegalArgumentException("priceTicks must be greater than zero");
        }

    }
}
