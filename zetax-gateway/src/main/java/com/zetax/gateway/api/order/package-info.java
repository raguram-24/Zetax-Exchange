/**
 * Order API (D-14): POST /api/orders, POST /api/orders/{id}/cancel, history GETs.
 * Answers 202 Accepted once the command is in Kafka. Trader identified by X-User-Id (D-16).
 * <p>TODO (phase 3): see TF-01, TF-03, TF-07.
 */
package com.zetax.gateway.api.order;
