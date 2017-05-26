package com.sctjsj.ebanghost.application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.sctjsj.ebanghost.bean.UserBean;
import com.sctjsj.ebanghost.receiver.IsForegroundReceiver;
import com.sctjsj.ebanghost.receiver.MyNetworkReceiver;
import com.sctjsj.ebanghost.service.SessionFlushService;
import com.sctjsj.ebanghost.util.bt.BluetoothService;
import com.zxy.recovery.core.Recovery;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris-Jason on 2016/10/31.
 */
public class MyApp extends Application {
    private int socketState=-1;
    private onBTStateChangedListener stateChangedListener;
    public int getSocketState() {
        return socketState;
    }

    public void setSocketState(int socketState) {
        this.socketState = socketState;
        if(stateChangedListener!=null){
            stateChangedListener.stateChanged(socketState);
        }

    }

    private String TAG="MyApp";
    private SharedPreferences spf;
    private List<Activity> activityList=new ArrayList<>();
    private int activityCount;//activity的count数


    private ActivityLifecycleCallbacks lifecycleCallbacks=new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.e(TAG,activity.getClass().getName()+"：onActivityStarted");
            activityCount++;
            if(activityCount>0){
                sendMsg(1);
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.e(TAG,activity.getClass().getName()+":onActivityStopped");
            activityCount--;
            if(activityCount<0){
                activityCount=0;
            }
            if(activityCount==0){
                sendMsg(0);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };
    @Override
    public void onCreate() {
        super.onCreate();

        //初始化系统配置
        spf=getSharedPreferences("SYSTEM",MODE_PRIVATE);
        //默认打开振动提醒
        if(spf.getBoolean("vibrateOn",true)){
            spf.edit().putBoolean("vibrateOn",true);
        }
        //初始化 xUtils
        x.Ext.init(this);

//        Recovery.getInstance().debug(true)
//                .recoverInBackground(false)
//                .silent(false, Recovery.SilentMode.RESTART)
//                .init(this);

        //注册 app 生命周期监听
        registerActivityLifecycleCallbacks(lifecycleCallbacks);
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
