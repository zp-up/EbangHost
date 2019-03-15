package com.sctjsj.lazyhost.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.igexin.sdk.PushManager;
import com.jaeger.library.StatusBarUtil;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.bean.UserBean;
import com.sctjsj.lazyhost.receiver.MyPushReceiver;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.LogUtil;
import com.sctjsj.lazyhost.util.ProgressUtil;
import com.sctjsj.lazyhost.util.RingtoneUtil;
import com.sctjsj.lazyhost.util.SessionUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.android.api.JPluginPlatformInterface;

/**
 * Created by Chris-Jason on 2016/11/9.
 */
public class LoginActivity extends AppCompatActivity {
    private ProgressUtil progressUtil;
    private MyApp app;
    @Bind(R.id.activity_login_ll_parent)
    LinearLayout mLLParent;//父容器
    @Bind(R.id.activity_login_et_account)
    EditText mETAccount;//账户
    @Bind(R.id.activity_login_et_pwd)
    EditText mETPwd;//密码

    private String account;
    private String pwd;
    private int shopId;
    private int shopType;
    private int userId;
    private JPluginPlatformInterface pHuaweiPushInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pHuaweiPushInterface = new JPluginPlatformInterface(this.getApplicationContext());
        int flag = getIntent().getFlags();
        //首次启动 Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT 为 0
        //再次点击图标启动时就不为零了
        app = (MyApp) getApplication();
        Intent intent = getIntent();
        try {
            String tag = intent.getStringExtra("TAG");
            Log.e("TAG", "接受到的消息:" + tag);
            try {
                if (app.getSpf().getBoolean("vibrateOn", true)) {
                    final RingtoneUtil ringtoneUtil = new RingtoneUtil(this);

                    if (tag != null && tag.contains("1")) {
                        ringtoneUtil.vibriate();
                        ringtoneUtil.playRing(1);
                        Log.e("ring", "1");
                    }

                    if (tag != null && tag.contains("5")) {
                        ringtoneUtil.vibriate();
                        ringtoneUtil.playRing(5);
                        Log.e("ring", "5");
                    }

                    if (tag != null && tag.contains("3")) {
                        ringtoneUtil.vibriate();
                        ringtoneUtil.playRing(3);
                        Log.e("ring", "3");
                    }

                    if (tag != null && tag.contains("4")) {
                        ringtoneUtil.vibriate();
                        ringtoneUtil.playRing(4);
                        Log.e("ring", "4");
                    }

                    if (tag != null && tag.contains("2！")) {
                        ringtoneUtil.vibriate();
                        ringtoneUtil.playRing(2);
                        Log.e("ring", "2");
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ringtoneUtil.stopVibrate();
                            ringtoneUtil.stopRing();
                        }
                    }, 10000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("getFlags---" + flag + "------" + (flag & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, null);
        ButterKnife.bind(this);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);//自定义的code
        }
        //初始化个推
        //PushManager.getInstance().initialize(this.getApplicationContext());
        /**
         * 接收到 CID 消息回调
         */
        MyPushReceiver.setOnGetPushMessageListener(new MyPushReceiver.OnGetPushMessageListener() {
            @Override
            public void onReceivedMessage(int type, String message) {
                if (1 == type) {
                    LogUtil.e("消息：" + message);
                }
            }
        });

        progressUtil = new ProgressUtil(this);
        if (app.getCurrentUser() != null) {
            mETAccount.setText(app.getCurrentUser().getAccount());
            mETPwd.setText(app.getCurrentUser().getPwd());
            oneKeyLogin();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        pHuaweiPushInterface.onStart(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        pHuaweiPushInterface.onStop(this);
    }

    @OnClick({R.id.activity_login_btn_submit, R.id.activity_login_tv_forget_pwd})
    public void loginClick(View view) {
        switch (view.getId()) {
            //登录
            case R.id.activity_login_btn_submit:

                if (checkBeforeLogin()) {
                    submit();
                }
                break;
            //忘记密码
            case R.id.activity_login_tv_forget_pwd:
                Intent intent = new Intent(LoginActivity.this, GetPwdBackActivity.class);
                startActivity(intent);
                break;
        }
    }

    private boolean checkBeforeLogin() {

        if (PushManager.getInstance().isPushTurnedOn(this)) {

            String cid = PushManager.getInstance().getClientid(this);
            if(TextUtils.isEmpty(cid)){
                Log.e("login","CID获取失败");
                return false;
            }

            app.getSpf().edit().putString("cid",cid).commit();
        }

        if (TextUtils.isEmpty(mETAccount.getText().toString())) {
            Snackbar.make(mLLParent, "账户名为空", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mETPwd.getText().toString())) {
            Snackbar.make(mLLParent, "登录密码", Snackbar.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }

    /**
     * 普通登录请求
     */
    public void submit() {
        if ("none-cookie".equals(app.getSpf().getString("cid", "none-cookie"))) {
            AlertDialog dialog = new AlertDialog.Builder(getApplicationContext())
                    .setTitle("温馨提示")
                    .setMessage("程序初始化错误，是否结束程序？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            try {
                                System.exit(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            dialog.show();
            return;
        }
        RequestParams params = new RequestParams(BnUrl.loginUrl);
        params.setUseCookie(true);
        params.addBodyParameter("userName", mETAccount.getText().toString());
        params.addBodyParameter("password", mETPwd.getText().toString());
        params.addBodyParameter("cid", app.getSpf().getString("cid", "none-cookie"));

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if (response != null) {

                    try {
                        boolean result = response.getBoolean("result");

                        String resultMsg = response.getString("resultMsg");
                        String resultData = response.getString("resultData");
                        //登录成功
                        if (result) {
                            String c = SessionUtil.getCookie();

                            if (!TextUtils.isEmpty(c)) {
                                app.getSpf().edit().putString("cookie", c).commit();
                            }
                            account = mETAccount.getText().toString();
                            pwd = mETPwd.getText().toString();
                            if (!TextUtils.isEmpty(resultData)) {
                                userId = Integer.valueOf(resultData);
                                pullUserInfo(String.valueOf(userId));
                            }

                        } else {
                            Snackbar.make(mLLParent, resultMsg, Snackbar.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e(ex.toString());
                progressUtil.dismissProgress();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                progressUtil.dismissProgress();
            }

            @Override
            public void onFinished() {
                progressUtil.dismissProgress();
            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                progressUtil.showProgress(false);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });


    }


    /**
     * 一键登录
     */
    private void oneKeyLogin() {

        if (app.getCurrentUser() == null) {
            return;
        }

        if (TextUtils.isEmpty(app.getCurrentUser().getAccount())) {
            return;
        }

        if (TextUtils.isEmpty(app.getCurrentUser().getPwd())) {
            return;
        }

        if (TextUtils.isEmpty(String.valueOf(app.getCurrentUser().getShopId()))) {
            return;
        }

        if (TextUtils.isEmpty(String.valueOf(app.getCurrentUser().getShopType()))) {
            return;
        }

        if (TextUtils.isEmpty(app.getSpf().getString("cid", "none-cid"))) {
            AlertDialog dialog = new AlertDialog.Builder(getApplicationContext())
                    .setTitle("温馨提示")
                    .setMessage("程序初始化错误，是否结束程序？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            try {
                                System.exit(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            dialog.show();
            return;
        }

        RequestParams params = new RequestParams(BnUrl.loginUrl);
        params.setUseCookie(true);
        final String s1 = app.getCurrentUser().getAccount().toString();
        final String s2 = app.getCurrentUser().getPwd().toString();
        params.addBodyParameter("userName", s1);
        params.addBodyParameter("password", s2);
        params.addBodyParameter("cid", app.getSpf().getString("cid", "none_cid"));
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                if (response != null) {
                    try {
                        boolean result = response.getBoolean("result");
                        int resultData = response.getInt("resultData");//id
                        String resultMsg = response.getString("resultMsg");
                        //登录成功
                        if (result) {
                            String c = SessionUtil.getCookie();
                            if (c != null) {
                                //保存 cookie
                                app.getSpf().edit().putString("cookie", c).commit();
                            }
                            account = s1;
                            pwd = s2;
                            userId = resultData;
                            pullUserInfo(String.valueOf(userId));

                        } else {
                            LogUtil.e("自动登录失败：" + resultMsg);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                progressUtil.dismissProgress();
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
                LogUtil.e("开始自动登录");
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });

    }


    /**
     * 根据用户 id 查询个人信息
     *
     * @param id
     */
    public void pullUserInfo(String id) {
        RequestParams params = new RequestParams(BnUrl.pullUserInfo);
        params.setUseCookie(false);
        params.addHeader("Cookie", app.getSpf().getString("cookie", "none-cookie"));
        params.addBodyParameter("ctype", "user");
        params.addBodyParameter("jf", "store");
        params.addBodyParameter("cond", "{id:" + id + "}");
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject response) {

                if (response != null) {
                    try {
                        JSONArray resultList = response.getJSONArray("resultList");
                        if (resultList.length() > 0) {
                            shopId = resultList.getJSONObject(0).getJSONObject("store").getInt("id");
                            shopType = resultList.getJSONObject(0).getJSONObject("store").getInt("type");
                            UserBean ub = new UserBean();
                            ub.setUserId(userId);
                            ub.setAccount(account);
                            ub.setPwd(pwd);
                            ub.setShopId(shopId);
                            ub.setShopType(shopType);
                            app.saveCurrentUser(ub);
                            progressUtil.dismissProgress();
                            /**
                             * 跳转到主页
                             */
                            Intent intent = new Intent(LoginActivity.this, IndexActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e(ex.toString());
                progressUtil.dismissProgress();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                progressUtil.dismissProgress();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//JPush 中调用 HMS SDK 解决错误的接口传入的 requestCode 为 10001,开发者调用是请注意不要同样使 用 10001
        if (requestCode == JPluginPlatformInterface.JPLUGIN_REQUEST_CODE) {
            pHuaweiPushInterface.onActivityResult(this, requestCode, resultCode, data);
        }
    }

}
