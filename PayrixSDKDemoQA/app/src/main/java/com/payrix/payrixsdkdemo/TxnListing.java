package com.payrix.payrixsdkdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.payrix.payrixsdk.PayDevice;
import com.payrix.payrixsdk.PayMerchant;
import com.payrix.payrixsdk.PayResponse;
import com.payrix.payrixsdk.PayrixSDK;
import com.payrix.payrixsdk.PayrixSDKCallbacks;
import com.payrix.payrixsdk.RefundResponse;
import com.payrix.payrixsdk.TxnDataRequest;
import com.payrix.payrixsdk.TxnDataResponse;
import com.payrix.payrixsdk.paycore.paycoremobile.PayCoreTxn;
import com.payrix.payrixsdk.paycore.payrixcore.TxnSession;
import com.payrix.payrixsdk.paycore.payrixcore.Txns;
import com.payrix.payrixsdk.paycore.payrixcore.TxnsPayment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public class TxnListing extends AppCompatActivity implements PayrixSDKCallbacks, SwipeRefreshLayout.OnRefreshListener, TxnListingAdapter.TxnListingOnClick {
    private int mPagesLoaded = 0;
    private int mTotalPages = 0;
    private int mCurrentPage = 0;
    private int mScrollPosition = 0;
    private boolean mIsLoading = false;
    private boolean mMorePagesAvailable = true;
    private ProgressBar progressBar;

    private List<Txns> mTxnData = new ArrayList<>();
    private RecyclerView mHistRecyclerView;
    private PayrixSDK payrixSDK = PayrixSDK.getInstance(this);
    private final SharedUtilities sharedUtils = SharedUtilities.getInstance();
    private final String TAG = getClass().getSimpleName();
    private TxnListingAdapter adapter;
    SwipeRefreshLayout mSwipeHistRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_listing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progress_bar);
        mHistRecyclerView = findViewById(R.id.recycler_view);
        mHistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TxnListingAdapter(mTxnData, this);
        //adapter.setClickListener(this);
        mHistRecyclerView.setAdapter(adapter);

        mSwipeHistRefreshLayout = findViewById(R.id.swipe_container);

        mSwipeHistRefreshLayout.setOnRefreshListener(this);
        mSwipeHistRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);



        toolbarTitle.setText(getString(R.string.payment_refund));

        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_black_24);
        toolbar.setElevation(10.0f);
        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });

        initScrollListener();
        loadNextPageOfTransactions();

        mSwipeHistRefreshLayout.setOnRefreshListener(this);
    }

    private void initScrollListener()
    {
        mHistRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!mIsLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == mTxnData.size() - 1)
                    {
                        //bottom of list!
                        loadNextPageOfTransactions();
                    }
                }
            }
        });
    }


    @Override
    protected void onResume() {
        payrixSDK = PayrixSDK.getInstance(this);
        mHistRecyclerView.invalidate();
        super.onResume();
    }


    public void loadNextPageOfTransactions() {
        if (mIsLoading) {
            return;
        } else {
            if (mMorePagesAvailable) {
                mIsLoading = true;
                progressBar.setVisibility(View.VISIBLE);

                mScrollPosition = mTxnData.size();
                int pageToLoad = mPagesLoaded + 1;
                retrieveTransactions(pageToLoad);  // Response is in Callback: didReceiveTransactionResponse
            }
        }
        return;
    }

    public void retrieveTransactions(int page) {
        String useMerchantID = sharedUtils.getMerchantID(this);
        int useRecordsPerPage = 20;
        String useSessionKey = sharedUtils.getSessionKey(this);

        TxnDataRequest dataRequest = TxnDataRequest.getInstance();
        if (sharedUtils.getUseTxnSession(this)) {
            dataRequest.useTxnSessionKey = true;
            dataRequest.paySessionKey = null;
            dataRequest.txnSessionKey = useSessionKey;
        } else {
            dataRequest.useTxnSessionKey = false;
            dataRequest.txnSessionKey = null;
            dataRequest.paySessionKey = useSessionKey;
        }


        dataRequest.pagination = useRecordsPerPage;
        dataRequest.currentPage = page;
        dataRequest.payrixMerchantID = useMerchantID;
        dataRequest.payrixSandoxDemoMode = sharedUtils.getDemoMode(this);
        dataRequest.requestType = 2;

        payrixSDK.doTransactionDataRequest(dataRequest);
    }


    private List<Txns> doSortTransactions(List<Txns> unSortedTxns) {
        Collections.sort(unSortedTxns, new SortByDate().reversed());
        return unSortedTxns;
    }

    private HistoryObj doBldHistoryObj(Txns fromTxn) {
        HistoryObj retHistObj = new HistoryObj();

        TxnsPayment usePayment = fromTxn.getPayment();
        if (usePayment != null) {
            retHistObj.setTransactionID(fromTxn.getId());
            retHistObj.setCardHolder(sharedUtils.bldFullName(fromTxn));
            retHistObj.setCardNumber(usePayment.getNumber());
            retHistObj.setCardType(usePayment.getMethod());
        }

        retHistObj.setTransactionDate(fromTxn.getCreated());
        retHistObj.setTransactionStatus(fromTxn.getStatus());
        retHistObj.setTransactionType(fromTxn.getType());
        retHistObj.setDescription(fromTxn.getDescription());

        retHistObj.setAuthorization(fromTxn.getAuthorization());
        retHistObj.setEntryMode(fromTxn.getEntryMode());
        retHistObj.setExpiration(fromTxn.getExpiration());
        retHistObj.setDescriptor(fromTxn.getDescriptor() == null ? "" : fromTxn.getDescriptor());
        retHistObj.setZip(fromTxn.getZip() == null ? "" : fromTxn.getZip());

        retHistObj.setOrigin(fromTxn.getOrigin());
        retHistObj.setSwiped(fromTxn.getSwiped());
        retHistObj.setCurrency(fromTxn.getCurrency());
        retHistObj.setAllowPartial(fromTxn.getAllowPartial());
        retHistObj.setSignature(fromTxn.getSignature());

        Double totAmt = 0.00;

        Integer theTotalAmt = fromTxn.getTotal();

        totAmt = theTotalAmt.doubleValue();
        retHistObj.setTotalAmt(totAmt);

        Integer useTax = 0;
        if (fromTxn.getType() != 5) {
            if (fromTxn.getTax() == null) {
                useTax = 0;
            } else {
                useTax = fromTxn.getTax();
            }
            retHistObj.setTaxAmt(useTax.doubleValue());

            if (fromTxn.getStatus() != PayCoreTxn.PayCoreTxnStatus.failed.getValue()) {
                Integer useApproved = fromTxn.getApproved();
                if (useApproved == null) {
                    useApproved = 0;
                }
                retHistObj.setApprovedAmt(useApproved.doubleValue());
            }
        }


        Integer useRefunded = fromTxn.getRefunded();
        if (useRefunded == null)
        {
            useRefunded = 0;
        }
        retHistObj.setRefundedAmt(useRefunded.doubleValue());

        /*if (fromTxn.getFortxn() != null) {
            retHistObj.setRefundedAmt(fromTxn.getFortxn().getRefunded().doubleValue());
        }*/

        Double useSaleAmt = totAmt - useTax.doubleValue();

        retHistObj.setSaleAmt(useSaleAmt);

        return  retHistObj;
    }


    @Override
    public void onClickItem(Txns passTxn) {
        HistoryObj passingHistoryObj;
        passingHistoryObj = doBldHistoryObj(passTxn);

        Intent historyIntent = new Intent(this, TxnDetails.class);
        historyIntent.putExtra("passedHistoryObj", passingHistoryObj);
        startActivity(historyIntent);
    }


    private class SortByDate implements Comparator<Txns> {
        public int compare(Txns a, Txns b)
        {
            return  a.getCreated().compareTo(b.getCreated());
        }
    }



    @Override
    public void didReceiveDeviceResults(Integer integer, String s, String s1, String s2) {}

    @Override
    public void didReceiveScanResults(Boolean aBoolean, String s, ArrayList<PayDevice> arrayList) {}

    @Override
    public void didReceiveBTConnectResults(Boolean aBoolean, String s) {}

    @Override
    public void didReceiveBTDisconnectResults(Boolean aBoolean) {}

    @Override
    public void didReceiveLoginResults(Boolean aBoolean, String s, List<PayMerchant> list, String s1) {}

    @Override
    public void didReceivePayResults(Integer integer, String s, String s1, PayResponse payResponse) {}

    @Override
    public void didReceiveRefundResults(Boolean aBoolean, Integer integer, String s, RefundResponse refundResponse) {}

    @Override
    public void didReceiveTxnResults(Boolean success, Integer integer, String s, TxnDataResponse txnDataResponse) {
        Log.d(TAG, "didReceiveTxnResults "+txnDataResponse);
        StringBuilder errorMsg = new StringBuilder();
        mIsLoading = false;

        if (txnDataResponse.requestErrors != null) {
            for (int ctr = 0; ctr < txnDataResponse.requestErrors.size(); ctr++) {
                errorMsg.append(txnDataResponse.requestErrors.get(ctr)).append("\n");
            }

            if (errorMsg.length() > 0) {
                sharedUtils.showMessage(this, "History", String.format("An Unexpected Error Occurred: %s", errorMsg));
            }
        } else {
            // Success Retrieving Transactions for History Display
            if (success) {
                mMorePagesAvailable = txnDataResponse.morePages;
                mCurrentPage = txnDataResponse.currentPage;
                mTotalPages = txnDataResponse.totalPages;
                mPagesLoaded = mCurrentPage;

                if (txnDataResponse.transactions != null) {
                    /*if (mLastHeader == null) {
                        mLastHeader = "";
                    }*/

                    //List<Txns> filteredTransactions = doFilterTransactions(txnDataResponse.transactions);
                    List<Txns> sortedTransactions = doSortTransactions(txnDataResponse.transactions);

                    if (sortedTransactions != null) {
                        String prevDate = "";
                        for (int ctr = 0; ctr < sortedTransactions.size(); ctr++) {
                            Txns filterDeclinedTxn = sortedTransactions.get(ctr);

                            try {
                                Txns txns = filterDeclinedTxn.getFortxn();

                                //2021-05-28 05:57:08.695
                                if (sortedTransactions.get(ctr).getCreated().contains("2021-05-28")) {
                                    System.out.println(String.format("JSON: %s", sortedTransactions.get(ctr)));
                                }
                            } catch (Exception e) {
                                // If forTxn is null this filters out refund and void transaction

                                String fullDateTime = sortedTransactions.get(ctr).getCreated();
                                String currDate = fullDateTime.substring(0, 10);
                                if ((!currDate.equalsIgnoreCase(prevDate)) /*&& (!mLastHeader.equals(currDate))*/) {
                                    // Date Changed - Create Header
                                    //mLastHeader = currDate;
                                    prevDate = currDate;
                                    Txns headerTXN = new Txns();
                                    headerTXN.setFirst(currDate);  // Save the Section Header Date in the 1st Name Field.
                                    //mTxnData.add(headerTXN);
                                }
                            }
                            mTxnData.add(sortedTransactions.get(ctr));
                        }
                    }

                    //mTxnData = mAllTransactions;

                    updateViews();
                }
            } else {
                // Success = False
                mMorePagesAvailable = false;
                Txns noTxn = new Txns();
                mTxnData.add(noTxn);
                updateViews();
            }

        }
    }

    private void updateViews() {
        mSwipeHistRefreshLayout.setRefreshing(false);
        mIsLoading = false;

        progressBar.setVisibility(View.GONE);
        adapter.notifyItemInserted(mTxnData.size());
        //adapter.notifyDataSetChanged();
    }

    @Override
    public void didReceiveTxnKeyResult(boolean b, TxnSession txnSession, String s) {

    }

    @Override
    public void didReceiveRefundEligibleStatus(Boolean aBoolean) {

    }

    @Override
    public void didReceiveDeviceData(Hashtable<String, String> hashtable) {

    }

    @Override
    public void onRefresh() {
        refreshAllData();
    }

    private void refreshAllData() {
        hideKeyboard();
        mPagesLoaded = 0;
        mTotalPages = 0;
        mCurrentPage = 0;
        mScrollPosition = 0;
        mIsLoading = false;
        mMorePagesAvailable = true;
        //mLastHeader = "";

        mSwipeHistRefreshLayout.setRefreshing(true);
        progressBar.setVisibility(View.GONE);

        mTxnData.clear();
        adapter.notifyDataSetChanged();
        loadNextPageOfTransactions();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



}