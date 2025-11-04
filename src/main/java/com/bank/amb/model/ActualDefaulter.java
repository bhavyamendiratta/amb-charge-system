package com.bank.amb.model;

public class ActualDefaulter {
    private String accountId;
    private int month;
    private double amb;
    private double shortfall;
    private String status;

    public ActualDefaulter() {
    }

    public ActualDefaulter(String accountId, int month, double amb, double shortfall, String status) {
        this.accountId = accountId;
        this.month = month;
        this.amb = amb;
        this.shortfall = shortfall;
        this.status = status;
    }

    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public double getAmb() {
        return amb;
    }

    public void setAmb(double amb) {
        this.amb = amb;
    }

    public double getShortfall() {
        return shortfall;
    }

    public void setShortfall(double shortfall) {
        this.shortfall = shortfall;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ActualDefaulter{" +
                "accountId='" + accountId + '\'' +
                ", month=" + month +
                ", amb=" + amb +
                ", shortfall=" + shortfall +
                ", status='" + status + '\'' +
                '}';
    }
}