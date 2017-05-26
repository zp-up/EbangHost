package com.sctjsj.ebanghost.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bm.library.PhotoView;
import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.bean.GalleryBean;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.ProgressUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GalleryScanActivity extends Activity {
    private int current=0;
    private PagerAdapter adapter;
    private MyApp app;
    @Bind(R.id.activity_gallery_scan_tv_remind)TextView mTVRemind;
    @Bind(R.id.activity_gallery_scan_vp)ViewPager mVP;
    @Bind(R.id.activity_gallery_scan)LinearLayout mLLParent;
    private List<GalleryBean> galleryBeanList=null;
    private ProgressUtil pUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gallery_scan);
        ButterKnife.bind(this);
        app= (MyApp) getApplication();
        pUtil=new ProgressUtil(this);

        galleryBeanList= (List<GalleryBean>) getIntent().getSerializableExtra("gallery");
        if(galleryBeanList.size()>0){
            mTVRemind.setText("1/"+galleryBeanList.size());
        }
        initVP();
        if(galleryBeanList!=null){
            mVP.setAdapter(adapter);
            mVP.setOffscreenPageLimit(9);

            mVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    current=position;

                    mTVRemind.setText((current+1)+"/"+galleryBeanList.size());

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.activity_gallery_scan_iv_back})
    public void galleryScanClick(View view){
        switch (view.getId()){
            //返回
            case R.id.activity_gallery_scan_iv_back:
                finish();
                break;

        }
    }

    private void initVP(){
        if(adapter==null){
            adapter=new PagerAdapter() {
                @Override
                public int getCount() {
                    return galleryBeanList.size();
                }

                @Override
                public boolean isViewFromObject(View view, Object object) {
                    return view==object;
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    PhotoView view = new PhotoView(GalleryScanActivity.this);
                    view.enable();
//                    ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                    view.setLayoutParams(params);
//                    view.setScaleType(ImageView.ScaleType.FIT_XY);
                    Picasso.with(GalleryScanActivity.this).load(galleryBeanList.get(position).getUrl()).into(view);
                    container.addView(view);
                    return view;
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView((View) object);
                }
            };
        }

    }


}
