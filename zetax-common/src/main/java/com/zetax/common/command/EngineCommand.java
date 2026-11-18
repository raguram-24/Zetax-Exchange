package com.zetax.common.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PlaceOrder.class, name = "PLACE_ORDER")
})
public sealed interface EngineCommand permits PlaceOrder {

    String symbol();
}
