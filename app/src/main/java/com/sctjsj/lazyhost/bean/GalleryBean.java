package com.sctjsj.lazyhost.bean;

import java.io.Serializable;

/**
 * Created by mayikang on 17/1/12.
 */

public class GalleryBean implements Serializable{
    private int acyId;
    private int shopId;
    private String url;

    public int getAcyId() {
        return acyId;
    }

    public void setAcyId(int acyId) {
        this.acyId = acyId;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
