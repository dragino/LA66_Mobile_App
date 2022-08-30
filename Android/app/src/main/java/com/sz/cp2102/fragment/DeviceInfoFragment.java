package com.sz.cp2102.fragment;

import android.app.Person;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sz.cp2102.BleActiity;
import com.sz.cp2102.R;
import com.sz.cp2102.bean.MessageEvent;
import com.sz.cp2102.config.EventBusId;
import com.sz.cp2102.utils.PreferencesUtil;
import com.sz.cp2102.utils.TextUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.utils.HexUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfoFragment extends Fragment {
    private TextView txt_1;
    private TextView txt_2;
    private TextView txt_3;
    private TextView txt_4;
    private TextView txt_5;
    private TextView txt_6;
    private TextView txt_7;
    private TextView txt_8;
    private TextView txt_9;
    private TextView txt_10;
    private TextView txt_11;
    private TextView txt_12;
    private TextView txt_13;
    private TextView txt_14;
    private TextView txt_15;
    private TextView txt_16;
    private TextView txt1;
    private TextView txt2;
    private ImageView img_11;
    private EditText editTextNumber;
    private ArrayList<String> pList = new ArrayList<String>();
    private ListView listView;
    private PasswordAdapter passwordAdapter;
    private Boolean isFrist=true;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        EventBus.getDefault().register(this);
        initView(view);
//        initData();
        Log.e("onCreateView11", "onCreateView222");
        return view;
    }


    private void initView(View view) {
        listView = view.findViewById(R.id.listView);
        passwordAdapter = new PasswordAdapter(getContext(), new Odclick() {
            @Override
            public void onPwClick(String str) {
                editTextNumber.setText(str );
                listView.setVisibility(View.INVISIBLE);
            }
        });
        listView.setAdapter(passwordAdapter);
        listView.setVisibility(View.INVISIBLE);
        txt_1 = view.findViewById(R.id.txt_1);
        txt_2 = view.findViewById(R.id.txt_2);
        txt_3 = view.findViewById(R.id.txt_3);
        txt_4 = view.findViewById(R.id.txt_4);
        txt_5 = view.findViewById(R.id.txt_5);
        txt_6 = view.findViewById(R.id.txt_6);
        txt_7 = view.findViewById(R.id.txt_7);
        txt_8 = view.findViewById(R.id.txt_8);
        txt_9 = view.findViewById(R.id.txt_9);
        txt_10 = view.findViewById(R.id.txt_10);
        txt_11 = view.findViewById(R.id.txt_11);
        txt_12 = view.findViewById(R.id.txt_12);
        txt_13 = view.findViewById(R.id.txt_13);
        txt_14 = view.findViewById(R.id.txt_14);
        txt_15 = view.findViewById(R.id.txt_15);
        txt_16 = view.findViewById(R.id.txt_16);
        txt1 = view.findViewById(R.id.txt1);
        txt2 = view.findViewById(R.id.txt2);
        img_11 = view.findViewById(R.id.img_11);
        editTextNumber = view.findViewById(R.id.editTextNumber);
        view.findViewById(R.id.title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageEvent<BleDevice> messageEvent = new MessageEvent<>();
                messageEvent.setId(EventBusId.dislink);
                EventBus.getDefault().post(messageEvent);
            }
        });
        String password = PreferencesUtil.getString(getActivity(),"password");
        String passwordL = PreferencesUtil.getString(getActivity(),"passwordList");
        editTextNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.e("afterTextChanged","56");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("afterTextChanged","57");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e("afterTextChanged","52");
                if(!isFrist){
                    listView.setVisibility(View.VISIBLE);
                }else {
                    isFrist=false;
                }
            }
        });
        editTextNumber.setText(password);
        if (passwordL!=null&& passwordL.length()>0){
            pList =  new Gson().fromJson(passwordL, new TypeToken<List<String>>() {}.getType());
        }
        passwordAdapter.addResult( pList);
        passwordAdapter.notifyDataSetChanged();
        view.findViewById(R.id.btn_send_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setVisibility(View.INVISIBLE);

                if (editTextNumber.getText().toString().length() > 0) {
                    String password = editTextNumber.getText().toString();
                    MessageEvent<String> messageEvent1 = new MessageEvent<>();
                    messageEvent1.setId(EventBusId.send);
                    messageEvent1.setBody(password);
                    EventBus.getDefault().post(messageEvent1);
                    PreferencesUtil.putString(getActivity(), "password", (editTextNumber.getText().toString()));
                    Log.e("pList.size()",pList.size()+"**");
                    if (pList.size() == 5) {
                        pList.add(0, password);
                        pList.remove(pList.size() - 1);
                    } else {
                        pList.add(0, password);
                    }
                    Log.e("pList.size()",pList.size()+"**");
                    passwordAdapter.addResult( pList);
                    passwordAdapter.notifyDataSetChanged();
                    PreferencesUtil.putString(getActivity(), "passwordList", new Gson().toJson(pList));
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.placePassword), Toast.LENGTH_SHORT).show();
                }
            }
        });

        txt1.setText(R.string.no_connected);
        view.findViewById(R.id.btn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLink) {
                    Toast.makeText(getActivity(), getResources().getText(R.string.isConnect), Toast.LENGTH_SHORT).show();
                    return;
                }
                MessageEvent<String> messageEvent1 = new MessageEvent<>();
                messageEvent1.setId(EventBusId.send);
                messageEvent1.setBody("AT+CFG");
                EventBus.getDefault().post(messageEvent1);
            }
        });
    }
    private  Boolean isSend=false;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deviceDetails(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.deviceDetails)) {
            return;
        }
        if (isLink) {
            MessageEvent<String> messageEvent1 = new MessageEvent<>();
            messageEvent1.setId(EventBusId.send);
            messageEvent1.setBody("AT+CFG");
            EventBus.getDefault().post(messageEvent1);
        }
        if (!isLink) {
            Toast.makeText(getActivity(), getResources().getText(R.string.isConnect), Toast.LENGTH_SHORT).show();
        }
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
        MessageEvent<String> messageEvent1 = new MessageEvent<>();
        messageEvent1.setId(EventBusId.send);
        messageEvent1.setBody("AT+CFG");
        EventBus.getDefault().post(messageEvent1);
        isLink = true;
//        auto = true;
    }

    private Boolean isLink = true;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void UODATE1(MessageEvent<BleDevice> event) {
        if (!event.getId().equals(EventBusId.UODATE1)) {
            return;
        }
//        auto = false;
//        txt2.setText("未连接");
        txt1.setText(R.string.text_noconnect);
        isLink = false;
        txt2.setText("Deveui");
        txt_1.setText("0");
        txt_2.setText("0");
        txt_3.setText("0");
        txt_4.setText("0");
        txt_5.setText("0");
        txt_6.setText("0");
        txt_7.setText("0");
        txt_8.setText("0");
        txt_9.setText("0");
        txt_10.setText("0");
        txt_11.setText("0");
        txt_12.setText("0");
        txt_13.setText("0");
        txt_14.setText("0");
        txt_15.setText("0");
        txt_16.setText("0");
//        PreferencesUtil.putBoolean(getActivity(), "auto", auto);
    }

    private Boolean isConfing = false;
    private String config = "";
    private String config1 = "53 74 6F 70 20 54 78 20 65 76 65 6E 74 73 2C 50 6C 65 61 73 65 20 77 61 69 74 20 66 6F 72 20 61 6C 6C 20 63 6F 6E 66 69 67 75 72 61 74 69 6F 6E 73 20 74 6F 20 70 72 69 6E 74 0D 50 72 69 6E 74 66 20 61 6C 6C 20 63 6F 6E 66 69 67 2E 2E 2E 0D 41 54 2B 44 45 55 49 3D 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 0D 0A 41 54 2B 44 41 44 44 52 3D 30 31 30 31 30 31 30 31 0A 0D 41 54 2B 41 50 50 4B 45 59 3D 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 0D 0A 41 54 2B 4E 57 4B 53 4B 45 59 3D 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 0D 0A 41 54 2B 41 50 50 53 4B 45 59 3D 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 0D 0A 41 54 2B 41 50 50 45 55 49 3D 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 0D 0A 41 54 2B 41 44 52 3D 30 0D 0A 41 54 2B 54 58 50 3D 30 0D 0A 41 54 2B 44 52 3D 30 0D 0A 41 54 2B 44 43 53 3D 30 0D 0A 41 54 2B 50 4E 4D 3D 30 0D 0A 41 54 2B 52 58 32 46 51 3D 30 0D 0A 41 54 2B 52 58 32 44 52 3D 30 0D 0A 41 54 2B 52 58 31 44 4C 3D 30 0D 0A 41 54 2B 52 58 32 44 4C 3D 30 0D 0A 41 54 2B 4A 4E 31 44 4C 3D 30 0D 0A 41 54 2B 4A 4E 32 44 4C 3D 30 0D 0A 41 54 2B 4E 4A 4D 3D 31 0D 0A 41 54 2B 4E 57 4B 49 44 3D 30 30 20 30 30 20 30 30 20 30 30 0D 0A 41 54 2B 46 43 55 3D 30 0D 0A 41 54 2B 46 43 44 3D 30 0D 0A 41 54 2B 43 4C 41 53 53 3D 41 0D 0A 41 54 2B 4E 4A 53 3D 30 0D 0A 41 54 2B 52 45 43 56 42 3D 30 3A 0D 0A 41 54 2B 52 45 43 56 3D 30 3A 0D 0A 41 54 2B 56 45 52 3D 76 31 2E 31 20 41 53 39 32 33 0A 0D 41 54 2B 43 46 4D 3D 30 0D 0A 41 54 2B 43 46 53 3D 30 0D 0A 41 54 2B 53 4E 52 3D 30 0D 0A 41 54 2B 52 53 53 49 3D 30 0D 0A 41 54 2B 54 44 43 3D 33 30 30 30 30 30 0D 0A 41 54 2B 50 4F 52 54 3D 30 0D 0A 41 54 2B 50 57 4F 52 44 3D 30 0D 0A 41 54 2B 43 48 53 3D 30 0D 0A 41 54 2B 53 4C 45 45 50 3D 30 0D 0A 41 54 2B 45 58 54 3D 31 0D 0A 41 54 2B 42 41 54 3D 33 36 37 33 0D 0A 41 54 2B 57 4D 4F 44 3D 30 0D 0A 41 54 2B 41 52 54 45 4D 50 3D 2D 32 30 30 2C 38 30 30 2C 2D 32 30 30 2C 38 30 30 0D 0A 41 54 2B 43 49 54 45 4D 50 3D 31 0D 0A 41 54 2B 44 57 45 4C 4C 54 3D 30 0D 0A 41 54 2B 52 4A 54 44 43 3D 32 30 0D 0A 41 54 2B 52 50 4C 3D 30 0D 0A 41 54 2B 54 49 4D 45 53 54 41 4D 50 3D 73 79 73 74 69 6D 65 3D 20 31 36 31 31 38 38 32 30 31 31 20 32 30 32 31 20 31 20 32 39 20 31 20 30 20 31 31 0A 0D 41 54 2B 4C 45 41 50 53 45 43 3D 30 0D 0A 41 54 2B 53 59 4E 43 4D 4F 44 3D 30 0D 0A 41 54 2B 53 59 4E 43 54 44 43 3D 30 0D 0A 41 54 2B 52 43 41 4C 3D 73 65 67 6D 65 6E 74 20 30 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 31 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 32 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 33 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 34 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 35 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 36 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 37 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 38 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 39 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 31 30 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 31 31 3D 30 20 30 0D 41 54 2B 52 43 41 42 4C 45 3D 30 2E 30 30 30 20 30 2E 30 30 30 0A 0D 41 54 2B 45 4E 50 54 43 48 4E 55 4D 3D 30 0D 0A 53 74 61 72 74 20 54 78 20 65 76 65 6E 74 73 0D ";

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void upConfig(MessageEvent<String> event) {
        if (!event.getId().equals(EventBusId.upConfig)) {
            return;
        }
        String data = TextUtils.trim(event.getBody()).toUpperCase();
        Log.e("upConfigdata", data);
        Log.e("upConfig", "upConfig");
        Log.e("upConfig1", TextUtils.isStart(data) + "");
        Log.e("upConfig2", TextUtils.isStop(data) + "");
        if (isConfing) {
            config = config + data;
        }
        if (TextUtils.isStart(data)) {
            isConfing = TextUtils.isStart(data);
            config = "";
        }
        if (TextUtils.isStop(data)) {
            isConfing = TextUtils.isStart(data);
            config = config + data;
//            config =  config1.toString().replace(" ","");
            setData();
        }
        if (isConfing) {
            config = config + data;
        }
    }

    public void setData() {
        Log.e("setData", config);
        try {
            txt1.setText(TextUtils.value(config, "AT+MODEL=").split(",")[0]);
            img_11.setImageResource(TextUtils.getImg(TextUtils.value(config, "AT+MODEL=").split(",")[0]));
        } catch (Exception e) {
            txt1.setText(getResources().getText(R.string.unknown));
        }
        txt2.setText(TextUtils.value(config, "AT+DEUI="));
        try {
            txt_1.setText(Integer.parseInt(TextUtils.value(config, "AT+TDC=")) / 1000 + "s");
        } catch (Exception e) {
            txt1.setText("0");
        }
        txt_2.setText(TextUtils.value1(config, "AT+VER=", 0));
        txt_3.setText(TextUtils.value(config, "AT+NJM=").equals("1") ? "OTAA" : "ABP");
        txt_4.setText(TextUtils.value(config, "AT+MOD="));
        txt_5.setText(getResources().getText(R.string.shanghang) + TextUtils.value(config, "AT+FCU=") + "\n" + getResources().getText(R.string.xiahang) + TextUtils.value(config, "AT+FCD="));
        txt_6.setText(TextUtils.value1(config, "AT+VER=", 1));
        txt_7.setText(TextUtils.value(config, "AT+NJS=").equals("1") ? getResources().getText(R.string.on_connet) : getResources().getText(R.string.un_connet));
        txt_8.setText(TextUtils.value(config, "AT+ADR=").equals("1") ? getResources().getText(R.string.open) : getResources().getText(R.string.close));
        txt_9.setText(TextUtils.value(config, "AT+DR="));
        txt_10.setText(TextUtils.getINTMOD(TextUtils.value(config, "AT+INTMOD=")));
        txt_11.setText(TextUtils.value(config, "AT+CFM=").equals("1") ? getResources().getText(R.string.open) : getResources().getText(R.string.close));
        txt_12.setText(TextUtils.value(config, "AT+TXP="));
        txt_13.setText(TextUtils.value(config, "AT+5VT="));
        txt_14.setText(TextUtils.value(config, "AT+CHE="));
        txt_15.setText(TextUtils.value(config, "AT+CLASS="));
        txt_16.setText(TextUtils.value(config, "AT+CHS=") + "Hz");
        MessageEvent<String> messageEvent1 = new MessageEvent<>();
        messageEvent1.setId(EventBusId.deveui);
        messageEvent1.setBody(TextUtils.value(config, "AT+DEUI="));
        EventBus.getDefault().post(messageEvent1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.e("onDestroy", "onDestroy4");
    }
    private class PasswordAdapter extends BaseAdapter {

        private Boolean seleteHex;
        private Context context;
        private List<String> logList;
        private String mac;
        private Odclick odclick;

        PasswordAdapter(Context context, Odclick odclick) {
            this.context = context;
            logList = new ArrayList<>();
            this.odclick = odclick;

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
            PasswordAdapter.ViewHolder holder;
            if (convertView != null) {
                holder = (PasswordAdapter.ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(context, R.layout.adapter_log_file1, null);
                holder = new PasswordAdapter.ViewHolder();
                holder.txt_log = (TextView) convertView.findViewById(R.id.txt_log);
                holder. ln= (View) convertView.findViewById(R.id.ln);
                convertView.setTag(holder);
            }
//            holder.txt_title.setText("数据:");
            holder.txt_log.setText(logList.get(position));
            holder. txt_log.setTag(logList.get(position));
            holder. txt_log.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    odclick.onPwClick((String) v.getTag());
//                    String path = (String) v.getTag();
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.addCategory(Intent.CATEGORY_DEFAULT);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    Uri uri = Uri.fromFile(new File(LogUtil.getFilePath(path)));
//                    intent.setDataAndType(uri, "text/plain");
//                    context.startActivity(intent);
                }
            });

            return convertView;
        }

        class ViewHolder {
            TextView txt_log;
            View ln;

        }
    }

    interface Odclick{
        void onPwClick(String str);
    }
}
