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

    public double calculateAMB(int startDay, int endDay) {
        if (dailyBalances == null || dailyBalances.length < 30) {
            return 0.0;
        }

        double sum = 0;
        for (int i = startDay - 1; i < endDay && i < dailyBalances.length; i++) {
            sum += dailyBalances[i];
        }

        int remainingDays = 30 - endDay;
        return (sum + (remainingDays * 0)) / 30.0;
    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public double[] getDailyBalances() { return dailyBalances; }
    public void setDailyBalances(double[] dailyBalances) { this.dailyBalances = dailyBalances; }
    public int getCurrentMonth() { return currentMonth; }
    public void setCurrentMonth(int currentMonth) { this.currentMonth = currentMonth; }
}