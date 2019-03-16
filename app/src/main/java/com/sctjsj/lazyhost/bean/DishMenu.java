package com.sctjsj.lazyhost.bean;

import java.util.ArrayList;

/**
 * Created by cheng on 16-11-10.
 */
public class DishMenu {
    private int dishMenuId;

    public int getDishMenuId() {
        return dishMenuId;
    }

    public void setDishMenuId(int dishMenuId) {
        this.dishMenuId = dishMenuId;
    }

    private String menuName;
    private ArrayList<Dish> dishList;

    public DishMenu(){

    }

    public DishMenu(String menuName, ArrayList dishList){
        this.menuName = menuName;
        this.dishList = dishList;
    }

    public ArrayList<Dish> getDishList() {
        return dishList;
    }

    public void setDishList(ArrayList<Dish> dishList) {
        this.dishList = dishList;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

}