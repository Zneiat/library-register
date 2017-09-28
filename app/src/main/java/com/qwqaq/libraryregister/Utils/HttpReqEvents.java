package com.qwqaq.libraryregister.Utils;

import okhttp3.Call;

/**
 * 数据请求事件
 */
public abstract class HttpReqEvents {
    public abstract void onError(Call call, Exception e, int id);
    public abstract void onResponse(String response, int id);
}