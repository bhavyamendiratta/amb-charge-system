package com.example.drools;

import com.example.drools.model.Person;
import com.example.drools.model.ValidationResult;
import com.example.drools.service.DroolsService;

public class DroolsApplication {

    public static void main(String[] args) {
        // Initialize Drools with Excel file
        DroolsService droolsService = new DroolsService("rules/AgeAndSalaryValidation.xls");

        // Test Case 1: Age >= 20 AND Salary >= 10000 → Welcome
        System.out.println("=== Test Case 1: Age=25, Salary=15000 ===");
        Person person1 = new Person(25, 15000);
        ValidationResult result1 = droolsService.validate(person1);
        System.out.println("Input: " + person1);
        System.out.println("Output: " + result1);
        System.out.println();

        // Test Case 2: Age < 20 AND Salary >= 10000 → Age less
        System.out.println("=== Test Case 2: Age=18, Salary=12000 ===");
        Person person2 = new Person(18, 12000);
        ValidationResult result2 = droolsService.validate(person2);
        System.out.println("Input: " + person2);
        System.out.println("Output: " + result2);
        System.out.println();

        // Test Case 3: Age >= 20 AND Salary < 10000 → Income less
        System.out.println("=== Test Case 3: Age=30, Salary=8000 ===");
        Person person3 = new Person(30, 8000);
        ValidationResult result3 = droolsService.validate(person3);
        System.out.println("Input: " + person3);
        System.out.println("Output: " + result3);
        System.out.println();

        // Test Case 4: Age < 20 AND Salary < 10000 → Both less
        System.out.println("=== Test Case 4: Age=17, Salary=5000 ===");
        Person person4 = new Person(17, 5000);
        ValidationResult result4 = droolsService.validate(person4);
        System.out.println("Input: " + person4);
        System.out.println("Output: " + result4);
        System.out.println();

        // Cleanup
        droolsService.dispose();
    }
}