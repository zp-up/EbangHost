package com.sctjsj.lazyhost.bean;

import java.io.Serializable;

/**
 * Created by Chris-Jason on 2016/11/14.
 */
public class DeliveryAddressBean implements Serializable{
    private int id;//收货地址 id
    private String consignee;//收货人
    private String tel;//电话

    private ProvinceBean provinceBean;//所在省

    private CityBean cityBean;//所在城市

    private DistrictBean districtBean;//所在区县

    private String detail;//详细地址

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public DeliveryAddressBean() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public ProvinceBean getProvinceBean() {
        return provinceBean;
    }

    public void setProvinceBean(ProvinceBean provinceBean) {
        this.provinceBean = provinceBean;
    }

    public CityBean getCityBean() {
        return cityBean;
    }

    public void setCityBean(CityBean cityBean) {
        this.cityBean = cityBean;
    }

    public DistrictBean getDistrictBean() {
        return districtBean;
    }

    public void setDistrictBean(DistrictBean districtBean) {
        this.districtBean = districtBean;
    }
}
