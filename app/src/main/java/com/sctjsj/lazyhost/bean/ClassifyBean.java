package com.sctjsj.lazyhost.bean;

import java.io.Serializable;

/**
 * Created by mayikang on 16/12/13.
 */

public class ClassifyBean implements Serializable{
    private int id;
    private String name;
    private int level;
    private boolean selected;
    private int display;//是否展示 1：是 2：否

    public int getDisplay() {
        return display;
    }

    public void setDisplay(int display) {
        this.display = display;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
