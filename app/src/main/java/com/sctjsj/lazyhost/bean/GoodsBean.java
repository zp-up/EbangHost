package com.sctjsj.lazyhost.bean;

/**
 * Created by Chris-Jason on 2016/11/9.
 */

import java.util.List;

/**
 * 商品实体
 */
public class GoodsBean {


    public double getDisPrice() {
        return disPrice;
    }

    public void setDisPrice(double disPrice) {
        this.disPrice = disPrice;
    }
    private int id;//id
    private String name;//名字
    private double price;//价格
    private double disPrice;//优惠金额
    private int count;//总数
    private String details;//商品描述
    private String logoUrl;//logo
    private int logoId;//上传图片后的图片表 id
    private int status;//商品状态
    private int isDelete;//是否删除
    private int isCertify;//审核状态
    private int goodstypeId;//所属类型 id
    private List<ClassifyBean> classifyBeanList;//商家自定义分类列表


    public int getIsCertify() {
        return isCertify;
    }

    public void setIsCertify(int isCertify) {
        this.isCertify = isCertify;
    }

    public int getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(int isDelete) {
        this.isDelete = isDelete;
    }


    public int getGoodstypeId() {
        return goodstypeId;
    }

    public void setGoodstypeId(int goodstypeId) {
        this.goodstypeId = goodstypeId;
    }



    public int getLogoId() {
        return logoId;
    }

    public void setLogoId(int logoId) {
        this.logoId = logoId;
    }



    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }


    public List<ClassifyBean> getClassifyBeanList() {
        return classifyBeanList;
    }

    public void setClassifyBeanList(List<ClassifyBean> classifyBeanList) {
        this.classifyBeanList = classifyBeanList;
    }

    public GoodsBean() {
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
