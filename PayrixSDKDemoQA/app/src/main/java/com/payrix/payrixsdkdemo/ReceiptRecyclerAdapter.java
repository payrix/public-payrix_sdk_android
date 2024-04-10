package com.payrix.payrixsdkdemo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReceiptRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	private List<ReceiptObj> mReceiptRows;
	
	private LayoutInflater mInflater;
	private ReceiptRecyclerAdapter.ItemClickListener mClickListener;
	
	private static int TYPE_STD = 0;
	private static int TYPE_ROW = 1;
	
	public Context raContext;
	
	// data is passed into the constructor
	ReceiptRecyclerAdapter(Context context, List<ReceiptObj> aReceiptObj)
	{
		// this.mInflater = LayoutInflater.from(context);
		this.mReceiptRows = aReceiptObj;
		this.raContext = context;
	}
	
	
	// inflates the row layout from xml when needed
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		RecyclerView.ViewHolder viewHolder = null;
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		
		if (viewType == TYPE_STD)
		{
			View v0 = inflater.inflate(R.layout.rv_receipt_std, parent, false);
			viewHolder = new HeaderViewHolder(v0);
		}
		else if (viewType == TYPE_ROW)
		{
			View v1 = inflater.inflate(R.layout.rv_receipt_row, parent, false);
			viewHolder = new StandardViewHolder(v1);
		}
		
		return viewHolder;
	}
	
	
	// binds the data to the TextView in each row
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position)
	{
		String aReceiptLabel = mReceiptRows.get(position).getRowLabel();
		String aReceiptValue = mReceiptRows.get(position).getRowValue();
		
		switch (viewHolder.getItemViewType())
		{
			case 0: // Centered Label
				HeaderViewHolder hdrHolder = (HeaderViewHolder) viewHolder;
				hdrHolder.lblCenterTxt.setText(aReceiptValue);
				break;
			case 1: // Row Info
				StandardViewHolder stdHolder = (StandardViewHolder) viewHolder;
				stdHolder.lblLeftTxt.setText(aReceiptLabel);
				stdHolder.lblRighttxt.setText(aReceiptValue);
				break;
		}
	}
	
	
	// total number of rows
	@Override
	public int getItemCount()
	{
		return mReceiptRows.size();
	}
	
	@Override
	public int getItemViewType(int position)
	{
		// Determine if Transaction or Section Header
		
		if (mReceiptRows.get(position).getRowLabel().equals(" "))
		{
			return TYPE_STD;
		}
		else
		{
			return TYPE_ROW;
		}
	}
	
	
	// stores and recycles views as they are scrolled off screen
	public class StandardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		TextView lblLeftTxt;
		TextView lblRighttxt;
		
		StandardViewHolder(View itemView)
		{
			super(itemView);
			lblLeftTxt = itemView.findViewById(R.id.lblLeftText_rr);
			lblRighttxt = itemView.findViewById(R.id.lblRightText_rr);
			// itemView.setOnClickListener(this);
		}
		
		@Override
		public void onClick(View view)
		{
			if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
		}
	}
	
	public class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		TextView lblCenterTxt;
		
		HeaderViewHolder(View itemView)
		{
			super(itemView);
			lblCenterTxt = itemView.findViewById(R.id.lblCenterText_rr);
			// itemView.setOnClickListener(this);
		}
		
		@Override
		public void onClick(View view)
		{
			if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
		}
	}
	
	// convenience method for getting data at click position
	ReceiptObj getItem(int id)
	{
		return mReceiptRows.get(id);  // getCardAppName();
	}
	
	// allows clicks events to be caught
	void setClickListener(ReceiptRecyclerAdapter.ItemClickListener itemClickListener)
	{
		mClickListener = itemClickListener;
	}
	
	// parent activity will implement this method to respond to click events
	public interface ItemClickListener
	{
		void onItemClick(View view, int position);
	}
}
