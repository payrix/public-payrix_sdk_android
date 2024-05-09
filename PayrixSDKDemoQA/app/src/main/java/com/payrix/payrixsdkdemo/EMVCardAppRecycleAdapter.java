package com.payrix.payrixsdkdemo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static java.lang.Thread.sleep;

public class EMVCardAppRecycleAdapter extends RecyclerView.Adapter<EMVCardAppRecycleAdapter.ViewHolder>
{
	public List<EMVCardAppObj> mCardApps;
	
	private LayoutInflater mInflater;
	private ItemClickListener mClickListener;
	
	public Context raContext;
	
	// data is passed into the constructor
	EMVCardAppRecycleAdapter(Context context, List<EMVCardAppObj> appObj)
	{
		this.mInflater = LayoutInflater.from(context);
		this.mCardApps = appObj;
		this.raContext = context;
	}
	
	
	// inflates the row layout from xml when needed
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = mInflater.inflate(R.layout.rv_emvcardapps_row, parent, false);
		return new ViewHolder(view);
	}
	
	
	// binds the data to the TextView in each row
	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		int aCardAppID = mCardApps.get(position).getCardAppID();
		String aCardAppName = mCardApps.get(position).getCardAppName();
		String useIDandName = aCardAppID + " - " + aCardAppName;
		Boolean isAppSelected = mCardApps.get(position).getCurrentSelection();
		
		holder.lblAppName.setText(useIDandName);
		
		if (isAppSelected)
		{
			holder.imgChkBox.setImageResource(R.drawable.checkmarkblue);
		}
		else
		{
			holder.imgChkBox.setImageResource(R.drawable.checkmarkempty);
		}
	}
	
	
	// total number of rows
	@Override
	public int getItemCount()
	{
		return mCardApps.size();
	}
	
	
	// stores and recycles views as they are scrolled off screen
	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		TextView lblAppName;
		ImageView imgChkBox;
		
		ViewHolder(View itemView)
		{
			super(itemView);
			lblAppName = itemView.findViewById(R.id.lblAppName_rv);
			imgChkBox = itemView.findViewById(R.id.imgChkbox_rv);
			itemView.setOnClickListener(this);
		}
		
		@Override
		public void onClick(View view)
		{
			if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
		}
	}
	
	// convenience method for getting data at click position
	EMVCardAppObj getItem(int id)
	{
		return mCardApps.get(id);  // getCardAppName();
	}
	
	// allows clicks events to be caught
	void setClickListener(ItemClickListener itemClickListener)
	{
		mClickListener = itemClickListener;
	}
	
	// parent activity will implement this method to respond to click events
	public interface ItemClickListener
	{
		void onItemClick(View view, int position);
	}
}
