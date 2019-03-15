package com.sctjsj.lazyhost.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.adapter.FgOrderQueryVPAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Chris-Jason on 2016/11/9.
 */
public class OrderQueryFg extends Fragment {
    private List<String> tabList;
    private List<Fragment>fgList;
    private TodayOrderFg todayOrderFg;
    private AllOrderFg allOrderFg;
    private FgOrderQueryVPAdapter vpAdapter;
    private int current =1;


    @Bind(R.id.iv_clear_search) ImageView mIVClearSearch;
    @Bind(R.id.input)EditText mInput;
    @Bind(R.id.fg_order_query_title_tab)TabLayout mTitleTab;
    @Bind(R.id.fg_order_query_view_pager)ViewPager mVP;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fg_order_query,null);
        ButterKnife.bind(this,view);
        initVP();

        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (TextUtils.isEmpty(mInput.getText().toString())) {
                    Toast.makeText(getActivity(), "请输入单号再搜索", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //发起搜索
                    doSearch(mInput.getText().toString());
                    return true;
                }
                return false;
            }
        });
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    if (!TextUtils.isEmpty(s.toString())) {
                        mIVClearSearch.setVisibility(View.VISIBLE);
                    } else {
                        mIVClearSearch.setVisibility(View.GONE);
                    }
                }

            }
        });


        return view;
    }

    private void doSearch(String name) {

        switch (current){
            case 1:
                if(todayOrderFg!=null){
                    todayOrderFg.search(name);
                }
                break;
            case 2:
                if(allOrderFg!=null){
                    allOrderFg.search(name);
                }
                break;
        }
    }

    /**
     * 初始化 viewpager
     */
    private void initVP() {
        //1.初始化 tab 指示器
        initTablayout();
        //2.初始化 vp
        initViewpagerData();
        initVPAdapter();
        //3.关联 tab 和 vp
        mTitleTab.setupWithViewPager(mVP);
        mTitleTab.setTabsFromPagerAdapter(vpAdapter);
        mVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                current = position+1;

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vpAdapter.notifyDataSetChanged();
    }

    /**
     * 初始化 tab
     */
    private void initTablayout(){
        if(tabList==null){
            tabList=new ArrayList<>();
            tabList.add("今日已完成订单");
            tabList.add("全部订单");
        }
        //指示器选中颜色
        mTitleTab.setSelectedTabIndicatorColor(Color.parseColor("#FF4081"));
    }

    /**
     * 初始化viewpager数据
     */
    private void initViewpagerData(){
        if(fgList==null){
            fgList=new ArrayList<>();
        }
        todayOrderFg=new TodayOrderFg();
        allOrderFg=new AllOrderFg();
        fgList.add(todayOrderFg);
        fgList.add(allOrderFg);
    }

    /**
     * 初始化vp适配器
     */
    private void initVPAdapter(){
        if(vpAdapter==null){
            vpAdapter=new FgOrderQueryVPAdapter(getChildFragmentManager(),tabList,fgList);
        }
        mVP.setAdapter(vpAdapter);
    }
    @OnClick({R.id.rl_clear_search_input})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.rl_clear_search_input:
                if (mInput != null) {
                    mInput.setText("");
                    mIVClearSearch.setVisibility(View.GONE);
                }

                if(todayOrderFg!=null){
                    todayOrderFg.initData();
                }

                if(allOrderFg!=null){
                    allOrderFg.initData();
                }

                break;
        }
    }

}

