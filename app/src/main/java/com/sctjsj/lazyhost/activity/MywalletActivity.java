package com.sctjsj.lazyhost.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.DecimalFormat;

public class MywalletActivity extends AppCompatActivity {
    private TextView jinrishouru, keyongyue, jinrizhichu;
    private double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mywallet);
        StatusBarUtil.setTranslucentForImageViewInFragment(this,0,null);
        findViewById(R.id.zhanghumingxixi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MywalletActivity.this, Account_Details_Activity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.kk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MywalletActivity.this.finish();
            }
        });
        findViewById(R.id.tixianxian).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MywalletActivity.this, Withdraw_Deposit_Activity.class);
                intent.putExtra("price", price);
                startActivity(intent);
            }
        });


        jinrishouru = (TextView) findViewById(R.id.jinrishouru);
        keyongyue = (TextView) findViewById(R.id.keyongyue);
        jinrizhichu = (TextView) findViewById(R.id.jinrizhichu);


        initData();
    }

    private void initData() {
        RequestParams params = new RequestParams(BnUrl.ServerIp + "/user/todayOrderStatistics$ajax.htm");
        params.setUseCookie(true);
        x.http().post(params, new org.xutils.common.Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.e("我的钱包页面返回的数据", "" + response.toString());
                if (response != null) {
                    try {
                        price = response.getDouble("balance");
                        jinrishouru.setText(new DecimalFormat("######0.00").format(response.getDouble("todayBalance")));
                        jinrizhichu.setText(new DecimalFormat("######0.00").format(response.getDouble("todayExpenditure")));
                        keyongyue.setText(new DecimalFormat("######0.00").format(response.getDouble("balance")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("TAG", "错误:" + ex.toString());
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
                LogUtil.e("");
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }
}
