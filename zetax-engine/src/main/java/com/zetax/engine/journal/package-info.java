/**
 * Journal abstraction (D-11): KafkaJournal for real runs, InMemoryJournal for unit tests.
 * <p>TODO (phase 2): replay from offset 0, silent until the last published sourceOffset (TF-06).
 */
package com.zetax.engine.journal;
