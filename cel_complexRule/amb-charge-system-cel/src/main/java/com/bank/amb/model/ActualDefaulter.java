package com.bank.amb.model;

public class ActualDefaulter {
    private String accountId;
    private int month;
    private double amb;
    private double shortfall;
    private String status;

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public double getAmb() { return amb; }
    public void setAmb(double amb) { this.amb = amb; }
    public double getShortfall() { return shortfall; }
    public void setShortfall(double shortfall) { this.shortfall = shortfall; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}