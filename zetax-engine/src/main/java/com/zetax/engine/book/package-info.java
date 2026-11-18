/**
 * The order book and matching logic: plain Java, no Spring, no Kafka.
 * <p>TODO (phase 1): Order, PriceLevel, OrderBook (TreeMap of price levels + order index),
 * limit / market / cancel with price-time priority, self-trade prevention, price band (D-13).
 * Unit-test every rule. See "Business and Technical Flow" B3.
 */
package com.zetax.engine.book;
