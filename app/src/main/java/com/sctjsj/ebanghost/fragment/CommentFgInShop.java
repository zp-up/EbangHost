package com.sctjsj.ebanghost.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.adapter.CommentListRVAdapter;
import com.sctjsj.ebanghost.adapter.CommentTagRVAdapter;
import com.sctjsj.ebanghost.bean.CommentBean;
import com.sctjsj.ebanghost.bean.CommentTagBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Chris-Jason on 2016/11/4.
 */

/**
 * 展示评论
 */
public class CommentFgInShop extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fg_comment_in_shop,null);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


}
