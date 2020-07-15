package com.payrix.payrixsdkdemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

public class DemoStart extends AppCompatActivity
{

    TextView mLblProcessingLog;
    SharedUtilities sharedUtils = SharedUtilities.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("         Payrix SDK Demo App");
        setSupportActionBar(toolbar);

        mLblProcessingLog = findViewById(R.id.lblProcessingLog);

    }


    /**
     * goAuthenticate Listens for the Authentication button to be tapped.
     * <p>
     * The method branches over to the Authenticate activity for processing
     *
     * @param view  the view for the button passed from the UI
     */
    public void goAuthenticate(View view)
    {
        Intent intent = new Intent(this, DemoAuthentication.class);
        startActivityForResult(intent,1);  // requestCode: 1 = Authentication; 2 = Scan / Select Reader; 3 = Payment Transaction
    }


    /**
     * goScanBT Listens for the Scan & Select button to be tapped.
     * <p>
     * The method branches over to the Scan & Select activity for processing
     *
     * @param view  the view for the button passed from the UI
     */
    public void goScanBT(View view)
    {
        Intent intent = new Intent(this, DemoScanBT.class);
        startActivityForResult(intent,2);  // requestCode: 1 = Authentication; 2 = Scan / Select Reader; 3 = Payment Transaction
    }


    /**
     * goPaymentTxn Listens for the Transaction button to be tapped.
     * <p>
     * The method branches over to the Transaction activity for processing
     *
     * @param view  the view for the button passed from the UI
     */
    public void goPaymentTxn(View view)
    {
        Intent intent = new Intent(this, DemoTransaction.class);
        startActivityForResult(intent,3);  // requestCode: 1 = Authentication; 2 = Scan / Select Reader; 3 = Payment Transaction
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
    }

    private void updateLog(String newEntry)
    {
        String theCurrentLog = mLblProcessingLog.getText().toString();
        theCurrentLog = theCurrentLog + newEntry;
        mLblProcessingLog.setText(theCurrentLog);
    }
}
