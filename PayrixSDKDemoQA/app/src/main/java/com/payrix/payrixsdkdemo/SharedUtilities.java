package com.payrix.payrixsdkdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.payrix.payrixsdk.PayRequest;
import com.payrix.payrixsdk.PayResponse;
import com.payrix.payrixsdk.PaySharedAttributes;
import com.payrix.payrixsdk.paycore.paycoremobile.PayCoreGlobals;
import com.payrix.payrixsdk.paycore.payrixcore.Txns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;
import static android.content.Context.MODE_PRIVATE;

public class SharedUtilities
{
    public static SharedUtilities utilsInstance = new SharedUtilities();
    public SharedUtilities(){}
    
    // public static synchronized SharedUtilities getInstance()
    public static synchronized SharedUtilities getInstance()
    {
        if (utilsInstance == null)
        {
            utilsInstance = new SharedUtilities();
        }
        return utilsInstance;
    }

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
    static String envSandboxOnKey = "com.Payrix.SandBoxOnKey";
    static String envSelectionKey = "com.Payrix.PlatformURLKey";
    static String btManufacturerKey = "com.Payrix.CurrentBTManfg";

    // BT Device Key
    static String btDeviceKey = "CurrentBTReader";
    
    // BT Platform Key
    static String payrixPlatformKey = "PAYRIXPLATFORMID";

    static String useTxnSessionDefault = "USE_TXN_SESSION_AS_DEFAULT";

//    public static synchronized SharedUtilities getInstance()
//    {
//        if (sharedInstance == null)
//        {
//            sharedInstance = new SharedUtilities();
//        }
//        return sharedInstance;
//    }


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
    
    
    public void setPlatformID(Context context, int withKey)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences.Editor sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE).edit();
        
        sharedAppInfo.putInt(payrixPlatformKey, withKey);
        sharedAppInfo.apply();
    }
    
    public int getPlatformID(Context context)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = context.getSharedPreferences(usePref, MODE_PRIVATE);
        int useKey = sharedAppInfo.getInt(payrixPlatformKey, 0);
        return useKey;
    }
    
    
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
        return sharedAppInfo.getString(sessionKeyDefaultsKey, null);
    }

    public void setUseTxnSession(Context context, boolean isTxnSession)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences.Editor sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE).edit();

        sharedAppInfo.putBoolean(useTxnSessionDefault, isTxnSession);
        sharedAppInfo.apply();
    }

    public boolean getUseTxnSession(Context context)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = context.getSharedPreferences(usePref, MODE_PRIVATE);
        return sharedAppInfo.getBoolean(useTxnSessionDefault, false);
    }
    
    
    public void setEnvSelection(Context context, String withURL)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences.Editor sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE).edit();
        
        sharedAppInfo.putString(envSelectionKey, withURL);
        sharedAppInfo.apply();
    }
    
    public String getEnvSelection(Context context)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = context.getSharedPreferences(usePref, MODE_PRIVATE);
        String useKey = sharedAppInfo.getString(envSelectionKey, null);
        return useKey;
    }
    
    
    public Boolean getSandBoxOn(Context context)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = context.getSharedPreferences(usePref, MODE_PRIVATE);
        Boolean useMode = sharedAppInfo.getBoolean(envSandboxOnKey, false);
        return useMode;
    }
    
    public void setSandBoxOn(Context context, Boolean isSandboxMode)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences.Editor sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE).edit();
        
        sharedAppInfo.putBoolean(envSandboxOnKey, isSandboxMode);
        sharedAppInfo.apply();
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
        String useValue = sharedAppInfo.getString(merchantIDKey, null);
        return useValue;
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
    
    
    public void setBTManfg(Context context, String withKey)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences.Editor sharedAppInfo = context.getSharedPreferences(usePref,MODE_PRIVATE).edit();
        sharedAppInfo.putString(btManufacturerKey, withKey);
        sharedAppInfo.apply();
    }
    
    public String getBTManfg(Context context)
    {
        String usePref = context.getResources().getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = context.getSharedPreferences(usePref, MODE_PRIVATE);
        String useKey = sharedAppInfo.getString(btManufacturerKey, null);
        return useKey;
    }
    
    
    /**
     **doCheckNetworkConnection**
     
     This method checks the network availability and responds with a Booloean (True, Connectivity Available or False, No Connectivity
     - Parameters:
     @param context    The calling app view context.
     
     **/
    public Boolean checkNetworkConnection(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        boolean isConnected = false;
        if (connectivityManager != null)
        {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            isConnected = (activeNetwork != null) && (activeNetwork.isConnectedOrConnecting());
        }
        
        return isConnected;
    }
    
    
    public void doWriteLogFile(Context context, String fileName, String fileData)
    {
        String fullName = "DemoLogFile\\" + fileName + ".txt";
        try
        {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fullName, Context.MODE_PRIVATE));
            outputStreamWriter.write(fileData);
            outputStreamWriter.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    
    
    public String doGenLogString(PayRequest payRequest, PayResponse payResponse)
    {
        if (payRequest != null)
        {
            return payRequest.toString();
        }
        else if (payResponse != null)
        {
            return payResponse.toString();
        }
        return "";
    }
    
    
    private String doReadLogFile(Context context, String fileName)
    {
        String ret = "";
        String fullName = "DemoLogFile\\" + fileName + ".txt";
        
        try
        {
            InputStream inputStream = context.openFileInput(fullName);
            if (inputStream != null)
            {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                
                while ( (receiveString = bufferedReader.readLine()) != null )
                {
                    stringBuilder.append("\n").append(receiveString);
                }
                
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e)
        {
            Log.e("PAYRIX-IO", "File not found: " + e.toString());
        }
        catch (IOException e)
        {
            Log.e("PAYRIX-IO", "Can not read file: " + e.toString());
        }
        return ret;
    }
    
    
    private ArrayList<String> doGetLogFileList(Context context, String fileName)
    {
        File fileDir = context.getFilesDir();
        File[] filesList = fileDir.listFiles();
        
        ArrayList<String> theFiles = new ArrayList<>();
        
        for (int ctr = 0; ctr < filesList.length; ctr++)
        {
            theFiles.add(filesList[ctr].getName());
        }
        return theFiles;
    }
    
    
    public String getURL(Context context, String theURI)
    {
        Boolean isSandBox =  getSandBoxOn(context);
        String theEnv = getEnvSelection(context);
        String useURL = "";
        if (isSandBox)
        {
            useURL = "https://test-" + theEnv + theURI;
        }
        else
        {
            useURL = "https://" + theEnv + theURI;
        }
        return useURL;
    }
    
    
    public PaySharedAttributes.CCType bldCCType(String cardType)
    {
        switch (cardType.toUpperCase())
        {
            case "AMEX":
                return PaySharedAttributes.CCType.American_Express;
            case "DISCOVER":
                return PaySharedAttributes.CCType.Discover;
            case "MASTERCARD":
                return PaySharedAttributes.CCType.MasterCard;
            case "VISA":
                return PaySharedAttributes.CCType.Visa;
            case "DINERSCLUB":
                return PaySharedAttributes.CCType.DinersClub;
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
    
    
    public String getCardName(PaySharedAttributes.CCType cardType)
    {
        if (cardType == PaySharedAttributes.CCType.American_Express)
        {
            return "AmericanExpress";
        }
        else if (cardType == PaySharedAttributes.CCType.Visa)
        {
            return "Visa";
        }
        else if (cardType == PaySharedAttributes.CCType.MasterCard)
        {
            return "MasterCard";
        }
        else if (cardType == PaySharedAttributes.CCType.DinersClub)
        {
            return "DinersClub";
        }
        else if (cardType == PaySharedAttributes.CCType.Discover)
        {
            return "Discover";
        }
        else
        {
            return "";
        }
    }
    
    
    public PaySharedAttributes.CCType getPayCoreCCType(int fromInt) // bldCCType(String cardType)
    {
        switch (fromInt)
        {
            case 1:
                return PaySharedAttributes.CCType.American_Express;
            case 5:
                return PaySharedAttributes.CCType.Discover;
            case 3:
                return PaySharedAttributes.CCType.MasterCard;
            case 2:
                return PaySharedAttributes.CCType.Visa;
            case 4:
                return PaySharedAttributes.CCType.DinersClub;
            default:
                break;
        }
        return null;
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

    public String bldFullName(Txns fromTxn)
    {
        String retFullname = "";
        if (((fromTxn.getFirst() == null) || (Objects.equals(fromTxn.getFirst(), ""))) && ((fromTxn.getLast() == null) || (Objects.equals(fromTxn.getLast(), ""))))
        {
            String useFirst = "";
            String useLast = "";

            useFirst = fromTxn.getFirst();
            useLast = fromTxn.getLast();

            if (((useFirst == null) || (useFirst.equals(""))) && ((useLast == null) || useLast.equals(""))) {
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
                    retFullname = String.format("%s %s", useFirst, useLast);
                }
            }
        }
        else
        {
            // Found a Name in Top Transaction
            if ((fromTxn.getFirst() == null) || (Objects.equals(fromTxn.getFirst(), "")))
            {
                retFullname = fromTxn.getLast();
            }
            else
            {
                retFullname = String.format("%s %s",fromTxn.getFirst(), fromTxn.getLast());
            }
        }
        return retFullname;
    }

}
