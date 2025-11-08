package com.bank.amb.model;

public class ExecutionContext {
    private int currentMonth;
    private int checkDay;
    private double minBalance = 10000.0;

    public int getCurrentMonth() { return currentMonth; }
    public void setCurrentMonth(int currentMonth) { this.currentMonth = currentMonth; }
    public int getCheckDay() { return checkDay; }
    public void setCheckDay(int checkDay) { this.checkDay = checkDay; }
    public double getMinBalance() { return minBalance; }
    public void setMinBalance(double minBalance) { this.minBalance = minBalance; }
}