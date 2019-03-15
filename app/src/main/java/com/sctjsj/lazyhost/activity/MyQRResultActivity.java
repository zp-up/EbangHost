package com.sctjsj.lazyhost.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import io.github.xudaojie.qrcodelib.CaptureActivity;

public class MyQRResultActivity extends CaptureActivity {
    private MyApp app;
    private ProgressDialog progress;
    private AlertDialog.Builder builder;
    private AlertDialog d;
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    /**
     * 处理扫码结果
     * @param resultString
     */
    @Override
    protected void handleResult(String resultString) {
        super.handleResult(resultString);
    }

}
