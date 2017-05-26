package com.sctjsj.ebanghost.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sctjsj.ebanghost.activity.BTScanActivity;
import com.sctjsj.ebanghost.event.btScanEvent.BTStateChangeEvent;

import org.greenrobot.eventbus.EventBus;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BluetoothReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        //利用事件总线传递广播
        EventBus.getDefault().post(new BTStateChangeEvent(context,intent));

    }
}