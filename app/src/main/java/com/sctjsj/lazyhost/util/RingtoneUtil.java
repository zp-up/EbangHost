package com.sctjsj.lazyhost.util;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

import com.sctjsj.lazyhost.R;

import java.io.IOException;


/**
 * Created by mayikang on 16/12/16.
 */

public class RingtoneUtil {
    private Activity activity;
    private Vibrator vibrator;
    private Ringtone ringtone;
    private MediaPlayer player;

    public RingtoneUtil(Activity activity){
        this.activity=activity;
        if(player==null){
            player=new MediaPlayer();
        }
    }


    public void playRing(int type){
        Uri notification=null;
         switch (type){
             //新订单提交
             case 1:
                 //您有新的订单请处理
                 notification = Uri.parse("android.resource://"+activity.getApplicationContext().getPackageName()+"/"+ R.raw.v_new_order_arrive);
                 break;
             //新订单支付成功
             case 2:
                 notification = Uri.parse("android.resource://"+activity.getApplicationContext().getPackageName()+"/"+ R.raw.v_new_order_pay_success);
                 break;
             //配送员已接单
             case 3:
                 notification = Uri.parse("android.resource://"+activity.getApplicationContext().getPackageName()+"/"+ R.raw.v_deliver_accept_order);
                 break;
             //配送员已送达
             case 4:
                 notification = Uri.parse("android.resource://"+activity.getApplicationContext().getPackageName()+"/"+ R.raw.v_deliver_send_arrive);
                 break;
             //买家已确认收货
             case 5:
                notification = Uri.parse("android.resource://"+activity.getApplicationContext().getPackageName()+"/"+ R.raw.v_buyer_confirm_receive);
                 break;

             default:
                 Log.e("ring","null");
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
        //判断是否有振动器
        if(vibrator!=null && vibrator.hasVibrator()){
            vibrator.vibrate(1000);
        }
    }
    //停止振动
    public void stopVibrate(){
        if(vibrator!=null){
            vibrator.cancel();
        }
    }
}
