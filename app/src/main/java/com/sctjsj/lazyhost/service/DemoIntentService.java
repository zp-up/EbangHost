package com.sctjsj.lazyhost.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


import com.google.gson.Gson;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTTransmitMessage;


import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;


public class DemoIntentService extends GTIntentService {

    public DemoIntentService() {

    }

    @Override
    public void onReceiveServicePid(Context context, int pid) {

    }

    @Override
    public void onReceiveMessageData(final Context context, GTTransmitMessage msg) {
        if (msg == null) {
            return;
        }

        if (!PushManager.getInstance().getClientid(this).equals(msg.getClientId())) {
            return;
        }

        byte[] b = msg.getPayload();

        if (b.length <= 0) {
            return;
        }

        final String data = new String(msg.getPayload());
        Log.e("intent-service","接收到了推送:"+data);



    }

    @Override
    public void onReceiveClientId(Context context, String clientid) {
        Log.e(TAG, "onReceiveClientId -> " + "clientid = " + clientid);
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
    }
}