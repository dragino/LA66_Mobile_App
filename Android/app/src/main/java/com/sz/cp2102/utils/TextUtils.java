package com.sz.cp2102.utils;

import android.content.Context;
import android.util.Log;

import com.sz.cp2102.R;
import com.clj.fastble.utils.HexUtil;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class TextUtils {
    /**
     * 功能：取对应位置字节
     */
    public static String data(String hexString, int type) {
        switch (type) {
            case 1:
                //起始位
                return hexString.substring(0, 2);
            case 2:
                //状态字
                return hexString.substring(2, 4);
            case 3:
                //电压
                return hexString.substring(4, 8);
            case 4:
                //电流
                return hexString.substring(8, 12);
            case 5:
                //转速
                return hexString.substring(12, 16);
            case 6:
                //油量
                return hexString.substring(16, 18);
            case 7:
                //保留1
                return hexString.substring(18, 20);
            case 8:
                //运行时间
                return hexString.substring(20, 24);
            case 9:
                //故障代码
                return hexString.substring(24, 28);
            case 10:
                //版本代码
                return hexString.substring(28, 30);
            case 11:
                //校验码
                return hexString.substring(30, 32);
        }
        return "";
    }

    /**
     * 功能：字节转2进制字符串
     */
    public static String hexStringTo2(String hexString) {
        return new BigInteger(hexString, 16).toString(2);
    }

    /**
     * 功能：字节转十进制字符串
     */
    public static int hexStringTo10(String hexString) {

        return Integer.valueOf(new BigInteger(hexString, 16).toString());
    }

    public static String hexStringTo16(int numb) {
        String str = Integer.toHexString(numb);
        String data = "0000".substring(0, 4 - str.length()) + str;
        return data.substring(2, 4) + data.substring(0, 2);
    }

    public static String Stringdi(String numb) {
        Log.e("aaa", numb);
        return numb.substring(2, 4) + numb.substring(0, 2);
    }

    public static String jym(String str) {
        int num = 0;
        for (int i = 0; i < (str.length() / 2); i++) {
            num = num + hexStringTo10(str.substring(0 + 2 * i, 2 * (i + 1)));
        }
        String s16 = Integer.toHexString(num);

        Log.e("654564", num + "");
        String d16 = "";
        if (s16.length() == 1) {
            d16 = "0" + s16;
        } else if (s16.length() > 2) {
            d16 = s16.substring(s16.length() - 2, s16.length());
        } else {
            d16 = s16;
        }
        Log.e("654564", d16 + "");
        Log.e("654564", hexStringTo10(d16) + "");
        String bm = Integer.toHexString(255 - hexStringTo10(d16) + 1);
        if (bm.length() == 1) {
            bm = "0" + bm;
        } else if (bm.length() > 2) {
            bm = bm.substring(bm.length() - 2, bm.length());
        } else {
            bm = bm;
        }
        return str + bm;
    }

    public static String trim(String str) {
        return str.replaceAll("\\s", "");
    }


    public static String zero(String str, int zero) {
        String aa = "";
        for (int i = 0; i < zero; i++) {
            aa = aa + "0";
        }
        return aa.substring(0, zero - str.length()) + str;
    }

    /**
     * 将字符串转成ASCII值
     */
    public static String strToASCII(String data) {
        String requestStr = "";
        for (int i = 0; i < data.length(); i++) {
            char a = data.charAt(i);
            int aInt = (int) a;
            requestStr = requestStr + integerToHexString(aInt);
        }
        return requestStr;
    }

    /**
     * 将十进制整数转为十六进制数，并补位
     */
    public static String integerToHexString(int s) {
        String ss = Integer.toHexString(s);
        if (ss.length() % 2 != 0) {
            ss = "0" + ss;//0F格式
        }
        return ss.toUpperCase();
    }


    private static String hexString = "0123456789abcdef";

    /*
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     */
    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
//将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(HexUtil.hexStringToBytes(bytes));
    }

    /**
     * 功能：开始位
     */
    public static Boolean isStart(String hexString) {
        //
        //Stop Tx events,Please wait for all configurations to print
        //
        return hexString.indexOf("0a0d53746f70205478206576656e74732c506c65617365207761697420666f7220616c6c20636f6e66696775726174696f6e7320746f207072696e740d0a0d0a") != -1 ||
                hexString.indexOf("0A0D53746F70205478206576656E74732C506C65617365207761697420666F7220616C6C20636F6E66696775726174696F6E7320746F207072696E740D0A0D0A") != -1;
    }

    /**
     * 功能：结束位
     */
    public static Boolean isStop(String hexString) {
        //Start Tx events
        //
        //OK
        return hexString.equals("0a0d5374617274205478206576656e74730d0a0d0a4f4b0d0a") ||
                hexString.equals("0A0D5374617274205478206576656E74730D0A0D0A4F4B0D0A");
    }
    /**
     * 功能：是否输入密码
     */
    public static Boolean passwordInput(String hexString ) {
        //Start Tx events
        //
        //OK
        return hexString.indexOf("436F72726563742050617373776F7264")!= -1||
                hexString.indexOf("436f72726563742050617373776f7264")!= -1;
    }

    /**
     * 功能：输入密码正确
     */
    public static Boolean passwordSuccess(String hexString ) {
        //Start Tx events
        //
        //OK
        return hexString.indexOf("436f72726563742050617373776f72640a0d")!= -1||
                hexString.indexOf("436F72726563742050617373776F72640A0D")!= -1;
    }

    /**
     * 功能：输入密码错误
     */
    public static Boolean passwordErr(String hexString ) {
        //Start Tx events
        //
        //OK
        return hexString.indexOf("496e636f72726563742050617373776f72640a0D")!= -1||
                hexString.indexOf("496E636F72726563742050617373776F72640A0D")!= -1;
    }

    /**
     * 功能：已输入过密码
     */
    public static Boolean passwordOnSuccess(String hexString ) {
        //Start Tx events
        //
        //OK
        return hexString.indexOf("0D0A41545F4552524F520D0A")!= -1||
                hexString.indexOf("0d0a41545f4552524f520d0a")!= -1;
    }

    /**
     * 功能：取值
     */
    public static String value(String hexString, String hex) {
        try {
            Log.e("decode", hexString);
            Log.e("decode", strToASCII(hex));
            String valuess = hexString.split(strToASCII(hex))[1];
            Log.e("decode", valuess);
            if (valuess.length() > 0) {
                Log.e("decode", valuess.split("0D0A")[0]);
                return decode(valuess.split("0D0A")[0]);
            } else {
                return "";
            }
        } catch (Exception e) {
            return "无";
        }

    }



    /**
     * 功能： 特殊取值
     */
    public static String value1(String hexString, String hex, int index) {
        try {
        String valuess = hexString.split(strToASCII(hex))[1];
        if (valuess.length() > 0) {
            Log.e("decode", valuess.split("0A0D")[0]);
            return decode(valuess.split("0A0D")[0].split("20")[index]);
        } else {
            return "";
        }
    } catch (Exception e) {
        return "无";
    }

}

    /**
     * 功能：
     */
    public static String getINTMOD(String hexString) {
        try {
        int a = Integer.valueOf(hexString);
        switch (a) {
            case 0:
                return "关闭";
            case 1:
                return "上升或下降";
            case 2:
                return "下降";
            case 3:
                return "上升";
            default:
                return "无";
        }
        } catch (Exception e) {
            return "无";
        }

    }

    /**
     * 功能：返回图片
     */
    public static Integer getImg(String hexString) {
        if (hexString.contains("LBT1")) {
            return R.mipmap.lbt1;
        }
        if (hexString.contains("LDDS20")) {
            return R.mipmap.ldds20;
        }
        if (hexString.contains("LDDS75")) {
            return R.mipmap.ldds75;
        }
        if (hexString.contains("LDS01")) {
            return R.mipmap.lds01;
        }
        if (hexString.contains("LDS02")) {
            return R.mipmap.lds02;
        }
        if (hexString.contains("LGT")) {
            return R.mipmap.lgt;
        }
        if (hexString.contains("LHT65")) {
            return R.mipmap.lht65;
        }
        if (hexString.contains("LLDS12")) {
            return R.mipmap.llds12;
        }
        if (hexString.contains("LLMS01")) {
            return R.mipmap.llms01;
        }
        if (hexString.contains("LSE01")) {
            return R.mipmap.lse01;
        }

        if (hexString.contains("LSN50V2_D20")) {
            return R.mipmap.lsn50v2_d20;
        }
        if (hexString.contains("LSN50V2_S31")) {
            return R.mipmap.lsn50v2_s31;
        }
        if (hexString.contains("LSN50V2_S31B")) {
            return R.mipmap.lsn50v2_s31b;
        }
        if (hexString.contains("LSPH01")) {
            return R.mipmap.lsph01;
        }
        if (hexString.contains("LT_22222")) {
            return R.mipmap.lt_22222;
        }
        if (hexString.contains("LTC2")) {
            return R.mipmap.ltc2;
        }
        if (hexString.contains("LWL01")) {
            return R.mipmap.lwl01;
        }
        if (hexString.contains("LWL02")) {
            return R.mipmap.lwl02;
        }
        if (hexString.contains("RS485_LN")) {
            return R.mipmap.rs485_ln;
        }
        return R.mipmap.logo1;
    }

}
