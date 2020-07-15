package com.payrix.payrixsdkdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.payrix.paycardapilibrary.PayCardCallbacks;
import com.payrix.paycardapilibrary.PayCardRDRMgr;
import com.payrix.paycardapilibrary.PayCardSharedAttr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class DemoScanBT extends AppCompatActivity implements PayCardCallbacks
{
    Button mBtnScan;
    TextView mLblSelectedReader;
    TextView mLblFoundDevices;
    Toolbar toolbar;

    List<BluetoothDevice> mFoundDevices;
    Set<String> listableDevices;

    ProgressBar mPBBTScan;

    /**
     * Instantiate PayCardRDRMgr  (Step 1)
     * This is the 1st step of the Bluetooth scanning process.
     * PayCard handles communication with the Bluetooth reader device.
     */
    public final PayCardRDRMgr payCardReader = PayCardRDRMgr.getInstance(this);

    SharedUtilities sharedUtils = SharedUtilities.getInstance();

    int mScanSuccess;

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

        /*
         * Start PayCardRDRMgr  (Step 2)
         * This step establishes the necessary connections for Callback processing and
         * initializes key PayCard parameters.
         * PayCard handles communication with the Bluetooth reader device.
         */

        checkPermissions();
        payCardReader.startPayCardReader(this);
    }


    /**
     * checkPermissions
     * This method verifies the necessary Bluetooth and other permissions have been
     * granted by the user.
     */
    private void checkPermissions()
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            // User may have declined earlier, ask Android if we should show him a reason

            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                // show an explanation to the user
                // Good practise: don't block thread after the user sees the explanation, try again to request the permission.
            }
            else
            {
                // request the permission.
                // CALLBACK_NUMBER is a integer constants

                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                // The callback method gets the result of the request.
            }
        }
        else
        {
            // got permission
            mBtnScan.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (requestCode == 0)
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
                sharedUtils.showMessage(this, "Card Reader Scan",
                        "You have denied access to Bluetooth services. A Bluetooth reader cannot be used until you enable using Settings");
            }
        }
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
        mPBBTScan.setVisibility(View.VISIBLE);
        mFoundDevices = new ArrayList<>();
        listableDevices = new HashSet<>();
        payCardReader.scanForReaders("");
    }


    /**
     * didFindRDRDevices (Step 4) is the callback for PayCard scanForReaders.
     * The located BT card reader devices are returned in this callback.
     *
     * In this demo app the found device are displayed in the UI log and the first item in the
     * list is saved for use in the transaction processing step.
     *
     * @param foundDevices  This is an array of located devices of type Android BluetoothDevice.
     */
    public void didFindRDRDevices(List<BluetoothDevice> foundDevices)
    {
        mPBBTScan.setVisibility(View.GONE);

        mFoundDevices = foundDevices;
        Integer ctr = 0;
        Integer lctr = 0;
        Integer cnt = foundDevices.size();

        String showReaders = "Located BT Readers:";

        while (ctr < cnt)
        {
            BluetoothDevice btDevice = foundDevices.get(ctr);

            Integer devClass = btDevice.getBluetoothClass().getDeviceClass();
            String devName = btDevice.getName();

            if (!(listableDevices.contains(devName)))
            {
                if ((devClass == 524) || (devClass == 1028))
                {
                    if (ctr == 0)
                    {
                        mLblSelectedReader.setText(devName);
                        sharedUtils.setBTReader(this, devName);
                        mScanSuccess = RESULT_OK;
                    }

                    listableDevices.add(devName);

                    lctr = lctr + 1;

                    showReaders = showReaders + "\n - " + devName;

                    Log.i("PAYRIXDEMO", "doBTReturnScanResults - Address: " + btDevice.getAddress());
                    Log.i("PAYRIXDEMO", "doBTReturnScanResults - Name: " + btDevice.getName());
                    Log.i("PAYRIXDEMO", "doBTReturnScanResults - UUID: " + btDevice.getUuids());
                    Log.i("PAYRIXDEMO", "doBTReturnScanResults - BT Class: " + btDevice.getBluetoothClass().getDeviceClass());
                }
            }

            ctr = ctr + 1;
        }

        if (listableDevices.size() > 0)
        {
            mLblFoundDevices.setText(showReaders);
        }
        else
        {
            sharedUtils.showMessage(this,"Settings" ,"No New Bluetooth Readers were located");
        }
    }

    /**
     * didReceiveCardReaderError PayCard scanForReaders.
     * The PayCard Reader general error handler callback.
     *
     * @param errNumber     An error number of type Integer
     * @param errMessage    An error message of type String
     */
    public void didReceiveCardReaderError(Integer errNumber, String errMessage)
    {
        Log.i("PAYCARD", "didReceiveCardReaderError: " + errMessage);
        sharedUtils.showMessage(this,"Settings Error:", errMessage);
    }

    /**
     * The following are all the other possible Callbacks that are offered in the PayCard library
     * of the Payrix SDK.
     *
     */
    public void didSuccessfulBTConnect(){}
    public void didReceiveBTDisconnect(){}
    public void didReceiveAudioConnectedNotice(){}
    public void didReceiveCardReaderConnectionFailed(String error){}
    public void didReceiveAudioDisconnectedNotice(){}
    public void didReceiveAudioAttachedNotice(){}
    public void didReceiveDeviceInfo(Hashtable<String, String> deviceInfoData){}
    public void didReceiveBTScanTimeOut(){}

    public void didReceiveCardReaderIssue(String issueMsg){}
    public void didReceiveReaderModeUpdate(PayCardSharedAttr.PayCardDeviceMode currentMode){}
    public void didReceiveMessageToDisplay(String message){}
    public void didReceiveSwipeSuccess(Hashtable<String, Object> gateWayData, String cardStatus, Hashtable<String, String> cardInfo){}
    public void didCompleteCancelRequest(boolean successful){}

    // Future
    public void requestForHostEMVProcess(Hashtable<String, Object> gatewayData, Object encEMV, Object emvKSN, String encMethod){}
    public void didReceiveFinalEMVBatchData(String tlv){}
    public void didCompleteEMVCardTransaction(boolean successful, String transactionMsg){}
}
