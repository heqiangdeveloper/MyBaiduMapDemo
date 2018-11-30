package com.cimcitech.mybaidumapdemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

/**
 * Created by qianghe on 2018/11/30.
 */

public class MyOrientationListener implements SensorEventListener {
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private double lastX;

    public MyOrientationListener(Context mContext) {
        this.mContext = mContext;
    }

    public void start(){
        //获取感应器总管理器
        mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager != null){
            //获取方向感应器
            mSensor = mSensorManager.getDefaultSensor(SensorManager.SENSOR_ORIENTATION);
        }

        Log.d("sensorlog","is support sensor:" + mSensor!=null ? "Y":"N");

        if(mSensor != null){
            //注册方向监听器
            mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop(){
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //判断是否是方向传感器被触发
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            float x = event.values[SensorManager.DATA_X];
            Log.d("sensorlog","x :" + x);
            //如果位置变化超过1度
            if(Math.abs(x - lastX) > 1.0){
                //判断前端页面是否初始化监听器
                if(mOnOrientationListener != null){
                    //将x传给前端
                    mOnOrientationListener.onOrientationChanged(x);
                }
            }
            lastX = x;
        }
    }

    public OnOrientationListener mOnOrientationListener;

    public void setOnOrientationListener(OnOrientationListener mOnOrientationListener) {
        this.mOnOrientationListener = mOnOrientationListener;
    }

    public interface OnOrientationListener{
        void onOrientationChanged(float x);
    }
}
