package com.bank.amb;

import com.bank.amb.model.Account;
import com.bank.amb.service.AMBService;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Application - AMB Rules Engine Demo
 *
 * Simulates a multi-month banking scenario with:
 * - Probable defaulter detection (Day 25)
 * - Actual defaulter confirmation (Day 3)
 * - Charge calculation (consecutive defaults)
 */
public class AMBApplication {

    private static final double MIN_BALANCE = 10000.0;

    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                           ║");
        System.out.println("║     AMB RULES ENGINE - GORULES DEMONSTRATION              ║");
        System.out.println("║     Average Monthly Balance Processing System             ║");
        System.out.println("║                                                           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("\n");

        AMBService ambService = new AMBService();

        // Create test accounts
        List<Account> accounts = createTestAccounts();

        System.out.println("📊 Initialized " + accounts.size() + " test accounts");
        System.out.println("💰 Minimum Required Balance: ₹" + String.format("%.2f", MIN_BALANCE));
        System.out.println("\n");

        // ═══════════════════════════════════════════════════════════════
        // MONTH 1 - Day 25: Check for probable defaulters
        // ═══════════════════════════════════════════════════════════════
        System.out.println("\n" + "═".repeat(70));
        System.out.println("MONTH 1 - DAY 25");
        System.out.println("═".repeat(70));

        // Set Month 1 balances
        setMonth1Balances(accounts);
        ambService.processDay25(accounts, 1, MIN_BALANCE);

        // ═══════════════════════════════════════════════════════════════
        // MONTH 2 - Day 3: Check actual defaulters from Month 1
        // ═══════════════════════════════════════════════════════════════
        System.out.println("\n" + "═".repeat(70));
        System.out.println("MONTH 2 - DAY 3");
        System.out.println("═".repeat(70));

        ambService.processDay3(accounts, 2, MIN_BALANCE);

        // ═══════════════════════════════════════════════════════════════
        // MONTH 2 - Day 25: Check for probable defaulters
        // ═══════════════════════════════════════════════════════════════
        System.out.println("\n" + "═".repeat(70));
        System.out.println("MONTH 2 - DAY 25");
        System.out.println("═".repeat(70));

        // Set Month 2 balances
        setMonth2Balances(accounts);
        ambService.processDay25(accounts, 2, MIN_BALANCE);

        // ═══════════════════════════════════════════════════════════════
        // MONTH 3 - Day 3: Check actual defaulters and apply charges
        // ═══════════════════════════════════════════════════════════════
        System.out.println("\n" + "═".repeat(70));
        System.out.println("MONTH 3 - DAY 3");
        System.out.println("═".repeat(70));

        ambService.processDay3(accounts, 3, MIN_BALANCE);

        // ═══════════════════════════════════════════════════════════════
        // FINAL REPORT
        // ═══════════════════════════════════════════════════════════════
        ambService.printReport();

        System.out.println("✅ AMB Rules Engine demo completed successfully!\n");
    }

    /**
     * Create test accounts with different scenarios
     */
    private static List<Account> createTestAccounts() {
        List<Account> accounts = new ArrayList<>();

        // Account 1: Good customer (always maintains balance)
        accounts.add(new Account("ACC001", "John Smith"));

        // Account 2: One-time defaulter (defaults in Month 1 only)
        accounts.add(new Account("ACC002", "Alice Johnson"));

        // Account 3: Consecutive defaulter (defaults in Month 1 & 2, gets charged)
        accounts.add(new Account("ACC003", "Bob Williams"));

        // Account 4: Recovering customer (defaults M1, recovers M2)
        accounts.add(new Account("ACC004", "Carol Davis"));

        return accounts;
    }

    /**
     * Set Month 1 daily balances
     */
    private static void setMonth1Balances(List<Account> accounts) {
        // ACC001: Good balance throughout (₹12,000)
        Account acc1 = accounts.get(0);
        for (int day = 1; day <= 30; day++) {
            acc1.setDailyBalance(day, 12000.0);
        }

        // ACC002: Low balance in Month 1 (₹8,000)
        Account acc2 = accounts.get(1);
        for (int day = 1; day <= 30; day++) {
            acc2.setDailyBalance(day, 8000.0);
        }

        // ACC003: Low balance in Month 1 (₹7,500)
        Account acc3 = accounts.get(2);
        for (int day = 1; day <= 30; day++) {
            acc3.setDailyBalance(day, 7500.0);
        }

        // ACC004: Low balance in Month 1 (₹9,000)
        Account acc4 = accounts.get(3);
        for (int day = 1; day <= 30; day++) {
            acc4.setDailyBalance(day, 9000.0);
        }
    }

    /**
     * Set Month 2 daily balances
     */
    private static void setMonth2Balances(List<Account> accounts) {
        // ACC001: Still good (₹12,000)
        Account acc1 = accounts.get(0);
        for (int day = 1; day <= 30; day++) {
            acc1.setDailyBalance(day, 12000.0);
        }

        // ACC002: Recovered to good balance (₹11,000)
        Account acc2 = accounts.get(1);
        for (int day = 1; day <= 30; day++) {
            acc2.setDailyBalance(day, 11000.0);
        }

        // ACC003: Still low - will trigger consecutive default charge (₹7,000)
        Account acc3 = accounts.get(2);
        for (int day = 1; day <= 30; day++) {
            acc3.setDailyBalance(day, 7000.0);
        }

        // ACC004: Recovered (₹10,500)
        Account acc4 = accounts.get(3);
        for (int day = 1; day <= 30; day++) {
            acc4.setDailyBalance(day, 10500.0);
        }
    }
}