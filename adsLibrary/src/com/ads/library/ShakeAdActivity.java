package com.ads.library;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ads.library.http.HttpAPi;
import com.ads.library.http.OnHttpCallBack;
import com.ads.library.node.ShakeInfoNode;
import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.qq.e.ads.cfg.DownAPPConfirmPolicy;
import com.qq.e.ads.nativ.NativeAD;
import com.qq.e.ads.nativ.NativeADDataRef;
import com.qq.e.comm.util.StringUtil;

import java.util.HashMap;
import java.util.List;

@SuppressLint("NewApi")
public class ShakeAdActivity extends Activity implements SensorEventListener
{

	private Activity mContext;
	private int shake_from = 0;// 1=广点通 2=百度
	private int shake_confirm = 0;// 0=开 1=关
	private String shake_app_id = "";
	private String shake_ad_id = "";
	private String my_app_id = "";
	private String user_id = "";

	private RelativeLayout mAnimLy;
	private ImageView mAnimView;
	private Animation mGetingAnim;
	private RelativeLayout mTokenEmptyLy;
	private Button network_err_btn;
	private Vibrator vibrator;// 震动
	private SensorManager manager;// 震动管理器
	private SoundPool sndPool;// 声音
	private HashMap<Integer, Integer> soundPoolMap = new HashMap<Integer, Integer>();
	private boolean vibratorAble = false; // 震动开关 false=关闭 true=打开
	private NativeAD nativeAD; // 广点通
	private BaiduNative baidu;// 百度广告
	private List<NativeADDataRef> gdtList;// 广点通广告缓存列表
	private List<NativeResponse> bdList;// 百度广告缓存列表
	private NativeADDataRef gdtDataRef; // 当前展示的广告
	private NativeResponse bdDataRef; // 当前展示的广告
	private ImageView shake_help_img;
	private Animation shakeAnim;// 摇晃动画
	private ImageView shake_center_iv;
	private ImageView shake_dialog_bg;
	private Dialog helpDialog;
	private Dialog fristDialog;
	private ShakeInfoNode mRollAdPrizeNode;
	private Bitmap adBitmap;
	private Dialog getLdDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shake_ad);
		mContext = ShakeAdActivity.this;
		SharedPreferences preferences = getSharedPreferences(AdManager.TAG, Activity.MODE_PRIVATE);
		shake_from = preferences.getInt("shake_from", 0);
		shake_confirm = preferences.getInt("shake_confirm", 0);
		shake_app_id = preferences.getString("shake_app_id", "");
		shake_ad_id = preferences.getString("shake_ad_id", "");
		my_app_id = preferences.getString("my_app_id", "");
		user_id = preferences.getString("user_id", "");

		// shake_from = 1;
		// shake_app_id = "e12450b5";
		// shake_ad_id = "2896037";
		// shake_from = 2;
		// shake_app_id = "1104745779";
		// shake_ad_id = "5000709048439488";
		if (shake_from == 0 || StringUtil.isEmpty(shake_app_id) || StringUtil.isEmpty(shake_ad_id))
		{
			logUtil("shake_from or shake_app_id or shake_ad_id is empty");
			errorLoading();
			return;
		}

		shakeAnim = AnimationUtils.loadAnimation(mContext, R.anim.shake_y);
		shakeAnim.setFillAfter(true);
		vibratorAble = true;
		mAnimLy = (RelativeLayout) findViewById(R.id.geting_anim_ly);
		mAnimView = (ImageView) findViewById(R.id.geting_anim_icon);
		mGetingAnim = AnimationUtils.loadAnimation(mContext, R.anim.dialog_wait_rotate);
		mTokenEmptyLy = (RelativeLayout) findViewById(R.id.getinfo_token_empty_ly);
		network_err_btn = (Button) findViewById(R.id.network_err_btn);
		shake_help_img = (ImageView) findViewById(R.id.shake_help_img);
		shake_center_iv = (ImageView) findViewById(R.id.shake_center_iv);
		shake_dialog_bg = (ImageView) findViewById(R.id.shake_dialog_bg);
		shake_help_img.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				takeHelpDialog();
			}
		});
		findViewById(R.id.shake_back).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ShakeAdActivity.this.finish();
			}
		});

		if (shake_from == 2)
		{
			fetchBdAd();
		}
		else if (shake_from == 1)
		{
			fetchGdtAd();
		}
		else
		{
			errorLoading();
			logUtil("shake_from is " + shake_from);
		}

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		initVibrator();
	}

	public void loading()
	{
		mGetingAnim = AnimationUtils.loadAnimation(mContext, R.anim.dialog_wait_rotate);
		mGetingAnim.setFillAfter(true);
		mAnimView.startAnimation(mGetingAnim);
		mTokenEmptyLy.setVisibility(View.INVISIBLE);
		mAnimLy.setVisibility(View.VISIBLE);
	}

	public void cancelLoading()
	{
		if (mAnimView != null) mAnimView.clearAnimation();
		mTokenEmptyLy.setVisibility(View.INVISIBLE);
		mAnimLy.setVisibility(View.INVISIBLE);
	}

	public void errorLoading()
	{
		if (mAnimView != null) mAnimView.clearAnimation();
		mTokenEmptyLy.setVisibility(View.VISIBLE);
		mAnimLy.setVisibility(View.INVISIBLE);
		network_err_btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (shake_from == 2)
				{
					fetchBdAd();
				}
				else if (shake_from == 1)
				{
					fetchGdtAd();
				}
			}
		});
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (manager != null) manager.unregisterListener(this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (mAnimView != null) mAnimView.clearAnimation();
	}

	/*
	 * 初始化摇一摇传感器
	 */
	public void initVibrator()
	{
		// 获取传感器管理服务
		manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// 震动
		vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		loadSound();
		Sensor mSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		manager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (vibratorAble == false) return;
		int sensorType = event.sensor.getType();
		float[] val = event.values;
		if (sensorType == Sensor.TYPE_ACCELEROMETER)
		{
			if ((Math.abs(val[0]) > 11 || Math.abs(val[1]) > 11 || Math.abs(val[2]) > 19))
			{
				// 摇动手机后，再伴随震动提示~~
				vibratorAble = false;
				vibrator.vibrate(1000);
				sndPool.play(soundPoolMap.get(0), (float) 0.2, (float) 0.2, 0, 0, (float) 0.6);
				logUtil("get shake mobile");
				getRollPrizeInfo();
				sndPool.play(soundPoolMap.get(1), (float) 0.2, (float) 0.2, 0, 0, (float) 0.6);
				if (shake_center_iv != null && shakeAnim != null) shake_center_iv.startAnimation(shakeAnim);
			}
		}
	}

	public void doShake()
	{
		if (shake_from == 2)
		{
			if (bdList != null && bdList.size() > 0)
			{
				bdDataRef = bdList.get(0);
				bdList.remove(bdDataRef);
				new Handler().postDelayed(new Runnable()
				{
					public void run()
					{
						takeGetLdDialog();
					}
				}, 4000);
				getUrlBitmap(bdDataRef.getImageUrl());
			}
			else
			{
				if (shake_center_iv != null) shake_center_iv.clearAnimation();
				ToastUtil.show(mContext, getString(R.string.shake_network_again));
				vibratorAble = true;
				fetchBdAd();
			}
		}
		else if (shake_from == 1)
		{
			if (gdtList != null && gdtList.size() > 0)
			{
				gdtDataRef = gdtList.get(0);
				gdtList.remove(gdtDataRef);
				new Handler().postDelayed(new Runnable()
				{
					public void run()
					{
						takeGetLdDialog();
					}
				}, 4000);
				getUrlBitmap(gdtDataRef.getImgUrl());
			}
			else
			{
				if (shake_center_iv != null) shake_center_iv.clearAnimation();
				ToastUtil.show(mContext, getString(R.string.shake_network_again));
				vibratorAble = true;
				fetchGdtAd();
			}
		}
		else
		{
			errorLoading();
			logUtil("shake_from is " + shake_from);
		}

	}

	/**
	 * 获取音效
	 */
	private void loadSound()
	{
		sndPool = new SoundPool(2, AudioManager.STREAM_SYSTEM, 5);
		new Thread()
		{
			public void run()
			{
				soundPoolMap.put(0, sndPool.load(mContext, R.raw.shake_sound_male, 1));

				soundPoolMap.put(1, sndPool.load(mContext, R.raw.shake_match, 1));
			}
		}.start();
	}

	/**
	 * 红包对话框
	 */
	private void takeGetLdDialog()
	{
		if (ShakeAdActivity.this.isFinishing()) return;
		if (getLdDialog == null)
		{
			getLdDialog = new Dialog(ShakeAdActivity.this, R.style.transparent_dialog_bg_style);
			getLdDialog.setCanceledOnTouchOutside(false);
			// 监听弹出对话框时的back键
			getLdDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
			{
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
				{
					if (keyCode == KeyEvent.KEYCODE_BACK)
					{
						dialog.dismiss();
						shake_dialog_bg.setVisibility(View.GONE);
						vibratorAble = true;
						return true;
					}
					else
					{
						return false;
					}
				}
			});
			getLdDialog.setContentView(LayoutInflater.from(mContext).inflate(R.layout.shake_red_dialog_layout, null));
		}
		if (getLdDialog.isShowing()) return;
		getLdDialog.show();
		Window window = getLdDialog.getWindow();
		window.setGravity(Gravity.CENTER);
		ImageView shake_dialog_ad_iv = (ImageView) window.findViewById(R.id.shake_dialog_ad_iv);
		if (shake_from == 2)
		{
			if (adBitmap != null)
			{
				shake_dialog_ad_iv.setImageBitmap(adBitmap);
			}
			else
			{
				shake_dialog_ad_iv.setImageResource(R.drawable.shake_loading_loadfailed_bg);
			}
			bdDataRef.recordImpression(shake_dialog_ad_iv);
		}
		else
		{
			if (adBitmap != null)
			{
				shake_dialog_ad_iv.setImageBitmap(adBitmap);
			}
			else
			{
				shake_dialog_ad_iv.setImageResource(R.drawable.random_loading_loadfailed_bg);
			}
			gdtDataRef.onExposured(shake_dialog_ad_iv);
		}
		window.findViewById(R.id.shake_dialog_get_iv).setOnClickListener(new android.view.View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mContext.getSharedPreferences(AdManager.TAG, Activity.MODE_PRIVATE).getBoolean("hasShaked", false) == false)
				{
					takeFristDialog();
					mContext.getSharedPreferences(AdManager.TAG, Activity.MODE_PRIVATE).edit().putBoolean("hasShaked", true).commit();
				}
				else
				{
					vibratorAble = true;
					getLdDialog.dismiss();
					shake_dialog_bg.setVisibility(View.GONE);
					if (shake_from == 2)
					{
						getPrize(null, bdDataRef, v, mRollAdPrizeNode.getAuth_code(), mRollAdPrizeNode.getPrize_id());
					}
					else
					{
						getPrize(gdtDataRef, null, v, mRollAdPrizeNode.getAuth_code(), mRollAdPrizeNode.getPrize_id());
					}
				}
			}
		});
		window.findViewById(R.id.shake_dialog_delete_iv).setOnClickListener(new android.view.View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				vibratorAble = true;
				getLdDialog.dismiss();
				shake_dialog_bg.setVisibility(View.GONE);
			}
		});
		if (mRollAdPrizeNode != null)
		{
			TextView shake_dialog_name_iv = (TextView) window.findViewById(R.id.shake_dialog_name_iv);
			TextView shake_dialog_des_iv = (TextView) window.findViewById(R.id.shake_dialog_des_iv);
			shake_dialog_name_iv.setText("+" + mRollAdPrizeNode.getFace_value() + mContext.getString(R.string.ads_rewards));
			shake_dialog_des_iv.setText(mRollAdPrizeNode.getMessage());
		}
		getLdDialog.setOnCancelListener(new OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				vibratorAble = true;
			}
		});
		if (shake_center_iv != null) shake_center_iv.clearAnimation();
		shake_dialog_bg.setVisibility(View.VISIBLE);
	}

	/*
	 * 首次摇一摇对话框
	 */
	private void takeFristDialog()
	{
		if (ShakeAdActivity.this.isFinishing()) return;
		fristDialog = new Dialog(ShakeAdActivity.this, R.style.transparent_dialog_bg_style);
		View dialogView = LayoutInflater.from(mContext).inflate(R.layout.shake_frist_dialog_layout, null);
		fristDialog.setContentView(dialogView);
		fristDialog.show();
		fristDialog.setCanceledOnTouchOutside(true);
		Window window = fristDialog.getWindow();
		window.findViewById(R.id.shake_frist_btn).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				fristDialog.dismiss();
			}
		});
	}

	/*
	 * 提示对话框
	 */
	private void takeHelpDialog()
	{
		if (ShakeAdActivity.this.isFinishing()) return;
		helpDialog = new Dialog(ShakeAdActivity.this, R.style.transparent_dialog_bg_style);
		View dialogView = LayoutInflater.from(mContext).inflate(R.layout.shake_help_dialog_layout, null);
		helpDialog.setContentView(dialogView);
		helpDialog.show();
		helpDialog.setCanceledOnTouchOutside(true);
		dialogView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				helpDialog.dismiss();
			}
		});
		Window window = helpDialog.getWindow();
		try
		{
			WindowManager.LayoutParams lp = window.getAttributes();
			int[] location = new int[2];
			shake_help_img.getLocationOnScreen(location);
			int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			shake_help_img.measure(w, h);
			int textViewHeight = shake_help_img.getMeasuredHeight();
			dialogView.measure(w, h);
			int dialogHeight = dialogView.getMeasuredHeight();
			lp.y = location[1] - dialogHeight - (textViewHeight / 3);
			window.setAttributes(lp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	/*
	 * 获取广点通广告
	 */
	public void fetchGdtAd()
	{
		loading();
		this.nativeAD = new NativeAD(mContext.getApplicationContext(), shake_app_id, shake_ad_id, new NativeAD.NativeAdListener()
		{
			@Override
			public void onADLoaded(List<NativeADDataRef> list)
			{
				if (list != null && list.size() > 0)
				{
					logUtil("gdt download success , size is : " + list.size());
					gdtList = list;
					cancelLoading();
				}
				else
				{
					logUtil("gdt download failed");
					errorLoading();
				}
			}

			@Override
			public void onNoAD(int i)
			{
				logUtil("gdt download failed , error is : " + i);
				errorLoading();
			}

			@Override
			public void onADStatusChanged(NativeADDataRef nativeADDataRef)
			{
			}
		});
		int count = 10; // 一次拉取的广告条数：范围1-30
		if (shake_confirm == 1)
		{
			nativeAD.setDownAPPConfirmPolicy(DownAPPConfirmPolicy.Default);
		}
		else
		{
			nativeAD.setDownAPPConfirmPolicy(DownAPPConfirmPolicy.NOConfirm);
		}
		nativeAD.loadAD(count);
	}

	/*
	 * 获取百度广告
	 */
	public void fetchBdAd()
	{
		AdView.setAppSid(mContext, shake_app_id);
		baidu = new BaiduNative(mContext, shake_ad_id, new BaiduNative.BaiduNativeNetworkListener()
		{
			@Override
			public void onNativeFail(NativeErrorCode arg0)
			{
				errorLoading();
				logUtil("baidu download failed , error is : " + arg0);
			}

			@Override
			public void onNativeLoad(List<NativeResponse> arg0)
			{
				if (arg0 != null && arg0.size() > 0)
				{
					bdList = arg0;
					cancelLoading();
					logUtil("baidu download success , size is : " + arg0.size());
				}
				else
				{
					errorLoading();
					logUtil("baidu download failed");
				}
			}
		});
		if (shake_confirm == 1)
		{
			RequestParameters requestParameters = new RequestParameters.Builder().confirmDownloading(true).build();
			baidu.makeRequest(requestParameters);
		}
		else
		{
			RequestParameters requestParameters = new RequestParameters.Builder().confirmDownloading(false).build();
			baidu.makeRequest(requestParameters);
		}
	}

	/*
	 * 将广告图片下载转为bitmap
	 */
	public void getUrlBitmap(String imgUrl)
	{

		adBitmap = null;

		if (StringUtil.isEmpty(imgUrl)) { return; }

		ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(ShakeAdActivity.this));

		ImageLoader.getInstance().loadImage(imgUrl, new ImageLoadingListener()
		{

			@Override
			public void onLoadingStarted(String arg0, View arg1)
			{
				logUtil("start download pic");
			}

			@Override
			public void onLoadingFailed(String arg0, View arg1, FailReason arg2)
			{
				logUtil("download pic failed , error is : " + arg2);
			}

			@Override
			public void onLoadingComplete(String arg0, View arg1, Bitmap arg2)
			{
				adBitmap = arg2;
				logUtil("download pic success");
			}

			@Override
			public void onLoadingCancelled(String arg0, View arg1)
			{
				logUtil("download pic cancle");
			}
		});
	}

	/*
	 * 获取摇一摇奖品信息
	 */
	public void getRollPrizeInfo()
	{
		HttpAPi.getInstance(mContext).requestRedTypeList(user_id, my_app_id, new OnHttpCallBack<ShakeInfoNode>()
		{
			@Override
			public void onSuccess(ShakeInfoNode shakeInfoNode)
			{
				if (shakeInfoNode != null)
				{
					mRollAdPrizeNode = shakeInfoNode;
					doShake();
					logUtil("getRollPrizeInfo success");
				}
				else
				{
					vibratorAble = true;
					ToastUtil.show(mContext, getString(R.string.shake_network_again));
					if (shake_center_iv != null) shake_center_iv.clearAnimation();
					logUtil("getRollPrizeInfo failed");
				}
			}

			@Override
			public void onFail(int httpCode, int statusCode, String msg)
			{
				vibratorAble = true;
				ToastUtil.show(mContext, getString(R.string.shake_network_again));
				if (shake_center_iv != null) shake_center_iv.clearAnimation();
				logUtil("getRollPrizeInfo failed");
			}
		});
	}

	/*
	 * 领取奖品
	 */
	public void getPrize(final NativeADDataRef clickAdDataRef, final NativeResponse bdDataRef, final View clickView, String authCode, int prizeId)
	{
		if (mRollAdPrizeNode == null)
		{
			ToastUtil.show(mContext, getString(R.string.shake_network_again));
			return;
		}
		HttpAPi.getInstance(mContext).getRollAdCash(authCode, prizeId, user_id, my_app_id, new OnHttpCallBack()
		{
			@Override
			public void onSuccess(Object o)
			{
				logUtil("getPrize success");
				Integer result = 0;
				try
				{
					result = Integer.valueOf(String.valueOf(o));
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
				switch (result)
				{
				case 0:
					ToastUtil.show(mContext, getString(R.string.get_info_network_err_tip));
					break;
				case 1:
					ToastUtil.show(mContext, String.format(mContext.getString(R.string.ad_get_award_suc), mRollAdPrizeNode.getFace_value() + ""));
					if (clickAdDataRef != null && clickView != null)
					{
						clickAdDataRef.onClicked(clickView);
					}
					if (bdDataRef != null && clickView != null)
					{
						bdDataRef.handleClick(clickView);
					}
					break;
				case -1:
					ToastUtil.show(mContext, getString(R.string.shake_get_limit));
					break;
				case -2:
					ToastUtil.show(mContext, getString(R.string.shake_get_often));
					break;
				case -3:
					ToastUtil.show(mContext, getString(R.string.shake_get_token_fail));
					break;
				case -4:
					ToastUtil.show(mContext, getString(R.string.shake_get_fail));
					break;
				}
			}

			@Override
			public void onFail(int httpCode, int statusCode, String msg)
			{
				logUtil("getPrize failed");
				ToastUtil.show(mContext, getString(R.string.shake_network_again));
			}
		});
	}

	public void logUtil(String msg)
	{
		boolean debug = mContext.getSharedPreferences(AdManager.TAG, Activity.MODE_PRIVATE).getBoolean("debug", false);
		if (debug == true) Log.d(AdManager.TAG, msg);
	}

}
