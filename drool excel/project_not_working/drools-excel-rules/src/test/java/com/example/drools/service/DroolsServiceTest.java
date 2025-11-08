package com.example.drools.service;

import com.example.drools.model.Person;
import com.example.drools.model.ValidationResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DroolsServiceTest {

    private DroolsService droolsService;

    @Before
    public void setUp() {
        droolsService = new DroolsService("rules/AgeAndSalaryValidation.xlsx");
    }

    @After
    public void tearDown() {
        if (droolsService != null) {
            droolsService.dispose();
        }
    }

    @Test
    public void testApprovedCase() {
        // Age >= 20 AND Salary >= 10000
        Person person = new Person(25, 15000);
        ValidationResult result = droolsService.validate(person);

        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        assertEquals("Welcome! You meet all criteria.", result.getMessage());
    }

    @Test
    public void testAgeTooLow() {
        // Age < 20 AND Salary >= 10000
        Person person = new Person(18, 12000);
        ValidationResult result = droolsService.validate(person);

        assertNotNull(result);
        assertEquals("AGE_TOO_LOW", result.getStatus());
        assertTrue(result.getMessage().contains("Age is less than 20"));
    }

    @Test
    public void testSalaryTooLow() {
        // Age >= 20 AND Salary < 10000
        Person person = new Person(30, 8000);
        ValidationResult result = droolsService.validate(person);

        assertNotNull(result);
        assertEquals("SALARY_TOO_LOW", result.getStatus());
        assertTrue(result.getMessage().contains("Income is less than 10000"));
    }

    @Test
    public void testBothTooLow() {
        // Age < 20 AND Salary < 10000
        Person person = new Person(17, 5000);
        ValidationResult result = droolsService.validate(person);

        assertNotNull(result);
        assertEquals("BOTH_TOO_LOW", result.getStatus());
        assertTrue(result.getMessage().contains("Both age and income"));
    }

    @Test
    public void testBoundaryAge20() {
        // Test boundary: Age = 20 (should be approved if salary OK)
        Person person = new Person(20, 10000);
        ValidationResult result = droolsService.validate(person);

        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    public void testBoundaryAge19() {
        // Test boundary: Age = 19 (should be age too low)
        Person person = new Person(19, 10000);
        ValidationResult result = droolsService.validate(person);

        assertNotNull(result);
        assertEquals("AGE_TOO_LOW", result.getStatus());
    }

    @Test
    public void testBoundarySalary10000() {
        // Test boundary: Salary = 10000 (should be approved if age OK)
        Person person = new Person(25, 10000);
        ValidationResult result = droolsService.validate(person);

        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    public void testBoundarySalary9999() {
        // Test boundary: Salary = 9999 (should be salary too low)
        Person person = new Person(25, 9999);
        ValidationResult result = droolsService.validate(person);

        assertNotNull(result);
        assertEquals("SALARY_TOO_LOW", result.getStatus());
    }
}