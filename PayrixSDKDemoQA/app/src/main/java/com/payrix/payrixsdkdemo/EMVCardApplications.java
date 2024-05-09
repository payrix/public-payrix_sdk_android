package com.payrix.payrixsdkdemo;

import java.io.Serializable;
import java.util.ArrayList;

class EMVCardApplications implements Serializable
{
	public static EMVCardApplications emvCardInstance;
	public ArrayList<EMVCardAppObj> theCardEMVAppList;

	public EMVCardApplications(){}
	
	public static synchronized EMVCardApplications getInstance()
	{
		if (emvCardInstance == null)
		{
			emvCardInstance = new EMVCardApplications();
		}
		return emvCardInstance;
	}
}
