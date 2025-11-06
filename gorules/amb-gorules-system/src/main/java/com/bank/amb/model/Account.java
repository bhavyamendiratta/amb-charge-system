package com.bank.amb.model;

import java.util.Arrays;

public class Account {
    private String accountId;
    private String accountName;
    private double[] dailyBalances; // 30 days
    private int currentMonth;

    public Account() {
        this.dailyBalances = new double[30];
    }

    public Account(String accountId, String accountName, double[] dailyBalances) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.dailyBalances = dailyBalances;
    }

    /**
     * Calculate Average Monthly Balance (AMB)
     * FIXED: Now correctly calculates average for the specified range
     *
     * For Day 1-25 check (on Day 26):
     *   - Sum days 1-25
     *   - Assume days 26-30 are zero
     *   - Divide total by 30
     *
     * For Day 1-30 check (on Day 3 of next month):
     *   - Sum all 30 days
     *   - Divide by 30
     */
    public double calculateAMB(int startDay, int endDay) {
        if (dailyBalances == null || dailyBalances.length < 30) {
            return 0.0;
        }

        double sum = 0;
        // Sum the days from startDay to endDay (inclusive)
        for (int i = startDay - 1; i < endDay && i < dailyBalances.length; i++) {
            sum += dailyBalances[i];
        }

        // For days beyond endDay up to day 30, assume balance is 0
        // This is the business rule for calculating AMB on day 26
        int remainingDays = 30 - endDay;

        // Always divide by 30 for monthly average
        return (sum + (remainingDays * 0)) / 30.0;
    }

    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public double[] getDailyBalances() {
        return dailyBalances;
    }

    public void setDailyBalances(double[] dailyBalances) {
        this.dailyBalances = dailyBalances;
    }

    public int getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(int currentMonth) {
        this.currentMonth = currentMonth;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId='" + accountId + '\'' +
                ", accountName='" + accountName + '\'' +
                ", currentMonth=" + currentMonth +
                ", dailyBalances=" + Arrays.toString(dailyBalances) +
                '}';
    }
}