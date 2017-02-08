package com.ads.library.http;

public interface OnHttpCallBack<T> {

    void onSuccess(T t);

    void onFail(int httpCode, int statusCode, String msg);

}
