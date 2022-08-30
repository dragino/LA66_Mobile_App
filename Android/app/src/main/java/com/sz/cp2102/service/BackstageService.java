package com.sz.cp2102.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.sz.cp2102.MyApplication;


public class BackstageService extends Service implements SerialInputOutputManager.Listener {

    private final IBinder mBinder = new LocalBinder();

    private Callback callback;
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onNewData(byte[] data) {
        if (callback!=null)
        callback.onDataChange(data,0);
    }

    @Override
    public void onRunError(Exception e) {
        if (callback!=null)
        callback.onRunError();
    }

    public static interface Callback {
        void onDataChange(byte[] buffer, int length);
        void onRunError( );
    }

    public class LocalBinder extends Binder {
        public BackstageService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BackstageService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void  reStart(){
        if(usbIoManager!=null){
            usbIoManager.stop();
            usbIoManager=null;
        }
        usbIoManager = new SerialInputOutputManager(MyApplication.port, this);
        usbIoManager.start();
    }
    private SerialInputOutputManager usbIoManager;
    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("onServiceConnected","onCreate");


        usbIoManager = new SerialInputOutputManager(MyApplication.port, this);
        usbIoManager.start();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                byte[] buffer = new byte[4096];
//
//                while (true) {
//
//                    int length = MyApplication.driver.ReadData(buffer, 4096);
//                    String recv = new String(buffer, 0, length);        //���ַ�����ʽ���
//                    if(callback!=null)
//                    callback.onDataChange(buffer,length);
//                }
//            }
//        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        return super.onStartCommand(intent, flags, startId);
    }

}
