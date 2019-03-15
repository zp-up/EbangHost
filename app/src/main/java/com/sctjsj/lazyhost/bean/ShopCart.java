package com.sctjsj.lazyhost.bean;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cheng on 16-11-12.
 */
public class ShopCart {
    private int shoppingAccount;//商品总数
    private double shoppingTotalPrice;//商品总价钱
    private Map<Dish,Integer> shoppingSingle;//单个物品,价钱
    //private Map<String,String>shoppingSingle;
    private List<Dish> dishList;//购物车中的商品列表

    public ShopCart(){
        this.shoppingAccount = 0;
        this.shoppingTotalPrice = 0;
        this.shoppingSingle = new HashMap<>();
        this.dishList=new ArrayList<>();
    }

    /**
     * 返回购物车商品总数量
     * @return
     */
    public int getShoppingAccount() {
        return shoppingAccount;
    }

    /**
     * 返回购物车商品总价格
     * @return
     */
    public double getShoppingTotalPrice() {
        return shoppingTotalPrice;
    }



    public Map<Dish, Integer> getShoppingSingleMap() {
        return shoppingSingle;
    }

    /**
     * 往购物车中添加商品
     * @param dish
     * @return
     */
    public boolean addShoppingSingle(Dish dish){
        int remain = dish.getDishRemain();
        if(remain<=0)
            return false;
        dish.setDishRemain(--remain);
        int num = 0;
        if(shoppingSingle.containsKey(dish)){
            num = shoppingSingle.get(dish);
        }
        num+=1;
        shoppingSingle.put(dish,num);
        //添加到列表中
         dishList.add(dish);

        shoppingTotalPrice += dish.getCurrentPrice();
        shoppingAccount++;
        return true;
    }

    /**
     * 从购物车中删除商品
     * @param dish
     * @return
     */
    public boolean subShoppingSingle(Dish dish){
        int num = 0;
        if(shoppingSingle.containsKey(dish)){
            num = shoppingSingle.get(dish);
        }
        if(num<=0) return false;
        num--;
        int remain = dish.getDishRemain();
        dish.setDishRemain(++remain);
        shoppingSingle.put(dish,num);
        //从列表移除
        dishList.remove(dish);
        shoppingTotalPrice -= dish.getCurrentPrice();
        shoppingAccount--;
        return true;
    }

    public List<Dish> getDishList(){
        return dishList;
    }
}
