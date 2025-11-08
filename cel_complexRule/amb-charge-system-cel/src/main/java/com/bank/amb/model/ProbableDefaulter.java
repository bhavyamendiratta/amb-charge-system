package com.bank.amb.model;

public class ProbableDefaulter {
    private String accountId;
    private int month;
    private double amb;
    private boolean smsSent;
    private String reason;

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public double getAmb() { return amb; }
    public void setAmb(double amb) { this.amb = amb; }
    public boolean isSmsSent() { return smsSent; }
    public void setSmsSent(boolean smsSent) { this.smsSent = smsSent; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}