package com.zetax.common.command;

import static com.zetax.common.testsupport.GoldenJson.MAPPER;
import static com.zetax.common.testsupport.GoldenJson.assertSameJson;
import static com.zetax.common.testsupport.GoldenJson.read;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zetax.common.order.enums.OrderType;
import com.zetax.common.order.enums.Side;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * Golden-file contract tests for the {@code commands} topic (BF-02 decisions 17, 24).
 * A failure means the message contract changed: decide it on purpose, never by accident.
 */
class EngineCommandJsonTest {

    private static final PlaceOrder PLACE_ORDER =
            new PlaceOrder("order_1", "alice", "ABC", Side.BUY, OrderType.LIMIT, 10100, 70);

    @Test
    void placeOrder_writesGoldenJson() throws IOException {
        assertSameJson(read("command/place-order.json"),
                MAPPER.writerFor(EngineCommand.class).writeValueAsString(PLACE_ORDER));
    }

    @Test
    void placeOrder_readsGoldenJson() throws IOException {
        assertEquals(PLACE_ORDER, MAPPER.readValue(read("command/place-order.json"), EngineCommand.class));
    }
}
