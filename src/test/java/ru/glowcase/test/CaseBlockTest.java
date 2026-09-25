package ru.glowcase.test;

import ru.glowcase.case_.CaseBlock;

public class CaseBlockTest {

    public static void run() {
        System.out.println("  [RUNNING] CaseBlockTest...");

        CaseBlock block1 = new CaseBlock("world", 100, 64, -200, "donate");
        CaseBlock block2 = new CaseBlock("world", 100, 64, -200, "donate");
        CaseBlock block3 = new CaseBlock("world_nether", 100, 64, -200, "donate");

        assertEqual(block1.getWorldName(), "world", "World name should be world");
        assertEqual(block1.getX(), 100, "X coordinate should be 100");
        assertEqual(block1.getY(), 64, "Y coordinate should be 64");
        assertEqual(block1.getZ(), -200, "Z coordinate should be -200");
        assertEqual(block1.getCaseId(), "donate", "Case ID should be donate");

        // Test equality and hashing
        assertEqual(block1, block2, "Blocks at the same location should be equal");
        assertEqual(block1.hashCode(), block2.hashCode(), "HashCodes should match for equal blocks");
        assertCondition(!block1.equals(block3), "Blocks in different worlds should not be equal");

        System.out.println("  [PASSED] CaseBlockTest");
    }

    private static void assertEqual(Object actual, Object expected, String message) {
        if (actual == null && expected == null) return;
        if (actual != null && actual.equals(expected)) return;
        throw new AssertionError(message + " (Expected: " + expected + ", Got: " + actual + ")");
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Condition failed: " + message);
        }
    }
}