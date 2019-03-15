package com.sctjsj.ebanghost.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by Chris-Jason on 2016/10/24.
 */
public class FgOrderQueryVPAdapter extends FragmentStatePagerAdapter {
    // table导航条里面的内容填充
    private List<String> tabList;
    private List<Fragment>fgList;
    private FragmentManager manager;

    public FgOrderQueryVPAdapter(FragmentManager fm, List<String> tabList, List<Fragment>fgList) {
        super(fm);
        this.manager=fm;
        this.tabList=tabList;
        this.fgList=fgList;
    }

    @Override
    public Fragment getItem(int position) {
        return fgList.get(position);
    }

    @Override
    public int getCount() {
        return fgList.size();
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return tabList.get(position);
    }
}


