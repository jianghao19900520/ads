package com.example.adsrun;

import java.io.Serializable;

public class AdsLinkNode implements Serializable
{

	public String url;
	public int num;
	public int id;
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public int getNum()
	{
		return num;
	}
	public void setNum(int num)
	{
		this.num = num;
	}
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	
	
}
