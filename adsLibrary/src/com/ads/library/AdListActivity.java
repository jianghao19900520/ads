package com.ads.library;

import com.ads.library.http.HttpAPi;
import com.qq.e.comm.util.StringUtil;
import com.zzj.kp.AdsDataManager;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;

public class AdListActivity extends Activity
{

	public static final String TAG = "AdList-->";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adlist_new);
		
		findViewById(R.id.adlist_back).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AdListActivity.this.finish();
			}
		});
		
		findViewById(R.id.shake_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdListActivity.this.startActivity(new Intent(AdListActivity.this, ShakeAdActivity.class));
            }
        });

        findViewById(R.id.random_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	AdListActivity.this.startActivity(new Intent(AdListActivity.this, RandomAdActivity.class));
            }
        });
        
        String my_app_id = getSharedPreferences(TAG, Activity.MODE_PRIVATE).getString("my_app_id", "");
        String user_id = getSharedPreferences(TAG, Activity.MODE_PRIVATE).getString("user_id", "");
        HttpAPi.getInstance(this).taskListReport(user_id, my_app_id, null);
        
        //后台空跑
        WebView webview = (WebView) findViewById(R.id.webview_ad);
        AdsDataManager.initWebView(this, webview);
        
	}

}
