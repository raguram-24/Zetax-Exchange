/**
 * STOMP WebSocket client (D-14). Destinations:
 *   /topic/book.{symbol}    book deltas (after an initial snapshot)
 *   /topic/trades.{symbol}  live trades
 *   /user/queue/orders      my order updates
 * Connect with ?userId= (D-16). On a sequence gap, fetch a fresh snapshot.
 *
 * TODO (phase 3b): pick a STOMP client library (ask first), connect, subscribe, reconnect.
 */
export {}
