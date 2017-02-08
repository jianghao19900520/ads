package com.ad.test;

import com.zzj.kp.AdsDataManager;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebView;

public class MainActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		WebView webview = (WebView) findViewById(R.id.webview);
		AdsDataManager.initWebView(this, webview);
		
	}

}
