package com.bank.amb.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a bank account with daily balances
 */
public class Account {

    private String accountId;
    private String accountName;
    private int currentMonth;
    private Map<Integer, Double> dailyBalances; // Day -> Balance

    public Account() {
        this.dailyBalances = new HashMap<>();
    }

    public Account(String accountId, String accountName) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.dailyBalances = new HashMap<>();
    }

    /**
     * Set balance for a specific day
     */
    public void setDailyBalance(int day, double balance) {
        this.dailyBalances.put(day, balance);
    }

    /**
     * Calculate Average Monthly Balance (AMB) for a date range
     * @param startDay Starting day (inclusive)
     * @param endDay Ending day (inclusive)
     * @return Average balance for the period
     */
    public double calculateAMB(int startDay, int endDay) {
        double sum = 0.0;
        int count = 0;

        for (int day = startDay; day <= endDay; day++) {
            Double balance = dailyBalances.get(day);
            if (balance != null) {
                sum += balance;
                count++;
            }
        }

        return count > 0 ? sum / count : 0.0;
    }

    // Getters and Setters
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public int getCurrentMonth() { return currentMonth; }
    public void setCurrentMonth(int currentMonth) { this.currentMonth = currentMonth; }

    public Map<Integer, Double> getDailyBalances() { return dailyBalances; }
    public void setDailyBalances(Map<Integer, Double> dailyBalances) { this.dailyBalances = dailyBalances; }

    @Override
    public String toString() {
        return String.format("Account{id='%s', name='%s', month=%d, days=%d}",
                accountId, accountName, currentMonth, dailyBalances.size());
    }
}