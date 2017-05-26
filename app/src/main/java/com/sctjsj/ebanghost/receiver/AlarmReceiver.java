package com.sctjsj.ebanghost.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sctjsj.ebanghost.service.SessionFlushService;


/**
 * Created by Chris-Jason on 2016/9/12.
 */
public class AlarmReceiver extends BroadcastReceiver {
    //接收到广播后执行服务
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i=new Intent(context, SessionFlushService.class);
        context.startService(i);
    }
}
