package com.sz.cp2102;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.sz.cp2102.bean.MessageEvent;
import com.sz.cp2102.config.EventBusId;
import com.sz.cp2102.utils.PreferencesUtil;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.interfaces.OnConfirmListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class SearchActivity extends Activity {

    private ListView listView;
    private ResultAdapter mResultAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txt2;
    private Boolean auto;
    private Boolean isLink = false;
    private BasePopupView loadingPopup;
    private String mac;
    private String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mac = getIntent().getStringExtra("mac");
        name = getIntent().getStringExtra("name");
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.transparent));
        setContentView(R.layout.activity_search);
        EventBus.getDefault().register(this);
        chechLocation();
        initView();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

    }

    public void initView() {
        findViewById(R.id.title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        loadingPopup = new XPopup.Builder(this).asLoading();
        txt2 = findViewById(R.id.txt2);
        txt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLink) {
                    MessageEvent<BleDevice> messageEvent = new MessageEvent<>();
                    messageEvent.setId(EventBusId.dislink);
                    EventBus.getDefault().post(messageEvent);
                }
            }
        });
        if (mac.length() > 0) {
            txt2.setText((name.length() > 0 ? name : mac) + "   " + getResources().getString(R.string.on_connected));
        } else {
            txt2.setText(getResources().getString(R.string.no_connected));
        }
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        listView = findViewById(R.id.listView);
        mResultAdapter = new ResultAdapter(this, mac);
        listView.setAdapter(mResultAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新需执行的操作
                setble();
            }
        });
        setble();
    }

    public void initData() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 将返回结果转给EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void chechLocation() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // 已获取权限
            // ...

        } else {
            // 没有权限，现在去获取
            // ...
            EasyPermissions.requestPermissions(this, getResources().getText(R.string.applyBlue).toString(),
                    1001, perms);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLink(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.onLink)) {
            return;
        }
        new XPopup.Builder(SearchActivity.this).asConfirm("发现设备", "是否连接该设备", new OnConfirmListener() {
            @Override
            public void onConfirm() {
                MessageEvent<BleDevice> messageEvent1 = new MessageEvent<>();
                messageEvent1.setId(EventBusId.onLinkMain);
                EventBus.getDefault().post(messageEvent1);
            }
        }).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onreLink(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.onreLink)) {
            return;
        }
        new XPopup.Builder(SearchActivity.this).asConfirm("发现新设备", "是否从新连接到新设备", new OnConfirmListener() {
            @Override
            public void onConfirm() {
                MessageEvent<BleDevice> messageEvent1 = new MessageEvent<>();
                messageEvent1.setId(EventBusId.onreLinkMain);
                EventBus.getDefault().post(messageEvent1);
            }
        }).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void linkSuccess(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.linkSuccess)) {
            return;
        }
        txt2.setText(event.getName() + "   " + getResources().getString(R.string.on_connected));

    }

    public void setble() {

        loadingPopup.show();
        Log.e("setble", "setble");
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()

                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);

        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {

            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                Log.e("lkklj", new Gson().toJson(bleDevice));
                Log.e("setble33", bleDevice.getMac());
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                Log.e("setble22", bleDevice.getMac());
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {

                myscanResultList = scanResultList;
                mResultAdapter.addResult(myscanResultList);
                mResultAdapter.notifyDataSetChanged();

                Log.e("setble22onScanFinished", myscanResultList.size() + "");
                swipeRefreshLayout.setRefreshing(false);
                loadingPopup.dismiss();
                String mac = PreferencesUtil.getString(SearchActivity.this, "mac", "");

                for (int i = 0; i < myscanResultList.size(); i++) {
                    Log.e("lkklj", new Gson().toJson(myscanResultList.get(i)));
                    if (myscanResultList.get(i).getName() != null) {
                        if (myscanResultList.get(i).getMac().equalsIgnoreCase(mac)) {
                            BleDevice bleDevice = myscanResultList.get(i);
                            Log.e("4564", bleDevice.getMac());
                            MessageEvent<BleDevice> messageEvent1 = new MessageEvent<>();
                            messageEvent1.setBody(bleDevice);
                            messageEvent1.setId(EventBusId.auto);
                            EventBus.getDefault().post(messageEvent1);
                            break;
                        }
                    }
                }


//                for (BleDevice d : scanResultList) {
//                    Log.e("setble99:", d.getMac());
//                    if (d.getName() != null) {
//                        Log.e("setble99Name:", d.getName());
//                        mResultAdapter.addResult("发现蓝牙设备:" + d.getName());
//                        mResultAdapter.notifyDataSetChanged();
//                        if (d.getName().equalsIgnoreCase("AC696X_1(BLE)")) {
//                            txtble.setText("发现蓝牙设备");
//                            mybleDevice = d;
//                        }
//                    } else {
//                        mResultAdapter.addResult("发现蓝牙设备:" + d.getMac());
//                    }
//                }
            }
        });
    }

    public List<BleDevice> myscanResultList;

    private class ResultAdapter extends BaseAdapter {

        private Context context;
        private List<BleDevice> characteristicList;
        private String mac;

        ResultAdapter(Context context, String mac) {
            this.context = context;
            this.mac = mac;
            characteristicList = new ArrayList<>();
        }

        void addResult(List<BleDevice> characteristicList) {
//            for ( int i=0;i<characteristicList.size();i++ ){
//
//            }
            this.characteristicList = characteristicList;
        }

        void clear() {
            characteristicList.clear();
        }

        @Override
        public int getCount() {
            return characteristicList.size();
        }

        @Override
        public BleDevice getItem(int position) {
            if (position > characteristicList.size())
                return null;
            return characteristicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ResultAdapter.ViewHolder holder;
            if (convertView != null) {
                holder = (ResultAdapter.ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(context, R.layout.adapter_ble, null);
                holder = new ResultAdapter.ViewHolder();
                holder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
                holder.txt_uuid = (TextView) convertView.findViewById(R.id.txt_uuid);
                holder.txt_type = (TextView) convertView.findViewById(R.id.txt_type);
                holder.bleDevice = characteristicList.get(position);
                convertView.setTag(holder);
            }
//            holder.txt_title.setText("数据:");
            holder.bleDevice = characteristicList.get(position);
            if (characteristicList.get(position).getName() != null) {
                holder.txt_title.setText(characteristicList.get(position).getName());
                holder.txt_uuid.setText(characteristicList.get(position).getMac());
            } else {
                if (characteristicList.get(position).getMac() != null) {
                    holder.txt_title.setText(characteristicList.get(position).getMac());
                } else {
                    holder.txt_title.setText("未知");
                }
            }
            if (mac.equals(characteristicList.get(position).getMac())) {
                holder.txt_type.setText(R.string.on_connected);
            } else {
                holder.txt_type.setText(R.string.no_connected);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ResultAdapter.ViewHolder viewHolder = (ResultAdapter.ViewHolder) v.getTag();
                    BleDevice bleDevice = viewHolder.bleDevice;
                    Log.e("4564", bleDevice.getMac());
                    MessageEvent<BleDevice> messageEvent = new MessageEvent<>();
                    messageEvent.setBody(bleDevice);
                    messageEvent.setId(EventBusId.BleDevice);
                    EventBus.getDefault().post(messageEvent);
                }
            });
            return convertView;
        }

        class ViewHolder {
            TextView txt_title;
            TextView txt_uuid;
            TextView txt_type;
            BleDevice bleDevice;
        }
    }

}
