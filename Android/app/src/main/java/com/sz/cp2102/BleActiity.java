package com.sz.cp2102;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.sz.cp2102.bean.MessageEvent;
import com.sz.cp2102.fragment.ConfigFragment;
import com.sz.cp2102.fragment.DeviceInfoFragment;
import com.sz.cp2102.fragment.HomeFragment;
import com.sz.cp2102.fragment.LogFragment;
import com.sz.cp2102.utils.PreferencesUtil;
import com.sz.cp2102.utils.TextUtils;
import com.sz.cp2102.view.NoScrollViewPager;
import com.sz.cp2102.adapter.IndexFragmentPageAdapter;
import com.sz.cp2102.config.EventBusId;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.interfaces.OnConfirmListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class BleActiity extends FragmentActivity {

    private BasePopupView loadingPopup;
    private Boolean isReset;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.transparent));

        loadingPopup = new XPopup.Builder(this).asLoading();
        setContentView(R.layout.activity_ble);
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setOperateTimeout(5000);
        chechLocation();
        initView();
        isReset = PreferencesUtil.getBoolean(getBaseContext(), "isReset", false);
        EventBus.getDefault().register(this);
        if(isReset){
            PreferencesUtil.putBoolean(getBaseContext(), "isReset", false);
            String mac = "";
            String name = "";
            if (mybleDevice == null | !isConnect) {

            } else {
                name = mybleDevice.getName() != null ? mybleDevice.getName() : "";
                mac= mybleDevice.getMac();
            }
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("mac", mac);
            intent.putExtra("name", name);
            startActivity(intent);
            Log.e("PreferencesUtil","PreferencesUtil2221");
        }else {

        }
        String TAG ="Environment";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.e(TAG,"exists:"+filePath);
        File dir = new File(filePath, "ajjText11111");
        Log.e(TAG,"exists:"+dir.getPath());
        if (!dir.exists()) {

            Log.e(TAG,"exists");
            Boolean bn=   dir.mkdir();
            Log.e(TAG,  bn+"**");
        }
        Log.e(TAG,  dir.exists()+"**");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showLoading(MessageEvent<String> event) {
        if (!event.getId().equals(EventBusId.showLoading)) {
            return;
        }
//        MessageEvent<String> messageEvent = new MessageEvent<>();
//        messageEvent.setId(EventBusId.showLoading);
//        EventBus.getDefault().post(messageEvent);
        loadingPopup.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dissLoading(MessageEvent<String> event) {
        if (!event.getId().equals(EventBusId.dissLoading)) {
            return;
        }
//        MessageEvent<String> messageEvent = new MessageEvent<>();
//        messageEvent.setId(EventBusId.dissLoading);
//        EventBus.getDefault().post(messageEvent);
        loadingPopup.dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeChatLoginSuccess(MessageEvent<String> event) {
        if (!event.getId().equals(EventBusId.STEP)) {
            return;
        }
    }

    public BleDevice device;

    public BleDevice getDevice() {
        return device;
    }

    public void setDevice(BleDevice device) {
        this.device = device;
    }

    public BleDevice getMybleDevice() {
        return mybleDevice;
    }

    public void setMybleDevice(BleDevice mybleDevice) {
        this.mybleDevice = mybleDevice;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDclick(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.BleDevice)) {
            return;
        }
        device = event.getBody();

        Log.e("qewq", device.getMac());
//        Log.e("qewq", device.getName() );
        Log.e("qewq1", device.getKey());
        if (device.getName() == null) {
            Toast.makeText(BleActiity.this, "暂不支持连接该设备", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (!device.getName().equalsIgnoreCase("BT24-M")) {
//            Toast.makeText(BleActiity.this, "暂不支持连接该设备", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (mybleDevice == null | !isConnect) {
            MessageEvent<BleDevice> messageEvent1 = new MessageEvent<>();
            messageEvent1.setId(EventBusId.onLink);
            EventBus.getDefault().post(messageEvent1);
//            new XPopup.Builder(BleActiity.this).asConfirm("发现设备", "是否连接该设备", new OnConfirmListener() {
//                @Override
//                public void onConfirm() {
//                    mybleDevice = device;
//                    Log.e("qewq", mybleDevice.getMac());
////                    Log.e("qewq", mybleDevice.getName() );
//                    Log.e("qewq", mybleDevice.getKey());
//                    link(mybleDevice);
//                }
//            }).show();
            return;
        }
        if (device.getMac().equals(mybleDevice.getMac())) {
            Toast.makeText(BleActiity.this, "已连接该设备", Toast.LENGTH_SHORT).show();
        } else {
            MessageEvent<BleDevice> messageEvent1 = new MessageEvent<>();
            messageEvent1.setId(EventBusId.onreLink);
            EventBus.getDefault().post(messageEvent1);
            new XPopup.Builder(BleActiity.this).asConfirm("发现新设备", "是否从新连接到新设备", new OnConfirmListener() {
                @Override
                public void onConfirm() {
                    BleManager.getInstance().disconnect(mybleDevice);
                    mybleDevice = device;
                    link(mybleDevice);
                }
            }).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void disconnect(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.disconnect)) {
            return;
        }
        BleManager.getInstance().disconnect(mybleDevice);
        mybleDevice = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLinkMain(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.onLinkMain)) {
            return;
        }
        mybleDevice = device;
        link(mybleDevice);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onreLinkMain(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.onreLinkMain)) {
            return;
        }
        BleManager.getInstance().disconnect(mybleDevice);
        mybleDevice = device;
        link(mybleDevice);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dislink(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.dislink)) {
            return;
        }
        if(mybleDevice==null){
            Toast.makeText(this,getResources().getText(R.string.isConnect),Toast.LENGTH_SHORT).show();
            return;
        }
        new XPopup.Builder(BleActiity.this).asConfirm(getResources().getString(R.string.onlink )  , getResources().getString(R.string.unlink ), new OnConfirmListener() {
            @Override
            public void onConfirm() {
                BleManager.getInstance().disconnect(mybleDevice);
                mybleDevice = null;
                MessageEvent<BleDevice> messageEvent = new MessageEvent<>();
                messageEvent.setId(EventBusId.UODATE1);
                PreferencesUtil.putBoolean(BleActiity.this, "auto", false);
                EventBus.getDefault().post(messageEvent);
            }
        }).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void auto(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.auto)) {
            return;
        }
        device = event.getBody();
        mybleDevice = device;
        link(mybleDevice);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void goSearch(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.goSearch)) {
            return;
        }

        Log.e("PreferencesUtil","PreferencesUtil1");
        String mac = "";
        String name = "";
        if (mybleDevice == null | !isConnect) {

        } else {
            name = mybleDevice.getName() != null ? mybleDevice.getName() : "";
            mac= mybleDevice.getMac();
        }

        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("mac", mac);
        intent.putExtra("name", name);
        startActivity(intent);
    }

    private NoScrollViewPager viewPager;
    private RadioGroup radioIndex;

    private List<Fragment> fragmentList;
    private IndexFragmentPageAdapter indexFragmentPageAdapter;

    public void initView() {
        viewPager = findViewById(R.id.view_pager);
        radioIndex = findViewById(R.id.radio_index);
        fragmentList = new ArrayList<>();
        fragmentList.add(new HomeFragment());
        fragmentList.add(new DeviceInfoFragment());
        fragmentList.add(new ConfigFragment());
        fragmentList.add(new LogFragment());
        FragmentManager fm = getSupportFragmentManager();
        indexFragmentPageAdapter = new IndexFragmentPageAdapter(fm, fragmentList);
        viewPager.setNoScroll(true);
        viewPager.setAdapter(indexFragmentPageAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(0);
        initListener();
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = df.format(new Date());
        Log.e("timestamp", timestamp);
        try {
            Log.e("timestamp11", MD5("20210521111439"));
            Log.e("timestamp11", MD5(MD5("20210521111439") + "d4y1IvMPk6jbeC6p9aG6G5FIV0YR7ypT"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        setBle();
    }

    public static String MD5(String data) throws Exception {
        System.out.println(data);
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        System.out.println(sb.toString().toUpperCase());
        return sb.toString().toUpperCase();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();

        BleManager.getInstance().destroy();
        Log.e("onDestroy","onDestroy");
    }

    public void setBle() {
        if (BleManager.getInstance().isSupportBle()) {
            Log.e("setble", "支持蓝牙");
            if (BleManager.getInstance().isBlueEnable()) {
                Log.e("setble", "蓝牙可用");
            } else {
                BleManager.getInstance().enableBluetooth();
            }
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            //从设置页面返回，判断权限是否申请。
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (EasyPermissions.hasPermissions(this, perms)) {

            } else {
                Toast.makeText(this, "权限申请失败!将无法正常使用APP", Toast.LENGTH_SHORT).show();

            }
        }
    }

    public void chechLocation() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // 已获取权限
            // ...

        } else {
            // 没有权限，现在去获取
            // ...
            EasyPermissions.requestPermissions(this, getResources().getText(R.string.applyBlue  ).toString(),
                    10001, perms);
        }
    }

    @AfterPermissionGranted(10001)
    public void onPermissionSuccess() {
        Toast.makeText(this, "AfterPermission调用成功了", Toast.LENGTH_SHORT).show();
    }
    @AfterPermissionGranted(10002)
    public void onPermissionSuccessWrite() {

    }

    private Boolean firstBollen = false;

    public void initListener() {
        radioIndex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
                switch (checkedId) {
                    case R.id.tag_home:
                        Log.e("onCheckedChanged", "tag_home");
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.msg_home2:
                        if (EasyPermissions.hasPermissions(BleActiity.this, perms)) {
                            // 已获取权限
                            // ...
//                            if (!firstBollen) {
//                                firstBollen = true;
//                                MessageEvent<String> messageEvent = new MessageEvent<>();
//                                messageEvent.setId(EventBusId.START);
//                                EventBus.getDefault().post(messageEvent);
//                            }
                            Log.e("onCheckedChanged", "msg_home2");
//                            MessageEvent<String> messageEvent = new MessageEvent<>();
//                            messageEvent.setId(EventBusId.deviceDetails);
//                            EventBus.getDefault().post(messageEvent);
                            viewPager.setCurrentItem(1);
                            return;
                        } else {
                            // 没有权限，现在去获取
                            // ...
                            EasyPermissions.requestPermissions(BleActiity.this, getResources().getText(R.string.applyBlue  ).toString(), 10001, perms);
                        }

                        break;
                    case R.id.msg_home1:
                        Log.e("onCheckedChanged", "msg_home1");
                        if (EasyPermissions.hasPermissions(BleActiity.this, perms)) {
                            // 已获取权限
                            // ...
//                            if (!firstBollen) {
//                                firstBollen = true;
//                                MessageEvent<String> messageEvent = new MessageEvent<>();
//                                messageEvent.setId(EventBusId.START);
//                                EventBus.getDefault().post(messageEvent);
//                            }
                            viewPager.setCurrentItem(2);
                        } else {
                            // 没有权限，现在去获取
                            // ...
                            EasyPermissions.requestPermissions(BleActiity.this, getResources().getText(R.string.applyBlue  ).toString(), 10001, perms);
                        }
                        break;
                    case R.id.home_me:
                        Log.e("onCheckedChanged", "home_me");
                        if (EasyPermissions.hasPermissions(BleActiity.this, perms)) {
                            // 已获取权限
                            // ...
                            viewPager.setCurrentItem(3);
                        } else {
                            // 没有权限，现在去获取
                            // ...
                            EasyPermissions.requestPermissions(BleActiity.this, getResources().getText(R.string.applyBlue  ).toString(), 10001, perms);
                        }
                        break;
                }
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        radioIndex.check(R.id.tag_home);
                        break;
                    case 1:
                        radioIndex.check(R.id.msg_home2);
                        break;
                    case 2:
                        radioIndex.check(R.id.msg_home1);
                        break;
                    case 3:
                        radioIndex.check(R.id.home_me);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }


    public String UUID_KEY_DATA = "00002a00-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR1 = "0000ffe2-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR2 = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR3 = "0000ae03-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR4 = "0000ae04-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR5 = "0000ae05-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR6 = "0000ae10-0000-1000-8000-00805f9b34fb";
    public String UUID_HERATRATE = "0000ae3b-0000-1000-8000-00805f9b34fb";
    public String UUID_TEMPERATURE = "0000ae3c-0000-1000-8000-00805f9b34fb";
    private Boolean isConnect = false;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void link(BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {

                Log.e("setble88:", "开始连接");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                Log.e("setble88:", "连接失败");
                isConnect = false;
            }


            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.e("setble88:", "连接成功");
                Log.e("setble88:", "状态码:" + status);

                PreferencesUtil.putBoolean(BleActiity.this, "auto", true);
                PreferencesUtil.putString(BleActiity.this, "mac", bleDevice.getMac());
                MessageEvent<BleDevice> messageEvent2 = new MessageEvent<>();

                messageEvent2.setName(mybleDevice.getName() != null ? mybleDevice.getName() : mybleDevice.getMac());
                messageEvent2.setId(EventBusId.linkSuccess);
                EventBus.getDefault().post(messageEvent2);
//                txtble.setText("蓝牙连接成功");
                MessageEvent<BleDevice> messageEvent1 = new MessageEvent<>();
                messageEvent1.setId(EventBusId.upDataTime);
                EventBus.getDefault().post(messageEvent1);
                isConnect = true;

                commet();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.e("setble8811:", "状态码:" + status);
                Log.e("setble8811:", "状态码:" + bleDevice.toString());
                if (status == 8) {
                    Toast.makeText(BleActiity.this, "已断开连接", Toast.LENGTH_SHORT).show();
                    BleManager.getInstance().disconnect(mybleDevice);
                    MessageEvent<BleDevice> messageEvent = new MessageEvent<>();
                    messageEvent.setId(EventBusId.UODATE1);
                    EventBus.getDefault().post(messageEvent);
                    mybleDevice = null;
                }

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void commet() {
        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(mybleDevice);
        List<BluetoothGattService> serviceList = gatt.getServices();
        for (BluetoothGattService service : serviceList) {
            UUID uuid_service = service.getUuid();
            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristicList) {
                UUID uuid_chara = characteristic.getUuid();
                Log.e("setble88664411", uuid_chara.toString());

                if (uuid_chara.toString().equals(UUID_CHAR2)) {
                    characteristic2 = characteristic;
                    list.add(characteristic);
                    ble_connect(mybleDevice, 2);
                }
                if (uuid_chara.toString().equals(UUID_CHAR1)) {
                    characteristic1 = characteristic;
                    list.add(characteristic);
                }

//                Log.e("characteristic",uuid_chara.);
                int charaProp = characteristic.getProperties();
            }
        }

    }

    public ArrayList<BluetoothGattCharacteristic> list = new ArrayList<BluetoothGattCharacteristic>();
    public BluetoothGattCharacteristic characteristic1;
    public BluetoothGattCharacteristic characteristic2;
    public BluetoothGattCharacteristic characteristic3;
    public BluetoothGattCharacteristic characteristic4;
    public BluetoothGattCharacteristic characteristic5;
    public BluetoothGattCharacteristic characteristic6;

    public BluetoothGattCharacteristic characteristic7;
    public BluetoothGattCharacteristic characteristic8;
    public BluetoothGattCharacteristic characteristic9;
    public BleDevice mybleDevice;
    private Boolean isConfing = false;
    private Boolean isStop = false;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void ble_connect(BleDevice bleDevice, int type) {
        Log.e("setble88999", "ble_connect");
        if (type == 2)
            BleManager.getInstance().notify(
                    bleDevice,
                    characteristic2.getService().getUuid().toString(),
                    characteristic2.getUuid().toString(),
                    new BleNotifyCallback() {
                        @Override
                        public void onNotifySuccess() {
                            // 打开通知操作成功
                            Log.e("setble66633", "onNotifySuccess2");
                            MessageEvent<BleDevice> messageEvent = new MessageEvent<>();
                            messageEvent.setBody(mybleDevice);
                            messageEvent.setId(EventBusId.UODATE);
                            EventBus.getDefault().post(messageEvent);
                        }

                        @Override
                        public void onNotifyFailure(BleException exception) {
                            Log.e("setble666633", "onNotifyFailure2");
                            // 打开通知操作失败
                        }

                        @Override
                        public void onCharacteristicChanged(byte[] data) {
                            // 打开通知后，设备发过来的数据将在这里出现
                            Log.e("setble666332", HexUtil.formatHexString(data, false));

                            Log.e("2upConfig",TextUtils.isStart(HexUtil.formatHexString(data, false))+"");
                            Log.e("1upConfig",TextUtils.isStop(HexUtil.formatHexString(data, false))+"");
                            if(TextUtils.isStart(HexUtil.formatHexString(data, false))){
                                isConfing = TextUtils.isStart(HexUtil.formatHexString(data, false));
                            }
                            if(TextUtils.isStop(HexUtil.formatHexString(data, false))){
                                isConfing = TextUtils.isStart(HexUtil.formatHexString(data, false));
                                MessageEvent<String> messageEvent2 = new MessageEvent<>();
                                messageEvent2.setBody(HexUtil.formatHexString(data, false));
                                messageEvent2.setId(EventBusId.upConfig);
                                EventBus.getDefault().post(messageEvent2);
                            }
                            if (isConfing) {
                                MessageEvent<String> messageEvent2 = new MessageEvent<>();
                                messageEvent2.setBody(HexUtil.formatHexString(data, false));
                                messageEvent2.setId(EventBusId.upConfig);
                                EventBus.getDefault().post(messageEvent2);
                            }
                            if(TextUtils.passwordInput(HexUtil.formatHexString(data, false))){
                                Toast.makeText(BleActiity.this, getResources().getString(R.string.passwordInput) , Toast.LENGTH_SHORT).show();
                            }
                            if(TextUtils.passwordErr(HexUtil.formatHexString(data, false))){
                                Toast.makeText(BleActiity.this, getResources().getString(R.string.passwordErr) , Toast.LENGTH_SHORT).show();
                            }
                            if(TextUtils.passwordSuccess(HexUtil.formatHexString(data, false))){
                                Toast.makeText(BleActiity.this, getResources().getString(R.string.passwordSuccess) , Toast.LENGTH_SHORT).show();
                            }
                            if(TextUtils.passwordOnSuccess(HexUtil.formatHexString(data, false))){
                                Toast.makeText(BleActiity.this, getResources().getString(R.string.passwordOnSuccess) , Toast.LENGTH_SHORT).show();
                            }



                            MessageEvent<String> messageEvent = new MessageEvent<>();
                            messageEvent.setBody(HexUtil.formatHexString(data, true));
                            messageEvent.setId(EventBusId.upData);
                            EventBus.getDefault().post(messageEvent);


                            //数据解析
//                            add("已接收" + HexUtil.formatHexString(data, true));
                        }
                    });
//        Boolean auto = PreferencesUtil.getBoolean(BleActiity.this, "auto", false);
//        if (auto) {
//            MessageEvent<String> messageEvent = new MessageEvent<>();
//            messageEvent.setId(EventBusId.Default);
//            EventBus.getDefault().post(messageEvent);
//        }

        BleManager.getInstance().setMtu(bleDevice, 512, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {

                Log.e("setbleononReadFailure", "onSetMTUFailure");
            }

            @Override
            public void onMtuChanged(int mtu) {

                Log.e("setbleononReadFailure", "onMtuChanged:" + mtu);
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void send(MessageEvent<String> event) {
        if (!event.getId().equals(EventBusId.send)) {
            return;
        }
//        MessageEvent<String> messageEvent = new MessageEvent<>();
//        messageEvent.setId(EventBusId.dissLoading);
//        EventBus.getDefault().post(messageEvent);
//        loadingPopup.dismiss();
        send(event.getBody());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void send(String hex) {
        if (mybleDevice == null) {
            Toast.makeText(BleActiity.this, getResources().getString(R.string.placeBle) , Toast.LENGTH_SHORT).show();
            return;
        }
        String hex1 = TextUtils.strToASCII(hex) + "0A";

        Log.e("545", hex1);
//        BleManager.getInstance().write(
//                mybleDevice,
//                characteristic1.getService().getUuid().toString(),
//                characteristic1.getUuid().toString(),
//                HexUtil.hexStringToBytes(hex1),
//                new BleWriteCallback() {
//                                @Override
//                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
//                        Log.e("setblesend", HexUtil.formatHexString(justWrite, true));
//                    }
//
//                    @Override
//                    public void onWriteFailure(BleException exception) {
//                        Log.e("setbleononReadFailure", "onWriteFailure");
//                        // 发送数据到设备失败
//                    }
//                });
        BleManager.getInstance().write(
                mybleDevice,
                characteristic2.getService().getUuid().toString(),
                characteristic2.getUuid().toString(),
                HexUtil.hexStringToBytes(hex1),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                        Log.e("setblesend", HexUtil.formatHexString(justWrite, true));
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.e("setbleononReadFailure", "onWriteFailure");
                        // 发送数据到设备失败
                    }
                });

    }
}
