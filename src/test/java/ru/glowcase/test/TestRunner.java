package ru.glowcase.test;

public class TestRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("       GlowCases Test Suite Execution            ");
        System.out.println("=================================================");

        long startTime = System.currentTimeMillis();
        int passed = 0;
        int failed = 0;

        try {
            ColorUtilTest.run();
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAILED] ColorUtilTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            CaseRarityTest.run();
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAILED] CaseRarityTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            CaseModelTest.run();
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAILED] CaseModelTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            CaseBlockTest.run();
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAILED] CaseBlockTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            CaseUserTest.run();
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAILED] CaseUserTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("=================================================");
        System.out.printf("  Summary: %d Passed, %d Failed (Time: %d ms)%n", passed, failed, elapsed);
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}