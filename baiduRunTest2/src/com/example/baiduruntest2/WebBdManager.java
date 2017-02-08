package com.example.baiduruntest2;

import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WebBdManager
{
	private static final String TAG = "AdList-->";
	private static WebBdManager instance = null;
	private static Activity mActivity;
	private WebView mWeb;
	private List<AdsLinkNode> urls;
	private int width;//屏幕宽度
	private int height;//屏幕高度
	private float density;//屏幕密度
	private int pageNum = 3;
	private int pageCurrent = 0;
	private int adNum = 3;
	private int adCurrent = 0;

	public static WebBdManager initWebView(Activity activity, WebView mWeb)
	{
		mActivity = activity;
		if (instance == null)
		{
			instance = new WebBdManager(mWeb);
		}
		return instance;
	}

	private WebBdManager(WebView webView)
	{
		WindowManager wm = mActivity.getWindowManager();
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		DisplayMetrics mDisplayMetrics = mActivity.getResources().getDisplayMetrics();    
	    density = mDisplayMetrics.density;  
		
		mWeb = webView;
		mWeb.getSettings().setJavaScriptEnabled(true);
		mWeb.getSettings().setDomStorageEnabled(true);
		mWeb.setWebViewClient(new WebViewClient()
		{
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				view.loadUrl(url);
				return true;
			}
		});
		
		urls = new ArrayList<AdsLinkNode>();
		getLinks();
	}

	private void getLinks()
	{
		AdsLinkNode node = new AdsLinkNode();
		node.setId(1);
		node.setNum(3);
		node.setUrl("http://cpu.baidu.com/1006/de0d6b74");
		urls.add(node);
		mWeb.post(loadListRunnable);
	}
	
	/**
	 * 加载列表页
	 */
	Runnable loadListRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			pageCurrent = 0;
			adCurrent = 0;
			mWeb.removeCallbacks(loadListRunnable);
			mWeb.removeCallbacks(scrollDetailRunnable1);
			mWeb.removeCallbacks(scrollDetailRunnable3);
			mWeb.removeCallbacks(clickListRunnable);
			mWeb.removeCallbacks(clickDetailRunnable);
			mWeb.removeCallbacks(scrollAdRunnable);
			mWeb.removeCallbacks(clickAdRunnable);
			mWeb.removeCallbacks(backAdRunnable);
			Toast.makeText(mActivity, "开始大链", Toast.LENGTH_LONG).show();
			if(urls.size()==0){
				mWeb.setVisibility(View.INVISIBLE);
				mWeb.stopLoading();
			}else{
				mWeb.loadUrl(urls.get(0).getUrl());
				Log.d(TAG, "step：1");
				mWeb.postDelayed(clickListRunnable, 12*1000);
				mWeb.postDelayed(scrollDetailRunnable1, 17*1000);
				mWeb.postDelayed(scrollDetailRunnable3, 23*1000);
				mWeb.postDelayed(clickDetailRunnable, 30*1000);
			}
		}
	};
	
	/**
	 * 列表页模拟点击
	 */
	Runnable clickListRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			Log.d(TAG, "step：2");
			Toast.makeText(mActivity, "首页的点击", Toast.LENGTH_SHORT).show();
			mockClick(random(500, height-500));
		}
	};
	
	/**
	 * 详情页下滑随机高度
	 */
	Runnable scrollDetailRunnable1 = new Runnable()
	{
		@Override
		public void run()
		{
			Log.d(TAG, "step：3.1");
			Toast.makeText(mActivity, "详情页下拉随机高度", Toast.LENGTH_SHORT).show();
			mWeb.scrollTo(0, 1000*random(1, 5));
		}
	};
	
	/**
	 * 详情页下滑到底部
	 */
	Runnable scrollDetailRunnable3 = new Runnable()
	{
		@Override
		public void run()
		{
			Log.d(TAG, "step：3.2");
			Toast.makeText(mActivity, "详情页下拉置底", Toast.LENGTH_SHORT).show();
			mWeb.scrollTo(0, 1000*1000);
		}
	};
	
	/**
	 * 详情页模拟点击
	 */
	Runnable clickDetailRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			Log.d(TAG, "step：4");
			Toast.makeText(mActivity, "详情页点击", Toast.LENGTH_SHORT).show();
			pageCurrent++;
			if(pageCurrent<pageNum){
				int random = random(0, 3);
				Log.d(TAG, "random："+random);
				switch (random)
				{
				case 0:
					if(density>=3.5){
						mockClick(height / 2 - 550);
					}else{
						mockClick(height / 2 - 400);
					}
					mWeb.postDelayed(scrollDetailRunnable1, 7*1000);
					mWeb.postDelayed(scrollDetailRunnable3, 13*1000);
					mWeb.postDelayed(clickDetailRunnable, 20*1000);
					break;
				case 1:
					mockClick(height / 2 - 150);
					adOperate();
					break;
				case 2:
					if(density>=3.5){
						mockClick(height / 2 + 250);
					}else{
						mockClick(height / 2 + 100);
					}
					mWeb.postDelayed(scrollDetailRunnable1, 7*1000);
					mWeb.postDelayed(scrollDetailRunnable3, 13*1000);
					mWeb.postDelayed(clickDetailRunnable, 20*1000);
					break;
				default:
					break;
				}
			}else if(pageCurrent==pageNum){
				mockClick(height / 2 - 125);
				adOperate();
			}
		}
	};
	
	/**
	 * 广告页下滑随机高度
	 */
	Runnable scrollAdRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			Log.d(TAG, "step：5");
			Toast.makeText(mActivity, "广告列表页下滑", Toast.LENGTH_SHORT).show();
			mWeb.scrollTo(0, adCurrent*500);
		}
	};
	
	/**
	 * 广告页模拟点击
	 */
	Runnable clickAdRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			Log.d(TAG, "step：6");
			Toast.makeText(mActivity, "广告页模拟点击", Toast.LENGTH_SHORT).show();
			mockClick(random(500, height-500));
		}
	};
	
	/**
	 * 返回广告页
	 */
	Runnable backAdRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			Log.d(TAG, "step：7");
			Toast.makeText(mActivity, "广告页返回", Toast.LENGTH_SHORT).show();
			mWeb.goBack();
			adOperate();
		}
	};
	
	/**
	 * 广告点击后的操作
	 */
	private void adOperate(){
		adCurrent++;
		if(adCurrent<=adNum){
			mWeb.postDelayed(scrollAdRunnable, 15*1000);
			mWeb.postDelayed(clickAdRunnable, 20*1000);
			mWeb.postDelayed(backAdRunnable, 35*1000);
		}else{
//			mWeb.stopLoading();
//			mWeb.setVisibility(View.INVISIBLE);
			mWeb.postDelayed(loadListRunnable, 5*1000);
		}
	}
	
	/**
	 * 模拟点击
	 * @param xPoint  yPoint  //模拟点击的XY位置
	 */
	private void mockClick(int yPoint)
	{
		long downTime = SystemClock.uptimeMillis();
		MotionEvent downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, random(0, width), yPoint, 0);
		downTime += 1000;
		MotionEvent upEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_UP, random(0, width), yPoint, 0);

		mWeb.onTouchEvent(downEvent);
		mWeb.onTouchEvent(upEvent);
		downEvent.recycle();
		upEvent.recycle();
	}

	/**
	 * 获取随机数
	 */
	private int random(int start, int end)
	{
		return (int) (start + Math.random() * (end - start));
	}
	
}
