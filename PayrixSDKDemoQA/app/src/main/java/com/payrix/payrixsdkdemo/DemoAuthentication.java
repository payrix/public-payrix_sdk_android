package com.payrix.payrixsdkdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.payrix.payrixsdk.PayDevice;
import com.payrix.payrixsdk.PayMerchant;
import com.payrix.payrixsdk.PayResponse;
import com.payrix.payrixsdk.PayrixSDK;
import com.payrix.payrixsdk.PayrixSDKCallbacks;
import com.payrix.payrixsdk.RefundResponse;
import com.payrix.payrixsdk.TxnDataResponse;
import com.payrix.payrixsdk.paycore.payrixcore.TxnSession;
import com.payrix.payrixsdk.paycore.payrixcore.TxnSessionConfig;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class DemoAuthentication extends AppCompatActivity implements PayrixSDKCallbacks
{
    EditText mTxtUserID;
    EditText mTxtUserPwd;
    Button mBtnAuth;
    TextView mLblAuthLog;
    ProgressBar mPBAuth;

    Toolbar toolbar;

    int mAuthSuccess;

    /**
     * Step 1: Instantiate the PayCoreMaster instance.  This class handles authentication.
     */
    PayrixSDK payrixSDK = PayrixSDK.getInstance(this);
    SharedUtilities sharedUtils = SharedUtilities.getInstance();

    /**
     * Please ensure this KEY is not hardcoded it can be store on your server and pulled during app initialization
     */
    private final String PRIVATE_API_KEY = "0a30e20b01555899cad0ad1c0fb32185";//"9ab82989c1159a0702a8e3790b226497";//"ca300a3dff5f1bbbee3878b41126cb4f";//; ;//
    private final String MERCHANT_ID = "t1_mer_66106eb6cf9733f1c0c497b";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        setSupportActionBar(toolbar);

        mTxtUserID = findViewById(R.id.txtUserID);
        mTxtUserPwd = findViewById(R.id.txtPwd);
        mBtnAuth = findViewById(R.id.btnAuth);
        mLblAuthLog = findViewById(R.id.lblAuthLog);
        mPBAuth = findViewById(R.id.pbAuth);

        toolbarTitle.setText("Payrix Authentication");

        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_black_24);
        toolbar.setElevation(10.0f);
        toolbar.setNavigationOnClickListener(view -> {
            // onBackPressed();
            Intent intent = new Intent();
            setResult(mAuthSuccess, intent);
            finish();
        });

        Boolean isSandBox = sharedUtils.getSandBoxOn(this);
        String theEnv = sharedUtils.getEnvSelection(this);
        
        payrixSDK.doSetPayrixPlatform(this, theEnv, isSandBox, null);

        System.out.println("robot "+theEnv);
        mTxtUserID.setText("SteveSykes");
        mTxtUserPwd.setText("Top0!0gun");
        
        mPBAuth.setVisibility(View.INVISIBLE);
        mAuthSuccess = 99;
        sharedUtils.setDemoMode(this, isSandBox);
    }
    


    /**
     * goLoginListener (Step 3)
     * This method listens for the Authenticate button to be tapped
     * The method verifies that valid input was entered and the proceeds
     * to authenticate with the gateway platform (Payrix).
     * The response from the gateway is via the Callback: didReceiveLoginResponse
     * @param view This represents the button view object
     */
    public void goLoginListener(View view)
    {
        if (sharedUtils.checkNetworkConnection(this))
        {
            mPBAuth.setVisibility(View.VISIBLE);
            if (!doCheckValidInput())
            {
                mPBAuth.setVisibility(View.INVISIBLE);
                mPBAuth.setVisibility(View.GONE);
                sharedUtils.showMessage(this,"Authentication", "Missing or Invalid Credentials... Retry");
            }
            else
            {
                mBtnAuth.setEnabled(false);
                payrixSDK.doValidateCredentials(mTxtUserID.getText().toString(), mTxtUserPwd.getText().toString());
            }
        }
        else
        {
            sharedUtils.showMessage(this, "Login", "The Network / Internet Connection is unavailable... Resolve and Retry.");
        }
    }

    /**
     *
     * @param view
     */
    public void txnSessionBtn(View view) {
        mPBAuth.setVisibility(View.VISIBLE);
        TxnSessionConfig txnSessionConfig = new TxnSessionConfig.Builder(PRIVATE_API_KEY, MERCHANT_ID)
                .setDuration(4000)
                .setMaxTimesApproved(200)
                .setMaxTimesUse(100).build();

        payrixSDK.doGetTxnSessionKey(txnSessionConfig);
    }

    private boolean doCheckValidInput()
    {
        if ((mTxtUserID.getText().toString().equals("")) ||
                (mTxtUserPwd.getText().toString().equals("")))
        {
            return false;
        }
        else
        {
            return true;
        }
    }


    /**
     * didReceiveLoginResponse  (Step 4)
     * This is a Callback from PayCoreMaster triggered by the validateLoginCredentials request.
     * If the request is successful the tha app proceeds to retrieve the related Merchant information.
     * If the request is not successful then an error message is displayed
     * @param success       A boolean set to True if the Authentication was successful and False if it was not
     * @param theSessionKey The sessionKey for tokenized access for further processing.
     * @param theMerchants  The array of PayMerchant objects
     * @param theMessage    The String of Errors (if any)
     */
    
    
    public void didReceiveLoginResults(Boolean success, String theSessionKey, List<PayMerchant> theMerchants, String theMessage)
    {
        mPBAuth.setVisibility(View.INVISIBLE);
        mPBAuth.setVisibility(View.GONE);
        mBtnAuth.setEnabled(true);
        
        if ((success) && (theMerchants != null))
        {
            sharedUtils.setSessionKey(this, theSessionKey);
            sharedUtils.setMerchantID(this, theMerchants.get(0).merchantID);
            sharedUtils.setMerchantDBA(this, theMerchants.get(0).merchantDBA);
            sharedUtils.setUseTxnSession(this, false);

            System.out.println(theSessionKey+" "+theMerchants.get(0).merchantID);
            String logMsg = "Authentication Successful: \n" + "- Merchant ID: " + theMerchants.get(0).merchantID + "\n- Merchant DBA: " + theMerchants.get(0).merchantDBA;
            updateLog(logMsg);
        }
        else
        {
            String useError = "Error: " + theMessage;
            sharedUtils.showMessage(this, "Authentication", useError);
            String logMsg = "Authentication Error: \n" + theMessage;
            updateLog(logMsg);
        }
    }
    
    
    /**
     **updateLog**
     * This method updates the UI Log of authentication events
     - Parameters:
     - newMessage: The String message to be displayed
     */
    private void updateLog(String newMessage)
    {
        String currentLog = mLblAuthLog.getText().toString();
        currentLog = currentLog + "\n" + newMessage;
        mLblAuthLog.setText(currentLog);
        System.out.println(currentLog);
    }
    
    
    // Unused Optional PayrixSDK Callbacks
    
    public void didReceiveDeviceResults(Integer responseType,  String actionMsg, String infoMsg, String deviceResponse){}
    public void didReceiveScanResults(Boolean scanSuccess, String scanMsg, ArrayList<PayDevice> payDevices){}
    public void didReceiveBTConnectResults(Boolean connectSuccess, String theDevice){}
    public void didReceiveBTDisconnectResults(Boolean disconnectSuccess){}
    public void didReceivePayResults(Integer responseType, String actionMsg, String infoMsg, PayResponse payResponse){}
    public void didReceiveRefundResults(Boolean success, Integer responseCode, String refundMsg, RefundResponse refundResponse){}
    public void didReceiveTxnResults(Boolean success, Integer responseCode, String txnMsg, TxnDataResponse txnDataResponse){}

    @Override
    public void didReceiveTxnKeyResult(boolean success, TxnSession txnSession, String errorMessage) {
        mPBAuth.setVisibility(View.GONE);
        String logMsg;
        if (success) {
            sharedUtils.setSessionKey(this, txnSession.getKey());
            sharedUtils.setMerchantID(this, txnSession.getMerchant());
            sharedUtils.setUseTxnSession(this, true);
            logMsg = "Authentication Successful: \n" + "- Merchant: " + txnSession.getMerchant();
        } else {
            logMsg = errorMessage;
        }
        updateLog(logMsg);

    }

    @Override
    public void didReceiveRefundEligibleStatus(Boolean aBoolean) {

    }

    @Override
    public void didReceiveDeviceData(Hashtable<String, String> hashtable) {

    }
}
