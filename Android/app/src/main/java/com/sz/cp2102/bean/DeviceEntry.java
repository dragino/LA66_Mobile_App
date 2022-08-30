package com.sz.cp2102.bean;

import android.hardware.usb.UsbDevice;

public      class DeviceEntry {
    public UsbDevice device;

    DeviceEntry(UsbDevice device ) {
        this.device = device;
    }
}
