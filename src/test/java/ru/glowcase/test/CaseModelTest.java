package ru.glowcase.test;

import org.bukkit.Material;
import ru.glowcase.case_.Case;
import ru.glowcase.case_.CaseItem;
import ru.glowcase.case_.CaseRarity;

import java.util.*;

public class CaseModelTest {

    public static void run() {
        System.out.println("  [RUNNING] CaseModelTest...");

        CaseItem item1 = new CaseItem("vip", Material.IRON_INGOT, "&aVIP", Collections.singletonList("&7VIP rank"),
                CaseRarity.COMMON, 50.0, false, Collections.singletonList("lp user {player} parent set vip"), null, false);
        CaseItem item2 = new CaseItem("premium", Material.GOLD_INGOT, "&ePREMIUM", Collections.singletonList("&7Premium rank"),
                CaseRarity.RARE, 30.0, false, Collections.singletonList("lp user {player} parent set premium"), null, false);
        CaseItem item3 = new CaseItem("dragon", Material.DRAGON_EGG, "&#ff3333DRAGON", Collections.singletonList("&7Dragon rank"),
                CaseRarity.LEGENDARY, 20.0, true, Collections.singletonList("lp user {player} parent set dragon"), null, true);

        List<CaseItem> items = Arrays.asList(item1, item2, item3);
        Case testCase = new Case("donate", "&6Donate Case", null, null, items);

        assertEqual(testCase.getId(), "donate", "Case ID should be donate");
        assertEqual(testCase.getItems().size(), 3, "Case should have 3 items");
        assertCondition(Math.abs(testCase.getTotalWeight() - 100.0) < 0.001, "Total weight should be 100.0");

        assertEqual(item3.isBroadcast(), true, "Dragon item should have broadcast = true");
        assertEqual(item3.isGlowing(), true, "Dragon item should have glowing = true");
        assertEqual(item3.getRarity(), CaseRarity.LEGENDARY, "Dragon rarity should be LEGENDARY");

        // Statistical distribution simulation (50,000 rolls)
        System.out.println("    -> Simulating 50,000 case rolls to verify weighted distribution...");
        int rolls = 50_000;
        Map<String, Integer> counts = new HashMap<>();
        counts.put("vip", 0);
        counts.put("premium", 0);
        counts.put("dragon", 0);

        for (int i = 0; i < rolls; i++) {
            CaseItem dropped = testCase.getRandomItem();
            assertCondition(dropped != null, "Dropped item cannot be null");
            counts.put(dropped.getId(), counts.get(dropped.getId()) + 1);
        }

        double vipPct = (counts.get("vip") / (double) rolls) * 100.0;
        double premPct = (counts.get("premium") / (double) rolls) * 100.0;
        double dragPct = (counts.get("dragon") / (double) rolls) * 100.0;

        System.out.printf("    -> Results: VIP: %.2f%% (Expected 50%%), PREMIUM: %.2f%% (Expected 30%%), DRAGON: %.2f%% (Expected 20%%)%n", vipPct, premPct, dragPct);

        assertCondition(Math.abs(vipPct - 50.0) < 1.5, "VIP drop percentage should be ~50% (+-1.5%)");
        assertCondition(Math.abs(premPct - 30.0) < 1.5, "PREMIUM drop percentage should be ~30% (+-1.5%)");
        assertCondition(Math.abs(dragPct - 20.0) < 1.5, "DRAGON drop percentage should be ~20% (+-1.5%)");

        System.out.println("  [PASSED] CaseModelTest");
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