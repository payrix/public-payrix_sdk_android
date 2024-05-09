package com.payrix.payrixsdkdemo;

public class EMVCardAppObj
{
	public int cardAppID;
	public String cardAppName;
	public Boolean currentSelection;
	public EMVCardAppObj(){}
	
	public EMVCardAppObj(int cardAppID, String cardAppName, int chkMarkResID, Boolean currentSelection)
	{
		this.cardAppID = cardAppID;
		this.cardAppName = cardAppName;
		this.currentSelection = currentSelection;
	}
	
	public int getCardAppID()
	{
		return cardAppID;
	}
	
	public void setCardAppID(int cardAppID)
	{
		this.cardAppID = cardAppID;
	}
	
	public String getCardAppName()
	{
		return cardAppName;
	}
	
	public void setCardAppName(String cardAppName)
	{
		this.cardAppName = cardAppName;
	}
	
	public Boolean getCurrentSelection()
	{
		return currentSelection;
	}
	
	public void setCurrentSelection(Boolean currentSelection)
	{
		this.currentSelection = currentSelection;
	}
}
