/**
 * Events written by the engine to the {@code trades} topic:
 * OrderAccepted, OrderRejected, TradeExecuted, OrderCancelled, CancelRejected, Symbol* events.
 * <p>Every event carries a deterministic {@code eventId} (partition-offset-n) so consumers can drop duplicates.
 * <p>TODO (phase 1/2): define them as Java records. See "Business and Technical Flow" B2 and B5.
 */
package com.zetax.common.event;
