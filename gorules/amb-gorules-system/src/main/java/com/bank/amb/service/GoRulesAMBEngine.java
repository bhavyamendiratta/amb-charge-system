package com.bank.amb.service;

import com.bank.amb.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GoRules-based AMB Rule Engine
 * This simulates GoRules decision table execution in pure Java
 */
public class GoRulesAMBEngine {

    private ObjectMapper mapper;

    public GoRulesAMBEngine() {
        this.mapper = new ObjectMapper();
        System.out.println("✓ GoRules Engine initialized successfully!");
        System.out.println();
    }

    public RuleExecutionResult executeRules(
            Account account,
            List<ProbableDefaulter> existingProbableDefaulters,
            List<ActualDefaulter> existingActualDefaulters,
            List<Charge> existingCharges,
            ExecutionContext context) {

        List<ProbableDefaulter> probableDefaulters = new ArrayList<>();
        List<ActualDefaulter> actualDefaulters = new ArrayList<>();
        List<Charge> charges = new ArrayList<>();

        account.setCurrentMonth(context.getCurrentMonth());

        // Execute rules based on check day
        if (context.getCheckDay() == 25) {
            executeProbableDefaulterRules(account, context, existingActualDefaulters,
                    existingProbableDefaulters, probableDefaulters);
        } else if (context.getCheckDay() == 3) {
            executeActualDefaulterRules(account, context, existingProbableDefaulters,
                    existingActualDefaulters, actualDefaulters);
            executeChargeRules(account, context, existingActualDefaulters, actualDefaulters,
                    existingCharges, charges);
        }

        int rulesFired = probableDefaulters.size() + actualDefaulters.size() + charges.size();

        System.out.println("\n========================================");
        System.out.println("Rules Execution Summary");
        System.out.println("========================================");
        System.out.println("Rules Fired: " + rulesFired);
        System.out.println("Probable Defaulters: " + probableDefaulters.size());
        System.out.println("Actual Defaulters: " + actualDefaulters.size());
        System.out.println("Charges Applied: " + charges.size());
        System.out.println("========================================\n");

        return new RuleExecutionResult(probableDefaulters, actualDefaulters, charges, rulesFired);
    }

    // RULE 1: Probable Defaulter Detection
    private void executeProbableDefaulterRules(Account account, ExecutionContext context,
                                               List<ActualDefaulter> existingActualDefaulters,
                                               List<ProbableDefaulter> existingProbableDefaulters,
                                               List<ProbableDefaulter> newProbableDefaulters) {

        double amb = account.calculateAMB(1, 25);

        if (amb >= context.getMinBalance()) {
            System.out.println("[RULE 1C] ✓ Balance Maintained: " + account.getAccountId() +
                    " (" + account.getAccountName() + ")");
            return;
        }

        // Check if already probable defaulter this month
        boolean alreadyProbable = existingProbableDefaulters.stream()
                .anyMatch(pd -> pd.getAccountId().equals(account.getAccountId()) &&
                        pd.getMonth() == context.getCurrentMonth());

        if (alreadyProbable) {
            return;
        }

        // Check if was actual defaulter last month
        boolean wasActualLastMonth = existingActualDefaulters.stream()
                .anyMatch(ad -> ad.getAccountId().equals(account.getAccountId()) &&
                        ad.getMonth() == (context.getCurrentMonth() - 1));

        ProbableDefaulter pd = new ProbableDefaulter();
        pd.setAccountId(account.getAccountId());
        pd.setMonth(context.getCurrentMonth());
        pd.setAmb(amb);

        if (wasActualLastMonth) {
            // RULE 1A: Was actual defaulter - NO SMS
            pd.setSmsSent(false);
            pd.setReason("Was actual defaulter in Month " + (context.getCurrentMonth() - 1) + " - NO SMS");

            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("[RULE 1A] PROBABLE DEFAULTER IDENTIFIED");
            System.out.println("  Account ID: " + account.getAccountId());
            System.out.println("  Account Name: " + account.getAccountName());
            System.out.println("  Month: " + context.getCurrentMonth());
            System.out.println("  AMB (Day 1-25): ₹" + String.format("%.2f", amb));
            System.out.println("  SMS Status: NOT SENT (was actual defaulter in Month " +
                    (context.getCurrentMonth() - 1) + ")");
            System.out.println("  Reason: Continuing defaulter - SMS already sent earlier");
            System.out.println("═══════════════════════════════════════════════════════════");
        } else {
            // RULE 1B: New probable defaulter - SEND SMS
            pd.setSmsSent(true);
            pd.setReason("New probable defaulter - SMS sent");

            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("[RULE 1B] NEW PROBABLE DEFAULTER IDENTIFIED");
            System.out.println("  Account ID: " + account.getAccountId());
            System.out.println("  Account Name: " + account.getAccountName());
            System.out.println("  Month: " + context.getCurrentMonth());
            System.out.println("  AMB (Day 1-25): ₹" + String.format("%.2f", amb));
            System.out.println("  Required Balance: ₹" + String.format("%.2f", context.getMinBalance()));
            System.out.println("  Deficit: ₹" + String.format("%.2f", (context.getMinBalance() - amb)));
            System.out.println("  ✉️  SMS: SENT");
            System.out.println("  Message: Your average monthly balance is below minimum");
            System.out.println("           required. Please maintain sufficient balance or");
            System.out.println("           charges will be deducted.");
            System.out.println("═══════════════════════════════════════════════════════════");
        }

        newProbableDefaulters.add(pd);
    }

    // RULE 2: Actual Defaulter Confirmation
    private void executeActualDefaulterRules(Account account, ExecutionContext context,
                                             List<ProbableDefaulter> existingProbableDefaulters,
                                             List<ActualDefaulter> existingActualDefaulters,
                                             List<ActualDefaulter> newActualDefaulters) {

        double amb = account.calculateAMB(1, 30);

        if (amb >= context.getMinBalance()) {
            return;
        }

        // Check if was probable defaulter last month
        boolean wasProbableLastMonth = existingProbableDefaulters.stream()
                .anyMatch(pd -> pd.getAccountId().equals(account.getAccountId()) &&
                        pd.getMonth() == (context.getCurrentMonth() - 1));

        if (!wasProbableLastMonth) {
            return;
        }

        // Check if already actual defaulter for that month
        boolean alreadyActual = existingActualDefaulters.stream()
                .anyMatch(ad -> ad.getAccountId().equals(account.getAccountId()) &&
                        ad.getMonth() == (context.getCurrentMonth() - 1));

        if (alreadyActual) {
            return;
        }

        ActualDefaulter ad = new ActualDefaulter();
        ad.setAccountId(account.getAccountId());
        ad.setMonth(context.getCurrentMonth() - 1);
        ad.setAmb(amb);
        ad.setShortfall(context.getMinBalance() - amb);
        ad.setStatus("Confirmed actual defaulter for Month " + (context.getCurrentMonth() - 1));

        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║ [RULE 2] ACTUAL DEFAULTER CONFIRMED                      ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("  Account ID: " + account.getAccountId());
        System.out.println("  Account Name: " + account.getAccountName());
        System.out.println("  Defaulted Month: " + ad.getMonth());
        System.out.println("  Full Month AMB: ₹" + String.format("%.2f", amb));
        System.out.println("  Required Balance: ₹" + String.format("%.2f", context.getMinBalance()));
        System.out.println("  Shortfall: ₹" + String.format("%.2f", ad.getShortfall()));
        System.out.println("  Status: " + ad.getStatus());
        System.out.println("╚═══════════════════════════════════════════════════════════╝");

        newActualDefaulters.add(ad);
    }

    // RULE 3: Charge Calculation
    private void executeChargeRules(Account account, ExecutionContext context,
                                    List<ActualDefaulter> existingActualDefaulters,
                                    List<ActualDefaulter> newActualDefaulters,
                                    List<Charge> existingCharges,
                                    List<Charge> newCharges) {

        // Combine existing and new actual defaulters
        List<ActualDefaulter> allActualDefaulters = new ArrayList<>(existingActualDefaulters);
        allActualDefaulters.addAll(newActualDefaulters);

        // Find actual defaulters for month-2 and month-1
        ActualDefaulter ad1 = allActualDefaulters.stream()
                .filter(ad -> ad.getAccountId().equals(account.getAccountId()) &&
                        ad.getMonth() == (context.getCurrentMonth() - 2))
                .findFirst()
                .orElse(null);

        ActualDefaulter ad2 = allActualDefaulters.stream()
                .filter(ad -> ad.getAccountId().equals(account.getAccountId()) &&
                        ad.getMonth() == (context.getCurrentMonth() - 1))
                .findFirst()
                .orElse(null);

        if (ad1 == null || ad2 == null) {
            return;
        }

        // Check if already charged for these months
        boolean alreadyCharged = existingCharges.stream()
                .anyMatch(c -> c.getAccountId().equals(account.getAccountId()) &&
                        (c.getChargedInMonth() == (context.getCurrentMonth() - 1) ||
                                c.getChargedInMonth() == (context.getCurrentMonth() - 2)));

        if (alreadyCharged) {
            return;
        }

        // Calculate charges
        double shortfall1 = ad1.getShortfall();
        double shortfall2 = ad2.getShortfall();

        double baseCharge1 = Math.min(shortfall1 * 0.06, 500.0);
        double gst1 = baseCharge1 * 0.18;
        double totalCharge1 = baseCharge1 + gst1;

        double baseCharge2 = Math.min(shortfall2 * 0.06, 500.0);
        double gst2 = baseCharge2 * 0.18;
        double totalCharge2 = baseCharge2 + gst2;

        double totalShortfall = shortfall1 + shortfall2;
        double totalBaseCharge = baseCharge1 + baseCharge2;
        double totalGST = gst1 + gst2;
        double grandTotal = totalCharge1 + totalCharge2;

        Charge charge = new Charge();
        charge.setAccountId(account.getAccountId());
        charge.setMonth1(context.getCurrentMonth() - 2);
        charge.setMonth2(context.getCurrentMonth() - 1);
        charge.setShortfall1(shortfall1);
        charge.setShortfall2(shortfall2);
        charge.setTotalShortfall(totalShortfall);
        charge.setBaseCharge(totalBaseCharge);
        charge.setGstAmount(totalGST);
        charge.setTotalCharge(grandTotal);
        charge.setChargedInMonth(context.getCurrentMonth());
        charge.setReason("Charged for 2-month consecutive defaults");

        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                  💰 CHARGE APPLIED 💰                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("║ Account Details:                                          ║");
        System.out.println("║   Account ID: " + String.format("%-44s", account.getAccountId()) + "║");
        System.out.println("║   Account Name: " + String.format("%-42s", account.getAccountName()) + "║");
        System.out.println("║   Charged On: Month " + String.format("%-40d", context.getCurrentMonth()) + "║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("║ Month " + String.format("%-2d", charge.getMonth1()) + " Charges:                                        ║");
        System.out.println("║   Shortfall: ₹" + String.format("%,-10.2f", shortfall1) + "                              ║");
        System.out.println("║   Base Charge (6%%): ₹" + String.format("%,-10.2f", baseCharge1) + " (capped at ₹500)  ║");
        System.out.println("║   GST (18%%): ₹" + String.format("%,-10.2f", gst1) + "                              ║");
        System.out.println("║   Subtotal: ₹" + String.format("%,-10.2f", totalCharge1) + "                               ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("║ Month " + String.format("%-2d", charge.getMonth2()) + " Charges:                                        ║");
        System.out.println("║   Shortfall: ₹" + String.format("%,-10.2f", shortfall2) + "                              ║");
        System.out.println("║   Base Charge (6%%): ₹" + String.format("%,-10.2f", baseCharge2) + " (capped at ₹500)  ║");
        System.out.println("║   GST (18%%): ₹" + String.format("%,-10.2f", gst2) + "                              ║");
        System.out.println("║   Subtotal: ₹" + String.format("%,-10.2f", totalCharge2) + "                               ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("║ TOTAL CHARGE: ₹" + String.format("%,-10.2f", grandTotal) + "                             ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("\n");

        newCharges.add(charge);
    }

    public static class RuleExecutionResult {
        private List<ProbableDefaulter> probableDefaulters;
        private List<ActualDefaulter> actualDefaulters;
        private List<Charge> charges;
        private int rulesFired;

        public RuleExecutionResult(List<ProbableDefaulter> probableDefaulters,
                                   List<ActualDefaulter> actualDefaulters,
                                   List<Charge> charges,
                                   int rulesFired) {
            this.probableDefaulters = probableDefaulters;
            this.actualDefaulters = actualDefaulters;
            this.charges = charges;
            this.rulesFired = rulesFired;
        }

        public List<ProbableDefaulter> getProbableDefaulters() { return probableDefaulters; }
        public List<ActualDefaulter> getActualDefaulters() { return actualDefaulters; }
        public List<Charge> getCharges() { return charges; }
        public int getRulesFired() { return rulesFired; }
    }
}