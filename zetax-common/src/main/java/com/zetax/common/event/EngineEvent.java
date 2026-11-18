package com.zetax.common.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/** Everything the engine writes to the {@code trades} topic (BF-02). Labels: decision 24. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderAccepted.class, name = "ORDER_ACCEPTED"),
    @JsonSubTypes.Type(value = TradeExecuted.class, name = "TRADE_EXECUTED")
})
public sealed interface EngineEvent permits OrderAccepted, TradeExecuted {

    /** {@code partition-offset-n}; consumers de-duplicate on it. */
    String eventId();

    /** Kafka key. */
    String symbol();

    /** Offset of the command that produced this event (replay, TF-06). */
    long sourceOffset();
}
