package com.sctjsj.ebanghost.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.ProgressUtil;
import com.sctjsj.ebanghost.util.RegexpValidateUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResetPwdActivity extends Activity {
    private ProgressUtil pUtil;
    private String TAG="ResetPwdActivity";
    private MyApp app;
    private String phoneNum;
    private String cookie;
    private String sms;

    @Bind(R.id.activity_reset_pwd_tv_remind)TextView mTVRemind;//提示
    @Bind(R.id.activity_reset_pwd_et_new_pwd)EditText mETNew;//新密码
    @Bind(R.id.activity_reset_pwd_et_confirm_new_pwd)EditText mETConfirm;//确认新密码
    @Bind(R.id.activity_reset_pwd_ll_parent)LinearLayout mLLParent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd);
        ButterKnife.bind(this);
        app= (MyApp) getApplication();
        app.addActivity(this);
        pUtil=new ProgressUtil(this);
        phoneNum=getIntent().getStringExtra("tel");
        cookie=getIntent().getStringExtra("cookie");
        sms=getIntent().getStringExtra("sms");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        app.removeActivity(this);
    }

    @OnClick({R.id.activity_reset_pwd_ll_back,R.id.reset_pwd_activity_btn_submit})
    public void resetPwdClick(View view){
        switch (view.getId()){
            //返回
            case R.id.activity_reset_pwd_ll_back:
                finish();
                break;

            //重设密码
            case R.id.reset_pwd_activity_btn_submit:
                if(beforeReset()){
                    submit();
                }
                break;

        }
    }

    /**
     * 提交之前检查
     * @return
     */
    public boolean beforeReset(){

        if(!RegexpValidateUtil.checkPwd(mETNew.getText().toString())){
            Snackbar.make(mLLParent,"密码要求8位以上", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if(!mETNew.getText().toString().equals(mETConfirm.getText().toString())){
            Snackbar.make(mLLParent,"两次输入的密码不一致", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

     public void submit(){
         RequestParams params=new RequestParams(BnUrl.resetPwdUrl);
         params.setUseCookie(false);
         params.addHeader("Cookie",cookie);
         params.addBodyParameter("mobile",phoneNum);
         params.addBodyParameter("code",sms);
         params.addBodyParameter("password",mETNew.getText().toString());
         x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
             @Override
             public void onSuccess(JSONObject response) {
                 try {
                     boolean result=response.getBoolean("result");
                     String msg=response.getString("msg");
                     //重构成功
                     if(result){
                          Snackbar.make(mLLParent,msg, Snackbar.LENGTH_SHORT).show();
                         new Handler().postDelayed(new Runnable() {
                             @Override
                             public void run() {
                                 app.saveCurrentUser(null);
                                 Intent intent=new Intent(ResetPwdActivity.this,IndexActivity.class);
                                 startActivity(intent);
                                 finish();
                             }
                         },1500);
                     }
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }

             @Override
             public void onError(Throwable ex, boolean isOnCallback) {
                 pUtil.dismissProgress();
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
}
