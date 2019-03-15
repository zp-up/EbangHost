package com.sctjsj.lazyhost.util;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.bean.CityBean;
import com.sctjsj.lazyhost.bean.DeliveryAddressBean;
import com.sctjsj.lazyhost.bean.DistrictBean;
import com.sctjsj.lazyhost.bean.GoodsBean;
import com.sctjsj.lazyhost.bean.OrderBean;
import com.sctjsj.lazyhost.bean.ProvinceBean;
import com.sctjsj.lazyhost.bean.ShopBean;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.bt.BluetoothService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_CONNECTED;

/**
 * Author:chris - jason
 * Date:2019/3/13.
 * Package:com.sctjsj.lazyhost.util
 */
//商家自动打印小票工具类
public class AutoPrintUtil {
    //等待打印的队列
    private volatile LinkedBlockingQueue<Integer> queue;
    private static Context context;
    private AutoPrintUtil() {
        queue = new LinkedBlockingQueue();
        //开启死循环来监测等待打印的队列
        new PrintThread().start();
    }

    public static AutoPrintUtil getInstance(Context c) {
        if(context==null){
            context = c.getApplicationContext();
        }
        return Holder.instance;
    }

    private static class Holder {
        private static final AutoPrintUtil instance = new AutoPrintUtil();
    }

    //加入等待打印的订单
    public synchronized void joinQueue(int rid) {
        try {
            if (!queue.contains(rid)) {
                queue.put(rid);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class PrintThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (queue.size() > 0) {
                    try {
                        //取出一个rid
                        Integer rid = queue.take();
                        if (rid != null && rid.intValue() != 0) {

                            //通过订单id，查询订单信息，自动打印
                            queryOrderInfo(rid);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void queryOrderInfo(int id) {
        RequestParams params = new RequestParams(BnUrl.singleSearchUrl);
        params.addBodyParameter("id", String.valueOf(id));
        params.addBodyParameter("ctype", "orderform");
        params.addBodyParameter("jf", "store|goodscart|goods|addr|area|parent");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(JSONObject response) {

                if (response != null) {
                    try {
                        JSONObject x = response.getJSONObject("data");
                        if (x != null) {
                            //每一条订单购买商品的集合
                            List<GoodsBean> gbList = new ArrayList<GoodsBean>();
                            String StoreOrderNo = x.getString("storeOrderNo");
                            //洗衣取送件时间
                            String distributionTime = x.getString("distributionTime");
                            String takeTime = x.getString("takeTime");

                            int typeIn = x.getInt("type");
                            //店铺名
                            String storeName = x.getJSONObject("store").getString("storeName");
                            int storeId = x.getJSONObject("store").getInt("id");
                            String storeAddress = x.getJSONObject("store").getString("storeAddress");
                            int type = x.getJSONObject("store").getInt("type");
                            ShopBean sb = new ShopBean();
                            sb.setId(storeId);
                            sb.setAddress(storeAddress);
                            sb.setName(storeName);
                            sb.setType(type);
                            //订单信息
                            int id = x.getInt("id");//订单 id
                            String name = x.getString("name");//给用户看的订单号
                            String buyerRemark = x.getString("buyerRemark");//买家备注
                            String paytime = x.getString("paytime");//下单时间
                            double shipPrice = x.getDouble("shipPrice");//运费

                            double totalPrice = x.getDouble("totalprice");//总价
                            double disValue = x.getDouble("disValue");//优惠金额
                            double packagePrice = x.getDouble("packagePrice");//包装费

                            int num = x.getInt("num");//商品总数量
                            int state = x.getInt("state");//订单状态
                            //购买的商品列表
                            JSONArray goodscart = x.getJSONArray("goodscart");
                            for (int j = 0; j < goodscart.length(); j++) {
                                JSONObject g = goodscart.getJSONObject(j);
                                int goodsId = g.getJSONObject("goods").getInt("id");//商品 id
//                                        String property = g.getJSONObject("goods").getString("name");//商品名称
//                                        String goodsName = g.getJSONObject("goods").getJSONObject("parent").getString("name");
                                int goodsNum = g.getInt("num");//商品数量
                                double price = g.getDouble("price");//单价
                                GoodsBean gb = new GoodsBean();
                                gb.setId(goodsId);

                                String goodsName = g.getJSONObject("goods").getInt("level") == 2 ? g.getJSONObject("goods").getString("name") : g.getJSONObject("goods").getString("name");
                                String specInfo = g.has("specInfo") ? g.getString("specInfo") : "";
                                gb.setName(goodsName + "    " + specInfo);
                                gb.setCount(goodsNum);
                                gb.setPrice(price);
                                gbList.add(gb);
                            }
                            DeliveryAddressBean dab = null;
                            String strAddr = x.getString("addr");
                            if (strAddr != null && !strAddr.equals("null")) {
                                //收货信息
                                JSONObject addr = new JSONObject(strAddr);

                                int deliveryId = addr.getInt("id");//收货地址 id
                                String recName = addr.getString("recName");//收货人
                                String consigneeTel = addr.getString("mobile");//收货人电话
                                String areaInfo = addr.getString("addressDetail");//详细地址

                                JSONObject area = addr.getJSONObject("area");//区
                                String districtName = area.getString("areaname");//区县名称
                                int districtId = area.getInt("id");//区县 id
                                DistrictBean districtBean = new DistrictBean();
                                districtBean.setId(districtId);
                                districtBean.setName(districtName);

                                JSONObject city = area.has("parent") ? area.getJSONObject("parent") : new JSONObject("{}");//市
                                String cityName = city.getString("areaname");//城市名
                                int cityId = city.getInt("id");//城市 id
                                CityBean cityBean = new CityBean();
                                cityBean.setId(cityId);
                                cityBean.setName(cityName);

                                JSONObject province = city.getJSONObject("parent");//省
                                String provinceName = province.getString("areaname");//省名
                                int provinceId = province.getInt("id");//省 id
                                ProvinceBean provinceBean = new ProvinceBean();
                                provinceBean.setId(provinceId);
                                provinceBean.setName(provinceName);

                                dab = new DeliveryAddressBean();
                                dab.setId(deliveryId);
                                dab.setProvinceBean(provinceBean);
                                dab.setCityBean(cityBean);
                                dab.setDistrictBean(districtBean);
                                dab.setConsignee(recName);
                                dab.setDetail(areaInfo);
                                dab.setTel(consigneeTel);
                            }

                            OrderBean ob = new OrderBean();
                            ob.setId(id);
                            ob.setName(name);
                            ob.setDeliveryAddressBean(dab);
                            ob.setState(state);

                            ob.setTotalprice(totalPrice);
                            ob.setDisvalue(disValue);
                            ob.setShopBean(sb);
                            ob.setNum(num);
                            ob.setType(typeIn);
                            ob.setBuyerRemark(buyerRemark);
                            ob.setShipPrice(shipPrice);
                            ob.setInsertTime(paytime);
                            ob.setGoodsBeanList(gbList);
                            ob.setSendTime(distributionTime);
                            ob.setTakeTime(takeTime);
                            ob.setPackagePrice(packagePrice);
                            ob.setStoreOrderNo(StoreOrderNo);



                            //printOrder(((MyApp)context).getBluetoothService(),1,ob);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
            }
        });
    }


    private void printOrder(BluetoothService bluetoothService,int count, OrderBean orderBean) {
        if (orderBean != null) {

            for (int x = 0; x < count; x++) {
                //title
                String start = "*** 懒购外卖 ***\n\n";
                bluetoothService.printCenter();
                bluetoothService.printSize(1);
                print(bluetoothService,start);

                //订单小号
                String  StoreOrderNo =orderBean.getStoreOrderNo()+"\n\n";
                bluetoothService.printCenter();
                bluetoothService.printSize(1);
                print(bluetoothService, StoreOrderNo);

                //店铺名字
                String storeName = orderBean.getShopBean().getName() + "\n\n";
                bluetoothService.printCenter();
                bluetoothService.printSize(1);
                print(bluetoothService, storeName);

                //订单号
                String orderNum = "订单号:" + orderBean.getName() + "\n\n";
                bluetoothService.printLeft();
                bluetoothService.printSize(0);
                print(bluetoothService,  orderNum);

                //下单时间
                String payTime = "支付时间:" + orderBean.getInsertTime() + "\n\n";
                bluetoothService.printLeft();
                bluetoothService.printSize(0);
                print(bluetoothService,  payTime);

                //循环商品信息
                List<GoodsBean> gbList = orderBean.getGoodsBeanList();
                double oriAll = 0;
                if (gbList != null && gbList.size() > 0) {
                    for (int i = 0; i < gbList.size(); i++) {
                        //商品名称
                        String goodsName = gbList.get(i).getName();
                        //购买数量
                        int goodsNum = gbList.get(i).getCount();
                        //原价
                        double goodsPrice = gbList.get(i).getPrice();
                        oriAll = oriAll + (goodsNum * goodsPrice);
                        //优惠价
                        double goodsDisPrice = gbList.get(i).getDisPrice();
                        String goods = goodsName + "    " + "*" + " " + goodsNum + "  " + goodsPrice + "\n\n";
                        bluetoothService.printLeft();
                        bluetoothService.printSize(0);
                        print(bluetoothService, goods);
                    }
                }

                //配送费
                String shipPrice = "配送费:" + orderBean.getShipPrice() + "\n\n";
                bluetoothService.printLeft();
                bluetoothService.printSize(0);
                print(bluetoothService, shipPrice);

                //原本应付的总额
                String oriTotal = "总额:" + (oriAll + orderBean.getShipPrice()) + "\n\n";
                bluetoothService.printLeft();
                bluetoothService.printSize(0);
                print(bluetoothService,  oriTotal);

                //优惠信息
                String disInfo = "优惠:" + orderBean.getDisvalue() + "\n\n";
                bluetoothService.printLeft();
                bluetoothService.printSize(0);
                print(bluetoothService,  disInfo);


                //总价
                String totalPay = "实付:" + orderBean.getTotalprice() + "\n\n";
                bluetoothService.printLeft();
                bluetoothService.printSize(2);
                print(bluetoothService, totalPay);

                //收货信息
                String address = orderBean.getDeliveryAddressBean().getProvinceBean().getName() +
                        orderBean.getDeliveryAddressBean().getCityBean().getName() +
                        orderBean.getDeliveryAddressBean().getDistrictBean().getName() +
                        orderBean.getDeliveryAddressBean().getDetail() + "\n\n";

                bluetoothService.printLeft();
                bluetoothService.printSize(2);
                print(bluetoothService, address);

                String consignee = "收货人:" + orderBean.getDeliveryAddressBean().getConsignee() + "\n\n";
                bluetoothService.printLeft();
                bluetoothService.printSize(2);
                print(bluetoothService, consignee);

                String tel = "联系电话:" + orderBean.getDeliveryAddressBean().getTel() + "\n\n\n\n";
                bluetoothService.printLeft();
                bluetoothService.printSize(2);
                print(bluetoothService, tel);

                //备注信息
                String remarkInfo = "备注:" + orderBean.getBuyerRemark() + "\n\n\n\n";
                bluetoothService.printLeft();
                bluetoothService.printSize(2);
                print(bluetoothService, remarkInfo);

                String end = "*** END ***\n\n\n\n\n";
                bluetoothService.printCenter();
                bluetoothService.printSize(1);
                print(bluetoothService,  end);
            }

        } else {
            //Toast.makeText(act, "打印信息有误", Toast.LENGTH_SHORT).show();
        }

    }

    private void print(BluetoothService bluetoothService,String message){

        if (bluetoothService == null) {
            return;
        }

        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != STATE_CONNECTED) {
            //Toast.makeText(context, "蓝牙未连接", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send;
            try {
                send = message.getBytes("GB2312");
            } catch (UnsupportedEncodingException e) {
                send = message.getBytes();
            }

            bluetoothService.write(send);
        }
    }



}
