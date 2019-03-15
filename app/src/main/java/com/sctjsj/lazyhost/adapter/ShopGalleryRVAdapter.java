package com.sctjsj.lazyhost.adapter;

/**
 * Created by Chris-Jason on 2016/11/4.
 */

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.widget.BigPicDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Chris-Jason on 2016/11/4.
 */

/**
 * 评论标签适配器
 */
public class ShopGalleryRVAdapter extends RecyclerView.Adapter<ShopGalleryRVAdapter.Holder> {

    private Context context;
    private List<String> data;
    private LayoutInflater inflater;

    public ShopGalleryRVAdapter(Context context, List<String>data){
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
        Holder holder=new Holder(inflater.inflate(R.layout.item_fg_detail_in_shop_shop_gallery,null));
        return holder;
    }

    /**
     * 设置数据
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final Holder holder, final int position) {
        if(data.get(position)!=null){
           Glide.with(context).load(data.get(position)).error(R.mipmap.icon_portrait_load_failed).into(holder.mIV);

        }
        holder.mIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击查看大图
                BigPicDialog bpDialog=new BigPicDialog((AppCompatActivity) context,data);

                bpDialog.show();

            }
        });
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * 绑定控件
     */
    class Holder extends RecyclerView.ViewHolder{
        ImageView mIV;
        public Holder(final View itemView) {
            super(itemView);
            mIV = (ImageView) itemView.findViewById(R.id.item_fg_detail_in_shop_shop_gallery_iv);

        }
    }
}
