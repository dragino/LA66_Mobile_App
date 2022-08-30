package com.sz.cp2102.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.clj.fastble.utils.HexUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

public class USBConnectionManager {
    private UsbManager manager;
    private UsbDevice mUsbDevice;
    private UsbInterface mInterface;
    private UsbEndpoint usbEpOut;

    private UsbEndpoint usbEpIn;
    private UsbDeviceConnection mDeviceConnection;
    private final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private OnUSBInitListener mOnUSBInitListener;

    public UsbEndpoint getUsbEpIn() {
        return usbEpIn;
    }

    public UsbEndpoint getUsbEpOut() {
        return usbEpOut;
    }

    //    int vID, int pID,
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public USBConnectionManager(Context context, OnUSBInitListener onUSBInitListener) {
        this.mOnUSBInitListener = onUSBInitListener;
        close();
        manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device.getProductName().indexOf("CP2102") != -1) {
                Log.e("getProductName", "CP2102");
                mUsbDevice = device;
                break;
            }
        }
        if (mUsbDevice != null) {
            mInterface = mUsbDevice.getInterface(0);
           Log.e("intface","："+ mUsbDevice.getInterfaceCount());
            if (mInterface != null) {
                Log.e("intface111","："+ mInterface.getEndpointCount());
//                if (mInterface.getEndpoint(0) != null) {
//                    usbEpIn = mInterface.getEndpoint(0);
//                }
//                if (mInterface.getEndpoint(1) != null) {
//                    usbEpOut = mInterface.getEndpoint(1);
//                }

                for (int i = 0; i < mInterface.getEndpointCount(); i++) {
                    UsbEndpoint ep = mInterface.getEndpoint(i);
                    Log.e("UsbEndpoint","UsbEndpoint:"+ep.getType() );
                    Log.e("UsbEndpoint1","UsbEndpoint1:"+ep.getDirection() );
                    if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                           Log.e("UsbEndpoint3","UsbEndpoint3:"+i );
                            usbEpOut = ep;
                        } else {
                            usbEpIn = ep;
                        }
                    }
                }


            } else {
                onUSBInitListener.error(OnUSBInitListener.NOT_FOUND_USBINTERFACE);
            }
        } else {
            onUSBInitListener.error(OnUSBInitListener.NOT_FOUND_DEVICE);
        }
    }

    private void getConnection() {
        mDeviceConnection = manager.openDevice(mUsbDevice);
        if (mDeviceConnection == null) {
            mOnUSBInitListener.error(OnUSBInitListener.OPEN_DEVICE_FAILURE);
        } else {
            Log.e("getConnection","getConnection");
//            // Set control line state
            mDeviceConnection.controlTransfer(0x21, 0x22, 0, 0, null, 0, 0);
//            // Set line encoding.
            mDeviceConnection.controlTransfer(0x21, 0x20, 0, 0, getLineEncoding(9600), 7, 0);
            if (mDeviceConnection.claimInterface(mInterface, true)) {
                //初始化成功，可用了
                mOnUSBInitListener.success();

            } else {
                //无通信权限
                mDeviceConnection.close();
                mOnUSBInitListener.error(OnUSBInitListener.OPEN_DEVICE_FAILURE);
            }


        }
    }

    private byte[] getLineEncoding(int baudRate) {
        final byte[] lineEncodingRequest = { (byte) 0x80, 0x25, 0x00, 0x00, 0x00, 0x00, 0x08 };
        //Get the least significant byte of baudRate,
        //and put it in first byte of the array being sent
        lineEncodingRequest[0] = (byte)(baudRate & 0xFF);

        //Get the 2nd byte of baudRate,
        //and put it in second byte of the array being sent
        lineEncodingRequest[1] = (byte)((baudRate >> 8) & 0xFF);

        //ibid, for 3rd byte (my guess, because you need at least 3 bytes
        //to encode your 115200+ settings)
        lineEncodingRequest[2] = (byte)((baudRate >> 16) & 0xFF);

        return lineEncodingRequest;

    }
    public int read(byte[] data) {
//        int inMax = usbEpIn.getMaxPacketSize();
//        ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);
//        UsbRequest usbRequest = new UsbRequest();
//        usbRequest.initialize(mDeviceConnection, usbEpIn);
//        usbRequest.queue(byteBuffer, inMax);
//        if(mDeviceConnection.requestWait() == usbRequest){
//            data = byteBuffer.array();
//            for(Byte byte1 : data){
//                System.err.println(byte1);
//            }
//        }
//        Log.e("write", "read" + usbEpOut.getMaxPacketSize());
        readFromUsb();
        if (mDeviceConnection != null &&  usbEpIn!= null)
            return mDeviceConnection.bulkTransfer( usbEpIn ,data, data.length, 3000);
        return -1;
    }

    public void write(byte[] data) {
        int i=0;
//        Log.e("write", "write" + usbEpOut .getMaxPacketSize());
        if (mDeviceConnection != null && usbEpOut != null)
            i=  mDeviceConnection.bulkTransfer(usbEpOut, data, data.length, 3000);
        Log.e("write2", "write:" + i);


    }

    private void readFromUsb() {

        //读取数据2

        int outMax = usbEpOut.getMaxPacketSize();

        int inMax = usbEpIn.getMaxPacketSize();

        ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);

        UsbRequest usbRequest = new UsbRequest();

        usbRequest.initialize(mDeviceConnection, usbEpIn);

        usbRequest.queue(byteBuffer, inMax);

        if (mDeviceConnection.requestWait() == usbRequest) {

            byte[] retData = byteBuffer.array();

            try {

                Log.e("收到数据：" , HexUtil.encodeHexStr( retData));

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

    }


    public void close() {
        if (mDeviceConnection != null && mInterface != null) {
            mDeviceConnection.releaseInterface(mInterface);
            mDeviceConnection.close();
            mDeviceConnection=null;
            mInterface=null;
        }
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                Log.e("BroadcastReceiver","BroadcastReceiver");
                if (granted) {
                    getConnection();
                } else {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if ( intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)&& mUsbDevice.equals(device) ){
                        getConnection();
                    }else {
                        mOnUSBInitListener.error(OnUSBInitListener.NO_PERMISSION);
                    }
                }

                context.unregisterReceiver(receiver);
            }
        }
    };

    public interface OnUSBInitListener {
        final int NOT_FOUND_DEVICE = -1;
        final int OPEN_DEVICE_FAILURE = -2;
        final int NOT_FOUND_USBINTERFACE = -3;
        final int NO_PERMISSION = -4;

        void success();

        void error(int code);
    }
}
