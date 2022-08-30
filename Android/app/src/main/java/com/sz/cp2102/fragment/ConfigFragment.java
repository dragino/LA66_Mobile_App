package com.sz.cp2102.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sz.cp2102.BleActiity;
import com.sz.cp2102.SearchActivity;
import com.sz.cp2102.bean.MessageEvent;
import com.sz.cp2102.R;
import com.sz.cp2102.config.EventBusId;
import com.sz.cp2102.utils.LogUtil;
import com.sz.cp2102.utils.PreferencesUtil;
import com.sz.cp2102.utils.TextUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnSelectListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class ConfigFragment extends Fragment implements View.OnClickListener {

    private BasePopupView selectPopup1;
    private BasePopupView selectPopup2;
    private BasePopupView selectPopup3;
    private BasePopupView selectPopup4;
    private BasePopupView selectPopup5;
    private BasePopupView selectPopup6;
    private Boolean auto;
    private View view1;
    private View view2;
    private View btn_send;
    private ListView listView;
    private LogAdapter logAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    public List<String> logList = new ArrayList<>();
    private TextView title;
    private Switch btn_switch;
    private int sendType = 0;
    private int timeType = 0;
    private Boolean seleteHex = false;
    private Boolean sendHex = false;

    private TextView btn_suspend;
    private TextView txt_1;
    private TextView txt1;
    private TextView txt2;
    private TextView txt_time1;
    private TextView txt_time2;
    private TextView txt_time3;
    private TextView txt_time4;
    private EditText editTextNumber;
    private EditText editTextTextPersonName;
    private TextView btn_selete;
    private TextView btn_send_hex;

    private TextView txt_atdetails;
    private EditText edittext1;
    private String[] atList = new String[]{"ATZ", "AT+FDR",
            "AT+ADR=0\n", "AT+ADR=1\n", "AT+NJM=0\n", "AT+NJM=1\n", "AT+CFM=0\n", "AT+CFM=1\n",
            "AT+TDC=1200000\n", "AT+MOD=1\n", "AT+INTMOD=0\n", "AT+INTMOD=1\n", "AT+INTMOD=2\n", "AT+INTMOD=3\n",
            "AT+5VT=0\n","AT+DR=0\n","AT+TXP=0\n","AT?"
    };
    private String[] atDetailsList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ble, container, false);
        EventBus.getDefault().register(this);
        initView(view);
        initData();
        Log.e("onCreateView", "onCreateView");

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

//        if (hidden && !isLink) {
//            Toast.makeText(getActivity(), getResources().getText(R.string.isConnect), Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(swipeRefreshLayout!=null)
            swipeRefreshLayout.setRefreshing(false);
    }

    public void initView(View view) {
        atDetailsList = new String[]{getResources().getString(R.string.at1), getResources().getString(R.string.at2),
                getResources().getString(R.string.at3), getResources().getString(R.string.at3), getResources().getString(R.string.at4), getResources().getString(R.string.at4),
                getResources().getString(R.string.at5), getResources().getString(R.string.at5),
                getResources().getString(R.string.at6), getResources().getString(R.string.at7), getResources().getString(R.string.at8), getResources().getString(R.string.at8), getResources().getString(R.string.at8),
                getResources().getString(R.string.at8), getResources().getString(R.string.at9),getResources().getString(R.string.at10),getResources().getString(R.string.at11),getResources().getString(R.string.at12),
        };
        txt_atdetails = view.findViewById(R.id.txt_atdetails);
        edittext1 = view.findViewById(R.id.edittext1);
        txt_time1 = view.findViewById(R.id.txt_time1);
        txt_time2 = view.findViewById(R.id.txt_time2);
        txt_time3 = view.findViewById(R.id.txt_time3);
        txt_time4 = view.findViewById(R.id.txt_time4);
        btn_selete = view.findViewById(R.id.btn_selete);
        btn_send_hex = view.findViewById(R.id.btn_send_hex);
        btn_suspend = view.findViewById(R.id.btn_suspend);
        editTextTextPersonName = view.findViewById(R.id.editTextTextPersonName);
        editTextNumber = view.findViewById(R.id.editTextNumber);
        editTextNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setBtn();
                timeType = 0;
            }
        });
        txt_1 = view.findViewById(R.id.txt_1);
        txt1 = view.findViewById(R.id.txt1);
        txt2 = view.findViewById(R.id.txt2);
        txt_1.setText(getResources().getString(R.string.pattern1));
        selectPopup1 = new XPopup.Builder(getActivity())
                .autoDismiss(false)
                .asCenterList(getResources().getString(R.string.swMode), new String[]{getResources().getString(R.string.pattern1), getResources().getString(R.string.pattern2)
                        , getResources().getString(R.string.pattern3)}, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        if (!isLink) {
                            Toast.makeText(getActivity(), getResources().getText(R.string.isConnect), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        selectPopup1.dismiss();
                        sendType = position;
                        txt_1.setText(text);
                    }
                });
        selectPopup2 = new XPopup.Builder(getActivity())
                .autoDismiss(false)
                .asCenterList(getResources().getString(R.string.NodeMode), new String[]{"1", "2", "3", "4", "5", "6"
                }, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        selectPopup2.dismiss();
                        if (!isLink) {
                            Toast.makeText(getActivity(), getResources().getText(R.string.isConnect), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        txt1.setText( text);
                        send("AT+MOD=" + (position + 1));
                    }
                });
        selectPopup3 = new XPopup.Builder(getActivity())
                .autoDismiss(false)
                .asCenterList(getResources().getString(R.string.InterruptMode), new String[]{getResources().getString(R.string.Disable), getResources().getString(R.string.falling_or_rising)
                        , getResources().getString(R.string.falling), getResources().getString(R.string.rising)}, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        selectPopup3.dismiss();
                        if (!isLink) {
                            Toast.makeText(getActivity(), getResources().getText(R.string.isConnect), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        txt2.setText(text);
                        send("AT+INTMOD=" + position);
                    }
                });
        selectPopup4 = new XPopup.Builder(getActivity())
                .autoDismiss(false)
                .asCenterList(getResources().getString(R.string.text_code), new String[]{getResources().getString(R.string.ascii), getResources().getString(R.string.hex)
                }, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        selectPopup4.dismiss();
                        if (position == 0) {
                            seleteHex = false;
                            btn_selete.setText(getResources().getString(R.string.text_code) + "(" + getResources().getString(R.string.ascii) + ")");
                        } else {
                            seleteHex = true;
                            btn_selete.setText(getResources().getString(R.string.text_code) + "(" + getResources().getString(R.string.hex) + ")");
                        }
                        logAdapter.setSeleteHex(seleteHex);
                        logAdapter.notifyDataSetChanged();
                    }
                });
        selectPopup5 = new XPopup.Builder(getActivity())
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
        selectPopup6 = new XPopup.Builder(getActivity())
                .autoDismiss(false)
                .asCenterList(getResources().getString(R.string.Common_instructions), atList, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        selectPopup6.dismiss();
                        txt_atdetails.setText(atDetailsList[position]);
                        edittext1.setText(atList[position]);

                    }
                });

        view.findViewById(R.id.bnt1).setOnClickListener(this);
        view.findViewById(R.id.txt_1).setOnClickListener(this);
        view.findViewById(R.id.title_left).setOnClickListener(this);
        view.findViewById(R.id.btn_right).setOnClickListener(this);
        view.findViewById(R.id.btn_save).setOnClickListener(this);
        view.findViewById(R.id.btn_send_hex).setOnClickListener(this);
        view.findViewById(R.id.btn_send_code).setOnClickListener(this);
        view.findViewById(R.id.btn_clear_log).setOnClickListener(this);
        view.findViewById(R.id.btn_time).setOnClickListener(this);
        view.findViewById(R.id.btn_selete).setOnClickListener(this);
        view.findViewById(R.id.btn_suspend).setOnClickListener(this);
        view.findViewById(R.id.txt_time1).setOnClickListener(this);
        view.findViewById(R.id.txt_time2).setOnClickListener(this);
        view.findViewById(R.id.txt_time3).setOnClickListener(this);
        view.findViewById(R.id.txt_time4).setOnClickListener(this);
        view.findViewById(R.id.btn_send).setOnClickListener(this);

        txt1.setOnClickListener(this);
        txt2.setOnClickListener(this);
        title = view.findViewById(R.id.title);
        view1 = view.findViewById(R.id.view1);
        view2 = view.findViewById(R.id.view2);
        btn_send = view.findViewById(R.id.btn_send);
        btn_switch = view.findViewById(R.id.btn_switch);
        isSwitch = true;
        switchrigth(isSwitch);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        listView = view.findViewById(R.id.listView);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);
        logAdapter = new LogAdapter(getContext());
        logAdapter.setSeleteHex(seleteHex);
        listView.setAdapter(logAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新需执行的操作
//                setble();
            }
        });
        swipeRefreshLayout.setRefreshing(false);

        btn_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("onCheckedChanged", isChecked + "**");
                String sw = isChecked ? "1" : "0";
                if (sendType == 0) {
                    send("AT+ADR=" + sw);
                }
                if (sendType == 1) {
                    send("AT+CFM=" + sw);
                }
                if (sendType == 2) {
                    send("AT+NJM=" + sw);
                }
            }
        });

        btn_selete.setText(getResources().getString(R.string.text_code) + "(" + getResources().getString(R.string.ascii) + ")");
        btn_send_hex.setText(getResources().getString(R.string.text_code_send) + "(" + getResources().getString(R.string.ascii) + ")");

        txt_atdetails.setText(atDetailsList[0]);
        edittext1.setText(atList[0]);
    }

    public void setBtn() {
        txt_time1.setBackgroundResource(R.drawable.bg_black);
        txt_time2.setBackgroundResource(R.drawable.bg_black);
        txt_time3.setBackgroundResource(R.drawable.bg_black);
        txt_time4.setBackgroundResource(R.drawable.bg_black);
    }

    private Boolean isSwitch;

    public void switchrigth(Boolean isSwitch) {
        if (isSwitch) {
            view1.setVisibility(View.VISIBLE);
            view2.setVisibility(View.GONE);
            btn_send.setVisibility(View.GONE);
        } else {
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.VISIBLE);
            btn_send.setVisibility(View.VISIBLE);
        }
    }


    public void initData() {

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void UODATE(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.UODATE)) {
            return;
        }
        BleDevice device = event.getBody();
        if (device.getName() != null) {
//            txt2.setText("已成功连接（" + device.getName() + ")");

        } else {
//            txt2.setText("已成功连接（" + device.getMac() + ")");
        }
        isLink = true;
        auto = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void upData(MessageEvent<String> event) {
        if (!event.getId().equals(EventBusId.upData)) {
            return;
        }
        if (!isPause) {
            String data = TextUtils.trim(event.getBody()).toUpperCase();
            logList.add(TextUtils.decode(data));
            logAdapter.addResult(logList);
            logAdapter.notifyDataSetChanged();
        }
    }

    private Boolean isLink = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void UODATE1(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.UODATE1)) {
            return;
        }
        auto = false;
        title.setText(R.string.text_noconnect);
        isLink = false;
        PreferencesUtil.putBoolean(getActivity(), "auto", auto);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deveui(MessageEvent<String> event) {
        if (!event.getId().equals(EventBusId.deveui)) {
            return;
        }
        title.setText(event.getBody());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        Log.e("onDestroy","onDestroy2");
    }

    public Boolean isPause = false;

    @Override
    public void onClick(View v) {
        if (!isLink) {
            Toast.makeText(getActivity(), getResources().getText(R.string.isConnect), Toast.LENGTH_SHORT).show();
            return;
        }
        MessageEvent<String> messageEvent1 = new MessageEvent<>();
        switch (v.getId()) {

            case R.id.bnt1:
                //
                selectPopup6.show();
                break;
            case R.id.txt_1:
                //
                selectPopup1.show();
                break;
            case R.id.txt1:
                //
                selectPopup2.show();
                break;
            case R.id.txt2:
                //
                selectPopup3.show();
                break;

            case R.id.title_left:
                //重启
                new XPopup.Builder(getActivity()).asConfirm("", getResources().getString(R.string.restart), new OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        send("ATZ");
                    }
                }).show();

                break;
            case R.id.btn_right:
                //切换
                isSwitch = !isSwitch;
                switchrigth(isSwitch);
                break;
            case R.id.btn_clear_log:
                //重启
                new XPopup.Builder(getActivity()).asConfirm("", getResources().getString(R.string.isclearlog), new OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        clearLog();
                    }
                }).show();
                break;
            case R.id.btn_save:
                //
                new XPopup.Builder(getActivity()).asConfirm(getResources().getString(R.string.save), getResources().getString(R.string.issave), new OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        saveLog();
                    }
                }).show();
                break;
            case R.id.btn_suspend:
                //重启
                pauseLog();
                break;
            case R.id.btn_send_code:
                //发送
                sendCode();
                break;
            case R.id.btn_send:
                //发送
                sendCode1();
                break;

            case R.id.btn_send_hex:
                //发送接收编码

                selectPopup5.show();
                break;
            case R.id.btn_selete:
                //日志接收编码

                selectPopup4.show();
                break;

            case R.id.txt_time1:
                //重启
                setBtn();
                v.setBackgroundResource(R.drawable.bg_black_on);
                timeType = 5;
                break;
            case R.id.txt_time2:
                //重启
                setBtn();
                v.setBackgroundResource(R.drawable.bg_black_on);
                timeType = 10;
                break;
            case R.id.txt_time3:
                //重启
                setBtn();
                v.setBackgroundResource(R.drawable.bg_black_on);
                timeType = 20;
                break;
            case R.id.txt_time4:
                //重启
                setBtn();
                v.setBackgroundResource(R.drawable.bg_black_on);
                timeType = 40;
                break;
            case R.id.btn_time:
                //设置时间
                if (timeType == 0) {
                    if (editTextNumber.getText().toString().trim().length() == 0) {
                        Toast.makeText(getActivity(), getResources().getText(R.string.tdcTime) , Toast.LENGTH_SHORT).show();
                    } else {
                        if (Integer.valueOf(editTextNumber.getText().toString().trim()) == 0) {
                            Toast.makeText(getActivity(), getResources().getText(R.string.tdcTime) , Toast.LENGTH_SHORT).show();
                        }
                        send("AT+TDC=" + Integer.valueOf(editTextNumber.getText().toString().trim()) * 60 * 1000);
                    }
                } else {
                    send("AT+TDC=" + timeType * 60 * 1000);
                }
                break;
        }
    }

    public void clearLog() {
        logList.clear();
        logAdapter.addResult(logList);
        logAdapter.notifyDataSetChanged();
    }

    public void saveLog() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            if (logList.size() > 0) {
                String log = "";
                for (int i = 0; i < logList.size(); i++) {
                    log = log + logList.get(i);
                }
                LogUtil.writerlog(log);
            } else {
                Toast.makeText(getActivity(), getResources().getText(R.string.log_Tips), Toast.LENGTH_SHORT).show();
            }

        } else {
            EasyPermissions.requestPermissions(getActivity(),  getResources().getText(R.string.applyBlue  ).toString(), 10002, perms);
        }


    }

    public void pauseLog() {
        isPause = !isPause;
        if (isPause) {
            btn_suspend.setText(getResources().getText(R.string.Pause_reception));
        } else {
            btn_suspend.setText(getResources().getText(R.string.Continue_receiving));
        }
    }


    public void sendCode1() {
        if (edittext1.getText().toString().trim().length() == 0) {
            Toast.makeText(getActivity(),getResources().getText(  R.string.instructions )  , Toast.LENGTH_SHORT).show();
        } else {

            send(edittext1.getText().toString().trim());
        }
    }

    public void sendCode() {
        if (editTextTextPersonName.getText().toString().trim().length() == 0) {
            Toast.makeText(getActivity(),getResources().getText(R.string.instructions)  , Toast.LENGTH_SHORT).show();
        } else {
            if (sendHex) {
                send(TextUtils.decode(editTextTextPersonName.getText().toString().trim()));
            } else {
                send(editTextTextPersonName.getText().toString().trim());
            }
        }
    }

    private class LogAdapter extends BaseAdapter {

        private Boolean seleteHex;
        private Context context;
        private List<String> logList;
        private String mac;

        LogAdapter(Context context) {
            this.context = context;
            logList = new ArrayList<>();

        }

        public void setSeleteHex(Boolean seleteHex) {
            this.seleteHex = seleteHex;
        }

        void addResult(List<String> characteristicList) {
//            for ( int i=0;i<characteristicList.size();i++ ){
//
//            }
            this.logList = characteristicList;
        }

        void clear() {
            logList.clear();
        }

        @Override
        public int getCount() {
            return logList.size();
        }

        @Override
        public String getItem(int position) {
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
                holder.txt_log.setText(TextUtils.strToASCII(logList.get(position)));
            } else {
                holder.txt_log.setText(logList.get(position));
            }


            return convertView;
        }

        class ViewHolder {
            TextView txt_log;
        }
    }

    public void send(String send) {
        MessageEvent<String> messageEvent1 = new MessageEvent<>();
        messageEvent1.setId(EventBusId.send);
        messageEvent1.setBody(send);
        EventBus.getDefault().post(messageEvent1);
    }
}
