package com.civicpulse.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContentNormalizerTest {

    @Test
    void testNormalizeStripsPunctuationAndExtraSpaces() {
        String input = "  Huge pothole near   Main Road, block #4!   ";
        String normalized = ContentNormalizer.normalize(input);
        assertEquals("huge pothole near main road block 4", normalized);
    }

    @Test
    void testComputeHashMatchesIdenticalContentWithDifferentFormatting() {
        String title1 = "Garbage not collected!";
        String desc1 = "Waste has been piling up for 3 days on 5th cross.";

        String title2 = "garbage NOT   collected.";
        String desc2 = "Waste has been piling up for 3 days on 5th cross!";

        String hash1 = ContentNormalizer.computeHash(title1, desc1);
        String hash2 = ContentNormalizer.computeHash(title2, desc2);

        assertNotNull(hash1);
        assertEquals(hash1, hash2, "Hashes should match for semantically identical content with varied formatting");
    }

    @Test
    void testComputeHashDiffersForDifferentContent() {
        String hash1 = ContentNormalizer.computeHash("Water leakage", "Main pipe leaking");
        String hash2 = ContentNormalizer.computeHash("Street light broken", "Lamp post damaged");

        assertNotEquals(hash1, hash2);
    }
}
