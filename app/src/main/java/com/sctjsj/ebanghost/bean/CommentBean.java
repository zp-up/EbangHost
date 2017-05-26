package com.sctjsj.ebanghost.bean;

/**
 * Created by Chris-Jason on 2016/11/7.
 */
public class CommentBean {
    private int id;//评价表 id
    private int goodsEvaluate;//商品评分
    private String goodsEvaluateInfo;//商品评价详情
    private int serviceEvaluate;//服务评分
    private String serviceEvaluateInfo;//服务评价详情
    private String insertTime;//评价时间

    public String getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(String insertTime) {
        this.insertTime = insertTime;
    }

    private UserBean userBean;
    private ShopBean shopBean;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGoodsEvaluate() {
        return goodsEvaluate;
    }

    public void setGoodsEvaluate(int goodsEvaluate) {
        this.goodsEvaluate = goodsEvaluate;
    }

    public String getGoodsEvaluateInfo() {
        return goodsEvaluateInfo;
    }

    public void setGoodsEvaluateInfo(String goodsEvaluateInfo) {
        this.goodsEvaluateInfo = goodsEvaluateInfo;
    }

    public int getServiceEvaluate() {
        return serviceEvaluate;
    }

    public void setServiceEvaluate(int serviceEvaluate) {
        this.serviceEvaluate = serviceEvaluate;
    }

    public String getServiceEvaluateInfo() {
        return serviceEvaluateInfo;
    }

    public void setServiceEvaluateInfo(String serviceEvaluateInfo) {
        this.serviceEvaluateInfo = serviceEvaluateInfo;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public ShopBean getShopBean() {
        return shopBean;
    }

    public void setShopBean(ShopBean shopBean) {
        this.shopBean = shopBean;
    }
}
