package com.example.think.myweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by think on 2017/7/23.
 */


public class Basic {

    //因为有些json字段并不适合用来做名称
    // 使用@SerializedName注解的方式让JSON字段与Java字段建立起映射关系
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{

        @SerializedName("loc")
        public String updateTime;
    }
}
