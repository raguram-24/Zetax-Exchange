package com.zetax.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TopicsTest {

    @Test
    void partitionCountIsFixed() {
        assertEquals(3, Topics.PARTITIONS, "Partition count is fixed by D-18; changing it breaks replay");
    }
}
