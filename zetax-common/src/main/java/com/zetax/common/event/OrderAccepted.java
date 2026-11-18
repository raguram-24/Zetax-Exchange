package com.zetax.common.event;

import com.zetax.common.order.enums.OrderType;
import com.zetax.common.order.enums.Side;
import java.util.Objects;

/**
 * The engine accepted an order. Carries the full order so the persistor can save it from this event alone
 * (BF-02 decision 12). {@code acceptedAt} = the command's Kafka record timestamp, epoch millis (decision 13).
 */
public record OrderAccepted(
        String eventId,
        String symbol,
        long sourceOffset,
        String orderId,
        String userId,
        Side side,
        OrderType orderType,
        long priceTicks,
        long qty,
        long acceptedAt) implements EngineEvent {

    public OrderAccepted {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(symbol, "symbol");
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(orderType, "orderType");
        if (sourceOffset < 0) {
            throw new IllegalArgumentException("sourceOffset must be >= 0, was " + sourceOffset);
        }
        if (priceTicks <= 0) {
            throw new IllegalArgumentException("priceTicks must be > 0, was " + priceTicks);
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be > 0, was " + qty);
        }
        if (acceptedAt < 0) {
            throw new IllegalArgumentException("acceptedAt must be >= 0, was " + acceptedAt);
        }
    }
}
