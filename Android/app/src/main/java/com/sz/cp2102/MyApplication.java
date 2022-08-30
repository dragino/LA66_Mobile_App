package com.sz.cp2102;

import android.app.Application;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.tencent.bugly.crashreport.CrashReport;

public class MyApplication extends Application {
    public static  UsbSerialPort port;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("6545", "545454");
        CrashReport.initCrashReport(getApplicationContext(), "865b700306", false);
        Log.e("6545", "54545411");
    }

}
