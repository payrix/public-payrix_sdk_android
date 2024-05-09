package com.payrix.payrixsdkdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.payrix.payrixsdk.PayResponse;
import com.payrix.payrixsdk.PaySharedAttributes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DemoReceipt extends AppCompatActivity implements ReceiptRecyclerAdapter.ItemClickListener
{
	ReceiptRecyclerAdapter receiptRA;
	Toolbar toolbar;
	Button mTBShareBtn;
	RadioGroup rgReceiptSel;
	RadioButton rbMerchant;
	RadioButton rbCustomer;
	Context mContext;
	RecyclerView receiptView;
	
	List<ReceiptObj> theReceiptList;
	
	SharedUtilities sharedUtils = SharedUtilities.getInstance();
	PayResponse payResponse = PayResponse.getInstance();
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demoreceipt);
		toolbar = findViewById(R.id.toolbar);
		TextView toolbarTitle = findViewById(R.id.toolbarTitle);
		receiptView = findViewById(R.id.rvReceiptData);
		setSupportActionBar(toolbar);
		
		rgReceiptSel = findViewById(R.id.rgReceiptSel);
		rbCustomer = findViewById(R.id.rbCustomerCpy);
		rbMerchant = findViewById(R.id.rbMerchantCpy);
		
		mTBShareBtn = findViewById(R.id.tbButton);
		mContext = this;
		
		toolbarTitle.setText("Transaction Receipt");
		
		theReceiptList = new ArrayList<>();
		toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_black_24);
		toolbar.setElevation(10.0f);
		toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				mTBShareBtn.setVisibility(View.INVISIBLE);
				onBackPressed();
			}
		});
		
		mTBShareBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				jumpToShare(view);
			}
		});
		
		rgReceiptSel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				sharedUtils.setPlatformID(mContext, checkedId);
				if (checkedId == R.id.rbMerchantCpy)
				{
					showReceipt("MERCHANT");
				}
				else if (checkedId == R.id.rbCustomerCpy)
				{
					showReceipt("CUSTOMER");
				}
			}
		});
		
		mTBShareBtn.setText("SHARE");
		mTBShareBtn.setVisibility(View.VISIBLE);
	}
	
	
	@Override
	protected void onResume()
	{
		super.onResume();
		PayResponse payResponse = PayResponse.getInstance();
		theReceiptList = new ArrayList<>();
		rbMerchant.setChecked(true);
	}
	
	private void jumpToShare(View view)
	{
		Bitmap screenShot = Bitmap.createBitmap(receiptView.getMeasuredWidth(),receiptView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		Canvas aCanvas = new Canvas(screenShot);
		receiptView.draw(aCanvas);
		
		String fileName = payResponse.payTxn.getId() + "Rcpt";
		doScreenshot(screenShot, fileName);
	}
	
	private void showReceipt(String byMerchOrCust)
	{
		String useData = "";
		theReceiptList = new ArrayList<>();
		switch (byMerchOrCust)
		{
			case "MERCHANT":
				// Loop through merchant receipt
				bldReceiptData(1);  // 1 = Merchant
				doShowReceiptList();
				break;
			case "CUSTOMER":
				// Loop through customer receipt
				bldReceiptData(2);  // 2 = Customer
				doShowReceiptList();
				break;
		}
	}
	
	
	private void doShowReceiptList()
	{
		RecyclerView recyclerView = findViewById(R.id.rvReceiptData);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		receiptRA = new ReceiptRecyclerAdapter(this, theReceiptList);
		recyclerView.setAdapter(receiptRA);
	}
	
	
	private void bldReceiptData(int typeR)
	{
		String receiptTypeLabel = "";
		String receiptStr = "- No Receipt Data -";
		
		ArrayList<Hashtable<String, String>> selectedData = new ArrayList<>();
		selectedData = payResponse.finalReceipt;
		
		if (typeR == 1)
		{
			receiptTypeLabel = "\n     ----------- MERCHANT COPY ------------";
		}
		else if (typeR == 2)
		{
			receiptTypeLabel = "\n     ----------- CUSTOMER COPY ------------";
		}
		
		if (payResponse.receiptApprovedDeclined.equalsIgnoreCase("DECLINED"))
		{
			receiptTypeLabel = "\n ";
		}
		
		if (selectedData == null)
		{
			ReceiptObj aRecObj = new ReceiptObj();
			aRecObj.setRowLabel(" ");
			aRecObj.setRowValue(receiptStr);
		}
		else
		{
			String theDevice = sharedUtils.getBTReader(this);
			int ctr = 0;
			int cnt = selectedData.size();
			while (ctr < cnt)
			{
				ReceiptObj aRecObj = new ReceiptObj();
				Hashtable<String, String> aRec = new Hashtable<>();
				aRec = selectedData.get(ctr);
				Set<String> keys = aRec.keySet();
				for(String key: keys)
				{
					String vals = selectedData.get(ctr).get(key);
					if ((key.equalsIgnoreCase("TID: ")) && (vals.equalsIgnoreCase("t1_payrix_bbpos_terminal00001")) && (theDevice != null) && (!theDevice.equals("")))
					{
						aRecObj.setRowLabel(key);
						aRecObj.setRowValue(theDevice);
					}
					else
					{
						aRecObj.setRowLabel(key);
						aRecObj.setRowValue(vals);
					}
					
				}
				theReceiptList.add(aRecObj);
				ctr = ctr + 1;
			}
			ReceiptObj aRecObj = new ReceiptObj();
			aRecObj.setRowLabel(" ");
			aRecObj.setRowValue(receiptTypeLabel);
			theReceiptList.add(aRecObj);
		}
	}
	
	private void doScreenshot(Bitmap bitmap, String filename)
	{
		Date date = new Date();
		try
		{
			OutputStream imgOutStream;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			{
				ContentResolver resolver = getContentResolver();
				ContentValues contentValues = new ContentValues();
				contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename + ".jpg");
				contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
				contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
				Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
				imgOutStream = resolver.openOutputStream(imageUri);
			} else {
				String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
				File image = new File(imagesDir, filename + ".jpg");
				imgOutStream = new FileOutputStream(image);
			}
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imgOutStream);
			imgOutStream.flush();
			imgOutStream.close();
		}
		catch (IOException io)
		{
			io.printStackTrace();
		}
		sharedUtils.showMessage(this,"SHARE", "The Receipt has been captured. Use Photo Gallery to Share");
	}
	
	@Override
	public void onItemClick(View view, int position){}
}