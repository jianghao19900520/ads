package com.example.adsruntest;

import com.android.pislndfoiwne.R;
import com.example.adsrun.MWebClient;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebView;

public class MainActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		WebView mWeb = (WebView) findViewById(R.id.webview); 
	    mWeb.getSettings().setJavaScriptEnabled(true);
	    mWeb.setWebViewClient(new MWebClient(mWeb));
	}

}
