package com.bank.amb;

import com.bank.amb.model.*;
import com.bank.amb.service.AMBRuleEngineProgrammatic;
import com.bank.amb.service.AMBRuleEngineProgrammatic.RuleExecutionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * DIAGNOSTIC TEST - Figure out why rules aren't firing
 */
public class AMBDiagnosticTest {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║           DIAGNOSTIC TEST - AMB RULE ENGINE           ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        // Initialize Rule Engine
        AMBRuleEngineProgrammatic ruleEngine = new AMBRuleEngineProgrammatic();

        // Create a simple test account with low balance
        Account testAccount = new Account();
        testAccount.setAccountId("TEST001");
        testAccount.setAccountName("Test User");

        // Set constant low balance of ₹500 (below ₹10,000 minimum)
        double[] balances = new double[30];
        for (int i = 0; i < 30; i++) {
            balances[i] = 500.0;
        }
        testAccount.setDailyBalances(balances);

        List<Account> accounts = new ArrayList<>();
        accounts.add(testAccount);

        System.out.println("📊 Test Data:");
        System.out.println("   Account ID: " + testAccount.getAccountId());
        System.out.println("   Account Name: " + testAccount.getAccountName());
        System.out.println("   Daily Balance: ₹500 (constant for all 30 days)");
        System.out.println("   Minimum Required: ₹10,000");
        System.out.println();

        // Calculate and print AMB
        double amb1_25 = testAccount.calculateAMB(1, 25);
        double amb1_30 = testAccount.calculateAMB(1, 30);

        System.out.println("📈 Calculated AMB:");
        System.out.println("   AMB (Day 1-25): ₹" + String.format("%.2f", amb1_25));
        System.out.println("   AMB (Day 1-30): ₹" + String.format("%.2f", amb1_30));
        System.out.println("   Below minimum? " + (amb1_25 < 10000 ? "YES ✓" : "NO ✗"));
        System.out.println();

        // TEST 1: Check on Day 25 of Month 1
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("TEST 1: Day 25 of Month 1 (Probable Defaulter Check)");
        System.out.println("═══════════════════════════════════════════════════════");

        ExecutionContext ctx1 = new ExecutionContext();
        ctx1.setCurrentMonth(1);
        ctx1.setCheckDay(25);
        ctx1.setMinBalance(10000.0);

        System.out.println("Context:");
        System.out.println("  Current Month: " + ctx1.getCurrentMonth());
        System.out.println("  Check Day: " + ctx1.getCheckDay());
        System.out.println("  Min Balance: ₹" + ctx1.getMinBalance());
        System.out.println();

        List<ProbableDefaulter> probableList = new ArrayList<>();
        List<ActualDefaulter> actualList = new ArrayList<>();
        List<Charge> chargeList = new ArrayList<>();

        RuleExecutionResult result1 = ruleEngine.executeRules(
                accounts,
                probableList,
                actualList,
                chargeList,
                ctx1
        );

        System.out.println("Expected: 1 Probable Defaulter with SMS");
        System.out.println("Actual: " + result1.getProbableDefaulters().size() + " Probable Defaulters");
        System.out.println("Result: " + (result1.getProbableDefaulters().size() == 1 ? "PASS ✓" : "FAIL ✗"));
        System.out.println();

        if (result1.getProbableDefaulters().isEmpty()) {
            System.err.println("❌ PROBLEM DETECTED: No probable defaulters created!");
            System.err.println("   This means Rule 1B is NOT firing.");
            System.err.println();
            debugRuleConditions(testAccount, ctx1);
        } else {
            probableList.addAll(result1.getProbableDefaulters());
            ProbableDefaulter pd = result1.getProbableDefaulters().get(0);
            System.out.println("✓ Probable Defaulter Created:");
            System.out.println("  Account: " + pd.getAccountId());
            System.out.println("  Month: " + pd.getMonth());
            System.out.println("  AMB: ₹" + String.format("%.2f", pd.getAmb()));
            System.out.println("  SMS Sent: " + pd.isSmsSent());
        }

        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("TEST 2: Day 3 of Month 2 (Actual Defaulter Check)");
        System.out.println("═══════════════════════════════════════════════════════");

        ExecutionContext ctx2 = new ExecutionContext();
        ctx2.setCurrentMonth(2);
        ctx2.setCheckDay(3);
        ctx2.setMinBalance(10000.0);

        System.out.println("Context:");
        System.out.println("  Current Month: " + ctx2.getCurrentMonth());
        System.out.println("  Check Day: " + ctx2.getCheckDay());
        System.out.println("  Existing Probable Defaulters: " + probableList.size());
        System.out.println();

        RuleExecutionResult result2 = ruleEngine.executeRules(
                accounts,
                probableList,
                actualList,
                chargeList,
                ctx2
        );

        System.out.println("Expected: 1 Actual Defaulter");
        System.out.println("Actual: " + result2.getActualDefaulters().size() + " Actual Defaulters");
        System.out.println("Result: " + (result2.getActualDefaulters().size() == 1 ? "PASS ✓" : "FAIL ✗"));
        System.out.println();

        if (result2.getActualDefaulters().isEmpty()) {
            System.err.println("❌ PROBLEM DETECTED: No actual defaulters confirmed!");
            System.err.println("   This means Rule 2 is NOT firing.");
            System.err.println("   Prerequisites:");
            System.err.println("   - checkDay == 3? " + (ctx2.getCheckDay() == 3 ? "YES" : "NO"));
            System.err.println("   - AMB < 10000? " + (amb1_30 < 10000 ? "YES" : "NO"));
            System.err.println("   - Probable defaulter exists? " + (!probableList.isEmpty() ? "YES" : "NO"));
            if (!probableList.isEmpty()) {
                System.err.println("   - Probable defaulter month: " + probableList.get(0).getMonth());
                System.err.println("   - Looking for month: " + (ctx2.getCurrentMonth() - 1));
            }
        }

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                  DIAGNOSTIC SUMMARY                    ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("Test 1 (Probable Defaulter): " +
                (result1.getProbableDefaulters().size() == 1 ? "PASS ✓" : "FAIL ✗"));
        System.out.println("Test 2 (Actual Defaulter): " +
                (result2.getActualDefaulters().size() == 1 ? "PASS ✓" : "FAIL ✗"));

        if (result1.getRulesFired() == 0 && result2.getRulesFired() == 0) {
            System.out.println("\n❌ CRITICAL: NO RULES FIRED AT ALL!");
            System.out.println("   Possible causes:");
            System.out.println("   1. DRL rules not loaded correctly");
            System.out.println("   2. Rule conditions not matching");
            System.out.println("   3. Drools session not initialized properly");
        }
        System.out.println();
    }

    private static void debugRuleConditions(Account account, ExecutionContext context) {
        System.out.println("🔍 DEBUGGING RULE 1B CONDITIONS:");
        System.out.println("   Rule expects:");
        System.out.println("   1. checkDay == 25");
        System.out.println("      Actual: " + context.getCheckDay() + " " +
                (context.getCheckDay() == 25 ? "✓" : "✗"));

        double amb = account.calculateAMB(1, 25);
        System.out.println("   2. calculateAMB(1, 25) < minBalance");
        System.out.println("      AMB: ₹" + String.format("%.2f", amb));
        System.out.println("      MinBalance: ₹" + String.format("%.2f", context.getMinBalance()));
        System.out.println("      AMB < MinBalance? " + (amb < context.getMinBalance() ? "✓" : "✗"));

        System.out.println("   3. Not ActualDefaulter in previous month");
        System.out.println("      (Should be TRUE for first month) ✓");

        System.out.println("   4. Not ProbableDefaulter already this month");
        System.out.println("      (Should be TRUE for first check) ✓");

        System.out.println();
        System.out.println("   Account currentMonth set? " + account.getCurrentMonth());
        System.out.println("   (Must match context.currentMonth = " + context.getCurrentMonth() + ")");
        System.out.println();
    }
}