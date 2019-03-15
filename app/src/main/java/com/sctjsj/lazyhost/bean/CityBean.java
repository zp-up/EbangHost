package com.sctjsj.lazyhost.bean;

import java.io.Serializable;

/**
 * Created by mayikang on 16/12/7.
 */

public class CityBean implements Serializable{
    private int id;//城市 id
    private String name;//城市名

    public CityBean() {
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
}
