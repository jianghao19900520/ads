package com.ads.library;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.ads.library.http.HttpAPi;
import com.ads.library.http.OnHttpCallBack;
import com.ads.library.node.AdsInitNode;
import com.qq.e.comm.util.StringUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AdManager
{

	private static AdManager instance = null;
	private Dialog dialog;
	private boolean dialogIsShow;
	public static final String TAG = "AdList-->";
	private Context mContext;
	private String user_id;
	private Boolean debug;
	private String mExdata;
	private Handler mHandler = new Handler(Looper.getMainLooper()){
		public void handleMessage(android.os.Message msg) {
			try
			{
				mContext.getSharedPreferences(TAG, Activity.MODE_PRIVATE).edit().putString("user_id", user_id).putBoolean("debug", debug).commit();
				if (dialogIsShow) return;

				dialog = new Dialog(mContext, R.style.transparent_dialog_bg_style);
				dialog.setContentView(new ProgressBar(mContext));
				dialog.setOnCancelListener(new OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface arg0)
					{
						dialogIsShow = false;
					}
				});
				
				dialog.show();
				dialogIsShow = true;
				doInit();
			}
			catch (Exception e)
			{
				Log.d(TAG, "init failed, error is:" + e.toString());
				if (dialog != null)
				{
					dialog.dismiss();
					dialog = null;
				}
			}
		};
	};

	public static AdManager getInstance()
	{
		if (instance == null)
		{
			instance = new AdManager();
		}
		return instance;
	}
	
	/**
	 * 设置透传参数
	 */
	public AdManager setExdata(String exdata){
		if (instance == null)
		{
			instance = new AdManager();
		}
		if(StringUtil.isEmpty(exdata)){
			mExdata = "";
		}else{
			mExdata = exdata;
		}
		return instance;
	}

	public void openAd(Context context, String user_id, boolean debug)
	{
		try
		{
			if (context == null)
			{
				ToastUtil.show(context, context.getString(R.string.init_failed));
				Log.d(TAG, "context is null");
				return;
			}
			if (StringUtil.isEmpty(user_id))
			{
				ToastUtil.show(context, context.getString(R.string.init_failed));
				logUtil(context.getString(R.string.init_failed_reason));
				return;
			}
			this.mContext = context;
			this.user_id = user_id;
			this.debug = debug;
			mHandler.sendEmptyMessage(0);
		}
		catch (Exception e)
		{
			Log.d(TAG, "init failed, error is:" + e.toString());
			if (dialog != null)
			{
				dialog.dismiss();
				dialog = null;
			}
		}
	}

	private void doInit()
	{
		try
		{
			TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			String imei = change(tm.getDeviceId());// IMEI
			String iccid = change(tm.getSimSerialNumber());// ICCID
			String imsi = change(tm.getSubscriberId());// IMSI
			String mac = change(getMacAddress());// MAC
			String app_pkg = change(mContext.getPackageName());// 包名
			String app_ver = change(mContext.getPackageManager().getPackageInfo(app_pkg, 0).versionName);// 版本号
			String os_ver = change(android.os.Build.VERSION.RELEASE);// 系统版本
			String os_model = change(android.os.Build.MODEL);// 手机型号

			HttpAPi.getInstance(mContext).adsInitHttp(app_pkg, mac, imei, imsi, iccid, app_ver, os_ver, os_model, user_id, mExdata, new OnHttpCallBack<AdsInitNode>()
			{

				@Override
				public void onSuccess(AdsInitNode node)
				{
					try
					{
						SharedPreferences.Editor editor = mContext.getSharedPreferences(TAG, Activity.MODE_PRIVATE).edit();
						editor.putString("my_app_id", node.getMy_app_id());
						
						editor.putString("shake_app_id", node.getShake().getApp_id());
						editor.putString("shake_ad_id", node.getShake().getAd_id());
						editor.putInt("shake_from", node.getShake().getFrom_type());
						editor.putInt("shake_confirm", node.getShake().getConfirm());
						
						editor.putString("random_app_id", node.getRandom().getApp_id());
						editor.putString("random_ad_id", node.getRandom().getAd_id());
						editor.putInt("random_from", node.getRandom().getFrom_type());
						editor.putInt("random_confirm", node.getRandom().getConfirm());
						
						editor.commit();
						if (dialog != null)
						{
							dialog.cancel();
							dialog = null;
						}
						Intent intent = new Intent(mContext, AdListActivity.class);
						mContext.startActivity(intent);
						logUtil("初始化接口成功！");
					}
					catch (Exception e)
					{
						ToastUtil.show(mContext, mContext.getString(R.string.init_failed));
						Log.d(TAG, "init failed, error is : " + e.toString());
						if (dialog != null)
						{
							dialog.dismiss();
							dialog = null;
						}
					}
				}

				@Override
				public void onFail(int httpCode, int statusCode, String msg)
				{
					ToastUtil.show(mContext, mContext.getString(R.string.init_failed));
					Log.d(TAG, "init failed, error is : " + msg);
					if (dialog != null)
					{
						dialog.dismiss();
						dialog = null;
					}
				}
			});
		}
		catch (Exception e)
		{
			ToastUtil.show(mContext, mContext.getString(R.string.init_failed));
			Log.d(TAG, "init failed , error is : " + e.toString());
			if (dialog != null)
			{
				dialog.dismiss();
				dialog = null;
			}
		}

	}

	private String change(String ss)
	{
		if (StringUtil.isEmpty(ss))
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

	private void logUtil(String msg)
	{
		boolean debug = mContext.getSharedPreferences(TAG, Activity.MODE_PRIVATE).getBoolean("debug", false);
		if (debug == true) Log.d(TAG, msg);
	}
}
