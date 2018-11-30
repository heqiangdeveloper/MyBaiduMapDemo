package com.cimcitech.mybaidumapdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.cloud.LocalSearchInfo;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.map)
    MapView mMapView;
    private BaiduMap mBaiduMap;
    private double latitude;
    private double longitude ;
    private LocationClient locationClient;
    private boolean isFirstLoc = true;
    private float mCurrentX;
    private MyOrientationListener mMyOrientationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(getApplicationContext());
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        //SDKInitializer.setCoordType(CoordType.BD09LL);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        askPermissons();
    }

    public void askPermissons(){
        List<String> lists = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            lists.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            lists.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
//        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission
//        .ACCESS_GPS) != PackageManager.PERMISSION_GRANTED){
//            lists.add(Manifest.permission.ACCESS_GPS);
//        }
        if(lists.size() != 0){
            String []permissions = lists.toArray(new String[lists.size()]);
            ActivityCompat.requestPermissions(this,permissions,1);
        }else{
            initMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(grantResults.length > 0){
                for(int result : grantResults){
                    if(result != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this,"必须同意所有的权限！",Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
                initMap();
            }else{
                Toast.makeText(this,"必须同意所有的权限！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void initMap(){
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        locationClient = new LocationClient(this);
        //firstLocation =true;
        // 设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开GPS
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
        option.setCoorType("bd0911");//返回的定位结果是百度经纬度，默认值是gcj02
        option.setScanSpan(5000);//设置发起定位请求的时间间隔为5000ms
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);// 返回的定位信息包含手机的机头方向
        locationClient.setLocOption(option);

        mMyOrientationListener = new MyOrientationListener(this);

        mMyOrientationListener.setOnOrientationListener(new myClass());
        //定位监听器
        locationClient.registerLocationListener(new MyLocationListener());
        //开始定位
        locationClient.start();
    }

    public class myClass implements MyOrientationListener.OnOrientationListener{
        @Override
        public void onOrientationChanged(float x) {
            mCurrentX = x;
            Log.d("sensorlog","get x is: " + x);
        }
    }

    @OnClick({R.id.normal_map_tv,R.id.satellite_map_tv,R.id.auto_locate_iv})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.normal_map_tv:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.satellite_map_tv:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.auto_locate_iv:
                setMyPlace(latitude,longitude);
                break;
        }
    }

    private void setMyPlace(double latitude,double longitude){
        LatLng xy = new LatLng(latitude, longitude);
        MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(xy);
        mBaiduMap.animateMapStatus(status);
    }

    private class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(null != location){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("loclog","latitude :" + latitude);
                Log.d("loclog","longitude :" + longitude);
                Log.d("loclog","address :" + location.getAddrStr());
                Log.d("loclog","mCurrentX :" + mCurrentX);
                MyLocationData myLocationData = new MyLocationData.Builder()
                        .direction(mCurrentX)
                        .accuracy(location.getRadius())
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude())
                        .build();
                mBaiduMap.setMyLocationData(myLocationData);

                BitmapDescriptor mBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.arrow);
                MyLocationConfiguration config = new MyLocationConfiguration
                        (MyLocationConfiguration.LocationMode.NORMAL, true,mBitmapDescriptor);
                mBaiduMap.setMyLocationConfigeration(config);

                if(isFirstLoc){
                    isFirstLoc = false;
                    LatLng xy = new LatLng(location.getLatitude(), location.getLongitude());
                    MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(xy);
                    mBaiduMap.animateMapStatus(status);
                }

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if(!locationClient.isStarted()){
            locationClient.start();
        }
        //开启方向传感器
        mMyOrientationListener.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //关闭定位
        mBaiduMap.setMyLocationEnabled(false);
        locationClient.stop();
        //关闭方向传感器
        mMyOrientationListener.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
}
