package com.payrix.payrixsdkdemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.payrix.payrixsdk.PayDevice;
import com.payrix.payrixsdk.PayMerchant;
import com.payrix.payrixsdk.PayResponse;
import com.payrix.payrixsdk.PaySharedAttributes;
import com.payrix.payrixsdk.PayrixSDK;
import com.payrix.payrixsdk.PayrixSDKCallbacks;
import com.payrix.payrixsdk.RefundRequest;
import com.payrix.payrixsdk.RefundResponse;
import com.payrix.payrixsdk.TxnDataRequest;
import com.payrix.payrixsdk.TxnDataResponse;
import com.payrix.payrixsdk.paycore.payrixcore.Merchants;
import com.payrix.payrixsdk.paycore.payrixcore.TxnSession;
import com.payrix.payrixsdk.paycore.payrixcore.Txns;
import com.payrix.payrixsdk.paycore.payrixcore.TxnsPayment;
import org.json.JSONObject;
import java.text.AttributedString;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;


public class TxnDetails extends AppCompatActivity implements PayrixSDKCallbacks {
    private final List<HistoryDetailObj> mHistDetailsData = new ArrayList<>();
    private TxnDetailsRecyclerAdapter mHistDetailsAdapter;
    private final String TAG = TxnDetails.class.getSimpleName();

    // Globals for Scene Management
    private TextView mLblTopTotal;
    private ImageView mImgTopCardType;
    private TextView mLblCardNumLast4;
    private TextView mLblCardHolder;
    private Button mBtnRefund, mBtnReverseAuth;
    private Button mBtnShareReceipt;
    private ProgressBar progressBar;
    SharedUtilities sharedUtils = SharedUtilities.getInstance();
    private final PayrixSDK payrixSDK = PayrixSDK.getInstance(this);
    //public final PayCoreMaster payCoreMaster = PayCoreMaster.getInstance(this);

    private HistoryObj mPassedTransaction;
    private Txns mOriginalTransaction;

    private Boolean mFoundOriginalTxn;

    private String mSessionKey;
    private String mMerchantID;
    private String mPassedTxnID;

    private Hashtable<String, String> receiptDetail;
    private List<String> receiptDetails;
    private List<String> receiptHTMLDetails;
    private String receiptShareSubject;
    private String receiptCardLast4;
    private Double totalRefundAmt = 0.0;
    private boolean isReverseAuthDone = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedUtils = SharedUtilities.getInstance();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Transaction Details");
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Transaction Details");

        mPassedTransaction = new HistoryObj();
        Intent intent = getIntent();
        mPassedTransaction = (HistoryObj) intent.getSerializableExtra("passedHistoryObj");

        receiptDetail = new Hashtable<>();
        receiptDetails = new ArrayList<>();
        receiptHTMLDetails = new ArrayList<>();
        receiptShareSubject = "";

        mSessionKey = sharedUtils.getSessionKey(this);
        mMerchantID = sharedUtils.getMerchantID(this);

        mFoundOriginalTxn = false;
        mPassedTxnID = mPassedTransaction.getTransactionID();
        Boolean isSandBox =  sharedUtils.getDemoMode(this);
        String theEnv =  sharedUtils.getEnvSelection(this);
        payrixSDK.doSetPayrixPlatform(this, theEnv, isSandBox, null/*useManfg*/);

        mLblTopTotal = findViewById(R.id.lblTopTotal_HistDetl);
        mImgTopCardType = findViewById(R.id.imgCardType_HistDetl);
        mLblCardNumLast4 = findViewById(R.id.lblLast4_HistDetl);
        mBtnRefund = findViewById(R.id.btnRefund_HistDetl);
        mBtnReverseAuth = findViewById(R.id.btnReverseAuth);
        mBtnShareReceipt = findViewById(R.id.btnShare_HistDetl);
        mLblCardHolder = findViewById(R.id.lblCardHolder_HistDetl);
        progressBar = findViewById(R.id.progressBar);

        RecyclerView histDetailsRecyclerView = findViewById(R.id.rvHistoryDetails);
        histDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mHistDetailsAdapter = new TxnDetailsRecyclerAdapter(this, mHistDetailsData);

        histDetailsRecyclerView.setAdapter(mHistDetailsAdapter);

        toolbar.setElevation(10.0f);
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_black_24);
        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });

        //Display transaction data gotten from previous screen
        if (mPassedTransaction != null) {
            mOriginalTransaction = getCurrentTxnData();
            doGetSubsequentTransactions();
            doAddHistDetailItem(mPassedTransaction, null);
            showInitialScene();
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
    }


    private void doAddHistDetailItem(HistoryObj historyObj, Txns txnObj) {
        int useObjectType = 0;
        if (historyObj != null)
        {
            String txnStatus = "";
            switch (historyObj.getTransactionStatus())
            {
                case 0:
                    txnStatus = "Pending";
                    break;
                case 1:
                    txnStatus = "Approved";
                    break;
                case 2:
                    txnStatus = "Failed";
                    break;
                case 3:
                    txnStatus = "Captured";
                    break;
                case 4:
                    txnStatus = "Settled";
                    break;
                case 5:
                    txnStatus = "Returned";
                    break;
                default:
                    txnStatus = "";
                    break;
            }

            if (historyObj.getTransactionType() == 1) // Sale
            {
                //useObjectType = 0;
                //HistoryDetailObj aNewDetailItem = new HistoryDetailObj(useObjectType);
                //mHistDetailsData.add(aNewDetailItem);

                useObjectType = 1;
                HistoryDetailObj aNewDetailItem = new HistoryDetailObj(useObjectType);

                aNewDetailItem.setDescriptor(historyObj.getDescriptor());
                aNewDetailItem.setZip(historyObj.getZip());

                //Former Standard
                aNewDetailItem.setTransactionID(historyObj.getTransactionID());
                aNewDetailItem.setTransactionType("Sale");
                aNewDetailItem.setTransactionStatus(txnStatus);
                aNewDetailItem.setAuthorization(historyObj.getAuthorization());
                aNewDetailItem.setTransactionDate(historyObj.getTransactionDate());


                aNewDetailItem.setEntryMode(historyObj.getEntryMode());
                aNewDetailItem.setExpiration(historyObj.getExpiration());
                aNewDetailItem.setCardNumber(historyObj.getCardNumber());
                aNewDetailItem.setSaleAmt((historyObj.getSaleAmt() / 100));
                aNewDetailItem.setTaxAmt((historyObj.getTaxAmt() / 100));
                aNewDetailItem.setTotalAmt((historyObj.getTotalAmt()  / 100));
                aNewDetailItem.setTxnDescription(historyObj.getDescription());



                mHistDetailsData.add(aNewDetailItem);

                String theMerchant = sharedUtils.getMerchantDBA(this);

                String theSaleAmt = String.format(Locale.getDefault(),"%1$,.2f", (historyObj.getSaleAmt() / 100));
                String theTaxAmt = String.format(Locale.getDefault(), "%1$,.2f", (historyObj.getTaxAmt() / 100));
                String theTotAmt = String.format(Locale.getDefault(), "%1$,.2f", (historyObj.getTotalAmt() / 100));

                String cardNumber = mPassedTransaction.getCardNumber();
                int numEnd = cardNumber.length();
                int numStart = numEnd - 4;
                receiptCardLast4 = cardNumber.substring(numStart);

                createReceiptSaleSection(theMerchant,
                        txnStatus,
                        historyObj.getTransactionDate(),
                        "Sale",
                        theSaleAmt,
                        theTaxAmt,
                        theTotAmt,
                        historyObj.getTransactionID(),
                        historyObj.getDescription());

            }
            else if ((historyObj.getTransactionType() == 2) || (historyObj.getTransactionType() == 3) ||
                    (historyObj.getTransactionType() == 4))
            {
                useObjectType = 3;

                String theTotAmt = String.format(Locale.getDefault(),"%1$,.2f", (historyObj.getTotalAmt() / 100));
                createReceiptOtherSection("Other", theTotAmt, historyObj.getTransactionID());
            }
            else if (historyObj.getTransactionType() == 5)  // Refund
            {
                useObjectType = 2;
                HistoryDetailObj aNewDetailItem = new HistoryDetailObj(useObjectType);

                aNewDetailItem.setRefundAmt((historyObj.getTotalAmt() / 100));
                aNewDetailItem.setTransactionType("Refund");
                aNewDetailItem.setRefundDate(historyObj.getTransactionDate());
                aNewDetailItem.setRefundTxnID(historyObj.getTransactionID());

                mHistDetailsData.add(aNewDetailItem);

                String theTotAmt = String.format(Locale.getDefault(),"%1$,.2f", (historyObj.getTotalAmt() / 100));
                createReceiptRefundSection(theTotAmt, historyObj.getTransactionDate(), historyObj.getTransactionID());
            }
        }
        else if (txnObj != null)
        {
            String txnStatus = "";
            switch (txnObj.getStatus())
            {
                case 0:
                    txnStatus = "Pending";
                    break;
                case 1:
                    txnStatus = "Approved";
                    break;
                case 2:
                    txnStatus = "Failed";
                    break;
                case 3:
                    txnStatus = "Captured";
                    break;
                case 4:
                    txnStatus = "Settled";
                    break;
                case 5:
                    txnStatus = "Returned";
                    break;
                default:
                    txnStatus = "";
                    break;
            }

            if (txnObj.getType() == 1) // Sale
            {
                useObjectType = 0;
                HistoryDetailObj aNewDetailItem = new HistoryDetailObj(useObjectType);

                aNewDetailItem.setDescriptor(txnObj.getDescriptor());
                aNewDetailItem.setZip(txnObj.getZip());
                aNewDetailItem.setTransactionID(txnObj.getID());
                aNewDetailItem.setTransactionType("Sale");
                aNewDetailItem.setTransactionStatus(txnStatus);
                aNewDetailItem.setTransactionDate(txnObj.getCreated());

                mHistDetailsData.add(aNewDetailItem);

                useObjectType = 1;
                aNewDetailItem = new HistoryDetailObj(useObjectType);

                Integer totAmt = txnObj.getTotal();
                Integer taxAmt = txnObj.getTax() == null ? 0 : txnObj.getTax();

                Double saleAmt = (totAmt.doubleValue() / 100) - (taxAmt.doubleValue() / 100);
                aNewDetailItem.setSaleAmt(saleAmt);

                aNewDetailItem.setTaxAmt((taxAmt.doubleValue() / 100));

                totAmt = txnObj.getTotal();
                aNewDetailItem.setTotalAmt((totAmt.doubleValue() / 100));

                aNewDetailItem.setTxnDescription(txnObj.getDescription());

                mHistDetailsData.add(aNewDetailItem);

                String theSaleAmt = String.format("%1$,.2f", saleAmt);
                String theTaxAmt = String.format("%1$,.2f", (taxAmt.doubleValue() / 100));
                String theTotAmt = String.format("%1$,.2f", (totAmt.doubleValue() / 100));
                String theMerchant = sharedUtils.getMerchantDBA(this);

                String cardNumber = mPassedTransaction.getCardNumber();
                int numEnd = cardNumber.length();
                int numStart = numEnd - 4;
                receiptCardLast4 = cardNumber.substring(numStart);

                createReceiptSaleSection(theMerchant,
                        txnStatus,
                        txnObj.getCreated(),
                        "Sale",
                        theSaleAmt,
                        theTaxAmt,
                        theTotAmt,
                        txnObj.getId(),
                        txnObj.getDescription());
            }
            else if ((txnObj.getType() == 2) || (txnObj.getType() == 3) ||
                    (txnObj.getType() == 4))
            {
                useObjectType = 0;
                HistoryDetailObj aNewDetailItem = new HistoryDetailObj(useObjectType);
                aNewDetailItem.setTransactionID(txnObj.getId());

                Integer totAmt = txnObj.getTotal();
                String theTotAmt = String.format("%1$,.2f", (totAmt.doubleValue() / 100));
                aNewDetailItem.setTxnDescription(String.format("Reverse-Auth Processed ($%s)", theTotAmt));
                mHistDetailsData.add(aNewDetailItem);


                totalRefundAmt += totAmt;
                isReverseAuthDone = txnObj.getType() == 4;

                useObjectType = 3;


                createReceiptOtherSection("Other", theTotAmt, txnObj.getId());
            }
            else if (txnObj.getType() == 5)  // Refund
            {
                useObjectType = 2;
                HistoryDetailObj aNewDetailItem = new HistoryDetailObj(useObjectType);

                Integer useAmt = txnObj.getTotal();

                aNewDetailItem.setRefundAmt((useAmt.doubleValue() / 100));
                aNewDetailItem.setTransactionType("Refund");
                aNewDetailItem.setRefundDate(txnObj.getCreated());
                aNewDetailItem.setRefundTxnID(txnObj.getId());

                totalRefundAmt += useAmt;
                mHistDetailsData.add(aNewDetailItem);

                String theTotAmt = String.format("%1$,.2f", (useAmt.doubleValue() / 100));

                createReceiptRefundSection(theTotAmt, txnObj.getCreated(), txnObj.getId());
            }
        }
    }


    private void showInitialScene() {
        mBtnRefund.setVisibility(View.INVISIBLE);

        doShowTransData();
        if (mFoundOriginalTxn) {
            doGetSubsequentTransactions();
        }
    }


    private void doShowTransData()
    {
        String cardHolder = mPassedTransaction.getCardHolder();
        mLblCardHolder.setText(cardHolder);

        Double dblTopAmount = mPassedTransaction.getTotalAmt();

        dblTopAmount = dblTopAmount / 100;

        String theTotalAmt = String.format(Locale.getDefault(), "%1$,.2f", dblTopAmount);

        if (mPassedTransaction.getTransactionType() == 5)
        {
            theTotalAmt = String.format("-($ %s)", theTotalAmt);
        }
        else
        {
            theTotalAmt = String.format("$ %s", theTotalAmt);
        }

        mLblTopTotal.setText(theTotalAmt);

        String cardNumber = mPassedTransaction.getCardNumber();
        int numEnd = cardNumber.length();
        int numStart = numEnd - 4;
        String cardLast4 = cardNumber.substring(numStart);
        mLblCardNumLast4.setText(cardLast4);

        /*PaySharedAttributes.CCType useCardType = sharedUtils.getPayCoreCCType(mPassedTransaction.getCardType());
        int imgResource = sharedUtils.getCardResource(useCardType);
        if (imgResource == 0) {
            mImgTopCardType.setImageResource(R.drawable.baseline_credit_card_black_48);
        } else {
            mImgTopCardType.setImageResource(imgResource);
        }*/
    }



    /**
     * goScanForReaders Listens for the Scan for Readers button to be tapped.
     *
     * @param view  the view for the button passed from the UI
     */

    public void goRefund(View view) {
        Log.d(TAG, String.format("Total Refund Done: %s", totalRefundAmt));
        showGetRefundAmt();
    }


    public void goReverseAuth(View view) {
        Double currAmt = mPassedTransaction.getApprovedAmt();

        Log.d(TAG, String.format("Total Refund Done: %s and Approved %s", totalRefundAmt, currAmt));
        if (totalRefundAmt <= 0) {
            if (currAmt != null) {
                processRefundRequest(currAmt, true);
            } else {
                sharedUtils.showMessage(this, "History Details", "You can't perform reverse auth on a failed or refunded transaction");
            }
        } else if (isReverseAuthDone) {
            sharedUtils.showMessage(this, "History Details", "Reverse auth already done on this transaction.");
        } else {
            sharedUtils.showMessage(this, "History Details", "Partial reversal is not allowed! Kindly use refund instead.");
        }
    }


    private void showGetRefundAmt() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.pwl_refund_dialog, null);

        final EditText enteredRefundAmt = dialogView.findViewById(R.id.txtRefundAmt_rfd);
        Button btnSaveRefund = dialogView.findViewById(R.id.btnSaveRefund_rfd);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel_rfd);
        TextView availRefundAmt = dialogView.findViewById(R.id.lblAvailAmt_rfd);

        Double currAmt = mPassedTransaction.getApprovedAmt();
        Double rfdAmt = mPassedTransaction.getRefundedAmt();
        Double rfdAvail = (currAmt / 100) - (rfdAmt / 100);
        String availRFDAmt = String.format("%1$,.2f", rfdAvail);
        availRefundAmt.setText(availRFDAmt);

        btnCancel.setOnClickListener(view -> dialogBuilder.dismiss());
        btnSaveRefund.setOnClickListener(view -> {
            if (enteredRefundAmt.getText() != null && !(enteredRefundAmt.getText().toString().equals("")) && !(enteredRefundAmt.getText().toString().isEmpty())) {
                double refundAmt = Double.parseDouble(enteredRefundAmt.getText().toString());
                if (refundAmt > 0.00) {
                    processRefundRequest((refundAmt * 100), false);
                }
            } else {
                mBtnRefund.setVisibility(View.VISIBLE);
            }
            dialogBuilder.dismiss();
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void processRefundRequest(Double refundAmt, boolean isReversal) {
        Log.d(TAG, String.format("REFUND AMOUNT %s", refundAmt));
        String theMerchantID = sharedUtils.getMerchantID(this);
        String theSessionID = sharedUtils.getSessionKey(this);

        if (theMerchantID == null || theSessionID == null) {
            sharedUtils.showMessage(this, "History Details", "Merchant and Session are needed to complete this transaction.");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            mBtnRefund.setEnabled(false);
            mBtnReverseAuth.setEnabled(false);

            RefundRequest refundRequest = RefundRequest.getInstance();
            refundRequest.payrixMerchantID = theMerchantID;
            refundRequest.originalTxn = mOriginalTransaction;
            refundRequest.paySessionKey = theSessionID;
            refundRequest.refundAmt = Math.toIntExact(Math.round(refundAmt));
            refundRequest.payrixSandoxDemoMode = sharedUtils.getDemoMode(this);
            refundRequest.originalTxnID = mPassedTxnID;
            if (isReversal) {
                refundRequest.requestType = 4;
                payrixSDK.doPaymentReversal(refundRequest);
            } else {
                refundRequest.requestType = 5;
                refundRequest.originalTxn = mOriginalTransaction;
                payrixSDK.doPaymentRefund(refundRequest);
            }
        }
    }


    private void doHandleRefundSuccess(Txns refundTransaction)
    {
        doAddHistDetailItem(null, refundTransaction);
        displayTransactionDetails();
        doCheckRefundEligible();
    }


    private void doHandleRefundErrors(ArrayList<String> theErrors)
    {
        String errorMsgs = "Errors During Refund Process: \n";

        for (int i = 0; i < theErrors.size(); i++)
        {
            errorMsgs = String.format("%s - %s%s", errorMsgs, theErrors.get(i), "\n");
        }

        sharedUtils.showMessage(this, "History Details", errorMsgs);
    }


    // Check if this transaction have subsequent transactions tied to it.
    private void doGetSubsequentTransactions() {

        String theMerchantID = sharedUtils.getMerchantID(this);
        String theSessionID = sharedUtils.getSessionKey(this);

        if ((theMerchantID == null) || (theMerchantID.equals("")) || (theSessionID == null) || (theSessionID.equals(""))) {
            sharedUtils.showMessage(this, "Transaction History", "Your session has expired.  Please Sign Out and Sign Back in and Retry.");
        } else {
            TxnDataRequest dataRequest = TxnDataRequest.getInstance();
            dataRequest.pagination = 20;
            dataRequest.currentPage = 0;
            dataRequest.payrixMerchantID = theMerchantID;
            //dataRequest.paySessionKey = theSessionID;
            dataRequest.useTxnSessionKey = sharedUtils.getUseTxnSession(this);
            if (sharedUtils.getUseTxnSession(this)) {
                dataRequest.txnSessionKey = sharedUtils.getSessionKey(this);
            } else {
                dataRequest.paySessionKey = sharedUtils.getSessionKey(this);
            }

            dataRequest.payrixTxnID = mPassedTxnID;
            dataRequest.totalPages = 0;
            dataRequest.payrixSandoxDemoMode = sharedUtils.getDemoMode(this);
            dataRequest.requestType = 3;
            dataRequest.txns = mOriginalTransaction;

            payrixSDK.doTransactionDataRequest(dataRequest);
        }
    }

    private Txns getCurrentTxnData() {
        Txns txns = new Txns();
        TxnsPayment usePayment = new TxnsPayment();
        usePayment.setNumber(mPassedTransaction.getCardNumber());
        usePayment.setMethod(mPassedTransaction.getCardType());

        txns.setPayment(usePayment);
        txns.setId(mPassedTransaction.getTransactionID());
        txns.setCreated(mPassedTransaction.getTransactionDate());
        txns.setStatus(mPassedTransaction.getTransactionStatus());
        txns.setType(mPassedTransaction.getTransactionType());
        txns.setDescription(mPassedTransaction.getDescription());
        txns.setTotal(mPassedTransaction.getTotalAmt().intValue());
        if (txns.getType() != 5) {
            txns.setTax(mPassedTransaction.getTaxAmt().intValue());
            if (mPassedTransaction.getApprovedAmt() != null) {
                txns.setApproved(mPassedTransaction.getApprovedAmt().intValue());
            }
        }

        int refund = totalRefundAmt == 0.0 ? mPassedTransaction.getRefundedAmt().intValue() : totalRefundAmt.intValue();
        txns.setRefunded(refund);



        txns.setOrigin(mPassedTransaction.getOrigin());
        txns.setSwiped(mPassedTransaction.getSwiped());
        txns.setCurrency(mPassedTransaction.getCurrency());
        txns.setAllowPartial(mPassedTransaction.getAllowPartial());
        txns.setSignature(mPassedTransaction.getSignature());
        txns.setAuthorization(mPassedTransaction.getAuthorization());
        txns.setEntryMode(mPassedTransaction.getEntryMode());

        //retHistObj.setCardHolder(sharedUtils.bldFullName(fromTxn));
        //Double useSaleAmt = totAmt.doubleValue() - useTax.doubleValue();
        //retHistObj.setSaleAmt(useSaleAmt);

        return txns;
    }


    private void doHandleRelatedTransSuccess(List<Txns> theTransactions)
    {
        for (int i = 0; i < theTransactions.size(); i++) {
            doAddHistDetailItem(null, theTransactions.get(i));
        }

        displayTransactionDetails();
    }


    private void displayTransactionDetails() {
        mHistDetailsAdapter.notifyItemInserted(mHistDetailsData.size());
        mHistDetailsAdapter.notifyDataSetChanged();
    }


    private void doCheckRefundEligible()
    {
        String theMerchantID = sharedUtils.getMerchantID(this);
        String theSessionID = sharedUtils.getSessionKey(this);
        if ((theMerchantID == null) || (theMerchantID.equals("")) || (theSessionID == null) || (theSessionID.equals(""))) {
            sharedUtils.showMessage(this, "Transaction History", "Your session has expired.  Please Sign Out and Sign Back in and Retry.");
        } else {
            TxnDataRequest dataRequest = TxnDataRequest.getInstance();
            dataRequest.payrixMerchantID = theMerchantID;
            //dataRequest.paySessionKey = theSessionID;
            dataRequest.useTxnSessionKey = sharedUtils.getUseTxnSession(this);
            if (sharedUtils.getUseTxnSession(this)) {
                dataRequest.txnSessionKey = sharedUtils.getSessionKey(this);
            } else {
                dataRequest.paySessionKey = sharedUtils.getSessionKey(this);
            }

            dataRequest.requestType = 4;
            dataRequest.payrixSandoxDemoMode = sharedUtils.getDemoMode(this);
            dataRequest.txns = getCurrentTxnData();

            payrixSDK.doTransactionDataRequest(dataRequest);
        }
    }


    private void doHandleRelatedTransErrors(ArrayList<String> theErrors)
    {
        if (theErrors != null)
        {
            String errMsgs = "The following errors occurred: \n";
            for (int i = 0; i < theErrors.size(); i++)
            {
                errMsgs = String.format("%s%s%s", errMsgs, theErrors.get(i), "\n");
            }
            sharedUtils.showMessage(this, "History Transactions", errMsgs);
        }
        else
        {
            sharedUtils.showMessage(this, "History Transactions", "An unexpected error occurred. Sign Out and In, and Retry.");
        }
    }


    // Methods to handle Receipt Creation and Sharing

    /**
     * goShareReceipt Listens for the Share Receipt button to be tapped.
     *
     * @param view  the view for the button passed from the UI
     */

    public void goShareReceipt(View view)
    {
        // Method to Share Receipt

        String useHTML = "";
        String useTEXT = "";

        useHTML = createReceiptHTMLHeader();

        for (int i = 0; i < receiptDetails.size(); i++)
        {
            useTEXT =String.format("%s%s", useTEXT, receiptDetails.get(i));
        }

        for (int i = 0; i < receiptHTMLDetails.size(); i++)
        {
            useHTML = String.format("%s%s", useHTML, receiptHTMLDetails.get(i));
        }

        useHTML = String.format("%s%s", useHTML, createReceiptHTMLTrailer(mPassedTxnID));


        AttributedString receiptATTR = new AttributedString(useTEXT);
        String receiptTEXT = sharedUtils.convertAttrbToString(receiptATTR);

        TextView receiptTEXTVIEW = new TextView(this);
        receiptTEXTVIEW.setText(receiptTEXT);

        jumpToShare(receiptTEXTVIEW.getText().toString(), useHTML, receiptShareSubject);

    }


    public void jumpToShare(String receiptText, String receiptHTML, String emailSubject)
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/*");  // shareIntent.setType("message/rfc822");  //  shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, receiptHTML);
        shareIntent.putExtra(Intent.EXTRA_TEXT, receiptText);
        startActivity(Intent.createChooser(shareIntent,"Share Selection"));
    }


    private String createReceiptHTMLHeader()
    {
        String builder = "<html>" +
                "\n" +
                "<head>" +
                "\n" +
                "<style>" +
                "\n" +
                "table {" +
                "\n" +
                "font-family: courier;" +
                "\n" +
                "border-collapse: collapse;" +
                "\n" +
                "width: 100%;" +
                "\n" +
                "}" +
                "\n" +
                "</style>" +
                "\n" +
                "</head>" +
                "\n" +
                "<body>" +
                "\n";

        return builder;
    }


    private String createReceiptHTMLTrailer(String forTxn)
    {
        String receiptHTML = "\n"+"</body>" + "\n"+"</html>";

        return receiptHTML;
    }



    private void createReceiptSaleSection (String sMerchant,
                                           String sStatus,
                                           String sDate,
                                           String sTranType,
                                           String sSaleAmt,
                                           String sTaxAmt,
                                           String sTotal,
                                           String sTransID,
                                           String sDesc)
    {
        // 1. Check if record exist in receiptDetail by transaction ID
        // 2. If exist, do nothing and return
        // 3. If does not exist, generate receipt string using input parameters
        // 4. Save the receipt string to receiptDetail dictionary using the transaction ID as the key.

        receiptShareSubject = String.format("Your Receipt From %s | Amt: %s  | %s", sMerchant, sTotal, sDate);

        String receiptCardType = sharedUtils.getCardName(sharedUtils.getPayCoreCCType(mOriginalTransaction.getPayment().getMethod()));

        if (receiptDetail.get(sTransID) == null) {
            // Sales Record Receipt Section does not exist, Create Sale Section of Receipt

            String aReceiptSaleSection = "";
            aReceiptSaleSection = "Transaction Details\n";
            aReceiptSaleSection = String.format("%s ---------------------\n\n", aReceiptSaleSection);

            StringBuilder receiptHTML = new StringBuilder();
            receiptHTML.append("<b>Transaction Details</b>");
            receiptHTML.append("<p>-----------------------------<br>");

            receiptHTML.append("<table>");
            receiptHTML.append("\n");
            receiptHTML.append("<tr>");

            Date tmpDate = new Date();
            String dateString = sDate;

            try
            {
                tmpDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).parse(sDate);
            }
            catch (ParseException e)
            {
                tmpDate = null;
            }

            if (tmpDate != null)
            {
                SimpleDateFormat txnDateFmt = new SimpleDateFormat("MM/dd/yy, hh:mm a", Locale.getDefault());
                dateString = txnDateFmt.format(tmpDate);
            }

            receiptHTML.append("<td>Merchant:</td> <td>");
            receiptHTML.append(sMerchant);
            receiptHTML.append("</td>");
            receiptHTML.append("</tr>");

            aReceiptSaleSection = String.format("%s%s%s%s", aReceiptSaleSection, "Merchant: \t\t", sMerchant, "\n");

            receiptHTML.append("<tr>");
            receiptHTML.append("<td>Card:</td> <td>");
            receiptHTML.append(receiptCardType);
            receiptHTML.append(" - ");
            receiptHTML.append(receiptCardLast4);
            receiptHTML.append("</td>");
            receiptHTML.append("</tr>");

            aReceiptSaleSection = String.format("%s%s", aReceiptSaleSection, "Card: \t\t\t");
            aReceiptSaleSection = String.format("%s%s%s%s%s", aReceiptSaleSection, receiptCardType, " - ", receiptCardLast4, "\n");

            receiptHTML.append("<tr>");
            receiptHTML.append("<td>Status:</td> <td>");
            receiptHTML.append(sStatus);
            receiptHTML.append("</td>");
            receiptHTML.append("</tr>");

            aReceiptSaleSection = String.format("%s%s%s%s", aReceiptSaleSection, "Status: \t\t", sStatus, "\n");

            receiptHTML.append("<tr>");
            receiptHTML.append("<td>Date:</td> <td>");
            receiptHTML.append(dateString);
            receiptHTML.append("</td>");
            receiptHTML.append("</tr>");

            aReceiptSaleSection = String.format("%s%s%s%s", aReceiptSaleSection, "Date: \t\t\t", dateString, "\n");

            receiptHTML.append("<tr>");
            receiptHTML.append("<td>Txn Type:</td> <td>");
            receiptHTML.append(sTranType);
            receiptHTML.append("</td>");
            receiptHTML.append("</tr>");

            aReceiptSaleSection = String.format("%s%s%s%s", aReceiptSaleSection, "Txn Type: \t\t", sTranType, "\n");

            receiptHTML.append("<tr>");
            receiptHTML.append("<td>Sale Amt:</td> <td>");
            receiptHTML.append(sSaleAmt);
            receiptHTML.append("</td>");
            receiptHTML.append("</tr>");

            aReceiptSaleSection = String.format("%s%s%s%s", aReceiptSaleSection, "Sale Amt: \t\t$ ", sSaleAmt, "\n");

            receiptHTML.append("<tr>");
            receiptHTML.append("<td>Tax:</td> <td>");
            receiptHTML.append(sTaxAmt);
            receiptHTML.append("</td>");
            receiptHTML.append("</tr>");

            aReceiptSaleSection = String.format("%s%s%s%s", aReceiptSaleSection, "Tax: \t\t\t$ ", sTaxAmt, "\n");

            receiptHTML.append("<tr>");
            receiptHTML.append("<td>Total:</td> <td>");
            receiptHTML.append(sTotal);
            receiptHTML.append("</td>");
            receiptHTML.append("</tr>");

            aReceiptSaleSection = String.format("%s%s%s%s", aReceiptSaleSection, "Total: \t\t\t$ ", sTotal, "\n");

            receiptHTML.append("</table>");
            receiptHTML.append("\n");

            if ((sDesc != "") && (sDesc != null))
            {
                aReceiptSaleSection = String.format("%s%s%s%s%s", aReceiptSaleSection, "Desription: \n", "  ", sDesc, "\n");
                receiptHTML.append("<p>Description:<br>");
                receiptHTML.append(sDesc);
                receiptHTML.append("\n");
            }

            receiptHTML.append("<p>Transaction ID:<br>  ");
            receiptHTML.append(sTransID);
            receiptHTML.append("<br>");

            aReceiptSaleSection = String.format("%s%s%s%s%s", aReceiptSaleSection, "ID:     ", "  ", sTransID, "\n");

            receiptDetail.put(sTransID, aReceiptSaleSection);
            receiptDetails.add(aReceiptSaleSection);
            receiptHTMLDetails.add(receiptHTML.toString());
        }
        else
        {
            // Sales Record Receipt Section does exist, so do Nothing and Return
            return;
        }
    }


    private void createReceiptRefundSection( String rRefundAmt,
                                             String rDate,
                                             String rRefundID)
    {
        // 1. Check if record exist in receiptDetail by Refund Transaction ID
        // 2. If exist, do nothing and return
        // 3. If does not exist, generate receipt string using input parameters
        // 4. Save the receipt string to receiptDetail dictionary using the refund transaction ID as the key.

        if (receiptDetail.get(rRefundID) == null)
        {
            // Refund Record Receipt Section does not exist, so Create Refund Section of Receipt

            String aReceiptRefundSection = "";


            Date tmpDate = new Date();
            String dateString = rDate;

            try
            {
                tmpDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(rDate);
            }
            catch (ParseException e)
            {
                tmpDate = null;
            }

            if (tmpDate != null)
            {
                SimpleDateFormat txnDateFmt = new SimpleDateFormat("MM/dd/yy, hh:mm a");
                dateString = txnDateFmt.format(tmpDate);
            }

            StringBuilder receiptHTML = new StringBuilder();
            receiptHTML.append("<b>---------- REFUND ----------</b>");
            receiptHTML.append("<table>");
            receiptHTML.append("\n");

            aReceiptRefundSection = String.format("%s%s",aReceiptRefundSection, "\n---------- REFUND ---------- \n");

            receiptHTML.append("<tr>");
            receiptHTML.append("<td>Amount:</td> <td>");
            receiptHTML.append(rRefundAmt);
            receiptHTML.append("</td>");
            receiptHTML.append("</tr>");

            aReceiptRefundSection = String.format("%s%s%s%s", aReceiptRefundSection, "Amount: \t$ ", rRefundAmt, "\n");

            receiptHTML.append("<tr>");
            receiptHTML.append("<td>Date:</td> <td>");
            receiptHTML.append(dateString);
            receiptHTML.append("</td>");
            receiptHTML.append("</tr>");

            receiptHTML.append("</table>");
            receiptHTML.append("\n");

            aReceiptRefundSection = String.format("%s%s%s%s", aReceiptRefundSection, "Date: \t\t", dateString, "\n");

            receiptHTML.append("<p>ID:<br>  ");
            receiptHTML.append(rRefundID);
            receiptHTML.append("<br>");

            aReceiptRefundSection = String.format("%s%s%s%s%s", aReceiptRefundSection, "ID:\n", "  ", rRefundID, "\n");

            receiptDetail.put(rRefundID, aReceiptRefundSection);
            receiptDetails.add(aReceiptRefundSection);
            receiptHTMLDetails.add(receiptHTML.toString());
        }
        else
        {
            // Refund Record Receipt Section does exist, so do Nothing and Return
            return;
        }
    }


    private void createReceiptOtherSection(String oOtherType,
                                           String oAmount,
                                           String oOtherID)
    {
        // 1. Check if record exist in receiptDetail by Other Transaction ID
        // 2. If exist, do nothing and return
        // 3. If does not exist, generate receipt string using input parameters
        // 4. Save the receipt string to receiptDetail dictionary using the Other transaction ID as the key.

        if (receiptDetail.get(oOtherID) == null)
        {
            // Other Type Record Receipt Section does not exist, so Create Other Section of Receipt
            String aReceiptOtherSection = "";

            StringBuilder receiptHTML = new StringBuilder();
            receiptHTML.append("<b>----------------------------</b>");
            aReceiptOtherSection = String.format("%s%s", aReceiptOtherSection, "\n---------------------------- \n");

            receiptHTML.append("<p>");
            receiptHTML.append(oOtherType);
            receiptHTML.append(": <br>");

            aReceiptOtherSection = String.format("%s%s%s", aReceiptOtherSection, oOtherType, ": \n");

            receiptHTML.append("<table>");
            receiptHTML.append("\n");

            receiptHTML.append("<tr>");
            receiptHTML.append("<td>Amount:</td> <td>");
            receiptHTML.append(oAmount);
            receiptHTML.append("</td>");
            receiptHTML.append("</tr>");

            aReceiptOtherSection = String.format("%s%s%s%s", aReceiptOtherSection, "Amount: $ \t\t\t", oAmount, "\n");

            receiptHTML.append("</table>");
            receiptHTML.append("\n");

            receiptHTML.append("<p>ID:<br>  ");
            receiptHTML.append(oOtherID);
            receiptHTML.append("<br>");

            aReceiptOtherSection = String.format("%s%s%s%s%s", aReceiptOtherSection, "ID: \t\t\t \n", "  ", oOtherID, "\n");

            receiptDetail.put(oOtherID, aReceiptOtherSection);
            receiptDetails.add(aReceiptOtherSection);
            receiptHTMLDetails.add(receiptHTML.toString());
        }
        else
        {
            // Other Type Record Receipt Section does exist, so do Nothing and Return
            return;
        }
    }

    @Override
    public void didReceiveDeviceResults(Integer integer, String s, String s1, String s2) {

    }

    @Override
    public void didReceiveScanResults(Boolean aBoolean, String s, ArrayList<PayDevice> arrayList) {

    }

    @Override
    public void didReceiveBTConnectResults(Boolean aBoolean, String s) {

    }

    @Override
    public void didReceiveBTDisconnectResults(Boolean aBoolean) {

    }

    @Override
    public void didReceiveLoginResults(Boolean aBoolean, String s, List<PayMerchant> list, String s1) {

    }

    @Override
    public void didReceivePayResults(Integer integer, String s, String s1, PayResponse payResponse) {

    }

    @Override
    public void didReceiveRefundResults(Boolean success, Integer responseCode, String refundMsg, RefundResponse refundResponse) {
        /*
         responseCode:
            * 0 = Transaction Complete  | Refund Request Completed Successfully
            * 1 = Refund Declined       | Refund Declined by Payrix Platform
            * 2 = Reversal Declined     | Reversal Declined by Payrix Platform
            * 3 = Not Refund Eligible   | The transaction does not meet the criteria for a Refund
            * 4 = Refund Eligible       | The transaction does meet the criteria for a Refund
            * 5 = Invalid Amt Requested | The amount requested is either zero or greater than amount available
            * 6 = Device RevAuth Failed | Device Reverse Auth Declined by Payrix Platform
            * 7 = Device RevAuth Success| Device Reverse Auth Approved
            * 9 = Unexpected Error      | An unexpected error occured
         */
        progressBar.setVisibility(View.GONE);
        mBtnRefund.setEnabled(true);
        mBtnReverseAuth.setEnabled(true);
        if (!success) {
            sharedUtils.showMessage(TxnDetails.this, "Transaction Receipt", refundMsg);
        } else {
            switch (responseCode) {
                case 0:
                    if (refundResponse != null && refundResponse.refundTxn != null) {
                        doHandleRefundSuccess(refundResponse.refundTxn);
                    } else {
                        sharedUtils.showMessage(TxnDetails.this, "Transaction Receipt", "");
                    }
                    break;
                case 1:
                    sharedUtils.showMessage(TxnDetails.this, "", "Refund Declined");
                    break;
                case 2:
                    Double refundAmt = mPassedTransaction.getApprovedAmt();
                    if (refundAmt > 0.00) {
                        processRefundRequest(refundAmt, false);
                    }
                    break;
                case 5:
                    sharedUtils.showMessage(TxnDetails.this, "", "The amount requested is either zero or greater than amount available");
                    break;
                case 7:
                    if (refundResponse != null && refundResponse.refundTxn != null) {
                        doHandleRefundSuccess(refundResponse.refundTxn);
                    }
                    sharedUtils.showMessage(TxnDetails.this, "Transaction Receipt", refundMsg);
                    break;
                default:
                    sharedUtils.showMessage(TxnDetails.this, "Transaction Receipt", refundMsg);
                    break;
            }
        }
    }

    // Todo later
    @Override
    public void didReceiveTxnResults(Boolean success, Integer responseCode, String error, TxnDataResponse txnDataResponse) {
        /*
         responseCode:
           *  1 = Retrieve Specific Txn;
           *  2 = Retrieve All Txns (for Merchant);
           *  3 = Retrieve Related (Subsequent) Transactions;
           *  4 = Check if Transaction is Refund Eligible
        */
        if (!success && error != null) {
            sharedUtils.showMessage(TxnDetails.this, "Transaction Receipt", error);
            return;
        }
        switch (responseCode) {
            case 3:
                if (success && txnDataResponse != null) {
                    doHandleRelatedTransSuccess(txnDataResponse.transactions);
                }

                doCheckRefundEligible();
                break;
            case 4:
                if (success && txnDataResponse != null && txnDataResponse.txnsRefundEligible) {
                    mBtnRefund.setVisibility(View.VISIBLE);
                } else {
                    mBtnRefund.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void didReceiveTxnKeyResult(boolean b, TxnSession txnSession, String s) {

    }

    @Override
    public void didReceiveRefundEligibleStatus(Boolean success) {
        if (success) {
            mBtnRefund.setVisibility(View.VISIBLE);
        } else {
            mBtnRefund.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void didReceiveDeviceData(Hashtable<String, String> hashtable) {

    }


    // OLD CALL BACK => TO BE CLEANED UP LATER




    public void didReceiveRefundResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors)
    {
        if (success)
        {
            doHandleRefundSuccess(theTransactions.get(0));
        }
        else
        {
            doHandleRefundErrors(theErrors);
        }
    }

    public void didReceiveSpecificTxnResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors)
    {
        if (success) {
            mFoundOriginalTxn = true;
            mOriginalTransaction = new Txns();
            mOriginalTransaction = theTransactions.get(0);
            doAddHistDetailItem(null, mOriginalTransaction);
        } else {
            mFoundOriginalTxn = false;
            mBtnRefund.setVisibility(View.INVISIBLE);
            doAddHistDetailItem(mPassedTransaction, null);
        }
        showInitialScene();
    }

    public void didReceiveSubsequentTxnResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors)
    {
        if (!success && (theErrors != null))
        {
            mBtnRefund.setVisibility(View.INVISIBLE);
            doHandleRelatedTransErrors(theErrors);
        }
        else
        {
            if ((theTransactions != null) && (mFoundOriginalTxn))
            {
                doHandleRelatedTransSuccess(theTransactions);
                doCheckRefundEligible();
            }
            else if (mFoundOriginalTxn)
            {
                displayTransactionDetails();
                doCheckRefundEligible();
            }
        }
    }

    public void didReceiveRevAuthResponse(Boolean aBoolean, List<Txns> list, JSONObject jsonObject, ArrayList<String> arrayList) { }
    public void didReceiveGeneralResponse(Boolean aBoolean, List<Txns> list, JSONObject jsonObject, ArrayList<String> arrayList) { }
    public void didReceiveLoginResponse(Boolean success, String sessionKey){}
    public void didReceiveMerchantIDInfo(Boolean aBoolean, List<Merchants> list, ArrayList<String> arrayList) { }
    public void didReceiveTransactionResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors){}


}