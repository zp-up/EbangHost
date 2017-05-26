package com.sctjsj.ebanghost.bean;

/**
 * Created by mayikang on 17/1/1.
 */

public class TurnoverBean {
    private int id;//主键 id
    private String name;
    private int userId;//用户 id
    private String date;// 统计日期
    private double value;//营业额


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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
