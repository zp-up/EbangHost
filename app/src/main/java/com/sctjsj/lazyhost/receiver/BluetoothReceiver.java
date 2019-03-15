package com.sctjsj.lazyhost.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sctjsj.lazyhost.activity.BTScanActivity;
import com.sctjsj.lazyhost.event.btScanEvent.BTStateChangeEvent;
import com.sctjsj.lazyhost.util.LogUtil;

import org.greenrobot.eventbus.EventBus;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BluetoothReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.e("接收到蓝牙广播");
        //利用事件总线传递广播
        EventBus.getDefault().post(new BTStateChangeEvent(context,intent));
    }
}