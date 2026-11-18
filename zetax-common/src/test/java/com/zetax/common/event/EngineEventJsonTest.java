package com.zetax.common.event;

import static com.zetax.common.testsupport.GoldenJson.MAPPER;
import static com.zetax.common.testsupport.GoldenJson.assertSameJson;
import static com.zetax.common.testsupport.GoldenJson.read;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zetax.common.order.enums.OrderType;
import com.zetax.common.order.enums.Side;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * Golden-file contract tests for the {@code trades} topic (BF-02 decisions 17, 24).
 * The Go market-data service reads the same JSON: a change here is a change on both sides.
 */
class EngineEventJsonTest {

    private static final OrderAccepted ORDER_ACCEPTED = new OrderAccepted("2-1532-0", "ABC", 1532,
            "order_1", "alice", Side.BUY, OrderType.LIMIT, 10100, 70, 1_763_000_000_000L);
    private static final TradeExecuted TRADE_EXECUTED = new TradeExecuted("2-1532-1", "ABC", 1532,
            "trade_2-1532-1", 10000, 30, "order_bob", "order_alice", "bob", "alice", 1_763_000_000_000L);

    @Test
    void orderAccepted_writesGoldenJson() throws IOException {
        assertSameJson(read("event/order-accepted.json"),
                MAPPER.writerFor(EngineEvent.class).writeValueAsString(ORDER_ACCEPTED));
    }

    @Test
    void orderAccepted_readsGoldenJson() throws IOException {
        assertEquals(ORDER_ACCEPTED, MAPPER.readValue(read("event/order-accepted.json"), EngineEvent.class));
    }

    @Test
    void tradeExecuted_writesGoldenJson() throws IOException {
        assertSameJson(read("event/trade-executed.json"),
                MAPPER.writerFor(EngineEvent.class).writeValueAsString(TRADE_EXECUTED));
    }

    @Test
    void tradeExecuted_readsGoldenJson() throws IOException {
        assertEquals(TRADE_EXECUTED, MAPPER.readValue(read("event/trade-executed.json"), EngineEvent.class));
    }
}
