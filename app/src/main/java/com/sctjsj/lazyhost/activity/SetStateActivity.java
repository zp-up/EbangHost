package com.sctjsj.lazyhost.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.LogUtil;
import com.sctjsj.lazyhost.util.ProgressUtil;
import com.zcw.togglebutton.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Admin on 2016/11/16.
 */

public class SetStateActivity extends AppCompatActivity {
    private MyApp app;
    private ProgressUtil pUtil;
    @Bind(R.id.activity_set_state_ll_parent)LinearLayout mLLParent;
    @Bind(R.id.activity_set_state_toggle)ToggleButton toggle;
    @Bind(R.id.activity_set_state_tv)TextView mTVState;

    private int shopStatus;//店铺状态

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_state);
        StatusBarUtil.setTranslucentForImageViewInFragment(this,0,null);
        ButterKnife.bind(this);
        app= (MyApp) getApplication();
        app.addActivity(this);
        pUtil=new ProgressUtil(this);

         shopStatus=app.getSpf().getInt("shopStatus",-1);

        if(2==shopStatus){
            toggle.setToggleOn();
            mTVState.setText("正在营业中");
        }
        if(3==shopStatus){
            toggle.setToggleOff();
            mTVState.setText("已打烊");

        }


        toggle.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                //storeStatus
                //2.正常3.关闭
               if(on){
                    changeState("2");
               } else{
                    changeState("3");
               }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        app.removeActivity(this);
    }

    @OnClick({R.id.activity_set_state_back_ll})
    public void thisActivtyListen(View view){
        switch (view.getId()){
            case R.id.activity_set_state_back_ll:
                finish();
                this.overridePendingTransition(R.anim.activity_anim_start,R.anim.activity_anim_end);
                break;
        }
    }

    /**
     * 改变店铺状态
     * 2-正在营业
     * 3.关闭
     */
    private void changeState(String s){
        RequestParams params=new RequestParams(BnUrl.changeShopStateUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
        params.addBodyParameter("ctype","store");
        params.addBodyParameter("data","{id:"+String.valueOf(app.getCurrentUser().getShopId())+",storeStatus:"+s+"}");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        //操作成功
                        if(result){
                            queryShopState();
                        }else{
                            Snackbar.make(mLLParent,"店铺状态修改失败",Snackbar.LENGTH_SHORT).show();
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

    /**
     * 查询当前店铺状态
     */
    private void queryShopState(){
        RequestParams paramas=new RequestParams(BnUrl.queryShopStateUrl);
        paramas.setUseCookie(false);
        paramas.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
        paramas.addBodyParameter("storeId",String.valueOf(app.getCurrentUser().getShopId()));
        x.http().post(paramas, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                //LogUtil.e(result.toString());
                try {
                    int storeStatus=result.getInt("storeStatus");
                    app.getSpf().edit().putInt("shopStatus",storeStatus).commit();

                    if(2==storeStatus){
                        toggle.setToggleOn();
                        mTVState.setText("正在营业中");
                        Snackbar.make(mLLParent,"店铺开始营业",Snackbar.LENGTH_SHORT).show();
                    }
                    if(3==storeStatus){
                        toggle.setToggleOff();
                        mTVState.setText("已打烊");
                        Snackbar.make(mLLParent,"店铺打烊了",Snackbar.LENGTH_SHORT).show();
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
