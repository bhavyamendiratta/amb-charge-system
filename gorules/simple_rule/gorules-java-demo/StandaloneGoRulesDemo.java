package com.example.gorules;

import java.util.*;

/**
 * Standalone GoRules Demo - No External Dependencies
 */
public class StandaloneGoRulesDemo {

    public static ValidationResult evaluate(int age, double salary) {
        // Rule 1: Age >= 20 AND Salary >= 10000 → Welcome
        if (age >= 20 && salary >= 10000) {
            return new ValidationResult(
                    age, salary,
                    "Welcome! You meet all criteria.",
                    "APPROVED"
            );
        }

        // Rule 2: Age < 20 AND Salary >= 10000 → Age less
        if (age < 20 && salary >= 10000) {
            return new ValidationResult(
                    age, salary,
                    "Age is less than 20. Minimum age requirement not met.",
                    "AGE_TOO_LOW"
            );
        }

        // Rule 3: Age >= 20 AND Salary < 10000 → Income less
        if (age >= 20 && salary < 10000) {
            return new ValidationResult(
                    age, salary,
                    "Income is less than 10000. Minimum salary requirement not met.",
                    "SALARY_TOO_LOW"
            );
        }

        // Rule 4: Age < 20 AND Salary < 10000 → Both less
        if (age < 20 && salary < 10000) {
            return new ValidationResult(
                    age, salary,
                    "Both age and income are below requirements.",
                    "BOTH_TOO_LOW"
            );
        }

        return new ValidationResult(age, salary, "Unknown status", "UNKNOWN");
    }

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   GoRules Zen Engine Demo - Age & Salary Validation      ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("Business Rules:");
        System.out.println("1. Age >= 20 AND Salary >= 10000 → Welcome");
        System.out.println("2. Age < 20 AND Salary >= 10000 → Age less");
        System.out.println("3. Age >= 20 AND Salary < 10000 → Income less");
        System.out.println("4. Age < 20 AND Salary < 10000 → Both less");
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println();

        runTestCase(1, 25, 15000);
        runTestCase(2, 18, 12000);
        runTestCase(3, 30, 8000);
        runTestCase(4, 17, 5000);
        runTestCase(5, 20, 10000);
        runTestCase(6, 45, 50000);
        runTestCase(7, 19, 10000);
        runTestCase(8, 20, 9999);

        System.out.println("=".repeat(70));
        System.out.println("✅ Demo completed successfully!");
    }

    private static void runTestCase(int testNum, int age, double salary) {
        System.out.println("Test Case " + testNum + ": Age = " + age + ", Salary = " + salary);
        ValidationResult result = evaluate(age, salary);
        printResult(result);
        System.out.println();
    }

    private static void printResult(ValidationResult result) {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.printf("│ Age: %-4d | Salary: %-10.2f | Status: %-15s │%n",
                result.age, result.salary, result.status);
        System.out.println("├─────────────────────────────────────────────────────────┤");
        System.out.printf("│ %-55s │%n", wrapText(result.message, 55));
        System.out.println("└─────────────────────────────────────────────────────────┘");
    }

    private static String wrapText(String text, int width) {
        if (text.length() <= width) {
            return text + " ".repeat(width - text.length());
        }
        return text.substring(0, width - 3) + "...";
    }

    static class ValidationResult {
        final int age;
        final double salary;
        final String message;
        final String status;

        ValidationResult(int age, double salary, String message, String status) {
            this.age = age;
            this.salary = salary;
            this.message = message;
            this.status = status;
        }
    }
}