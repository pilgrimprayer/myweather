package com.example.think.myweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by think on 2017/7/23.
 */


public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More{

        @SerializedName("txt")
        public String info;
    }
}
