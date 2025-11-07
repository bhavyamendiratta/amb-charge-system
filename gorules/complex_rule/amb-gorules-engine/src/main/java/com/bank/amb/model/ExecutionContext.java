package com.bank.amb.model;

/**
 * Execution context for rule evaluation
 */
public class ExecutionContext {

    private int checkDay;        // Day of month when rules are checked (25 or 3)
    private int currentMonth;    // Current month number
    private double minBalance;   // Minimum required balance

    public ExecutionContext() {}

    public ExecutionContext(int checkDay, int currentMonth, double minBalance) {
        this.checkDay = checkDay;
        this.currentMonth = currentMonth;
        this.minBalance = minBalance;
    }

    // Getters and Setters
    public int getCheckDay() { return checkDay; }
    public void setCheckDay(int checkDay) { this.checkDay = checkDay; }

    public int getCurrentMonth() { return currentMonth; }
    public void setCurrentMonth(int currentMonth) { this.currentMonth = currentMonth; }

    public double getMinBalance() { return minBalance; }
    public void setMinBalance(double minBalance) { this.minBalance = minBalance; }

    @Override
    public String toString() {
        return String.format("ExecutionContext{checkDay=%d, month=%d, minBalance=%.2f}",
                checkDay, currentMonth, minBalance);
    }
}