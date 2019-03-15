package com.sctjsj.lazyhost.receiver;

/**
 * Created by Chris-Jason on 2016/8/31.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.widget.Toast;

import com.sctjsj.lazyhost.util.LogUtil;

/**
 * 监听网络变化的广播接收器
 */
public class MyNetworkReceiver extends BroadcastReceiver {
    private int alertCount=0;
    private static onNetStateChangeListener netListener;
    /**
     * 接收到广播后处理逻辑
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean success=false;//网络连接状态，true表示已连接，false表示未连接
        //获得网络连接服务
        ConnectivityManager connManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //获取WIFI连接状态
        NetworkInfo.State state=connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        //判断是否正在使用wifi
        if(NetworkInfo.State.CONNECTED==state){
            //正在使用wifi
            success=true;
        }

        //获取GPRS连接状态
        state=connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        //判断是否正在使用gprs网络
        if(NetworkInfo.State.CONNECTED==state){
            success=true;
        }

        //无网络连接
        if(!success){
            //判断toast提示次数
            if(alertCount<=1){
                if(netListener!=null){
                    netListener.onNetNotAvailable();
                }
                alertCount++;
            }
        }else {
           // Toast.makeText(context, "网络已连接", Toast.LENGTH_LONG).show();
        }


    }


    public static void setNetStateChangeListener(onNetStateChangeListener listener){
        MyNetworkReceiver.netListener=listener;
    }


    public interface onNetStateChangeListener{
        public void onNetAvailable();
        public void onNetNotAvailable();
    }

}

