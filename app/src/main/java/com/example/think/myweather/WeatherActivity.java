package com.example.think.myweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.think.myweather.gson.Forecast;
import com.example.think.myweather.gson.Weather;
import com.example.think.myweather.service.AutoUpdateService;
import com.example.think.myweather.util.HttpUtil;
import com.example.think.myweather.util.Utility;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerlayout;

    private Button navButton;

    public SwipeRefreshLayout swipeRefresh;
    //用来记录城市天气的id
    private String mWeatherId;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //判断当前的设备版本是否大于21/Android5.0  若设备版本号在Android5.0以上  就执行代码   使得软件界面的头布局和设备系统栏融为一体
        if(Build.VERSION.SDK_INT>21){
            //为了背景图与状态栏(电池电量 wifi这一栏)融合一起的效果
            View decorView = getWindow().getDecorView();
           //通过decorView获取到程序显示的区域，包括标题栏，但不包括状态栏
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
           //布局显示在状态栏上面
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            //状态栏设置为透明色
        }

        setContentView(R.layout.activity_weather);
        initView();//初始化各个控件
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
       //下拉刷新进度条的颜色
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);//获取这个文件的xml？？
        //轻量级的存储类，特别适合用于保存软件配置参数用xml文件存放数据
        String weatherString = prefs.getString("weather",null);
        //创建 key为weather的 内容空白null

        if(weatherString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            //无缓存时区服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        //下拉刷新监听
        {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });


        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic!=null){
            //if有缓存
            Glide.with(this).load(bingPic).into(bingPicImg);//图片加载
        }else {
            //没缓存
            loadBingPic();
        }
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开滑动菜单-抽屉效果-ChooseAreaFragment 活动开启
                drawerlayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 加载必应图片 每日一图
     */
    private void loadBingPic() {
        //用了回调 所以方法里定义方法
        String resquestBingPic = "http://guolin.tech/api/bing_pic";
       //
        HttpUtil.sendOkHttpRequest(resquestBingPic, new Callback()
            //访问网址 都会
        //Callback回调
        {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                // 图片加载失败
                e.printStackTrace();
                Toast.makeText(WeatherActivity.this,"背景图片加载失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //服务器返回具体内容
                //图片加载成功
                final String bingPic = response.body().string();
                //获取服务器返回的xml文本
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                //存入数据库
                //回调接口都还是在子线程中运行
                runOnUiThread(new Runnable() {
                //更新UI要切换到主线程
                    @Override
                    public void run() {
                        //更新UI的代码
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                        Toast.makeText(WeatherActivity.this,"背景图片加载成功",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 根据天气的id去请求天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=dc6f30eba6ae4cb0a8d96a52bccbfa79";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {//callback方法重写
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        //刷新完毕后关闭下拉提示 隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                //返回文本
                final Weather weather = Utility.handleWeatherResponse(responseText);
                //解析成对象
                runOnUiThread(new Runnable() {
                    //从当前进程切换到主线程
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            //数据缓存于sharedpre..
                            showWeatherInfo(weather);
                            //显示-数据显示到相应控件上
                            Toast.makeText(WeatherActivity.this,"获取天气信息成功",Toast.LENGTH_SHORT).show();


                            //激活后台自动更新服务
                            //一旦选中目的地 此服务一直在后台运行
                            Intent intent = new Intent(WeatherActivity.this,AutoUpdateService.class);
                            startService(intent);

                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();//图片加载
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;//类属性
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);//控件操作
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for(Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度:"+weather.suggestion.comfort.info;
        String carWash = "洗车指数:"+weather.suggestion.carWash.info;
        String sport = "运动指数: "+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    //初始化各个控件
    private void initView() {
        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        bingPicImg = (ImageView)findViewById(R.id.bing_pic);
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        drawerlayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (Button)findViewById(R.id.nav_button);
    }
}