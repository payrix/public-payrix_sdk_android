package com.payrix.payrixsdkdemo;

import android.widget.ImageView;

import com.payrix.payrixsdk.paycore.payrixcore.TxnsPayment;

import java.io.Serializable;

public class HistoryObj implements Serializable {
    private Double totalAmt;
    private String transactionDate;
    private String cardHolder;
    private ImageView imgAcceptDecline;
    private int cardType;
    private String cardNumber;
    private int transactionType;
    private int transactionStatus;
    private String transactionID;
    private Double taxAmt;
    private Double saleAmt;
    private Double approvedAmt;
    private Double refundedAmt;
    private String description;
    private String authorization;
    private String entryMode;
    private String expiration;
    private String descriptor;
    private String zip;
    private TxnsPayment payment;
    private int origin;
    private int swiped;
    private String currency;
    private int allowPartial;
    private int signature;
    private Integer unattended;



// Getters

    public Double getTotalAmt()
    {
        return totalAmt;
    }

    public Double getTaxAmt()
    {
        return taxAmt;
    }

    public Double getSaleAmt()
    {
        return saleAmt;
    }

    public String getCardHolder()
    {
        return cardHolder;
    }

    public int getCardType()
    {
        return cardType;
    }

    public String getCardNumber()
    {
        return cardNumber;
    }

    public int getTransactionType()
    {
        return transactionType;
    }

    public int getTransactionStatus()
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

    public String getDescription()
    {
        return description;
    }

    public ImageView getImgAcceptDecline()
    {
        return imgAcceptDecline;
    }

    public Double getApprovedAmt()
    {
        return approvedAmt;
    }

    public Double getRefundedAmt()
    {
        return refundedAmt;
    }

    // Setters

    public void setTotalAmt(Double totalAmt)
    {
        this.totalAmt = totalAmt;
    }

    public void setCardHolder(String cardHolder)
    {
        this.cardHolder = cardHolder;
    }

    public void setCardNumber(String cardNumber)
    {
        this.cardNumber = cardNumber;
    }

    public void setCardType(int cardType)
    {
        this.cardType = cardType;
    }

    public void setImgAcceptDecline(ImageView imgAcceptDecline)
    {
        this.imgAcceptDecline = imgAcceptDecline;
    }

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

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getEntryMode() {
        return entryMode;
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

    public void setTransactionID(String transactionID)
    {
        this.transactionID = transactionID;
    }

    public void setTransactionStatus(int transactionStatus)
    {
        this.transactionStatus = transactionStatus;
    }

    public void setTransactionType(int transactionType)
    {
        this.transactionType = transactionType;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setApprovedAmt(Double approvedAmt)
    {
        this.approvedAmt = approvedAmt;
    }

    public void setRefundedAmt(Double refundedAmt)
    {
        this.refundedAmt = refundedAmt;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
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

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public int getSwiped() {
        return swiped;
    }

    public void setSwiped(int swiped) {
        this.swiped = swiped;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getAllowPartial() {
        return allowPartial;
    }

    public void setAllowPartial(int allowPartial) {
        this.allowPartial = allowPartial;
    }

    public int getSignature() {
        return signature;
    }

    public void setSignature(int signature) {
        this.signature = signature;
    }

    public int getUnattended() {
        return unattended;
    }

    public void setUnattended(int unattended) {
        this.unattended = unattended;
    }
}

