package com.sz.cp2102.utils;

import android.os.Environment;
import android.util.Log;

import com.sz.cp2102.fragment.LogFragment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class LogUtil {

    private static final int LEV_D = 1;
    private static final int LEV_W = 2;

    public static String getFilePath(String tag) {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();

        Log.e("currentTimeMillis",filePath+tag);
        return filePath+tag  ;
    }

    /**
     * 为避免产生大量垃圾日志文件，引入一个常量来决定是否需要打印日志
     * @param msg 打印的日志消息
     */
    public static void writerlog(String msg) {
        if (LEV_W == 2) {
            //保存到的文件路径
            final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            FileWriter fw = null;
            BufferedWriter bw = null;

            try {
                //创建文件夹
                File dir = new File(filePath, "cp2102Log");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                long time = System.currentTimeMillis();
                SimpleDateFormat sDateFormat =  new  SimpleDateFormat("yyyyMMddhhmmss");   //此处格式可以任意设置
                String  date =  sDateFormat.format(time  );
                //创建文件
                Log.e("currentTimeMillis",date);
                File file = new File(dir, date.trim()+".txt");
                if (!file.exists()) {
                    file.createNewFile();
                }
                //写入日志文件
                fw = new FileWriter(file, true);
                bw = new BufferedWriter(fw);
                bw.write( msg + "\n");
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }


    }


    // 数据文件夹
    private final String dataFile = "bleLog";

    /**
     * 生成文件夹
     * */
    private static File makeDataFile() {
        File file = null;
        final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            file =  new File(filePath, "bleLog");
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
        return  file;
    }

    /**
     * 获取dataFile文件夹下的所有文件
     * */
    public static ArrayList<logFile> getAllDataFileName(){
        try{


        // 文件夹路径
        String collectionPath = makeDataFile().getPath();

        ArrayList<logFile> fileList = new ArrayList<>();

        File file = new File(collectionPath);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                System.out.println("文     件：" + tempList[i].getName());
                // tempList[i].toString();// 路径
                // tempList[i].getName();// 文件名
                // 文件名
                logFile logFile=new logFile();
                String fileName = tempList[i].getName();
                logFile.setName(fileName);
                logFile.setPath(tempList[i].getPath());
                    // 文件大小
                    // String fileSize = FileSizeUtil.getAutoFileOrFilesSize(tempList[i].toString());
                    fileList.add(logFile);
            }
        }

        return fileList;
        }catch (Exception e){
            return new ArrayList<logFile>();
        }
    }
}
