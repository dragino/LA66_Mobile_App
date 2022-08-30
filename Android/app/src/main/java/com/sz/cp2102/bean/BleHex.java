package com.sz.cp2102.bean;

public class BleHex {
    //启动
    public static final String START = "5A8101000000000000000000000000";
    //停止
    public static final String STOP =  "5A8100000000000000000000000000";
    //自启电压
    public static final String setV = "5A8501";
    //输出电压
    public static final String setVMAX = "5A8201";
    //自熄电流
    public static final String setI = "5A8601";
    //最大限流值
    public static final String setIMAX = "5A8301";
    //end
    public static final String setEnd = "00000000000000000000";
}
