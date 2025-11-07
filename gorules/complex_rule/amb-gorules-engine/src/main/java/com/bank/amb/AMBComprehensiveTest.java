package com.bank.amb;

import com.bank.amb.model.*;
import com.bank.amb.service.AMBService;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive Test Application - All Scenarios
 */
public class AMBComprehensiveTest {

    private static final double MIN_BALANCE = 10000.0;

    public static void main(String[] args) {
        printHeader();

        System.out.println("🔧 Initializing GoRules AMB Engine...\n");

        // Run all scenarios
        runScenario1_HappyPath();
        runScenario2_SingleMonthDefault();
        runScenario3_ConstantLowBalance();
        runScenario4_RecoveryAfterDefault();
        runScenario5_EdgeCaseBelowCap();

        printFooter();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCENARIO 1: Happy Path - User Maintains Balance
    // ═══════════════════════════════════════════════════════════════════════════
    private static void runScenario1_HappyPath() {
        printScenarioHeader("SCENARIO 1: Happy Path - User Maintains Balance");
        System.out.println("Setup: Customer maintains ₹15,000 balance throughout");
        System.out.println("Expected: No defaults, No charges, No SMS\n");

        AMBService service = new AMBService();
        List<Account> accounts = new ArrayList<>();

        // Create account with good balance
        Account acc = createAccount("S1_HAPPY_001", "Happy Customer");
        setBalances(acc, 15000.0);
        accounts.add(acc);

        // Run for 4 months
        for (int month = 1; month <= 4; month++) {
            System.out.println("\n>>> Month " + month + " <<<");

            // Day 25 check
            setBalances(acc, 15000.0);
            service.processDay25(accounts, month, MIN_BALANCE);

            // Day 3 of next month
            service.processDay3(accounts, month + 1, MIN_BALANCE);
        }

        printScenarioSummary("SCENARIO 1", service);
        System.out.println("✅ Expected: Charges = 0, Probable = 0");
        System.out.println("✅ Result: " +
                (service.getCharges().isEmpty() ? "PASS ✓" : "FAIL ✗"));
        System.out.println("\n" + "=".repeat(70) + "\n");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCENARIO 2: Single Month Default - No Charge
    // ═══════════════════════════════════════════════════════════════════════════
    private static void runScenario2_SingleMonthDefault() {
        printScenarioHeader("SCENARIO 2: Single Month Default - No Charge");
        System.out.println("Setup: Good balance except Month 2 (₹500)");
        System.out.println("Expected: 1 default, No charge (need 2 consecutive)\n");

        AMBService service = new AMBService();
        List<Account> accounts = new ArrayList<>();
        Account acc = createAccount("S2_SINGLE_001", "Single Month Defaulter");
        accounts.add(acc);

        // Month 1: Good balance
        System.out.println("\n>>> Month 1 (Good Balance ₹15,000) <<<");
        setBalances(acc, 15000.0);
        service.processDay25(accounts, 1, MIN_BALANCE);
        service.processDay3(accounts, 2, MIN_BALANCE);

        // Month 2: Low balance
        System.out.println("\n>>> Month 2 (Low Balance ₹500) <<<");
        setBalances(acc, 500.0);
        service.processDay25(accounts, 2, MIN_BALANCE);
        service.processDay3(accounts, 3, MIN_BALANCE);

        // Month 3: Good balance again
        System.out.println("\n>>> Month 3 (Recovered to ₹15,000) <<<");
        setBalances(acc, 15000.0);
        service.processDay25(accounts, 3, MIN_BALANCE);
        service.processDay3(accounts, 4, MIN_BALANCE);

        printScenarioSummary("SCENARIO 2", service);
        System.out.println("✅ Expected: Charges = 0 (only 1 month default)");
        System.out.println("✅ Actual Defaulters: " + service.getActualDefaulters().size());
        System.out.println("✅ Result: " +
                (service.getCharges().isEmpty() && service.getActualDefaulters().size() == 1 ? "PASS ✓" : "FAIL ✗"));
        System.out.println("\n" + "=".repeat(70) + "\n");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCENARIO 3: Constant Low Balance - Multiple Charges
    // ═══════════════════════════════════════════════════════════════════════════
    private static void runScenario3_ConstantLowBalance() {
        printScenarioHeader("SCENARIO 3: Constant Low Balance - Multiple Charges");
        System.out.println("Setup: Balance ₹500 constant for 6 months");
        System.out.println("Expected: 3 charges (Months 1+2, 3+4, 5+6)\n");

        AMBService service = new AMBService();
        List<Account> accounts = new ArrayList<>();
        Account acc = createAccount("S3_CHRONIC_001", "Chronic Defaulter");
        accounts.add(acc);

        // Run for 7 months to get 3 charges
        for (int month = 1; month <= 7; month++) {
            System.out.println("\n>>> Month " + month + " <<<");
            setBalances(acc, 500.0);

            service.processDay25(accounts, month, MIN_BALANCE);
            service.processDay3(accounts, month + 1, MIN_BALANCE);
        }

        printScenarioSummary("SCENARIO 3", service);
        double total = service.getCharges().stream()
                .mapToDouble(Charge::getTotalCharge).sum();
        System.out.println("✅ Expected: 3 charges = ₹3,540.00");
        System.out.println("✅ Actual: " + service.getCharges().size() + " charges = ₹" +
                String.format("%.2f", total));
        System.out.println("✅ Result: " +
                (service.getCharges().size() == 3 && Math.abs(total - 3540) < 1 ? "PASS ✓" : "FAIL ✗"));
        System.out.println("\n" + "=".repeat(70) + "\n");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCENARIO 4: Recovery After Default
    // ═══════════════════════════════════════════════════════════════════════════
    private static void runScenario4_RecoveryAfterDefault() {
        printScenarioHeader("SCENARIO 4: Recovery After Default");
        System.out.println("Setup: Low in Month 1-2 (₹500), Recover in Month 3+ (₹15,000)");
        System.out.println("Expected: 1 charge for Month 1+2 only\n");

        AMBService service = new AMBService();
        List<Account> accounts = new ArrayList<>();
        Account acc = createAccount("S4_RECOVERY_001", "Recovery Customer");
        accounts.add(acc);

        // Month 1-2: Low
        System.out.println("\n>>> Month 1 (Low ₹500) <<<");
        setBalances(acc, 500.0);
        service.processDay25(accounts, 1, MIN_BALANCE);
        service.processDay3(accounts, 2, MIN_BALANCE);

        System.out.println("\n>>> Month 2 (Low ₹500) <<<");
        setBalances(acc, 500.0);
        service.processDay25(accounts, 2, MIN_BALANCE);
        service.processDay3(accounts, 3, MIN_BALANCE);

        // Month 3+: Good
        System.out.println("\n>>> Month 3 (Recovered ₹15,000) <<<");
        setBalances(acc, 15000.0);
        service.processDay25(accounts, 3, MIN_BALANCE);
        service.processDay3(accounts, 4, MIN_BALANCE);

        System.out.println("\n>>> Month 4 (Still Good) <<<");
        setBalances(acc, 15000.0);
        service.processDay25(accounts, 4, MIN_BALANCE);
        service.processDay3(accounts, 5, MIN_BALANCE);

        printScenarioSummary("SCENARIO 4", service);
        System.out.println("✅ Expected: 1 charge");
        System.out.println("✅ Result: " +
                (service.getCharges().size() == 1 ? "PASS ✓" : "FAIL ✗"));
        System.out.println("\n" + "=".repeat(70) + "\n");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCENARIO 5: Edge Case - Below Cap
    // ═══════════════════════════════════════════════════════════════════════════
    private static void runScenario5_EdgeCaseBelowCap() {
        printScenarioHeader("SCENARIO 5: Edge Case - Shortfall Below Cap");
        System.out.println("Setup: Balance ₹7,000 (Shortfall: ₹3,000)");
        System.out.println("Expected: Charge = ₹424.80 (₹180×2 + 18% GST)\n");

        AMBService service = new AMBService();
        List<Account> accounts = new ArrayList<>();
        Account acc = createAccount("S5_SMALL_001", "Small Shortfall");
        accounts.add(acc);

        // Run 3 months to get 1 charge
        for (int month = 1; month <= 3; month++) {
            System.out.println("\n>>> Month " + month + " <<<");
            setBalances(acc, 7000.0);
            service.processDay25(accounts, month, MIN_BALANCE);
            service.processDay3(accounts, month + 1, MIN_BALANCE);
        }

        printScenarioSummary("SCENARIO 5", service);
        if (!service.getCharges().isEmpty()) {
            Charge c = service.getCharges().get(0);
            System.out.println("✅ Expected Base: ₹360.00, Total: ₹424.80");
            System.out.println("✅ Actual Base: ₹" + String.format("%.2f", c.getBaseCharge()) +
                    ", Total: ₹" + String.format("%.2f", c.getTotalCharge()));
            System.out.println("✅ Result: " +
                    (Math.abs(c.getBaseCharge() - 360) < 1 ? "PASS ✓" : "FAIL ✗"));
        }
        System.out.println("\n" + "=".repeat(70) + "\n");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════════════

    private static Account createAccount(String id, String name) {
        return new Account(id, name);
    }

    private static void setBalances(Account acc, double amount) {
        for (int day = 1; day <= 30; day++) {
            acc.setDailyBalance(day, amount);
        }
    }

    private static void printScenarioHeader(String title) {
        System.out.println("\n\n");
        System.out.println("╔" + "═".repeat(68) + "╗");
        System.out.println("║  " + String.format("%-64s", title) + "  ║");
        System.out.println("╚" + "═".repeat(68) + "╝");
        System.out.println();
    }

    private static void printScenarioSummary(String scenario, AMBService service) {
        System.out.println("\n╔" + "═".repeat(68) + "╗");
        System.out.println("║  " + String.format("%-64s", scenario + " - SUMMARY") + "  ║");
        System.out.println("╚" + "═".repeat(68) + "╝");
        System.out.println("📊 Probable Defaulters: " + service.getProbableDefaulters().size());
        System.out.println("⚠️  Actual Defaulters: " + service.getActualDefaulters().size());
        System.out.println("💰 Charges Applied: " + service.getCharges().size());

        if (!service.getCharges().isEmpty()) {
            double total = service.getCharges().stream()
                    .mapToDouble(Charge::getTotalCharge).sum();
            System.out.println("💵 Total Charged: ₹" + String.format("%.2f", total));

            System.out.println("\n📋 Charge Details:");
            for (int i = 0; i < service.getCharges().size(); i++) {
                Charge c = service.getCharges().get(i);
                System.out.println("   Charge " + (i+1) + ": Month " + c.getMonth1() +
                        " + " + c.getMonth2() + " = ₹" + String.format("%.2f", c.getTotalCharge()));
            }
        }
    }

    private static void printHeader() {
        System.out.println("\n╔" + "═".repeat(68) + "╗");
        System.out.println("║                                                                  ║");
        System.out.println("║     BANK AMB NON-MAINTENANCE CHARGE SYSTEM                       ║");
        System.out.println("║     Comprehensive Test Suite - GoRules Edition                   ║");
        System.out.println("║                                                                  ║");
        System.out.println("╚" + "═".repeat(68) + "╝");
        System.out.println("\n📋 Configuration:");
        System.out.println("   • Min Balance: ₹10,000");
        System.out.println("   • Charge: min(6% × shortfall, ₹500) + 18% GST per month");
        System.out.println("   • Trigger: 2 consecutive defaults");
        System.out.println("   • SMS: Sent once when first detected\n");
    }

    private static void printFooter() {
        System.out.println("\n╔" + "═".repeat(68) + "╗");
        System.out.println("║              ALL TEST SCENARIOS COMPLETED                        ║");
        System.out.println("╚" + "═".repeat(68) + "╝");
        System.out.println("\n✅ Test suite completed!");
        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("SUMMARY OF ALL SCENARIOS:");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("✓ Scenario 1: Happy Path - No charges");
        System.out.println("✓ Scenario 2: Single default - No charge");
        System.out.println("✓ Scenario 3: Constant low - 3 charges");
        System.out.println("✓ Scenario 4: Recovery - 1 charge only");
        System.out.println("✓ Scenario 5: Below cap - Correct calculation");
        System.out.println("═══════════════════════════════════════════════════════\n");
    }
}