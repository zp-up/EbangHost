package com.sctjsj.lazyhost.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sctjsj.lazyhost.service.SessionFlushService;


/**
 * Created by mayikang on 16/12/5.
 */

public class IsForegroundReceiver extends BroadcastReceiver {
    private String TAG="IsForegroundReceiver";
    private SessionFlushService flushService;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG,"receiver-broadcast：程序已经处于后台，请解绑定时 Service");
        Intent intent1=new Intent(context, SessionFlushService.class);
        context.stopService(intent1);
    }
}
