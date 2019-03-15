package com.sctjsj.lazyhost.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.sctjsj.lazyhost.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Admin on 2016/11/15.
 */

public class NewOrderFg extends Fragment {
    @Bind(R.id.fg_neworder_lv)ListView mListView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fg_neworder,null);
        ButterKnife.bind(this,view);
        mListView.setAdapter(new MyAdapter(getActivity()));
        return view;
    }
    private class MyAdapter extends BaseAdapter{
       Context context;

        public MyAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view=LayoutInflater.from(context).inflate(R.layout.item_of_fg_dispose_order,null);
            return view;
        }
    }
}
