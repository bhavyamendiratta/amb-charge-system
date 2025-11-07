package com.bank.amb;

import com.bank.amb.engine.AMBRulesEngine;
import com.bank.amb.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for AMB Rules Engine
 */
class AMBRulesEngineTest {

    private AMBRulesEngine engine;
    private static final double MIN_BALANCE = 10000.0;

    @BeforeEach
    void setUp() {
        engine = new AMBRulesEngine();
    }

    @Test
    @DisplayName("Test Rule 1A: Probable Defaulter - Was Actual Last Month (No SMS)")
    void testProbableDefaulterNoSMS() {
        // Arrange
        Account account = createAccount("ACC001", "Test User", 8000.0);
        ExecutionContext context = new ExecutionContext(25, 2, MIN_BALANCE);

        List<ActualDefaulter> existingActualDefaulters = new ArrayList<>();
        existingActualDefaulters.add(new ActualDefaulter("ACC001", 1, 8000.0, 2000.0, "Defaulter"));

        // Act
        AMBRulesEngine.RuleExecutionResult result = engine.executeRules(
                List.of(account),
                new ArrayList<>(),
                existingActualDefaulters,
                new ArrayList<>(),
                context
        );

        // Assert
        assertEquals(1, result.getProbableDefaulters().size());
        ProbableDefaulter pd = result.getProbableDefaulters().get(0);
        assertFalse(pd.isSmsSent(), "SMS should NOT be sent for continuing defaulters");
        assertEquals("ACC001", pd.getAccountId());
    }

    @Test
    @DisplayName("Test Rule 1B: Probable Defaulter - New (Send SMS)")
    void testProbableDefaulterSendSMS() {
        // Arrange
        Account account = createAccount("ACC002", "New Defaulter", 8000.0);
        ExecutionContext context = new ExecutionContext(25, 2, MIN_BALANCE);

        // Act
        AMBRulesEngine.RuleExecutionResult result = engine.executeRules(
                List.of(account),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                context
        );

        // Assert
        assertEquals(1, result.getProbableDefaulters().size());
        ProbableDefaulter pd = result.getProbableDefaulters().get(0);
        assertTrue(pd.isSmsSent(), "SMS SHOULD be sent for new defaulters");
        assertEquals("ACC002", pd.getAccountId());
    }

    @Test
    @DisplayName("Test Rule 2: Actual Defaulter Confirmation")
    void testActualDefaulterConfirmation() {
        // Arrange
        Account account = createAccount("ACC003", "Confirmed Defaulter", 8000.0);
        ExecutionContext context = new ExecutionContext(3, 2, MIN_BALANCE);

        List<ProbableDefaulter> existingProbable = new ArrayList<>();
        existingProbable.add(new ProbableDefaulter("ACC003", 1, 8000.0, true, "Probable"));

        // Act
        AMBRulesEngine.RuleExecutionResult result = engine.executeRules(
                List.of(account),
                existingProbable,
                new ArrayList<>(),
                new ArrayList<>(),
                context
        );

        // Assert
        assertEquals(1, result.getActualDefaulters().size());
        ActualDefaulter ad = result.getActualDefaulters().get(0);
        assertEquals("ACC003", ad.getAccountId());
        assertEquals(1, ad.getMonth());
        assertTrue(ad.getShortfall() > 0);
    }

    @Test
    @DisplayName("Test Rule 3: Charge Calculation for Consecutive Defaults")
    void testChargeCalculation() {
        // Arrange
        Account account = createAccount("ACC004", "Consecutive Defaulter", 7000.0);
        ExecutionContext context = new ExecutionContext(3, 3, MIN_BALANCE);

        List<ActualDefaulter> existingActual = new ArrayList<>();
        existingActual.add(new ActualDefaulter("ACC004", 1, 8000.0, 2000.0, "Defaulter"));
        existingActual.add(new ActualDefaulter("ACC004", 2, 7500.0, 2500.0, "Defaulter"));

        // Act
        AMBRulesEngine.RuleExecutionResult result = engine.executeRules(
                List.of(account),
                new ArrayList<>(),
                existingActual,
                new ArrayList<>(),
                context
        );

        // Assert
        assertEquals(1, result.getCharges().size());
        Charge charge = result.getCharges().get(0);
        assertEquals("ACC004", charge.getAccountId());
        assertTrue(charge.getTotalCharge() > 0);

        // Verify charge calculation: (2000 * 0.06 + 2500 * 0.06) * 1.18 = (120 + 150) * 1.18 = 318.60
        double expectedBase = (2000.0 * 0.06) + (2500.0 * 0.06); // 270
        double expectedGST = expectedBase * 0.18; // 48.60
        double expectedTotal = expectedBase + expectedGST; // 318.60

        assertEquals(expectedBase, charge.getBaseCharge(), 0.01);
        assertEquals(expectedGST, charge.getGstAmount(), 0.01);
        assertEquals(expectedTotal, charge.getTotalCharge(), 0.01);
    }

    @Test
    @DisplayName("Test Good Customer - No Rules Triggered")
    void testGoodCustomer() {
        // Arrange
        Account account = createAccount("ACC005", "Good Customer", 15000.0);
        ExecutionContext context = new ExecutionContext(25, 1, MIN_BALANCE);

        // Act
        AMBRulesEngine.RuleExecutionResult result = engine.executeRules(
                List.of(account),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                context
        );

        // Assert
        assertEquals(0, result.getProbableDefaulters().size());
        assertEquals(0, result.getActualDefaulters().size());
        assertEquals(0, result.getCharges().size());
    }

    @Test
    @DisplayName("Test Charge Cap - Maximum ₹500 per month")
    void testChargeCap() {
        // Arrange
        Account account = createAccount("ACC006", "High Deficit", 1000.0);
        ExecutionContext context = new ExecutionContext(3, 3, MIN_BALANCE);

        // Create huge shortfalls (should be capped at 500 per month)
        List<ActualDefaulter> existingActual = new ArrayList<>();
        existingActual.add(new ActualDefaulter("ACC006", 1, 1000.0, 9000.0, "Defaulter")); // 9000 * 0.06 = 540 -> capped to 500
        existingActual.add(new ActualDefaulter("ACC006", 2, 1000.0, 9000.0, "Defaulter")); // 9000 * 0.06 = 540 -> capped to 500

        // Act
        AMBRulesEngine.RuleExecutionResult result = engine.executeRules(
                List.of(account),
                new ArrayList<>(),
                existingActual,
                new ArrayList<>(),
                context
        );

        // Assert
        assertEquals(1, result.getCharges().size());
        Charge charge = result.getCharges().get(0);

        // Each month capped at 500, so: (500 + 500) * 1.18 = 1180
        double expectedBase = 1000.0; // 500 + 500 (capped)
        double expectedGST = expectedBase * 0.18; // 180
        double expectedTotal = expectedBase + expectedGST; // 1180

        assertEquals(expectedBase, charge.getBaseCharge(), 0.01);
        assertEquals(expectedTotal, charge.getTotalCharge(), 0.01);
    }

    // Helper method
    private Account createAccount(String id, String name, double balance) {
        Account account = new Account(id, name);
        for (int day = 1; day <= 30; day++) {
            account.setDailyBalance(day, balance);
        }
        return account;
    }
}