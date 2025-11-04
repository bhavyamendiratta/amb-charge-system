package com.bank.amb;

import com.bank.amb.model.*;
import com.bank.amb.service.AMBRuleEngineProgrammatic;
import com.bank.amb.service.AMBRuleEngineProgrammatic.RuleExecutionResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AMBApplication {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("Bank AMB Non-Maintenance Charge System");
        System.out.println("Using Drools Rule Engine");
        System.out.println("==============================================\n");

        // Initialize Rule Engine
        AMBRuleEngineProgrammatic ruleEngine = new AMBRuleEngineProgrammatic();

        // Create test accounts
        List<Account> accounts = createTestAccounts();

        // Storage for results
        List<ProbableDefaulter> allProbableDefaulters = new ArrayList<>();
        List<ActualDefaulter> allActualDefaulters = new ArrayList<>();
        List<Charge> allCharges = new ArrayList<>();

        // ========================================
        // MONTH 3, DAY 26 - Probable Defaulter Check
        // ========================================
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  MONTH 3, DAY 26 - PROBABLE CHECK     ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        ExecutionContext context1 = new ExecutionContext();
        context1.setCurrentMonth(3);
        context1.setCheckDay(26);

        RuleExecutionResult result1 = ruleEngine.executeRules(
                accounts,
                allProbableDefaulters,
                allActualDefaulters,
                allCharges,
                context1
        );

        allProbableDefaulters.addAll(result1.getProbableDefaulters());

        // ========================================
        // MONTH 4, DAY 3 - Actual Defaulter Check
        // ========================================
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  MONTH 4, DAY 3 - ACTUAL CHECK        ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        ExecutionContext context2 = new ExecutionContext();
        context2.setCurrentMonth(4);
        context2.setCheckDay(3);

        RuleExecutionResult result2 = ruleEngine.executeRules(
                accounts,
                allProbableDefaulters,
                allActualDefaulters,
                allCharges,
                context2
        );

        allActualDefaulters.addAll(result2.getActualDefaulters());

        // ========================================
        // MONTH 4, DAY 26 - Probable Defaulter Check
        // ========================================
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  MONTH 4, DAY 26 - PROBABLE CHECK     ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        ExecutionContext context3 = new ExecutionContext();
        context3.setCurrentMonth(4);
        context3.setCheckDay(26);

        RuleExecutionResult result3 = ruleEngine.executeRules(
                accounts,
                allProbableDefaulters,
                allActualDefaulters,
                allCharges,
                context3
        );

        allProbableDefaulters.addAll(result3.getProbableDefaulters());

        // ========================================
        // MONTH 5, DAY 3 - Actual Check & Charging
        // ========================================
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  MONTH 5, DAY 3 - CHARGE CALCULATION  ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        ExecutionContext context4 = new ExecutionContext();
        context4.setCurrentMonth(5);
        context4.setCheckDay(3);

        RuleExecutionResult result4 = ruleEngine.executeRules(
                accounts,
                allProbableDefaulters,
                allActualDefaulters,
                allCharges,
                context4
        );

        allActualDefaulters.addAll(result4.getActualDefaulters());
        allCharges.addAll(result4.getCharges());

        // ========================================
        // Final Summary
        // ========================================
        printFinalSummary(allProbableDefaulters, allActualDefaulters, allCharges);
    }

    private static List<Account> createTestAccounts() {
        List<Account> accounts = new ArrayList<>();

        // Account 1: Low balance (will default)
        Account acc1 = new Account();
        acc1.setAccountId("ACC001");
        acc1.setAccountName("John Doe");
        acc1.setDailyBalances(new double[]{
                100,120,140,20,40,60,600,70,1000,700,
                100,120,140,20,40,60,600,70,1000,700,
                140,20,40,60,600,10,20,40,500,60
        });
        accounts.add(acc1);

        // Account 2: Medium balance (will default)
        Account acc2 = new Account();
        acc2.setAccountId("ACC002");
        acc2.setAccountName("Jane Smith");
        acc2.setDailyBalances(new double[]{
                5000,5200,5100,5300,5400,5200,5100,5000,4900,5100,
                5200,5300,5400,5200,5100,5000,4900,5100,5200,5300,
                5400,5200,5100,5000,4900,5100,5200,5300,5400,5200
        });
        accounts.add(acc2);

        // Account 3: High balance (will not default)
        Account acc3 = new Account();
        acc3.setAccountId("ACC003");
        acc3.setAccountName("Bob Johnson");
        acc3.setDailyBalances(new double[]{
                12000,11000,13000,14000,12500,13000,14000,12000,11000,13000,
                14000,12500,13000,14000,12000,11000,13000,14000,12500,13000,
                14000,12000,11000,13000,14000,12500,13000,14000,12000,11000
        });
        accounts.add(acc3);

        return accounts;
    }

    private static void printFinalSummary(
            List<ProbableDefaulter> probableDefaulters,
            List<ActualDefaulter> actualDefaulters,
            List<Charge> charges) {

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║              FINAL EXECUTION SUMMARY                   ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        System.out.println("📊 Total Probable Defaulters: " + probableDefaulters.size());
        for (ProbableDefaulter pd : probableDefaulters) {
            System.out.println("   • " + pd.getAccountId() +
                    " | Month: " + pd.getMonth() +
                    " | AMB: ₹" + String.format("%.2f", pd.getAmb()) +
                    " | SMS: " + (pd.isSmsSent() ? "✓" : "✗"));
        }

        System.out.println("\n⚠️  Total Actual Defaulters: " + actualDefaulters.size());
        for (ActualDefaulter ad : actualDefaulters) {
            System.out.println("   • " + ad.getAccountId() +
                    " | Month: " + ad.getMonth() +
                    " | AMB: ₹" + String.format("%.2f", ad.getAmb()) +
                    " | Shortfall: ₹" + String.format("%.2f", ad.getShortfall()));
        }

        System.out.println("\n💰 Total Charges Applied: " + charges.size());
        for (Charge charge : charges) {
            System.out.println("   • " + charge.getAccountId() +
                    " | Months: " + charge.getMonth1() + " & " + charge.getMonth2() +
                    " | Total Shortfall: ₹" + String.format("%.2f", charge.getTotalShortfall()) +
                    " | Charge: ₹" + String.format("%.2f", charge.getTotalCharge()));
        }

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║              EXECUTION COMPLETED                       ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
    }
}