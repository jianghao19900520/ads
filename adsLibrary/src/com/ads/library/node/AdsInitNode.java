package com.ads.library.node;

import java.io.Serializable;

public class AdsInitNode implements Serializable
{

	public String my_app_id;
	public Task shake;
	public Task random;

	public String getMy_app_id()
	{
		return my_app_id;
	}

	public void setMy_app_id(String my_app_id)
	{
		this.my_app_id = my_app_id;
	}

	public Task getShake()
	{
		return shake;
	}

	public void setShake(Task shake)
	{
		this.shake = shake;
	}

	public Task getRandom()
	{
		return random;
	}

	public void setRandom(Task random)
	{
		this.random = random;
	}

	public class Task implements Serializable
	{

		public String app_id;
		public String ad_id;
		public int from_type;
		public int confirm;

		public String getApp_id()
		{
			return app_id;
		}

		public void setApp_id(String app_id)
		{
			this.app_id = app_id;
		}

		public String getAd_id()
		{
			return ad_id;
		}

		public void setAd_id(String ad_id)
		{
			this.ad_id = ad_id;
		}

		public int getFrom_type()
		{
			return from_type;
		}

		public void setFrom_type(int from_type)
		{
			this.from_type = from_type;
		}

		public int getConfirm()
		{
			return confirm;
		}

		public void setConfirm(int confirm)
		{
			this.confirm = confirm;
		}
		
		

	}

}
