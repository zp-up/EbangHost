package com.sctjsj.lazyhost.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.sctjsj.lazyhost.BuildConfig;
import com.sctjsj.lazyhost.bean.UserBean;
import com.sctjsj.lazyhost.service.SessionFlushService;
import com.tencent.bugly.Bugly;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_NONE;

/**
 * Created by Chris-Jason on 2016/10/31.
 */
public class MyApp extends Application {
    private String TAG = "MyApp";

    private int socketState = STATE_NONE;//默认的蓝牙状态
    //蓝牙监听器列表
    private List<onBTStateChangedListener> btStateListenerList;

    private SharedPreferences spf;
    private List<Activity> activityList = new ArrayList<>();
    private int activityCount;//activity的count数

    public int getSocketState() {
        return socketState;
    }


    private ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            activityCount++;
            if (activityCount > 0) {
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
            Log.e(TAG, activity.getClass().getName() + ":onActivityStopped");
            activityCount--;
            if (activityCount < 0) {
                activityCount = 0;
            }
            if (activityCount == 0) {
                sendMsg(0);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        //主进程中初始化
        if (isCurrentMainProcess()) {
            //初始化bugly
            Bugly.init(getApplicationContext(), "2f6163f433", false);
            //初始化系统配置
            spf = getSharedPreferences("SYSTEM", MODE_PRIVATE);
            //默认打开振动提醒
            if (spf.getBoolean("vibrateOn", true)) {
                spf.edit().putBoolean("vibrateOn", true);
            }
            //初始化 xUtils
            x.Ext.init(this);

            //注册 app 生命周期监听
            registerActivityLifecycleCallbacks(lifecycleCallbacks);

            //初始化极光推送，先获取一次cid，可能为空
            if (BuildConfig.DEBUG) {
                JPushInterface.setDebugMode(true);
            }
            JPushInterface.init(this);
            String registrationId = JPushInterface.getRegistrationID(this);
            if (!TextUtils.isEmpty(registrationId)) {
                this.getSpf().edit().putString("cid", registrationId).commit();
            }

            btStateListenerList = new ArrayList<>();
        }

    }

    //判断当前进程是否为主进程
    private boolean isCurrentMainProcess() {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            //获取运行的进程列表
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = manager.getRunningAppProcesses();

            if (runningAppProcessInfoList != null && runningAppProcessInfoList.size() > 0) {

                for (ActivityManager.RunningAppProcessInfo info : runningAppProcessInfoList) {
                    //当前进程 id = 运行进程 id，默认主进程名等于包名
                    if (info.pid == android.os.Process.myPid()) {
                        if (TextUtils.equals(info.processName, getPackageName())) {
                            return true;
                        }
                    }
                }

            }
        }

        return false;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    //程序处于前台时，开启定时任务，处于后台，结束定时任务
    private void sendMsg(int op) {
        switch (op) {
            case 0:
                Log.e(TAG, "程序处于后台啦");
                Intent intent1 = new Intent(this, SessionFlushService.class);
                this.stopService(intent1);
                break;
            case 1:
                Log.e(TAG, "程序处于前台啦");
                if (getCurrentUser() != null) {
                    Intent intent2 = new Intent(this, SessionFlushService.class);
                    startService(intent2);
                }
                break;
        }

    }

    /**
     * 保存当前登录的用户信息到本地
     *
     * @param ub
     */
    public void saveCurrentUser(UserBean ub) {
        String userJson = new Gson().toJson(ub);
        spf.edit().putString("user", userJson).commit();
    }

    /**
     * 获取当前登录的用户
     *
     * @return
     */
    public UserBean getCurrentUser() {
        UserBean ub = new UserBean();
        String userJson = spf.getString("user", "none");
        //取出保存的用户信息
        if (userJson != null && !"null".equals(userJson) && !"none".equals(userJson)) {
            try {
                JSONObject jsonObject = new JSONObject(userJson);
                ub.setUserId(jsonObject.getInt("userId"));
                ub.setAccount(jsonObject.getString("account"));
                ub.setPwd(jsonObject.getString("pwd"));
                ub.setShopId(jsonObject.getInt("shopId"));
                ub.setShopType(jsonObject.getInt("shopType"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ub;
        } else {
            return null;
        }

    }

    /**
     * 获取 SPF
     *
     * @return
     */
    public SharedPreferences getSpf() {
        spf = getSharedPreferences("SYSTEM", MODE_PRIVATE);
        return spf;
    }

    /**
     * 添加
     *
     * @param activity
     */
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    /**
     * 移除
     *
     * @param activity
     */
    public void removeActivity(Activity activity) {
        if (activityList.contains(activity)) {
            activityList.remove(activity);
            activity.finish();
        }
    }

    /**
     * 结束所有 Activity
     */
    public void finishAllActivity() {
        for (Activity ac : activityList) {
            if (!ac.isFinishing()) {
                ac.finish();
            }
        }
        saveCurrentUser(null);
        //杀死该应用进程
        android.os.Process.killProcess(android.os.Process.myPid());

    }


    public interface onBTStateChangedListener {
        void stateChanged(int currentState);
    }

    //设置蓝牙状态改变监听器
    public void setOnBTStateChangedListener(onBTStateChangedListener listener) {
        if (listener == null) {
            return;
        }
        if (btStateListenerList != null && !btStateListenerList.contains(listener)) {
            btStateListenerList.add(listener);
        }
    }

    //回调蓝牙连接状态改变
    public synchronized void setSocketState(int socketState) {
        this.socketState = socketState;

        //当前设备没有蓝牙模块，或者没有开启蓝牙 不用回调
        if (BluetoothAdapter.getDefaultAdapter() == null || !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            return;
        }

        if (btStateListenerList != null && btStateListenerList.size() > 0) {
            for (onBTStateChangedListener listener : btStateListenerList) {
                listener.stateChanged(this.socketState);
            }
        }

    }

}
