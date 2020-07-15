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

import com.payrix.paycoreapilibrary.paycoremobile.PayCoreCallbacks;
import com.payrix.paycoreapilibrary.paycoremobile.PayCoreGlobals;
import com.payrix.paycoreapilibrary.paycoremobile.PayCoreMaster;
import com.payrix.paycoreapilibrary.payrixcore.Txns;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DemoAuthentication extends AppCompatActivity implements PayCoreCallbacks
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
    public PayCoreMaster payCoreMaster = PayCoreMaster.getInstance(this);

    SharedUtilities sharedUtils = SharedUtilities.getInstance();

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
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // onBackPressed();
                Intent intent = new Intent();
                // tipIntent.putExtra("returnedCurrTrans", mCurrentTransaction);

                setResult(mAuthSuccess, intent);
                finish();
            }
        });

        /*
         * Password Preset for Demo App
         */

//        mTxtUserID.setText("aSandboxID");
//        mTxtUserPwd.setText("A!Sandbox@Pwd");

        /*
         * Step 2:
         * Set Demo - Sandbox mode
         */

        mPBAuth.setVisibility(View.INVISIBLE);
        mAuthSuccess = 99;
        doSetLiveDemoMode(true);
    }


    /**
     * doSetLiveDemoMode  (Step 2)
     * This method sets the Live or Demo (Sandbox) platform to execute transactions on.
     * @param demoMode  Boolean indicator for environment. True = Demo (Sandbox); False = Live (Production)
     */
    private void doSetLiveDemoMode(Boolean demoMode)
    {
        sharedUtils.setDemoMode(this, demoMode);
        PayCoreGlobals payCoreGlobals = PayCoreGlobals.getInstance();
        payCoreGlobals.demoMode = demoMode;
        payCoreGlobals.PWLWhiteLabelAPIHostName = sharedUtils.pwlAPIHostName;
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
        if (payCoreMaster.doCheckNetworkConnection(this))
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
             //   payCoreMaster = PayCoreMaster.getInstance(this);
                payCoreMaster.validateLoginCredentials(mTxtUserID.getText().toString(), mTxtUserPwd.getText().toString());
            }
        }
        else
        {
            sharedUtils.showMessage(this, "Login", "The Network / Internet Connection is unavailable... Retry later.");
        }
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
     * @param sessionKey    The sessionKey for tokenized access for further processing.
     */
    public void didReceiveLoginResponse(Boolean success, String sessionKey)
    {
        if (success)
        {
            completeLoginProcess(sessionKey);
        }
        else
        {
            // Show Credential Failure Message
            mPBAuth.setVisibility(View.INVISIBLE);
            mPBAuth.setVisibility(View.GONE);
            sharedUtils.showMessage(this, "Authentication", "Missing or Invalid Login Credentials... Retry");
        }
    }


    /**
     * completeLoginProcess  (Step 5)
     * This method uses the authentication SessionKey to perform the next request to PayCoreMaster
     * for the Merchant information.
     * The response from PayCoreMaster is returned in the Callback: didReceiveMerchantIDInfo
     *
     * @param sessionKey    The sessionKey for tokenized access for further processing.
     */
    private void completeLoginProcess(String sessionKey)
    {
        sharedUtils.setSessionKey(this, sessionKey);
   //     payCoreMaster = PayCoreMaster.getInstance(this);
        payCoreMaster.getMerchantID(sessionKey);
    }


    /**
     * didReceiveMerchantIDInfo  (Step 6)
     * This is the last step of the Authentication process.
     * The Merchant information, as well as the Session information is saved for later use.
     * @param success       Boolean indicating if the request was successful or not.
     * @param merchantID    The Merchant ID associated with the Credentials
     * @param merchantDBA   The DBA name for the Merchant
     */
    public void didReceiveMerchantIDInfo(Boolean success, String merchantID, String merchantDBA)
    {
        mPBAuth.setVisibility(View.GONE);
        if ((success) && (merchantID != null))
        {
            sharedUtils.setMerchantID(this, merchantID);
            sharedUtils.setMerchantDBA(this, merchantDBA);
            mAuthSuccess = RESULT_OK;

            String showResults = "Authentication Successful: \n" + "- Merchant ID: " + merchantID + "\n- Merchant DBA: " + merchantDBA;

            mLblAuthLog.setText(showResults);
        }
        else
        {
            // Show Issue with Merchant Information Message
            sharedUtils.showMessage(this, "Authentication", "Unexpected Error reading Merchant Information... Retry");
        }
    }

    // Unused Callback(s)
    public void didReceiveTransactionResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors){}
    public void didReceiveSubsequentTxnResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors){}
    public void didReceiveRefundEligibleStatus(Boolean success){}
    public void didReceiveSpecificTxnResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors){}
    public void didReceiveRefundResponse(Boolean success, List<Txns> theTransactions, JSONObject theDetails, ArrayList<String> theErrors){}
}
