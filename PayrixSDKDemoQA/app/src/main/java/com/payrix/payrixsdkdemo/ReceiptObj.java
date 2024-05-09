package com.payrix.payrixsdkdemo;

public class ReceiptObj
{
	private String rowLabel;
	private String rowValue;
	
	public ReceiptObj(){}
	
	public ReceiptObj(String rowLabel, String rowValue)
	{
		this.rowLabel = rowLabel;
		this.rowValue = rowValue;
	}
	
	public String getRowLabel()
	{
		return rowLabel;
	}
	
	public void setRowLabel(String rowLabel)
	{
		this.rowLabel = rowLabel;
	}
	
	public String getRowValue()
	{
		return rowValue;
	}
	
	public void setRowValue(String rowValue)
	{
		this.rowValue = rowValue;
	}
}
