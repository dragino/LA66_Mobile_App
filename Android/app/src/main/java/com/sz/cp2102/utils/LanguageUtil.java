package com.sz.cp2102.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import com.sz.cp2102.BleActiity;

import java.util.Locale;

/**
 * 语言切换
 * Created by 41455 on 2016/10/13.
 */
public class LanguageUtil {
    /**
     * @param isEnglish true  ：点击英文，把中文设置未选中
     *                  false ：点击中文，把英文设置未选中
     */
    public static void set(boolean isEnglish, Activity activity) {
        Configuration configuration = activity.getResources().getConfiguration();
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        if (isEnglish) {
            //设置英文
            configuration.locale = Locale.ENGLISH;
        } else {
            //设置中文
            configuration.locale = Locale.SIMPLIFIED_CHINESE;
        }
        //更新配置
        activity.getResources().updateConfiguration(configuration, displayMetrics);

        //更新语言后，destroy当前页面，重新绘制

        activity.finish();

        Intent it = new Intent(activity, BleActiity.class);
        //清空任务栈确保当前打开activit为前台任务栈栈顶
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(it);
    }
}
