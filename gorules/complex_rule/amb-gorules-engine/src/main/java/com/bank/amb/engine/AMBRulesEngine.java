package com.bank.amb.engine;

import io.gorules.zen_engine.ZenEngine;
import io.gorules.zen_engine.JsonBuffer;
import io.gorules.zen_engine.ZenDecisionLoaderCallback;
import io.gorules.zen_engine.ZenEngineResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bank.amb.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * AMB Rules Engine using GoRules Zen Engine
 */
public class AMBRulesEngine {

    private final ZenEngine engine;
    private final ObjectMapper objectMapper;
    private final String decisionKey = "amb-rules";

    public AMBRulesEngine() {
        this.objectMapper = new ObjectMapper();

        ZenDecisionLoaderCallback loaderCallback = (key) -> {
            String content = loadDecisionModel("src/main/resources/rules/amb-rules.json");
            return CompletableFuture.completedFuture(new JsonBuffer(content));
        };

        this.engine = new ZenEngine(loaderCallback, null);
        System.out.println("✓ AMB Rules Engine initialized with GoRules Zen Engine!");
    }

    public RuleExecutionResult executeRules(
            List<Account> accounts,
            List<ProbableDefaulter> existingProbableDefaulters,
            List<ActualDefaulter> existingActualDefaulters,
            List<Charge> existingCharges,
            ExecutionContext context) {

        List<ProbableDefaulter> probableDefaulters = new ArrayList<>();
        List<ActualDefaulter> actualDefaulters = new ArrayList<>();
        List<Charge> charges = new ArrayList<>();

        int rulesExecuted = 0;

        for (Account account : accounts) {
            try {
                Map<String, Object> input = prepareInput(
                        account, existingProbableDefaulters, existingActualDefaulters, context);

                JsonBuffer inputBuffer = new JsonBuffer(objectMapper.writeValueAsString(input));
                CompletableFuture<ZenEngineResponse> futureResponse =
                        engine.evaluate(decisionKey, inputBuffer, null);

                ZenEngineResponse response = futureResponse.join();
                String resultJson = response.result().toString();
                Map<String, Object> result = objectMapper.readValue(resultJson, Map.class);

                processResults(account, result, context,
                        probableDefaulters, actualDefaulters, charges,
                        existingProbableDefaulters, existingActualDefaulters);

                rulesExecuted++;

            } catch (Exception e) {
                System.err.println("Error evaluating account " + account.getAccountId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        printSummary(probableDefaulters, actualDefaulters, charges, rulesExecuted);

        return new RuleExecutionResult(probableDefaulters, actualDefaulters, charges, rulesExecuted);
    }

    private Map<String, Object> prepareInput(
            Account account,
            List<ProbableDefaulter> existingPD,
            List<ActualDefaulter> existingAD,
            ExecutionContext context) {

        Map<String, Object> input = new HashMap<>();

        input.put("checkDay", context.getCheckDay());
        input.put("accountId", account.getAccountId());
        input.put("accountName", account.getAccountName());
        input.put("currentMonth", context.getCurrentMonth());
        input.put("minBalance", context.getMinBalance());

        double ambDay1To25 = account.calculateAMB(1, 25);
        double ambDay1To30 = account.calculateAMB(1, 30);
        input.put("ambDay1To25", ambDay1To25);
        input.put("ambDay1To30", ambDay1To30);

        // ============ DEBUG LOGGING ============
        System.out.println("  🔍 DEBUG INPUT:");
        System.out.println("     Account: " + account.getAccountId());
        System.out.println("     Check Day: " + context.getCheckDay());
        System.out.println("     Current Month: " + context.getCurrentMonth());
        System.out.println("     AMB (1-25): ₹" + String.format("%.2f", ambDay1To25));
        System.out.println("     AMB (1-30): ₹" + String.format("%.2f", ambDay1To30));
        System.out.println("     Min Balance: ₹" + String.format("%.2f", context.getMinBalance()));
        System.out.println("     Below Min? " + (ambDay1To25 < context.getMinBalance()));
        // =======================================

        boolean wasActualLastMonth = existingAD.stream()
                .anyMatch(ad -> ad.getAccountId().equals(account.getAccountId())
                        && ad.getMonth() == context.getCurrentMonth() - 1);
        input.put("wasActualDefaulterLastMonth", wasActualLastMonth);

        boolean wasProbableLastMonth = existingPD.stream()
                .anyMatch(pd -> pd.getAccountId().equals(account.getAccountId())
                        && pd.getMonth() == context.getCurrentMonth() - 1);
        input.put("wasProbableDefaulterLastMonth", wasProbableLastMonth);

        // ============ FIX: Check for actual defaulters in the CORRECT months ============
        // For charges on Day 3 of Month N, we need:
        // - Month N-2 actual defaulter (adMonth2)
        // - Month N-1 actual defaulter (adMonth1)

        ActualDefaulter adMonth2 = existingAD.stream()
                .filter(ad -> ad.getAccountId().equals(account.getAccountId())
                        && ad.getMonth() == context.getCurrentMonth() - 2)  // Look 2 months back
                .findFirst().orElse(null);

        ActualDefaulter adMonth1 = existingAD.stream()
                .filter(ad -> ad.getAccountId().equals(account.getAccountId())
                        && ad.getMonth() == context.getCurrentMonth() - 1)  // Look 1 month back
                .findFirst().orElse(null);

        input.put("actualDefaulterMonth2", adMonth2 != null);
        input.put("actualDefaulterMonth1", adMonth1 != null);
        input.put("shortfallMonth2", adMonth2 != null ? adMonth2.getShortfall() : 0.0);
        input.put("shortfallMonth1", adMonth1 != null ? adMonth1.getShortfall() : 0.0);

        // ============ MORE DEBUG ============
        System.out.println("     Was Actual Last Month: " + wasActualLastMonth);
        System.out.println("     Was Probable Last Month: " + wasProbableLastMonth);
        System.out.println("     Actual Defaulter M-1 (Month " + (context.getCurrentMonth() - 1) + "): " + (adMonth1 != null));
        System.out.println("     Actual Defaulter M-2 (Month " + (context.getCurrentMonth() - 2) + "): " + (adMonth2 != null));
        if (adMonth1 != null) {
            System.out.println("     Shortfall M-1: ₹" + String.format("%.2f", adMonth1.getShortfall()));
        }
        if (adMonth2 != null) {
            System.out.println("     Shortfall M-2: ₹" + String.format("%.2f", adMonth2.getShortfall()));
        }
        // ====================================

        return input;
    }

    @SuppressWarnings("unchecked")
    private void processResults(
            Account account,
            Map<String, Object> result,
            ExecutionContext context,
            List<ProbableDefaulter> probableDefaulters,
            List<ActualDefaulter> actualDefaulters,
            List<Charge> charges,
            List<ProbableDefaulter> existingPD,
            List<ActualDefaulter> existingAD) {

        // ============ DEBUG OUTPUT ============
        System.out.println("  📤 DEBUG OUTPUT:");
        System.out.println("     Result: " + result);
        // ======================================

        // Process Probable Defaulter
        String pdAction = (String) result.get("probableDefaulterAction");
        if ("MARK_PROBABLE_DEFAULTER".equals(pdAction)) {
            Boolean sendSMS = (Boolean) result.get("sendSMS");
            String reason = (String) result.get("probableDefaulterReason");

            ProbableDefaulter pd = new ProbableDefaulter();
            pd.setAccountId(account.getAccountId());
            pd.setMonth(context.getCurrentMonth());
            pd.setAmb(account.calculateAMB(1, 25));
            pd.setSmsSent(sendSMS != null && sendSMS);
            pd.setReason(reason);

            probableDefaulters.add(pd);
            printProbableDefaulter(account, pd, context);
        }

        // Process Actual Defaulter
        String adAction = (String) result.get("actualDefaulterAction");
        ActualDefaulter newActualDefaulter = null;
        if ("MARK_ACTUAL_DEFAULTER".equals(adAction)) {
            double shortfall = context.getMinBalance() - account.calculateAMB(1, 30);

            ActualDefaulter ad = new ActualDefaulter();
            ad.setAccountId(account.getAccountId());
            ad.setMonth(context.getCurrentMonth() - 1);
            ad.setAmb(account.calculateAMB(1, 30));
            ad.setShortfall(shortfall);
            ad.setStatus("Confirmed actual defaulter");

            actualDefaulters.add(ad);
            newActualDefaulter = ad;  // Save for charge check
            printActualDefaulter(account, ad, context);
        }

        // ============ NEW: CHECK FOR CHARGES AFTER ADDING NEW ACTUAL DEFAULTER ============
        // If we just added a new actual defaulter, check if we now have 2 consecutive defaults
        if (newActualDefaulter != null && context.getCheckDay() == 3) {
            // Look for an actual defaulter in the previous month
            int targetMonth = context.getCurrentMonth() - 2;  // Two months back from current

            ActualDefaulter previousDefaulter = existingAD.stream()
                    .filter(ad -> ad.getAccountId().equals(account.getAccountId())
                            && ad.getMonth() == targetMonth)
                    .findFirst().orElse(null);

            if (previousDefaulter != null) {
                // We have TWO consecutive defaults! Apply charge
                System.out.println("  💰 CHARGE TRIGGERED! Found consecutive defaults for months " +
                        targetMonth + " and " + (context.getCurrentMonth() - 1));

                Charge charge = calculateCharge(account.getAccountId(), context.getCurrentMonth(),
                        previousDefaulter.getShortfall(), newActualDefaulter.getShortfall());
                charges.add(charge);
                printCharge(account, charge);
            }
        }
        // ==================================================================================

        // OLD charge processing from rules (kept for backwards compatibility)
        String chargeAction = (String) result.get("chargeAction");
        if ("APPLY_CHARGE".equals(chargeAction)) {
            Object sf1Obj = result.get("shortfallMonth1");
            Object sf2Obj = result.get("shortfallMonth2");

            double shortfall1 = sf1Obj instanceof Number ? ((Number) sf1Obj).doubleValue() : 0.0;
            double shortfall2 = sf2Obj instanceof Number ? ((Number) sf2Obj).doubleValue() : 0.0;

            Charge charge = calculateCharge(account.getAccountId(), context.getCurrentMonth(),
                    shortfall1, shortfall2);
            charges.add(charge);
            printCharge(account, charge);
        }
    }

    private Charge calculateCharge(String accountId, int currentMonth,
                                   double shortfall1, double shortfall2) {
        // Month 1 charge
        double baseCharge1 = Math.min(shortfall1 * 0.06, 500.0);
        double gst1 = baseCharge1 * 0.18;
        double totalCharge1 = baseCharge1 + gst1;

        // Month 2 charge
        double baseCharge2 = Math.min(shortfall2 * 0.06, 500.0);
        double gst2 = baseCharge2 * 0.18;
        double totalCharge2 = baseCharge2 + gst2;

        // Total
        double totalShortfall = shortfall1 + shortfall2;
        double totalBaseCharge = baseCharge1 + baseCharge2;
        double totalGST = gst1 + gst2;
        double grandTotal = totalCharge1 + totalCharge2;

        Charge charge = new Charge();
        charge.setAccountId(accountId);
        charge.setMonth1(currentMonth - 2);
        charge.setMonth2(currentMonth - 1);
        charge.setShortfall1(shortfall1);
        charge.setShortfall2(shortfall2);
        charge.setTotalShortfall(totalShortfall);
        charge.setBaseCharge(totalBaseCharge);
        charge.setGstAmount(totalGST);
        charge.setTotalCharge(grandTotal);
        charge.setChargedInMonth(currentMonth);
        charge.setReason("Charged for 2-month consecutive defaults");

        return charge;
    }

    private void printProbableDefaulter(Account account, ProbableDefaulter pd, ExecutionContext context) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println(pd.isSmsSent() ? "[RULE 1B] NEW PROBABLE DEFAULTER" : "[RULE 1A] PROBABLE DEFAULTER");
        System.out.println("  Account: " + account.getAccountName() + " (" + account.getAccountId() + ")");
        System.out.println("  Month: " + pd.getMonth());
        System.out.println("  AMB (Day 1-25): ₹" + String.format("%.2f", pd.getAmb()));
        System.out.println("  Required: ₹" + String.format("%.2f", context.getMinBalance()));
        System.out.println("  Deficit: ₹" + String.format("%.2f", context.getMinBalance() - pd.getAmb()));
        System.out.println("  SMS: " + (pd.isSmsSent() ? "✉️ SENT" : "❌ NOT SENT"));
        System.out.println("  Reason: " + pd.getReason());
        System.out.println("═══════════════════════════════════════════════════════════");
    }

    private void printActualDefaulter(Account account, ActualDefaulter ad, ExecutionContext context) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║ [RULE 2] ACTUAL DEFAULTER CONFIRMED                      ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("  Account: " + account.getAccountName() + " (" + account.getAccountId() + ")");
        System.out.println("  Defaulted Month: " + ad.getMonth());
        System.out.println("  AMB (Full Month): ₹" + String.format("%.2f", ad.getAmb()));
        System.out.println("  Required: ₹" + String.format("%.2f", context.getMinBalance()));
        System.out.println("  Shortfall: ₹" + String.format("%.2f", ad.getShortfall()));
        System.out.println("  Status: " + ad.getStatus());
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");
    }

    private void printCharge(Account account, Charge charge) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                  💰 CHARGE APPLIED 💰                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("  Account: " + account.getAccountName() + " (" + account.getAccountId() + ")");
        System.out.println("  Charged In Month: " + charge.getChargedInMonth());
        System.out.println("  ─────────────────────────────────────────────────────────");
        System.out.println("  Month " + charge.getMonth1() + " Shortfall: ₹" + String.format("%.2f", charge.getShortfall1()));
        System.out.println("  Month " + charge.getMonth2() + " Shortfall: ₹" + String.format("%.2f", charge.getShortfall2()));
        System.out.println("  ─────────────────────────────────────────────────────────");
        System.out.println("  Base Charge (6%): ₹" + String.format("%.2f", charge.getBaseCharge()));
        System.out.println("  GST (18%): ₹" + String.format("%.2f", charge.getGstAmount()));
        System.out.println("  ─────────────────────────────────────────────────────────");
        System.out.println("  💰 TOTAL CHARGE: ₹" + String.format("%.2f", charge.getTotalCharge()));
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");
    }

    private void printSummary(List<ProbableDefaulter> pd, List<ActualDefaulter> ad,
                              List<Charge> charges, int rulesExecuted) {
        System.out.println("\n========================================");
        System.out.println("GoRules Execution Summary");
        System.out.println("========================================");
        System.out.println("Accounts Evaluated: " + rulesExecuted);
        System.out.println("Probable Defaulters: " + pd.size());
        System.out.println("Actual Defaulters: " + ad.size());
        System.out.println("Charges Applied: " + charges.size());
        System.out.println("========================================\n");
    }

    private String loadDecisionModel(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load decision model: " + filePath, e);
        }
    }

    public static class RuleExecutionResult {
        private List<ProbableDefaulter> probableDefaulters;
        private List<ActualDefaulter> actualDefaulters;
        private List<Charge> charges;
        private int rulesExecuted;

        public RuleExecutionResult(List<ProbableDefaulter> probableDefaulters,
                                   List<ActualDefaulter> actualDefaulters,
                                   List<Charge> charges,
                                   int rulesExecuted) {
            this.probableDefaulters = probableDefaulters;
            this.actualDefaulters = actualDefaulters;
            this.charges = charges;
            this.rulesExecuted = rulesExecuted;
        }

        public List<ProbableDefaulter> getProbableDefaulters() { return probableDefaulters; }
        public List<ActualDefaulter> getActualDefaulters() { return actualDefaulters; }
        public List<Charge> getCharges() { return charges; }
        public int getRulesExecuted() { return rulesExecuted; }
    }
}