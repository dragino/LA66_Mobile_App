package com.sz.cp2102;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.clj.fastble.utils.HexUtil;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.InputConfirmPopupView;
import com.lxj.xpopup.interfaces.OnCancelListener;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.sz.cp2102.bean.LogBean;
import com.sz.cp2102.service.BackstageService;
import com.sz.cp2102.utils.LogUtil;
import com.sz.cp2102.utils.PreferencesUtil;
import com.sz.cp2102.utils.TextUtils;
import com.sz.cp2102.utils.USBConnectionManager;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity implements SerialInputOutputManager.Listener, EasyPermissions.PermissionCallbacks, ServiceConnection {
    private TextView text_statu1;
    private TextView text_statu2;
    private TextView text_statu3;
    private TextView text_statu31;

    private ImageView img1;

    private ListView listView;
    private LogAdapter logAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    public List<LogBean> logList = new ArrayList<>();
    private Switch btn_switch;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.transparent));
        locationTime = PreferencesUtil.getInt(MainActivity.this, "locationTime", locationTime);

        initView();
//        try {
//            getDriver();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        time3();
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (EasyPermissions.hasPermissions(this, perms)) {
            initMap();
        } else {
            EasyPermissions.requestPermissions(this, "申请文件读写和定位功能", 10002, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        // ...
        initMap();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...
        showText("Please give location permission manually");
    }

    public void showText(String str) {

        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 将权限的处理交给EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private Boolean seleteHex = false;

    public void initView() {
        btn_switch = findViewById(R.id.btn_switch);
        btn_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isTime = isChecked;


            }
        });
        img1 = findViewById(R.id.img1);
        text_statu1 = findViewById(R.id.text_statu1);
        text_statu2 = findViewById(R.id.text_statu2);
        text_statu3 = findViewById(R.id.text_statu3);
        text_statu31 = findViewById(R.id.text_statu31);

        findViewById(R.id.btn_Reconnection).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {


                new XPopup.Builder(MainActivity.this).asConfirm("Is Connection la66?", "",
                        "Canel", "OK",
                        new OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                                try {
                                    getDriver();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new OnCancelListener() {
                            @Override
                            public void onCancel() {

                            }
                        }, false).show();
//                initUsb();
            }
        });
        findViewById(R.id.btn_Reconnection1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new XPopup.Builder(MainActivity.this).asConfirm("Check   it is online?", "",
                        "Canel", "OK",
                        new OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                                try {
                                    checkConnect();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new OnCancelListener() {
                            @Override
                            public void onCancel() {

                            }
                        }, false).show();
            }
        });

//        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        listView = findViewById(R.id.listView);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);
        logAdapter = new LogAdapter(this);
        logAdapter.setSeleteHex(seleteHex);
        logAdapter.addResult(logList);
        listView.setAdapter(logAdapter);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                //刷新需执行的操作
////                setble();
//            }
//        });
//        swipeRefreshLayout.setRefreshing(false);
        findViewById(R.id.btn_send_hex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPopup5.show();
            }
        });
        findViewById(R.id.btn_send_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode();
            }
        });
        editTextTextPersonName = findViewById(R.id.editTextTextPersonName);
        btn_send_hex = findViewById(R.id.btn_send_hex);
        btn_send_hex.setText(getResources().getString(R.string.text_code_send) + "(" + getResources().getString(R.string.ascii) + ")");
        selectPopup5 = new XPopup.Builder(this)
                .autoDismiss(false)
                .asCenterList(getResources().getString(R.string.text_code_send), new String[]{getResources().getString(R.string.ascii), getResources().getString(R.string.hex)
                }, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        selectPopup5.dismiss();
                        if (position == 0) {
                            sendHex = false;
                            btn_send_hex.setText(getResources().getString(R.string.text_code_send) + "(" + getResources().getString(R.string.ascii) + ")");
                        } else {
                            sendHex = true;
                            btn_send_hex.setText(getResources().getString(R.string.text_code_send) + "(" + getResources().getString(R.string.hex) + ")");
                        }

                    }
                });
        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new XPopup.Builder(MainActivity.this).asConfirm("Exit APP", "",
                        "Canel", "OK",
                        new OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                                System.exit(0);
                            }
                        }, new OnCancelListener() {
                            @Override
                            public void onCancel() {

                            }
                        }, false).show();
            }
        });
        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new XPopup.Builder(MainActivity.this).asConfirm("", getResources().getString(R.string.isclearlog),
                        "Canel", "OK",
                        new OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                                clearLog();
                            }
                        }, new OnCancelListener() {
                            @Override
                            public void onCancel() {

                            }
                        }, false).show();
            }
        });
        btn_location = findViewById(R.id.btn_location);
        btn_location.setText("Uplink Interval（" + locationTime + "s）");

        findViewById(R.id.btn_location1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                InputConfirmPopupView a = new XPopup.Builder(MainActivity.this).asInputConfirm("Please enter the positioning interval(S)",
                        "",
                        new OnInputConfirmListener() {
                            @Override
                            public void onConfirm(String text) {
                                try {
                                    locationTime = Integer.parseInt(text);
                                    PreferencesUtil.putInt(MainActivity.this, "locationTime", locationTime);
                                    btn_location.setText("Uplink Interval（" + locationTime + "s）");
                                    mLocationClient.stopLocation();
                                    mLocationOption.setInterval(locationTime * 1000);
                                    //给定位客户端对象设置定位参数
                                    mLocationClient.setLocationOption(mLocationOption);
                                    mLocationClient.startLocation();
                                } catch (Exception e) {

                                }

                            }
                        });
                EditText et_input = a.findViewById(R.id.et_input);
                et_input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                TextView textView = a.findViewById(R.id.tv_confirm);
                textView.setText("Ok");
                TextView textView1 = a.findViewById(R.id.tv_cancel);
                textView1.setText("Canel");
                a.show();

            }
        });

    }

    public void saveLog() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(MainActivity.this, perms)) {
            if (logList.size() > 0) {
                String log = "";
                for (int i = 0; i < logList.size(); i++) {
                    log = log + logList.get(i).getText();
                }
                LogUtil.writerlog(log);
            } else {
                Toast.makeText(MainActivity.this, getResources().getText(R.string.log_Tips), Toast.LENGTH_SHORT).show();
            }

        } else {
            EasyPermissions.requestPermissions(MainActivity.this, getResources().getText(R.string.applyBlue).toString(), 10002, perms);
        }


    }

    public void sendCode() {
        if (editTextTextPersonName.getText().toString().trim().length() == 0) {
            Toast.makeText(this, getResources().getText(R.string.instructions), Toast.LENGTH_SHORT).show();
        } else {
            if (sendHex) {
                send(TextUtils.decode(editTextTextPersonName.getText().toString().trim()));
            } else {
                send(editTextTextPersonName.getText().toString().trim());
            }
        }
    }

    private long sendtime = 0;
    private Boolean isSendData = false;

    public void send(String send) {
        try {
            timeNum=0;
            Log.e("tyyy", send + "*");
            Log.e("tyyy", send.indexOf("SENDB") + "*");
            Log.e("tyyy", send + "*" + isSendData);
            if (isSendData) {
                if (send.indexOf("SENDB") != -1) {
                    return;
                }
                if (sendDataList.size() == 0) {
//                    showText("请在发包，待发包结束后自动发送");
                    Log.e("tyyy", send + "*" + send + "Please send it automatically after the contract is awarded");
                    sendtime = new Date().getTime();
                    sendDataList.add(send);
                    if (send.indexOf("NJS") == -1)
                        runOnUiThread(() -> {
                            showText("Please send it automatically after the contract is awarded");
                        });
                } else {
//                    showText("存在待发送指令，请稍后");
                    Log.e("tyyy", send + "*" + isSendData);
                    Log.e("tyyy", send + "*" + send + "There are instructions to be sent, please wait：" + isSendData);
                    if (send.indexOf("NJS") == -1)
                        runOnUiThread(() -> {
                            showText("There are instructions to be sent, please wait");
                        });
                }
                return;
            }

            Log.e("TextUtils", send);
            String hex1 = TextUtils.strToASCII(send) + "0D0A";
            Log.e("TAg", hex1);

            LogBean logBean = new LogBean();
            logBean.setText(send + TextUtils.decode("0D0A"));
            logBean.setTime(new Date().getTime());
            logBean.setType(2);
            logList.add(logBean);
            runOnUiThread(() -> {
                logAdapter.addResult(logList);
                logAdapter.notifyDataSetChanged();
                listView.setSelection(listView.getBottom());
            });
            if (isConnect) {
                MyApplication.port.write(HexUtil.hexStringToBytes(hex1), 3000);
                if (send.indexOf("SENDB") != -1) {
                    isSendData = true;
                }
            }
        } catch (Exception e) {
            Log.e("Exception", "send");
            e.printStackTrace();
        }
    }

    private EditText editTextTextPersonName;
    private TextView btn_send_hex;
    private TextView btn_location;
    private int locationTime = 60;
    private Boolean sendHex = false;
    private BasePopupView selectPopup5;
    public UsbManager mUsbManager;
    public UsbDevice UsbDevice;
    public Boolean isConnect = false;
    public Boolean isLan = false;
    public Boolean isLocation = false;
    public Boolean isTime = false;

    public void clearLog() {
        logList.clear();
        logAdapter.addResult(logList);
        logAdapter.notifyDataSetChanged();

    }

    private final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    final List<UsbDevice> result = new ArrayList<UsbDevice>();
    public UsbSerialDriver driver;
    public UsbManager manager1;

    public void getDriver() throws Exception {
        // Find all available drivers from attached devices.
        manager1 = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager1);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        driver = availableDrivers.get(0);
        if (manager1.hasPermission(driver.getDevice())) {
            getConnection();
        } else {
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION_USB_PERMISSION);
            registerReceiver(receiver, filter);
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0,
                    new Intent(ACTION_USB_PERMISSION), 0);
            manager1.requestPermission(driver.getDevice(), mPermissionIntent);
        }


    }

    private UsbDeviceConnection connection;

    public void getConnection() throws Exception {
        connection = manager1.openDevice(driver.getDevice());
//        connection.controlTransfer()
        if (connection == null) {
            Log.e("UsbDeviceConnection", "manager.openDevice(driver.getDevice())");
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return;
        } else {

        }
        Log.e("UsbDeviceConnection123", "manager.openDevice(driver.getDevice())");
        if (MyApplication.port != null) {
            MyApplication.port.close();
        }
        MyApplication.port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        MyApplication.port.open(connection);
        MyApplication.port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        success();
        Log.e("UsbDeviceConnection", "success");
        if (myService == null) {
            bindService(new Intent(this, BackstageService.class), this, BIND_AUTO_CREATE);
        } else {
            myService.reStart();
        }

//        Log.e("TAH", ":" + usbIoManager.getReadBufferSize());
//        Log.e("TAH", ":" + usbIoManager.getWriteBufferSize());

//       usbIoManager.setReadTimeout(3000);
//       usbIoManager.setWriteTimeout(3000);


        time1();
        time2();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy", "onDestroy");
        MyApplication.port = null;

        myService.onDestroy();
        myService = null;
    }

    private BackstageService myService;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.e("onServiceConnected", "onServiceConnected");
        BackstageService.LocalBinder binder = (BackstageService.LocalBinder) service;
        myService = binder.getService();
        myService.setCallback(new BackstageService.Callback() {
            @Override
            public void onDataChange(byte[] buffer, int length) {
                String recv = HexUtil.formatHexString(buffer, true);
                if (startTime == 0) {
                    str = str + recv;
                } else {
                    str = str + recv;
                }
                startTime = new Date().getTime();
                if (isSendData & ((sendTIme - startTime) > 5000)) {
                    runOnUiThread(() -> {
                        text_statu31.setText("");
                        img1.setImageDrawable(null);
                    });
                } else if (isSendData & ((sendTIme - startTime) > 7000)) {
                    isSendData = false;
                    sendTIme = 0;
                    if (sendDataList.size() > 0) {
                        send(sendDataList.get(sendDataList.size() - 1));
                        sendDataList.remove(sendDataList.size() - 1);
                    }
                }
//                setReadData(buffer, length);
            }

            @Override
            public void onRunError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_statu1.setText("LA66 Not Detect");
                        isConnect = false;
                        text_statu2.setText("LoRaWAN：Offline");
                        isSendData = false;
                        isLan = false;
                    }
                });

            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                Log.e("BroadcastReceiver", "BroadcastReceiver");
                if (granted) {
                    try {
                        getConnection();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && driver.getDevice().equals(device)) {
                        try {
                            getConnection();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //没有通讯权限
                        error();
                    }
                }

                context.unregisterReceiver(receiver);
            }
        }
    };


    public void success() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text_statu1.setText("LA66 Detected");
                isConnect = true;
            }
        });
    }

    public void error() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                text_statu1.setText("LA66 Not Detect");
//                isConnect = false;
//            }
//        });
    }

    public Timer timer1;

    public Timer timer2;
    public Timer timer3;

    public void time3() {
        if (timer3 != null) {
            timer3 = null;
            timer3 = new Timer();
        } else {
            timer3 = new Timer();
        }
        timer3.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isConnect) {
                    try {
                        Log.e("isConnect", "schedule");
                        getDriver();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }, 1000 * 1, 1000 * 3);
    }

    public void time2() {
        if (timer2 != null) {
            timer2 = null;
            timer2 = new Timer();
        } else {
            timer2 = new Timer();
        }
        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    checkConnect();
//                    new readThread().start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1000 * 1, 1000 * 30);
    }
    private int timeNum=0;
    public void time1() {
        if (timer1 != null) {
            timer1.cancel();
            timer1 = null;
            timer1 = new Timer();
        } else {
            timer1 = new Timer();
        }
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                endTime = new Date().getTime();

                if (endTime > startTime + 100 && startTime != 0) {
                    runOnUiThread(() -> {
                        try {
                            Log.e("schedule", str);
//                            Log.e("ttt", str.length() + "**");
                            String stt = str.replace(" ", "").toUpperCase();
//                            Log.e("ttt", stt + "**");
//                            Log.e("ttt", TextUtils.decode(stt));

                            if (stt.contains("527373693D20")) {
                                timeNum=0;
                                String str1 = stt.split("527373693D202D")[1].split("0D")[0];
                                String str = stt.split("527373693D202D")[1].split("0D")[0];
                                Log.e("***777", str);
                                Log.e("***777", TextUtils.decode(str));
                                Log.e("***7771", sendTIme + "*");
//                                text_statu3.setText("LoRaWAN RSSI:" + TextUtils.decode("2D" + str));
                                text_statu31.setText(TextUtils.decode("2D" + str));
                                int rssi = Integer.valueOf(TextUtils.decode(str));
                                if (rssi > 129) {
                                    img1.setImageDrawable(getResources().getDrawable(R.mipmap.img_rssi1));
                                } else if (rssi > 109) {
                                    img1.setImageDrawable(getResources().getDrawable(R.mipmap.img_rssi2));
                                } else if (rssi > 90) {
                                    img1.setImageDrawable(getResources().getDrawable(R.mipmap.img_rssi3));
                                } else if (rssi > 70) {
                                    img1.setImageDrawable(getResources().getDrawable(R.mipmap.img_rssi4));
                                } else {
                                    img1.setImageDrawable(getResources().getDrawable(R.mipmap.img_rssi5));
                                }
                                if (dataList.size() > 0)
                                    dataList.remove(dataList.size() - 1);
                                sendTIme = 0;
                                isSendData = false;
//                                dataList.clear();
                                Log.e("datalist", dataList.size() + "**");
                                if (dataList.size() > 0) {
                                    send("AT+SENDB=01,02," + dataList.get(dataList.size() - 1).length() / 2 + "," + dataList.get(dataList.size() - 1));
                                } else {
                                    if (sendDataList.size() > 0) {
                                        send(sendDataList.get(sendDataList.size() - 1));
                                        sendDataList.remove(sendDataList.size() - 1);
                                    }
                                }
                            }
                            if (stt.contains("0D0A0D0A4F4B0D0A") && stt.length() == 18) {
                                timeNum=0;
                                if (stt.split("0D0A0D0A4F4B0D0A")[0].equalsIgnoreCase("31")) {
                                    text_statu2.setText("LoRaWAN：Online");
                                    if (!isLan) {
                                        isLan = true;
//                                        send("AT+JOIN?");
                                    }
//                                            send("AT+RSSI=?");
                                } else {
                                    text_statu2.setText("LoRaWAN：Offline");
                                    isLan = false;
                                    isSendData = false;
                                }
                            }
                            Log.e("sttschedule", stt);
                            Log.e("sttschedule", ":" + stt.indexOf("727854696d656f7574"));

                            if (stt.indexOf("727854696d656f7574") != -1 || stt.indexOf("727854696D656F7574") != -1
                                    || stt.indexOf("41545F425553595F4552524F50") != -1 || stt.indexOf("41545f425553595f4552524f50") != -1) {
                                sendTIme = 0;
                                isSendData = false;
                                timeNum++;
                                if(  timeNum==2){
                                    runOnUiThread(() -> {
                                        text_statu31.setText("");
                                        img1.setImageDrawable(null);
                                    });
                                }
                            }
                            LogBean logBean = new LogBean();
                            logBean.setText(TextUtils.decode(stt));
                            logBean.setTime(new Date().getTime());
                            logBean.setType(1);
                            logList.add(logBean);
                            logAdapter.addResult(logList);
                            logAdapter.notifyDataSetChanged();

                            listView.setSelection(listView.getBottom());

                            startTime = 0;
                            str = "";
                            Log.e("Exception", "notifyDataSetChanged:" + logList.size());
                            Log.e("Exception", "notifyDataSetChanged:" + listView.getBottom());
                            Log.e("Exception", "notifyDataSetChanged");
                        } catch (Exception e) {
                            Log.e("Exception", "Exceptiontime1");
                            e.printStackTrace();
                            startTime = 0;
                            str = "";
                        }
                    });

                }
                if (endTime > sendTIme + 7000 && sendTIme != 0 && rssiData.length() != 0) {
                    Log.e("***777sendTIme", sendTIme + "*");
                    Log.e("***777sendTIme", endTime + "*");
//                    if(dataList.size()==100){
//                        dataList.remove(0);
//                    }
//                    dataList.add(rssiData);
//                    sendTIme=0;
//                    rssiData="";
                }
            }
        }, 1000 * 2, 1000 * 1 + 100);

    }

    public void checkConnect() throws IOException {
        send("AT+NJS=?");

//        String hex1 = TextUtils.strToASCII("AT+NJS=?") + "0D0A";
//        Log.e("TAg", hex1);
////        if (manager != null)
////            manager.write(HexUtil.hexStringToBytes(hex1));
//        if (port == null) {
//            return;
//        }
//        port.write(HexUtil.hexStringToBytes(hex1), 3000);

//        byte[] data = new byte[64];
//        Log.e("checkConnect", ":" + port.getRTS());
//        Log.e("checkConnect", ":" + port.getDTR());
//        int length = 0;
//        length = port.read(data, 3000);
//
//        if (length > 0) {
//            Message msg = Message.obtain();
//            String recv = HexUtil.formatHexString(data, true);
//            msg.obj = recv;
//            handler.sendMessage(msg);
//        }

    }


    private long startTime = 0;
    private long endTime = 0;
    private String str = "";

    @Override
    public void onNewData(byte[] data) {

    }

    @Override
    public void onRunError(Exception e) {
        Log.e("onRunError", "onRunError");
        error();

    }


    private String TAG = "LAnActivity";
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    private double latitude;
    private double longitude;

    private long sendTIme = 0;
    private ArrayList<String> dataList = new ArrayList<>();
    private ArrayList<String> sendDataList = new ArrayList<>();
    private String rssiData = "";
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    latitude = aMapLocation.getLatitude();//获取纬度
                    longitude = aMapLocation.getLongitude();//获取经度
                    Log.e(TAG, "纬度：" + aMapLocation.getLatitude());
                    Log.e(TAG, "经度：" + aMapLocation.getLongitude());
//                    mLocationClient.stopLocation();
                    if (!isLocation && latitude != 0 && isLan) {
                        Log.e("isLocation", latitude + "");
//                                                Log.e("isLocation", Long.valueOf(latitude+"")+"");
                        DecimalFormat df = new DecimalFormat("0");
                        String data = TextUtils.integerToHexString(Integer.valueOf(df.format(latitude * 1000000))) +
                                TextUtils.integerToHexString(Integer.valueOf(df.format(longitude * 1000000)));
                        if (isTime) {
                            Log.e("TAH", new Date().getTime() + "");
                            data = data + (Long.toHexString(new Date().getTime() / 1000).length() % 2 == 0 ?
                                    Long.toHexString(new Date().getTime() / 1000) : ("0" + Long.toHexString(new Date().getTime() / 1000)));
                        }
                        sendTIme = new Date().getTime();
                        Log.e("**77start", sendTIme + "");
                        rssiData = data + "";
                        dataList.add(rssiData);
                        send("AT+SENDB=01,02," + data.length() / 2 + "," + data);
                    }
                    //可在其中解析amapLocation获取相应内容。
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };

    public void initMap() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        if (null != mLocationClient) {
            mLocationClient.setLocationOption(mLocationOption);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.stopLocation();
            mLocationClient.startLocation();
        }
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//获取一次定位结果：
//该方法默认为false。
//        mLocationOption.setOnceLocation(true);
//        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(locationTime * 1000);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(20000);
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

    }


    private class LogAdapter extends BaseAdapter {

        private Boolean seleteHex;
        private Context context;
        private List<LogBean> logList;
        private String mac;

        LogAdapter(Context context) {
            this.context = context;
            logList = new ArrayList<>();
        }

        public void setSeleteHex(Boolean seleteHex) {
            this.seleteHex = seleteHex;
        }

        void addResult(List<LogBean> characteristicList) {
//            for ( int i=0;i<characteristicList.size();i++ ){
//
//            }

            this.logList.clear();
            this.logList.addAll(characteristicList);
            notifyDataSetChanged();
            listView.setSelection(listView.getBottom());

//            this.logList=characteristicList;
        }

        void clear() {
            logList.clear();
        }

        @Override
        public int getCount() {
            return logList.size();
        }

        @Override
        public LogBean getItem(int position) {
            if (position > logList.size())
                return null;
            return logList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LogAdapter.ViewHolder holder;
            if (convertView != null) {
                holder = (LogAdapter.ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(context, R.layout.adapter_log, null);
                holder = new LogAdapter.ViewHolder();
                holder.txt_log = (TextView) convertView.findViewById(R.id.txt_log);
                convertView.setTag(holder);
            }
//            holder.txt_title.setText("数据:");
            if (seleteHex) {
                holder.txt_log.setText(TextUtils.strToASCII(logList.get(position).getText()));
            } else {
                holder.txt_log.setText(logList.get(position).getText());
            }
            if (logList.get(position).getType() == 1) {
                holder.txt_log.setTextColor(context.getResources().getColor(R.color.black));
            } else {
                holder.txt_log.setTextColor(context.getResources().getColor(R.color.qmui_config_color_red));
            }
            return convertView;
        }

        class ViewHolder {
            TextView txt_log;
        }
    }
}
