package com.sctjsj.ebanghost.adapter;

/**
 * Created by Chris-Jason on 2016/11/4.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ornolfr.ratingview.RatingView;
import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.bean.CommentBean;
import com.sctjsj.ebanghost.bean.CommentTagBean;
import com.sctjsj.ebanghost.util.DpUtils;
import com.squareup.picasso.Picasso;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Chris-Jason on 2016/11/4.
 */

/**
 * 评论标签适配器
 */
public class CommentListRVAdapter extends RecyclerView.Adapter<CommentListRVAdapter.MyHolder> {

    private Context context;
    private List<CommentBean> data=new ArrayList<>();


    public CommentListRVAdapter(Context context, List<CommentBean>data){
        this.context=context;
        this.data=data;

    }

    /**
     * 绑定Holder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyHolder holder=new MyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_fg_in_shop_comment_list,null));
        return holder;
    }

    /**
     * 设置数据
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
            if(data.get(position).getUserBean().getPortraiturl()!=null && !"null".equals(data.get(position).getUserBean().getPortraiturl()) && !TextUtils.isEmpty(data.get(position).getUserBean().getPortraiturl())){
                Picasso.with(context).load(data.get(position).getUserBean().getPortraiturl()).resize(DpUtils.dpToPx(context,60),DpUtils.dpToPx(context,60)).centerCrop().error(R.mipmap.icon_portrait_load_failed).into(holder.mCIVPortrait);

            }
            holder.mTVName.setText(data.get(position).getUserBean().getAccount());
            holder.mTVTime.setText(data.get(position).getInsertTime());
            holder.mRating.setRating(((data.get(position).getGoodsEvaluate()+data.get(position).getServiceEvaluate())/2));
            holder.mTVTAG.setText(data.get(position).getGoodsEvaluateInfo());



    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyHolder extends ViewHolder{
        CircleImageView mCIVPortrait;
        TextView mTVName,mTVTime,mTVTAG;
        RatingView mRating;
        public MyHolder(View itemView) {
            super(itemView);
            mCIVPortrait= (CircleImageView) itemView.findViewById(R.id.item_comment_civ_portrait);
            mTVName= (TextView) itemView.findViewById(R.id.item_comment_tv_name);
            mTVTAG= (TextView) itemView.findViewById(R.id.item_comment_tv_tag);
            mTVTime= (TextView) itemView.findViewById(R.id.item_comment_tv_time);
            mRating= (RatingView) itemView.findViewById(R.id.item_comment_rate);
        }
    }

}
