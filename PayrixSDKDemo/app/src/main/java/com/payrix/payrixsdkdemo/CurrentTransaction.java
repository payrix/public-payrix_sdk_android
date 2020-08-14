package com.payrix.payrixsdkdemo;
import com.payrix.paycoreapilibrary.paycoremobile.PayCoreGlobals;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class CurrentTransaction implements Serializable
{
    public static CurrentTransaction sharedInstance = null;
    private CurrentTransaction(){}

    public static synchronized CurrentTransaction getInstance()
    {
        if (sharedInstance == null)
        {
            sharedInstance = new CurrentTransaction();
        }
        return sharedInstance;
    }

    public String merchantID;
    public String merchantDBA;
    public String transactionID;

    public Double amount;
    public String ccNumber;
    public PayCoreGlobals.PayCoreCCType ccCardType;
    public String ccName;
    public String ccEXP;
    public String ccCVV;
    public String zip;

    public Integer tipPercentage;
    public Double tipAbsoluteAmount;
    public Double taxPercentage;
    public ArrayList<String> errorMessages;

    public String receiptAID_4F;
    public String receiptEMVChipInd;  // Swipe | Manual Entry | Chip
    public String receiptAIDName_9F12;
    public String receiptPINStmt;
    public String receiptTVRCVR_95;
    public String receiptApprovedDeclined;
    public String receiptAuthApprovalCode;
    public String receiptTSI_9B;
    public String receiptCryptoCert_9F26;

    public void init(String merchantID)
    {
        this.merchantID = merchantID;
    }

    // An init that copies only the fields we'd like to retain between transactions
    public void init(CurrentTransaction txn)
    {
        this.merchantID = txn.merchantID;
        this.merchantDBA = txn.merchantDBA;
        this.taxPercentage = txn.taxPercentage;
    }

    public void init()
    {
        this.merchantID = null;
        this.merchantDBA = null;
        this.taxPercentage = null;
        this.transactionID = null;
        this.amount = null;
        this.ccNumber = null;

        this.ccName = null;
        this.ccEXP = null;
        this.ccCVV = null;
        this.zip = null;

        this.tipPercentage = null;
        this.tipAbsoluteAmount = null;
    }

    public void setMerchantDBA(String useDBA)
    {
        this.merchantDBA = useDBA;
    }
}
