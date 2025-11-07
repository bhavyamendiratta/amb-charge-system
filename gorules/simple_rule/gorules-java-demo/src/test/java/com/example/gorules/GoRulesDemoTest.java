package com.example.gorules;

import com.example.gorules.GoRulesDemo.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoRulesDemoTest {

    private GoRulesDemo demo;

    @BeforeEach
    void setUp() {
        demo = new GoRulesDemo();
    }

    @Test
    @DisplayName("Test Case 1: Age >= 20 AND Salary >= 10000 → Welcome")
    void testBothCriteriaMet() {
        ValidationResult result = demo.evaluate(25, 15000);
        assertEquals("APPROVED", result.status);
        assertTrue(result.message.contains("Welcome"));
    }

    @Test
    @DisplayName("Test Case 2: Age < 20 AND Salary >= 10000 → Age less")
    void testAgeTooLow() {
        ValidationResult result = demo.evaluate(18, 12000);
        assertEquals("AGE_TOO_LOW", result.status);
        assertTrue(result.message.contains("Age is less"));
    }

    @Test
    @DisplayName("Test Case 3: Age >= 20 AND Salary < 10000 → Income less")
    void testSalaryTooLow() {
        ValidationResult result = demo.evaluate(30, 8000);
        assertEquals("SALARY_TOO_LOW", result.status);
        assertTrue(result.message.contains("Income is less"));
    }

    @Test
    @DisplayName("Test Case 4: Age < 20 AND Salary < 10000 → Both less")
    void testBothTooLow() {
        ValidationResult result = demo.evaluate(17, 5000);
        assertEquals("BOTH_TOO_LOW", result.status);
        assertTrue(result.message.contains("Both age and income"));
    }

    @Test
    @DisplayName("Edge Case: Exactly at threshold")
    void testExactlyAtThreshold() {
        ValidationResult result = demo.evaluate(20, 10000);
        assertEquals("APPROVED", result.status);
    }

    @Test
    @DisplayName("Edge Case: Just below age threshold")
    void testJustBelowAgeThreshold() {
        ValidationResult result = demo.evaluate(19, 10000);
        assertEquals("AGE_TOO_LOW", result.status);
    }

    @Test
    @DisplayName("Edge Case: Just below salary threshold")
    void testJustBelowSalaryThreshold() {
        ValidationResult result = demo.evaluate(20, 9999);
        assertEquals("SALARY_TOO_LOW", result.status);
    }

    @Test
    @DisplayName("High values test")
    void testHighValues() {
        ValidationResult result = demo.evaluate(50, 100000);
        assertEquals("APPROVED", result.status);
    }
}