package com.sctjsj.lazyhost.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.igexin.sdk.PushManager;
import com.sctjsj.lazyhost.activity.IndexActivity;
import com.sctjsj.lazyhost.bean.UserBean;
import com.sctjsj.lazyhost.receiver.IsForegroundReceiver;
import com.sctjsj.lazyhost.receiver.MyNetworkReceiver;
import com.sctjsj.lazyhost.service.DemoIntentService;
import com.sctjsj.lazyhost.service.DemoPushService;
import com.sctjsj.lazyhost.service.SessionFlushService;
import com.sctjsj.lazyhost.util.bt.BluetoothService;
import com.tencent.bugly.Bugly;
import com.xiaomi.mipush.sdk.HWPushHelper;
import com.zxy.recovery.core.Recovery;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.thirdpush.huawei.HWPushManager;
import cn.pedant.SweetAlert.SweetAlertDialog;
import dalvik.system.DexClassLoader;

import static com.sctjsj.lazyhost.constant.OtherConstant.DEVICE_NAME;
import static com.sctjsj.lazyhost.constant.OtherConstant.MESSAGE_DEVICE_NAME;
import static com.sctjsj.lazyhost.constant.OtherConstant.MESSAGE_READ;
import static com.sctjsj.lazyhost.constant.OtherConstant.MESSAGE_STATE_CHANGE;
import static com.sctjsj.lazyhost.constant.OtherConstant.MESSAGE_WRITE;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_CONNECTED;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_CONNECTING;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_LISTEN;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_NONE;

/**
 * Created by Chris-Jason on 2016/10/31.
 */
public class MyApp extends Application {
    private int socketState=-1;
    private onBTStateChangedListener stateChangedListener;
    private String TAG="MyApp";
    private SharedPreferences spf;
    private List<Activity> activityList=new ArrayList<>();
    private int activityCount;//activity的count数
    private BluetoothService bluetoothService;

    public int getSocketState() {
        return socketState;
    }

    public void setSocketState(int socketState) {
        this.socketState = socketState;
        if(stateChangedListener!=null){
            stateChangedListener.stateChanged(socketState);
        }
    }

    private ActivityLifecycleCallbacks lifecycleCallbacks=new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.e(TAG,activity.getClass().getName()+"：onActivityStarted");
            activityCount++;
            if(activityCount>0){
                sendMsg(1);
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.e(TAG,activity.getClass().getName()+":onActivityStopped");
            activityCount--;
            if(activityCount<0){
                activityCount=0;
            }
            if(activityCount==0){
                sendMsg(0);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Bugly.init(getApplicationContext(),"2f6163f433",false);
        //初始化系统配置
        spf=getSharedPreferences("SYSTEM",MODE_PRIVATE);
        //默认打开振动提醒
        if(spf.getBoolean("vibrateOn",true)){
            spf.edit().putBoolean("vibrateOn",true);
        }
        //初始化 xUtils
        x.Ext.init(this);
        PushManager.getInstance().initialize(this.getApplicationContext(), DemoPushService.class);
        PushManager.getInstance().initialize(this.getApplicationContext(), DemoIntentService.class);
//        Recovery.getInstance().debug(true)
//                .recoverInBackground(false)
//                .silent(false, Recovery.SilentMode.RESTART)
//                .init(this);

        //注册 app 生命周期监听
        registerActivityLifecycleCallbacks(lifecycleCallbacks);
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        String registrationId = JPushInterface.getRegistrationID(this);
        Log.e("TAG","registrationId:"+registrationId);
        this.getSpf().edit().putString("cid",registrationId).commit();

        //创建蓝牙服务
        //bluetoothService = new BluetoothService(this,blueHandler);
    }

    public BluetoothService getBluetoothService() {
        return bluetoothService;
    }



//    private Handler blueHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MESSAGE_STATE_CHANGE:
//
//                    switch (msg.arg1) {
//                        //连接成功
//                        case STATE_CONNECTED:
//                            //关闭连接进度
//                            if (connectProgress != null) {
//                                connectProgress.dismiss();
//                            }
//                            Toast.makeText(MyApp.this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
//
//                            break;
//                        //正在连接
//                        case STATE_CONNECTING:
//                            if (connectProgress == null) {
//                                connectProgress = new SweetAlertDialog(IndexActivity.this, SweetAlertDialog.PROGRESS_TYPE);
//                            }
//                            connectProgress.setTitleText("");
//                            connectProgress.setCancelable(false);
//                            connectProgress.setCancelText("取消连接");
//                            connectProgress.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                @Override
//                                public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                    connectProgress.dismiss();
//                                    bluetoothService.stop();
//                                }
//                            });
//                            connectProgress.setContentText("正在连接设备");
//                            connectProgress.show();
//                            break;
//
//                        //连接失败
//                        case STATE_LISTEN:
//                            if (connectProgress != null) {
//                                connectProgress.dismiss();
//                            }
//                            break;
//                        case STATE_NONE:
//                            Toast.makeText(IndexActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
//                            if (connectProgress != null) {
//                                connectProgress.dismiss();
//                            }
//
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    playNoConnection();
//                                }
//                            }, 1000);
//
//                            break;
//                    }
//                    break;
//
//                case MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    String writeMessage = new String(writeBuf);
//                    break;
//
//                case MESSAGE_READ:
//                    byte[] readBuf = (byte[]) msg.obj;
//                    // construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    break;
//                case MESSAGE_DEVICE_NAME:
//                    // 获取连接设备的名字
//                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                    Toast.makeText(IndexActivity.this, "连接至" + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                    break;
//            }
//        }
//
//
//    };


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    //程序处于前台时，开启定时任务，处于后台，结束定时任务
    private void sendMsg(int op){
        switch (op){
            case 0:
                Log.e(TAG,"程序处于后台啦");
                Intent intent1=new Intent(this,SessionFlushService.class);
                this.stopService(intent1);
                break;
            case 1:
                Log.e(TAG,"程序处于前台啦");
                if(getCurrentUser()!=null){
                    Intent intent2=new Intent(this,SessionFlushService.class);
                    startService(intent2);
                }
                break;
        }

    }
    /**
     *保存当前登录的用户信息到本地
     * @param ub
     */
    public void saveCurrentUser(UserBean ub){
        String userJson=new Gson().toJson(ub);
        spf.edit().putString("user",userJson).commit();
    }

    /**
     * 获取当前登录的用户
     * @return
     */
    public UserBean getCurrentUser(){
        UserBean ub=new UserBean();
        String userJson=spf.getString("user","none");
        //取出保存的用户信息
        if(userJson!=null && !"null".equals(userJson)&& !"none".equals(userJson)){
            try {
                JSONObject jsonObject=new JSONObject(userJson);
                ub.setUserId(jsonObject.getInt("userId"));
                ub.setAccount(jsonObject.getString("account"));
                ub.setPwd(jsonObject.getString("pwd"));
                ub.setShopId(jsonObject.getInt("shopId"));
                ub.setShopType(jsonObject.getInt("shopType"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ub;
        }else{
            return null;
        }

    }

    /**
     *获取 SPF
     * @return
     */
    public SharedPreferences getSpf() {
        spf=getSharedPreferences("SYSTEM",MODE_PRIVATE);
        return spf;
    }

    /**
     * 添加
     * @param activity
     */
    public  void addActivity(Activity activity){
        activityList.add(activity);
    }

    /**
     * 移除
     * @param activity
     */
    public  void removeActivity(Activity activity){
        if(activityList.contains(activity)){
            activityList.remove(activity);
            activity.finish();
        }
    }

    /**
     * 结束所有 Activity
     */
    public  void finishAllActivity(){
        for(Activity ac:activityList){
            if(!ac.isFinishing()){
                ac.finish();
            }
        }
        saveCurrentUser(null);
        //杀死该应用进程
        android.os.Process.killProcess(android.os.Process.myPid());

    }


    public interface onBTStateChangedListener{
       void stateChanged(int currentState);
    }

    public void setOnBTStateChangedListener(onBTStateChangedListener listener){
        this.stateChangedListener=listener;
    }
}
