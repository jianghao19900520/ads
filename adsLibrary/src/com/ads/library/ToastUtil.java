package com.ads.library;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtil
{

	private static Toast mToast = null;
	private static Handler handler = new Handler(Looper.getMainLooper());

	/**
	 * 显示toast提示,默认Toast.LENGTH_SHORT
	 * 
	 * @param context
	 *            上下文
	 * @param msgResId
	 *            toast 信息资源 id
	 */
	public static void show(Context context, int msgResId)
	{
		show(context, context.getString(msgResId), Toast.LENGTH_SHORT);
	}

	/**
	 * 显示toast提示,默认Toast.LENGTH_SHORT
	 * 
	 * @param context
	 *            上下文
	 * @param msg
	 *            toast 信息
	 */
	public static void show(Context context, CharSequence msg)
	{
		show(context, msg, Toast.LENGTH_SHORT);
	}

	/**
	 * toast 提示，覆盖上次提示 使用ui looper
	 * 
	 * @param context
	 *            上下文
	 * @param msg
	 *            toast 信息
	 * @param duration
	 *            Toast.LENGTH_LONG or Toast.LENGTH_SHORT
	 */
	public static void show(final Context context, final CharSequence msg, final int duration)
	{
		if (TextUtils.isEmpty(msg)) { return; }
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				// 创建单例
				if (null == mToast)
				{
					mToast = new Toast(context);
					mToast.setGravity(Gravity.CENTER, 0, 0);
				}

				// 更新视图
				final View toastRoot = LayoutInflater.from(context).inflate(R.layout.cus_toast_ly, null);
				final TextView tv = (TextView) toastRoot.findViewById(R.id.cus_toast_text);
				if (msg instanceof String)
				{
					tv.setText(Html.fromHtml((String) msg));
				}
				else
				{
					tv.setText(msg);
				}

				// 显示
				mToast.setView(toastRoot);
				mToast.setDuration(duration);
				mToast.show();
			}
		});
	}

	/**
	 * 取消 cancel
	 */
	public static void cancel()
	{
		if (null != mToast)
		{
			mToast.cancel();
		}
	}

}
