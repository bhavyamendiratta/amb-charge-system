package com.bank.amb.model;

/**
 * Represents a probable defaulter (detected on Day 25)
 */
public class ProbableDefaulter {

    private String accountId;
    private int month;
    private double amb;
    private boolean smsSent;
    private String reason;

    public ProbableDefaulter() {}

    public ProbableDefaulter(String accountId, int month, double amb, boolean smsSent, String reason) {
        this.accountId = accountId;
        this.month = month;
        this.amb = amb;
        this.smsSent = smsSent;
        this.reason = reason;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return String.format("ProbableDefaulter{accountId='%s', month=%d, amb=%.2f, smsSent=%s}",
                accountId, month, amb, smsSent);
    }
}