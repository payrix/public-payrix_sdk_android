package com.payrix.payrixsdkdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.payrix.paycardapilibrary.PayCardCallbacks;
import com.payrix.paycardapilibrary.PayCardMaster;
import com.payrix.paycardapilibrary.PayCardRDRMgr;
import com.payrix.paycardapilibrary.PayCardSharedAttr;
import com.payrix.paycoreapilibrary.paycoremobile.PayCoreCallbacks;
import com.payrix.paycoreapilibrary.paycoremobile.PayCoreGlobals;
import com.payrix.paycoreapilibrary.paycoremobile.PayCoreMaster;
import com.payrix.paycoreapilibrary.paycoremobile.PayCoreTxn;
import com.payrix.paycoreapilibrary.payrixcore.Txns;
import com.payrix.paycoreapilibrary.payrixcore.TxnsPayment;

import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class DemoTransaction extends AppCompatActivity implements PayCardCallbacks, PayCoreCallbacks
{

    EditText mTxtCost;
    EditText mTxtTax;
    EditText mTxtTip;
    EditText mTxtCardNum;
    Button mBtnStartTxn;
    TextView mLblTotal;
    TextView mLblTxnLog;
    Toolbar toolbar;

    ConstraintLayout mConstrCardEntry;
    TextView mCardHolder;
    TextView mExpMM;
    TextView mExpYY;
    TextView mZip;
    TextView mCVV;

    CurrentTransaction mCurrentTransaction = CurrentTransaction.getInstance();

    Double mCurrentTaxRate;
    Integer mCurrentTipPercent;
    Double mCurrentTipAmt;

    SharedUtilities sharedUtils = SharedUtilities.getInstance();

    /** (Step 1)
     * Instantiate PayCardRDRMgr class - Which handles the native Bluetooth communications
     * Instantiate PayCardMaster class - Which handles the transactional requests and responses with the card reader.
     * Instantiate PayCoreMaster class - Which handles the transactional requests and responses with the Payrix gateway API's.
     */

    public final PayCardRDRMgr payCardReader = PayCardRDRMgr.getInstance(this);
    public final PayCardMaster payCardMaster = PayCardMaster.getInstance(this);
    public final PayCoreMaster payCoreMaster = PayCoreMaster.getInstance(this);

    String btDeviceSerialNumber;
    boolean mConnectionInProgress;
    boolean mCardSwipeActive;
    boolean mWaitingForCard;
    CountDownTimer mSwipeTimer;
    Hashtable<String, Object> mNewTransDict;
    String mDebitCreditType;
    String mCardEntryMode;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_transaction);
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        setSupportActionBar(toolbar);

        mTxtCost = findViewById(R.id.txtCost);
        mTxtTax = findViewById(R.id.txtTax);
        mTxtTip = findViewById(R.id.txtTip);
        mTxtCardNum = findViewById(R.id.txtCardNum);
        mBtnStartTxn = findViewById(R.id.btnStartTxn);
        mLblTotal = findViewById(R.id.lblTxnTotal);
        mLblTxnLog = findViewById(R.id.lblTxnLog);
        mCVV = findViewById(R.id.txtCVV);

        mLblTxnLog.setMovementMethod(new ScrollingMovementMethod());

        mConstrCardEntry = findViewById(R.id.constrCardDetails);
        mCardHolder = findViewById(R.id.txtCardHolder);
        mExpMM = findViewById(R.id.txtExpMM);
        mExpYY = findViewById(R.id.txtExpYY);
        mZip = findViewById(R.id.txtZip);

        mTxtCardNum.setText("");

        toolbarTitle.setText("Payment Transaction");

        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_black_24);
        toolbar.setElevation(10.0f);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // onBackPressed();
                Intent intent = new Intent();
                // tipIntent.putExtra("returnedCurrTrans", mCurrentTransaction);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        doSetCurrentTransaction();

        mConnectionInProgress = false;
        mCardSwipeActive = false;

        mConstrCardEntry.setVisibility(View.INVISIBLE);
        mBtnStartTxn.setVisibility(View.VISIBLE);

        /* (Step 2)
         * Start PayCardRDRMgr and PayCardMaster
         * This step establishes the necessary connections for Callback processing and
         * initializes key PayCard parameters.
         * PayCard handles communication with the Bluetooth reader device.
         * PayCardRDRMgr class handles native bluetooth, while PayCardMaster class handles transactional requests.
         */
        payCardReader.startPayCardReader(this);
        payCardMaster.startPayCardMaster(this);

        // Test Code with Hard Values to be Delete

//        mTxtCardNum.setText("4111111111111111");
//        mTxtCost.setText("12.50");
//        mTxtTax.setText("6.5");
//        mTxtTip.setText("2.00");
//        mZip.setText("33027");
//        mCVV.setText("357");
//        mExpMM.setText("12");
//        mExpYY.setText("23");
//        mCardHolder.setText("John Doe");

        // ****************************************
    }


    /** (Step 3)
     * doSetCurrentTransaction
     * This method prepares to perform a transaction.
     * The demo app uses an object called CurrentTransaction to capture and hold the payment transaction information
     * throughout the lifecycle of the transaction.
     * In the demo, information created during the Authentication and BT Scan steps are used here and elsewhere in this class.
     */
    void doSetCurrentTransaction()
    {
        String usePref = getString(R.string.sharedprefname);
        SharedPreferences sharedAppInfo = getSharedPreferences(usePref, MODE_PRIVATE);

        String merchantID = sharedUtils.getMerchantID(this);
        mCurrentTransaction.merchantID = merchantID;
        String merchantDBA = sharedUtils.getMerchantDBA(this);
        mCurrentTransaction.merchantDBA = merchantDBA;
        mCurrentTransaction.taxPercentage = 0.00;
    }

    /**
     * doBuildTxnAmts
     * This is a utility method to calculate values as needed and then store them in the
     * CurrentTransaction object.
     */
    public void doBuildTxnAmts()
    {
        // Convert array to string
        Float fltCost = Float.parseFloat(mTxtCost.getText().toString());
        Float fltTaxRate = Float.parseFloat(mTxtTax.getText().toString());
        fltTaxRate = fltTaxRate / 100.00f;
        Float fltTip = Float.parseFloat(mTxtTip.getText().toString());

        DecimalFormat decFmt = new DecimalFormat("0.00");
        decFmt.setRoundingMode(RoundingMode.UP);

        mCurrentTransaction.amount = Double.parseDouble(decFmt.format(fltCost.doubleValue()));
        mCurrentTransaction.taxPercentage = fltTaxRate.doubleValue();
        mCurrentTransaction.tipAbsoluteAmount = Double.parseDouble(decFmt.format(fltTip.doubleValue()));
        mCurrentTransaction.tipPercentage = 0;
    }


    /** (Step 4)
     * goStartTxn
     * This method listens for the Start Transaction button to be tapped.
     * The information provided is used to start transaction processing.
     * In this demo if the Card Number is the provided then the transaction is managed as
     * a manual entry transaction.  Otherwise the transaction will require a BT card reader be used.
     *
     * @param view  the Button view passed when the button is passed.
     */
    public void goStartTxn(View view)
    {
        mLblTxnLog.setText("");
        doBuildTxnAmts();

        Double dblTaxAmt = mCurrentTransaction.amount * mCurrentTransaction.taxPercentage;
        Double dblTotal = mCurrentTransaction.amount + dblTaxAmt + mCurrentTransaction.tipAbsoluteAmount;

        DecimalFormat decFmt = new DecimalFormat("0.00");
        decFmt.setRoundingMode(RoundingMode.UP);

        String logMsg = "Starting Transaction Processing: \nTotal Amount: " + decFmt.format(dblTotal);
        mLblTxnLog.setText(logMsg);

        mLblTotal.setText(decFmt.format(dblTotal));

        hideKeyboard();

        if (mTxtCardNum.getText().toString().equalsIgnoreCase(""))
        {
            doCardReaderTxn();
            // Follow Steps: 5a - 11a
        }
        else
        {
            // Display remainder of fields required for manual entry.
            mBtnStartTxn.setVisibility(View.INVISIBLE);
            mConstrCardEntry.setVisibility(View.VISIBLE);
            // Follow Steps: 5b - 7b
        }
    }


    private void hideKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /** (Step 5a)
     * doCardReaderTxn
     * Connect the Bluetooth Reader using the reader found in the Scan Reader step.
     * The Callback from the connection attempt is: didSuccessfulBTConnect
     * or didReceiveBTScanTimeOut or didReceiveCardReaderError
     *
     */
    private void doCardReaderTxn()
    {
        btDeviceSerialNumber = sharedUtils.getBTReader(this);
        payCardReader.connectBTReader(btDeviceSerialNumber);
    }


    /** (Step 5b)
     * goManualTxn
     * This method listens for the Continue button to be tapped.
     * The information provided is used to start transaction processing.
     * In this demo if the Card Number is the provided then the transaction is managed as
     * a manual entry transaction.
     *
     * The information captured in the Manual Transaction area of UI is set in CurrentTransaction object
     * for later processing.
     *
     * @param view  the Button view passed when the button is passed.
     */
    public void goManualTxn(View view)
    {
        hideKeyboard();

        mCurrentTransaction.ccName = mCardHolder.getText().toString();
        mCurrentTransaction.ccEXP = mExpMM.getText().toString() + mExpYY.getText().toString();
        mCurrentTransaction.ccCVV = mCVV.getText().toString();
        mCurrentTransaction.zip = mZip.getText().toString();
        mCurrentTransaction.ccNumber = mTxtCardNum.getText().toString();

        String determinedCardType = sharedUtils.determineCardType(mTxtCardNum.getText().toString());
        mCurrentTransaction.ccCardType = doBuildCardType(determinedCardType);

        doProcessCard();
    }


    /** (Step 6a)
     * didSuccessfulBTConnect
     * Step nn: Connected to Bluetooth Reader Successfully now start Card Reading (Swipe) process
     *
     */
    public void didSuccessfulBTConnect()
    {
        // Successfully connected, so start transaction processing
        if (!mCardSwipeActive)
        {
            mCardSwipeActive = true;
            startCardTransactionProcess();
        }
    }


    /** (Step 7a)
     * startCardTransactionProcess
     * Started Card Reading (Swipe) process with PayCard doReadCard request passing the transaction information.
     * Callbacks are:  didReceiveReaderModeUpdate, didReceiveSwipeSuccess, and didReceiveCardReaderIssue
     *
     */
    private void startCardTransactionProcess()
    {
        Double dblTaxAmt = mCurrentTransaction.amount * mCurrentTransaction.taxPercentage;
        Double dblTotal = mCurrentTransaction.amount + dblTaxAmt + mCurrentTransaction.tipAbsoluteAmount;

        payCardMaster.doReadCard(dblTotal,
                dblTaxAmt,
                mCurrentTransaction.tipAbsoluteAmount,
                "USD", sharedUtils.getURL(this,""), PayCardSharedAttr.PayCardDeviceMode.cardDeviceMode_Swipe);
    }


    /** (Step 6b)
     * doProcessCard
     * This method prepares and starts the manual card processing using the
     * PayCoreMaster method: doManualCardTransaction
     *
     * The callback is: didReceiveTransactionResponse
     *
     */
    public void doProcessCard()
    {
        mCurrentTransaction.receiptEMVChipInd = "Manual";
        mCardEntryMode = "MANUAL";

        String sessionKey = sharedUtils.getSessionKey(this);

        Double dblTaxAmt = mCurrentTransaction.amount * mCurrentTransaction.taxPercentage;
        Double dblTotal = mCurrentTransaction.amount + dblTaxAmt + mCurrentTransaction.tipAbsoluteAmount;

        PayCoreGlobals.PayCoreCCType theCCType;

        if (mCurrentTransaction.ccCardType != null)
        {
            theCCType = PayCoreGlobals.PayCoreCCType.valueOf(mCurrentTransaction.ccCardType.toString());
        }
        else
        {
            theCCType = null;
        }

        String useURL = sharedUtils.getURL(this,"/txns");

        payCoreMaster.doManualCardTransaction(mCurrentTransaction.merchantID,
                sessionKey,
                useURL,
                mCurrentTransaction.amount,
                mCurrentTransaction.taxPercentage,
                mCurrentTransaction.tipPercentage.doubleValue(),
                mCurrentTransaction.tipAbsoluteAmount,
                dblTotal,
                mCurrentTransaction.ccName,
                mCurrentTransaction.ccNumber,
                theCCType,
                mCurrentTransaction.ccCVV,
                mCurrentTransaction.ccEXP,
                PayCoreTxn.PayCoreTxnOrigin.eCommerceSystem,
                mCurrentTransaction.zip);
    }


    /** (Step 8a)
     * didReceiveReaderModeUpdate
     * This callback handles messages and action requests from the card reader.
     * In the demo app these messages and action requests such as "Please Swipe Card"
     * are displayed in the UI Log.
     *
     * @param currentMode   is the message or action request from the device.
     *
     */
    public void didReceiveReaderModeUpdate(PayCardSharedAttr.PayCardDeviceMode currentMode)
    {
        if (mSwipeTimer != null)
        {
            mSwipeTimer.cancel();
        }

        switch (currentMode)
        {
            case cardDeviceMode_Swipe:
                updateLog("Please Swipe Card");
                mWaitingForCard = true;
           //     swipeTimer();
                break;

            case cardDeviceMode_Insert:
                updateLog("Please Insert Card");
                mWaitingForCard = true;
            //    swipeTimer();
                break;

            case cardDeviceMode_Tap:
                updateLog("Please Tap Card");
                mWaitingForCard = true;
            //    swipeTimer();
                break;

            case cardDeviceMode_SwipeOrInsert:
                updateLog("Please Swipe or Insert Card");
                mWaitingForCard = true;
            //    swipeTimer();
                break;

            case cardDeviceMode_SwipeOrTap:
                updateLog("Please Swipe or Tap Card");
                mWaitingForCard = true;
            //    swipeTimer();
                break;

            case cardDeviceMode_SwipeOrInsertOrTap:
                updateLog("Please Swipe or Insert or Tap Card");
                mWaitingForCard = true;
            //    swipeTimer();
                break;

            case cardDeviceMode_InsertNotSwipe:
                updateLog("Please Insert (Do Not Swipe) Card");
                mWaitingForCard = true;
            //    swipeTimer();
                break;

            case cardDeviceMode_ManualEntry:
                updateLog("Manual Entry Required. Tap KeyPad button below");
                mWaitingForCard = false;
            //    mSwipeTimer.cancel();
                break;

            case cardDeviceMode_Confirm:
                // Process the EMV Confirm Request.  If in future we wish the consumer to confirm this will be revised.
                updateLog("Confirming...");
                mWaitingForCard = false;
                payCardMaster.doCardConfirm(true);
            //    mSwipeTimer.cancel();
                break;

            default:
                // Handle unknown Mode States
                updateLog("Unexpected Mode State: " + currentMode.toString());
                mWaitingForCard = false;
            //    mSwipeTimer.cancel();
                break;
        }
    }


    private void updateLog(String newEntry)
    {
        String theCurrentLog = mLblTxnLog.getText().toString();
        theCurrentLog = theCurrentLog + "\n" + newEntry;
        mLblTxnLog.setText(theCurrentLog);
    }


    /** (Step 9a)
     * didReceiveSwipeSuccess
     * This callback handles the successful completion of the card swipe / reading process.
     * The information is then sent to PayCore for gateway transaction processing.
     *
     * @param gateWayData   The data returned by the device
     * @param cardStatus    The status of the card request
     * @param cardInfo      The structure information from the Payrix SDK prepared for gateway processing.
     *
     */
    public void didReceiveSwipeSuccess(Hashtable<String, Object> gateWayData, String cardStatus, Hashtable<String, String> cardInfo)
    {
        if (mSwipeTimer != null)
        {
            mSwipeTimer.cancel();
        }
        mCardSwipeActive = true;
        mWaitingForCard = false;

        mNewTransDict = new Hashtable<>();

        updateLog("Processing Payment...");

        String useMonth = cardInfo.get("cardExpiryMonth");
        String useYear = cardInfo.get("cardExpiryYear");

        mCurrentTransaction.ccName = cardInfo.get("cardHolderName");
        mCurrentTransaction.ccEXP = useMonth + useYear;
        mCurrentTransaction.ccNumber = cardInfo.get("cardNumber");

        mNewTransDict = gateWayData;

        String cardType = cardInfo.get("cardType");
        if (cardType.toUpperCase() != "UNKNOWN")
        {
            mDebitCreditType = cardType;
            mNewTransDict.put("type", sharedUtils.bldCCType(cardType));
        }

        mNewTransDict.put("merchant", mCurrentTransaction.merchantID);
        mNewTransDict.put("origin", PayCoreTxn.PayCoreTxnOrigin.eCommerceSystem.getValue());
        mNewTransDict.put("posentrymode",PayCardSharedAttr.readerEntryMode.read_MagneticStrip.toString());

        mCurrentTransaction.receiptEMVChipInd = "Swipe";

        doCardToCoreTransaction("SWIPE");
    }


    /** (Step 10a)
     * doCardToCoreTransaction
     * This method handles engaging PayCoreMaster class for transaction processing with the Payrix gateway.
     * The method called is: doCardReaderTransaction
     * The callbacks for this method call is: didReceiveTransactionResponse
     *
     * @param entryMode This is the entry mode used to capture the Card information.
     *
     */
    private void doCardToCoreTransaction(String entryMode)
    {
        if (mSwipeTimer != null)
        {
            mSwipeTimer.cancel();
        }
        mCardEntryMode = entryMode;
        String sessionKey = sharedUtils.getSessionKey(this);
        payCoreMaster.doCardReaderTransaction(mNewTransDict, sessionKey, sharedUtils.getURL(this,"/txns"));
    }


    /** (Step 11a and 7b)
     * didReceiveTransactionResponse
     * This callback handles the response from the Payrix gateway for the doCardReaderTransaction or doManualCardTransaction requests.
     * This is the last step in the transaction processing.
     *
     * @param success           A Boolean indicator showing if the transaction was approved or declined by the Payrix gateway.
     * @param theTransactions   An array of transactions containing the successful transaction results
     * @param theDetails        Any details regarding the transactions (if any)
     * @param theErrors         Any errors messages regarding the transaction if the transaction failed.
     *
     */
    public void didReceiveTransactionResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors)
    {
        // Handle Response from Request
        // Determine next process step based on Entry-Mode (mCardEntryMode)
        mCardSwipeActive = false;

        if (success)
        {
            // Successful Transaction

            Txns aTransaction = theTransactions.get(0);
            if ((mCardEntryMode.toUpperCase().equalsIgnoreCase("SWIPE") ) || (mCardEntryMode.toUpperCase().equalsIgnoreCase("MANUAL")))
            {
                updateLog("Payment Completed Successfully...");

                TxnsPayment txnsPayment = aTransaction.getPayment();
                String ccNum = txnsPayment.getNumber();
                if (ccNum == null)
                {
                    ccNum = "----";
                }

                mCurrentTransaction.ccNumber = ccNum;
                Integer methodCCType = txnsPayment.getMethod();
                mCurrentTransaction.ccCardType = sharedUtils.getPayCoreCCType(methodCCType);

                if (mDebitCreditType != null)
                {
                    PayCoreGlobals.PayCoreCCType useCardType = sharedUtils.bldCCType(mDebitCreditType);
                    mCurrentTransaction.ccCardType = useCardType;
                }

                mCurrentTransaction.receiptApprovedDeclined = "APPROVED";
                mCurrentTransaction.ccName = aTransaction.getFirst() + " " + aTransaction.getLast();
                mCurrentTransaction.ccEXP = aTransaction.getExpiration();
                mCurrentTransaction.transactionID = aTransaction.getId();
                mCurrentTransaction.receiptAuthApprovalCode = aTransaction.getAuthorization();

                updateLog("- Signature can be captured at this point -");
                updateLog("** Transaction Completed Successfully **");

                doClearUI();
            }
        }
        else
        {
            // Failed Transaction
            if ((mCardEntryMode.toUpperCase().equalsIgnoreCase("SWIPE") ) || (mCardEntryMode.toUpperCase().equalsIgnoreCase("MANUAL")))
            {
                updateLog("Payment Declined...");
                mCurrentTransaction.errorMessages = theErrors;

                int ctr = 0;

                while (ctr < theErrors.size())
                {
                    updateLog(theErrors.get(ctr));
                    ctr++;
                }

                if (theTransactions != null)
                {
                    Txns aTransaction = theTransactions.get(0);
                    TxnsPayment txnsPayment = aTransaction.getPayment();
                    String ccNum = txnsPayment.getNumber();
                    if (ccNum == null)
                    {
                        ccNum = "----";
                    }
                    mCurrentTransaction.ccNumber = ccNum;

                    mCurrentTransaction.ccName = aTransaction.getFirst() + " " + aTransaction.getLast();
                    mCurrentTransaction.ccEXP = aTransaction.getExpiration();
                    mCurrentTransaction.transactionID = aTransaction.getId();
                }

                if (mDebitCreditType != null)
                {
                    PayCoreGlobals.PayCoreCCType useCardType = sharedUtils.bldCCType(mDebitCreditType);
                    mCurrentTransaction.ccCardType = useCardType;
                }

                mCurrentTransaction.receiptApprovedDeclined = "DECLINED";
                updateLog("** Transaction Processing Failed **");
                doClearUI();
            }
        }
    }


    private void doClearUI()
    {
        mLblTotal.setText("");
        mCurrentTransaction.init();
        mTxtCardNum.setText("");
        mTxtTip.setText("");
        mTxtTax.setText("");
        mTxtCost.setText("");

        mCardHolder.setText("");
        mExpYY.setText("");
        mExpMM.setText("");
        mCVV.setText("");
        mZip.setText("");

        mConstrCardEntry.setVisibility(View.INVISIBLE);
        mBtnStartTxn.setVisibility(View.VISIBLE);
    }


    private PayCoreGlobals.PayCoreCCType doBuildCardType(String inTypeString)
    {
        // Convert array to string

        if (inTypeString.toUpperCase() != "UNKNOWN")
        {
            switch ( inTypeString.toUpperCase())
            {
                case "AMEX":
                    return PayCoreGlobals.PayCoreCCType.AmericanExpress;
                case "VISA":
                    return PayCoreGlobals.PayCoreCCType.Visa;
                case "MASTERCARD":
                    return PayCoreGlobals.PayCoreCCType.MasterCard;
                case "DINERSCLUB":
                    return PayCoreGlobals.PayCoreCCType.DinersClub;
                case "DISCOVER":
                    return PayCoreGlobals.PayCoreCCType.Discover;
                default:
                    return null;
            }
        }
        else
        {
            return null;
        }
    }


    public void didCompleteCancelRequest(boolean successful)
    {
        // Cancel completed so the back request can be performed.
        onBackPressed();
    }


    public void didReceiveMessageToDisplay(String message)
    {
        updateLog(message);
    }

    /**
     * The following are all the other possible Callbacks that are offered in the PayCard and PayCore libraries
     * of the Payrix SDK.
     *
     */

    // Unused Optional PayCore Callbacks

    public void didReceiveFinalEMVBatchData(String tlv) {}
    public void didCompleteEMVCardTransaction(boolean successful, String transactionMsg) {}
    public void didReceiveLoginResponse(Boolean success, String sessionKey){}
    public void didReceiveMerchantIDInfo(Boolean success, String merchantID, String merchantDBA){}
    public void didReceiveSubsequentTxnResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors){}
    public void didReceiveSpecificTxnResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors){}
    public void didReceiveRefundResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors){}

    // Unused Optional PayCard Callbacks

    public void didReceiveBTDisconnect() {}
    public void didReceiveAudioDisconnectedNotice() {}
    public void didReceiveBTScanTimeOut() {}
    public void didReceiveAudioConnectedNotice() {}
    public void didFindRDRDevices(List<BluetoothDevice> foundDevices){}
    public void didReceiveCardReaderConnectionFailed(String error){}
    public void didReceiveDeviceInfo(Hashtable<String, String> deviceInfoData){}
    public void didReceiveCardReaderIssue(String issueMsg){}
    public void requestForHostEMVProcess(Hashtable<String, Object> gatewayData, Object encEMV, Object emvKSN, String encMethod){}
    public void didReceiveRefundEligibleStatus(Boolean success){}

    public void didReceiveAudioAttachedNotice() {}
    public void didReceiveCardReaderError(Integer errNumber, String errMessage) {}
}
