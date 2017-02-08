package com.ads.library;

import android.R.string;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ads.library.http.HttpAPi;
import com.ads.library.http.OnHttpCallBack;
import com.ads.library.node.LimitNode;
import com.ads.library.node.RandomBeanNode;
import com.ads.library.node.RandomInfoNode;
import com.ads.library.node.RandomLoopNode;
import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.AdView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.qq.e.ads.cfg.DownAPPConfirmPolicy;
import com.qq.e.ads.nativ.NativeAD;
import com.qq.e.ads.nativ.NativeADDataRef;
import com.qq.e.comm.util.StringUtil;

import net.tsz.afinal.FinalBitmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomAdActivity extends Activity
{

	private Activity mContext;
	private SharedPreferences preferences;
	private int random_from = 0;// 1=广点通 2=百度
	private int random_confirm = 0;// 0=开  1=关
	private String random_app_id = "";
	private String random_ad_id = "";
	private String my_app_id = "";
	private String user_id = "";

	private LayoutInflater mLayoutInflater;
	private boolean getLdByAdStartHasClick;

	private RelativeLayout mAnimLy;
	private ImageView mAnimView;
	private Animation mGetingAnim;
	private RelativeLayout mTokenEmptyLy;
	private Button network_err_btn;
	private LinearLayout getldbyad_linearlayout;
	private ListView listView;
	private RadomAdAdapter adapter;
	private RelativeLayout getldbyad_listview_again;
	private ImageView getldbyad_bg_img;
	private RelativeLayout getldbyad_listview_start_rl;

	private ArrayList<RandomLoopNode> loops;
	private int statusPosition = -1;
	private int clickPosition = 0;
	private boolean webviewFrom = false; // 判断是否从广告webview返回来的
	private RandomLoopNode currentLoopNode;
	private String key = ""; // 后台生成的key
	private int currentLd; // 当前点击广告对应的奖励数
	private NativeResponse baiduNode;
	private NativeADDataRef gdtNode;
	private DisplayImageOptions.Builder builder;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radom_ad);
		mContext = RandomAdActivity.this;
		preferences = getSharedPreferences(AdManager.TAG, Activity.MODE_PRIVATE);
		random_from = preferences.getInt("random_from", 0);
		random_confirm = preferences.getInt("random_confirm", 0);
		random_app_id = preferences.getString("random_app_id", "");
		random_ad_id = preferences.getString("random_ad_id", "");
		my_app_id = preferences.getString("my_app_id", "");
		user_id = preferences.getString("user_id", "");

		getLdByAdStartHasClick = preferences.getBoolean("getLdByAdStartHasClick", false);
		initView();
		if (random_from == 0 || StringUtil.isEmpty(random_app_id) || StringUtil.isEmpty(random_ad_id))
		{
			logUtil("random_from or random_app_id or random_ad_id is empty");
			errorLoading();
			return;
		}
		AdView.setAppSid(mContext, random_app_id);
		mLayoutInflater = LayoutInflater.from(mContext);
		ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(RandomAdActivity.this));
		builder = new DisplayImageOptions.Builder()
        .showImageOnLoading(R.drawable.random_loading_loadfailed_bg) // 设置图片在下载期间显示的图片
        .showImageForEmptyUri(R.drawable.random_loading_loadfailed_bg)// 设置图片Uri为空或是错误的时候显示的图片
        .showImageOnFail(R.drawable.random_loading_loadfailed_bg) // 设置图片加载/解码过程中错误时候显示的图片
        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
        .cacheOnDisk(true)// 设置下载的图片是否缓存在SD卡中
        .considerExifParams(true) // 是否考虑JPEG图像EXIF参数（旋转，翻转）
        .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
        .bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型//
        .resetViewBeforeLoading(true);// 设置图片在下载前是否重置，复位
		getAd();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (statusPosition != -1)
		{
			getldbyad_listview_again.setVisibility(View.VISIBLE);
			getldbyad_linearlayout.setPadding(0, 0, 0, 0);
			if (webviewFrom == true) getLdInfo();
		}
		else
		{
			getldbyad_listview_again.setVisibility(View.GONE);
			getldbyad_linearlayout.setPadding(0, 0, 0, 30);
		}
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
				getAd();
			}
		});
	}

	public void initView()
	{
		getldbyad_linearlayout = (LinearLayout) findViewById(R.id.getldbyad_linearlayout);
		listView = (ListView) findViewById(R.id.getldbyad_listview);
		adapter = new RadomAdAdapter();

		getldbyad_listview_again = (RelativeLayout) findViewById(R.id.getldbyad_listview_again);
		getldbyad_bg_img = (ImageView) findViewById(R.id.getldbyad_bg_img);
		getldbyad_listview_start_rl = (RelativeLayout) findViewById(R.id.getldbyad_listview_start_rl);
		if (getLdByAdStartHasClick == true)
		{
			getldbyad_bg_img.setVisibility(View.GONE);
			getldbyad_listview_start_rl.setVisibility(View.GONE);
		}
		else
		{
			getldbyad_listview_start_rl.setVisibility(View.VISIBLE);
			preferences.edit().putBoolean("getLdByAdStartHasClick", true).commit();
		}
		getldbyad_listview_start_rl.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getldbyad_listview_start_rl.setVisibility(View.GONE);
				getldbyad_bg_img.setVisibility(View.VISIBLE);
			}
		});
		getldbyad_bg_img.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getldbyad_bg_img.setVisibility(View.GONE);
			}
		});
		getldbyad_listview_again.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				statusPosition = -1;
				getldbyad_listview_again.setVisibility(View.GONE);
				getldbyad_linearlayout.setPadding(0, 0, 0, 30);
				getAd();
			}
		});
		mAnimLy = (RelativeLayout) findViewById(R.id.geting_anim_ly);
		mAnimView = (ImageView) findViewById(R.id.geting_anim_icon);
		mGetingAnim = AnimationUtils.loadAnimation(mContext, R.anim.dialog_wait_rotate);
		mTokenEmptyLy = (RelativeLayout) findViewById(R.id.getinfo_token_empty_ly);
		network_err_btn = (Button) findViewById(R.id.network_err_btn);
		findViewById(R.id.random_back).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				RandomAdActivity.this.finish();
			}
		});
	}

	/*
	 * 获取广告位列表
	 */
	public void getAd()
	{
		loading();
		HttpAPi.getInstance(mContext).getRandomInfo(user_id, my_app_id, new OnHttpCallBack<RandomInfoNode>()
		{
			@Override
			public void onSuccess(RandomInfoNode randomInfoNode)
			{
				if (randomInfoNode == null || randomInfoNode.getAdsLoop() == null)
				{
					errorLoading();
					logUtil("getAd failed");
				}
				else
				{
					loops = randomInfoNode.getAdsLoop();
					if (random_from == 2)
					{
						fetchBdAd(mContext);
					}
					else
					{
						fetchGdtAd(mContext);
					}
					logUtil("getAd success");
				}
			}

			@Override
			public void onFail(int httpCode, int statusCode, String msg)
			{
				errorLoading();
				logUtil("getAd failed");
			}
		});
	}

	/*
	 * 获取百度广告资源
	 */
	public void fetchBdAd(Activity activity)
	{
		BaiduNative baidu = new BaiduNative(activity, random_ad_id, new BaiduNative.BaiduNativeNetworkListener()
		{
			@Override
			public void onNativeFail(NativeErrorCode arg0)
			{
				errorLoading();
				logUtil("fetchBdAd failed : " + arg0);
			}

			@Override
			public void onNativeLoad(List<NativeResponse> arg0)
			{
				if (arg0 != null && arg0.size() > 0)
				{
					adapter.setBaiDuData(arg0);
					listView.setSelection(0);
					listView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					cancelLoading();
					logUtil("fetchBdAd success : " + arg0.size());
				}
				else
				{
					errorLoading();
					logUtil("fetchBdAd failed");
				}
			}
		});
		if(random_confirm==1){
			RequestParameters requestParameters = new RequestParameters.Builder().confirmDownloading(true)
					.build();
			baidu.makeRequest(requestParameters);
		}else{
			RequestParameters requestParameters = new RequestParameters.Builder().confirmDownloading(false)
					.build();
			baidu.makeRequest(requestParameters);
		}
	}

	/*
	 * 获取广点通广告资源
	 */
	public void fetchGdtAd(Activity activity)
	{
		NativeAD nativeAD = new NativeAD(mContext.getApplicationContext(), random_app_id, random_ad_id, new NativeAD.NativeAdListener()
		{
			@Override
			public void onADLoaded(List<NativeADDataRef> list)
			{
				if (list != null && list.size() > 0)
				{
					adapter.setGdtData(list);
					listView.setSelection(0);
					listView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					cancelLoading();
					logUtil("fetchGdtAd success : " + list.size());
				}
				else
				{
					errorLoading();
					logUtil("fetchGdtAd failed");
				}
			}

			@Override
			public void onNoAD(int i)
			{
				errorLoading();
				logUtil("fetchGdtAd failed : " + i);
			}

			@Override
			public void onADStatusChanged(NativeADDataRef nativeADDataRef)
			{
			}
		});
		int count = 8; // 一次拉取的广告条数：范围1-30
		if(random_confirm==1){
			nativeAD.setDownAPPConfirmPolicy(DownAPPConfirmPolicy.Default);
		}else{
			nativeAD.setDownAPPConfirmPolicy(DownAPPConfirmPolicy.NOConfirm);
		}
		nativeAD.loadAD(count);
	}

	/*
	 * 记录点击时间
	 */
	public void checkAdTime(final RandomLoopNode loopNode, final NativeADDataRef gdtNode, final NativeResponse baiduNode, final View view,
			final RelativeLayout getldbyad_listview_again, final int position)
	{

		HttpAPi.getInstance(mContext).randomCheck(user_id, loops.get(position).getPosition_id(), my_app_id, new OnHttpCallBack()
		{
			@Override
			public void onSuccess(Object o)
			{
				LimitNode node = (LimitNode) o;
				if(node == null){
					logUtil("randomCheck failed");
					ToastUtil.show(mContext, getString(R.string.comm_network_error_retry));
				}else{
					if(node.getResult()==-1){
						logUtil("randomCheck limit > 3");
						ToastUtil.show(mContext, getString(R.string.shake_get_limit));
						return;
					}
					if (StringUtil.isEmpty(node.getKey()))
					{
						logUtil("randomCheck success , but key is null");
						key = "";
						ToastUtil.show(mContext, getString(R.string.comm_network_error_retry));
						return;
					}
					key = node.getKey();
					logUtil("randomCheck success");
					statusPosition = position;
					clickPosition = position;
					if (random_from == 2)
					{
						baiduNode.handleClick(view);
					}
					else
					{
						gdtNode.onClicked(view);
					}
					webviewFrom = true;
					getldbyad_listview_again.setVisibility(View.GONE);
					ToastUtil.show(mContext, getString(R.string.lingdou_wakuang_more));
				}
			}

			@Override
			public void onFail(int httpCode, int statusCode, String msg)
			{
				logUtil("randomCheck failed");
				ToastUtil.show(mContext, getString(R.string.comm_network_error_retry));
			}
		});
	}

	/*
	 * 根据打开页面时间获取灵豆数据
	 */
	public void getLdInfo()
	{

		if (StringUtil.isEmpty(key)) return;

		HttpAPi.getInstance(mContext).getRandomBean(key, user_id, loops.get(clickPosition).getPosition_id(), my_app_id, new OnHttpCallBack<RandomBeanNode>()
		{
			@Override
			public void onSuccess(RandomBeanNode randomBeanNode)
			{
				if (randomBeanNode != null && randomBeanNode.getAdd() > 0)
				{
					currentLd = randomBeanNode.getAdd();
					takeGetLdDialog(randomBeanNode.getTotal(), randomBeanNode.getRemark(), randomBeanNode.getAdd());
					try
					{
						loops.get(clickPosition).setAmount(randomBeanNode.getTotal()+"");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					adapter.notifyDataSetChanged();
					logUtil("getRandomBean success");
				}
				else
				{
					logUtil("getRandomBean failed");
					ToastUtil.show(mContext, getString(R.string.comm_network_error_retry));
				}
			}

			@Override
			public void onFail(int httpCode, int statusCode, String msg)
			{
				logUtil("getRandomBean failed");
				ToastUtil.show(mContext, getString(R.string.comm_network_error_retry));
			}
		});
		webviewFrom = false;
	}

	/**
	 * 获取灵豆对话框
	 */
	private void takeGetLdDialog(int totleLd, String message, int getLd)
	{
		if (RandomAdActivity.this.isFinishing()) return;
		
		final Dialog getLdDialog = new Dialog(mContext, R.style.transparent_dialog_bg_style);
		getLdDialog.setCanceledOnTouchOutside(false);
		getLdDialog.setContentView(LayoutInflater.from(mContext).inflate(R.layout.getldbyad_reply_dialog_layout, null));
		getLdDialog.show();
		Window window = getLdDialog.getWindow();
		window.setGravity(Gravity.CENTER);
		TextView getld_dialog_title = (TextView) window.findViewById(R.id.getld_dialog_title);
		TextView getld_dialog_message = (TextView) window.findViewById(R.id.getld_dialog_message);
		Button getld_dialog_btn = (Button) window.findViewById(R.id.getld_dialog_btn);
		getld_dialog_title.setText(mContext.getString(R.string.shake_this_pic) + totleLd + mContext.getString(R.string.ads_rewards));
		getld_dialog_message.setText(message);
		getld_dialog_btn.setText(mContext.getString(R.string.shake_get_rewards) + getLd + mContext.getString(R.string.ads_rewards));
		getld_dialog_btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				HttpAPi.getInstance(mContext).getBean(key, user_id, loops.get(clickPosition).getPosition_id(), my_app_id, currentLd, new OnHttpCallBack()
				{
					@Override
					public void onSuccess(Object o)
					{
						logUtil("getBean success");
						Integer addBean = 0;
						try
						{
							addBean = Integer.valueOf(String.valueOf(o));
						}
						catch (NumberFormatException e)
						{
							e.printStackTrace();
						}
						if (addBean > 0)
						{
							ToastUtil.show(mContext, String.format(mContext.getString(R.string.ad_get_award_suc), addBean + ""));
						}
						else
						{
							ToastUtil.show(mContext, getString(R.string.shake_get_fail));
						}
						getLdDialog.dismiss();
					}

					@Override
					public void onFail(int httpCode, int statusCode, String msg)
					{
						ToastUtil.show(mContext, getString(R.string.shake_get_fail));
						getLdDialog.dismiss();
						logUtil("getBean failed");
					}
				});
			}
		});
	}

	public class RadomAdAdapter extends BaseAdapter
	{

		private List<NativeResponse> bdAdList = new ArrayList<NativeResponse>();
		private List<NativeADDataRef> gdtAdList = new ArrayList<NativeADDataRef>();

		public void setBaiDuData(List<NativeResponse> list)
		{
			bdAdList.clear();
			if (null != list)
			{
				bdAdList.addAll(list);
			}
		}

		public void setGdtData(List<NativeADDataRef> list)
		{
			gdtAdList.clear();
			if (null != list)
			{
				gdtAdList.addAll(list);
			}
		}

		@Override
		public int getCount()
		{
			return loops.size();
		}

		@Override
		public Object getItem(int position)
		{
			return loops.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			View view;
			ViewHolder viewHolder;
			if (convertView == null)
			{
				view = mLayoutInflater.inflate(R.layout.activity_getldbyad_list_item, null);
				viewHolder = new ViewHolder();
				viewHolder.getldbyad_listview_item_rlTemplate1 = (RelativeLayout) view.findViewById(R.id.getldbyad_listview_item_rlTemplate1);
				viewHolder.getldbyad_listview_item_iv_main = (ImageView) view.findViewById(R.id.getldbyad_listview_item_iv_main);
				viewHolder.getldbyad_listview_item_iv_bg = (ImageView) view.findViewById(R.id.getldbyad_listview_item_iv_bg);
				viewHolder.getldbyad_listview_item_tv_ld = (TextView) view.findViewById(R.id.getldbyad_listview_item_tv_ld);
				view.setTag(viewHolder);
			}
			else
			{
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}
			RandomLoopNode loopNode = loops.get(position);
			viewHolder.getldbyad_listview_item_tv_ld.setText(mContext.getString(R.string.random_this_pic) + loopNode.getAmount() + mContext.getString(R.string.ads_rewards));

			if (random_from == 2)
			{
				baiduNode = bdAdList.size() > position ? bdAdList.get(position) : bdAdList.get(0);
				ImageLoader.getInstance().displayImage(baiduNode.getImageUrl(), viewHolder.getldbyad_listview_item_iv_main, builder.build());
				baiduNode.recordImpression(viewHolder.getldbyad_listview_item_rlTemplate1);
			}
			else
			{
				gdtNode = gdtAdList.size() > position ? gdtAdList.get(position) : gdtAdList.get(0);
				ImageLoader.getInstance().displayImage(gdtNode.getImgUrl(), viewHolder.getldbyad_listview_item_iv_main, builder.build());
				gdtNode.onExposured(viewHolder.getldbyad_listview_item_rlTemplate1);
			}
			viewHolder.getldbyad_listview_item_iv_main.setAlpha(0);
			viewHolder.getldbyad_listview_item_rlTemplate1.setTag(position);
			if (statusPosition != -1)
			{
				viewHolder.getldbyad_listview_item_iv_bg.setVisibility(View.VISIBLE);
				viewHolder.getldbyad_listview_item_tv_ld.setVisibility(View.VISIBLE);
				viewHolder.getldbyad_listview_item_rlTemplate1.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
					}
				});
			}
			else
			{
				viewHolder.getldbyad_listview_item_iv_bg.setVisibility(View.GONE);
				viewHolder.getldbyad_listview_item_tv_ld.setVisibility(View.GONE);
				viewHolder.getldbyad_listview_item_rlTemplate1.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						RandomLoopNode loopNode = loops.get(Integer.parseInt(String.valueOf(view.getTag())));
						currentLoopNode = loopNode;
						checkAdTime(loopNode, gdtNode, baiduNode, view, getldbyad_listview_again, Integer.parseInt(String.valueOf(view.getTag())));
					}
				});
			}

			if (clickPosition == position)
			{
				viewHolder.getldbyad_listview_item_tv_ld.setTextColor(0xffef4113);
			}
			else
			{
				viewHolder.getldbyad_listview_item_tv_ld.setTextColor(0xff555555);
			}
			return view;
		}
	}

	class ViewHolder
	{
		RelativeLayout getldbyad_listview_item_rlTemplate1;
		ImageView getldbyad_listview_item_iv_main;
		ImageView getldbyad_listview_item_iv_bg;
		TextView getldbyad_listview_item_tv_ld;
	}

	public void logUtil(String msg)
	{
		boolean debug = mContext.getSharedPreferences(AdManager.TAG, Activity.MODE_PRIVATE).getBoolean("debug", false);
		if (debug == true) Log.d(AdManager.TAG, msg);
	}
	
}
