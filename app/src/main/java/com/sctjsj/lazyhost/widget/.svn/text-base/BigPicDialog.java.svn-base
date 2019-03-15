package com.sctjsj.ebanghost.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bm.library.PhotoView;

import com.sctjsj.ebanghost.R;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by liudezhi on 16/8/19.
 */
public class BigPicDialog extends Dialog implements View.OnClickListener {

    private AppCompatActivity mActivity;
    private ViewPager mVP;
    private ImageView mIVLeft,mIVRight;
    private List<String>picList;
    private List<Fragment> fgList;
    /**
     * Activity中创建
     * @param mActivity
     */
    public BigPicDialog(AppCompatActivity mActivity, List<String> picList) {
        super(mActivity, R.style.dialog_bottom);
        this.mActivity = mActivity;
        this.picList=picList;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.big_pic_dialog_layout);
        initLocation();
        initView();
    }

    private void initLocation(){
        Window Window = this.getWindow();
        //显示位置
        Window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams params = Window.getAttributes();
        params.y = 20;
        Window.setAttributes(params);
    }

    /**
     * 初始化VP视图
     */
    private void initView(){
        mIVLeft = (ImageView) findViewById(R.id.big_pic_dialog_layout_iv_to_left);
        mIVRight = (ImageView) findViewById(R.id.big_pic_dialog_layout_iv_to_right);
        /**
         * 设置监听
         */
        mIVLeft.setOnClickListener(this);
        mIVRight.setOnClickListener(this);

        /**
         * 初始化VP
         */
        mVP = (ViewPager) findViewById(R.id.big_pic_dialog_vp);
        mVP.setPageMargin((int) (mActivity.getResources().getDisplayMetrics().density * 15));
        mVP.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return picList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view==object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                PhotoView view = new PhotoView(mActivity);
                view.enable();
                ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(params);
                view.setScaleType(ImageView.ScaleType.FIT_XY);
                Picasso.with(mActivity).load(picList.get(position)).into(view);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });
    }

    /**
     * 初始化数据
     */

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.big_pic_dialog_layout_iv_to_left:
                mVP.setCurrentItem(mVP.getCurrentItem()>0?mVP.getCurrentItem()-1:0);
                break;
            case R.id.big_pic_dialog_layout_iv_to_right:
                mVP.setCurrentItem(mVP.getCurrentItem()<picList.size()? mVP.getCurrentItem()+1:picList.size());
                break;
        }


    }
}
