package com.sctjsj.lazyhost.bean;

import org.xutils.x;

import java.util.List;

/**
 * Created by Chris-Jason on 2016/11/9.
 */
public class OrderBean {
    /**订单状态
     * 5-待付款
     * 10-已付款
     * 11-申请退款
     * 12-同意退款
     * 13-退款失败
     * 14-退款成功
     * 15-已接单
     * 16-拒接单
     * 17-配送中
     * 20-已消费
     * 30-已下单
     * 40-已收件
     * 60-已结算
     * 18-已送达
     */
    private  String storeOrderNo;
    private int id;//订单表 id也是订单号
    private String name;//订单名称，展示给用户看的订单号
    private List<GoodsBean> goodsBeanList;//一张订单里面的商品列表
    private double totalprice;//订单总价，包括运费等
    private double disvalue;//优惠金额
    private ShopBean shopBean; //订单店铺
    private int num;//订单总商品数量
    private int state;//订单状态
    private CommentBean commentBean;//评价
    private boolean isEvaluated;//该订单是否已经被评价过
    private String insertTime;//下单时间

    private String sendTime;//送件时间
    private String takeTime;//取件时间
    private double packagePrice;
    private double payValue;
    private int type;

    public double getPayValue() {
        return payValue;
    }

    public void setPayValue(double payValue) {
        this.payValue = payValue;
    }

    public String getStoreOrderNo() {
        return storeOrderNo;
    }

    public void setStoreOrderNo(String storeOrderNo) {
        this.storeOrderNo = storeOrderNo;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {

        return type;
    }

    public void setPackagePrice(double packagePrice) {
        this.packagePrice = packagePrice;
    }

    public double getPackagePrice() {

        return packagePrice;
    }

    public String getTakeTime() {
        return takeTime;
    }

    public void setTakeTime(String takeTime) {
        this.takeTime = takeTime;
    }

    public String getSendTime() {

        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public double getDisvalue() {
        return disvalue;
    }

    public void setDisvalue(double disvalue) {
        this.disvalue = disvalue;
    }

    public String getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(String insertTime) {
        this.insertTime = insertTime;
    }

    private String buyerRemark;//买家备注
    private double shipPrice;//运费

    public String getBuyerRemark() {
        return buyerRemark;
    }

    public void setBuyerRemark(String buyerRemark) {
        this.buyerRemark = buyerRemark;
    }

    public double getShipPrice() {
        return shipPrice;
    }

    public void setShipPrice(double shipPrice) {
        this.shipPrice = shipPrice;
    }

    public DeliveryAddressBean getDeliveryAddressBean() {
        return deliveryAddressBean;
    }

    public void setDeliveryAddressBean(DeliveryAddressBean deliveryAddressBean) {
        this.deliveryAddressBean = deliveryAddressBean;
    }

    private DeliveryAddressBean deliveryAddressBean;//收货地址

    public OrderBean(){

    }

    public boolean isEvaluated() {
        return isEvaluated;
    }

    public void setEvaluated(boolean evaluated) {
        isEvaluated = evaluated;
    }

    public List<GoodsBean> getGoodsBeanList() {
        return goodsBeanList;
    }

    public void setGoodsBeanList(List<GoodsBean> goodsBeanList) {
        this.goodsBeanList = goodsBeanList;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(double totalprice) {
        this.totalprice = totalprice;
    }

    public ShopBean getShopBean() {
        return shopBean;
    }

    public void setShopBean(ShopBean shopBean) {
        this.shopBean = shopBean;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public CommentBean getCommentBean() {
        return commentBean;
    }

    public void setCommentBean(CommentBean commentBean) {
        this.commentBean = commentBean;
    }
}
