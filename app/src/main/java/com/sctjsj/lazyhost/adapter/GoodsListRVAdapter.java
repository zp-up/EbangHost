package com.sctjsj.lazyhost.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sctjsj.lazyhost.activity.BigPicActivity;
import com.sctjsj.lazyhost.interf.ShopCartImp;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.bean.Dish;
import com.sctjsj.lazyhost.bean.DishMenu;
import com.sctjsj.lazyhost.bean.ShopCart;
import com.sctjsj.lazyhost.util.DpUtils;
import com.sctjsj.lazyhost.util.NumformatUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by cheng on 16-11-10.
 */
public class GoodsListRVAdapter extends RecyclerView.Adapter {
    private final int MENU_TYPE = 0;
    private final int DISH_TYPE = 1;
    private final int HEAD_TYPE = 2;

    private Context mContext;
    private ArrayList<DishMenu> mMenuList;
    private int mItemCount;
    private ShopCart shopCart;
    private ShopCartImp shopCartImp;

    public GoodsListRVAdapter(Context mContext, ArrayList<DishMenu> mMenuList, ShopCart shopCart){
        this.mContext = mContext;
        this.mMenuList = mMenuList;
        this.mItemCount = mMenuList.size();
        this.shopCart = shopCart;
        for(DishMenu menu:mMenuList){
            mItemCount+=menu.getDishList().size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        int sum=0;
        for(DishMenu menu:mMenuList){
            if(position==sum){
                return MENU_TYPE;
            }
            sum+=menu.getDishList().size()+1;
        }
        return DISH_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==MENU_TYPE){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_menu_item, parent, false);
            MenuViewHolder viewHolder = new MenuViewHolder(view);
            return viewHolder;
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goods_list_in_shop, parent, false);
            DishViewHolder viewHolder = new DishViewHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(getItemViewType(position)==MENU_TYPE){
            MenuViewHolder menuholder = (MenuViewHolder)holder;
            if(menuholder!=null) {
                menuholder.right_menu_title.setText(getMenuByPosition(position).getMenuName());
                menuholder.right_menu_layout.setContentDescription(position+"");
            }
        }else {
            final DishViewHolder dishholder = (DishViewHolder) holder;
            if (dishholder != null) {
                final Dish dish = getDishByPosition(position);
                //设置商品名
                dishholder.right_dish_name_tv.setText(dish.getDishName());
                //设置商品价格

                dishholder.right_dish_cur_price_tv.setText("￥"+ NumformatUtil.save2(dish.getCurrentPrice()));
                if(dish.getCurrentPrice()>0){
                    dishholder.right_dish_ori_price_tv.setVisibility(View.VISIBLE);
                    dishholder.right_dish_ori_price_tv.setText("立减"+dish.getDiscount()+"元");
                }else {
                    dishholder.right_dish_ori_price_tv.setVisibility(View.GONE);
                }

                dishholder.right_dish_layout.setContentDescription(position+"");
                dishholder.saleNum.setText("售:"+dish.getSalenum());

                //点击查看大图
                dishholder.ivIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(mContext, BigPicActivity.class);
                        intent.putExtra("url",dish.getUrl());
                        mContext.startActivity(intent);
                    }
                });

                Glide.with(mContext).load(dish.getUrl()).error(R.mipmap.icon_portrait_load_failed).into(dishholder.ivIcon);

                int count = 0;
                if(shopCart.getShoppingSingleMap().containsKey(dish)){
                    count = shopCart.getShoppingSingleMap().get(dish);
                }
                if(count<=0){
                    dishholder.right_dish_remove_iv.setVisibility(View.GONE);
                    dishholder.right_dish_account_tv.setVisibility(View.GONE);
                }else {
                    dishholder.right_dish_remove_iv.setVisibility(View.VISIBLE);
                    dishholder.right_dish_account_tv.setVisibility(View.VISIBLE);
                    dishholder.right_dish_account_tv.setText(count+"");
                }
                dishholder.right_dish_add_iv.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {


                        //保证不能重复添加
                        if(shopCart.addShoppingSingle(dish)) {
                            notifyItemChanged(position);
                            if(shopCartImp!=null){
                                shopCartImp.add(dishholder.right_dish_add_iv,position);
                            }

                        }
                    }
                });

                dishholder.right_dish_remove_iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(shopCart.subShoppingSingle(dish)){
                            notifyItemChanged(position);
                            if(shopCartImp!=null)
                                shopCartImp.remove(view,position);
                        }
                    }
                });
            }
        }
    }

    public DishMenu getMenuByPosition(int position){
        int sum =0;
        for(DishMenu menu:mMenuList){
            if(position==sum){
                return menu;
            }
            sum+=menu.getDishList().size()+1;
        }
        return null;
    }

    public Dish getDishByPosition(int position){
        for(DishMenu menu:mMenuList){
            if(position>0 && position<=menu.getDishList().size()){
                return menu.getDishList().get(position-1);
            }
            else{
                position-=menu.getDishList().size()+1;
            }
        }
        return null;
    }

    public DishMenu getMenuOfMenuByPosition(int position){
        for(DishMenu menu:mMenuList){
            if(position==0)return menu;
            if(position>0 && position<=menu.getDishList().size()){
                return menu;
            }
            else{
                position-=menu.getDishList().size()+1;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    public ShopCartImp getShopCartImp() {

        return shopCartImp;
    }

    public void setShopCartImp(ShopCartImp shopCartImp) {
        this.shopCartImp = shopCartImp;
    }

    private class MenuViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout right_menu_layout;
        private TextView right_menu_title;
        public MenuViewHolder(View itemView) {
            super(itemView);
            right_menu_layout = (LinearLayout)itemView.findViewById(R.id.right_menu_item);
            right_menu_title = (TextView)itemView.findViewById(R.id.right_menu_tv);
        }
    }

    private class DishViewHolder extends RecyclerView.ViewHolder{
        private TextView right_dish_name_tv;//商品名字
        private TextView right_dish_cur_price_tv;//商品现在的折扣价格
        private TextView right_dish_ori_price_tv;//商品原来的价格
        private LinearLayout right_dish_layout;//整个商品 item
        private ImageView right_dish_remove_iv;//添加按钮
        private ImageView right_dish_add_iv;//移除按钮
        private TextView right_dish_account_tv;//剩余数量
        private TextView saleNum;//已售数量
        private ImageView ivIcon;//商品头像

        public DishViewHolder(View itemView) {
            super(itemView);
            //商品名字
            right_dish_name_tv = (TextView)itemView.findViewById(R.id.right_dish_name);
            //商品现价
            right_dish_cur_price_tv = (TextView)itemView.findViewById(R.id.right_dish_dis_price);
            //商品原价
            right_dish_ori_price_tv= (TextView) itemView.findViewById(R.id.right_dish_ori_price);
            //商品 layout
            right_dish_layout = (LinearLayout)itemView.findViewById(R.id.item_goods_list_in_shop_ll_parent);
            // 移除
            right_dish_remove_iv = (ImageView)itemView.findViewById(R.id.right_dish_remove);
            //添加
            right_dish_add_iv = (ImageView)itemView.findViewById(R.id.right_dish_add);
            //计数
            right_dish_account_tv = (TextView) itemView.findViewById(R.id.right_dish_account);
            //已售
            saleNum= (TextView) itemView.findViewById(R.id.item_goods_list_in_shop_tv_salenum);
            // icon
            ivIcon= (ImageView) itemView.findViewById(R.id.item_goods_list_in_shop_iv_icon);
        }

    }
}
