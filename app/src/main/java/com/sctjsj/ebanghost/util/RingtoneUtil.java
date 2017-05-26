package com.sctjsj.ebanghost.util;

import android.app.Activity;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import com.sctjsj.ebanghost.R;

/**
 * Created by mayikang on 16/12/16.
 */

public class RingtoneUtil {
    private Activity activity;
    private Vibrator vibrator;
    private Ringtone ringtone;

    public RingtoneUtil(Activity activity){
        this.activity=activity;
    }

    //蜂鸣声
    public void playRing(int type){
        Uri notification=null;
         switch (type){
             //新订单提交
             case 1:
                 notification = Uri.parse("android.resource://"+activity.getPackageName()+"/"+ R.raw.audio_new_order_come);

                 break;
             //新订单支付成功
             case 2:
                 notification = Uri.parse("android.resource://"+activity.getPackageName()+"/"+ R.raw.audio_new_order_payed);

                 break;
             //配送员已接单
             case 3:
                 notification = Uri.parse("android.resource://"+activity.getPackageName()+"/"+ R.raw.audio_delivery_accept_order);

                 break;
             //配送员已送达
             case 4:
                 notification = Uri.parse("android.resource://"+activity.getPackageName()+"/"+ R.raw.audio_delivery_confirm_send);

                 break;
             //买家已确认收货
             case 5:
                notification = Uri.parse("android.resource://"+activity.getPackageName()+"/"+ R.raw.audio_buyer_confirm_get);

                 break;
         }

        if(notification==null){
            return;
        }
         ringtone = RingtoneManager.getRingtone(activity.getApplicationContext(), notification);
         ringtone.play();
    }

    //关闭蜂鸣
    public void stopRing(){
        if(ringtone!=null&&ringtone.isPlaying()){
            ringtone.stop();
        }
    }

    //振动
    public void vibriate(){
        if(vibrator==null){
            vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
        }
        long [] pattern = {100,400,100,400};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern,2);
    }
    //停止振动
    public void stopVibrate(){
        if(vibrator!=null){
            vibrator.cancel();
        }
    }
}
