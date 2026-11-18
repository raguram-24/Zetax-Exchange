package com.zetax.common.event;

import java.util.Objects;

/**
 * One match between a resting (maker) and an incoming (taker) order, at the maker's price (R-02).
 * {@code tradeId} = {@code "trade_" + eventId} (BF-02 decision 20); {@code executedAt} = the command's Kafka record
 * timestamp, epoch millis (decision 13).
 */
public record TradeExecuted(
        String eventId,
        String symbol,
        long sourceOffset,
        String tradeId,
        long priceTicks,
        long qty,
        String makerOrderId,
        String takerOrderId,
        String makerUserId,
        String takerUserId,
        long executedAt) implements EngineEvent {

    public TradeExecuted {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(symbol, "symbol");
        Objects.requireNonNull(tradeId, "tradeId");
        Objects.requireNonNull(makerOrderId, "makerOrderId");
        Objects.requireNonNull(takerOrderId, "takerOrderId");
        Objects.requireNonNull(makerUserId, "makerUserId");
        Objects.requireNonNull(takerUserId, "takerUserId");
        if (sourceOffset < 0) {
            throw new IllegalArgumentException("sourceOffset must be >= 0, was " + sourceOffset);
        }
        if (priceTicks <= 0) {
            throw new IllegalArgumentException("priceTicks must be > 0, was " + priceTicks);
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be > 0, was " + qty);
        }
        if (executedAt < 0) {
            throw new IllegalArgumentException("executedAt must be >= 0, was " + executedAt);
        }
        if (makerOrderId.equals(takerOrderId)) {
            throw new IllegalArgumentException("an order can't trade with itself: " + makerOrderId);
        }
    }
}
