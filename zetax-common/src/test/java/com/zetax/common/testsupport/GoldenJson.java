package com.zetax.common.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import tools.jackson.databind.json.JsonMapper;

/** Golden-file helpers for contract tests (BF-02 decisions 17, 24). */
public final class GoldenJson {

    public static final JsonMapper MAPPER = JsonMapper.builder().build();

    private GoldenJson() {
    }

    /** Reads {@code src/test/resources/golden/<path>}, e.g. {@code "command/place-order.json"}. */
    public static String read(String path) throws IOException {
        try (InputStream in = GoldenJson.class.getResourceAsStream("/golden/" + path)) {
            if (in == null) {
                throw new IllegalStateException("missing golden file: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Compares as JSON trees: spacing and field order don't matter, only names and values. */
    public static void assertSameJson(String expected, String actual) {
        assertEquals(MAPPER.readTree(expected), MAPPER.readTree(actual));
    }
}
