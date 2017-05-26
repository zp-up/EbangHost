package com.sctjsj.ebanghost.adapter;

/**
 * Created by Chris-Jason on 2016/11/4.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.bean.CommentTagBean;

import java.util.List;

/**
 * Created by Chris-Jason on 2016/11/4.
 */

/**
 * 评论标签适配器
 */
public class CommentTagRVAdapter extends RecyclerView.Adapter<CommentTagRVAdapter.Holder> {

    private Context context;
    private List<CommentTagBean> data;
    private LayoutInflater inflater;

    public CommentTagRVAdapter(Context context, List<CommentTagBean>data){
        this.context=context;
        this.data=data;
        inflater=LayoutInflater.from(context);
    }

    /**
     * 绑定Holder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        Holder holder=new Holder(inflater.inflate(R.layout.item_comment_fg_in_shop_rv_comment_tag,null));
        return holder;
    }

    /**
     * 设置数据
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(Holder holder, int position) {

    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    class Holder extends RecyclerView.ViewHolder{

        public Holder(View itemView) {
            super(itemView);
        }
    }
}
