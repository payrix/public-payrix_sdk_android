package com.payrix.payrixsdkdemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.payrix.payrixsdk.PayDevice;
import com.payrix.payrixsdk.PayMerchant;
import com.payrix.payrixsdk.PayRequest;
import com.payrix.payrixsdk.PayResponse;
import com.payrix.payrixsdk.PaySharedAttributes;
import com.payrix.payrixsdk.PayrixSDK;
import com.payrix.payrixsdk.PayrixSDKCallbacks;
import com.payrix.payrixsdk.RefundResponse;
import com.payrix.payrixsdk.TxnDataResponse;
import com.payrix.payrixsdk.paycore.payrixcore.TxnSession;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class DemoTransaction extends AppCompatActivity implements PayrixSDKCallbacks {
    EditText mTxtCost;
    EditText mTxtTax;
    EditText mTxtTip;
    EditText mTxtCardNum;
    Button mBtnStartTxn;
    Button mTBReceiptBtn;
    TextView mLblTotal;
    TextView mLblTxnLog;
    Toolbar toolbar;
    TextView mLblActionMsg;

    LinearLayoutCompat mCardEntry;
    TextView mCardHolder;
    TextView mExpMM;
    TextView mExpYY;
    TextView mZip;
    TextView mCVV;

    String demoTag = "PAYRIX-DEMO:";

    CurrentTransaction mCurrentTransaction = CurrentTransaction.getInstance();
    SharedUtilities sharedUtils = SharedUtilities.getInstance();
    PayrixSDK payrixSDK = PayrixSDK.getInstance(this);
    String cardEntryMode = "";  // SWIPE or EMV or TAP
    String btDeviceSerialNumber;
    boolean mConnectionInProgress;
    Double mCurrentTaxRate;
    Integer mCurrentTipPercent;
    Double mCurrentTipAmt;
    EMVCardAppObj mTheSelectedApp;
    boolean mWaitingForCard;
    CountDownTimer mSwipeTimer;
    Hashtable<String, Object> mNewTransDict;
    String mDebitCreditType;
    String mCardEntryMode;
    private ProgressBar progressBar;
    private Button cancelBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_transaction);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        mLblActionMsg = findViewById(R.id.lblActionMsg);
        mTBReceiptBtn = findViewById(R.id.tbButton);
        progressBar = findViewById(R.id.loading);

        mLblTxnLog.setMovementMethod(new ScrollingMovementMethod());
        mCardEntry = findViewById(R.id.cardDetails);
        mCardHolder = findViewById(R.id.txtCardHolder);
        mExpMM = findViewById(R.id.txtExpMM);
        mExpYY = findViewById(R.id.txtExpYY);
        mZip = findViewById(R.id.txtZip);
        cancelBtn = findViewById(R.id.btnCancelTxn);

        mTxtCardNum.setText("");

        toolbarTitle.setText("Payment Transaction");

        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_black_24);
        toolbar.setElevation(10.0f);
        toolbar.setNavigationOnClickListener(view -> {
            // onBackPressed();
            mTBReceiptBtn.setVisibility(View.INVISIBLE);
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });

        mTBReceiptBtn.setOnClickListener(view -> jumpToReceiptUI());

        mTxtCost.addTextChangedListener(textWatcher);
        mTxtTip.addTextChangedListener(textWatcher);
        mTxtTax.addTextChangedListener(textWatcher);

        mTBReceiptBtn.setText("RECEIPT");
        mTBReceiptBtn.setVisibility(View.INVISIBLE);

        mConnectionInProgress = false;

        mCardEntry.setVisibility(View.GONE);

        mBtnStartTxn.setVisibility(View.VISIBLE);

        /* (Step 2)
         * Start PayCardRDRMgr and PayCardMaster
         * This step establishes the necessary connections for Callback processing and
         * initializes key PayCard parameters.
         * PayCard handles communication with the Bluetooth reader device.
         * PayCardRDRMgr class handles native bluetooth, while PayCardMaster class handles transactional requests.
         */

        PaySharedAttributes.PaySupportedReaders useManfg = doDetermineManfg();
        Boolean isSandBox =  sharedUtils.getSandBoxOn(this);
        String theEnv =  sharedUtils.getEnvSelection(this);
        payrixSDK.doSetPayrixPlatform(this, theEnv, isSandBox, useManfg);

        //For device / phone (Google Pixel 6) that triggers google pay app and closes Payrix app during transaction.
        //Call this method to disable this anomaly.
        payrixSDK.doPhoneNFCDisable(this, true);

        sharedUtils.setDemoMode(this, isSandBox);

        // Test Code with Hard Values to be Delete

        mLblTotal.setText("2.50");
        mTxtCost.setText("2.50");
        mTxtTax.setText("0.0");
        mTxtTip.setText("0.00");

//        mTxtCardNum.setText("4111111111111111");
//        mZip.setText("33027");
//        mCVV.setText("357");
//        mExpMM.setText("12");
//        mExpYY.setText("23");
//        mCardHolder.setText("John Doe");

        doSetCurrentTransaction();

        mTxtCost.addTextChangedListener(textWatcher);
        mTxtTax.addTextChangedListener(textWatcher);
        mTxtTip.addTextChangedListener(textWatcher);

        mTxtCardNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0)
                {
                    mCardEntry.setVisibility(View.VISIBLE);
//                    goStartTxn();
                } else {
                    mCardEntry.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!sharedUtils.checkNetworkConnection(this)) {
            sharedUtils.showMessage(this, "Payrix SDK Demo App", "The Network Connection is Not Available; Resolve and Retry");
        }
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.length() > 0 && Double.parseDouble(charSequence.toString()) > 0) {
                doBuildTxnAmts();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    private void jumpToReceiptUI() {
        Intent receiptIntent = new Intent(this, DemoReceipt.class);
        startActivity(receiptIntent);
    }


    private PaySharedAttributes.PaySupportedReaders doDetermineManfg() {
        // String devManfg = sharedUtils.getBTManfg(this);
        return PaySharedAttributes.PaySupportedReaders.reader_BBPOS;
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
        String merchantID = sharedUtils.getMerchantID(this);
        mCurrentTransaction.merchantID = merchantID;
        mCurrentTransaction.merchantDBA = sharedUtils.getMerchantDBA(this);
        mCurrentTransaction.taxPercentage = 0.00;

        if (merchantID == null)
        {
            updateLog("Authenticate before processing a transaction");
            sharedUtils.showMessage(this, "Demo Transaction", "Authenticate before processing a transaction");
        }
    }

    /**
     * doBuildTxnAmts
     * This is a utility method to calculate values as needed and then store them in the
     * CurrentTransaction object.
     */
    public void doBuildTxnAmts() {
        // Convert array to string
        float fltCost = Float.parseFloat(mTxtCost.getText().toString());
        float fltTaxRate = 0.0f;
        float fltTip = 0.0f;
        if (!mTxtTax.getText().toString().equalsIgnoreCase("")) {
            fltTaxRate = Float.parseFloat(mTxtTax.getText().toString());
            fltTaxRate = fltTaxRate / 100.00f;
        }
        if (!mTxtTip.getText().toString().equalsIgnoreCase("")) {
            fltTip = Float.parseFloat(mTxtTip.getText().toString());
        }

        DecimalFormat decFmt = new DecimalFormat("0.00");

        mCurrentTransaction.amount = Double.parseDouble(decFmt.format((double) fltCost));
        mCurrentTransaction.taxPercentage = (double) fltTaxRate;
        mCurrentTransaction.tipAbsoluteAmount = Double.parseDouble(decFmt.format((double) fltTip));
        mCurrentTransaction.tipPercentage = 0;

        Double calcTax = mCurrentTransaction.amount * mCurrentTransaction.taxPercentage;
        Double calcTotal = mCurrentTransaction.amount + calcTax + mCurrentTransaction.tipAbsoluteAmount;

        mLblTotal.setText(decFmt.format(calcTotal));

        String logMsg = "Total Amount: " + decFmt.format(calcTotal);
        updateLog(logMsg);
    }


    /** (Step 4)
     * goStartTxn
     * This method listens for the Start Transaction button to be tapped.
     * The information provided is used to start transaction processing.
     * In this demo if the Card Number is the provided then the transaction is managed as
     * a manual entry transaction.  Otherwise the transaction will require a BT card reader be used.
     *
     */
    public void goStartTxn()
    {
        mTBReceiptBtn.setVisibility(View.INVISIBLE);
        mLblTxnLog.setText("");
        doBuildTxnAmts();

        Double dblTaxAmt = mCurrentTransaction.amount * mCurrentTransaction.taxPercentage;
        Double dblTotal = mCurrentTransaction.amount + dblTaxAmt + mCurrentTransaction.tipAbsoluteAmount;

        DecimalFormat decFmt = new DecimalFormat("0.00");

        String logMsg = "Starting Transaction Processing: \nTotal Amount: " + decFmt.format(dblTotal);
        mLblTxnLog.setText(logMsg);

        mLblTotal.setText(decFmt.format(dblTotal));


        if ((mTxtCardNum.getText() == null) || (mTxtCardNum.getText().toString().equalsIgnoreCase("")))
        {
            cancelBtn.setVisibility(View.VISIBLE);
            doPrepCardReader();
            // Follow Steps: 5a - 11a
        }
        else
        {
            // Display remainder of fields required for manual entry.
            //mBtnStartTxn.setVisibility(View.INVISIBLE);
            //mBtnStartTxn.setText("Continue");
            cancelBtn.setVisibility(View.GONE);
            doManualTxn();


           // mCardEntry.setVisibility(View.VISIBLE);
            // Follow Steps: 5b - 7b
        }
    }


    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /** (Step 5a)
     * doPrepCardReader
     * Connect the Bluetooth Reader using the reader found in the Scan Reader step.
     * The Callback from the connection attempt is: didSuccessfulBTConnect
     * or didReceiveBTScanTimeOut or didReceiveCardReaderError
     *
     */
    private void doPrepCardReader() {
        PayDevice payDevice = new PayDevice();
        payDevice.init();
        payDevice.deviceManfg = sharedUtils.getBTManfg(this);
        payDevice.deviceSerial = sharedUtils.getBTReader(this);
        payrixSDK.doConnectBTReader(payDevice);
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
     */
    public void goProcessTxn(View view)
    {
        progressBar.setVisibility(View.VISIBLE);
        hideKeyboard();
        goStartTxn();
    }

    public void cancelTxn(View view) {
        progressBar.setVisibility(View.VISIBLE);
        payrixSDK.doCancelCheckCard();
    }


    /** (Step 6a)
     * didReceiveBTConnectResults
     * Step nn: Connected to Bluetooth Reader Successfully now start Card Reading (Swipe) process
     *
     */
    @Override
    public void didReceiveBTConnectResults(Boolean connectSuccess, String theDevice)
    {
        if (connectSuccess) {
            updateLog("BT Device: " + btDeviceSerialNumber + " Connected");
            doCardReaderTxn();
        } else {
            updateLog("BT CONNECTION FAILED: Device: " + btDeviceSerialNumber);
            updateLog("Transaction Not Started \n\n");
        }
    }


    /**
     **didReceiveBTScanTimeOut**
     This is the callback for the BT Connect request.
     */
    public void didReceiveBTScanTimeOut()
    {
        updateLog("The automatic connection of the Card Reader was unsuccessful.");
    }


    @Override
    public void didReceiveScanResults(Boolean scanSuccess, String scanMsg, ArrayList<PayDevice> payDevices)
    {
        if (scanSuccess) {
            updateLog(scanMsg);
            // Capture Scanned Readers objects
        } else {
            progressBar.setVisibility(View.GONE);
            updateLog("The automatic connection of the Card Reader was unsuccessful.");
        }
    }


    /**
     **doCardReaderTxn**
     * (Step 7a)
     Started Card Reading (Swipe) process with PayCard doReadCard request passing the transaction information.
     */
    private void doCardReaderTxn()
    {
        PayRequest payRequest = PayRequest.getInstance();
        cardEntryMode = "EMV";

        Float bdAmount = mCurrentTransaction.amount.floatValue();

        Float bdTaxPcnt = mCurrentTransaction.taxPercentage.floatValue();
        Float bdTipAbs = mCurrentTransaction.tipAbsoluteAmount.floatValue();

        updateLog("Float Amount: " + bdAmount);
        updateLog("Float TaxPcnt: " + bdTaxPcnt);
        updateLog("Float TipAmt: " + bdTipAbs);

        Float calcTax = bdAmount * bdTaxPcnt;   // mCurrentTransaction.amount * mCurrentTransaction.taxPercentage;
        Float calcTotal = bdAmount + calcTax + bdTipAbs;  // mCurrentTransaction.amount + calcTax + mCurrentTransaction.tipAbsoluteAmount;

        updateLog("Float TaxAmt: " + calcTax);
        updateLog("Float Total: " + calcTotal);

        Float m100 = 100.00f;

        int useVal = Math.round(calcTotal * m100);  // Error Occurs here; CalcTotal was 1.06; UseVal is 105.99999
        payRequest.payTotalAmt = useVal; //.intValue();

        useVal = Math.round(calcTax * m100);
        payRequest.payTaxAmt =  useVal; //.intValue();

        useVal = Math.round(bdTipAbs *m100);
        payRequest.payTipAmt =  useVal; //.intValue();

        useVal = Math.round(bdTaxPcnt * m100);
        payRequest.payTaxPercent = useVal; //.intValue();

        payRequest.payCurrencyCode = "USD";
        payRequest.payHostURL = sharedUtils.getURL(this,"");

        useVal = Math.round(bdAmount * m100);
        payRequest.payAmount = useVal; //.intValue();

        payRequest.payManualEntry = false;
        payRequest.payDeviceMode = PaySharedAttributes.PayDeviceMode.cardDeviceMode_SwipeOrInsertOrTap;
        payRequest.payrixMerchantID = sharedUtils.getMerchantID(this);
        payRequest.payrixSandoxDemoMode = true;
        payRequest.order = String.valueOf(System.currentTimeMillis());// Added for test purpose

        updateLog("Integer Vals in PayRequest- Amount: " + payRequest.payTotalAmt);
        updateLog("Integer Vals in PayRequest- TaxAmt: " + payRequest.payTaxAmt);
        updateLog("Integer Vals in PayRequest- TipAmt: " + payRequest.payTipAmt);

        payRequest.useTxnSessionKey = sharedUtils.getUseTxnSession(this);
        if (sharedUtils.getUseTxnSession(this)) {
            payRequest.txnSessionKey = sharedUtils.getSessionKey(this);
        } else {
            payRequest.paySessionKey = sharedUtils.getSessionKey(this);
        }

        doWriteToLog("Sale-Request", payRequest, null);
        payrixSDK.doPaymentTransaction(payRequest);
    }


    private void doWriteToLog(String logType, PayRequest payRequest, PayResponse payResponse)
    {
        String pattern = "yy-MM-dd_HHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        String useTimeStamp = simpleDateFormat.format(new Date());

        String fName = "Log_" + useTimeStamp + "_" + logType;

        String logData = sharedUtils.doGenLogString(payRequest, payResponse);
        sharedUtils.doWriteLogFile(this, fName, logData);
    }


    /**
     **doManualTxn**
     * (Step 5b)
     Prepare the transaction for processing using the manually entered card information.
     */
    private void doManualTxn() {
        mCurrentTransaction.ccName = mCardHolder.getText().toString();
        mCurrentTransaction.ccEXP = mExpMM.getText().toString() + mExpYY.getText().toString();
        mCurrentTransaction.ccCVV = mCVV.getText().toString();
        mCurrentTransaction.zip = mZip.getText().toString();
        mCurrentTransaction.ccNumber = mTxtCardNum.getText().toString();

        String theCardType = sharedUtils.determineCardType(mCurrentTransaction.ccNumber);
        mCurrentTransaction.ccCardType = determineCardValue(theCardType);

        doProcessManualCard();
    }


    /**
     **doProcessManualCard**
     * (Step 6b)
     This method prepares and starts the manual card processing using the
     PayCoreMaster method: doManualCardTransaction

     The callback is: didReceiveTransactionResponse
     */
    private void doProcessManualCard()
    {
        Float m100 = 100.00f;
        PayRequest payRequest = PayRequest.getInstance();
        PaySharedAttributes.CCType useCardType;
        if (mCurrentTransaction.ccCardType != null) {
            useCardType = mCurrentTransaction.ccCardType; // PaySharedAttributes.CCType(rawValue: (useCCtype.rawValue))
        } else {
            useCardType = null;
        }

        Float useTaxPcnt = 0.00f;
        if (mCurrentTransaction.taxPercentage != null) {
            useTaxPcnt = mCurrentTransaction.taxPercentage.floatValue() / m100;
        }

        mCurrentTransaction.receiptEMVChipInd = "Manual Entry";

        String sessionKey = sharedUtils.getSessionKey(this);

        Float bdAmount = mCurrentTransaction.amount.floatValue();
        Float bdTaxPcnt = mCurrentTransaction.taxPercentage.floatValue();
        Float bdTipAbs = mCurrentTransaction.tipAbsoluteAmount.floatValue();

        updateLog("Big Dec Amount: " + bdAmount);
        updateLog("Big Dec TaxPcnt: " + bdTaxPcnt);
        updateLog("Big Dec TipAmt: " + bdTipAbs);

        Float calcTax = bdAmount * bdTaxPcnt;
        Float calcTotal = bdAmount + calcTax + bdTipAbs;

        updateLog("Big Dec TaxAmt: " + calcTax);
        updateLog("Big Dec Total: " + calcTotal);

        int useVal = Math.round(calcTotal * m100);
        payRequest.payTotalAmt = useVal;  // .intValue();

        useVal = Math.round(calcTax * m100);
        payRequest.payTaxAmt = useVal; //.intValue();

        useVal = Math.round(bdTipAbs * m100);
        payRequest.payTipAmt = useVal; //.intValue();

        useVal = Math.round(bdAmount * m100);
        payRequest.payAmount = useVal; //.intValue();

        payRequest.payrixSandoxDemoMode = true;
        payRequest.payHostURL = sharedUtils.getURL(this, "");
        payRequest.payCurrencyCode = "USD";
        payRequest.payrixMerchantID = sharedUtils.getMerchantID(this);//mCurrentTransaction.merchantID;


        useVal = Math.round(bdTaxPcnt * m100);
        payRequest.payTaxPercent = useVal; // .intValue();
        payRequest.payTipPercent = mCurrentTransaction.tipPercentage;

        payRequest.payManualEntry = true;
        payRequest.payCardHolder = mCurrentTransaction.ccName;
        payRequest.payCCNumber = mCurrentTransaction.ccNumber;
        payRequest.payCardType = useCardType;
        payRequest.payCardCVV = mCurrentTransaction.ccCVV;
        payRequest.payCardExp = mCurrentTransaction.ccEXP;
        payRequest.payOrigin = PaySharedAttributes.PayTxnOrigin.eCommerceSystem;
        payRequest.payCardZip = mCurrentTransaction.zip;
        payRequest.payDeviceMode = PaySharedAttributes.PayDeviceMode.cardDeviceMode_Unknown;
        payRequest.order = String.valueOf(System.currentTimeMillis()); // Added for test purpose
        payRequest.useTxnSessionKey = sharedUtils.getUseTxnSession(this);
        if (sharedUtils.getUseTxnSession(this)) {
            payRequest.txnSessionKey = sessionKey;
        } else {
            payRequest.paySessionKey = sessionKey;
        }

        updateLog("Integer Vals in PayRequest- Amount: " + payRequest.payTotalAmt);
        updateLog("Integer Vals in PayRequest- TaxAmt: " + payRequest.payTaxAmt);
        updateLog("Integer Vals in PayRequest- TipAmt: " + payRequest.payTipAmt);

        doWriteToLog("Manual-Request",payRequest,null);

        payrixSDK.doPaymentTransaction(payRequest);
    }


    @Override
    public void didReceivePayResults(Integer responseType, String actionMsg, String infoMsg, PayResponse payResponse)
    {
        // ResponseTypes: 1 = Card Action Message   | App Should immediately display to user to do that action
        //                2 = Info Message          | App Should Display the informative message, but not required
        //                3 = PIN Entry Required    | App should Display Field for PIN Entry
        //                4 = App Selection Needed  | App should Display List of Apps to Display
        //                5 = Send Final EMV Data   | App should Catch and use EMV Data as desired
        //                9 = Error Occurred        | App Should Display the error and end processing the transaction
        //                0 = Transaction Complete  | The transaction ended and the PayResponse object contains the
        //                                            completed transaction data.

        progressBar.setVisibility(View.GONE);
        String useMsg = "";

        switch (responseType)
        {
            case 0:
                // Transaction Complete
                doWriteToLog("Sale-Response", null, payResponse);
                sharedUtils.showMessage(this, "Pay Results", "Transaction Complete - " + payResponse.receiptApprovedDeclined + " -" + infoMsg);

                useMsg = "Transaction Complete - \n" + payResponse.receiptApprovedDeclined + "\n" + infoMsg;
                updateLog(useMsg);

                if (payResponse.payTxn != null && payResponse.payTxn.getOrder() != null) {
                    updateLog(String.format("Order no: %s",payResponse.payTxn.getOrder()));
                }

                if (payResponse.ccName != null) {
                    updateLog(String.format("CCName: %s", payResponse.ccName));
                }

                String paymentStatus = payResponse.receiptApprovedDeclined;
                // verifyResponse(payResponse: usePayResponse, isForReceipt: true)

                mLblActionMsg.setText("... Transaction Completed: " + paymentStatus + " ...");

                String debugLog = "";
                if (payResponse.debugSDKData != null) {
                    debugLog = payResponse.debugSDKData.toString();
                } else {
                    debugLog = "No Log Data";
                }

                if ((actionMsg.equalsIgnoreCase("declined")) || (actionMsg.equalsIgnoreCase("approved")))
                {
                    mTBReceiptBtn.setVisibility(View.VISIBLE);
                } else {
                    mTBReceiptBtn.setVisibility(View.INVISIBLE);
                }

                updateLog(debugLog);
                doPayrixLog(debugLog);
                doSleep(2000);
                mLblActionMsg.setText("...");
                break;
            case 1:
                // Take Action

                mLblActionMsg.setText(actionMsg);
                useMsg = "Action Message: " + actionMsg;
                if (actionMsg.equalsIgnoreCase("CANCEL")) {
                    cancelBtn.setVisibility(View.GONE);
                    useMsg = "Action Message: " + infoMsg;
                    mLblTxnLog.setText("");
                }
                updateLog(useMsg);
                //paymentStatus = actionMsg;
                doPayrixLog(useMsg);
                doSleep(1500);  // 1000 = 1,000 millisec = 1 sec.
                break;
            case 2:
                // Information Message

                mLblActionMsg.setText("");
                useMsg = "Info Message: " + infoMsg + " - " + actionMsg;
                updateLog(useMsg);
                doPayrixLog(useMsg);
                doSleep(1500);
                break;
            case 3:
                // PIN Entry Required
                doSleep(3000);
                useMsg = "PIN Entry NOT SUPPORTED BY PAYRIX SDK";
                mLblActionMsg.setText(useMsg);
                updateLog(useMsg);
                doPayrixLog(useMsg);
                break;
            case 4:
                // App Selection Needed
                useMsg = "App Selection Needed: ";
                mLblActionMsg.setText(useMsg);
                updateLog(useMsg);
                doPayrixLog(useMsg);
                doAppSelection(payResponse);
                break;
            case 5:
                // Final EMV Batch Data Sent
                doWriteToLog("Sale-Final EMV Data", null, payResponse);
                useMsg = "EMV Data Received";
                mLblActionMsg.setText(useMsg);
                updateLog(useMsg);
                doPayrixLog(useMsg);
                break;
            case 9:
                // Take Action
                String unexpectedMsg = actionMsg + "\n" + infoMsg;
                sharedUtils.showMessage(this, "Action Message", unexpectedMsg);
                break;

            case 99:
                // Simulation of App Selection Complete
                sharedUtils.showMessage(this, "Simulation Completed", "App Selection - Simulation Completed");
                paymentStatus = infoMsg;
                break;

            default:
                sharedUtils.showMessage(this, "Info Message", infoMsg);
                break;
        }
    }


    private void doAppSelection(PayResponse response)
    {
        ArrayList<Hashtable<String, Integer>> appsSelectionArray = response.appSelection;
        int chkBoxOffResID = 0;
        EMVCardApplications theAppSelections = EMVCardApplications.getInstance();
        theAppSelections.theCardEMVAppList = new ArrayList<>();

        if (!appsSelectionArray.isEmpty()) {
            int ctr = 0;
            int cnt = appsSelectionArray.size();

            while (ctr < cnt) {
                EMVCardAppObj aCardAppObj = new EMVCardAppObj();
                Hashtable<String, Integer> anAppRec = appsSelectionArray.get(ctr);
                Iterator<Map.Entry<String, Integer>> itr = anAppRec.entrySet().iterator();

                Map.Entry<String, Integer> entry = null;
                while(itr.hasNext()) {
                    entry = itr.next();
                    aCardAppObj.setCardAppID(entry.getValue());
                    aCardAppObj.setCardAppName(entry.getKey());
                    aCardAppObj.setCurrentSelection(false);
                    theAppSelections.theCardEMVAppList.add(aCardAppObj);
                }
                ctr = ctr + 1;
            }

            Intent appSelIntent = new Intent(this, DemoAppSelection.class);
            // appSelIntent.putExtra("passedRequestType", 5); // Show Tip Screen and Continue To Card Processing
            startActivityForResult(appSelIntent,5);
        }
        else
        {
            sharedUtils.showMessage(this, "Demo Transaction", "No Apps provided for App Selection");
        }
    }

    @Override
    public void didReceiveRefundResults(Boolean success, Integer responseCode, String refundMsg, RefundResponse refundResponse)
    {
        if (success)
        {
            if (responseCode == 7)
            {
                // Successful Device Reversal
                sharedUtils.showMessage(this, "Device Reversal Request", "Device Reversal Request SUCCEEDED!");
                mLblActionMsg.setText("");
                String useMsg = "Successful Device Reversal";
                updateLog(useMsg);
                doPayrixLog(useMsg);
            }
        }
        else
        {
            if (responseCode == 6)
            {
                // Device Reversal Request Failed
                sharedUtils.showMessage(this, "Device Reversal Request", "Device Reversal Request FAILED!");
                mLblActionMsg.setText("");
                String useMsg = "Device Reversal Request FAILED: " + refundMsg;
                updateLog(useMsg);
                doPayrixLog(useMsg);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 5)
        {
            // Tax Results
            if (resultCode == RESULT_OK)
            {
                // Updated Tax Information Returned
                Intent appIntent = data;
                int appID = (int)appIntent.getSerializableExtra("returnedAppSelection");

                //Send the Selected App ID to the bbPOS Reader
                payrixSDK.doProcessAppSelection(appID, "Name Not Required");
            }
        }
    }


    private void doPayrixLog(String logData)
    {
        Log.i(demoTag, logData);
    }

    private void doSleep(int delayTime)
    {
        try
        {
            //set time in miliseconds
            Thread.sleep(delayTime);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String doDumpDebugInfo(Hashtable<String, String> debugLog)
    {
        String dumpedLog = "Payrix Debug Log: \n";
        Iterator<Map.Entry<String, String>> itr = debugLog.entrySet().iterator();
        Map.Entry<String, String> entry = null;
        while(itr.hasNext())
        {
            entry = itr.next();
            dumpedLog = dumpedLog + "Key: " + entry.getKey() + " for Value: " + entry.getValue() + "\n";
        }

        return dumpedLog;
    }


    private PaySharedAttributes.CCType determineCardValue(String theCardType)
    {
        PaySharedAttributes.CCType useType;

        switch (theCardType)
        {
            case "AMEX":
                useType = PaySharedAttributes.CCType.American_Express;
                break;
            case "VISA":
                useType = PaySharedAttributes.CCType.Visa;
                break;
            case "MASTERCARD":
                useType = PaySharedAttributes.CCType.MasterCard;
                break;
            case "DISCOVER":
                useType = PaySharedAttributes.CCType.Discover;
                break;
            default:
                useType = null;
                break;
        }
        return useType;
    }


    private void updateLog(String newEntry)
    {
        String theCurrentLog = mLblTxnLog.getText().toString();
        theCurrentLog = theCurrentLog + "\n" + newEntry;
        mLblTxnLog.setText(theCurrentLog);
    }


    private void doClearUI() {
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

        mCardEntry.setVisibility(View.GONE);
        mBtnStartTxn.setVisibility(View.VISIBLE);
    }


    public void didCompleteCancelRequest(boolean successful) {
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

    // Unused Optional PayrixSDK Callbacks

    public void didReceiveDeviceResults(Integer responseType,  String actionMsg, String infoMsg, String deviceResponse){}
    public void didReceiveBTDisconnectResults(Boolean disconnectSuccess){}
    public void didReceiveLoginResults(Boolean loginSuccess, String theSessionKey, List<PayMerchant> theMerchants, String theMessage){}
    public void didReceiveTxnResults(Boolean success, Integer responseCode, String txnMsg, TxnDataResponse txnDataResponse){}

    @Override
    public void didReceiveTxnKeyResult(boolean b, TxnSession txnSession, String s) {

    }


    @Override
    public void didReceiveRefundEligibleStatus(Boolean aBoolean) {

    }

    @Override
    public void didReceiveDeviceData(Hashtable<String, String> hashtable) {

    }

}
