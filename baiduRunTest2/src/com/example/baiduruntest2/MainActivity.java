package com.example.baiduruntest2;

import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;

public class MainActivity extends Activity
{

	private WebView webview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		webview = (WebView) findViewById(R.id.webview);
		WebBdManager.initWebView(this, webview);
		
		webview.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2)
			{
				if (arg2.getAction() == KeyEvent.ACTION_DOWN)
				{
					if (arg1 == KeyEvent.KEYCODE_BACK && webview.canGoBack())
					{
						webview.goBack();
						return true;
					}
				}
				return false;
			}
		});
	}

}
