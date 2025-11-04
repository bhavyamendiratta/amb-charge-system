package com.bank.amb.service;

import com.bank.amb.model.*;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.List;

/**
 * AMB Rule Engine - Programmatic Configuration (No XML needed)
 * This version creates rules programmatically to avoid kmodule.xml issues
 */
public class AMBRuleEngineProgrammatic {

    private KieServices kieServices;
    private KieContainer kieContainer;

    // Inline DRL Rules as String - FIXED checkDay
    private static final String DRL_RULES =
            "package com.bank.amb.rules;\n" +
                    "\n" +
                    "import com.bank.amb.model.Account;\n" +
                    "import com.bank.amb.model.ProbableDefaulter;\n" +
                    "import com.bank.amb.model.ActualDefaulter;\n" +
                    "import com.bank.amb.model.Charge;\n" +
                    "import com.bank.amb.model.ExecutionContext;\n" +
                    "\n" +
                    "global java.util.List probableDefaultersList;\n" +
                    "global java.util.List actualDefaultersList;\n" +
                    "global java.util.List chargesList;\n" +
                    "\n" +
                    "// RULE 1A: Probable Defaulter - Was Actual Defaulter Last Month (NO SMS)\n" +
                    "rule \"R1A_ProbableDefaulter_WasActualPrevMonth_NoSMS\"\n" +
                    "    salience 110\n" +
                    "    when\n" +
                    "        $context : ExecutionContext( checkDay == 25 )\n" +
                    "        $account : Account(\n" +
                    "            $accountId : accountId,\n" +
                    "            $accountName : accountName,\n" +
                    "            $currentMonth : currentMonth,\n" +
                    "            $amb : calculateAMB(1, 25),\n" +
                    "            calculateAMB(1, 25) < $context.minBalance\n" +
                    "        )\n" +
                    "        ActualDefaulter(\n" +
                    "            accountId == $accountId,\n" +
                    "            month == ($currentMonth - 1)\n" +
                    "        )\n" +
                    "        not ProbableDefaulter(\n" +
                    "            accountId == $accountId,\n" +
                    "            month == $currentMonth\n" +
                    "        )\n" +
                    "    then\n" +
                    "        ProbableDefaulter pd = new ProbableDefaulter();\n" +
                    "        pd.setAccountId($accountId);\n" +
                    "        pd.setMonth($currentMonth);\n" +
                    "        pd.setAmb($amb);\n" +
                    "        pd.setSmsSent(false);\n" +
                    "        pd.setReason(\"Was actual defaulter in Month \" + ($currentMonth - 1) + \" - NO SMS\");\n" +
                    "\n" +
                    "        insert(pd);\n" +
                    "        probableDefaultersList.add(pd);\n" +
                    "\n" +
                    "        System.out.println(\"═══════════════════════════════════════════════════════════\");\n" +
                    "        System.out.println(\"[RULE 1A] PROBABLE DEFAULTER IDENTIFIED\");\n" +
                    "        System.out.println(\"  Account ID: \" + $accountId);\n" +
                    "        System.out.println(\"  Account Name: \" + $accountName);\n" +
                    "        System.out.println(\"  Month: \" + $currentMonth);\n" +
                    "        System.out.println(\"  AMB (Day 1-25): ₹\" + String.format(\"%.2f\", $amb));\n" +
                    "        System.out.println(\"  SMS Status: NOT SENT (was actual defaulter in Month \" + ($currentMonth - 1) + \")\");\n" +
                    "        System.out.println(\"  Reason: Continuing defaulter - SMS already sent earlier\");\n" +
                    "        System.out.println(\"═══════════════════════════════════════════════════════════\");\n" +
                    "end\n" +
                    "\n" +
                    "// RULE 1B: Probable Defaulter - New (SEND SMS)\n" +
                    "rule \"R1B_ProbableDefaulter_New_SendSMS\"\n" +
                    "    salience 100\n" +
                    "    when\n" +
                    "        $context : ExecutionContext( checkDay == 25 )\n" +
                    "        $account : Account(\n" +
                    "            $accountId : accountId,\n" +
                    "            $accountName : accountName,\n" +
                    "            $currentMonth : currentMonth,\n" +
                    "            $amb : calculateAMB(1, 25),\n" +
                    "            calculateAMB(1, 25) < $context.minBalance\n" +
                    "        )\n" +
                    "        not ActualDefaulter(\n" +
                    "            accountId == $accountId,\n" +
                    "            month == ($currentMonth - 1)\n" +
                    "        )\n" +
                    "        not ProbableDefaulter(\n" +
                    "            accountId == $accountId,\n" +
                    "            month == $currentMonth\n" +
                    "        )\n" +
                    "    then\n" +
                    "        ProbableDefaulter pd = new ProbableDefaulter();\n" +
                    "        pd.setAccountId($accountId);\n" +
                    "        pd.setMonth($currentMonth);\n" +
                    "        pd.setAmb($amb);\n" +
                    "        pd.setSmsSent(true);\n" +
                    "        pd.setReason(\"New probable defaulter - SMS sent\");\n" +
                    "\n" +
                    "        insert(pd);\n" +
                    "        probableDefaultersList.add(pd);\n" +
                    "\n" +
                    "        System.out.println(\"═══════════════════════════════════════════════════════════\");\n" +
                    "        System.out.println(\"[RULE 1B] NEW PROBABLE DEFAULTER IDENTIFIED\");\n" +
                    "        System.out.println(\"  Account ID: \" + $accountId);\n" +
                    "        System.out.println(\"  Account Name: \" + $accountName);\n" +
                    "        System.out.println(\"  Month: \" + $currentMonth);\n" +
                    "        System.out.println(\"  AMB (Day 1-25): ₹\" + String.format(\"%.2f\", $amb));\n" +
                    "        System.out.println(\"  Required Balance: ₹\" + String.format(\"%.2f\", $context.getMinBalance()));\n" +
                    "        System.out.println(\"  Deficit: ₹\" + String.format(\"%.2f\", ($context.getMinBalance() - $amb)));\n" +
                    "        System.out.println(\"  ✉️  SMS: SENT\");\n" +
                    "        System.out.println(\"  Message: Your average monthly balance is below minimum\");\n" +
                    "        System.out.println(\"           required. Please maintain sufficient balance or\");\n" +
                    "        System.out.println(\"           charges will be deducted.\");\n" +
                    "        System.out.println(\"═══════════════════════════════════════════════════════════\");\n" +
                    "end\n" +
                    "\n" +
                    "// RULE 2: Actual Defaulter Check\n" +
                    "rule \"R2_ActualDefaulter\"\n" +
                    "    salience 80\n" +
                    "    when\n" +
                    "        $context : ExecutionContext( checkDay == 3 )\n" +
                    "        $account : Account(\n" +
                    "            $accountId : accountId,\n" +
                    "            $accountName : accountName,\n" +
                    "            $currentMonth : currentMonth,\n" +
                    "            $amb : calculateAMB(1, 30),\n" +
                    "            calculateAMB(1, 30) < $context.minBalance\n" +
                    "        )\n" +
                    "        $pd : ProbableDefaulter(\n" +
                    "            accountId == $accountId,\n" +
                    "            month == ($currentMonth - 1)\n" +
                    "        )\n" +
                    "        not ActualDefaulter(\n" +
                    "            accountId == $accountId,\n" +
                    "            month == ($currentMonth - 1)\n" +
                    "        )\n" +
                    "    then\n" +
                    "        ActualDefaulter ad = new ActualDefaulter();\n" +
                    "        ad.setAccountId($accountId);\n" +
                    "        ad.setMonth($currentMonth - 1);\n" +
                    "        ad.setAmb($amb);\n" +
                    "        ad.setShortfall($context.getMinBalance() - $amb);\n" +
                    "        ad.setStatus(\"Confirmed actual defaulter for Month \" + ($currentMonth - 1));\n" +
                    "\n" +
                    "        insert(ad);\n" +
                    "        actualDefaultersList.add(ad);\n" +
                    "\n" +
                    "        System.out.println(\"╔═══════════════════════════════════════════════════════════╗\");\n" +
                    "        System.out.println(\"║ [RULE 2] ACTUAL DEFAULTER CONFIRMED                      ║\");\n" +
                    "        System.out.println(\"╚═══════════════════════════════════════════════════════════╝\");\n" +
                    "        System.out.println(\"  Account ID: \" + $accountId);\n" +
                    "        System.out.println(\"  Account Name: \" + $accountName);\n" +
                    "        System.out.println(\"  Defaulted Month: \" + ad.getMonth());\n" +
                    "        System.out.println(\"  Full Month AMB: ₹\" + String.format(\"%.2f\", $amb));\n" +
                    "        System.out.println(\"  Required Balance: ₹\" + String.format(\"%.2f\", $context.getMinBalance()));\n" +
                    "        System.out.println(\"  Shortfall: ₹\" + String.format(\"%.2f\", ad.getShortfall()));\n" +
                    "        System.out.println(\"  Status: \" + ad.getStatus());\n" +
                    "        System.out.println(\"╚═══════════════════════════════════════════════════════════╝\");\n" +
                    "end\n" +
                    "\n" +
                    "// RULE 3: Charge Calculation (Separate charges per month, then sum)\n" +
                    "rule \"R3_ChargeCalculation\"\n" +
                    "    salience 70\n" +
                    "    when\n" +
                    "        $context : ExecutionContext( checkDay == 3 )\n" +
                    "        $account : Account(\n" +
                    "            $accountId : accountId,\n" +
                    "            $accountName : accountName,\n" +
                    "            $currentMonth : currentMonth\n" +
                    "        )\n" +
                    "        $ad1 : ActualDefaulter(\n" +
                    "            accountId == $accountId,\n" +
                    "            month == ($currentMonth - 2),\n" +
                    "            $shortfall1 : shortfall\n" +
                    "        )\n" +
                    "        $ad2 : ActualDefaulter(\n" +
                    "            accountId == $accountId,\n" +
                    "            month == ($currentMonth - 1),\n" +
                    "            $shortfall2 : shortfall\n" +
                    "        )\n" +
                    "        not Charge(\n" +
                    "            accountId == $accountId,\n" +
                    "            chargedInMonth == ($currentMonth - 1) || chargedInMonth == ($currentMonth - 2)\n" +
                    "        )\n" +
                    "    then\n" +
                    "        // Calculate charge for Month 1\n" +
                    "        double baseCharge1 = Math.min($shortfall1 * 0.06, 500.0);\n" +
                    "        double gst1 = baseCharge1 * 0.18;\n" +
                    "        double totalCharge1 = baseCharge1 + gst1;\n" +
                    "\n" +
                    "        // Calculate charge for Month 2\n" +
                    "        double baseCharge2 = Math.min($shortfall2 * 0.06, 500.0);\n" +
                    "        double gst2 = baseCharge2 * 0.18;\n" +
                    "        double totalCharge2 = baseCharge2 + gst2;\n" +
                    "\n" +
                    "        // Total\n" +
                    "        double totalShortfall = $shortfall1 + $shortfall2;\n" +
                    "        double totalBaseCharge = baseCharge1 + baseCharge2;\n" +
                    "        double totalGST = gst1 + gst2;\n" +
                    "        double grandTotal = totalCharge1 + totalCharge2;\n" +
                    "\n" +
                    "        Charge charge = new Charge();\n" +
                    "        charge.setAccountId($accountId);\n" +
                    "        charge.setMonth1($currentMonth - 2);\n" +
                    "        charge.setMonth2($currentMonth - 1);\n" +
                    "        charge.setShortfall1($shortfall1);\n" +
                    "        charge.setShortfall2($shortfall2);\n" +
                    "        charge.setTotalShortfall(totalShortfall);\n" +
                    "        charge.setBaseCharge(totalBaseCharge);\n" +
                    "        charge.setGstAmount(totalGST);\n" +
                    "        charge.setTotalCharge(grandTotal);\n" +
                    "        charge.setChargedInMonth($currentMonth);\n" +
                    "        charge.setReason(\"Charged for 2-month consecutive defaults\");\n" +
                    "\n" +
                    "        insert(charge);\n" +
                    "        chargesList.add(charge);\n" +
                    "\n" +
                    "        System.out.println(\"\\n\");\n" +
                    "        System.out.println(\"╔═══════════════════════════════════════════════════════════╗\");\n" +
                    "        System.out.println(\"║                  💰 CHARGE APPLIED 💰                     ║\");\n" +
                    "        System.out.println(\"╚═══════════════════════════════════════════════════════════╝\");\n" +
                    "        System.out.println(\"║ Account Details:                                          ║\");\n" +
                    "        System.out.println(\"║   Account ID: \" + String.format(\"%-44s\", $accountId) + \"║\");\n" +
                    "        System.out.println(\"║   Account Name: \" + String.format(\"%-42s\", $accountName) + \"║\");\n" +
                    "        System.out.println(\"║   Charged On: Month \" + String.format(\"%-40d\", $currentMonth) + \"║\");\n" +
                    "        System.out.println(\"╠═══════════════════════════════════════════════════════════╣\");\n" +
                    "        System.out.println(\"║ Month \" + String.format(\"%-2d\", charge.getMonth1()) + \" Charges:                                        ║\");\n" +
                    "        System.out.println(\"║   Shortfall: ₹\" + String.format(\"%,-10.2f\", $shortfall1) + \"                              ║\");\n" +
                    "        System.out.println(\"║   Base Charge (6%%): ₹\" + String.format(\"%,-10.2f\", baseCharge1) + \" (capped at ₹500)  ║\");\n" +
                    "        System.out.println(\"║   GST (18%%): ₹\" + String.format(\"%,-10.2f\", gst1) + \"                              ║\");\n" +
                    "        System.out.println(\"║   Subtotal: ₹\" + String.format(\"%,-10.2f\", totalCharge1) + \"                               ║\");\n" +
                    "        System.out.println(\"╠═══════════════════════════════════════════════════════════╣\");\n" +
                    "        System.out.println(\"║ Month \" + String.format(\"%-2d\", charge.getMonth2()) + \" Charges:                                        ║\");\n" +
                    "        System.out.println(\"║   Shortfall: ₹\" + String.format(\"%,-10.2f\", $shortfall2) + \"                              ║\");\n" +
                    "        System.out.println(\"║   Base Charge (6%%): ₹\" + String.format(\"%,-10.2f\", baseCharge2) + \" (capped at ₹500)  ║\");\n" +
                    "        System.out.println(\"║   GST (18%%): ₹\" + String.format(\"%,-10.2f\", gst2) + \"                              ║\");\n" +
                    "        System.out.println(\"║   Subtotal: ₹\" + String.format(\"%,-10.2f\", totalCharge2) + \"                               ║\");\n" +
                    "        System.out.println(\"╠═══════════════════════════════════════════════════════════╣\");\n" +
                    "        System.out.println(\"║ TOTAL CHARGE: ₹\" + String.format(\"%,-10.2f\", grandTotal) + \"                             ║\");\n" +
                    "        System.out.println(\"╚═══════════════════════════════════════════════════════════╝\");\n" +
                    "        System.out.println(\"\\n\");\n" +
                    "end\n";

    public AMBRuleEngineProgrammatic() {
        try {
            System.out.println("Initializing Drools (Programmatic Mode)...");
            kieServices = KieServices.Factory.get();

            // Create KieFileSystem and add rules programmatically
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            kieFileSystem.write("src/main/resources/rules/bank-amb-rules.drl", DRL_RULES);

            // Build the KieModule
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            // Check for errors
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                System.err.println("Error building rules:");
                kieBuilder.getResults().getMessages(Message.Level.ERROR).forEach(msg -> {
                    System.err.println("  " + msg.getText());
                });
                throw new RuntimeException("Rule compilation failed");
            }

            // Create KieContainer
            kieContainer = kieServices.newKieContainer(
                    kieServices.getRepository().getDefaultReleaseId()
            );

            System.out.println("✓ Drools initialized successfully (Programmatic)!");
            System.out.println();

        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize Drools!");
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Drools", e);
        }
    }

    public RuleExecutionResult executeRules(
            List<Account> accounts,
            List<ProbableDefaulter> existingProbableDefaulters,
            List<ActualDefaulter> existingActualDefaulters,
            List<Charge> existingCharges,
            ExecutionContext context) {

        KieSession kieSession = null;

        try {
            // Create new session
            kieSession = kieContainer.newKieSession();

            if (kieSession == null) {
                throw new RuntimeException("Failed to create KieSession");
            }

            // Initialize result lists
            List<ProbableDefaulter> probableDefaulters = new ArrayList<>();
            List<ActualDefaulter> actualDefaulters = new ArrayList<>();
            List<Charge> charges = new ArrayList<>();

            // Set global variables
            kieSession.setGlobal("probableDefaultersList", probableDefaulters);
            kieSession.setGlobal("actualDefaultersList", actualDefaulters);
            kieSession.setGlobal("chargesList", charges);

            // Insert context
            kieSession.insert(context);

            // Insert existing data
            for (ProbableDefaulter pd : existingProbableDefaulters) {
                kieSession.insert(pd);
            }
            for (ActualDefaulter ad : existingActualDefaulters) {
                kieSession.insert(ad);
            }
            for (Charge charge : existingCharges) {
                kieSession.insert(charge);
            }

            // Insert accounts
            for (Account account : accounts) {
                account.setCurrentMonth(context.getCurrentMonth());
                kieSession.insert(account);
            }

            // Fire all rules
            int rulesFired = kieSession.fireAllRules();

            System.out.println("\n========================================");
            System.out.println("Rules Execution Summary");
            System.out.println("========================================");
            System.out.println("Rules Fired: " + rulesFired);
            System.out.println("Probable Defaulters: " + probableDefaulters.size());
            System.out.println("Actual Defaulters: " + actualDefaulters.size());
            System.out.println("Charges Applied: " + charges.size());
            System.out.println("========================================\n");

            return new RuleExecutionResult(
                    probableDefaulters,
                    actualDefaulters,
                    charges,
                    rulesFired
            );

        } catch (Exception e) {
            System.err.println("ERROR during rule execution: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Rule execution failed", e);
        } finally {
            if (kieSession != null) {
                kieSession.dispose();
            }
        }
    }

    public static class RuleExecutionResult {
        private List<ProbableDefaulter> probableDefaulters;
        private List<ActualDefaulter> actualDefaulters;
        private List<Charge> charges;
        private int rulesFired;

        public RuleExecutionResult(
                List<ProbableDefaulter> probableDefaulters,
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