package com.bank.amb.service;

import com.bank.amb.model.*;
import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelValidationException;
import dev.cel.common.types.SimpleType;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;

import java.util.*;

/**
 * Pure CEL-based Rule Engine for AMB Charge System
 * All rules written in CEL expressions (no DRL)
 */
public class AMBRuleEngine {

    private final CelCompiler compiler;
    private final CelRuntime runtime;
    private final Map<String, CelAbstractSyntaxTree> celRules;

    public AMBRuleEngine() throws CelValidationException {
        // Initialize CEL compiler with all variables
        this.compiler = CelCompilerFactory.standardCelCompilerBuilder()
                .addVar("checkDay", SimpleType.INT)
                .addVar("currentMonth", SimpleType.INT)
                .addVar("minBalance", SimpleType.DOUBLE)
                .addVar("amb", SimpleType.DOUBLE)
                .addVar("ambFull", SimpleType.DOUBLE)
                .addVar("wasActualDefaulterLastMonth", SimpleType.BOOL)
                .addVar("isProbableDefaulterThisMonth", SimpleType.BOOL)
                .addVar("wasProbableDefaulterLastMonth", SimpleType.BOOL)
                .addVar("wasActualDefaulter2MonthsAgo", SimpleType.BOOL)
                .addVar("alreadyCharged", SimpleType.BOOL)
                .build();

        this.runtime = CelRuntimeFactory.standardCelRuntimeBuilder().build();
        this.celRules = compileCELRules();
    }

    /**
     * Compile all business rules as CEL expressions
     */
    private Map<String, CelAbstractSyntaxTree> compileCELRules() throws CelValidationException {
        Map<String, CelAbstractSyntaxTree> rules = new HashMap<>();

        // Rule 1A: Probable Defaulter - Was Actual Last Month (No SMS)
        rules.put("RULE_1A", compiler.compile(
                "checkDay == 25 && amb < minBalance && wasActualDefaulterLastMonth && !isProbableDefaulterThisMonth"
        ).getAst());

        // Rule 1B: Probable Defaulter - New (Send SMS)
        rules.put("RULE_1B", compiler.compile(
                "checkDay == 25 && amb < minBalance && !wasActualDefaulterLastMonth && !isProbableDefaulterThisMonth"
        ).getAst());

        // Rule 2: Actual Defaulter
        rules.put("RULE_2", compiler.compile(
                "checkDay == 3 && ambFull < minBalance && wasProbableDefaulterLastMonth"
        ).getAst());

        // Rule 3: Charge Calculation
        rules.put("RULE_3", compiler.compile(
                "checkDay == 3 && wasActualDefaulter2MonthsAgo && wasActualDefaulterLastMonth && !alreadyCharged"
        ).getAst());

        return rules;
    }

    /**
     * Execute all CEL rules for given accounts and context
     */
    public RuleResult execute(List<Account> accounts, ExecutionContext context,
                              List<ProbableDefaulter> existingPD,
                              List<ActualDefaulter> existingAD,
                              List<Charge> existingCharges) {

        List<ProbableDefaulter> newPD = new ArrayList<>();
        List<ActualDefaulter> newAD = new ArrayList<>();
        List<Charge> newCharges = new ArrayList<>();

        for (Account account : accounts) {
            account.setCurrentMonth(context.getCurrentMonth());

            // Calculate AMB values
            double amb = account.calculateAMB(1, 25);
            double ambFull = account.calculateAMB(1, 30);

            // Build evaluation context
            Map<String, Object> evalCtx = buildEvalContext(account, context, amb, ambFull,
                    existingPD, existingAD, existingCharges);

            // Execute rules in order
            try {
                evaluateRule1A(evalCtx, account, newPD);
                evaluateRule1B(evalCtx, account, context, newPD);
                evaluateRule2(evalCtx, account, context, newAD);
            } catch (CelEvaluationException e) {
                System.err.println("Error evaluating rules for account " + account.getAccountId());
                e.printStackTrace();
            }
        }

        // After all accounts processed, evaluate Rule 3 with complete AD list
        List<ActualDefaulter> allAD = new ArrayList<>(existingAD);
        allAD.addAll(newAD);

        for (Account account : accounts) {
            account.setCurrentMonth(context.getCurrentMonth());
            double amb = account.calculateAMB(1, 25);
            double ambFull = account.calculateAMB(1, 30);

            Map<String, Object> evalCtx = buildEvalContext(account, context, amb, ambFull,
                    existingPD, allAD, existingCharges);
            try {
                evaluateRule3(evalCtx, account, context, allAD, newCharges);
            } catch (CelEvaluationException e) {
                System.err.println("Error evaluating rule 3 for account " + account.getAccountId());
                e.printStackTrace();
            }
        }

        return new RuleResult(newPD, newAD, newCharges);
    }

    private Map<String, Object> buildEvalContext(Account account, ExecutionContext context,
                                                 double amb, double ambFull,
                                                 List<ProbableDefaulter> existingPD,
                                                 List<ActualDefaulter> existingAD,
                                                 List<Charge> existingCharges) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("checkDay", context.getCheckDay());
        ctx.put("currentMonth", context.getCurrentMonth());
        ctx.put("minBalance", context.getMinBalance());
        ctx.put("amb", amb);
        ctx.put("ambFull", ambFull);

        ctx.put("wasActualDefaulterLastMonth",
                existingAD.stream().anyMatch(ad -> ad.getAccountId().equals(account.getAccountId())
                        && ad.getMonth() == context.getCurrentMonth() - 1));

        ctx.put("isProbableDefaulterThisMonth",
                existingPD.stream().anyMatch(pd -> pd.getAccountId().equals(account.getAccountId())
                        && pd.getMonth() == context.getCurrentMonth()));

        ctx.put("wasProbableDefaulterLastMonth",
                existingPD.stream().anyMatch(pd -> pd.getAccountId().equals(account.getAccountId())
                        && pd.getMonth() == context.getCurrentMonth() - 1));

        ctx.put("wasActualDefaulter2MonthsAgo",
                existingAD.stream().anyMatch(ad -> ad.getAccountId().equals(account.getAccountId())
                        && ad.getMonth() == context.getCurrentMonth() - 2));

        ctx.put("alreadyCharged",
                existingCharges.stream().anyMatch(c -> c.getAccountId().equals(account.getAccountId())
                        && (c.getChargedInMonth() == context.getCurrentMonth() - 1
                        || c.getChargedInMonth() == context.getCurrentMonth() - 2)));

        return ctx;
    }

    private void evaluateRule1A(Map<String, Object> ctx, Account account,
                                List<ProbableDefaulter> results) throws CelEvaluationException {
        Boolean matches = (Boolean) runtime.createProgram(celRules.get("RULE_1A")).eval(ctx);
        if (matches) {
            ProbableDefaulter pd = new ProbableDefaulter();
            pd.setAccountId(account.getAccountId());
            pd.setMonth((Integer) ctx.get("currentMonth"));
            pd.setAmb((Double) ctx.get("amb"));
            pd.setSmsSent(false);
            pd.setReason("Continuing defaulter - NO SMS");
            results.add(pd);

            System.out.println("[CEL RULE 1A] Probable Defaulter (No SMS): " + account.getAccountId());
        }
    }

    private void evaluateRule1B(Map<String, Object> ctx, Account account, ExecutionContext context,
                                List<ProbableDefaulter> results) throws CelEvaluationException {
        Boolean matches = (Boolean) runtime.createProgram(celRules.get("RULE_1B")).eval(ctx);
        if (matches) {
            ProbableDefaulter pd = new ProbableDefaulter();
            pd.setAccountId(account.getAccountId());
            pd.setMonth((Integer) ctx.get("currentMonth"));
            pd.setAmb((Double) ctx.get("amb"));
            pd.setSmsSent(true);
            pd.setReason("New probable defaulter - SMS SENT");
            results.add(pd);

            System.out.println("[CEL RULE 1B] New Probable Defaulter (SMS): " + account.getAccountId()
                    + " | AMB: ₹" + String.format("%.2f", pd.getAmb()));
        }
    }

    private void evaluateRule2(Map<String, Object> ctx, Account account, ExecutionContext context,
                               List<ActualDefaulter> results) throws CelEvaluationException {
        Boolean matches = (Boolean) runtime.createProgram(celRules.get("RULE_2")).eval(ctx);
        if (matches) {
            ActualDefaulter ad = new ActualDefaulter();
            ad.setAccountId(account.getAccountId());
            ad.setMonth(context.getCurrentMonth() - 1);
            ad.setAmb((Double) ctx.get("ambFull"));
            ad.setShortfall(context.getMinBalance() - (Double) ctx.get("ambFull"));
            ad.setStatus("Confirmed actual defaulter");
            results.add(ad);

            System.out.println("[CEL RULE 2] Actual Defaulter: " + account.getAccountId()
                    + " | Month: " + ad.getMonth()
                    + " | Shortfall: ₹" + String.format("%.2f", ad.getShortfall()));
        }
    }

    private void evaluateRule3(Map<String, Object> ctx, Account account, ExecutionContext context,
                               List<ActualDefaulter> existingAD, List<Charge> results)
            throws CelEvaluationException {
        Boolean matches = (Boolean) runtime.createProgram(celRules.get("RULE_3")).eval(ctx);
        if (matches) {
            ActualDefaulter ad1 = existingAD.stream()
                    .filter(ad -> ad.getAccountId().equals(account.getAccountId())
                            && ad.getMonth() == context.getCurrentMonth() - 2)
                    .findFirst().orElse(null);

            ActualDefaulter ad2 = existingAD.stream()
                    .filter(ad -> ad.getAccountId().equals(account.getAccountId())
                            && ad.getMonth() == context.getCurrentMonth() - 1)
                    .findFirst().orElse(null);

            if (ad1 != null && ad2 != null) {
                double baseCharge1 = Math.min(ad1.getShortfall() * 0.06, 500.0);
                double gst1 = baseCharge1 * 0.18;
                double baseCharge2 = Math.min(ad2.getShortfall() * 0.06, 500.0);
                double gst2 = baseCharge2 * 0.18;

                Charge charge = new Charge();
                charge.setAccountId(account.getAccountId());
                charge.setMonth1(ad1.getMonth());
                charge.setMonth2(ad2.getMonth());
                charge.setShortfall1(ad1.getShortfall());
                charge.setShortfall2(ad2.getShortfall());
                charge.setTotalShortfall(ad1.getShortfall() + ad2.getShortfall());
                charge.setBaseCharge(baseCharge1 + baseCharge2);
                charge.setGstAmount(gst1 + gst2);
                charge.setTotalCharge((baseCharge1 + gst1) + (baseCharge2 + gst2));
                charge.setChargedInMonth(context.getCurrentMonth());
                results.add(charge);

                System.out.println("[CEL RULE 3] Charge Applied: " + account.getAccountId()
                        + " | Months: " + charge.getMonth1() + "+" + charge.getMonth2()
                        + " | Total: ₹" + String.format("%.2f", charge.getTotalCharge()));
            }
        }
    }

    public static class RuleResult {
        public final List<ProbableDefaulter> probableDefaulters;
        public final List<ActualDefaulter> actualDefaulters;
        public final List<Charge> charges;

        public RuleResult(List<ProbableDefaulter> pd, List<ActualDefaulter> ad, List<Charge> ch) {
            this.probableDefaulters = pd;
            this.actualDefaulters = ad;
            this.charges = ch;
        }
    }
}