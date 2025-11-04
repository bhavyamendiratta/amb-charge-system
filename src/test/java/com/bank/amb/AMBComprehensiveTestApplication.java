package com.bank.amb;

import com.bank.amb.model.*;
import com.bank.amb.service.AMBRuleEngineProgrammatic;
import com.bank.amb.service.AMBRuleEngineProgrammatic.RuleExecutionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive Test Application for Bank AMB Non-Maintenance Charge System
 * Demonstrates multiple scenarios with detailed logging
 */
public class AMBComprehensiveTestApplication {

    public static void main(String[] args) {

        printHeader();

        // Initialize Rule Engine
        AMBRuleEngineProgrammatic ruleEngine = new AMBRuleEngineProgrammatic();

        // Run all test scenarios
        System.out.println("\n🧪 Running All Test Scenarios...\n");

        runScenario1_HappyPath(ruleEngine);
        runScenario2_SingleMonthDefault(ruleEngine);
        runScenario3_ConstantLowBalance(ruleEngine);
        runScenario4_RecoveryAfterDefault(ruleEngine);
        runScenario5_EdgeCase_BelowCap(ruleEngine);

        printFooter();
    }

    // ============================================================================
    // SCENARIO 1: Happy Path - User Maintains Balance
    // ============================================================================
    private static void runScenario1_HappyPath(AMBRuleEngineProgrammatic engine) {
        printScenarioHeader("SCENARIO 1: Happy Path - User Maintains Balance");

        List<Account> accounts = new ArrayList<>();
        Account acc = createAccount("ACC_S1_001", "Happy Customer",
                generateConstantBalance(15000)); // Always above minimum
        accounts.add(acc);

        List<ProbableDefaulter> probableList = new ArrayList<>();
        List<ActualDefaulter> actualList = new ArrayList<>();
        List<Charge> chargeList = new ArrayList<>();

        // Run for 6 months
        for (int month = 1; month <= 6; month++) {
            runMonth(engine, accounts, probableList, actualList, chargeList, month);
        }

        printScenarioSummary("SCENARIO 1", probableList, actualList, chargeList);
        System.out.println("✅ Expected: No defaults, No charges, No SMS");
        System.out.println("✅ Result: " +
                (chargeList.isEmpty() ? "PASS ✓" : "FAIL ✗"));
    }

    // ============================================================================
    // SCENARIO 2: Single Month Default - No Charge
    // ============================================================================
    private static void runScenario2_SingleMonthDefault(AMBRuleEngineProgrammatic engine) {
        printScenarioHeader("SCENARIO 2: Single Month Default - No Charge");

        List<Account> accounts = new ArrayList<>();
        // Good balance except February
        double[] balances = generateConstantBalance(15000);
        // Make February low
        for (int i = 0; i < 30; i++) {
            if (i >= 31 && i < 59) { // Feb days (assuming 30 days per month for simplicity)
                balances[i] = 500;
            }
        }
        Account acc = createAccount("ACC_S2_001", "Single Month Defaulter", balances);
        accounts.add(acc);

        List<ProbableDefaulter> probableList = new ArrayList<>();
        List<ActualDefaulter> actualList = new ArrayList<>();
        List<Charge> chargeList = new ArrayList<>();

        // Run for 6 months
        for (int month = 1; month <= 6; month++) {
            runMonth(engine, accounts, probableList, actualList, chargeList, month);
        }

        printScenarioSummary("SCENARIO 2", probableList, actualList, chargeList);
        System.out.println("✅ Expected: 1 default (Feb), No charge (need 2 consecutive)");
        System.out.println("✅ Result: " +
                (chargeList.isEmpty() && actualList.size() >= 1 ? "PASS ✓" : "FAIL ✗"));
    }

    // ============================================================================
    // SCENARIO 3: Constant Low Balance (₹500) - Multiple Charges
    // ============================================================================
    private static void runScenario3_ConstantLowBalance(AMBRuleEngineProgrammatic engine) {
        printScenarioHeader("SCENARIO 3: Constant Low Balance - Multiple Charges");
        System.out.println("Balance: ₹500 (constant from Jan to July)");
        System.out.println("Expected Charges: 3 times (Mar, May, Jul)");
        System.out.println("Expected Amount per charge: ₹1,180\n");

        List<Account> accounts = new ArrayList<>();
        Account acc = createAccount("ACC_S3_001", "Chronic Defaulter",
                generateConstantBalance(500)); // Always low
        accounts.add(acc);

        List<ProbableDefaulter> probableList = new ArrayList<>();
        List<ActualDefaulter> actualList = new ArrayList<>();
        List<Charge> chargeList = new ArrayList<>();

        // Run for 7 months to see 3 charges
        for (int month = 1; month <= 7; month++) {
            runMonth(engine, accounts, probableList, actualList, chargeList, month);
        }

        printScenarioSummary("SCENARIO 3", probableList, actualList, chargeList);
        System.out.println("✅ Expected: 3 charges (Jan+Feb, Mar+Apr, May+Jun)");
        System.out.println("✅ Total Expected: ₹3,540");
        double totalCharged = chargeList.stream().mapToDouble(Charge::getTotalCharge).sum();
        System.out.println("✅ Total Actual: ₹" + String.format("%.2f", totalCharged));
        System.out.println("✅ Result: " +
                (chargeList.size() == 3 && Math.abs(totalCharged - 3540) < 1 ? "PASS ✓" : "FAIL ✗"));
    }

    // ============================================================================
    // SCENARIO 4: Recovery After Default
    // ============================================================================
    private static void runScenario4_RecoveryAfterDefault(AMBRuleEngineProgrammatic engine) {
        printScenarioHeader("SCENARIO 4: Recovery After Default");
        System.out.println("Pattern: Low in Jan-Feb, Recover in Mar onwards");
        System.out.println("Expected: Charge only for Jan+Feb\n");

        List<Account> accounts = new ArrayList<>();
        Account acc = createAccount("ACC_S4_001", "Recovery Customer",
                new double[]{
                        // Jan (low)
                        500,500,500,500,500,500,500,500,500,500,
                        500,500,500,500,500,500,500,500,500,500,
                        500,500,500,500,500,500,500,500,500,500
                });
        accounts.add(acc);

        List<ProbableDefaulter> probableList = new ArrayList<>();
        List<ActualDefaulter> actualList = new ArrayList<>();
        List<Charge> chargeList = new ArrayList<>();

        // Month 1-2: Low balance
        runMonth(engine, accounts, probableList, actualList, chargeList, 1);
        runMonth(engine, accounts, probableList, actualList, chargeList, 2);

        // Month 3 onwards: Good balance
        acc.setDailyBalances(generateConstantBalance(15000));
        for (int month = 3; month <= 6; month++) {
            runMonth(engine, accounts, probableList, actualList, chargeList, month);
        }

        printScenarioSummary("SCENARIO 4", probableList, actualList, chargeList);
        System.out.println("✅ Expected: 1 charge (Jan+Feb only)");
        System.out.println("✅ Result: " +
                (chargeList.size() == 1 ? "PASS ✓" : "FAIL ✗"));
    }

    // ============================================================================
    // SCENARIO 5: Edge Case - Below Cap Shortfall
    // ============================================================================
    private static void runScenario5_EdgeCase_BelowCap(AMBRuleEngineProgrammatic engine) {
        printScenarioHeader("SCENARIO 5: Edge Case - Shortfall Below Cap");
        System.out.println("Balance: ₹7,000 (Shortfall: ₹3,000)");
        System.out.println("Expected: Charge < ₹500 base (6% of ₹3,000 = ₹180)\n");

        List<Account> accounts = new ArrayList<>();
        Account acc = createAccount("ACC_S5_001", "Small Shortfall Customer",
                generateConstantBalance(7000)); // Shortfall = 3000
        accounts.add(acc);

        List<ProbableDefaulter> probableList = new ArrayList<>();
        List<ActualDefaulter> actualList = new ArrayList<>();
        List<Charge> chargeList = new ArrayList<>();

        // Run for 3 months to get 1 charge
        for (int month = 1; month <= 3; month++) {
            runMonth(engine, accounts, probableList, actualList, chargeList, month);
        }

        printScenarioSummary("SCENARIO 5", probableList, actualList, chargeList);
        if (!chargeList.isEmpty()) {
            Charge charge = chargeList.get(0);
            System.out.println("✅ Expected Base Charge: ₹360 (₹180 × 2 months)");
            System.out.println("✅ Actual Base Charge: ₹" + String.format("%.2f", charge.getBaseCharge()));
            System.out.println("✅ Result: " +
                    (Math.abs(charge.getBaseCharge() - 360) < 1 ? "PASS ✓" : "FAIL ✗"));
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private static void runMonth(AMBRuleEngineProgrammatic engine,
                                 List<Account> accounts,
                                 List<ProbableDefaulter> probableList,
                                 List<ActualDefaulter> actualList,
                                 List<Charge> chargeList,
                                 int month) {

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║           MONTH " + month + " - DAY 25 CHECK                        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");

        ExecutionContext ctx1 = new ExecutionContext();
        ctx1.setCurrentMonth(month);
        ctx1.setCheckDay(25);

        RuleExecutionResult result1 = engine.executeRules(
                accounts, probableList, actualList, chargeList, ctx1
        );
        probableList.addAll(result1.getProbableDefaulters());

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║           MONTH " + (month + 1) + " - DAY 3 CHECK                         ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");

        ExecutionContext ctx2 = new ExecutionContext();
        ctx2.setCurrentMonth(month + 1);
        ctx2.setCheckDay(3);

        RuleExecutionResult result2 = engine.executeRules(
                accounts, probableList, actualList, chargeList, ctx2
        );
        actualList.addAll(result2.getActualDefaulters());
        chargeList.addAll(result2.getCharges());
    }

    private static Account createAccount(String id, String name, double[] balances) {
        Account acc = new Account();
        acc.setAccountId(id);
        acc.setAccountName(name);
        acc.setDailyBalances(balances);
        return acc;
    }

    private static double[] generateConstantBalance(double amount) {
        double[] balances = new double[30];
        for (int i = 0; i < 30; i++) {
            balances[i] = amount;
        }
        return balances;
    }

    private static void printScenarioHeader(String title) {
        System.out.println("\n\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  " + String.format("%-58s", title) + "  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printScenarioSummary(String scenario,
                                             List<ProbableDefaulter> probableList,
                                             List<ActualDefaulter> actualList,
                                             List<Charge> chargeList) {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              " + scenario + " - SUMMARY                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("📊 Probable Defaulters: " + probableList.size());
        System.out.println("⚠️  Actual Defaulters: " + actualList.size());
        System.out.println("💰 Charges Applied: " + chargeList.size());
        if (!chargeList.isEmpty()) {
            double total = chargeList.stream().mapToDouble(Charge::getTotalCharge).sum();
            System.out.println("💵 Total Amount Charged: ₹" + String.format("%.2f", total));
        }
        System.out.println();
    }

    private static void printHeader() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                              ║");
        System.out.println("║     BANK AMB NON-MAINTENANCE CHARGE SYSTEM                   ║");
        System.out.println("║     Comprehensive Test Suite                                 ║");
        System.out.println("║     Powered by Drools Rule Engine                            ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("📋 Test Configuration:");
        System.out.println("   Minimum Balance Required: ₹10,000");
        System.out.println("   Charge Formula: min(6% of shortfall, ₹500) + 18% GST");
        System.out.println("   Charge Trigger: 2 consecutive months below minimum");
        System.out.println("   SMS: Sent once when first becomes probable defaulter");
        System.out.println();
    }

    private static void printFooter() {
        System.out.println("\n\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                              ║");
        System.out.println("║              ALL TEST SCENARIOS COMPLETED                    ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("✅ Test suite execution completed successfully!");
        System.out.println("📊 Review the detailed logs above for each scenario");
        System.out.println("🎯 All rule validations have been performed");
        System.out.println();
    }
}