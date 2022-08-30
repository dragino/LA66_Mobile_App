package com.sz.cp2102.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sz.cp2102.SearchActivity;
import com.sz.cp2102.adapter.MallPagerAdapter;
import com.sz.cp2102.bean.MessageEvent;
import com.sz.cp2102.utils.LanguageUtil;
import com.sz.cp2102.utils.PreferencesUtil;
import com.sz.cp2102.view.DigitalTextView;
import com.sz.cp2102.R;
import com.sz.cp2102.bean.BleHex;
import com.sz.cp2102.config.EventBusId;
import com.sz.cp2102.utils.TextUtils;
import com.clj.fastble.data.BleDevice;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.tmall.ultraviewpager.UltraViewPager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {


    private UltraViewPager ultraViewPager;
    //    private ListView listView;
//    private MainActivity.ResultAdapter mResultAdapter;
    private TextView txt_language;
    private View title_left;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        EventBus.getDefault().register(this);
        initView(view);
        initData();


        return view;
    }

    private Boolean isEu;
    private BasePopupView loadingPopup;

    private BasePopupView loadingPopup1;

//    private void add(String aa) {
//        mResultAdapter.addResult(aa);
//        mResultAdapter.notifyDataSetChanged();
//    }

    public void initView(View view) {
//        listView = view.findViewById(R.id.listView);
//        mResultAdapter = new MainActivity.ResultAdapter(getActivity());
//        listView.setAdapter(mResultAdapter);
        ultraViewPager = view.findViewById(R.id.ultra_viewpager);
        txt_language = view.findViewById(R.id.txt_language);
        title_left = view.findViewById(R.id.title_left);
        loadingPopup = new XPopup.Builder(getActivity()).asLoading("启动中");
        loadingPopup1 = new XPopup.Builder(getActivity()).asConfirm("故障提示", destaStrMain, new OnConfirmListener() {
            @Override
            public void onConfirm() {
                loadingPopup1.dismiss();
            }
        });
        swtBanner();
        isEu = PreferencesUtil.getBoolean(getContext(), "isEu", false);

        txt_language.setText(isEu ? "eu" : "中文");
        view.findViewById(R.id.btn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesUtil.putBoolean(getContext(), "isReset", true);
                MessageEvent<BleDevice> messageEvent = new MessageEvent<>();
                messageEvent.setId(EventBusId.disconnect);
                EventBus.getDefault().post(messageEvent);
                PreferencesUtil.putBoolean(getContext(), "isEu", !isEu);
                LanguageUtil.set(!isEu, getActivity());
            }
        });
        view.findViewById(R.id.title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageEvent<BleDevice> messageEvent1 = new MessageEvent<>();
                messageEvent1.setId(EventBusId.goSearch);
                EventBus.getDefault().post(messageEvent1);
            }
        });
    }



    private List<Integer> bannerList;
    private MallPagerAdapter mallItemAdapter;

    public void swtBanner() {
        //以下是设置轮播图
        bannerList = new ArrayList<>();
        bannerList.add(R.mipmap.laq4);
        bannerList.add(R.mipmap.lbt1);
        bannerList.add(R.mipmap.ldds20);
        bannerList.add(R.mipmap.ldds75);
        bannerList.add(R.mipmap.lds01);
        bannerList.add(R.mipmap.lds02);
        bannerList.add(R.mipmap.lgt);
        bannerList.add(R.mipmap.lht65);
        bannerList.add(R.mipmap.llds12);
        bannerList.add(R.mipmap.llms01);
        bannerList.add(R.mipmap.lse01);
        bannerList.add(R.mipmap.lsn50v2_d20);
        bannerList.add(R.mipmap.lsn50v2_s31);
        bannerList.add(R.mipmap.lsn50v2_s31b);
        bannerList.add(R.mipmap.lsph01);
        bannerList.add(R.mipmap.lt_22222);
        bannerList.add(R.mipmap.ltc2);
        bannerList.add(R.mipmap.lwl01);
        bannerList.add(R.mipmap.lwl02);
        bannerList.add(R.mipmap.rs485_ln);
        ultraViewPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
        //UltraPagerAdapter 绑定子view到UltraViewPager
        mallItemAdapter = new MallPagerAdapter(bannerList, getActivity());
/*
        ((MallPagerAdapter) adapter).setOnBannerClickListener(new MallPagerAdapter.OnBannerClickListener() {
            @Override
            public void onClick(int position) {
                //类型(1商品跳转 2外部链接 3其它)
                if (bannerList.get(position).getBannerType().equals("1")) {
                    getGoodsById(bannerList.get(position).getBannerUrl());
                } else if (bannerList.get(position).getBannerType().equals("2")) {
                    try {
                        Uri uri = Uri.parse(bannerList.get(position).getBannerUrl());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } catch (Exception e) {

                    }
                }
            }
        });
*/
        ultraViewPager.setAdapter(mallItemAdapter);
        //内置indicator初始化
        ultraViewPager.initIndicator();
        //设置indicator样式
        ultraViewPager.getIndicator()
                .setOrientation(UltraViewPager.Orientation.HORIZONTAL)
                .setFocusResId(R.mipmap.img_yuandian1)
                .setNormalResId(R.mipmap.img_yuandian2)
                .setRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
        //设置indicator对齐方式
        ultraViewPager.getIndicator().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        ultraViewPager.getIndicator().setMargin(0,0,0,30);
        //构造indicator,绑定到UltraViewPager
        ultraViewPager.getIndicator().build();

        //设定页面循环播放
        ultraViewPager.setInfiniteLoop(true);
        //设定页面自动切换  间隔1秒
        ultraViewPager.setAutoScroll(2000);
    }

    public void initData() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);

        Log.e("onDestroy","onDestroy1");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void upData(MessageEvent<String> event) {
        if (!event.getId().equals(EventBusId.upData)) {
            return;
        }
        String data = TextUtils.trim(event.getBody()).toUpperCase();

    }

    String destaStrMain = "";

    public String str(String str, String str1) {
        Log.e("datahome1122", str);
        Log.e("datahome1122", str1);
        if (str.length() > 0) {
            return str + "、" + str1;
        } else {
            return str + str1;
        }
    }

}
