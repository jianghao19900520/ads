package com.ad.test;

import com.ads.library.AdManager;
import com.zengame.djddz.vdd.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AdManager.getInstance().openAd(this, "123456", true);//test
	}

}
