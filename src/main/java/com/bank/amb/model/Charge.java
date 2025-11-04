package com.bank.amb.model;

public class Charge {
    private String accountId;
    private int month1;
    private int month2;
    private double shortfall1;
    private double shortfall2;
    private double totalShortfall;
    private double baseCharge;
    private double gstAmount;
    private double totalCharge;
    private String reason;
    private int chargedInMonth;

    public Charge() {
    }

    public Charge(String accountId, int month1, int month2, double shortfall1, double shortfall2) {
        this.accountId = accountId;
        this.month1 = month1;
        this.month2 = month2;
        this.shortfall1 = shortfall1;
        this.shortfall2 = shortfall2;
        this.totalShortfall = shortfall1 + shortfall2;
    }

    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getMonth1() {
        return month1;
    }

    public void setMonth1(int month1) {
        this.month1 = month1;
    }

    public int getMonth2() {
        return month2;
    }

    public void setMonth2(int month2) {
        this.month2 = month2;
    }

    public double getShortfall1() {
        return shortfall1;
    }

    public void setShortfall1(double shortfall1) {
        this.shortfall1 = shortfall1;
    }

    public double getShortfall2() {
        return shortfall2;
    }

    public void setShortfall2(double shortfall2) {
        this.shortfall2 = shortfall2;
    }

    public double getTotalShortfall() {
        return totalShortfall;
    }

    public void setTotalShortfall(double totalShortfall) {
        this.totalShortfall = totalShortfall;
    }

    public double getBaseCharge() {
        return baseCharge;
    }

    public void setBaseCharge(double baseCharge) {
        this.baseCharge = baseCharge;
    }

    public double getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(double gstAmount) {
        this.gstAmount = gstAmount;
    }

    public double getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(double totalCharge) {
        this.totalCharge = totalCharge;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getChargedInMonth() {
        return chargedInMonth;
    }

    public void setChargedInMonth(int chargedInMonth) {
        this.chargedInMonth = chargedInMonth;
    }

    @Override
    public String toString() {
        return "Charge{" +
                "accountId='" + accountId + '\'' +
                ", month1=" + month1 +
                ", month2=" + month2 +
                ", shortfall1=" + shortfall1 +
                ", shortfall2=" + shortfall2 +
                ", totalShortfall=" + totalShortfall +
                ", baseCharge=" + baseCharge +
                ", gstAmount=" + gstAmount +
                ", totalCharge=" + totalCharge +
                ", reason='" + reason + '\'' +
                ", chargedInMonth=" + chargedInMonth +
                '}';
    }
}