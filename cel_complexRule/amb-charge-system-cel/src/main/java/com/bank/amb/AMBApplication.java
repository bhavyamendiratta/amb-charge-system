package com.bank.amb;

import com.bank.amb.model.*;
import com.bank.amb.service.AMBRuleEngine;
import dev.cel.common.CelValidationException;

import java.util.ArrayList;
import java.util.List;

public class AMBApplication {

    public static void main(String[] args) {
        try {
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("   AMB Charge System - Pure CEL Implementation");
            System.out.println("═══════════════════════════════════════════════════════\n");

            // Initialize CEL Rule Engine
            AMBRuleEngine engine = new AMBRuleEngine();
            System.out.println("✓ CEL Rule Engine initialized\n");

            // Create test accounts
            List<Account> accounts = createTestAccounts();

            // Track all results
            List<ProbableDefaulter> allPD = new ArrayList<>();
            List<ActualDefaulter> allAD = new ArrayList<>();
            List<Charge> allCharges = new ArrayList<>();

            // MONTH 3, DAY 25 - Probable Check
            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║   MONTH 3, DAY 25 - Probable Check   ║");
            System.out.println("╚══════════════════════════════════════╝");

            ExecutionContext ctx1 = new ExecutionContext();
            ctx1.setCurrentMonth(3);
            ctx1.setCheckDay(25);

            AMBRuleEngine.RuleResult result1 = engine.execute(accounts, ctx1, allPD, allAD, allCharges);
            allPD.addAll(result1.probableDefaulters);
            System.out.println("✓ Found " + result1.probableDefaulters.size() + " probable defaulters\n");

            // MONTH 4, DAY 3 - Actual Check
            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║   MONTH 4, DAY 3 - Actual Check      ║");
            System.out.println("╚══════════════════════════════════════╝");

            ExecutionContext ctx2 = new ExecutionContext();
            ctx2.setCurrentMonth(4);
            ctx2.setCheckDay(3);

            AMBRuleEngine.RuleResult result2 = engine.execute(accounts, ctx2, allPD, allAD, allCharges);
            allAD.addAll(result2.actualDefaulters);
            System.out.println("✓ Found " + result2.actualDefaulters.size() + " actual defaulters\n");

            // MONTH 4, DAY 25 - Probable Check
            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║   MONTH 4, DAY 25 - Probable Check   ║");
            System.out.println("╚══════════════════════════════════════╝");

            ExecutionContext ctx3 = new ExecutionContext();
            ctx3.setCurrentMonth(4);
            ctx3.setCheckDay(25);

            AMBRuleEngine.RuleResult result3 = engine.execute(accounts, ctx3, allPD, allAD, allCharges);
            allPD.addAll(result3.probableDefaulters);
            System.out.println("✓ Found " + result3.probableDefaulters.size() + " probable defaulters\n");

            // MONTH 5, DAY 3 - Charge Calculation
            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║   MONTH 5, DAY 3 - Charge Calc       ║");
            System.out.println("╚══════════════════════════════════════╝");

            ExecutionContext ctx4 = new ExecutionContext();
            ctx4.setCurrentMonth(5);
            ctx4.setCheckDay(3);

            AMBRuleEngine.RuleResult result4 = engine.execute(accounts, ctx4, allPD, allAD, allCharges);
            allAD.addAll(result4.actualDefaulters);
            allCharges.addAll(result4.charges);
            System.out.println("✓ Applied " + result4.charges.size() + " charges\n");

            // Summary
            printSummary(allPD, allAD, allCharges);

        } catch (CelValidationException e) {
            System.err.println("CEL validation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Account> createTestAccounts() {
        List<Account> accounts = new ArrayList<>();

        // Account 1: Low balance - will default
        Account acc1 = new Account();
        acc1.setAccountId("ACC001");
        acc1.setAccountName("John Doe");
        acc1.setDailyBalances(new double[]{
                100,120,140,20,40,60,600,70,1000,700,
                100,120,140,20,40,60,600,70,1000,700,
                140,20,40,60,600,10,20,40,500,60
        });
        accounts.add(acc1);

        // Account 2: Medium balance - will default
        Account acc2 = new Account();
        acc2.setAccountId("ACC002");
        acc2.setAccountName("Jane Smith");
        acc2.setDailyBalances(new double[]{
                5000,5200,5100,5300,5400,5200,5100,5000,4900,5100,
                5200,5300,5400,5200,5100,5000,4900,5100,5200,5300,
                5400,5200,5100,5000,4900,5100,5200,5300,5400,5200
        });
        accounts.add(acc2);

        // Account 3: High balance - won't default
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

    private static void printSummary(List<ProbableDefaulter> pd, List<ActualDefaulter> ad, List<Charge> charges) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              FINAL SUMMARY                           ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        System.out.println("📊 Probable Defaulters: " + pd.size());
        pd.forEach(p -> System.out.println("   • " + p.getAccountId() + " | Month " + p.getMonth()
                + " | SMS: " + (p.isSmsSent() ? "✓" : "✗")));

        System.out.println("\n⚠️  Actual Defaulters: " + ad.size());
        ad.forEach(a -> System.out.println("   • " + a.getAccountId() + " | Month " + a.getMonth()
                + " | Shortfall: ₹" + String.format("%.2f", a.getShortfall())));

        System.out.println("\n💰 Charges: " + charges.size());
        double total = 0;
        for (Charge c : charges) {
            System.out.println("   • " + c.getAccountId() + " | Months " + c.getMonth1() + "+" + c.getMonth2()
                    + " | ₹" + String.format("%.2f", c.getTotalCharge()));
            total += c.getTotalCharge();
        }
        if (!charges.isEmpty()) {
            System.out.println("\n💵 Total Charges: ₹" + String.format("%.2f", total));
        }

        System.out.println("\n═══════════════════════════════════════════════════════\n");
    }
}