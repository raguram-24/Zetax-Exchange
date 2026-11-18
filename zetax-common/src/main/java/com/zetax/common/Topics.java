package com.zetax.common;

/** Kafka topic names and partition count (D-11, D-18). Both topics are keyed by symbol. */
public final class Topics {

    /** The journal: place / cancel / symbol admin commands. Retention forever, no compaction. */
    public static final String COMMANDS = "commands";

    /** Engine output: accepted, rejected, trade, cancelled events. */
    public static final String TRADES = "trades";

    /** Fixed forever (D-18). Changing it re-maps symbols to partitions and breaks replay. */
    public static final int PARTITIONS = 3;

    private Topics() {
    }
}
