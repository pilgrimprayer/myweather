    package com.example.think.myweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.think.myweather.db.City;
import com.example.think.myweather.db.County;
import com.example.think.myweather.db.Province;
import com.example.think.myweather.util.HttpUtil;
import com.example.think.myweather.util.Utility;

import org.litepal.crud.DataSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by 付存哲kk on 2017/2/27.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();
    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的市
     */
    private City selectedCity;
    /**
     * 当前选中的级别
     */
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
      //重写onCreateView方法 加载碎片布局choose_area

        titleText = (TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        //获取控件实例

        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
       //listview适配器

        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //点击某个省
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //根据级别判断 查询哪个

                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                    //如果当前级别是LEVEL_COUNTY，说明已经选完了地点，可以出地点天气了
                    // 就启动WeatherActivity，并把当前的天气id传过去
                }else if(currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    //result = object instanceof class
                    //instanceof通过返回一个布尔值来指出，这个对象是否是这个特定类或者是它的子类的一个实例。

                    if(getActivity() instanceof MainActivity){
                       //如果当前页面== 主页面
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        //从当前活动跳到 weatherAcitivity
                        intent.putExtra("weather_id",weatherId);
                        //并传入weatherID
                        startActivity(intent);
                        //开启跳转
                        getActivity().finish();
                        //当前活动结束
                    }else if(getActivity() instanceof WeatherActivity){
                       //如果当前页面==weather活动下的页面xml

                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.drawerlayout.closeDrawers();//抽屉效果-侧面出页面 关掉？？
                        activity.swipeRefresh.setRefreshing(true);//出现刷新进度条
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener()
        //点击返回按钮 根据当前列表级别更新判断级别
        {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });

        queryProvinces();//加载省级数据
    }
    /**
     * 查询省份  优先从数据库中查询  如果没有查询到再去服务器上进行查询
     */
    private void queryProvinces() {
        //与UI有关
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);//返回键隐藏

        provinceList = DataSupport.findAll(Province.class);
        //litepal查询接口 从数据库中读取省级数据
        if(provinceList.size()>0){
            //如果读取到
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());//数据显示在界面
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            //如果没读取到 从服务器中查询
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }


    /**
     * 查询城市
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }

    }

    /**
     * 查询县
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }
    /**
     * 根据传入的地址和类型从服务器上查询省市县的数据 目的是找到数据并显示
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();//进度条

        HttpUtil.sendOkHttpRequest(address, new Callback()//
        //向服务器发送请求
        {
            @Override
            public void onFailure(Call call, IOException e) {

                getActivity().runOnUiThread(new Runnable()
                {   //子线程切换到主线程
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }
            //服务器响应数据 回调onResonse方法
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){

                    result = Utility.handleProvinceResponse(responseText);
                    //解析和处理服务器返回数据 并存入数据库
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    //找到了
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();//重新调用省级数据
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
