package com.payrix.payrixsdkdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.payrix.paycoreapilibrary.paycoremobile.PayCoreGlobals;
import com.payrix.paycoreapilibrary.payrixcore.Txns;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

public class SharedUtilities
{
    public static SharedUtilities sharedInstance = null;
    private SharedUtilities(){}

    // Active Merchant Information and Last used Tax Rate
    static String merchantIDKey = "merchantID";
    static String merchantDBAKey = "merchantDBA";
    static String lastUsedTaxRateKey = "lastUsedTaxRate";

    // Sign In Keys
    static String userNameDefaultsKey = "UserName";
    static String sessionKeyDefaultsKey = "SessionKey";

    // Default URLs
    public static String pwlAPIHostName = "api.payrix.com";
    public static String pwlForgotPasswordURL = "https://portal.payrix.com";

    // The Demo Mode Key
    static String demoModeKey = "com.Payrix.DemoModeKey";

    // BT Device Key
    static String btDeviceKey = "CurrentBTReader";

    public static synchronized SharedUtilities getInstance()
    {
        if (sharedInstance == null)
        {
            sharedInstance = new SharedUtilities();
        }
        return sharedInstance;
    }


    public Double calcTax(Double forAmount, Double withTaxRate)
    {
        Double rtnTaxAmt;

        if ((withTaxRate == 0) || (withTaxRate == null))
        {
            rtnTaxAmt = 0.0;
        }
        else
        {
            rtnTaxAmt = forAmount * withTaxRate;
        }

        return  rtnTaxAmt;
    }


//    public Double calcTip(CurrentTransaction currTxn)
//    {
//        Double rtnTipAmt;
//        Double orTipAmt = currTxn.tipAbsoluteAmount;
//        Integer withTipRate = currTxn.tipPercentage;
//        Double forAmount = currTxn.amount;
//
//        if ((orTipAmt == 0) || (orTipAmt == null))
//        {
//            // Amount is Zero so Calc Tip
//
//            if ((withTipRate == 0) || (withTipRate == null))
//            {
//                rtnTipAmt = 0.0;
//            }
//            else
//            {
//                rtnTipAmt = forAmount * (withTipRate.doubleValue() / 100);
//            }
//        }
//        else
//        {
//            rtnTipAmt = orTipAmt;
//        }
//
//        return  rtnTipAmt;
//    }


//    public Double calcTotalWithTipTax(CurrentTransaction currTxn)
//    {
//        Double rtnTotal;
//
//        Double useTax = calcTax(currTxn.amount, currTxn.taxPercentage);
//        Double useTip = calcTip(currTxn);
//        rtnTotal = currTxn.amount + useTax + useTip;
//
//        return rtnTotal;
//    }


    public void setSessionKey(Context context, String withKey)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences.Editor sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE).edit();

        sharedAppInfo.putString(sessionKeyDefaultsKey, withKey);
        sharedAppInfo.apply();
    }

    public String getSessionKey(Context context)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = context.getSharedPreferences(usePref, MODE_PRIVATE);
        String useKey = sharedAppInfo.getString(sessionKeyDefaultsKey, null);
        return useKey;
    }


    public Boolean getDemoMode(Context context)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = context.getSharedPreferences(usePref, MODE_PRIVATE);
        Boolean useMode = sharedAppInfo.getBoolean(demoModeKey, false);
        return useMode;
    }


    public void setDemoMode(Context context, Boolean withMode)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences.Editor sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE).edit();

        sharedAppInfo.putBoolean(demoModeKey, withMode);
        sharedAppInfo.apply();
    }



    public void setMerchantID(Context context, String withID)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences.Editor sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE).edit();

        sharedAppInfo.putString(merchantIDKey, withID);
        sharedAppInfo.apply();
    }

    public String getMerchantID(Context context)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = context.getSharedPreferences(usePref, MODE_PRIVATE);
        String useKey = sharedAppInfo.getString(merchantIDKey, null);
        return useKey;
    }

    public void setMerchantDBA(Context context, String withDBA)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences.Editor sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE).edit();

        sharedAppInfo.putString(merchantDBAKey, withDBA);
        sharedAppInfo.apply();
    }

    public String getMerchantDBA(Context context)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = context.getSharedPreferences(usePref, MODE_PRIVATE);
        String useKey = sharedAppInfo.getString(merchantDBAKey, null);
        return useKey;
    }


    public void setBTReader(Context context, String withKey)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences.Editor sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE).edit();
        sharedAppInfo.putString(btDeviceKey, withKey);
        sharedAppInfo.apply();
    }

    public String getBTReader(Context context)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = context.getSharedPreferences(usePref, MODE_PRIVATE);
        String useKey = sharedAppInfo.getString(btDeviceKey, null);
        return useKey;
    }


    public String getURL(Context context, String uri)
    {
        PayCoreGlobals payCoreGlobals = PayCoreGlobals.getInstance();
        payCoreGlobals.demoMode = true;
        payCoreGlobals.PWLWhiteLabelAPIHostName = pwlAPIHostName;

        String useHost;
        if (uri == null)
        {
            uri = "";
        }

        useHost = "https://test-" + pwlAPIHostName + uri;

        return useHost;
    }


    public PayCoreGlobals.PayCoreCCType bldCCType(String cardType)
    {
        switch (cardType.toUpperCase())
        {
            case "AMEX":
                return PayCoreGlobals.PayCoreCCType.AmericanExpress;
            case "DISCOVER":
                return PayCoreGlobals.PayCoreCCType.Discover;
            case "MASTERCARD":
                return PayCoreGlobals.PayCoreCCType.MasterCard;
            case "VISA":
                return PayCoreGlobals.PayCoreCCType.Visa;
            case "DINERSCLUB":
                return PayCoreGlobals.PayCoreCCType.DinersClub;
            default:
                break;
        }
        return null;
    }

    public String convertAttrbToString(AttributedString inAttrbStr)
    {
        AttributedCharacterIterator x = inAttrbStr.getIterator();
        String finalString = "";

        finalString += x.current();
        while (x.getIndex() < x.getEndIndex())
            finalString += x.next();
        finalString = finalString.substring(0,finalString.length()-1);
        return finalString;
    }



    public void doSaveSharedInfo(Context context, String prefKey, String prefVal, String prefType)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences.Editor sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE).edit();

        if (prefType.toUpperCase() == "STRING")
        {
            sharedAppInfo.putString(prefKey, prefVal);
        }
        else if (prefType.toUpperCase() == "INTEGER")
        {
            int useInt = Integer.parseInt(prefVal);
            sharedAppInfo.putInt(prefKey, useInt);
        }
        else if (prefType.toUpperCase() == "BOOLEAN")
        {
            Boolean useBool = false;
            if (prefVal.toLowerCase() == "true")
            {
                useBool = true;
            }
            else
            {
                useBool = false;
            }
            sharedAppInfo.putBoolean(prefKey, useBool);
        }

        sharedAppInfo.apply();
    }

    public String doGetSharedInfo(Context context, String prefKey, String prefType)
    {
        String usePref = context.getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE);

        String returnedInfo = "";

        if (prefType.toUpperCase() == "STRING")
        {
            returnedInfo = sharedAppInfo.getString(prefKey,"");
        }
        else if (prefType.toUpperCase() == "INTEGER")
        {
            int useInt = sharedAppInfo.getInt(prefKey,0);
            returnedInfo = Integer.toString(useInt);
        }
        else if (prefType.toUpperCase() == "BOOLEAN")
        {
            Boolean useBool = false;
            useBool = sharedAppInfo.getBoolean(prefKey, false);

            if (useBool == true)
            {
                returnedInfo = "true";
            }
            else
            {
                returnedInfo = "false";
            }
        }

        return returnedInfo;
    }

//    public int getCardResource(PayCoreGlobals.PayCoreCCType cardType)
//    {
//        if (cardType == PayCoreGlobals.PayCoreCCType.AmericanExpress)
//        {
//            return R.drawable.amex;
//        }
//        else if (cardType == PayCoreGlobals.PayCoreCCType.Discover)
//        {
//            return R.drawable.discover;
//        }
//        else if (cardType == PayCoreGlobals.PayCoreCCType.MasterCard)
//        {
//            return R.drawable.mastercard;
//        }
//        else if (cardType == PayCoreGlobals.PayCoreCCType.Visa)
//        {
//            return R.drawable.visa;
//        }
//
//        return 0;
//    }


    public String getCardName(PayCoreGlobals.PayCoreCCType cardType)
    {
        if (cardType == PayCoreGlobals.PayCoreCCType.AmericanExpress)
        {
            return "AMEX";
        }
        else if (cardType == PayCoreGlobals.PayCoreCCType.Discover)
        {
            return "DISCOVER";
        }
        else if (cardType == PayCoreGlobals.PayCoreCCType.MasterCard)
        {
            return "MASTERCARD";
        }
        else if (cardType == PayCoreGlobals.PayCoreCCType.Visa)
        {
            return "VISA";
        }

        return "----";
    }


    public PayCoreGlobals.PayCoreCCType getPayCoreCCType(int fromInt)
    {
        PayCoreGlobals.PayCoreCCType retType;
        retType = null;
        switch (fromInt)
        {
            case 1:
                return PayCoreGlobals.PayCoreCCType.AmericanExpress;
            case 2:
                return PayCoreGlobals.PayCoreCCType.Visa;
            case 3:
                return PayCoreGlobals.PayCoreCCType.MasterCard;
            case 4:
                return PayCoreGlobals.PayCoreCCType.DinersClub;
            case 5:
                return PayCoreGlobals.PayCoreCCType.Discover;
        }

        return retType;
    }


    public String bldFullName(Txns fromTxn)
    {
        String retFullname = "";
        if (((fromTxn.getFirst() == null) || (fromTxn.getFirst() == "")) && ((fromTxn.getLast() == null) || (fromTxn.getLast() == "")))
        {
            String useFirst = "";
            String useLast = "";

            Txns aForTxn = fromTxn.getFortxn();

            if (aForTxn != null)
            {
                useFirst = aForTxn.getFirst();
                useLast = aForTxn.getLast();

                if (((useFirst == null) || (useFirst == "")) && ((useLast == null) || useLast == "")) {
                    retFullname = " ";
                }
                else
                {
                    if ((useFirst == null) || (useFirst == ""))
                    {
                        retFullname = useLast;
                    }
                    else
                    {
                        retFullname = useFirst + " " + useLast;
                    }
                }
            }
            else
            {
                retFullname = " ";
            }
        }
        else
        {
            // Found a Name in Top Transaction
            if ((fromTxn.getFirst() == null) || (fromTxn.getFirst() == ""))
            {
                retFullname = fromTxn.getLast();
            }
            else
            {
                retFullname = fromTxn.getFirst() + " " + fromTxn.getLast();
            }
        }
        return retFullname;
    }


    /**
     **doDetermineCardType**

     This method accepts in a Card Number (or maskedPAN) and returns the Card Type (Visa, MC, Amex, Discover)

     @param     cardOrPAN  The card number
     @return    A String containing the Card Brand Name in Caps (AMEX, VISA, MASTERCARD, DISCOVER, or UNKNOWN)
     */

    public String determineCardType(String cardOrPAN)
    {
        String rtnCardType = "UNKNOWN";
        // Note: The following Regular Expressions (RegEx) are based on what is used
        // in the host APIs.

        String amexPattern = "(^34|^37).{13}";
        String visaPattern = "(^4)(.{15}|.{12})";
        String mastercardPattern = "((^5[1-5])(.{17}|.{14}))|((^2[2-7])(.{17}|.{14}))";
        String discoverPattern = "^30[0-5].{5}|^3095.{4}|^35(2[8-9].{4}|[3-8].{5})|";
        discoverPattern = discoverPattern + "^36|^38|^39|^64|^65|^6011|^62(2(1(2[6-9].{2}|";
        discoverPattern = discoverPattern + "[3-9].{2}|[3-9].{3})|[2-9].{4})|[3-6].{5})|";
        discoverPattern = discoverPattern + "^628[2-8].{4}";

        if (Pattern.matches(amexPattern, cardOrPAN) == true)
        {
            rtnCardType = "AMEX";
        }
        else if (Pattern.matches(visaPattern, cardOrPAN) == true)
        {
            rtnCardType = "VISA";
        }
        else if (Pattern.matches(mastercardPattern, cardOrPAN) == true)
        {
            rtnCardType = "MASTERCARD";
        }
        else if (Pattern.matches(discoverPattern, cardOrPAN) == true)
        {
            rtnCardType = "DISCOVER";
        }

        return rtnCardType;
    }



    public String fmtHexToDisplay(String inHex)
    {
        String rtnFmtHex = "";
        char[] inArray = inHex.toCharArray();
        int cnt2 = 0;

        for (char aChr: inArray)
        {
            if (cnt2 == 2)
            {
                rtnFmtHex += " ";
                cnt2 = 0;
            }
            cnt2 = cnt2 + 1;
            rtnFmtHex += aChr;
        }

        return rtnFmtHex;
    }


    public  String getDemoModeVersion(Context uiContext)
    {
        String useDemolabel = "";
        try
        {
            PackageInfo pkgInfo = uiContext.getPackageManager().getPackageInfo(uiContext.getPackageName(), 0);
            useDemolabel = pkgInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            useDemolabel = "DEMO MODE";
        }
        return useDemolabel;
    }


    public void showMessage(Context msgContext, String showTitle, String showMessage)
    {
        AlertDialog.Builder alertDialogBld = new AlertDialog.Builder(msgContext);

        alertDialogBld.setTitle(showTitle);
        alertDialogBld.setMessage(showMessage);

        alertDialogBld.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBld.create();
        alertDialog.show();

    }
}
