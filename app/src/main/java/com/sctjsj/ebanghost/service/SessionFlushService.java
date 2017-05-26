package com.sctjsj.ebanghost.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.constant.OtherConstant;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.receiver.AlarmReceiver;
import com.sctjsj.ebanghost.util.AuthorityUtil;
import com.sctjsj.ebanghost.util.SessionUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by Chris-Jason on 2016/10/28.
 */

/**
 * 刷新session定时任务
 */
public class SessionFlushService extends Service {
    private String TAG="SessionFlushService";
    private MyApp app;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"服务开启");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"服务销毁");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        app = (MyApp) getApplication();

        flushHttp();
        return super.onStartCommand(intent, flags, startId);
    }




    private void flushHttp(){
        RequestParams params=new RequestParams(BnUrl.isOnlineUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none_cookie"));
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    Log.e(TAG,"定时任务开始执行:"+response.toString());
                    try {
                        String status=response.getString("status");
                        //登录失败
                        if(!"success".equals(status)){
                            new AuthorityUtil(app.getApplicationContext()).autoLogin();
                        }else{
                            app.getSpf().edit().putString("cookie",SessionUtil.getCookie()).commit();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                send();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e(TAG,"定时任务："+ex.toString());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });
    }

    private void send(){
        AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
        //15分钟发送一次广播
        int tenMin=1*1000*60*10;
        long triggerAtTime= SystemClock.elapsedRealtime()+tenMin;
        Intent i=new Intent(SessionFlushService.this,AlarmReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(SessionFlushService.this, OtherConstant.TOKEN_FLUSH_GET_PENDING_INTENT,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
    }

}
