package com.sctjsj.lazyhost.event.btEvent;

import android.bluetooth.BluetoothDevice;

public class ToConnectBTEvent {

    private int op;//1.连接 2.断开连接
    private BluetoothDevice device;

    public ToConnectBTEvent(int op, BluetoothDevice device) {
        this.op = op;
        this.device = device;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }
}
