package ru.glowcase.test;

import ru.glowcase.util.ColorUtil;
import java.util.Arrays;
import java.util.List;

public class ColorUtilTest {

    public static void run() {
        System.out.println("  [RUNNING] ColorUtilTest...");

        // Test null and empty
        assertEqual(ColorUtil.color((String) null), null, "Null string should return null");
        assertEqual(ColorUtil.color(""), "", "Empty string should return empty string");

        // Test standard color codes
        String colored = ColorUtil.color("&aHello &cWorld");
        assertCondition(colored.contains("Hello") && colored.contains("World"), "Should retain text content");
        assertCondition(!colored.contains("&a") && !colored.contains("&c"), "Should translate &a and &c codes");

        // Test hex colors
        String hexColored = ColorUtil.color("&#fb6b02Glow");
        assertCondition(hexColored.contains("Glow"), "Should retain hex text");
        assertCondition(!hexColored.contains("&#fb6b02"), "Should replace hex pattern");

        // Test list colorization
        List<String> inputList = Arrays.asList("&eLine 1", "&bLine 2");
        List<String> outputList = ColorUtil.color(inputList);
        assertEqual(outputList.size(), 2, "List size should remain 2");
        assertCondition(!outputList.get(0).contains("&e"), "First line should be colored");
        assertCondition(!outputList.get(1).contains("&b"), "Second line should be colored");

        System.out.println("  [PASSED] ColorUtilTest");
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