package com.sctjsj.ebanghost.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.MyCountTimer;
import com.sctjsj.ebanghost.util.ProgressUtil;
import com.sctjsj.ebanghost.util.RegexpValidateUtil;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GetPwdBackActivity extends Activity {

    private String TAG="GetPwdbackActivity";
    private MyApp app;
    private OkHttpClient client;
    private String cookie;
    private ProgressUtil pUtil;

    private int green= Color.parseColor("#33aab4");
    private int gray=Color.parseColor("#bfbfbf");

    @Bind(R.id.activity_get_pwd_back_ll_parent)
    LinearLayout mLLParent;//布局容器

    @Bind(R.id.activity_get_pwd_back_rl_verify_code_parent)
    RelativeLayout mRLPiccodeParent;//图片验证码容器

    @Bind(R.id.activity_get_pwd_back_et_phone)
    EditText mETPhone;//手机号

    @Bind(R.id.activity_get_pwd_back_et_pic_code)
    EditText mETPiccode;//图片验证码

    @Bind(R.id.activity_get_pwd_back_et_message_code)
    EditText mETSmsCode;//短信验证码

    @Bind(R.id.activity_get_pwd_back_tv_get_message_code)
    TextView mTVSendSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_pwd_back);
        ButterKnife.bind(this);
        app= (MyApp) getApplication();
        app.addActivity(this);
        pUtil=new ProgressUtil(this);
        //加载图片验证码
        getPicVerifyCode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        app.removeActivity(this);
    }

    @OnClick({R.id.activity_get_pwd_back_btn_next,R.id.activity_get_pwd_back_ll_back,
            R.id.activity_get_pwd_back_tv_change_verify_code,R.id.activity_get_pwd_back_tv_get_message_code
    })
    public void getPwdbackClick(View view){
        switch (view.getId()){
            //下一步
            case R.id.activity_get_pwd_back_btn_next:
                if (beforeNext()){
                    Intent intent=new Intent(GetPwdBackActivity.this,ResetPwdActivity.class);
                    intent.putExtra("tel",mETPhone.getText().toString());
                    intent.putExtra("sms",mETSmsCode.getText().toString());
                    intent.putExtra("cookie",cookie);
                    startActivity(intent);
                }

                break;
            //返回
            case R.id.activity_get_pwd_back_ll_back:
                finish();
                break;

            //换一张图片验证码
            case R.id.activity_get_pwd_back_tv_change_verify_code:
                getPicVerifyCode();
                break;

            //获取短信验证码
            case R.id.activity_get_pwd_back_tv_get_message_code:
                if(beforeSendSms()){
                    sendSmsCode();
                }
                break;
        }
    }

    /**
     * 加载验证码图片
     */
    private void getPicVerifyCode(){
        mRLPiccodeParent.removeAllViews();
        ImageView tempIV=new ImageView(GetPwdBackActivity.this);
        ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        tempIV.setLayoutParams(params);
        tempIV.setScaleType(ImageView.ScaleType.FIT_XY);
        mRLPiccodeParent.addView(tempIV);

        if(client==null){
            client = new OkHttpClient();
        }
        final CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client.setCookieHandler(cookieManager);
        final OkHttpDownloader downloader = new OkHttpDownloader(client);
        Picasso picasso=new Picasso.Builder(GetPwdBackActivity.this).downloader(downloader).build();
        picasso.load(BnUrl.getPicCodeUrl).skipMemoryCache().into(tempIV, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                List<HttpCookie> list=cookieManager.getCookieStore().getCookies();
                //获取此次请求的cookie
                if(list!=null&&list.size()>0){
                    cookie=list.get(0).toString();
                }
            }

            @Override
            public void onError() {
                Snackbar.make(mLLParent,"图片验证码加载失败", Snackbar.LENGTH_SHORT).show();
                pUtil.dismissProgress();
            }
        });
    }

    /**
     * 发送短信之前检测
     * @return
     */
    private boolean beforeSendSms(){

        if(!RegexpValidateUtil.checkMobileNumber(mETPhone.getText().toString())){
            Snackbar.make(mLLParent,"请输入正确的手机号码", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(mETPiccode.getText().toString())){
            Snackbar.make(mLLParent,"请输入图片验证码", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * 下一步之前检测
     * @return
     */
    private boolean beforeNext(){

        if(!RegexpValidateUtil.checkMobileNumber(mETPhone.getText().toString())){
            Snackbar.make(mLLParent,"请输入正确的手机号码", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(mETSmsCode.getText().toString())){
            Snackbar.make(mLLParent,"请输入接收到的短信验证码", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * 发送短信
     */
    private void sendSmsCode(){
        RequestParams params=new RequestParams(BnUrl.getSmsCodeUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",cookie);
        params.addBodyParameter("code",mETPiccode.getText().toString());
        params.addBodyParameter("mobile",mETPhone.getText().toString());
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    boolean results=result.getBoolean("result");
                    //发送成功
                    if(results){
                        Snackbar.make(mLLParent,"短信发送成功", Snackbar.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pUtil.dismissProgress();

                            }
                        },1000);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Snackbar.make(mLLParent,"短信发送失败", Snackbar.LENGTH_LONG).show();
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
                new MyCountTimer(60000,1000,mTVSendSms,R.string.get_verify_code_txt,green,gray).start();
                pUtil.showProgress(false);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });
    }

}
