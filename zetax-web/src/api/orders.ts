/**
 * REST client for the gateway (D-14): POST /api/orders, POST /api/orders/{id}/cancel, history GETs.
 * Every request sends the X-User-Id header (D-16). Responses are 202 Accepted; results arrive over WebSocket.
 *
 * TODO (phase 3b): placeOrder(), cancelOrder(), getMyOrders(), getTrades().
 */
export {}
