package com.sctjsj.ebanghost.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.bm.library.PhotoView;
import com.sctjsj.ebanghost.R;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BigPicActivity extends Activity {
    private String url;
    @Bind(R.id.photo_view)PhotoView pv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉Activity上面的状态栏
        setContentView(R.layout.activity_big_pic);
        ButterKnife.bind(this);
        pv.enable();
        url=getIntent().getStringExtra("url");
        if(url!=null){
            Picasso.with(this).load(url).error(R.mipmap.icon_portrait_load_failed).into(pv);
        }

    }



    @OnClick({R.id.activity_big_pic_ll_parent,R.id.activity_big_pic_iv_back})
    public void bigpicClick(View v){
        switch (v.getId()){
            case R.id.activity_big_pic_ll_parent:
                finish();
                break;
            case R.id.activity_big_pic_iv_back:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }
}
