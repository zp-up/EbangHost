package com.sctjsj.lazyhost.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bumptech.glide.Glide;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.activity.IndexActivity;
import com.sctjsj.lazyhost.activity.LoginActivity;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.bean.CityBean;
import com.sctjsj.lazyhost.bean.DeliveryAddressBean;
import com.sctjsj.lazyhost.bean.DistrictBean;
import com.sctjsj.lazyhost.bean.GoodsBean;
import com.sctjsj.lazyhost.bean.OrderBean;
import com.sctjsj.lazyhost.bean.ProvinceBean;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.AnimationUtil;
import com.sctjsj.lazyhost.util.ListViewUtil;
import com.sctjsj.lazyhost.util.LogUtil;
import com.sctjsj.lazyhost.util.NumformatUtil;
import com.sctjsj.lazyhost.util.ProgressUtil;
import com.sctjsj.lazyhost.util.bt.BluetoothService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.RefreshHandler;

import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_NONE;

/**
 * Created by Chris-Jason on 2016/11/9.
 */
public class WaitOrderFg extends Fragment {
    @Bind(R.id.fg_order_dispose_refresh_layout)
    QRefreshLayout refreshLayout;
    @Bind(R.id.fg_order_dispose_rv)
    RecyclerView mRV;
    @Bind(R.id.fg_today_order_rl_none_remind)
    RelativeLayout mRLNone;
    @Bind(R.id.remind)
    TextView tvRemind;
    private int pageIndex = 1;
    private MyApp app;
    private List<OrderBean> data;
    private Handler mHandler;
    private ProgressUtil pUtil;
    private MyAdapter adapter;
    //定位
    private AMapLocationClient mLocationClient;
    private AMapLocationListener mLocationListener;
    private AMapLocationClientOption mLocationClientOption;
    private double longT;
    private double lanT;

    private IndexActivity act;

    //订单自动加载
    private volatile boolean isLoading = false;
    private LinearLayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_wait_order, null);
        ButterKnife.bind(this, view);
        app = (MyApp) getActivity().getApplication();
        act = (IndexActivity) getActivity();
        mLayoutManager = new LinearLayoutManager(getActivity());
        if (adapter == null) {
            adapter = new MyAdapter();
            mRV.setAdapter(adapter);
            mRV.setLayoutManager(mLayoutManager);
        }
        pUtil = new ProgressUtil(getActivity());
        data = new ArrayList<>();
        refreshLayout.setLoadMoreEnable(true);
        refreshLayout.setRefreshHandler(new RefreshHandler() {
            @Override
            public void onRefresh(QRefreshLayout refresh) {
                initData();
            }

            @Override
            public void onLoadMore(QRefreshLayout refresh) {
                loadMoreData();
            }
        });

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 99:
                        initData();
                        break;
                }
            }
        };

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);//自定义的code
        } else {
            //启动定位
            initLocation();
        }

        //实现自动加载订单
//        mRV.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                int lastCompletelyVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
//                int itemCount = mLayoutManager.getItemCount();
//
//                //判断滑动方向
//                if (dy > 0 && lastCompletelyVisibleItemPosition + 3 > itemCount) {
//                    if (!isLoading) {
//                        isLoading = true;
//                        loadMoreData();
//                    }
//                }
//            }
//        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    /**
     * 高德定位
     */
    private void initLocation() {
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(getActivity());
        }
        //初始化定位监听器
        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    //获取定位错误码
                    int errorCode = aMapLocation.getErrorCode();
                    switch (errorCode) {
                        //定位成功
                        case 0:
                            /**
                             * 解析地址
                             */
                            //当前经纬度
                            longT = aMapLocation.getLongitude();
                            lanT = aMapLocation.getLatitude();
                            LogUtil.e("经度：" + longT);
                            mHandler.sendEmptyMessage(99);
                            break;
                        //网络链路异常
                        case 4:
                            Toast.makeText(getActivity(), "定位失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                            break;
                        //定位客户端初始化失败
                        case 9:
                            Toast.makeText(getActivity(), "定位失败，请重启定位客户端", Toast.LENGTH_SHORT).show();
                            break;
                        //无定位权限
                        case 12:
                            Toast.makeText(getActivity(), "定位失败，请检查定位权限", Toast.LENGTH_LONG).show();
                            break;
                    }

                } else {
                    LogUtil.e("aMapLocation==null");
                }
            }
        };
        //定位客户端设置定位监听器
        mLocationClient.setLocationListener(mLocationListener);
        /**
         * 配置定位参数
         */
        mLocationClientOption = new AMapLocationClientOption();
        //设置定位模式：高精度定位模式
        mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息
        mLocationClientOption.setNeedAddress(true);
        //设置是否指定位一次
        mLocationClientOption.setOnceLocation(false);
        //如果设置了只定位一次，将获取3s内经度最高的一次定位结果
        if (mLocationClientOption.isOnceLocation()) {
            mLocationClientOption.setOnceLocationLatest(true);
        }
        //设置是否强制刷新wifi
        mLocationClientOption.setWifiActiveScan(true);
        //设置是否允许模拟位置
        mLocationClientOption.setMockEnable(false);
        //设置定位时间间隔3min
        mLocationClientOption.setInterval(1000 * 60 * 3);
        mLocationClient.setLocationOption(mLocationClientOption);
        mLocationClient.startLocation();
    }

    /**
     * 初始化订单列表
     */

    public void initData() {
        try {
            if (app == null){
                if (getActivity() == null){
                    return;
                }
                app = (MyApp) getActivity().getApplication();
            }
            if (app.getCurrentUser() == null) {
                Toast.makeText(getActivity(), "登录超时,请重新登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return;
            }
            data.clear();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            pageIndex = 1;
            RequestParams params = new RequestParams(BnUrl.selfDeliveryOrderUrl);
            params.setUseCookie(false);
            params.addBodyParameter("pageIndex", String.valueOf(pageIndex));
            params.addBodyParameter("userId", app.getCurrentUser().getUserId()+"");
            params.addBodyParameter("jf", "goodscart|goods|addr|area|parent");
            x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
                @Override
                public void onWaiting() {

                }

                @Override
                public void onStarted() {
                    pUtil.showProgress(false);
                }

                @Override
                public void onLoading(long total, long current, boolean isDownloading) {

                }

                @Override
                public void onSuccess(JSONObject response) {

                    if (response != null) {
                        Log.e("TAG", "订单:" + response.toString());
                        try {
                            JSONArray resultList = response.getJSONArray("resultList");
                            if (resultList != null && resultList.length() > 0) {
                                //循环订单的条数
                                for (int i = 0; i < resultList.length(); i++) {

                                    //每一条订单购买商品的集合
                                    List<GoodsBean> gbList = new ArrayList<GoodsBean>();
                                    JSONObject x = resultList.getJSONObject(i);
                                    String storeOrderNo=x.getString("storeOrderNo");
                                    String distributionTime = x.getString("distributionTime");
                                    String takeTime = x.getString("takeTime");

                                    //订单信息
                                    int id = x.getInt("id");//订单 id
                                    String name = x.getString("name");//给用户看的订单号
                                    String buyerRemark = x.getString("buyerRemark");//买家备注
                                    String paytime = x.getString("paytime");//下单时间

                                    double disValue = x.getDouble("disValue");//优惠
                                    double shipPrice = x.getDouble("shipPrice");//运费
                                    double totalPrice = x.getDouble("totalprice");//总价
                                    double packagePrice = x.getDouble("packagePrice");//包装费
                                    int num = x.getInt("num");//商品总数量
                                    int state = x.getInt("state");//订单状态
                                    int type = x.getInt("type");
                                    //购买的商品列表
                                    JSONArray goodscart = x.getJSONArray("goodscart");
                                    for (int j = 0; j < goodscart.length(); j++) {
                                        JSONObject g = goodscart.getJSONObject(j);
                                        int goodsId = g.getJSONObject("goods").getInt("id");//商品 id
                                        //String goodsName = g.getJSONObject("goods").getString("name");//商品名称
                                        int goodsNum = g.getInt("num");//商品数量
                                        double price = g.getDouble("price");//单价
                                        GoodsBean gb = new GoodsBean();
                                        gb.setId(goodsId);
                                        String goodsName = g.getJSONObject("goods").getInt("level") == 2?g.getJSONObject("goods").getString("name"):g.getJSONObject("goods").getString("name");
                                        String specInfo = g.getString("specInfo");
                                        gb.setName(goodsName+"    "+specInfo);
                                        gb.setCount(goodsNum);
                                        gb.setPrice(price);
                                        gbList.add(gb);
                                    }

                                    String strAddr = x.getString("addr");
                                    DeliveryAddressBean dab = null;
                                    //二维码订单没有收货地址
                                    if (strAddr != null && !strAddr.equals("null")) {
                                        JSONObject addr = new JSONObject(strAddr);
                                        //收货信息
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

                                        JSONObject city = area.getJSONObject("parent");//市
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
                                    ob.setType(type);
                                    ob.setName(name);
                                    ob.setDeliveryAddressBean(dab);
                                    ob.setState(state);
                                    ob.setTotalprice(totalPrice);
                                    ob.setNum(num);
                                    ob.setDisvalue(disValue);
                                    ob.setBuyerRemark(buyerRemark);
                                    ob.setShipPrice(shipPrice);
                                    ob.setInsertTime(paytime);
                                    ob.setGoodsBeanList(gbList);
                                    ob.setTakeTime(takeTime);
                                    ob.setPackagePrice(packagePrice);
                                    ob.setSendTime(distributionTime);
                                    ob.setStoreOrderNo( storeOrderNo);

                                    data.add(ob);
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            if (data.size() <= 0) {
                                mRLNone.setVisibility(View.VISIBLE);
                            } else {
                                mRLNone.setVisibility(View.GONE);
                            }
                            pageIndex++;
                            initRVAdapter();
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
                    pUtil.dismissProgress();
                    refreshLayout.LoadMoreComplete();
                    refreshLayout.refreshComplete();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRVAdapter() {
        if (adapter == null) {
            adapter = new MyAdapter();
            mRV.setAdapter(adapter);
            mRV.setLayoutManager(mLayoutManager);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 上啦加载
     */
    public void loadMoreData() {
        Log.e("TAG","页数:"+pageIndex);
        if (app.getCurrentUser() == null) {
            Toast.makeText(getActivity(), "登录超时,请重新登录", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return;
        }
        RequestParams params = new RequestParams(BnUrl.selfDeliveryOrderUrl);
        params.setUseCookie(false);
        params.addBodyParameter("pageIndex", String.valueOf(pageIndex));
        params.addBodyParameter("userId", app.getCurrentUser().getUserId()+"");
        params.addBodyParameter("jf", "goodscart|goods|addr|area|parent");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                pUtil.showProgress(false);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (response != null) {
                    try {
                        JSONArray resultList = response.getJSONArray("resultList");
                        if (resultList != null && resultList.length() > 0) {
                            //循环订单的条数
                            for (int i = 0; i < resultList.length(); i++) {
                                //每一条订单购买商品的集合
                                List<GoodsBean> gbList = new ArrayList<GoodsBean>();
                                JSONObject x = resultList.getJSONObject(i);
                                String storeOrderNo = x.getString("storeOrderNo");
                                String distributionTime = x.getString("distributionTime");
                                String takeTime = x.getString("takeTime");
                                //订单信息
                                int id = x.getInt("id");//订单 id
                                String name = x.getString("name");//给用户看的订单号
                                String buyerRemark = x.getString("buyerRemark");//买家备注
                                String paytime = x.getString("paytime");//下单时间


                                double disvalue = x.getDouble("disValue");
                                double shipPrice = x.getDouble("shipPrice");//运费
                                double totalPrice = x.getDouble("totalprice");//总价
                                double packagePrice = x.getDouble("packagePrice");//包装费
                                int num = x.getInt("num");//商品总数量
                                int state = x.getInt("state");//订单状态
                                int type = x.getInt("type");
                                //购买的商品列表
                                JSONArray goodscart = x.getJSONArray("goodscart");
                                for (int j = 0; j < goodscart.length(); j++) {
                                    JSONObject g = goodscart.getJSONObject(j);
                                    int goodsId = g.getJSONObject("goods").getInt("id");//商品 id
                                    //String goodsName = g.getJSONObject("goods").getString("name");//商品名称
                                    int goodsNum = g.getInt("num");//商品数量
                                    double price = g.getDouble("price");//单价
                                    GoodsBean gb = new GoodsBean();
                                    gb.setId(goodsId);
                                    String goodsName = g.getJSONObject("goods").getInt("level") == 2?g.getJSONObject("goods").getString("name"):g.getJSONObject("goods").getString("name");
                                    String specInfo = g.getString("specInfo");
                                    gb.setName(goodsName+"    "+specInfo);
                                    gb.setCount(goodsNum);
                                    gb.setPrice(price);
                                    gbList.add(gb);
                                }

                                String strAddr = x.getString("addr");
                                DeliveryAddressBean dab = null;
                                //二维码订单没有收货地址
                                if (strAddr != null && !strAddr.equals("null")) {
                                    JSONObject addr = new JSONObject(strAddr);
                                    //收货信息
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

                                    JSONObject city = area.getJSONObject("parent");//市
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
                                ob.setType(type);
                                ob.setId(id);
                                ob.setName(name);
                                ob.setDeliveryAddressBean(dab);
                                ob.setState(state);
                                ob.setTotalprice(totalPrice);
                                ob.setNum(num);
                                ob.setBuyerRemark(buyerRemark);
                                ob.setShipPrice(shipPrice);
                                ob.setInsertTime(paytime);
                                ob.setGoodsBeanList(gbList);
                                ob.setDisvalue(disvalue);
                                ob.setTakeTime(takeTime);
                                ob.setPackagePrice(packagePrice);
                                ob.setSendTime(distributionTime);
                                ob.setStoreOrderNo(storeOrderNo);
                                data.add(ob);
                            }

                        } else {
                            tvRemind.setVisibility(View.VISIBLE);
                            tvRemind.setAnimation(AnimationUtil.getShowAlphaAnimation());

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tvRemind.setAnimation(AnimationUtil.getHiddenAlphaAnimation());
                                    tvRemind.setVisibility(View.GONE);
                                }
                            }, 2000);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if (data.size() <= 0) {
                            mRLNone.setVisibility(View.VISIBLE);
                        } else {
                            mRLNone.setVisibility(View.GONE);
                        }
                        pageIndex++;
                        initRVAdapter();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                pUtil.dismissProgress();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                pUtil.dismissProgress();
            }

            @Override
            public void onFinished() {
                isLoading = false;
                pUtil.dismissProgress();
                refreshLayout.LoadMoreComplete();
                refreshLayout.refreshComplete();
            }
        });
    }

    public void search(String msg){
        data.clear();
        pageIndex=1;

        RequestParams params = new RequestParams(BnUrl.selfDeliveryOrderUrl);
        params.setUseCookie(false);
        params.addBodyParameter("pageSize","999");
        params.addBodyParameter("pageIndex", String.valueOf(1));
        params.addBodyParameter("userId", app.getCurrentUser().getUserId()+"");
        params.addBodyParameter("name",msg);
        params.addBodyParameter("jf", "goodscart|goods|addr|area|parent");

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                pUtil.showProgress(false);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(JSONObject response) {

                if (response != null) {
                    Log.e("TAG", "订单:" + response.toString());
                    try {
                        JSONArray resultList = response.getJSONArray("resultList");
                        if (resultList != null && resultList.length() > 0) {
                            //循环订单的条数
                            for (int i = 0; i < resultList.length(); i++) {

                                //每一条订单购买商品的集合
                                List<GoodsBean> gbList = new ArrayList<GoodsBean>();
                                JSONObject x = resultList.getJSONObject(i);
                                String storeOrderNo=x.getString("storeOrderNo");
                                String distributionTime = x.getString("distributionTime");
                                String takeTime = x.getString("takeTime");

                                //订单信息
                                int id = x.getInt("id");//订单 id
                                String name = x.getString("name");//给用户看的订单号
                                String buyerRemark = x.getString("buyerRemark");//买家备注
                                String paytime = x.getString("paytime");//下单时间

                                double disValue = x.getDouble("disValue");//优惠
                                double shipPrice = x.getDouble("shipPrice");//运费
                                double totalPrice = x.getDouble("totalprice");//总价
                                double packagePrice = x.getDouble("packagePrice");//包装费
                                int num = x.getInt("num");//商品总数量
                                int state = x.getInt("state");//订单状态
                                int type = x.getInt("type");
                                //购买的商品列表
                                JSONArray goodscart = x.getJSONArray("goodscart");
                                for (int j = 0; j < goodscart.length(); j++) {
                                    JSONObject g = goodscart.getJSONObject(j);
                                    int goodsId = g.getJSONObject("goods").getInt("id");//商品 id
                                    //String goodsName = g.getJSONObject("goods").getString("name");//商品名称
                                    int goodsNum = g.getInt("num");//商品数量
                                    double price = g.getDouble("price");//单价
                                    GoodsBean gb = new GoodsBean();
                                    gb.setId(goodsId);
                                    String goodsName = g.getJSONObject("goods").getInt("level") == 2?g.getJSONObject("goods").getString("name"):g.getJSONObject("goods").getString("name");
                                    String specInfo = g.getString("specInfo");
                                    gb.setName(goodsName+"    "+specInfo);
                                    gb.setCount(goodsNum);
                                    gb.setPrice(price);
                                    gbList.add(gb);
                                }

                                String strAddr = x.getString("addr");
                                DeliveryAddressBean dab = null;
                                //二维码订单没有收货地址
                                if (strAddr != null && !strAddr.equals("null")) {
                                    JSONObject addr = new JSONObject(strAddr);
                                    //收货信息
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

                                    JSONObject city = area.getJSONObject("parent");//市
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
                                ob.setType(type);
                                ob.setName(name);
                                ob.setDeliveryAddressBean(dab);
                                ob.setState(state);
                                ob.setTotalprice(totalPrice);
                                ob.setNum(num);
                                ob.setDisvalue(disValue);
                                ob.setBuyerRemark(buyerRemark);
                                ob.setShipPrice(shipPrice);
                                ob.setInsertTime(paytime);
                                ob.setGoodsBeanList(gbList);
                                ob.setTakeTime(takeTime);
                                ob.setPackagePrice(packagePrice);
                                ob.setSendTime(distributionTime);
                                ob.setStoreOrderNo( storeOrderNo);

                                data.add(ob);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if (data.size() <= 0) {
                            mRLNone.setVisibility(View.VISIBLE);
                        } else {
                            mRLNone.setVisibility(View.GONE);
                        }
                        pageIndex++;
                        initRVAdapter();
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
                pUtil.dismissProgress();
                refreshLayout.LoadMoreComplete();
                refreshLayout.refreshComplete();
            }
        });

    }

    class MyAdapter extends RecyclerView.Adapter<XHolder> {

        @Override
        public XHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(getActivity()).inflate(R.layout.item_of_fg_wait_order, null);
            XHolder holder = new XHolder(item);
            return holder;
        }

        @Override
        public void onBindViewHolder(XHolder holder, final int position) {

            holder.storeNum.setText("商家简号:"+(TextUtils.isEmpty( data.get(position).getStoreOrderNo())?"":data.get(position).getStoreOrderNo()) );

            //默认打印2份
            final int[] printCount = {-1};
            //打印订单
            holder.mBtnPrintOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //弹出选择框，让商家选择打印订单的分数
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle("请选择打印份数");
                    String[] items = new String[]{"打印1份", "打印2份"};
                    builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //打印一份
                            if (0 == which) {
                                printCount[0] = 1;
                            }
                            //打印两份
                            if (1 == which) {
                                printCount[0] = 2;
                            }

                        }
                    });
                    builder.setPositiveButton("确认打印", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (printCount[0] < 1) {
                                Toast.makeText(getActivity(), "请选择打印份数", Toast.LENGTH_SHORT).show();
                            } else {
                                printOrder(printCount[0], data.get(position));
                            }

                            printCount[0] = -1;
                        }
                    });
                    builder.show();
                }
            });

            if (3 == app.getCurrentUser().getShopType()) {
                holder.mLLTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(data.get(position).getSendTime());
                holder.tvTakeTime.setText(data.get(position).getTakeTime());
            } else {
                holder.mLLTime.setVisibility(View.GONE);
            }

            //二维码订单没有收货地址
            if (data.get(position).getDeliveryAddressBean() != null) {
                //设置收货地址
                holder.mTVAddress.setText(data.get(position).getDeliveryAddressBean().getDetail());
                //设置收货人
                holder.mTVConsignee.setText(data.get(position).getDeliveryAddressBean().getConsignee() + "  " +
                        data.get(position).getDeliveryAddressBean().getTel());

            } else {

                holder.mLLAddressParent.setVisibility(View.GONE);
                holder.mRLNameAndTel.setVisibility(View.GONE);
            }

            //查看购买者信息
            holder.mTVQueryBuyer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data != null && data.size() > 0 && data.get(position).getId() != 0) {
                        queryBuyerDetail(String.valueOf(data.get(position).getId()));
                    }
                }
            });

            //设置订单状态
            switch (data.get(position).getState()) {
                //订单已经失效
                case 1:
                    holder.mTVGeneralRemind.setText("订单已经失效");
                    break;
                //等待买家付款
                case 5:
                    holder.mTVGeneralRemind.setText("等待买家付款");
                    break;

                //买家已付款
                case 10:
                    holder.mTVGeneralRemind.setText("买家已付款");
                    break;

                //买家申请退款
                case 11:
                    holder.mTVGeneralRemind.setText("买家申请退款");
                    break;

                //店家同意付款
                case 12:
                    holder.mTVGeneralRemind.setText("商家同意退款");
                    break;

                //退款失败
                case 13:
                    holder.mTVGeneralRemind.setText("退款失败");
                    break;

                //退款成功
                case 14:
                    holder.mTVGeneralRemind.setText("退款成功");
                    break;

                //等待商家接单
                case 15:
                    holder.mTVGeneralRemind.setText("等待商家接单");
                    break;

                //商家拒绝接单
                case 16:
                    holder.mTVGeneralRemind.setText("商家拒绝");
                    break;

                //商家已接单
                case 17:
                    holder.mTVGeneralRemind.setText("商家已接单");
                    break;

                //商品配送中
                case 18:
                    holder.mTVGeneralRemind.setText("正在配送中");
                    break;

                //商品已送达
                case 19:
                    holder.mTVGeneralRemind.setText("已送达");
                    break;
                //已消费
                case 20:
                    holder.mTVGeneralRemind.setText("买家已消费");
                    break;
                //等待骑手接单
                case 21:
                    holder.mTVGeneralRemind.setText("等待骑手接单");
                    break;
                //等待配送
                case 22:
                    holder.mTVGeneralRemind.setText("等待骑手取货");
                    break;

                //已下单,送水/洗衣 提交订单后的状态
                case 30:
                    holder.mTVGeneralRemind.setText("已下单");
                    break;

                //已收件,店家更新，录入衣服信息到订单
                case 40:
                    holder.mTVGeneralRemind.setText("已收件");
                    break;

                default:
                    holder.mTVGeneralRemind.setText("订单状态错误" + data.get(position).getState());
                    break;
            }

            //点击确认送达
            holder.mBtnConfirmSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final SweetAlertDialog dialog = new SweetAlertDialog(getActivity(),SweetAlertDialog.WARNING_TYPE);
                    dialog.setCancelable(true);
                    dialog.setTitleText("确认送达？");
                    dialog.setContentText("请在配送结束后点击~");
                    dialog.setCancelText("取消");
                    dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {

                        }
                    });
                    dialog.setConfirmText("确认");
                    dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            confirmSend(data.get(position).getId());
                            dialog.dismissWithAnimation();
                        }
                    });
                    dialog.show();
                }
            });

            //设置下单时间
            holder.mTVPaytime.setText(data.get(position).getInsertTime());
            //设置订单编号
            holder.mTVOrderNum.setText(data.get(position).getName());
            //设置购买的商品列表
            MyLVAdapter myLVAdapter = new MyLVAdapter(data.get(position).getGoodsBeanList());
            holder.mLV.setAdapter(myLVAdapter);
            ListViewUtil.setListViewHeightBasedOnChildren(holder.mLV);
            //设置商品价格
            double countPrice = 0;
            for (int i = 0; i < data.get(position).getGoodsBeanList().size(); i++) {
                countPrice += data.get(position).getGoodsBeanList().get(i).getPrice();
            }
            //  小计
            holder.mTVGoodsPay.setText("￥" + NumformatUtil.save2(countPrice));
            //优惠
            holder.mTVDisprice.setText("￥" + NumformatUtil.save2(data.get(position).getDisvalue()));
            //配送费
            holder.mTVShipprice.setText("￥" + NumformatUtil.save2(data.get(position).getShipPrice()));
            //本单收入
            holder.mTVTotalPay.setText("￥" + NumformatUtil.save2(data.get(position).getTotalprice()));
            holder.packagePrice.setText("￥" + NumformatUtil.save2(data.get(position).getPackagePrice()));
            if (data.get(position).getType() == 1) {
                holder.ivError.setVisibility(View.VISIBLE);
            } else {
                holder.ivError.setVisibility(View.GONE);
            }
            holder.tvRemark.setText( data.get(position).getBuyerRemark() == null || TextUtils.isEmpty(data.get(position).getBuyerRemark()) ? "暂无特殊要求" : data.get(position).getBuyerRemark());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    //订单确认送达
    private void confirmSend(int id) {
        RequestParams params = new RequestParams(BnUrl.updateOrderUrl);
        params.setUseCookie(false);
        params.addBodyParameter("ofId", id+"");
        params.addBodyParameter("statu", "6");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                if(result!=null){
                    try {
                        boolean res = result.getBoolean("result");
                        //收货成功
                        if(res){
                            initData();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                pUtil.dismissProgress();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                pUtil.dismissProgress();
            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                pUtil.showProgress(false);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });
    }

    class XHolder extends ViewHolder {
        TextView mTVAddress, mTVConsignee, mTVPaytime, mTVOrderNum, mTVShipprice,
                mTVGoodsPay, mTVTotalPay, mTVDisprice, mTVQueryBuyer, mTVGeneralRemind;
        ListView mLV;
        RelativeLayout mRLNameAndTel;
        LinearLayout mLLAddressParent;
        RelativeLayout mRLGeneralRemindParent;
        LinearLayout mLLTime;
        TextView tvTakeTime, tvSendTime;
        TextView packagePrice;
        ImageView ivError;
        TextView storeNum;
        TextView tvRemark;
        Button mBtnPrintOrder;
        Button mBtnConfirmSend;
        public XHolder(View itemView) {
            super(itemView);
            mBtnPrintOrder = (Button) itemView.findViewById(R.id.item_of_fg_dispose_order_print_order_info);
            mBtnConfirmSend = itemView.findViewById(R.id.btn_confirm_send);
            storeNum = itemView.findViewById(R.id.storeNum);
            mTVDisprice = (TextView) itemView.findViewById(R.id.item_of_fg_all_tv_disprice);
            mTVAddress = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_delivery_address);
            mTVConsignee = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_consignee);
            mTVPaytime = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_paytime);
            mTVOrderNum = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_order_num);
            mLV = (ListView) itemView.findViewById(R.id.item_of_fg_dispose_lv_goods_list);
            mTVShipprice = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_shipprice);
            mTVGoodsPay = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_goods_pay);
            mTVTotalPay = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_total_pay);

            mTVQueryBuyer = (TextView) itemView.findViewById(R.id.item_all_tv_query_buyer);
            mLLAddressParent = (LinearLayout) itemView.findViewById(R.id.item_of_fg_all_order_ll_address);
            mRLNameAndTel = (RelativeLayout) itemView.findViewById(R.id.item_of_fg_all_order_rl_name_and_tel);

            mRLGeneralRemindParent = (RelativeLayout) itemView.findViewById(R.id.item_fg_dispose_rl_general_remind_parent);
            mTVGeneralRemind = (TextView) itemView.findViewById(R.id.item_fg_dispose_tv_general_remind);

            mLLTime = (LinearLayout) itemView.findViewById(R.id.take_and_delivery_ll_parent);
            tvSendTime = (TextView) itemView.findViewById(R.id.tv_send_time);
            tvTakeTime = (TextView) itemView.findViewById(R.id.tv_take_time);
            packagePrice = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_packagePrice);
            ivError = (ImageView) itemView.findViewById(R.id.iv_error);
            tvRemark = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_remark);
        }
    }

    class MyLVAdapter extends BaseAdapter {
        List<GoodsBean> gbList;

        MyLVAdapter(List<GoodsBean> gbList) {
            this.gbList = gbList;
        }

        @Override
        public int getCount() {
            return gbList.size();
        }

        @Override
        public Object getItem(int position) {
            return gbList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Holder h;

            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_goods_list_in_order, null);
                h = new Holder();

                h.mTVName = (TextView) convertView.findViewById(R.id.item_goods_list_in_order_tv_name);
                h.mTVCount = (TextView) convertView.findViewById(R.id.item_goods_list_in_order_tv_count);
                h.mTVPrice = (TextView) convertView.findViewById(R.id.item_goods_list_in_order_tv_price);
                convertView.setTag(h);
            } else {
                h = (Holder) convertView.getTag();
            }

            /**
             * 设置数据
             */

            h.mTVName.setText(gbList.get(position).getName());
            h.mTVCount.setText("*" + gbList.get(position).getCount());
            h.mTVPrice.setText("小计:￥" + NumformatUtil.save2(gbList.get(position).getPrice()));


            return convertView;
        }

        class Holder {
            TextView mTVName, mTVCount, mTVPrice;
        }

    }


    /**
     * 查看买家信息
     *
     * @param ofId
     */
    public void queryBuyerDetail(String ofId) {
        RequestParams params = new RequestParams(BnUrl.selfDeliveryOrderUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie", app.getSpf().getString("cookie", ""));
        params.addBodyParameter("ofId", ofId);
        params.addBodyParameter("jf", "photo");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                pUtil.showProgress(false);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (response != null) {
                    try {
                        JSONObject user = response.getJSONObject("user");
                        int id = user.getInt("id");//用户 id
                        String username = user.getString("username");//用户名
                        String phone = user.getString("phone");//电话
                        String url = user.getJSONObject("photo").getString("url");

                        View v = LayoutInflater.from(getActivity()).inflate(R.layout.layout_order_of_buyer, null);
                        ImageView mIV = (ImageView) v.findViewById(R.id.layout_order_of_buyer_iv_icon);
                        TextView mTVName = (TextView) v.findViewById(R.id.layout_order_of_buyer_tv_name);
                        TextView mTVTel = (TextView) v.findViewById(R.id.layout_order_of_buyer_tv_tel);

                        mTVName.setText(username);
                        mTVTel.setText("Tel:" + phone);
                        Glide.with(getActivity()).load(url).error(R.mipmap.icon_portrait_load_failed).into(mIV);

                        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(getActivity());
                        b.setTitle("购买者信息");
                        b.setView(v);
                        b.show();

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
                pUtil.dismissProgress();
            }
        });
    }

    private void printOrder(int count, OrderBean orderBean) {

        if (orderBean != null) {
            BluetoothService service = act.getBTService();
            if(service==null){
                app.setSocketState(STATE_NONE);
                return;
            }

            for (int x = 0; x < count; x++) {
                //title
                String start = "*** 懒购外卖 ***\n\n";
                service.printCenter();
                service.printSize(1);
                act.sendPrintMessage(service, start);

                //订单小号
                String  StoreOrderNo =orderBean.getStoreOrderNo()+"\n\n";
                service.printCenter();
                service.printSize(1);
                act.sendPrintMessage( service, StoreOrderNo);

                //店铺名字
                if(orderBean.getShopBean()!=null){
                    String storeName = orderBean.getShopBean().getName() + "\n\n";
                    service.printCenter();
                    service.printSize(1);
                    act.sendPrintMessage( service, storeName);
                }


                //订单号
                String orderNum = "订单号:" + orderBean.getName() + "\n\n";
                service.printLeft();
                service.printSize(0);
                act.sendPrintMessage( service, orderNum);

                //下单时间
                String payTime = "支付时间:" + orderBean.getInsertTime() + "\n\n";
                service.printLeft();
                service.printSize(0);
                act.sendPrintMessage( service, payTime);

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
                        service.printLeft();
                        service.printSize(0);
                        act.sendPrintMessage( service, goods);
                    }
                }

                //配送费
                String shipPrice = "配送费:" + orderBean.getShipPrice() + "\n\n";
                service.printLeft();
                service.printSize(0);
                act.sendPrintMessage(service, shipPrice);

                //包装费
                String packagePrice = "包装费:"+ orderBean.getPackagePrice();
                service.printLeft();
                service.printSize(0);
                act.sendPrintMessage( service, packagePrice);

                //原本应付的总额
                String oriTotal = "总额:" + (oriAll + orderBean.getTotalprice()) + "\n\n";
                service.printLeft();
                service.printSize(0);
                act.sendPrintMessage(service, oriTotal);

                //优惠信息
                String disInfo = "优惠:" + orderBean.getDisvalue() + "\n\n";
                service.printLeft();
                service.printSize(0);
                act.sendPrintMessage( service, disInfo);


                //总价
                String totalPay = "实付:" + orderBean.getTotalprice() + "\n\n";
                service.printLeft();
                service.printSize(2);
                act.sendPrintMessage( service, totalPay);

                //收货信息
                String address = orderBean.getDeliveryAddressBean().getProvinceBean().getName() +
                        orderBean.getDeliveryAddressBean().getCityBean().getName() +
                        orderBean.getDeliveryAddressBean().getDistrictBean().getName() +
                        orderBean.getDeliveryAddressBean().getDetail() + "\n\n";

                service.printLeft();
                service.printSize(2);
                act.sendPrintMessage( service, address);

                String consignee = "收货人:" + orderBean.getDeliveryAddressBean().getConsignee() + "\n\n";
                service.printLeft();
                service.printSize(2);
                act.sendPrintMessage(service, consignee);

                String tel = "联系电话:" + orderBean.getDeliveryAddressBean().getTel() + "\n\n\n\n";
                service.printLeft();
                service.printSize(2);
                act.sendPrintMessage( service, tel);

                //备注信息
                String remarkInfo = "备注:" + orderBean.getBuyerRemark() + "\n\n\n\n";
                service.printLeft();
                service.printSize(2);
                act.sendPrintMessage( service, remarkInfo);

                String end = "*** END ***\n\n\n\n\n";
                service.printCenter();
                service.printSize(1);
                act.sendPrintMessage(service, end);
            }

        } else {
            Toast.makeText(act, "打印信息有误", Toast.LENGTH_SHORT).show();
        }

    }




}
