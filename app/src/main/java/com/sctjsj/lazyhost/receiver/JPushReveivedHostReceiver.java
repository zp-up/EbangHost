package com.sctjsj.lazyhost.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;


import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.activity.IndexActivity;
import com.sctjsj.lazyhost.activity.LoginActivity;

import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by wuqaing on 2018/11/26.
 */

public class JPushReveivedHostReceiver extends BroadcastReceiver {
    String TAG = "TAG";
    private NotificationManager nm;
    private static final int NOTIFICATION_SHOW_SHOW_AT_MOST = 2;
    final String CHANNEL_ID = "channel_id_2";
    final String CHANNEL_NAME = "channel_name_2";

    public static void setOnGetPushMessageListener(MyPushReceiver.OnGetPushMessageListener onGetPushMessageListener) {
        JPushReveivedHostReceiver.onGetPushMessageListener = onGetPushMessageListener;
    }

    private static MyPushReceiver.OnGetPushMessageListener onGetPushMessageListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == nm) {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        Bundle bundle = intent.getExtras();
        Log.e(TAG, "onReceive - " + intent.getAction() + ", extras: " + bundle.toString());

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            Log.e(TAG, "JPush用户注册成功");

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.e(TAG, "接受到推送下来的自定义消息:" + bundle.getString(JPushInterface.EXTRA_MESSAGE));


        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.e(TAG, "接受到推送下来的通知");

            receivingNotification(context, bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.e(TAG, "用户点击打开了通知");

            openNotification(context, bundle);

        } else {
            Log.e(TAG, "Unhandled intent - " + intent.getAction());
        }

    }

    private void receivingNotification(Context context, Bundle bundle) {
        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
        Log.e(TAG, " title : " + title);
        String message = bundle.getString(JPushInterface.EXTRA_ALERT);
        Log.e(TAG, "message : " + message);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        Log.e(TAG, "extras : " + extras);
        message = extras;
        try {
            if (onGetPushMessageListener != null) {
                onGetPushMessageListener.onReceivedMessage(2, message);
                if (message != null && message.contains("您有新订单")) {
                    //processCustomMessage(context,bundle,1);
                }
                if (message != null && message.contains("买家已确认收货")) {
                    //processCustomMessage(context,bundle,5);
                }
                if (message != null && message.contains("配送员已接单")) {
                    //processCustomMessage(context,bundle,3);
                }
                if (message != null && message.contains("配送员已送达")) {
                    //processCustomMessage(context,bundle,4);
                }
                if (message != null && message.contains("买家支付成功")) {
                    //processCustomMessage(context,bundle,2);
                }
            } else {
                Intent start = context.getPackageManager().getLaunchIntentForPackage("com.sctjsj.lazyhost");
                start.addCategory(Intent.CATEGORY_LAUNCHER);
                start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                if (message != null && message.contains("您有新订单")) {
                    start.putExtra("TAG", "1");
                    //processCustomMessage(context,bundle,1);
                    Log.e("ring", "1");
                }

                if (message != null && message.contains("买家已确认收货")) {
                    start.putExtra("TAG", "5");
                    //processCustomMessage(context,bundle,5);
                    Log.e("ring", "5");
                }

                if (message != null && message.contains("配送员已接单")) {
                    start.putExtra("TAG", "3");
                    //processCustomMessage(context,bundle,3);
                    Log.e("ring", "3");
                }

                if (message != null && message.contains("配送员已送达")) {
                    start.putExtra("TAG", "4");
                    //processCustomMessage(context,bundle,4);
                    Log.e("ring", "4");
                }

                if (message != null && message.contains("买家支付成功")) {
                    start.putExtra("TAG", "2");
                    //processCustomMessage(context,bundle,2);
                    Log.e("ring", "2");
                }
                context.startActivity(start);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void openNotification(Context context, Bundle bundle) {
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        String myValue = "";
        try {
            JSONObject extrasJson = new JSONObject(extras);
            myValue = extrasJson.optString("myKey");
        } catch (Exception e) {
            Log.e(TAG, "Unexpected: extras is not a valid json", e);
            return;
        }
        if (bundle != null && bundle.getString(JPushInterface.EXTRA_ALERT) != null && bundle.getString(JPushInterface.EXTRA_ALERT).contains("您的账号在其他地方登陆")){
            Intent mIntent = new Intent(context, LoginActivity.class);
            mIntent.putExtras(bundle);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mIntent);
        }else {
            Intent mIntent = new Intent(context, IndexActivity.class);
            mIntent.putExtras(bundle);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mIntent);
        }
    }
    /**
     * 实现自定义推送声音
     * @param context
     * @param bundle
     */
    private void processCustomMessage(Context context, Bundle bundle,int type) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);

        String title = bundle.getString(JPushInterface.EXTRA_TITLE);
        String msg = bundle.getString(JPushInterface.EXTRA_ALERT);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_app_start);

        Intent mIntent = new Intent(context,IndexActivity.class);
        mIntent.putExtras(bundle);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mIntent, 0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //只在Android O之上需要渠道
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            //如果这里用IMPORTANCE_NOENE就需要在系统的设置里面开启渠道，
            //通知才能正常弹出
            nm.createNotificationChannel(notificationChannel);
        }
        notification.setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentText(TextUtils.isEmpty(msg)?"新消息":msg)
                .setContentTitle("懒购专送")
                .setSmallIcon(R.mipmap.icon_app_start)
                .setLargeIcon(bitmap)
                .setNumber(NOTIFICATION_SHOW_SHOW_AT_MOST);
        try {
            Uri notificationSound=null;
            switch (type){
                //新订单提交
                case 1:
                    notificationSound = Uri.parse("android.resource://com.sctjsj.lazyhost/"+ R.raw.v_new_order_arrive);
                    break;
                //新订单支付成功
                case 2:
                    notificationSound = Uri.parse("android.resource://com.sctjsj.lazyhost/"+ R.raw.v_new_order_pay_success);
                    break;
                //配送员已接单
                case 3:
                    notificationSound = Uri.parse("android.resource://com.sctjsj.lazyhost/"+ R.raw.v_deliver_accept_order);
                    break;
                //配送员已送达
                case 4:
                    notificationSound = Uri.parse("android.resource://com.sctjsj.lazyhost/"+ R.raw.v_deliver_send_arrive);
                    break;
                //买家已确认收货
                case 5:
                    notificationSound = Uri.parse("android.resource://com.sctjsj.lazyhost/"+ R.raw.v_buyer_confirm_receive);
                    break;
                default:
                    Log.e("ring","null");
                    break;
            }

            Log.e("ring","null");
            if(notification==null){
                return;
            }
            notification.setSound(notificationSound);
        }catch (Exception e){
            e.printStackTrace();
        }
        nm.notify(NOTIFICATION_SHOW_SHOW_AT_MOST, notification.build());  //id随意，正好使用定义的常量做id，0除外，0为默认的Notification
    }

}
