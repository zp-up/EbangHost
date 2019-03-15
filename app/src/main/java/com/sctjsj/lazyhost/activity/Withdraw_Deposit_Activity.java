package com.sctjsj.lazyhost.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.ProgressUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.DecimalFormat;

public class Withdraw_Deposit_Activity extends AppCompatActivity {

    private double balance = 0.00;
    private TextView tvBalance;
    private ImageView ivBack;
    private TextView tvAccount;
    private EditText etBalance;
    private EditText etAccount;
    private EditText etRealName;
    private RelativeLayout rlWX,rlAlipay;
    private boolean isOk = false;

    private TextView tvNotice;

    private double depositBalance = 0.00;
    private ProgressUtil progressUtil;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_withdraw__deposit);

        progressUtil = new ProgressUtil(this);
        Intent intent = getIntent();
        if (intent.getDoubleExtra("price",-1) != -1){
            balance = intent.getDoubleExtra("price",0.00);
        }
        initView();

    }

    private void initView() {
        tvNotice = (TextView) findViewById(R.id.tv_notice);
        tvBalance = (TextView) findViewById(R.id.tv_balance);
        tvBalance.setText("提现金额(余额:￥"+new DecimalFormat("######0.00").format(balance)+")");
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Withdraw_Deposit_Activity.this.finish();
            }
        });
        tvAccount = (TextView) findViewById(R.id.tv_account);
        etAccount = (EditText) findViewById(R.id.et_account);
        etAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0){
                    tvAccount.setText("("+s.toString().trim()+")");
                }else {
                    tvAccount.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etRealName = (EditText) findViewById(R.id.et_real_name);
        etBalance = (EditText) findViewById(R.id.et_price);
        etBalance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() != 0){
                    try {
                        if (Double.valueOf(etBalance.getText().toString().trim()) > balance){
                            etBalance.setTextColor(Color.parseColor("#ff7d66"));
                            tvNotice.setVisibility(View.VISIBLE);
                            tvNotice.setText("余额不足");
                            isOk = false;
                        }else {
                            isOk = true;
                            etBalance.setTextColor(Color.parseColor("#636363"));
                            tvNotice.setVisibility(View.GONE);
                            tvNotice.setText("余额不足");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        etBalance.setTextColor(Color.parseColor("#ff7d66"));
                        tvNotice.setVisibility(View.VISIBLE);
                        tvNotice.setText("输入内容有误");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        rlWX = (RelativeLayout) findViewById(R.id.rl_btn_wx);
        rlAlipay = (RelativeLayout) findViewById(R.id.rl_btn_alipay);
        rlWX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Withdraw_Deposit_Activity.this,"正在开发中...",Toast.LENGTH_SHORT).show();
            }
        });
        rlAlipay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deposit();
            }
        });
    }

    private void deposit() {
        if (!isOk){
            Toast.makeText(Withdraw_Deposit_Activity.this,"余额不足。",Toast.LENGTH_SHORT).show();
            return;
        }
        if (etBalance.getText().toString().trim().length() == 0){
            Toast.makeText(Withdraw_Deposit_Activity.this,"请输入提现的金额",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Double.valueOf(etBalance.getText().toString().trim());
        }catch (Exception e){
            Toast.makeText(Withdraw_Deposit_Activity.this,"请输入正确的提现金额",Toast.LENGTH_SHORT).show();
            return;
        }
        if (Double.valueOf(etBalance.getText().toString().trim()) > balance){
            Toast.makeText(Withdraw_Deposit_Activity.this,"余额不足。",Toast.LENGTH_SHORT).show();
            return;
        }
        if (etAccount.getText().toString().trim().length() == 0){
            Toast.makeText(Withdraw_Deposit_Activity.this,"请输入正确的提现账户",Toast.LENGTH_SHORT).show();
            return;
        }
        if (etRealName.getText().toString().trim().length() == 0){
            Toast.makeText(Withdraw_Deposit_Activity.this,"请输入真实姓名",Toast.LENGTH_SHORT).show();
            return;
        }
        RequestParams params = new RequestParams(BnUrl.depositUrl);
        params.addHeader("Cookie", ((MyApp) getApplication()).getSpf().getString("cookie", ""));
        params.addBodyParameter("price",etBalance.getText().toString().trim());
        params.addBodyParameter("type","1");
        params.addBodyParameter("payType","1");
        params.addBodyParameter("account",etAccount.getText().toString().trim());
        params.addBodyParameter("realName",etRealName.getText().toString().trim());
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                if (result != null){
                    Log.e("TAG","提现结果:"+result);
                    try {
                        boolean isOk = result.getBoolean("result");
                        String msg = result.getString("resultMsg");
                        Toast.makeText(Withdraw_Deposit_Activity.this,msg,Toast.LENGTH_SHORT).show();
                        if (isOk){
                            Withdraw_Deposit_Activity.this.finish();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
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


}
