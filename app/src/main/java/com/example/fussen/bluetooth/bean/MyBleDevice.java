package com.example.fussen.bluetooth.bean;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

/**
 * Created by shihu.wang on 2016/4/14.
 * Email shihu.wang@bodyplus.cc
 */
public class MyBleDevice implements Serializable {

    private BluetoothDevice device; // 蓝牙设备对象

    private int rssi = -100; // 设备信号强度

    private String deviceSn; // 设备SN号

    public String macAddress; // 设备MAC地址
    public String mName; // 设备MAC地址


}