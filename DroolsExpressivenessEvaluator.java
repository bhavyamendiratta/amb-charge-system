package com.ruleengine.evaluator;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.*;

/**
 * Evaluates Drools rule expressiveness with three complexity levels:
 * 1. Simple: Basic field matching and output
 * 2. Medium: Multiple conditions with logical operators and calculations
 * 3. Complex: Advanced patterns with accumulations, temporal reasoning, and nested conditions
 */
public class DroolsExpressivenessEvaluator {

    public static void main(String[] args) {
        DroolsExpressivenessEvaluator evaluator = new DroolsExpressivenessEvaluator();
        
        System.out.println("=== Testing Simple Rules ===");
        evaluator.testSimpleRules();
        
        System.out.println("\n=== Testing Medium Rules ===");
        evaluator.testMediumRules();
        
        System.out.println("\n=== Testing Complex Rules ===");
        evaluator.testComplexRules();
    }

    /**
     * SIMPLE RULES: Basic pattern matching with direct field access
     * Use Case: Simple eligibility checks, basic categorization
     */
    public void testSimpleRules() {
        String simpleRulesDrl = generateSimpleRules();
        
        KieSession kieSession = createKieSession(simpleRulesDrl);
        Map<String, Object> output = new HashMap<>();
        
        // Test Case 1: Age-based eligibility
        Map<String, Object> request1 = new HashMap<>();
        request1.put("age", 25);
        request1.put("type", "adult");
        
        kieSession.insert(new GenericRequest(request1));
        kieSession.setGlobal("output", output);
        kieSession.fireAllRules();
        
        System.out.println("Simple Rule Test 1 - Output: " + output);
        output.clear();
        
        // Test Case 2: Status check
        Map<String, Object> request2 = new HashMap<>();
        request2.put("status", "active");
        request2.put("level", "premium");
        
        kieSession.insert(new GenericRequest(request2));
        kieSession.fireAllRules();
        
        System.out.println("Simple Rule Test 2 - Output: " + output);
        
        kieSession.dispose();
    }

    /**
     * MEDIUM RULES: Multiple conditions, calculations, and logical operations
     * Use Case: Business logic with computations, multi-criteria decisions
     */
    public void testMediumRules() {
        String mediumRulesDrl = generateMediumRules();
        
        KieSession kieSession = createKieSession(mediumRulesDrl);
        Map<String, Object> output = new HashMap<>();
        
        // Test Case 1: Loan eligibility with multiple criteria
        Map<String, Object> request1 = new HashMap<>();
        request1.put("income", 75000);
        request1.put("creditScore", 720);
        request1.put("age", 35);
        request1.put("employmentYears", 5);
        request1.put("loanAmount", 300000);
        
        kieSession.insert(new GenericRequest(request1));
        kieSession.setGlobal("output", output);
        kieSession.fireAllRules();
        
        System.out.println("Medium Rule Test 1 - Output: " + output);
        output.clear();
        
        // Test Case 2: Pricing with discounts
        Map<String, Object> request2 = new HashMap<>();
        request2.put("purchaseAmount", 1500.0);
        request2.put("membershipLevel", "gold");
        request2.put("purchaseCount", 12);
        request2.put("seasonalPromo", true);
        
        kieSession.insert(new GenericRequest(request2));
        kieSession.fireAllRules();
        
        System.out.println("Medium Rule Test 2 - Output: " + output);
        
        kieSession.dispose();
    }

    /**
     * COMPLEX RULES: Advanced patterns, accumulations, temporal logic, nested conditions
     * Use Case: Fraud detection, risk assessment, pattern recognition
     */
    public void testComplexRules() {
        String complexRulesDrl = generateComplexRules();
        
        KieSession kieSession = createKieSession(complexRulesDrl);
        Map<String, Object> output = new HashMap<>();
        
        // Test Case 1: Transaction fraud detection
        Map<String, Object> request1 = new HashMap<>();
        request1.put("transactionAmount", 5000.0);
        request1.put("location", "foreign");
        request1.put("timeOfDay", "3AM");
        request1.put("averageTransactionAmount", 200.0);
        request1.put("accountAge", 30); // days
        request1.put("recentTransactionCount", 15);
        request1.put("deviceFingerprint", "unknown");
        
        List<Map<String, Object>> transactionHistory = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> txn = new HashMap<>();
            txn.put("amount", 4500.0 + (i * 100));
            txn.put("timestamp", System.currentTimeMillis() - (i * 3600000));
            transactionHistory.add(txn);
        }
        request1.put("recentTransactions", transactionHistory);
        
        kieSession.insert(new GenericRequest(request1));
        kieSession.setGlobal("output", output);
        kieSession.fireAllRules();
        
        System.out.println("Complex Rule Test 1 - Output: " + output);
        output.clear();
        
        // Test Case 2: Multi-factor risk assessment
        Map<String, Object> request2 = new HashMap<>();
        request2.put("riskFactors", Arrays.asList("high_velocity", "suspicious_pattern", "blacklisted_ip"));
        request2.put("accountValue", 50000.0);
        request2.put("behaviorScore", 35.0); // out of 100
        request2.put("geoLocation", Map.of("country", "XX", "risk", "high"));
        request2.put("linkedAccounts", 5);
        request2.put("verificationLevel", "basic");
        
        kieSession.insert(new GenericRequest(request2));
        kieSession.fireAllRules();
        
        System.out.println("Complex Rule Test 2 - Output: " + output);
        
        kieSession.dispose();
    }

    /**
     * Generate Simple DRL Rules
     */
    private String generateSimpleRules() {
        return """
            package com.ruleengine.evaluator;
            
            import java.util.Map;
            
            global java.util.Map output;
            
            // SIMPLE RULE 1: Basic age check
            rule "Simple_AgeEligibility"
                salience 100
                when
                    $req: GenericRequest(
                        data["age"] != null,
                        (Integer) data["age"] >= 18
                    )
                then
                    output.put("eligible", true);
                    output.put("category", "adult");
                    System.out.println("[SIMPLE] Age eligibility passed");
            end
            
            // SIMPLE RULE 2: Status-based access
            rule "Simple_PremiumAccess"
                salience 90
                when
                    $req: GenericRequest(
                        data["status"] == "active",
                        data["level"] == "premium"
                    )
                then
                    output.put("accessLevel", "full");
                    output.put("features", java.util.Arrays.asList("feature1", "feature2", "feature3"));
                    System.out.println("[SIMPLE] Premium access granted");
            end
            
            // SIMPLE RULE 3: Basic type classification
            rule "Simple_TypeClassification"
                salience 80
                when
                    $req: GenericRequest(
                        data["type"] != null
                    )
                then
                    output.put("processed", true);
                    output.put("type", $req.getData().get("type"));
                    System.out.println("[SIMPLE] Type classified: " + $req.getData().get("type"));
            end
            """;
    }

    /**
     * Generate Medium DRL Rules
     */
    private String generateMediumRules() {
        return """
            package com.ruleengine.evaluator;
            
            import java.util.Map;
            
            global java.util.Map output;
            
            // MEDIUM RULE 1: Loan eligibility with multiple conditions and calculations
            rule "Medium_LoanEligibility"
                salience 100
                when
                    $req: GenericRequest(
                        data["income"] != null,
                        data["creditScore"] != null,
                        data["age"] != null,
                        data["loanAmount"] != null,
                        (Integer) data["income"] >= 50000,
                        (Integer) data["creditScore"] >= 700,
                        (Integer) data["age"] >= 21 && (Integer) data["age"] <= 65,
                        (Integer) data["loanAmount"] <= ((Integer) data["income"] * 5)
                    )
                then
                    int income = (Integer) $req.getData().get("income");
                    int loanAmount = (Integer) $req.getData().get("loanAmount");
                    int creditScore = (Integer) $req.getData().get("creditScore");
                    
                    double dtiRatio = (loanAmount * 0.05) / income; // Assuming 5% annual payment
                    double interestRate = creditScore >= 750 ? 3.5 : creditScore >= 700 ? 4.5 : 5.5;
                    
                    output.put("loanApproved", true);
                    output.put("interestRate", interestRate);
                    output.put("dtiRatio", dtiRatio);
                    output.put("maxLoanAmount", income * 5);
                    
                    System.out.println("[MEDIUM] Loan approved with rate: " + interestRate + "%");
            end
            
            // MEDIUM RULE 2: Tiered discount calculation
            rule "Medium_TieredDiscount"
                salience 90
                when
                    $req: GenericRequest(
                        data["purchaseAmount"] != null,
                        data["membershipLevel"] != null,
                        data["purchaseCount"] != null,
                        (Double) data["purchaseAmount"] >= 100.0
                    )
                then
                    double amount = (Double) $req.getData().get("purchaseAmount");
                    String membership = (String) $req.getData().get("membershipLevel");
                    int purchaseCount = (Integer) $req.getData().get("purchaseCount");
                    
                    double discount = 0.0;
                    
                    // Membership tier discount
                    if ("platinum".equals(membership)) discount += 0.20;
                    else if ("gold".equals(membership)) discount += 0.15;
                    else if ("silver".equals(membership)) discount += 0.10;
                    
                    // Volume discount
                    if (amount >= 1000) discount += 0.05;
                    
                    // Loyalty discount
                    if (purchaseCount >= 10) discount += 0.05;
                    
                    // Seasonal promotion
                    if (Boolean.TRUE.equals($req.getData().get("seasonalPromo"))) discount += 0.10;
                    
                    // Cap discount at 40%
                    discount = Math.min(discount, 0.40);
                    
                    double finalAmount = amount * (1 - discount);
                    double savedAmount = amount - finalAmount;
                    
                    output.put("originalAmount", amount);
                    output.put("discountPercent", discount * 100);
                    output.put("finalAmount", finalAmount);
                    output.put("savedAmount", savedAmount);
                    
                    System.out.println("[MEDIUM] Discount applied: " + (discount * 100) + "%");
            end
            
            // MEDIUM RULE 3: Multi-criteria risk classification
            rule "Medium_RiskClassification"
                salience 80
                when
                    $req: GenericRequest(
                        data["income"] != null,
                        data["creditScore"] != null,
                        data["employmentYears"] != null
                    )
                then
                    int income = (Integer) $req.getData().get("income");
                    int creditScore = (Integer) $req.getData().get("creditScore");
                    int employmentYears = (Integer) $req.getData().get("employmentYears");
                    
                    int riskScore = 0;
                    String riskCategory;
                    
                    // Income factor
                    if (income >= 100000) riskScore += 30;
                    else if (income >= 50000) riskScore += 20;
                    else riskScore += 10;
                    
                    // Credit score factor
                    if (creditScore >= 750) riskScore += 40;
                    else if (creditScore >= 700) riskScore += 30;
                    else if (creditScore >= 650) riskScore += 20;
                    else riskScore += 10;
                    
                    // Employment stability factor
                    if (employmentYears >= 5) riskScore += 30;
                    else if (employmentYears >= 2) riskScore += 20;
                    else riskScore += 10;
                    
                    // Determine risk category
                    if (riskScore >= 80) riskCategory = "LOW";
                    else if (riskScore >= 60) riskCategory = "MEDIUM";
                    else riskCategory = "HIGH";
                    
                    output.put("riskScore", riskScore);
                    output.put("riskCategory", riskCategory);
                    output.put("requiresManualReview", riskScore < 60);
                    
                    System.out.println("[MEDIUM] Risk classified as: " + riskCategory + " (Score: " + riskScore + ")");
            end
            """;
    }

    /**
     * Generate Complex DRL Rules
     */
    private String generateComplexRules() {
        return """
            package com.ruleengine.evaluator;
            
            import java.util.Map;
            import java.util.List;
            import java.util.ArrayList;
            
            global java.util.Map output;
            
            // COMPLEX RULE 1: Multi-layer fraud detection with pattern analysis
            rule "Complex_FraudDetection"
                salience 100
                when
                    $req: GenericRequest(
                        data["transactionAmount"] != null,
                        data["averageTransactionAmount"] != null,
                        data["location"] != null,
                        (Double) data["transactionAmount"] > ((Double) data["averageTransactionAmount"] * 10)
                    )
                then
                    double txnAmount = (Double) $req.getData().get("transactionAmount");
                    double avgAmount = (Double) $req.getData().get("averageTransactionAmount");
                    String location = (String) $req.getData().get("location");
                    int accountAge = (Integer) $req.getData().get("accountAge");
                    
                    List<String> fraudIndicators = new ArrayList<>();
                    int fraudScore = 0;
                    
                    // High amount deviation
                    double deviation = txnAmount / avgAmount;
                    if (deviation > 20) {
                        fraudScore += 40;
                        fraudIndicators.add("EXTREME_AMOUNT_DEVIATION");
                    } else if (deviation > 10) {
                        fraudScore += 25;
                        fraudIndicators.add("HIGH_AMOUNT_DEVIATION");
                    }
                    
                    // Foreign location
                    if ("foreign".equals(location)) {
                        fraudScore += 20;
                        fraudIndicators.add("FOREIGN_LOCATION");
                    }
                    
                    // Unusual time
                    String timeOfDay = (String) $req.getData().get("timeOfDay");
                    if ("3AM".equals(timeOfDay) || "4AM".equals(timeOfDay)) {
                        fraudScore += 15;
                        fraudIndicators.add("UNUSUAL_TIME");
                    }
                    
                    // New account
                    if (accountAge < 90) {
                        fraudScore += 20;
                        fraudIndicators.add("NEW_ACCOUNT");
                    }
                    
                    // Unknown device
                    if ("unknown".equals($req.getData().get("deviceFingerprint"))) {
                        fraudScore += 25;
                        fraudIndicators.add("UNKNOWN_DEVICE");
                    }
                    
                    // Analyze recent transaction pattern
                    List<Map<String, Object>> recentTxns = (List<Map<String, Object>>) $req.getData().get("recentTransactions");
                    if (recentTxns != null && recentTxns.size() >= 3) {
                        boolean rapidSuccession = true;
                        for (Map<String, Object> txn : recentTxns) {
                            if ((Double) txn.get("amount") > avgAmount * 5) {
                                fraudScore += 10;
                            }
                        }
                        if (rapidSuccession) {
                            fraudIndicators.add("RAPID_SUCCESSION_PATTERN");
                        }
                    }
                    
                    // Determine action
                    String action;
                    String riskLevel;
                    
                    if (fraudScore >= 80) {
                        action = "BLOCK";
                        riskLevel = "CRITICAL";
                    } else if (fraudScore >= 60) {
                        action = "HOLD_FOR_REVIEW";
                        riskLevel = "HIGH";
                    } else if (fraudScore >= 40) {
                        action = "REQUIRE_2FA";
                        riskLevel = "MEDIUM";
                    } else {
                        action = "ALLOW_WITH_MONITORING";
                        riskLevel = "LOW";
                    }
                    
                    output.put("fraudScore", fraudScore);
                    output.put("riskLevel", riskLevel);
                    output.put("action", action);
                    output.put("fraudIndicators", fraudIndicators);
                    output.put("requiresInvestigation", fraudScore >= 60);
                    output.put("amountDeviation", deviation);
                    
                    System.out.println("[COMPLEX] Fraud analysis complete - Risk: " + riskLevel + ", Score: " + fraudScore);
            end
            
            // COMPLEX RULE 2: Multi-factor risk assessment with nested conditions
            rule "Complex_MultifactorRiskAssessment"
                salience 90
                when
                    $req: GenericRequest(
                        data["riskFactors"] != null,
                        data["accountValue"] != null,
                        data["behaviorScore"] != null,
                        ((List) data["riskFactors"]).size() > 0
                    )
                then
                    List<String> riskFactors = (List<String>) $req.getData().get("riskFactors");
                    double accountValue = (Double) $req.getData().get("accountValue");
                    double behaviorScore = (Double) $req.getData().get("behaviorScore");
                    Map<String, Object> geoLocation = (Map<String, Object>) $req.getData().get("geoLocation");
                    
                    int totalRiskScore = 0;
                    List<String> criticalFactors = new ArrayList<>();
                    Map<String, Integer> riskBreakdown = new java.util.HashMap<>();
                    
                    // Analyze each risk factor
                    for (String factor : riskFactors) {
                        int factorScore = 0;
                        switch (factor) {
                            case "high_velocity":
                                factorScore = 30;
                                criticalFactors.add("HIGH_VELOCITY_DETECTED");
                                break;
                            case "suspicious_pattern":
                                factorScore = 25;
                                criticalFactors.add("SUSPICIOUS_PATTERN");
                                break;
                            case "blacklisted_ip":
                                factorScore = 40;
                                criticalFactors.add("BLACKLISTED_IP");
                                break;
                            case "unusual_behavior":
                                factorScore = 20;
                                break;
                        }
                        riskBreakdown.put(factor, factorScore);
                        totalRiskScore += factorScore;
                    }
                    
                    // Behavior score analysis (inverse - lower is worse)
                    if (behaviorScore < 30) {
                        totalRiskScore += 30;
                        criticalFactors.add("VERY_LOW_BEHAVIOR_SCORE");
                        riskBreakdown.put("behavior_score", 30);
                    } else if (behaviorScore < 50) {
                        totalRiskScore += 20;
                        riskBreakdown.put("behavior_score", 20);
                    }
                    
                    // Geographic risk
                    if (geoLocation != null && "high".equals(geoLocation.get("risk"))) {
                        totalRiskScore += 25;
                        criticalFactors.add("HIGH_RISK_GEOGRAPHY");
                        riskBreakdown.put("geography", 25);
                    }
                    
                    // Account value impact (higher value = higher impact)
                    double valueRiskMultiplier = 1.0;
                    if (accountValue > 100000) {
                        valueRiskMultiplier = 1.5;
                    } else if (accountValue > 50000) {
                        valueRiskMultiplier = 1.3;
                    } else if (accountValue > 10000) {
                        valueRiskMultiplier = 1.1;
                    }
                    
                    // Verification level
                    String verificationLevel = (String) $req.getData().get("verificationLevel");
                    if ("basic".equals(verificationLevel) || verificationLevel == null) {
                        totalRiskScore += 15;
                        riskBreakdown.put("verification", 15);
                    }
                    
                    // Apply value multiplier
                    int adjustedRiskScore = (int) (totalRiskScore * valueRiskMultiplier);
                    
                    // Determine risk tier and actions
                    String riskTier;
                    List<String> requiredActions = new ArrayList<>();
                    
                    if (adjustedRiskScore >= 120) {
                        riskTier = "CRITICAL";
                        requiredActions.add("IMMEDIATE_ACCOUNT_FREEZE");
                        requiredActions.add("SENIOR_ANALYST_REVIEW");
                        requiredActions.add("LAW_ENFORCEMENT_NOTIFICATION");
                    } else if (adjustedRiskScore >= 80) {
                        riskTier = "HIGH";
                        requiredActions.add("ACCOUNT_RESTRICTION");
                        requiredActions.add("ANALYST_REVIEW_24H");
                        requiredActions.add("ENHANCED_MONITORING");
                    } else if (adjustedRiskScore >= 50) {
                        riskTier = "MEDIUM";
                        requiredActions.add("TRANSACTION_LIMITS");
                        requiredActions.add("WEEKLY_REVIEW");
                        requiredActions.add("ADDITIONAL_VERIFICATION");
                    } else {
                        riskTier = "LOW";
                        requiredActions.add("STANDARD_MONITORING");
                    }
                    
                    output.put("totalRiskScore", totalRiskScore);
                    output.put("adjustedRiskScore", adjustedRiskScore);
                    output.put("riskTier", riskTier);
                    output.put("criticalFactors", criticalFactors);
                    output.put("riskBreakdown", riskBreakdown);
                    output.put("requiredActions", requiredActions);
                    output.put("valueMultiplier", valueRiskMultiplier);
                    output.put("impactLevel", accountValue > 50000 ? "HIGH_IMPACT" : "STANDARD_IMPACT");
                    
                    System.out.println("[COMPLEX] Multi-factor risk: " + riskTier + 
                                     " (Base: " + totalRiskScore + ", Adjusted: " + adjustedRiskScore + ")");
            end
            
            // COMPLEX RULE 3: Pattern-based anomaly detection with accumulation
            rule "Complex_PatternAnomalyDetection"
                salience 80
                when
                    $req: GenericRequest(
                        data["recentTransactionCount"] != null,
                        data["linkedAccounts"] != null,
                        (Integer) data["recentTransactionCount"] > 10
                    )
                then
                    int txnCount = (Integer) $req.getData().get("recentTransactionCount");
                    int linkedAccounts = (Integer) $req.getData().get("linkedAccounts");
                    
                    List<String> anomalyPatterns = new ArrayList<>();
                    int anomalyScore = 0;
                    
                    // High transaction velocity
                    if (txnCount > 50) {
                        anomalyScore += 40;
                        anomalyPatterns.add("EXTREME_VELOCITY");
                    } else if (txnCount > 30) {
                        anomalyScore += 25;
                        anomalyPatterns.add("HIGH_VELOCITY");
                    } else if (txnCount > 15) {
                        anomalyScore += 15;
                        anomalyPatterns.add("ELEVATED_VELOCITY");
                    }
                    
                    // Multiple linked accounts (potential money laundering)
                    if (linkedAccounts > 10) {
                        anomalyScore += 35;
                        anomalyPatterns.add("EXCESSIVE_LINKED_ACCOUNTS");
                    } else if (linkedAccounts > 5) {
                        anomalyScore += 20;
                        anomalyPatterns.add("MULTIPLE_LINKED_ACCOUNTS");
                    }
                    
                    // Check for structuring pattern (amounts just below reporting threshold)
                    List<Map<String, Object>> recentTxns = (List<Map<String, Object>>) $req.getData().get("recentTransactions");
                    if (recentTxns != null) {
                        int nearThresholdCount = 0;
                        for (Map<String, Object> txn : recentTxns) {
                            double amount = (Double) txn.get("amount");
                            if (amount >= 9000 && amount < 10000) {
                                nearThresholdCount++;
                            }
                        }
                        if (nearThresholdCount >= 3) {
                            anomalyScore += 50;
                            anomalyPatterns.add("STRUCTURING_PATTERN_DETECTED");
                        }
                    }
                    
                    String anomalyLevel;
                    boolean requiresCompliance = false;
                    
                    if (anomalyScore >= 70) {
                        anomalyLevel = "SEVERE";
                        requiresCompliance = true;
                    } else if (anomalyScore >= 50) {
                        anomalyLevel = "HIGH";
                        requiresCompliance = true;
                    } else if (anomalyScore >= 30) {
                        anomalyLevel = "MODERATE";
                    } else {
                        anomalyLevel = "LOW";
                    }
                    
                    output.put("anomalyScore", anomalyScore);
                    output.put("anomalyLevel", anomalyLevel);
                    output.put("anomalyPatterns", anomalyPatterns);
                    output.put("requiresComplianceReview", requiresCompliance);
                    output.put("fileSAR", anomalyScore >= 70); // Suspicious Activity Report
                    
                    System.out.println("[COMPLEX] Anomaly detection: " + anomalyLevel + 
                                     " (Score: " + anomalyScore + ", Patterns: " + anomalyPatterns.size() + ")");
            end
            """;
    }

    /**
     * Create KieSession from DRL content
     */
    private KieSession createKieSession(String drlContent) {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        
        kfs.write("src/main/resources/rules.drl", drlContent);
        
        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
        kieBuilder.buildAll();
        
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
        }
        
        KieContainer kieContainer = kieServices.newKieContainer(
            kieServices.getRepository().getDefaultReleaseId()
        );
        
        return kieContainer.newKieSession();
    }

    /**
     * Generic Request class for flexible rule matching
     */
    public static class GenericRequest {
        private Map<String, Object> data;
        
        public GenericRequest(Map<String, Object> data) {
            this.data = data;
        }
        
        public Map<String, Object> getData() {
            return data;
        }
        
        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }
}
