package com.sctjsj.lazyhost.fragment;

/**
 * Created by mayikang on 2018/7/30.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * 封装的 Fragment 基类
 */
public abstract class BaseFragment extends Fragment {

    //是否懒加载
    private boolean isLazyload=true;
    /**
     * 宿主的activity
     **/
    protected FragmentActivity mActivity;

    /**
     * 根view
     **/
    protected View mRootView;

    /**
     * Fg是否对用户可见
     **/
    protected boolean mIsVisible;

    /**
     * 是否加载完成
     * 当执行完oncreatview方法后即为true，标识碎片加载完成
     */
    protected boolean mIsPrepare;


    /**
     * 碎片和 Activity 绑定
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    /**
     * 加载碎片
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //1.加载布局 XML 文件
        mRootView = inflater.inflate(setLayoutResource(), container, false);

        initView();
        //返回LoadSir生成的LoadLayout
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mIsPrepare=true;
        isLazyload=setLazyLod();
        //非懒加载模式，直接初始化数据
        if(!isLazyload){
            initData();
        }
    }

    protected abstract void initData();

   public abstract void initView();

    protected abstract boolean setLazyLod();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        this.mIsVisible = isVisibleToUser;

        if (isVisibleToUser) {
            onVisibleToUser();
        }
    }

    /**
     * 用户可见时执行的操作
     */
    protected void onVisibleToUser() {
        //只有碎片加载完成和可见的时候，执行懒加载数据
        if (mIsPrepare && mIsVisible) {
            //只需要懒加载一次
            mIsPrepare=false;
            initData();
        }
    }

    /**
     * 设置根布局资源id
     *
     * @return
     */
    protected abstract int setLayoutResource();


    @SuppressWarnings("unchecked")
    protected <T extends View> T getView(int id) {
        if (mRootView == null) {
            return null;
        }

        return (T) mRootView.findViewById(id);
    }




}
