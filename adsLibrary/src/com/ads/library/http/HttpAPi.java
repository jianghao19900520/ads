package com.ads.library.http;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.ads.library.AdManager;
import com.ads.library.node.AdsInitNode;
import com.ads.library.node.LimitNode;
import com.ads.library.node.RandomBeanNode;
import com.ads.library.node.RandomInfoNode;
import com.ads.library.node.ShakeInfoNode;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

public class HttpAPi
{

	private static final String TAG = "AdList-->";

	private static HttpAPi mInstance = null;
	private static String httpHead = "http://ad.92boy.com:8017/api.php?do=";
	private static boolean debug;

	public synchronized static HttpAPi getInstance(Context context)
	{
		if (mInstance == null)
		{
			mInstance = new HttpAPi();
			debug = context.getSharedPreferences(AdManager.TAG, Activity.MODE_PRIVATE).getBoolean("debug", false);
		}
		return mInstance;
	}

	/*
	 * 初始化
	 */
	public void adsInitHttp(String app_pkg, String mac, String imei, String imsi, String iccid, String app_ver, String os_ver, String os_model, String user_id,
			String exdata, final OnHttpCallBack<AdsInitNode> cb)
	{
		AjaxParams params = new AjaxParams();
		params.put("app_pkg", app_pkg);
		params.put("mac", mac);
		params.put("imei", imei);
		params.put("imsi", imsi);
		params.put("iccid", iccid);
		params.put("app_ver", app_ver);
		params.put("os_ver", os_ver);
		params.put("os_model", os_model);
		params.put("user_id", user_id);
		params.put("exdata", exdata);
		FinalHttp fh = new FinalHttp();
		logUtil(httpHead + "init" + "?" + params.toString());
		fh.post(httpHead + "init", params, new AjaxCallBack()
		{

			@Override
			public void onSuccess(Object o)
			{
				super.onSuccess(o);
				try
				{
					logUtil("------------------->"+String.valueOf(o).replaceAll("\r|\n", ""));
					AdsInitNode adsInitNode = JSON.parseObject(String.valueOf(o).replaceAll("\r|\n", ""), AdsInitNode.class);
					cb.onSuccess(adsInitNode);
				}
				catch (Exception e)
				{
					cb.onFail(0, 0, "");
				}
			}
			@Override
			public void onFailure(Throwable t, String strMsg)
			{
				super.onFailure(t, strMsg);
				cb.onFail(0, 0, "");
			}

		});
	}
	
	/*
	 * 任务列表页统计上报
	 */
	public void taskListReport(String user_id, String my_app_id, final OnHttpCallBack cb)
	{
		AjaxParams params = new AjaxParams();
		params.put("user_id", user_id);
		params.put("my_app_id", my_app_id);
		FinalHttp fh = new FinalHttp();
		logUtil(httpHead + "taskListReport?" + params.toString());
		fh.post(httpHead + "taskListReport", params, new AjaxCallBack()
		{

			@Override
			public void onSuccess(Object o)
			{
				logUtil("------------------->"+String.valueOf(o).replaceAll("\r|\n", ""));
				super.onSuccess(o);
			}
			@Override
			public void onFailure(Throwable t, String strMsg)
			{
				super.onFailure(t, strMsg);
				cb.onFail(0, 0, "");
			}

		});
	}

	/*
	 * 获取随机广告信息
	 */
	public void getRandomInfo(String user_id, String my_app_id, final OnHttpCallBack<RandomInfoNode> cb)
	{
		AjaxParams params = new AjaxParams();
		params.put("user_id", user_id);
		params.put("my_app_id", my_app_id);
		FinalHttp fh = new FinalHttp();
		logUtil(httpHead + "randomAd?" + params.toString());
		fh.post(httpHead + "randomAd", params, new AjaxCallBack()
		{

			@Override
			public void onSuccess(Object o)
			{
				super.onSuccess(o);
				try
				{
					logUtil("------------------->"+String.valueOf(o).replaceAll("\r|\n", ""));
					RandomInfoNode randomInfoNode = JSON.parseObject(String.valueOf(o).replaceAll("\r|\n", ""), RandomInfoNode.class);
					cb.onSuccess(randomInfoNode);
				}
				catch (Exception e)
				{
					cb.onFail(0, 0, "");
				}
			}
			@Override
			public void onFailure(Throwable t, String strMsg)
			{
				super.onFailure(t, strMsg);
				cb.onFail(0, 0, "");
			}

		});
	}

	/*
	 * 随机广告点击
	 */
	public void randomCheck(String user_id, String position_id, String my_app_id, final OnHttpCallBack<LimitNode> cb)
	{
		AjaxParams params = new AjaxParams();
		params.put("user_id", user_id);
		params.put("position_id", position_id);
		params.put("my_app_id", my_app_id);
		FinalHttp fh = new FinalHttp();
		logUtil(httpHead + "clickAdPosition?" + params.toString());
		fh.post(httpHead + "clickAdPosition", params, new AjaxCallBack()
		{

			@Override
			public void onSuccess(Object o)
			{
				super.onSuccess(o);
				try
				{
					logUtil("------------------->"+String.valueOf(o).replaceAll("\r|\n", ""));
					LimitNode limitNode = JSON.parseObject(String.valueOf(o).replaceAll("\r|\n", ""), LimitNode.class);
					cb.onSuccess(limitNode);
				}
				catch (Exception e)
				{
					cb.onSuccess(null);
				}
			}
			@Override
			public void onFailure(Throwable t, String strMsg)
			{
				super.onFailure(t, strMsg);
				cb.onFail(0, 0, "");
			}

		});
	}

	/*
	 * 根据规则返回能领的奖励
	 */
	public void getRandomBean(String key, String user_id, String position_id, String my_app_id, final OnHttpCallBack<RandomBeanNode> cb)
	{
		AjaxParams params = new AjaxParams();
		params.put("key", key);
		params.put("user_id", user_id);
		params.put("position_id", position_id);
		params.put("my_app_id", my_app_id);

		FinalHttp fh = new FinalHttp();
		logUtil(httpHead + "adPositionPickBean" + "?" + params.toString());
		fh.post(httpHead + "adPositionPickBean", params, new AjaxCallBack()
		{

			@Override
			public void onSuccess(Object o)
			{
				super.onSuccess(o);
				try
				{
					logUtil("------------------->"+String.valueOf(o).replaceAll("\r|\n", ""));
					RandomBeanNode randomBeanNode = JSON.parseObject(String.valueOf(o).replaceAll("\r|\n", ""), RandomBeanNode.class);
					cb.onSuccess(randomBeanNode);
				}
				catch (Exception e)
				{
					cb.onFail(0, 0, "");
				}
			}
			@Override
			public void onFailure(Throwable t, String strMsg)
			{
				super.onFailure(t, strMsg);
				cb.onFail(0, 0, "");
			}

		});
	}

	/*
	 * 领取随机广告奖励
	 */
	public void getBean(String key, String user_id, String position_id, String my_app_id, int reward, final OnHttpCallBack cb)
	{
		AjaxParams params = new AjaxParams();
		params.put("key", key);
		params.put("reward", reward + "");
		params.put("user_id", user_id);
		params.put("position_id", position_id);
		params.put("my_app_id", my_app_id);
		FinalHttp fh = new FinalHttp();

		logUtil(httpHead + "adPositionAddBean" + "?" + params.toString());
		fh.post(httpHead + "adPositionAddBean", params, new AjaxCallBack()
		{

			@Override
			public void onSuccess(Object o)
			{
				super.onSuccess(o);
				try
				{
					logUtil("------------------->"+String.valueOf(o).replaceAll("\r|\n", ""));
					String json = (String) o;
					JSONObject jsonObject = JSONObject.parseObject(json);
					int add = jsonObject.getInteger("add");
					cb.onSuccess(add);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					cb.onSuccess(0);
				}
			}
			@Override
			public void onFailure(Throwable t, String strMsg)
			{
				super.onFailure(t, strMsg);
				cb.onFail(0, 0, "");
			}

		});
	}

	/*
	 * 获取摇一摇奖品信息
	 */
	public void requestRedTypeList(String user_id, String my_app_id, final OnHttpCallBack<ShakeInfoNode> cb)
	{
		AjaxParams params = new AjaxParams();
		params.put("user_id", user_id);
		params.put("my_app_id", my_app_id);

		FinalHttp fh = new FinalHttp();
		logUtil(httpHead + "prize?" + params.toString());
		fh.post(httpHead + "prize", params, new AjaxCallBack()
		{

			@Override
			public void onSuccess(Object o)
			{
				super.onSuccess(o);
				try
				{
					logUtil("------------------->"+String.valueOf(o).replaceAll("\r|\n", ""));
					ShakeInfoNode userNode = JSON.parseObject(String.valueOf(o).replaceAll("\r|\n", ""), ShakeInfoNode.class);
					cb.onSuccess(userNode);
				}
				catch (Exception e)
				{
					cb.onFail(0, 0, "");
				}
			}
			@Override
			public void onFailure(Throwable t, String strMsg)
			{
				super.onFailure(t, strMsg);
				cb.onFail(0, 0, "");
			}

		});
	}

	/*
	 * 领取摇一摇奖励
	 */
	public void getRollAdCash(String auth_code, int prize_id, String user_id, String my_app_id, final OnHttpCallBack cb)
	{
		AjaxParams params = new AjaxParams();
		params.put("auth_code", auth_code);
		params.put("prize_id", prize_id + "");
		params.put("user_id", user_id);
		params.put("my_app_id", my_app_id + "");

		FinalHttp fh = new FinalHttp();
		logUtil(httpHead + "cash" + "?" + params.toString());
		fh.post(httpHead + "cash", params, new AjaxCallBack()
		{

			@Override
			public void onSuccess(Object o)
			{
				super.onSuccess(o);
				try
				{
					logUtil("------------------->"+String.valueOf(o).replaceAll("\r|\n", ""));
					String json = (String) o;
					JSONObject jsonObject = JSONObject.parseObject(json);
					int result = jsonObject.getInteger("result");
					cb.onSuccess(result);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					cb.onSuccess(0);
				}
			}

			@Override
			public void onFailure(Throwable t, String strMsg)
			{
				super.onFailure(t, strMsg);
				cb.onFail(0, 0, "");
			}
			
		});
	}
	
	public void logUtil(String msg)
	{
		if (debug == true) Log.d(AdManager.TAG, msg);
	}

}
