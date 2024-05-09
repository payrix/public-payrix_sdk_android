package com.payrix.payrixsdkdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DemoAppSelection extends AppCompatActivity implements EMVCardAppRecycleAdapter.ItemClickListener
{
	EMVCardAppRecycleAdapter cardAppRA;
	Toolbar toolbar;
	boolean dataInputChanged;
	SharedUtilities sharedUtils;
	List<EMVCardAppObj> theEMVAppList;
	EMVCardApplications theSelectedApp;
	
	EMVCardApplications theAppSelections;
	int mSelectedAppID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demoappselection);
		sharedUtils = SharedUtilities.getInstance();
		
		// Setup Toolbar
		toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle("Select an Application");
		setSupportActionBar(toolbar);
		
		TextView toolbarTitle = findViewById(R.id.toolbarTitle);
		toolbarTitle.setText("Select an Application");
		
		dataInputChanged = false;
		
		toolbar.setElevation(10.0f);
		toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (dataInputChanged == true)
				{
					handleUpdateNotSaved();
				}
				else
				{
					onBackPressed();
				}
			}
		});
		
	}
	
	
	@Override
	protected void onResume()
	{
		super.onResume();
		Intent intent = getParentActivityIntent();
		theAppSelections = EMVCardApplications.getInstance();
		theEMVAppList = theAppSelections.theCardEMVAppList;
		doShowAppsList();
	}
	
	
	private void doShowAppsList()
	{
		RecyclerView recyclerView = findViewById(R.id.rvCardEMVApps);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		cardAppRA = new EMVCardAppRecycleAdapter(this, theEMVAppList);  // cardAppRA
		cardAppRA.setClickListener(this);
		recyclerView.setAdapter(cardAppRA);
	}
	
	
	@Override
	public void onItemClick(View view, int position)
	{
		// Toast.makeText(this, "You clicked " + dbAdapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
		
		// TODO: Steps to handle click on a device
		// 1. Look at the ReaderDeviceObj for the item selected to check if isCurrent is already on.
		//    - If so, do nothing; If not, go to step 2.
		// 2. Read through array looking for isCurrent is on.  If not found go to Step 3.
		//    - If found, use the ReaderDeviceObj and set the isCurrent to OFF, and updated the DB.
		// 3. Use the saved position to get the ReaderDeviceObj and set isCurrent to ON, and update the DB.
		// 4. Call doBTSettingsShow to Refresh the UI.
		//
		dataInputChanged = true;
		EMVCardAppObj selectedApp = theEMVAppList.get(position);
		if (!selectedApp.getCurrentSelection())
		{
			// App not currently selected
			int ctr = 0;
			int cnt = theEMVAppList.size();
			do
			{
				EMVCardAppObj anApp;
				anApp = theEMVAppList.get(ctr);
				if (anApp.getCurrentSelection())
				{
					anApp.setCurrentSelection(false);
				}
				ctr = ctr + 1;
			}
			while (ctr < cnt);
			
			// Set New Selection to ON
			
			selectedApp.setCurrentSelection(true);
			mSelectedAppID = selectedApp.getCardAppID();
			
			// Refresh the UI
			
			doShowAppsList();
		}
	}
	
	
	public void goContinue(View view)
	{
		if (dataInputChanged)
		{
			dataInputChanged = false;
			Intent intent = new Intent();
			intent.putExtra("returnedAppSelection", mSelectedAppID);
			setResult(RESULT_OK, intent);
			finish();
		}
		else
		{
			sharedUtils.showMessage(this, "Application Selection", "An App Must be Selected");
		}
	}
		
		
	private void handleUpdateNotSaved()
	{
		android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this, R.style.AppTheme).create();
		alertDialog.setTitle("Application Selection");
		alertDialog.setMessage("Do you wish to Exit without Saving?");
		alertDialog.setInverseBackgroundForced(false);
		alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "EXIT", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int i)
			{
				onBackPressed();
			}
		});
		
		alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int i)
			{
				dialogInterface.dismiss();
			}
		});
		alertDialog.show();
	}
}