package com.sz.cp2102.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sz.cp2102.BleActiity;
import com.sz.cp2102.bean.MessageEvent;
import com.sz.cp2102.R;
import com.sz.cp2102.bean.BleHex;
import com.sz.cp2102.config.EventBusId;
import com.sz.cp2102.utils.LogUtil;
import com.sz.cp2102.utils.PreferencesUtil;
import com.sz.cp2102.utils.TextUtils;
import com.sz.cp2102.utils.logFile;
import com.clj.fastble.BleManager;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import com.lxj.xpopup.interfaces.OnSelectListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class LogFragment extends Fragment {
    private ListView listView;
    public List<logFile> logList = new ArrayList<>();
    private LogAdapter logAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        EventBus.getDefault().register(this);

        initView(view);
        initData();
        Log.e("onCreateView11", "onCreateView222");
        return view;
    }

    public void initView(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        listView = view.findViewById(R.id.listView);
        logAdapter = new LogAdapter(getContext());
        listView.setAdapter(logAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //????????????????????????
                setdata();
            }
        });
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            setdata();
        } else {
            EasyPermissions.requestPermissions(getActivity(), getResources().getText(R.string.applyBlue).toString(), 10002, perms);
        }
    }

    public void setdata() {
        logList.clear();
        logList.addAll(LogUtil.getAllDataFileName());
        logAdapter.addResult(logList);
        logAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }


    public void initData() {
        int timeAll = PreferencesUtil.getInt(getContext(), "timeAll", 0);
        int time = PreferencesUtil.getInt(getContext(), "time", 0);
        timeAll = timeAll + time;
        PreferencesUtil.putInt(getContext(), "timeAll", timeAll);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.e("onDestroy", "onDestroy3");
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }


    private int timeAll = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void upData(MessageEvent<String> event) {
        if (!event.getId().equals(EventBusId.upData)) {
            return;
        }
    }


    private class LogAdapter extends BaseAdapter {

        private Boolean seleteHex;
        private Context context;
        private List<logFile> logList;
        private String mac;

        LogAdapter(Context context) {
            this.context = context;
            logList = new ArrayList<>();

        }

        public void setSeleteHex(Boolean seleteHex) {
            this.seleteHex = seleteHex;
        }

        void addResult(List<logFile> characteristicList) {
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
        public logFile getItem(int position) {
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
                convertView = View.inflate(context, R.layout.adapter_log_file, null);
                holder = new LogAdapter.ViewHolder();
                holder.txt_log = (TextView) convertView.findViewById(R.id.txt_log);
                holder.btn_selete = (TextView) convertView.findViewById(R.id.btn_selete);
                convertView.setTag(holder);
            }
//            holder.txt_title.setText("??????:");
            holder.txt_log.setText(logList.get(position).getName());
            holder.txt_log.setTag(logList.get(position).getPath());
            holder.txt_log.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = (String) v.getTag();
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.addCategory(Intent.CATEGORY_DEFAULT);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    Uri uri = Uri.fromFile(new File(LogUtil.getFilePath(path)));
//                    intent.setDataAndType(uri, "text/plain");
//                    context.startActivity(intent);
                    openFile(path);
                }
            });
            holder.btn_selete.setTag(logList.get(position).getPath());
            holder.btn_selete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = (String) v.getTag();
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.addCategory(Intent.CATEGORY_DEFAULT);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    Uri uri = Uri.fromFile(new File(LogUtil.getFilePath(path)));
//                    intent.setDataAndType(uri, "text/plain");
//                    context.startActivity(intent);
                    showDelete(path);
                }
            });


            return convertView;
        }

        class ViewHolder {
            TextView txt_log;
            TextView btn_selete;

        }
    }

    public void showEdittex(final String delFile) {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            new XPopup.Builder(getActivity()).asInputConfirm(getResources().getString(R.string.deteleLog), getResources().getString(R.string.file), new OnInputConfirmListener() {
                @Override
                public void onConfirm(String text) {
                    if (text.length()>0){
                        renameFile(delFile,text);
                    }else {
                        Toast.makeText(getActivity(), R.string.textEmpty, Toast.LENGTH_SHORT).show();
                    }
                }
            }).show();
        } else {
            EasyPermissions.requestPermissions(getActivity(), getResources().getText(R.string.applyBlue).toString(), 10002, perms);
        }
    }

    public void showDelete(final String delFile) {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            new XPopup.Builder(getActivity()).asConfirm(getResources().getString(R.string.deteleLog),
                    getResources().getString(R.string.deleteDevice), new OnConfirmListener() {
                        @Override
                        public void onConfirm() {
                            if (delete(delFile)) {
                                setdata();
                            }
                        }
                    }).show();
        } else {
            EasyPermissions.requestPermissions(getActivity(), getResources().getText(R.string.applyBlue).toString(), 10002, perms);
        }
    }

    /**
     * oldPath ??? newPath????????????????????????????????????
     */
    private void renameFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        if (oldFile.exists()) {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File(filePath, "bleLog");
            File newFile = new File(dir, newPath + ".txt");
            boolean b = oldFile.renameTo(newFile);
            File file2 = new File(newFile.getPath());
            setdata();
        } else {
            Toast.makeText(getActivity(), R.string.fileIsdelete, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param delFile ?????????????????????????????????
     * @return ??????????????????true???????????????false
     */
    public boolean delete(String delFile) {
        File file = new File(delFile);
        if (!file.exists()) {
//            Toast.makeText(HnUiUtils.getContext(), "??????????????????:" + delFile + "????????????", Toast.LENGTH_SHORT).show();
            Log.e("??????????????????:", delFile + "????????????");
            return false;
        } else {
            if (file.isFile())
                return deleteSingleFile(delFile);
            else
                return deleteDirectory(delFile);
        }
    }

    /**
     * ??????????????????
     *
     * @param filePath$Name ??????????????????????????????
     * @return ??????????????????????????????true???????????????false
     */
    public static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // ????????????????????????????????????????????????????????????????????????????????????
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--Method--", "Copy_Delete.deleteSingleFile: ??????????????????" + filePath$Name + "?????????");

                return true;
            } else {
                Log.e("??????????????????", filePath$Name + "?????????");
                return false;
            }
        } else {
            Log.e("???????????????????????????", filePath$Name + "????????????");
            return false;
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param filePath ?????????????????????????????????
     * @return ????????????????????????true???????????????false
     */
    public static boolean deleteDirectory(String filePath) {
        // ??????dir?????????????????????????????????????????????????????????
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
        File dirFile = new File(filePath);
        // ??????dir???????????????????????????????????????????????????????????????
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            Log.e("?????????????????????", filePath + "????????????");
            return false;
        }
        boolean flag = true;
        // ????????????????????????????????????????????????
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // ???????????????
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag)
                    break;
            }
            // ???????????????
            else if (file.isDirectory()) {
                flag = deleteDirectory(file
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            Log.e("?????????????????????", "544");
            return false;
        }
        // ??????????????????
        if (dirFile.delete()) {
            Log.e("--Method--", "Copy_Delete.deleteDirectory: ????????????" + filePath + "?????????");
            return true;
        } else {
            Log.e("???????????????", filePath + "?????????");
            return false;
        }
    }
    /**???????????????????????????dataType**/
    private static final String DATA_TYPE_ALL = "*/*";//?????????????????????????????????????????????????????????????????????????????????????????????
    private static final String DATA_TYPE_APK = "application/vnd.android.package-archive";
    private static final String DATA_TYPE_VIDEO = "video/*";
    private static final String DATA_TYPE_AUDIO = "audio/*";
    private static final String DATA_TYPE_HTML = "text/html";
    private static final String DATA_TYPE_IMAGE = "image/*";
    private static final String DATA_TYPE_PPT = "application/vnd.ms-powerpoint";
    private static final String DATA_TYPE_EXCEL = "application/vnd.ms-excel";
    private static final String DATA_TYPE_WORD = "application/msword";
    private static final String DATA_TYPE_CHM = "application/x-chm";
    private static final String DATA_TYPE_TXT = "text/plain";
    private static final String DATA_TYPE_PDF = "application/pdf";
    /**
     * ?????????????????????Uri
     * @param intent ?????????Intent
     * @param file ????????????
     * @return
     */
    private  Uri getUri(Intent intent, File file) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //?????????????????????7.0??????
            uri =
                    FileProvider.getUriForFile(getContext(),
                            getContext().getPackageName() + ".fileprovider",
                            file);
            //???????????????????????????????????????????????????Uri??????????????????
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
    /**
     * ????????????
     * @param filePath ???????????????????????????????????????
     */
    private   void openFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()){
            //?????????????????????
            Toast.makeText(getContext(), "?????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
            return;
        }
        /* ??????????????? */
        String end = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).toLowerCase(Locale.getDefault());
        /* ???????????????????????????MimeType */
        Intent intent = null;
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            intent =  generateVideoAudioIntent(filePath,DATA_TYPE_AUDIO);
        } else if (end.equals("3gp") || end.equals("mp4")) {
            intent = generateVideoAudioIntent(filePath,DATA_TYPE_VIDEO);
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_IMAGE);
        } else if (end.equals("apk")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_APK);
        } else if (end.equals("ppt")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_PPT);
        } else if (end.equals("xls")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_EXCEL);
        } else if (end.equals("doc")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_WORD);
        } else if (end.equals("pdf")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_PDF);
        } else if (end.equals("chm")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_CHM);
        } else if (end.equals("txt")) {
            intent = generateCommonIntent(filePath, DATA_TYPE_TXT);
        } else {
            intent = generateCommonIntent(filePath,DATA_TYPE_ALL);
        }
        getContext().startActivity(intent);
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????Intent
     * @param filePath ????????????
     * @param dataType ????????????
     * @return
     */
    private   Intent generateCommonIntent(String filePath, String dataType) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(filePath);
        Uri uri = getUri(intent, file);
        intent.setDataAndType(uri, dataType);
        return intent;
    }

    /**
     * ??????????????????????????????Intent
     * @param filePath ????????????
     * @param dataType ????????????
     * @return
     */
    private  Intent generateVideoAudioIntent(String filePath, String dataType){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        File file = new File(filePath);
        intent.setDataAndType(getUri(intent,file), dataType);
        return intent;
    }
    /**
     * ???????????????????????????Intent
     * @param filePath ????????????
     * @return
     */
    private  Intent generateHtmlFileIntent(String filePath) {
        Uri uri = Uri.parse(filePath)
                .buildUpon()
                .encodedAuthority("com.android.htmlfileprovider")
                .scheme("content")
                .encodedPath(filePath)
                .build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, DATA_TYPE_HTML);
        return intent;
    }
}

