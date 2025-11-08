package com.example.drools.model;

/**
 * Output model class to hold validation results from Drools rules.
 * This object is inserted into the KieSession and populated by rule actions.
 */
public class ValidationResult {
    private String message;
    private String status;

    /**
     * Default constructor required by Drools
     */
    public ValidationResult() {
    }

    /**
     * Constructor with parameters
     * @param message Validation message
     * @param status Validation status code
     */
    public ValidationResult(String message, String status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "message='" + message + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}