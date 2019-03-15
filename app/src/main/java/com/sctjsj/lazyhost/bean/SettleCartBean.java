package com.sctjsj.lazyhost.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mayikang on 16/11/26.
 */

/**
 * 店铺页购物车实体
 */
public class SettleCartBean implements Serializable{
    private int shopId;//店铺 id
    private int buyerId;//买家 id
    private String deliveryAddressId;//收货地址 id
    private double totalPrice;//本单总价
    private int totalCount;//商品总数
    private String remark;//买家备注

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    private List<Dish> dishList;//购买的商品列表（第一种格式），一条记录就是一件商品，
    private List<GoodsBean> goodsBeenList;//购买的商品列表（第二种格式），一种商品只有一条记录，一种商品的数量累加在一起

    public List<GoodsBean> getGoodsBeenList() {
        return goodsBeenList;
    }

    public void setGoodsBeenList(List<GoodsBean> goodsBeenList) {
        this.goodsBeenList = goodsBeenList;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public String getDeliveryAddressId() {
        return deliveryAddressId;
    }

    public void setDeliveryAddressId(String deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public SettleCartBean(){

    }

    public SettleCartBean(int shopId, int buyerId, List<Dish> dishList) {
        this.shopId = shopId;
        this.buyerId = buyerId;
        this.dishList = dishList;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public List<Dish> getDishList() {
        return dishList;
    }

    public void setDishList(List<Dish> dishList) {
        this.dishList = dishList;
    }
}
