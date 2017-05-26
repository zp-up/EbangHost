package com.sctjsj.ebanghost.util;

/**
 * Created by Chris-Jason on 2016/11/2.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.url.BnUrl;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * 负责用户的登入、登出
 */
public class AuthorityUtil {
    private String TAG="AuthorityUtil";
    private  Context context;
    private MyApp app;
    private ProgressUtil pUtil;
    public AuthorityUtil(Context context){
        this.context=context;
        app= (MyApp) context.getApplicationContext();
        pUtil=new ProgressUtil(context);
    }



    /**
     * 自动登录
     */
    public void autoLogin(){
        if(app.getCurrentUser()!=null){
            RequestParams params=new RequestParams(BnUrl.loginUrl);
            params.setUseCookie(true);
            params.addBodyParameter("userName",app.getCurrentUser().getAccount());
            params.addBodyParameter("password",app.getCurrentUser().getPwd());
            params.addBodyParameter("cid",app.getSpf().getString("cid","none_cid"));
            x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject response) {

                    if(response!=null){
                        try {
                            boolean result=response.getBoolean("result");
                            //登录成功
                            if(result){
                                String c= SessionUtil.getCookie();
                                if(c!=null){
                                    SharedPreferences.Editor edit=app.getSpf().edit();
                                    //保存 cookie
                                    edit.putString("cookie",c).commit();

                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {

                    //清除本地用户信息
                    app.saveCurrentUser(null);
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


    }

    /**
     * 登出
     */
    public void logOut(final AuthorityInt authorityInt){
        RequestParams params=new RequestParams(BnUrl.logoutUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    boolean result=response.getBoolean("result");
                    //清除本地信息
                    app.saveCurrentUser(null);
                    //回调接口
                    authorityInt.onSuccess();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                pUtil.dismissProgress();
            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                pUtil.showProgress(false);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });

    }

    /**
     * 暴露接口回调
     */
   public interface AuthorityInt{
        public void onSuccess();
        public void onError();
    }
}
