package com.sctjsj.lazyhost.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.igexin.sdk.PushConsts;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.activity.IndexActivity;
import com.sctjsj.lazyhost.application.MyApp;

/**
 * Created by mayikang on 16/12/1.
 */

public class MyPushReceiver extends BroadcastReceiver {
    private String TAG="BroadcastReceiver";
    private MyApp app;
    private static OnGetPushMessageListener onGetPushMessageListener;
    private Context context;
    /**
     * 接收到消息
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        this.context=context;
        app= (MyApp) context.getApplicationContext();
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            // TODO:处理cid返回
            case PushConsts.GET_CLIENTID:
                String cid = bundle.getString("clientid");
                Log.e("cid","cid="+cid);
                if(!TextUtils.isEmpty(cid)){
                    //保存 cid 到本地

//                    if(onGetPushMessageListener!=null){
//                        onGetPushMessageListener.onReceivedMessage(1,"cid:"+cid);
//                    }

                }

                break;
            // TODO:接收处理透传（payload）数据
            case PushConsts.GET_MSG_DATA:
                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");
                byte[] payload = bundle.getByteArray("payload");
                if (payload != null) {
                    String data = new String(payload);
                    if(!TextUtils.isEmpty(data)){
                        Log.e("消息",data);
                        if(onGetPushMessageListener!=null){
                            //回调消息
                            onGetPushMessageListener.onReceivedMessage(2,data);
                            sendNotification(data);
                        }

                    }

                }

                break;
            default:
                break;
        }
    }

    private void sendNotification(String msg) {
        //1.获取通知栏管理对象
        NotificationManager manager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder=new Notification.Builder(context);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_app_start));
        builder.setContentTitle("懒购商户");
        builder.setContentText(msg);
        builder.setSmallIcon(R.mipmap.icon_app_start).setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_VIBRATE|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true).setPriority(Notification.PRIORITY_MAX);

        Intent in=new Intent(context,IndexActivity.class);
        PendingIntent pi=PendingIntent.getActivity(context,1,in,0);
        builder.setContentIntent(pi);

        Notification notify=builder.build();
        manager.notify("tag-name",99,notify);

    }

    /**
     * 暴露回调接口给前台使用
     * @param onGetPushMessageListener
     */
    public static void setOnGetPushMessageListener(OnGetPushMessageListener onGetPushMessageListener) {
        MyPushReceiver.onGetPushMessageListener = onGetPushMessageListener;
    }

    //回调接口
    public interface OnGetPushMessageListener{
        //当接收到推送消息时
        public void onReceivedMessage(int type, String message);
    }


}
