package com.sctjsj.ebanghost.bean;

/**
 * Created by Chris-Jason on 2016/11/4.
 */

/**
 * 店铺实体类
 */
public class ShopBean {
    private int id;//店铺 id
    private String name;//店铺名称
    private int storeStatus;//店铺状态{0：歇业；1：营业}
    private int type;//店铺类型{1：美食；2：甜品；3：洗衣；4：礼物}
    private String portraitUrl;//店铺 logo
    private String shopTel;//商家电话
    private double baseShipPrice;//基础配送费
    private int soldNum;//已售数量

    public int getSoldNum() {
        return soldNum;
    }

    public void setSoldNum(int soldNum) {
        this.soldNum = soldNum;
    }

    public double getBaseShipPrice() {
        return baseShipPrice;
    }

    public void setBaseShipPrice(double baseShipPrice) {
        this.baseShipPrice = baseShipPrice;
    }

    private double longtitude;

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public double getLantitude() {
        return lantitude;
    }

    public void setLantitude(double lantitude) {
        this.lantitude = lantitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private double lantitude;
    private String address;

    public String getShopTel() {
        return shopTel;
    }

    public void setShopTel(String shopTel) {
        this.shopTel = shopTel;
    }

    public ShopBean(){

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

    public int getStoreStatus() {
        return storeStatus;
    }

    public void setStoreStatus(int storeStatus) {
        this.storeStatus = storeStatus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPortraitUrl() {
        return portraitUrl;
    }

    public void setPortraitUrl(String portraitUrl) {
        this.portraitUrl = portraitUrl;
    }
}
