package ru.glowcase.test;

import ru.glowcase.case_.CaseRarity;

public class CaseRarityTest {

    public static void run() {
        System.out.println("  [RUNNING] CaseRarityTest...");

        assertEqual(CaseRarity.fromString("COMMON"), CaseRarity.COMMON, "COMMON should parse to COMMON");
        assertEqual(CaseRarity.fromString("legendary"), CaseRarity.LEGENDARY, "Case-insensitive legendary should parse to LEGENDARY");
        assertEqual(CaseRarity.fromString("MYTHICAL"), CaseRarity.MYTHICAL, "MYTHICAL should parse to MYTHICAL");

        assertEqual(CaseRarity.fromString("UNKNOWN_RARITY"), CaseRarity.COMMON, "Unknown rarity should fallback to COMMON");
        assertEqual(CaseRarity.fromString(null), CaseRarity.COMMON, "Null rarity should fallback to COMMON");

        assertEqual(CaseRarity.LEGENDARY.getDefaultName(), "Легендарный", "LEGENDARY default name");
        assertCondition(CaseRarity.LEGENDARY.getFireworkColor() != null, "Firework color should not be null");
        assertCondition(CaseRarity.LEGENDARY.getColorCode().length() > 0, "Color code should not be empty");

        System.out.println("  [PASSED] CaseRarityTest");
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