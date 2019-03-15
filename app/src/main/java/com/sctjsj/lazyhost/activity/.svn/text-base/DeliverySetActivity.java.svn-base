package com.sctjsj.ebanghost.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.application.MyApp;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Admin on 2016/11/17.
 */

public class DeliverySetActivity extends AppCompatActivity {
    private MyApp app;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_set);
        app= (MyApp) getApplication();
        app.addActivity(this);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        app.removeActivity(this);
    }

    @OnClick({R.id.activity_set_back_ll})
    public void thisActivtyListen(View view){
        switch (view.getId()){
            case R.id.activity_set_back_ll:
                finish();
                this.overridePendingTransition(R.anim.activity_anim_start,R.anim.activity_anim_end);
                break;
        }
    }
}
