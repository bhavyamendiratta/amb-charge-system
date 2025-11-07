package com.example.gorules;

import io.gorules.zen_engine.ZenEngine;
import io.gorules.zen_engine.JsonBuffer;
import io.gorules.zen_engine.ZenEvaluateOptions;
import io.gorules.zen_engine.ZenDecisionLoaderCallback;
import io.gorules.zen_engine.ZenCustomNodeCallback;
import io.gorules.zen_engine.ZenEngineResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * GoRules Demo using REAL Zen Engine 0.4.0
 */
public class GoRulesDemo {

    private static final Logger log = LoggerFactory.getLogger(GoRulesDemo.class);

    private final ZenEngine engine;
    private final String decisionKey;
    private final ObjectMapper objectMapper;

    public GoRulesDemo() {
        this.objectMapper = new ObjectMapper();

        // Initialize ZenEngine with callbacks for custom node handling and decision loading
        ZenDecisionLoaderCallback loaderCallback = (key) -> {
            log.debug("Loading decision: {}", key);
            String content = loadDecisionModel("src/main/resources/rules/age-salary-rules.json");
            return CompletableFuture.completedFuture(new JsonBuffer(content));
        };

        ZenCustomNodeCallback customNodeCallback = null; // Not using custom nodes

        this.engine = new ZenEngine(loaderCallback, customNodeCallback);
        this.decisionKey = "age-salary-rules"; // Decision key identifier

        log.info("GoRules Demo initialized with REAL Zen Engine 0.4.0");
    }

    @SuppressWarnings("unchecked")
    public ValidationResult evaluate(int age, double salary) {
        log.info("Evaluating: age={}, salary={}", age, salary);

        try {
            // Prepare input as JSON string
            Map<String, Object> input = new HashMap<>();
            input.put("age", age);
            input.put("salary", salary);

            String inputJson = objectMapper.writeValueAsString(input);
            JsonBuffer inputBuffer = new JsonBuffer(inputJson);

            // Create evaluation options (can be null for defaults)
            ZenEvaluateOptions options = null;

            // Evaluate using real Zen Engine with decision key (returns CompletableFuture)
            CompletableFuture<ZenEngineResponse> futureResponse = engine.evaluate(decisionKey, inputBuffer, options);

            // Wait for the result
            ZenEngineResponse response = futureResponse.join();

            // Get the result JSON string directly from response
            String resultJson = response.result().toString();
            Map<String, Object> result = objectMapper.readValue(resultJson, Map.class);

            String message = (String) result.get("message");
            String status = (String) result.get("status");

            ValidationResult validationResult = new ValidationResult(age, salary, message, status);
            log.info("Result: {}", validationResult);

            return validationResult;

        } catch (Exception e) {
            log.error("Evaluation failed", e);
            throw new RuntimeException("Evaluation failed: " + e.getMessage(), e);
        }
    }

    private String loadDecisionModel(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            log.debug("Loaded decision model from: {}", filePath);
            return content;
        } catch (IOException e) {
            log.error("Failed to load decision model: {}", filePath, e);
            throw new RuntimeException("Cannot load decision model", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   GoRules Zen Engine Demo - Age & Salary Validation      ║");
        System.out.println("║           Using REAL Zen Engine v0.4.0                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();

        GoRulesDemo demo = new GoRulesDemo();

        System.out.println("Business Rules:");
        System.out.println("1. Age >= 20 AND Salary >= 10000 → Welcome");
        System.out.println("2. Age < 20 AND Salary >= 10000 → Age less");
        System.out.println("3. Age >= 20 AND Salary < 10000 → Income less");
        System.out.println("4. Age < 20 AND Salary < 10000 → Both less");
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println();

        printTestCase("Test 1: Both criteria met", demo.evaluate(25, 15000));
        printTestCase("Test 2: Age too low", demo.evaluate(18, 12000));
        printTestCase("Test 3: Salary too low", demo.evaluate(30, 8000));
        printTestCase("Test 4: Both too low", demo.evaluate(17, 5000));
        printTestCase("Test 5: Edge case - exactly at threshold", demo.evaluate(20, 10000));
        printTestCase("Test 6: High values", demo.evaluate(45, 50000));
        printTestCase("Test 7: Just below age threshold", demo.evaluate(19, 10000));
        printTestCase("Test 8: Just below salary threshold", demo.evaluate(20, 9999));

        System.out.println("=".repeat(70));
        System.out.println("✅ Demo completed successfully with REAL Zen Engine!");
    }

    private static void printTestCase(String description, ValidationResult result) {
        System.out.println(description);
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.printf("│ Age: %-4d | Salary: %-10.2f | Status: %-15s │%n",
                result.age, result.salary, result.status);
        System.out.println("├─────────────────────────────────────────────────────────┤");
        System.out.printf("│ %-55s │%n", wrapText(result.message, 55));
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private static String wrapText(String text, int width) {
        if (text == null) return " ".repeat(width);
        if (text.length() <= width) {
            return text + " ".repeat(width - text.length());
        }
        return text.substring(0, width - 3) + "...";
    }

    public static class ValidationResult {
        public final int age;
        public final double salary;
        public final String message;
        public final String status;

        public ValidationResult(int age, double salary, String message, String status) {
            this.age = age;
            this.salary = salary;
            this.message = message;
            this.status = status;
        }

        @Override
        public String toString() {
            return String.format("ValidationResult{age=%d, salary=%.2f, status=%s, message='%s'}",
                    age, salary, status, message);
        }
    }
}