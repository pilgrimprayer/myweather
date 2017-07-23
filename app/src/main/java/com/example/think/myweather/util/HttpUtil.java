package com.example.think.myweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by think on 2017/7/23.
 */


public class HttpUtil {

    //sendOkHttpRequest-发起一条HTTp请求，传入地址，注册回调  以处理 服务器响应
    //？？callback 回调
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        //创实例
        OkHttpClient client = new OkHttpClient();

        //如果要发起一天http请求，创建一个request
        //url(address)为目标访问地址
        Request request = new Request.Builder().url(address).build();

        //返回数据？？？
        client.newCall(request).enqueue(callback);
    }

}

