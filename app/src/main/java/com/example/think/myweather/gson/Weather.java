package com.example.think.myweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by think on 2017/7/23.
 * 综合刚刚创的类
 */


public class Weather {
    //当前请求状态
    public String status;
    //一些天气的基本信息
    public Basic basic;
    //空气质量
    public AQI aqi;
    //当前天气信息
    public Now now;
    //一些天气相关的建议
    public Suggestion suggestion;
    //未来几天内的天气信息-一天是一个类 未来几天则是个类集合
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
