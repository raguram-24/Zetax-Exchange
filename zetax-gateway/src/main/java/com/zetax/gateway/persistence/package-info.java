/**
 * Persistence writer (D-04): consumes the trades topic in a consumer group, batch-writes to Postgres,
 * idempotent by event_id, commits offsets after the DB commit.
 * <p>TODO (phase 3): see TF-05, B7.
 */
package com.zetax.gateway.persistence;
