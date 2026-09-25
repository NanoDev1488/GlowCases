package ru.glowcase.test;

import ru.glowcase.user.CaseUser;
import java.util.UUID;

public class CaseUserTest {

    public static void run() {
        System.out.println("  [RUNNING] CaseUserTest...");

        UUID uuid = UUID.randomUUID();
        CaseUser user = new CaseUser(uuid, "Steve");

        assertEqual(user.getUuid(), uuid, "UUID should match");
        assertEqual(user.getName(), "Steve", "Name should match");
        assertEqual(user.getKeys("donate"), 0, "Initial keys should be 0");
        assertEqual(user.getTotalOpened(), 0, "Initial total opened should be 0");

        // Add keys
        user.addKeys("donate", 5);
        assertEqual(user.getKeys("donate"), 5, "Keys should be 5 after adding 5");
        assertEqual(user.isDirty(), true, "User should be marked dirty after modifying keys");

        // Take keys successfully
        boolean took = user.takeKeys("donate", 2);
        assertEqual(took, true, "Taking 2 keys should succeed");
        assertEqual(user.getKeys("donate"), 3, "Keys remaining should be 3");

        // Fail to take more keys than available
        boolean tookTooMany = user.takeKeys("donate", 10);
        assertEqual(tookTooMany, false, "Taking 10 keys should fail");
        assertEqual(user.getKeys("donate"), 3, "Keys should still be 3 after failed take");

        // Test statistics
        user.incrementOpened("donate");
        user.incrementOpened("donate");
        user.incrementOpened("runes");
        assertEqual(user.getTotalOpened(), 3, "Total opened should be 3");
        assertEqual(user.getCaseOpened("donate"), 2, "Donate opened should be 2");
        assertEqual(user.getCaseOpened("runes"), 1, "Runes opened should be 1");

        System.out.println("  [PASSED] CaseUserTest");
    }

    private static void assertEqual(Object actual, Object expected, String message) {
        if (actual == null && expected == null) return;
        if (actual != null && actual.equals(expected)) return;
        throw new AssertionError(message + " (Expected: " + expected + ", Got: " + actual + ")");
    }
}