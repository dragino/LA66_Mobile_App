package com.sz.cp2102.adapter;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.viewpager.widget.PagerAdapter;

import com.sz.cp2102.R;

import java.util.List;

public class MallPagerAdapter extends PagerAdapter {

    private List<Integer> list;
    private Context context;

    public MallPagerAdapter(List<Integer> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.item_banner, null);
        ImageView imageView = linearLayout.findViewById(R.id.iv_banner);
        imageView.setImageResource(list.get(position));
        container.addView(linearLayout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return linearLayout;
    }

    public  void openBrowser(String url) {
        if (url==null){

            return;
        }
        if (url.length()==0){

            return;
        }
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if (url.indexOf("http") == -1){
            intent.setData(Uri.parse("https://"+url));
        }else {
            intent.setData(Uri.parse(url));
        }
        // 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
        // 官方解释 : Name of the component implementing an activity that can display the intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            final ComponentName componentName = intent.resolveActivity(context.getPackageManager());
            Log.e("suyan = " , componentName.getClassName()+"");
            context.startActivity(Intent.createChooser(intent, "请选择浏览器"));
        } else {
            Toast.makeText(context,"链接错误或无浏览器",Toast.LENGTH_LONG).show();
//            showText("链接错误或无浏览器");
//            GlobalMethod.showToast(context, "链接错误或无浏览器");
        }
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        LinearLayout view = (LinearLayout) object;
        container.removeView(view);
    }

    private OnBannerClickListener onBannerClickListener;

    public interface OnBannerClickListener {
        void onClick(int position);
    }

    public void setOnBannerClickListener(OnBannerClickListener onBannerClickListener) {
        this.onBannerClickListener = onBannerClickListener;
    }
}

