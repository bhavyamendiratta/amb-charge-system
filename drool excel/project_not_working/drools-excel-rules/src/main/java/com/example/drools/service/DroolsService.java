package com.example.drools.service;

import com.example.drools.model.Person;
import com.example.drools.model.ValidationResult;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class DroolsService {
    private static final Logger logger = LoggerFactory.getLogger(DroolsService.class);
    private KieContainer kieContainer;

    public DroolsService(String excelFilePath) {
        initializeKieContainer(excelFilePath);
    }

    private void initializeKieContainer(String excelFilePath) {
        try {
            KieServices kieServices = KieServices.Factory.get();

            // Load Excel file from classpath
            InputStream excelStream = getClass().getClassLoader().getResourceAsStream(excelFilePath);
            if (excelStream == null) {
                throw new IllegalArgumentException("Excel file not found: " + excelFilePath);
            }

            // Convert Excel to DRL
            // Note: InputType.XLS works for both .xls and .xlsx files
            SpreadsheetCompiler compiler = new SpreadsheetCompiler();
            String drl = compiler.compile(excelStream, InputType.XLS);

            logger.info("Generated DRL from Excel:");
            logger.info(drl);

            // Create KieFileSystem and add the generated DRL
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            kieFileSystem.write("src/main/resources/rules/ValidationRules.drl", drl);

            // Build the KieContainer
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
            }

            kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
            logger.info("Drools KieContainer initialized successfully");

        } catch (Exception e) {
            logger.error("Error initializing Drools", e);
            throw new RuntimeException("Failed to initialize Drools", e);
        }
    }

    public ValidationResult validate(Person person) {
        ValidationResult result = new ValidationResult();

        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.insert(person);
            kieSession.insert(result);
            kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }

        return result;
    }

    public void dispose() {
        if (kieContainer != null) {
            kieContainer.dispose();
        }
    }
}