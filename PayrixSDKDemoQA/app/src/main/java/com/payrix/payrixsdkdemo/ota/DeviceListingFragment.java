package com.payrix.payrixsdkdemo.ota;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.payrix.payrixsdk.PayDevice;
import com.payrix.payrixsdk.PayMerchant;
import com.payrix.payrixsdk.PayResponse;
import com.payrix.payrixsdk.PaySharedAttributes;
import com.payrix.payrixsdk.PayrixOTA;
import com.payrix.payrixsdk.PayrixSDK;
import com.payrix.payrixsdk.PayrixSDKCallbacks;
import com.payrix.payrixsdk.RefundResponse;
import com.payrix.payrixsdk.TxnDataResponse;
import com.payrix.payrixsdk.paycore.payrixcore.TxnSession;
import com.payrix.payrixsdkdemo.R;
import com.payrix.payrixsdkdemo.SharedUtilities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class DeviceListingFragment extends Fragment implements PayrixSDKCallbacks, DeviceListingAdapter.OnDeviceListingListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private AppCompatTextView deviceStatusMessageView;
    private DeviceListingAdapter adapter;
    private List<PayDevice> payDeviceList;
    private final PayrixSDK payrixSDK = PayrixSDK.getInstance(this);
    private final SharedUtilities sharedUtils = SharedUtilities.getInstance();
    private OnDemoOTAListener mListener;
    private ProgressBar progressBar;
    private final String TAG = DeviceListingFragment.class.getSimpleName();


    public DeviceListingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_listing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = view.findViewById(R.id.refresh_swipe);
        deviceStatusMessageView = view.findViewById(R.id.device_status_message);
        recyclerView = view.findViewById(R.id.ota_device_listing);
        progressBar = view.findViewById(R.id.loading);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        payDeviceList = new ArrayList<>();
        adapter = new DeviceListingAdapter(payDeviceList, this);
        recyclerView.setAdapter(adapter);

        PaySharedAttributes.PaySupportedReaders useManfg = PaySharedAttributes.PaySupportedReaders.reader_BBPOS;
        Boolean isSandBox = sharedUtils.getSandBoxOn(requireContext());
        String theEnv = sharedUtils.getEnvSelection(requireContext());
        payrixSDK.doSetPayrixPlatform(requireContext(), theEnv, isSandBox, useManfg);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            scanForReaders();
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        scanForReaders();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDemoOTAListener) {
            mListener = (OnDemoOTAListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Scanning for all nearby device(s), but before scanning disconnect from already connected device by calling doDisconnectBTReader().
     *  Method {@link PayrixSDKCallbacks#didReceiveBTDisconnectResults(Boolean)} didReceiveBTDisconnectResults()} listens to {@link PayrixSDK#doDisconnectBTReader()}. After then, start the device search.
     */
    private void scanForReaders() {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        deviceStatusMessageView.setVisibility(View.VISIBLE);
        deviceStatusMessageView.setText(getString(R.string.searching_for_readers));
        payrixSDK.doDisconnectBTReader();
    }

    @Override
    public void didReceiveDeviceResults(Integer integer, String s, String s1, String s2) {

    }

    @Override
    public void didReceiveScanResults(Boolean success, String message, ArrayList<PayDevice> deviceList) {
        Log.d(TAG, "didReceiveScanResults: Size= "+deviceList.size());
        progressBar.setVisibility(View.GONE);
        if (success) {
            payDeviceList.clear();

            if (deviceList.size() == 0) {
                deviceStatusMessageView.setVisibility(View.VISIBLE);
                deviceStatusMessageView.setText(R.string.no_device_found_message);
                return;
            }

            payDeviceList.addAll(deviceList);
            deviceStatusMessageView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            adapter.notifyDataSetChanged();
        } else {
            sharedUtils.showMessage(requireContext(), "OTA Scan Results", message);
        }
    }

    /**
     * STEP 3
     * @param success - If connection is successful get the selected device information. Method {@link PayrixSDKCallbacks#didReceiveDeviceData(Hashtable)} gets called with the device info
     * @param message - If not successful return a message why it was unsuccessful
     */
    @Override
    public void didReceiveBTConnectResults(Boolean success, String message) {
        Log.d(TAG, "didReceiveBTConnectResults");
        if (success) {
            payrixSDK.doGetDeviceInfo();
        } else {
            progressBar.setVisibility(View.GONE);
            sharedUtils.showMessage(requireContext(), "OTA Message", "Device not connected. " + message);
        }
    }

    @Override
    public void didReceiveBTDisconnectResults(Boolean aBoolean) {
        Log.d(TAG, "didReceiveBTDisconnectResults");
        payrixSDK.doScanForBTReaders();
    }

    @Override
    public void didReceiveLoginResults(Boolean aBoolean, String s, List<PayMerchant> list, String s1) {

    }

    @Override
    public void didReceivePayResults(Integer integer, String s, String s1, PayResponse payResponse) {

    }

    @Override
    public void didReceiveRefundResults(Boolean aBoolean, Integer integer, String s, RefundResponse refundResponse) {

    }

    @Override
    public void didReceiveTxnResults(Boolean aBoolean, Integer integer, String s, TxnDataResponse txnDataResponse) {

    }

    @Override
    public void didReceiveTxnKeyResult(boolean b, TxnSession txnSession, String s) {

    }

    @Override
    public void didReceiveRefundEligibleStatus(Boolean aBoolean) {

    }

    /**
     * This listen and get the current device info.
     * Goto DemoOTA class to see {@link OnDemoOTAListener#gotoDeviceUpdate(Hashtable)} implementation
     * @param deviceInfo - Dictionary of device info
     */
    @Override
    public void didReceiveDeviceData(Hashtable<String, String> deviceInfo) {
        progressBar.setVisibility(View.GONE);
        Log.d(TAG, "didReceiveDeviceData: "+deviceInfo);
        mListener.gotoDeviceUpdate(deviceInfo);
    }

    /**
     * STEP 2
     * Select and connect to the device of your choice from the device list.
     * After a successful connection {@link PayrixSDKCallbacks#didReceiveBTConnectResults(Boolean, String)} gets called. Do STEP 3
     * @param payDevice - This is the device selected to connect too
     */
    @Override
    public void onDevice(PayDevice payDevice) {
        progressBar.setVisibility(View.VISIBLE);
        payrixSDK.doConnectBTReader(payDevice);
    }
}