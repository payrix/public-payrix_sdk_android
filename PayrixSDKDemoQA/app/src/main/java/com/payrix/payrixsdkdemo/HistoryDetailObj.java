package com.payrix.payrixsdkdemo;

import com.payrix.payrixsdk.paycore.payrixcore.TxnsPayment;

public class HistoryDetailObj {
    private int objectType; // Type 0 = Details Standard Segment; Type 1 = Details Sales Segment; Type 2 = Details Refund Segment; Type 3 = Details Miscellaneous Segment

    // Object Type 0: Details Standard Segment

    private String transactionID;
    private String transactionType;
    private String transactionStatus;
    private String transactionDate;
    private String authorization;

    // Object Type 1: Details Sales Segment

    private Double saleAmt;
    private Double taxAmt;
    private Double totalAmt;
    private String txnDescription;
    private String entryMode;
    private String cardNumber;
    private String expiration;
    private String descriptor;
    private String zip;
    private TxnsPayment payment;

    // Object Type 2: Details Refund Segment

    private Double refundAmt;
    private String refundDate;
    private String refundTxnID;

    // Object Type 3: Details Miscellaneous Segment

    private String miscLine1;
    private String miscLine2;


    public HistoryDetailObj (int objectType)
    {
        this.objectType = objectType;
    }


    // Getters

    public int getObjectType() { return objectType; }

    public Double getTotalAmt() { return totalAmt; }

    public Double getTaxAmt()
    {
        return taxAmt;
    }

    public Double getSaleAmt()
    {
        return saleAmt;
    }

    public String getTransactionType()
    {
        return transactionType;
    }

    public String getTransactionStatus()
    {
        return transactionStatus;
    }

    public String getTransactionID()
    {
        return transactionID;
    }

    public String getTransactionDate()
    {
        return transactionDate;
    }

    public String getTxnDescription() { return txnDescription; }

    public Double getRefundAmt() { return refundAmt; }

    public String getRefundDate() { return refundDate; }

    public String getRefundTxnID() { return refundTxnID; }

    public String getMiscLine1() { return miscLine1; }

    public String getMiscLine2() { return miscLine2; }

    public String getAuthorization() {
        return authorization;
    }

    public String getEntryMode() {
        return entryMode;
    }

    // Setters
    public void setObjectType(int objectType) { this.objectType = objectType; }

    public void setTotalAmt(Double totalAmt) { this.totalAmt = totalAmt; }

    public void setSaleAmt(Double saleAmt)
    {
        this.saleAmt = saleAmt;
    }

    public void setTaxAmt(Double taxAmt)
    {
        this.taxAmt = taxAmt;
    }

    public void setTransactionDate(String transactionDate)
    {
        this.transactionDate = transactionDate;
    }

    public void setTransactionID(String transactionID)
    {
        this.transactionID = transactionID;
    }

    public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }

    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public void setTxnDescription(String txnDescription) { this.txnDescription = txnDescription; }

    public void setRefundAmt(Double refundAmt) { this.refundAmt = refundAmt; }

    public void setRefundDate(String refundDate) { this.refundDate = refundDate; }

    public void setRefundTxnID(String refundTxnID) { this.refundTxnID = refundTxnID; }

    public void setMiscLine1(String miscLine1) { this.miscLine1 = miscLine1; }

    public void setMiscLine2(String miscLine2) { this.miscLine2 = miscLine2; }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public void setEntryMode(String entryMode) {
        this.entryMode = entryMode;
    }

    public TxnsPayment getPayment() {
        return payment;
    }

    public void setPayment(TxnsPayment payment) {
        this.payment = payment;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }


}
