package com.payrix.payrixsdkdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.payrix.payrixsdk.PayDevice;
import com.payrix.payrixsdk.PayMerchant;
import com.payrix.payrixsdk.PayResponse;
import com.payrix.payrixsdk.PaySharedAttributes;
import com.payrix.payrixsdk.PayrixSDK;
import com.payrix.payrixsdk.PayrixSDKCallbacks;
import com.payrix.payrixsdk.RefundResponse;
import com.payrix.payrixsdk.TxnDataResponse;
import com.payrix.payrixsdk.paycore.payrixcore.TxnSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class DemoScanBT extends AppCompatActivity implements PayrixSDKCallbacks
{
    Button mBtnScan;
    TextView mLblSelectedReader;
    TextView mLblFoundDevices;
    Toolbar toolbar;
    static final int PERMISSION_REQUEST_CODE = 200;
    Set<String> setOfDevices;

    ProgressBar mPBBTScan;
    
    SharedUtilities sharedUtils = SharedUtilities.getInstance();

    int mScanSuccess;
    Integer permissionCtr;
    
    PayrixSDK payrixSDK = PayrixSDK.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_scanbt);

        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        setSupportActionBar(toolbar);

        mBtnScan = findViewById(R.id.btnScan);
        mLblSelectedReader = findViewById(R.id.lblSelectedReader);
        mLblFoundDevices = findViewById(R.id.lblFoundDevices);
        mPBBTScan = findViewById(R.id.pbBTScan);

        toolbarTitle.setText("Card Reader Scan");

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
                setResult(mScanSuccess, intent);
                finish();
            }
        });

        mPBBTScan.setVisibility(View.INVISIBLE);
        mBtnScan.setVisibility(View.INVISIBLE);

        mScanSuccess = 99;
    
        PaySharedAttributes.PaySupportedReaders useManfg = doDetermineManfg();
        Boolean isSandBox = sharedUtils.getSandBoxOn(this);
        String theEnv =  sharedUtils.getEnvSelection(this);
        payrixSDK.doSetPayrixPlatform(this, theEnv, isSandBox, useManfg);
    
        sharedUtils.setDemoMode(this, isSandBox);
        
        checkPermissions();
    }
    
    
    /**
     * checkPermissions
     * This method verifies the necessary Bluetooth and other permissions have been
     * granted by the user.
     */
    private void checkPermissions()
    {
        if (Build.VERSION.SDK_INT >= 31) {
            //(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                    || (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                    || (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
                    || (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

                // Permission is not granted
                String[] permissionList = new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(this, permissionList, PERMISSION_REQUEST_CODE);
            } else {
                mBtnScan.setVisibility(View.VISIBLE);
            }
        } else {
            //(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
                    || (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
                    || (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    || (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    || (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)) {

                // Permission is not granted
                String[] permissionList = new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE};
                ActivityCompat.requestPermissions(this, permissionList, PERMISSION_REQUEST_CODE);
            } else {
                mBtnScan.setVisibility(View.VISIBLE);
            }
        }

    }

    
    
    //@Override
    @SuppressLint("Override")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200)
        {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // permission was granted, do your work....
                mBtnScan.setVisibility(View.VISIBLE);
            }
            else
            {
                // permission denied
                mBtnScan.setVisibility(View.INVISIBLE);
                sharedUtils.showMessage(this, "Card Reader Scan",
                        "You have denied access to Bluetooth services. A Bluetooth reader cannot be used until you enable using Settings");
            }
        }
    }
    
    protected boolean checkBluetoothPermission() {
        //Android 12 and above
        if (Build.VERSION.SDK_INT >= 31) {
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED);
        } else {
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED);
        }
    }
    
    private PaySharedAttributes.PaySupportedReaders doDetermineManfg()
    {
        return PaySharedAttributes.PaySupportedReaders.reader_BBPOS;
    }

    /**
     * goScanForReaders (Step 3) Listens for the Scan for Readers button to be tapped.
     * the PayCardReader class handles BT related activities.
     * Here the method: scanForReaders is invoked to start the BT scanning (search) process
     * for eligible BT Card Readers.
     * The results are returned in either Callback: didFindRDRDevices, didReceiveBTScanTimeOut, or didReceiveCardReaderError.
     *
     * @param view  the view for the button passed from the UI
     */
    public void goScanForReaders(View view)
    {
        if (checkBluetoothPermission())
        {
            mPBBTScan.setVisibility(View.VISIBLE);
            setOfDevices = new HashSet<>();
            payrixSDK.doScanForBTReaders();
        }
        else
        {
            sharedUtils.showMessage(this, "Card Reader Scan",
                    "You have denied access to Bluetooth services. A Bluetooth reader cannot be used until you enable using Settings");
        }
    }
    
    public void didReceiveScanResults(Boolean scanSuccess, String scanMsg, ArrayList<PayDevice> payDevices)
    {
        mPBBTScan.setVisibility(View.GONE);
        if (!scanSuccess) {
            sharedUtils.showMessage(this, "Error", scanMsg);
            return;
        }

        int devCnt = payDevices.size();
        int ctr = 0;
        String showReaders = "Located BT Readers:";
        
        if (devCnt >= 1)
        {
            while (ctr < devCnt)
            {
                PayDevice aDevice = payDevices.get(ctr);
                String devName = aDevice.deviceSerial;
                
                if (!setOfDevices.contains(devName))
                {
                    // Device not previously located
                    setOfDevices.add(devName);
                    if (ctr == 0)
                    {
                        mLblSelectedReader.setText(devName);
                        sharedUtils.setBTReader(this, devName);
                        sharedUtils.setBTManfg(this,"BBPOS");
                        mScanSuccess = RESULT_OK;
                    }
                    showReaders = showReaders + "\n - " + devName;
                }
                ctr = ctr + 1;
            }
        }
        mPBBTScan.setVisibility(View.INVISIBLE);
        mPBBTScan.setVisibility(View.GONE);
        
        if (setOfDevices.size() > 0)
        {
            mLblFoundDevices.setText(showReaders);
        }
        else
        {
            sharedUtils.showMessage(this,"Settings" ,"No New Bluetooth Readers were located");
        }
    }
    
    
    // Unused Optional PayrixSDK Callbacks
    
    public void didReceiveDeviceResults(Integer responseType,  String actionMsg, String infoMsg, String deviceResponse){}
    public void didReceiveBTConnectResults(Boolean connectSuccess, String theDevice){}
    public void didReceiveBTDisconnectResults(Boolean disconnectSuccess){}
    public void didReceiveLoginResults(Boolean loginSuccess, String theSessionKey, List<PayMerchant> theMerchants, String theMessage){}
    public void didReceivePayResults(Integer responseType, String actionMsg, String infoMsg, PayResponse payResponse){}
    public void didReceiveRefundResults(Boolean success, Integer responseCode, String refundMsg, RefundResponse refundResponse){}
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
