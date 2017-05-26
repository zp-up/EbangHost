package com.sctjsj.ebanghost.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.DpUtils;
import com.sctjsj.ebanghost.util.ProgressUtil;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by Admin on 2016/11/15.
 */

public class UseFeedbackActivity extends AppCompatActivity{
    private ProgressUtil pUtil;
    @Bind(R.id.activity_use_feedback_et_content)EditText mETContent;
    @Bind(R.id.activity_use_feedback_ll_parent)LinearLayout mLLParent;


    private  String storeName;//店家名字
    private String storeAddress;//店家地址
    private String telephone;//店家电话

    private MyApp app;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_feedback);
        ButterKnife.bind(this);
        app= (MyApp) getApplication();
        app.addActivity(this);
        pUtil=new ProgressUtil(this);

        storeName=getIntent().getStringExtra("storeName");
        storeAddress=getIntent().getStringExtra("storeAddress");
        telephone=getIntent().getStringExtra("telephone");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        app.removeActivity(this);
    }

    @OnClick({R.id.activity_advice_back,R.id.activity_use_feedback_btn_submit})
    public void AdviceListen(View view){
        switch (view.getId()){
            //返回
            case R.id.activity_advice_back:
                finish();
                this.overridePendingTransition(R.anim.activity_anim_start,R.anim.activity_anim_end);
                break;
            //反馈
            case R.id.activity_use_feedback_btn_submit:
                if(check()){
                    submit();
                }
                break;
        }
    }

    public boolean check(){
        if(mETContent.getText().toString().length()<8){
            Snackbar.make(mLLParent,"请输入8字以上反馈信息",Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 提交反馈
     */
    private void submit(){
        RequestParams params=new RequestParams(BnUrl.feedbackUrl);
        params.addBodyParameter("content","【商家使用反馈】"+"\n"
                +"商户名："+storeName+"\n"
                +"商户地址："+storeAddress+"\n"
                +"商户电话："+telephone+"\n"
                +"反馈内容："+mETContent.getText().toString());
        params.addBodyParameter("url","1051757395@qq.com");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            Snackbar.make(mLLParent,"反馈成功",Snackbar.LENGTH_SHORT).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            },1500);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                pUtil.dismissProgress();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                pUtil.dismissProgress();
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
