/**
 * Market-data publisher: consumes the trades topic, keeps a price-level view per symbol,
 * pushes over STOMP WebSocket: /topic/book.{symbol}, /topic/trades.{symbol}, /user/queue/orders.
 * <p>TODO (phase 3): snapshot + deltas with sequence numbers (TF-04).
 */
package com.zetax.gateway.marketdata;
