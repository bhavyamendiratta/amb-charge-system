package com.bank.amb.service;

import com.bank.amb.engine.AMBRulesEngine;
import com.bank.amb.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for AMB processing
 * Manages the business flow and coordinates rule execution
 */
public class AMBService {

    private final AMBRulesEngine rulesEngine;

    // In-memory storage (in production, use database)
    private List<ProbableDefaulter> probableDefaultersDB;
    private List<ActualDefaulter> actualDefaultersDB;
    private List<Charge> chargesDB;

    public AMBService() {
        this.rulesEngine = new AMBRulesEngine();
        this.probableDefaultersDB = new ArrayList<>();
        this.actualDefaultersDB = new ArrayList<>();
        this.chargesDB = new ArrayList<>();
    }

    /**
     * Process accounts on Day 25 - Check for probable defaulters
     */
    public void processDay25(List<Account> accounts, int currentMonth, double minBalance) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║          PROCESSING DAY 25 - PROBABLE DEFAULTERS          ║");
        System.out.println("║              Month: " + currentMonth + " | Min Balance: ₹" + String.format("%.2f", minBalance) + "        ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        ExecutionContext context = new ExecutionContext(25, currentMonth, minBalance);

        AMBRulesEngine.RuleExecutionResult result = rulesEngine.executeRules(
                accounts,
                probableDefaultersDB,
                actualDefaultersDB,
                chargesDB,
                context
        );

        // Store results
        probableDefaultersDB.addAll(result.getProbableDefaulters());
    }

    /**
     * Process accounts on Day 3 - Check for actual defaulters and apply charges
     */
    public void processDay3(List<Account> accounts, int currentMonth, double minBalance) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║         PROCESSING DAY 3 - ACTUAL DEFAULTERS & CHARGES    ║");
        System.out.println("║              Month: " + currentMonth + " | Min Balance: ₹" + String.format("%.2f", minBalance) + "        ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        ExecutionContext context = new ExecutionContext(3, currentMonth, minBalance);

        AMBRulesEngine.RuleExecutionResult result = rulesEngine.executeRules(
                accounts,
                probableDefaultersDB,
                actualDefaultersDB,
                chargesDB,
                context
        );

        // Store results
        actualDefaultersDB.addAll(result.getActualDefaulters());
        chargesDB.addAll(result.getCharges());
    }

    /**
     * Get all probable defaulters
     */
    public List<ProbableDefaulter> getProbableDefaulters() {
        return new ArrayList<>(probableDefaultersDB);
    }

    /**
     * Get all actual defaulters
     */
    public List<ActualDefaulter> getActualDefaulters() {
        return new ArrayList<>(actualDefaultersDB);
    }

    /**
     * Get all charges
     */
    public List<Charge> getCharges() {
        return new ArrayList<>(chargesDB);
    }

    /**
     * Print complete report
     */
    public void printReport() {
        System.out.println("\n\n");
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                    COMPLETE REPORT                        ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");

        // Probable Defaulters
        System.out.println("\n📋 PROBABLE DEFAULTERS (" + probableDefaultersDB.size() + "):");
        System.out.println("─".repeat(70));
        if (probableDefaultersDB.isEmpty()) {
            System.out.println("  No probable defaulters found.");
        } else {
            for (ProbableDefaulter pd : probableDefaultersDB) {
                System.out.printf("  • Account: %-15s | Month: %2d | AMB: ₹%-10.2f | SMS: %s%n",
                        pd.getAccountId(), pd.getMonth(), pd.getAmb(),
                        pd.isSmsSent() ? "✓" : "✗");
            }
        }

        // Actual Defaulters
        System.out.println("\n⚠️  ACTUAL DEFAULTERS (" + actualDefaultersDB.size() + "):");
        System.out.println("─".repeat(70));
        if (actualDefaultersDB.isEmpty()) {
            System.out.println("  No actual defaulters found.");
        } else {
            for (ActualDefaulter ad : actualDefaultersDB) {
                System.out.printf("  • Account: %-15s | Month: %2d | AMB: ₹%-10.2f | Shortfall: ₹%-10.2f%n",
                        ad.getAccountId(), ad.getMonth(), ad.getAmb(), ad.getShortfall());
            }
        }

        // Charges
        System.out.println("\n💰 CHARGES APPLIED (" + chargesDB.size() + "):");
        System.out.println("─".repeat(70));
        if (chargesDB.isEmpty()) {
            System.out.println("  No charges applied.");
        } else {
            double totalCharges = 0.0;
            for (Charge charge : chargesDB) {
                System.out.printf("  • Account: %-15s | Months: %d+%d | Charge: ₹%-10.2f%n",
                        charge.getAccountId(), charge.getMonth1(), charge.getMonth2(),
                        charge.getTotalCharge());
                totalCharges += charge.getTotalCharge();
            }
            System.out.println("─".repeat(70));
            System.out.printf("  TOTAL CHARGES COLLECTED: ₹%.2f%n", totalCharges);
        }

        System.out.println("\n" + "═".repeat(70) + "\n");
    }

    /**
     * Clear all data (for testing)
     */
    public void clearAllData() {
        probableDefaultersDB.clear();
        actualDefaultersDB.clear();
        chargesDB.clear();
    }
}