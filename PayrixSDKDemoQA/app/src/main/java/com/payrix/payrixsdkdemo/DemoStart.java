package com.payrix.payrixsdkdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.payrix.payrixsdk.PayDevice;
import com.payrix.payrixsdk.PayMerchant;
import com.payrix.payrixsdk.PayResponse;
import com.payrix.payrixsdk.PayrixSDK;
import com.payrix.payrixsdk.PayrixSDKCallbacks;
import com.payrix.payrixsdk.RefundResponse;
import com.payrix.payrixsdk.TxnDataResponse;
import com.payrix.payrixsdk.paycore.payrixcore.TxnSession;
import com.payrix.payrixsdkdemo.ota.DemoOTA;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class DemoStart extends AppCompatActivity implements PayrixSDKCallbacks, View.OnClickListener {
    private TextView mLblProcessingLog;
    private final SharedUtilities sharedUtils = SharedUtilities.getInstance();

    private AppCompatTextView sandboxText, liveText;
    private CardView rbSandbox, rbLiveProd;
    private Context context;

    private Integer permissionCtr;
    AppCompatTextView liveProdText;
    TextView lblVersionBld;

    private final PayrixSDK payrixSDK = PayrixSDK.getInstance(this);

    public static final int ACTION_AUTHENTICATE = 100;
    public static final int ACTION_SCAN_BT = 200;
    private final int ACTION_PAYMENT_TRANSACTION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Payrix SDK Demo App");
        setSupportActionBar(toolbar);

        mLblProcessingLog = findViewById(R.id.lblProcessingLog);
        rbLiveProd = findViewById(R.id.liveProdBtn);
        rbSandbox = findViewById(R.id.sandboxBtn);
        sandboxText = findViewById(R.id.sandboxText);
        liveText = findViewById(R.id.liveText);

        TextView lblVersionBld = findViewById(R.id.lblVersion);
        liveProdText = findViewById(R.id.liveText);
        sandboxText = findViewById(R.id.sandboxText);

        lblVersionBld = findViewById(R.id.lblVersion);


        context = this;

        String appVersion = "";

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        lblVersionBld.setText(appVersion);

        rbSandbox.setOnClickListener(this);
        rbLiveProd.setOnClickListener(this);

        rbLiveProd.setOnClickListener(this);
        rbSandbox.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!sharedUtils.checkNetworkConnection(this)) {
            sharedUtils.showMessage(this, "Payrix SDK Demo App", "The Network Connection is Not Available; Resolve and Retry");
        }

        int startPlatform = sharedUtils.getPlatformID(this);
        doSetPlatform(startPlatform);
    }

    private void doSetPlatform(int thePlatform) {
        String theEnv = "";
        switchCustomTap(thePlatform);
        switch (thePlatform) {
            case 0:
            case R.id.sandboxBtn:
                sharedUtils.setEnvSelection(context, "api.payrix.com");//"api.payrix.com"

                theEnv =  "api.payrix.com";
                payrixSDK.doSetPayrixPlatform(context, theEnv, true, null);
                sharedUtils.setDemoMode(context, true);
                break;
            case R.id.liveProdBtn:

                sharedUtils.setEnvSelection(context, "api.payrix.com");

                theEnv = "api.payrix.com";
                payrixSDK.doSetPayrixPlatform(context, theEnv, false, null);
                sharedUtils.setDemoMode(context, false);
                break;
        }

        doPayrixPermissionsCheck();
    }


    private void doPayrixPermissionsCheck() {
        permissionCtr = 0;
        /*int permissionWriteExternal = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionWriteExternal != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 8);
        }
        else
        {
            permissionCtr = permissionCtr + 1;
        }*/

        int permissionCoarseLoc = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCoarseLoc != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 9);
        }
        else
        {
            permissionCtr = permissionCtr + 1;
        }

        int permissionFineLoc = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionFineLoc != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }
        else
        {
            permissionCtr = permissionCtr + 1;
        }

        /*int permissionReadExternal = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionReadExternal != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
        }
        else
        {
            permissionCtr = permissionCtr + 1;
        }*/
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        while (permissionCtr < 2)
        {
            switch (requestCode)
            {
                case 8:
                    permissionCtr = permissionCtr + 1;
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.i("PAYRIXDEMO-START", "External Storage Permission Granted");
                    }
                    else
                    {
                        Toast.makeText(this, "External Storage Permission Denied!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 9:
                    permissionCtr = permissionCtr + 1;
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.i("PAYRIXDEMO-START", "Coarse Location Permission Granted");
                    }
                    else
                    {
                        Toast.makeText(this, "Coarse Location Permission Denied!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 10:
                    permissionCtr = permissionCtr + 1;
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.i("PAYRIXDEMO-START", "FINE Location Permission Granted");
                    }
                    else
                    {
                        Toast.makeText(this, "FINE Location Permission Denied!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 11:
                    permissionCtr = permissionCtr + 1;
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.i("PAYRIXDEMO-START", "FINE Location Permission Granted");
                    }
                    else
                    {
                        Toast.makeText(this, "Ext Storage Permission Denied!", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    /**
     * goAuthenticate Listens for the Authentication button to be tapped.
     * <p>
     * The method branches over to the Authenticate activity for processing
     *
     * @param view  the view for the button passed from the UI
     */
    public void goAuthenticate(View view) {
        Intent intent = new Intent(this, DemoAuthentication.class);
        intent.putExtra("ACTION", ACTION_AUTHENTICATE);
        launchSomeActivity.launch(intent);
        //startActivityForResult(intent,1);  // requestCode: 1 = Authentication; 2 = Scan / Select Reader; 3 = Payment Transaction
    }


    /**
     * goScanBT Listens for the Scan & Select button to be tapped.
     * <p>
     * The method branches over to the Scan & Select activity for processing
     *
     * @param view  the view for the button passed from the UI
     */
    public void goScanBT(View view) {
        Intent intent = new Intent(this, DemoScanBT.class);
        intent.putExtra("ACTION", ACTION_SCAN_BT);
        launchSomeActivity.launch(intent);
        //startActivityForResult(intent,2);  // requestCode: 1 = Authentication; 2 = Scan / Select Reader; 3 = Payment Transaction
    }


    /**
     * goPaymentTxn Listens for the Transaction button to be tapped.
     * <p>
     * The method branches over to the Transaction activity for processing
     *
     * @param view  the view for the button passed from the UI
     */
    public void goPaymentTxn(View view) {
        Intent intent = new Intent(this, DemoTransaction.class);
        intent.putExtra("ACTION", ACTION_PAYMENT_TRANSACTION);
        launchSomeActivity.launch(intent);
        //startActivityForResult(intent,3);  // requestCode: 1 = Authentication; 2 = Scan / Select Reader; 3 = Payment Transaction
    }

    /**
     * This takes you to list of eligible transactions to perform refund on
     * @param view
     */
    public void onRefundClicked(View view) {
        Intent intent = new Intent(this, TxnListing.class);
        startActivity(intent);
    }

    /**
     * This is the starting point for OTA.
     * @param view
     */
    public void goToDemoOTA(View view) {
        startActivity(new Intent(DemoStart.this, DemoOTA.class));
    }

    /**
     * onActivityResult
     * Listens for the Tax or Tip Processing to complete and return
     * Based on the returning result the method updates the current Tax Rate or Tip Percent / Amount
     *
     * @param requestCode This is the value that matches the request type initially triggered (1=Tax, 2=Tip)
     * @param resultCode  This states the success or inaction by the called class. User Cancelled request.
     * @param data        This contains the returned data from the request.
     */

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        switch (data.getIntExtra("ACTION", 0)) {
                            case ACTION_AUTHENTICATE:
                                // Authentication
                                String useMerchantID = sharedUtils.getMerchantID(this);
                                String useMerchantDBA = sharedUtils.getMerchantDBA(this);

                                String showResults = "Authentication Successful: \n" + "- Merchant ID: " + useMerchantID + "\n- Merchant DBA: " + useMerchantDBA;
                                updateLog(showResults);
                                break;
                            case ACTION_SCAN_BT:
                                // Scan / Select Reader
                                String useBTReader = sharedUtils.getBTReader(this);
                                updateLog("\nBT Reader Scan Successful: \n" + "- Reader ID: " + useBTReader);
                                break;
                            case ACTION_PAYMENT_TRANSACTION:

                                break;
                        }
                    }
                }
            });



    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // requestCode: 1 = Authentication; 2 = Scan / Select Reader; 3 = Payment Transaction

        switch (requestCode)
        {
            case 1:
                if (resultCode == RESULT_OK)
                {
                    // Authentication
                    String useMerchantID = sharedUtils.getMerchantID(this);
                    String useMerchantDBA = sharedUtils.getMerchantDBA(this);

                    String showResults = "Authentication Successful: \n" + "- Merchant ID: " + useMerchantID + "\n- Merchant DBA: " + useMerchantDBA;
                    updateLog(showResults);
                    break;
                }
            case 2:
                if (resultCode == RESULT_OK)
                {
                    // Scan / Select Reader
                    String useBTReader = sharedUtils.getBTReader(this);

                    String showResults = "\nBT Reader Scan Successful: \n" + "- Reader ID: " + useBTReader;
                    updateLog(showResults);
                    break;
                }
            case 3:
                if (resultCode == RESULT_OK)
                {
                    // Txn w/ Reader

                    break;
                }
            case 4:
                if (resultCode == RESULT_OK)
                {
                    // Txn w/o Reader

                    break;
                }
            default:
        }
    }*/

    private void updateLog(String newEntry) {
        String theCurrentLog = mLblProcessingLog.getText().toString();
        theCurrentLog = theCurrentLog + newEntry;
        mLblProcessingLog.setText(theCurrentLog);
    }

    public void didReceiveDeviceResults(Integer responseType,  String actionMsg, String infoMsg, String deviceResponse){}
    public void didReceiveScanResults(Boolean aBoolean, String s, ArrayList<PayDevice> arrayList) {}
    public void didReceiveBTConnectResults(Boolean connectSuccess, String theDevice){}
    public void didReceiveBTDisconnectResults(Boolean disconnectSuccess){}
    public void didReceiveLoginResults(Boolean loginSuccess, String theSessionKey, List<PayMerchant> theMerchants, String theMessage){}
    public void didReceivePayResults(Integer integer, String s, String s1, PayResponse payResponse) {}
    public void didReceiveRefundResults(Boolean aBoolean, Integer integer, String s, RefundResponse refundResponse) {}
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

    @Override
    public void onClick(View view) {
        sharedUtils.setPlatformID(context, view.getId());
        switchCustomTap(view.getId());
        if (view.getId() == R.id.sandboxBtn) {
            // Set the Platform URL
            sharedUtils.setSandBoxOn(context, true);
            sharedUtils.setEnvSelection(context, "api.payrix.com");

            String theEnv =  "api.payrix.com";
            payrixSDK.doSetPayrixPlatform(context, theEnv, true, null);
            sharedUtils.setDemoMode(context, true);
        } else if (view.getId() == R.id.liveProdBtn) {

            // Set the Platform URL
            sharedUtils.setSandBoxOn(context, false);
            sharedUtils.setEnvSelection(context, "api.payrix.com");

            String theEnv =  "api.payrix.com";
            payrixSDK.doSetPayrixPlatform(context, theEnv, false, null);
            sharedUtils.setDemoMode(context, false);
        }
    }

    private void switchCustomTap(int id) {
        if (id == R.id.sandboxBtn) {
            rbSandbox.setCardBackgroundColor(getColor(R.color.colorPrimaryDark));
            sandboxText.setTextColor(getColor(R.color.white));

            rbLiveProd.setCardBackgroundColor(getColor(R.color.colorAccent));
            liveProdText.setTextColor(getColor(R.color.black));
        } else {
            rbLiveProd.setCardBackgroundColor(getColor(R.color.colorPrimaryDark));
            liveProdText.setTextColor(getColor(R.color.white));

            rbSandbox.setCardBackgroundColor(getColor(R.color.colorAccent));
            sandboxText.setTextColor(getColor(R.color.black));
        }
    }
}

