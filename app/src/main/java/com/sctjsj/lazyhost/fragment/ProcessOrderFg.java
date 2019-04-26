package com.sctjsj.lazyhost.fragment;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;

import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bumptech.glide.Glide;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.activity.IndexActivity;
import com.sctjsj.lazyhost.activity.LoginActivity;
import com.sctjsj.lazyhost.activity.MyQRResultActivity;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.bean.CityBean;
import com.sctjsj.lazyhost.bean.DeliveryAddressBean;
import com.sctjsj.lazyhost.bean.DeliveryBean;
import com.sctjsj.lazyhost.bean.DistrictBean;
import com.sctjsj.lazyhost.bean.GoodsBean;
import com.sctjsj.lazyhost.bean.OrderBean;
import com.sctjsj.lazyhost.bean.ProvinceBean;
import com.sctjsj.lazyhost.bean.ShopBean;
import com.sctjsj.lazyhost.constant.JumpCode;
import com.sctjsj.lazyhost.constant.OtherConstant;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.AnimationUtil;
import com.sctjsj.lazyhost.util.ListViewUtil;
import com.sctjsj.lazyhost.util.LogUtil;
import com.sctjsj.lazyhost.util.NumformatUtil;
import com.sctjsj.lazyhost.util.ProgressUtil;
import com.sctjsj.lazyhost.util.PushNotifyUtil;
import com.sctjsj.lazyhost.util.RingtoneUtil;
import com.sctjsj.lazyhost.util.bt.BluetoothService;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import butterknife.OnClick;
import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.RefreshHandler;

import static android.app.Activity.RESULT_OK;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_NONE;

/**
 * Created by Chris-Jason on 2016/11/9.
 */
public class ProcessOrderFg extends Fragment {
    @Bind(R.id.fg_order_dispose_refresh_layout)
    QRefreshLayout refreshLayout;
    @Bind(R.id.fg_order_dispose_rv)
    RecyclerView mRV;
    @Bind(R.id.fg_process_order_rl_none_remind)
    RelativeLayout mRLNone;
    @Bind(R.id.remind)
    TextView tvRemind;

    private RingtoneUtil ringtoneUtil;
    private int pageIndex = 1;
    private MyApp app;
    private List<OrderBean> data;
    private Handler mHandler;
    private ProgressUtil pUtil;
    private MyAdapter adapter;
    private IndexActivity act;
    //定位
    private AMapLocationClient mLocationClient;
    private AMapLocationListener mLocationListener;
    private AMapLocationClientOption mLocationClientOption;
    private double longT;
    private double lanT;

    //自动加载
    private LinearLayoutManager mLayoutManager;
    private volatile boolean isLoading = false;

    //二维码相关
    private android.app.AlertDialog.Builder builder;
    private android.app.AlertDialog d;
    private ProgressDialog progress;

    @Bind(R.id.fg_process_order_ll_parent)
    LinearLayout mLLParent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_process_order, null);
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


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);//自定义的code

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("权限提示");
            builder.setMessage("定位需要开启定位权限，请到 APP 设置页面进行设置");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Uri packageURI = Uri.parse("package:" + "com.sctjsj.lazyhost");
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    startActivity(intent);
                }
            });
            builder.show();

        } else {
            //启动定位
            initLocation();
        }


        //实现自动加载订单
        mRV.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastCompletelyVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
                int itemCount = mLayoutManager.getItemCount();

                //判断滑动方向
                if (dy > 0 && lastCompletelyVisibleItemPosition + 3 > itemCount) {
                    if (!isLoading) {
                        isLoading = true;
                        loadMoreData();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ringtoneUtil == null) {
            ringtoneUtil = new RingtoneUtil(getActivity());
        }
//        //设置推送消息回调
//        JPushReveivedHostReceiver.setOnGetPushMessageListener(new MyPushReceiver.OnGetPushMessageListener() {
//            @Override
//            public void onReceivedMessage(int type, String message) {
//
//                //只处理透传式消息
//                if(2==type){
//
//                    Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
//                    initData();
//
//                    if(app.getSpf().getBoolean("vibrateOn",true)){
//                        ringtoneUtil.vibriate();
//                        Log.e("TAG","哈哈:"+message);
//                        /**
//                         * 判断提示音类型
//                         */
//
//                        if(message!=null && message.contains("您有新订单")){
//                            ringtoneUtil.playRing(1);
//                            Log.e("ring","1");
//                        }
//
//                        if(message!=null && message.contains("买家已确认收货")){
//                            ringtoneUtil.playRing(5);
//                            Log.e("ring","5");
//                        }
//
//                        if(message!=null && message.contains("配送员已接单")){
//                            ringtoneUtil.playRing(3);
//                            Log.e("ring","3");
//                        }
//
//                        if(message!=null && message.contains("配送员已送达")){
//                            ringtoneUtil.playRing(4);
//                            Log.e("ring","4");
//                        }
//
//                        if(message!=null && message.contains("买家支付成功")){
//                            ringtoneUtil.playRing(2);
//                            Log.e("ring","2");
//                        }
//
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                ringtoneUtil.stopVibrate();
//                                ringtoneUtil.stopRing();
//                            }
//                        },10000);
//                    }
//
//                }
//            }
//        });
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
                            mHandler.sendEmptyMessage(99);
                            mLocationClient.stopLocation();
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
        mLocationClientOption.setOnceLocation(true);
        //如果设置了只定位一次，将获取3s内经度最高的一次定位结果
        if (mLocationClientOption.isOnceLocation()) {
            mLocationClientOption.setOnceLocationLatest(true);
        }
        //设置是否强制刷新wifi
        mLocationClientOption.setWifiActiveScan(true);
        //设置是否允许模拟位置
        mLocationClientOption.setMockEnable(false);
        //设置定位时间间隔10min
        mLocationClientOption.setInterval(1000 * 60 * 5);
        mLocationClient.setLocationOption(mLocationClientOption);
        mLocationClient.startLocation();
    }

    public synchronized void setPrintId(int id) {
        ((IndexActivity)getActivity()).joinQueue(id);
    }

    /**
     * 初始化订单列表
     * 带处理的状态
     * 11-申请退款
     * 15-接单、拒绝接单
     * 17-分配配送员
     */
    public void initData() {
        try {
            if (app == null) {
                if (getActivity() == null) {
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
            initRVAdapter();
            pageIndex = 1;
            Log.e("TAG", "用户ID:" + app.getCurrentUser().getShopId());
            //http://192.168.1.124:8080/wp/pageSearch$ajax.htm?size=4&pageIndex=1&ctype=orderform&orderby=id desc&cond={store:{id:24}}&jf=goodscart|goods|addr|area|parent
            RequestParams params = new RequestParams(BnUrl.pullShopOrderById);
            params.addBodyParameter("size", "8");
            params.addBodyParameter("pageIndex", String.valueOf(pageIndex));
            params.addBodyParameter("ctype", "orderform");
            params.addBodyParameter("orderby", "id desc");
            params.addBodyParameter("cond", "{store:{id:" + app.getCurrentUser().getShopId() + "},in_state:\"10,11,15,17\",isDelete:-1,type:-1}");
            params.addBodyParameter("jf", "store|goodscart|goods|addr|area|parent");//|children|chilrdens|createdGoodsSpeces|children
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
                    Log.e("待处理订单", response.toString());

                    if (response != null) {
                        try {
                            JSONArray resultList = response.getJSONArray("resultList");
                            if (resultList.length() > 0) {
                                //循环订单的条数
                                for (int i = 0; i < resultList.length(); i++) {

                                    //每一条订单购买商品的集合
                                    List<GoodsBean> gbList = new ArrayList<GoodsBean>();
                                    JSONObject x = resultList.getJSONObject(i);
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
                                    double payValue = x.getDouble("payValue");

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
                                    ob.setPayValue(payValue);
                                    ob.setSendTime(distributionTime);
                                    ob.setTakeTime(takeTime);
                                    ob.setPackagePrice(packagePrice);
                                    ob.setStoreOrderNo(StoreOrderNo);
                                    data.add(ob);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            if (data.size() <= 0) {
                                mRLNone.setVisibility(View.VISIBLE);
                            } else {
                                pageIndex++;
                                mRLNone.setVisibility(View.GONE);
                            }

                            initRVAdapter();
                        }
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    Log.e("TAG", "报错：" + ex.toString());
                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {
                    refreshLayout.LoadMoreComplete();
                    refreshLayout.refreshComplete();
                    pUtil.dismissProgress();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 上拉加载
     */
    public void loadMoreData() {

        if (app.getCurrentUser() == null) {
            Toast.makeText(getActivity(), "登录超时,请重新登录", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return;
        }
        RequestParams params = new RequestParams(BnUrl.pullShopOrderById);
        params.addBodyParameter("size", "8");
        params.addBodyParameter("pageIndex", String.valueOf(pageIndex));
        params.addBodyParameter("ctype", "orderform");
        params.addBodyParameter("orderby", "id desc");
        params.addBodyParameter("cond", "{store:{id:" + app.getCurrentUser().getShopId() + "},in_state:\"10,11,15,17\",isDelete:-1,type:-1}");
        params.addBodyParameter("jf", "store|goodscart|goods|addr|area|parent");//|children|chilrdens|createdGoodsSpeces|children
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
                Log.e("TAG", "返回结果:" + response);

                if (response != null) {
                    try {
                        org.json.JSONArray resultList = response.getJSONArray("resultList");
                        if (resultList != null && resultList.length() > 0) {
                            //循环订单的条数
                            for (int i = 0; i < resultList.length(); i++) {
                                //每一条订单购买商品的集合
                                List<GoodsBean> gbList = new ArrayList<GoodsBean>();
                                JSONObject x = resultList.getJSONObject(i);
                                String StoreOrderNo=x.getString("storeOrderNo");
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
                                double packagePrice = x.getDouble("packagePrice");//包装费
                                double payValue = x.getDouble("payValue");

                                int num = x.getInt("num");//商品总数量
                                int state = x.getInt("state");//订单状态
                                //购买的商品列表
                                JSONArray goodscart = x.getJSONArray("goodscart");
                                for (int j = 0; j < goodscart.length(); j++) {
                                    JSONObject g = goodscart.getJSONObject(j);
                                    int goodsId = g.getJSONObject("goods").getInt("id");//商品 id

//                                    String goodsName = g.getJSONObject("goods").getJSONObject("parent").getString("name");
//                                    String property = g.getJSONObject("goods").getString("name");//商品名称

                                    int goodsNum = g.getInt("num");//商品数量
                                    double price = g.getDouble("price");//单价
                                    GoodsBean gb = new GoodsBean();
                                    gb.setId(goodsId);

                                    String goodsName = g.getJSONObject("goods").getInt("level") == 2 ? g.getJSONObject("goods").getString("name") : g.getJSONObject("goods").getString("name");

                                    String specInfo = g.has("specInfo") ? g.getString("specInfo") : "";
                                    gb.setName(goodsName + "    " + specInfo);
                                    gb.setCount(goodsNum);
                                    if (g.getInt("level") == 2)
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
                                    String areaInfo = addr.getString("areaInfo");//详细地址

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
                                double disValue = x.getDouble("disValue");//优惠金额
                                OrderBean ob = new OrderBean();
                                ob.setId(id);
                                ob.setInsertTime(paytime);
                                ob.setPayValue(payValue);
                                ob.setName(name);
                                ob.setDeliveryAddressBean(dab);
                                ob.setShopBean(sb);
                                ob.setType(typeIn);
                                ob.setState(state);
                                ob.setTotalprice(totalPrice);
                                ob.setDisvalue(disValue);
                                ob.setNum(num);
                                ob.setBuyerRemark(buyerRemark);
                                ob.setShipPrice(shipPrice);
                                ob.setInsertTime(paytime);
                                ob.setGoodsBeanList(gbList);
                                ob.setTakeTime(takeTime);
                                ob.setPackagePrice(packagePrice);
                                ob.setSendTime(distributionTime);
                                ob.setStoreOrderNo(StoreOrderNo);
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
                            pageIndex++;
                            mRLNone.setVisibility(View.GONE);
                        }

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
                isLoading = false;
                pUtil.dismissProgress();
                refreshLayout.LoadMoreComplete();
                refreshLayout.refreshComplete();
            }
        });
    }


    @OnClick({R.id.fg_process_order_iv_qr_code,R.id.xxx})
    public void fgProcessClick(View view) {
        switch (view.getId()) {

            case R.id.xxx:
//                PopBTScanDialog d= new PopBTScanDialog(getActivity());
//                d.show();
                break;

            //扫描二维码
            case R.id.fg_process_order_iv_qr_code:
                //1.先判断摄像头权限
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //发起授权请求
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, OtherConstant.REQUEST_CAMERA_AUTHORITY);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("权限提示");
                    builder.setMessage("二维码扫描需要开启相机权限，请到 APP 设置页面进行设置");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri packageURI = Uri.parse("package:" + "com.sctjsj.lazyhost");
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }
                    });
                    builder.show();
                } else {
                    //已经授权
                    Intent intent = new Intent(getActivity(), MyQRResultActivity.class);
                    startActivityForResult(intent, JumpCode.REQUEST_QR_SCAN);
                }

                break;

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case JumpCode.REQUEST_QR_SCAN:
                if (resultCode == RESULT_OK && data != null) {
                    //获取扫描的结果
                    String result = data.getStringExtra("result");

                    if (!TextUtils.isEmpty(result)) {
                        checkQRcode(result);
                    } else {
                        Snackbar.make(mLLParent, "扫描结果为空", Snackbar.LENGTH_LONG).show();
                    }

                }
                break;
        }
    }

    class MyAdapter extends RecyclerView.Adapter<XHolder> {

        @Override
        public XHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(getActivity()).inflate(R.layout.item_of_fg_dispose_order, null);
            XHolder holder = new XHolder(item);
            return holder;
        }

        @Override
        public void onBindViewHolder(XHolder holder, final int position) {

            holder.storeNum.setText("商家简号:"+(TextUtils.isEmpty( data.get(position).getStoreOrderNo())?"":data.get(position).getStoreOrderNo()) );

            if (data.get(position).getDeliveryAddressBean() != null) {
                //设置收货地址
                holder.mTVAddress.setText(data.get(position).getDeliveryAddressBean().getDetail());
                //设置收货人
                holder.mTVConsignee.setText(data.get(position).getDeliveryAddressBean().getConsignee() + "  " +
                        data.get(position).getDeliveryAddressBean().getTel());

                holder.mRLGenerealRemindParent.setVisibility(View.GONE);
            } else {
                holder.mLLDeliveryParent.setVisibility(View.GONE);
                holder.mRLDealParent.setVisibility(View.GONE);
                holder.mRLGenerealRemindParent.setVisibility(View.VISIBLE);
            }

            if (3 == data.get(position).getShopBean().getType()) {
                holder.mLLTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(data.get(position).getSendTime());
                holder.tvTakeTime.setText(data.get(position).getTakeTime());
            } else {
                holder.mLLTime.setVisibility(View.GONE);
            }


            //异步查看买家信息
            holder.mTVQueryBuyer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data != null && data.size() > 0 && data.get(position).getId() != 0) {
                        queryBuyerDetail(String.valueOf(data.get(position).getId()));
                    }
                }
            });

            holder.packagePrice.setText("￥" + NumformatUtil.save2(data.get(position).getPackagePrice()));
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
            holder.mTVGoodsPay.setText("￥" + NumformatUtil.save2(countPrice));
            //设置配送费
            holder.mTVShipprice.setText("￥" + NumformatUtil.save2(data.get(position).getShipPrice()));
            //设置优惠金额
            holder.mTVDisprice.setText("￥" + NumformatUtil.save2(data.get(position).getDisvalue()));
            //设置总价
            holder.mTVTotalPay.setText("￥" + NumformatUtil.save2(data.get(position).getTotalprice()));
            //设置备注
            holder.mTVBuyerRemark.setText(TextUtils.isEmpty(data.get(position).getBuyerRemark()) ? "无特殊要求" : data.get(position).getBuyerRemark());
            //确认接单
            /**
             * 判断店铺类型
             * 桶装水：接单后，店家直接送到宿舍，不需要配送
             * 米兰洗衣：接单后，店家自己去宿舍收件，回去洗
             */
            holder.mBtnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acceptOrder(String.valueOf(data.get(position).getId()), "1");
                }
            });
            //拒绝接单
            holder.mBtnDisaccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    acceptOrder(String.valueOf(data.get(position).getId()), "2");
                }
            });
            final int[] choosedItem = {-1};
            //分配配送
            holder.mBtnSendToDelivery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //弹出框，选择自送还是配送员配送
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    final AlertDialog b = builder.create();
                    builder.setCancelable(true);
                    builder.setTitle("配送方式");
                    //builder.setMessage("请选择配送方式");
                    //1：店家自己送 2：店家的配送员 3：平台的配送员
                    String[] items = new String[]{"懒购专送"};

                    builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //懒购专送
                            if (0 == which) {
                                choosedItem[0] = 0;
                            }
//
//                            if(1==which){
//                                choosedItem[0] =1;
//                            }
//                            //懒购专送
//                            if(2==which){
//                                choosedItem[0]=2;
//                            }
                        }
                    });
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (choosedItem[0]) {

                                case 0:
                                    //暂时不开放配送端
                                    sendToDeliveryByPlatform(data.get(position).getId());

                                    b.dismiss();
//                                    deliveryBySelf(String.valueOf(data.get(position).getId()));
//                                    b.dismiss();
                                    break;
//                                case 1:
//                                    b.dismiss();
//                                    //a.查询店铺下的配送员列表
//                                    getDeliveryByStoreId(data.get(position).getId());
//                                    //b.分配给指定的配送员
//
//                                    break;
                                case 1:
                                    //暂时不开放配送端
                                    sendToDeliveryByPlatform(data.get(position).getId());

                                    b.dismiss();
                                    break;
                            }

                            choosedItem[0] = -1;

                        }
                    });
                    builder.show();

                }
            });

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


            //设置订单状态
            switch (data.get(position).getState()) {
                //订单已经失效
                case 1:
                    holder.mTVBeforePayRemind.setVisibility(View.VISIBLE);
                    holder.mTVBeforePayRemind.setText("订单已经失效");
                    holder.mTVGeneralRemind.setText("订单已经失效");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.GONE);//订单状态提示
                    break;
                //等待买家付款
                case 5:
                    //
                    holder.mTVBeforePayRemind.setVisibility(View.VISIBLE);
                    holder.mTVBeforePayRemind.setText("等待买家付款");
                    holder.mTVGeneralRemind.setText("等待买家付款");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.GONE);//订单状态提示

                    break;

                //买家已付款
                case 10:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("买家已付款");
                    holder.mTVGeneralRemind.setText("买家已付款");
                    holder.mLLDealOrderParent.setVisibility(View.VISIBLE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.GONE);//订单状态提示
                    break;

                //买家申请退款
                case 11:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("买家申请退款");
                    holder.mTVGeneralRemind.setText("买家申请退款");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("买家申请退款");
                    break;

                //店家同意付款
                case 12:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("商家同意退款");
                    holder.mTVGeneralRemind.setText("商家同意退款");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("商家同意退款");
                    break;

                //退款失败
                case 13:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("退款失败");
                    holder.mTVGeneralRemind.setText("退款失败");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("退款失败");
                    break;

                //退款成功
                case 14:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("退款成功");
                    holder.mTVGeneralRemind.setText("退款成功");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("退款成功");
                    break;

                //等待商家接单
                case 15:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("");
                    holder.mTVGeneralRemind.setText("");
                    holder.mLLDealOrderParent.setVisibility(View.VISIBLE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.GONE);//订单状态提示
                    break;

                //商家拒绝接单
                case 16:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("");
                    holder.mTVGeneralRemind.setText("");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("已拒绝接单");
                    break;

                //商家已接单
                case 17:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("");
                    holder.mTVGeneralRemind.setText("");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.VISIBLE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.GONE);//订单状态提示

                    break;

                //商品配送中
                case 18:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("");
                    holder.mTVGeneralRemind.setText("");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("正在配送中");
                    break;

                //商品已送达
                case 19:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("");
                    holder.mTVGeneralRemind.setText("");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("已送达");
                    break;
                //已消费
                case 20:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("");
                    holder.mTVGeneralRemind.setText("买家已消费");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("已核销");
                    break;
                //等待骑手接单
                case 21:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("");
                    holder.mTVGeneralRemind.setText("");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("等待骑手接单");
                    break;
                //等待配送
                case 22:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("");
                    holder.mTVGeneralRemind.setText("");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("等待配送");
                    break;

                //已下单,送水/洗衣 提交订单后的状态
                case 30:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("");
                    holder.mTVGeneralRemind.setText("");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("买家已下单，等待送达");
                    break;

                //已收件,店家更新，录入衣服信息到订单
                case 40:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("");
                    holder.mTVGeneralRemind.setText("");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("已收件");
                    break;

                default:
                    holder.mTVBeforePayRemind.setVisibility(View.GONE);
                    holder.mTVBeforePayRemind.setText("");
                    holder.mTVGeneralRemind.setText("订单状态错误");
                    holder.mLLDealOrderParent.setVisibility(View.GONE);//等待接单操作
                    holder.mLLDisposeOrder.setVisibility(View.GONE);//等待分配配送操作
                    holder.mTVAfterPayRemind.setVisibility(View.VISIBLE);//订单状态提示
                    holder.mTVAfterPayRemind.setText("订单状态错误");
                    break;

            }
            if (data.get(position).getType() == 1) {
                holder.ivError.setVisibility(View.VISIBLE);
            } else {
                holder.ivError.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    //平台配送
    private void sendToDeliveryByPlatform(int id) {
        RequestParams params = new RequestParams(BnUrl.getSendToShopDeliveryByPlatformUrl);
        params.addHeader("Cookie", app.getSpf().getString("cookie", "none-cookie"));
        params.addBodyParameter("orderId", id + "");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject result) {
                if (result != null) {
                    try {
                        boolean results = result.getBoolean("result");
                        if (results) {
                            Snackbar.make(mLLParent, "订单已交给平台配送", Snackbar.LENGTH_SHORT).show();
                            //刷新数据
                            initData();
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
                mTVGoodsPay, mTVTotalPay, mTVBuyerRemark, mTVAfterPayRemind, mTVBeforePayRemind,
                mTVDisprice, mTVQueryBuyer;
        ListView mLV;
        LinearLayout mLLDealOrderParent, mLLDisposeOrder, mLLTime;

        Button mBtnAccept, mBtnDisaccept, mBtnSendToDelivery, mBtnPrintOrder;

        LinearLayout mLLDeliveryParent;
        RelativeLayout mRLDealParent;

        //        通用的订单状态提醒
        RelativeLayout mRLGenerealRemindParent;
        TextView mTVGeneralRemind;
        TextView tvTakeTime, tvSendTime;
        TextView storeNum;
        TextView packagePrice;
        ImageView ivError;

        public XHolder(View itemView) {
            super(itemView);
            storeNum=itemView.findViewById(R.id.storeNum);
            mTVAddress = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_delivery_address);
            mTVConsignee = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_consignee);
            mTVPaytime = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_paytime);
            mTVOrderNum = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_order_num);
            mLV = (ListView) itemView.findViewById(R.id.item_of_fg_dispose_lv_goods_list);
            mTVShipprice = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_shipprice);
            mTVGoodsPay = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_goods_pay);
            mTVTotalPay = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_total_pay);
            mTVBuyerRemark = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_buyer_remark);
            mTVDisprice = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_disprice);

            mTVQueryBuyer = (TextView) itemView.findViewById(R.id.item_dispose_tv_query_buyer);

            //付款前后的状态提示
            mTVAfterPayRemind = (TextView) itemView.findViewById(R.id.tv_after_pay_order_remind);
            mTVBeforePayRemind = (TextView) itemView.findViewById(R.id.tv_before_pay_order_remind);


            mLLDealOrderParent = (LinearLayout) itemView.findViewById(R.id.ll_deal_order_parent);
            mLLDisposeOrder = (LinearLayout) itemView.findViewById(R.id.ll_dispose_order);

            mBtnAccept = (Button) itemView.findViewById(R.id.item_of_fg_dispose_order_accept_order);
            mBtnDisaccept = (Button) itemView.findViewById(R.id.item_of_fg_dispose_order_dis_accept_order);
            mBtnSendToDelivery = (Button) itemView.findViewById(R.id.item_of_fg_dispose_order_send_to_delivery);

            mBtnPrintOrder = (Button) itemView.findViewById(R.id.item_of_fg_dispose_order_print_order_info);
            mLLDeliveryParent = (LinearLayout) itemView.findViewById(R.id.item_of_fg_dispose_ll_delivery);
            mRLDealParent = (RelativeLayout) itemView.findViewById(R.id.item_of_fg_dispose_rl_deal_with);

            mRLGenerealRemindParent = (RelativeLayout) itemView.findViewById(R.id.item_fg_dispose_rl_general_remind_parent);
            mTVGeneralRemind = (TextView) itemView.findViewById(R.id.item_fg_dispose_tv_general_remind);

            mLLTime = (LinearLayout) itemView.findViewById(R.id.take_and_delivery_ll_parent);
            tvTakeTime = (TextView) itemView.findViewById(R.id.tv_take_time);
            tvSendTime = (TextView) itemView.findViewById(R.id.tv_send_time);
            packagePrice = (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_packagePrice);
            ivError = (ImageView) itemView.findViewById(R.id.iv_error);
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

            h.mTVName.setText(gbList.get(position).getName());
            h.mTVCount.setText("*" + gbList.get(position).getCount());
            h.mTVPrice.setText("小计:￥" + NumformatUtil.save2(gbList.get(position).getPrice()));
            //Log.e("TAG","数量:"+gbList.get(position).getCount()+"------"+gbList.get(position).getPrice());

            return convertView;
        }

        class Holder {
            TextView mTVName, mTVCount, mTVPrice;
        }

    }

    /**
     * 商家确认接单 1/拒绝接单 2
     */
    private void acceptOrder(final String orderId, final String status) {
        RequestParams params = new RequestParams(BnUrl.acceptOrderUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie", app.getSpf().getString("cookie", "none-cookie"));
        params.addBodyParameter("ofId", orderId);
        params.addBodyParameter("statu", status);

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.e("TAG", "商家接单/拒单:" + response);
                if (response != null) {
                    try {
                        boolean result = response.getBoolean("result");
                        if (result) {
                            //接单成功
                            if ("1".equals(status)) {
                                Snackbar.make(mLLParent, "接单成功", Snackbar.LENGTH_SHORT).show();
                                //刷新数据
                                initData();
                                //推送给买家
                                PushNotifyUtil.ShopPushToBuyer(orderId, "1", new PushNotifyUtil.PushResultCallack() {
                                    @Override
                                    public void pushSuccess() {

                                    }

                                    @Override
                                    public void pushFaild() {

                                    }
                                });

                            }

                            //拒绝接单成功
                            if ("2".equals(status)) {
                                Snackbar.make(mLLParent, "已拒接此单", Snackbar.LENGTH_SHORT).show();
                                //刷新数据
                                initData();
                                //推送给买家
                                PushNotifyUtil.ShopPushToBuyer(orderId, "2", new PushNotifyUtil.PushResultCallack() {
                                    @Override
                                    public void pushSuccess() {

                                    }

                                    @Override
                                    public void pushFaild() {

                                    }
                                });

                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                pUtil.dismissProgress();
                Log.e("TAG", "错误:" + ex.toString());
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

    /**
     * 商家派单给配送员配送
     *
     * @param orderId
     */
    private void sendToDelivery(final String orderId) {
        RequestParams params = new RequestParams(BnUrl.sendOrderToDeliveryUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie", app.getSpf().getString("cookie", "none-cookie"));
        params.addBodyParameter("ofId", orderId);
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                if (response != null) {
                    try {
                        boolean result = response.getBoolean("result");
                        if (result) {
                            Snackbar.make(mLLParent, "订单已交给配送员", Snackbar.LENGTH_SHORT).show();
                            //刷新数据
                            initData();
                            //推送给配送员
                            PushNotifyUtil.ShopPushToDelivery(orderId, new PushNotifyUtil.PushResultCallack() {
                                @Override
                                public void pushSuccess() {

                                }

                                @Override
                                public void pushFaild() {

                                }
                            });
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


    /**
     * 店家自送
     *
     * @param orderId
     */
    private void deliveryBySelf(final String orderId) {
        RequestParams params = new RequestParams(BnUrl.deliveryBySelfUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie", app.getSpf().getString("cookie", "none-cookie"));
        params.addBodyParameter("Ofid", orderId);
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                if (response != null) {
                    try {
                        boolean result = response.getBoolean("result");
                        if (result) {
                            Snackbar.make(mLLParent, "开始配送", Snackbar.LENGTH_SHORT).show();
                            //刷新数据
                            initData();
                            //推送给买家，在配送中
                            PushNotifyUtil.ShopPushToBuyer(orderId, "3", new PushNotifyUtil.PushResultCallack() {
                                @Override
                                public void pushSuccess() {

                                }

                                @Override
                                public void pushFaild() {

                                }
                            });

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


    /**
     * 检查二维码是否可以核销
     */
    private void checkQRcode(String code) {
        //http://www.lp-kd.com/user/isQRcodeAvailable$ajax.htm?code=170310112909&jf=goods|store|of
        RequestParams params = new RequestParams(BnUrl.checkQRCode);
        params.setUseCookie(false);
        params.addHeader("Cookie", app.getSpf().getString("cookie", ""));
        params.addBodyParameter("code", code);
        params.addBodyParameter("jf", "goods|store|of");
        x.http().post(params, new Callback.ProgressCallback<String>() {
            @Override
            public void onSuccess(String response) {

                //扫描不相关的字符串
                if (response.equals("{\"domainBase\":\"templates/default/zh_CN/pc/\"}")) {
                    Snackbar.make(mLLParent, "非法信息", Snackbar.LENGTH_LONG).show();
                    onFinished();
                    return;
                }

                //扫描结果为空
                if (response == null) {
                    Snackbar.make(mLLParent, "非法信息", Snackbar.LENGTH_LONG).show();
                    onFinished();
                    return;
                }

                try {
                    JSONObject r = new JSONObject(response);
                    boolean result = r.getBoolean("result");
                    if (result) {

                        JSONObject chTbl = r.getJSONObject("chTbl");
                        int storeId = chTbl.getJSONObject("goods").getJSONObject("store").getInt("id");

                        if (app.getCurrentUser().getShopId() != storeId) {
                            Snackbar.make(mLLParent, "非本店商品无权核销", Snackbar.LENGTH_LONG).show();
                            onFinished();
                            return;
                        }

                        int state = chTbl.getInt("state");//二维码状态
                        final String orderChId = chTbl.getString("orderChId");//消费码
                        String conTime = chTbl.getString("conTime");//消费时间

                        //商品信息
                        JSONObject goods = chTbl.getJSONObject("goods");
                        String name = goods.getString("name");//商品名
                        double price = goods.getDouble("price");//商品价格

                        //订单信息
                        JSONObject of = chTbl.getJSONObject("of");
                        final int orderId = of.getInt("id");//订单 id

                        //状态:1-待使用 2-已使用 3-申请退款 4-申请退款成功 5-7天未使用自动退款
                        if (1 == state) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("二维码核销");
                            builder.setMessage(name + ":" + orderChId);
                            builder.setIcon(R.mipmap.icon_app_start);
                            builder.setPositiveButton("确认核销", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    goWriteOff(orderChId, String.valueOf(orderId));
                                }
                            });
                            builder.show();
                        }

                        if (2 == state) {
                            AlertDialog.Builder b1 = new AlertDialog.Builder(getActivity());
                            b1.setMessage(name + ":" + orderChId + "已使用");
                            b1.setCancelable(false);
                            b1.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            b1.show();
                        }

                        if (3 == state) {
                            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                            b.setMessage(name + ":" + orderChId + "正在申请退款中，无法使用");
                            b.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            b.show();
                        }

                        if (4 == state) {
                            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                            b.setMessage(name + ":" + orderChId + "已退款成功，无法使用");
                            b.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            b.show();
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                dissmiss();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                dissmiss();
            }

            @Override
            public void onFinished() {
                dissmiss();
            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                show(false);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });

    }


    private void show(boolean b) {
        if (progress == null) {
            progress = new ProgressDialog(getActivity());
            progress.setMessage("努力加载中");
        }
        progress.setCancelable(b);
        progress.show();
    }

    private void dissmiss() {
        if (progress != null) {
            progress.dismiss();
        }
    }

    /**
     * 前往核销
     */
    private void goWriteOff(final String id, final String orderId) {
        RequestParams params = new RequestParams(BnUrl.writeOffQRUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie", app.getSpf().getString("cookie", "none"));
        params.addBodyParameter("code", id);
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if (response != null) {
                    try {
                        boolean result = response.getBoolean("result");
                        if (result) {
                            //通知买家核销成功
                            PushNotifyUtil.ShopPushToBuyer(orderId, "7", new PushNotifyUtil.PushResultCallack() {
                                @Override
                                public void pushSuccess() {

                                }

                                @Override
                                public void pushFaild() {

                                }
                            });

                            Snackbar.make(mLLParent, "核销成功", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(mLLParent, "核销失败", Snackbar.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                dissmiss();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                dissmiss();
            }

            @Override
            public void onFinished() {
                dissmiss();
            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                show(false);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });
    }

    /**
     * 打印订单
     *
     * @param orderBean
     */
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
                String oriTotal = "总额:" + orderBean.getTotalprice() + "\n\n";
                service.printLeft();
                service.printSize(0);
                act.sendPrintMessage(service, oriTotal);

                //优惠信息
                String disInfo = "优惠:" + orderBean.getDisvalue() + "\n\n";
                service.printLeft();
                service.printSize(0);
                act.sendPrintMessage( service, disInfo);


                //总价
                String totalPay = "实付:" + orderBean.getPayValue() + "\n\n";
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

    /**
     * 查看买家信息
     *
     * @param ofId
     */
    public void queryBuyerDetail(String ofId) {
        RequestParams params = new RequestParams(BnUrl.getBuyerByOrderId);
        params.setUseCookie(false);
        params.addHeader("Cookie", app.getSpf().getString("cookie", ""));
        params.addBodyParameter("ofId", ofId);
        params.addBodyParameter("jf", "photo");
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if (response != null) {
                    try {
                        JSONObject user = response.getJSONObject("user");
                        int id = user.getInt("id");//用户 id
                        String username = user.getString("username");//用户名
                        String phone = user.getString("phone");//电话
                        String photoStr = user.getString("photo");
                        String url = null;
                        if (photoStr != null && !"null".equals(photoStr)) {
                            url = user.getJSONObject("photo").getString("url");
                        }
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

            }
        });
    }

    /**
     * 根据店铺 id 来查询店铺下的配送员列表
     */
    private void getDeliveryByStoreId(final int ofid) {
        final List<DeliveryBean> deliveryBeanList = new ArrayList<>();
        deliveryBeanList.clear();


        RequestParams params = new RequestParams(BnUrl.getDeliveryByStoreUrl);
        params.addBodyParameter("sid", app.getCurrentUser().getShopId() + "");
        params.addBodyParameter("pageSize", "999");
//         params.addBodyParameter("pageSize","999");
//         params.addBodyParameter("ctype","user");
//         params.addBodyParameter("cond","{type:3,shipInStore:{id:"+app.getCurrentUser().getShopId()+"},isDelete:0,isLocked:0}");
        params.addBodyParameter("jf", "photo|area|shipInStore");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {

                if (result != null) {
                    try {
                        Log.e("TAG", "骑手查询:" + result);
                        JSONArray resultList = result.getJSONArray("resultList");
                        if (resultList != null && resultList.length() > 0) {
                            for (int i = 0; i < resultList.length(); i++) {
                                JSONObject object = resultList.getJSONObject(i);
                                int id = object.getInt("id");
                                String photo = object.getString("photo");
                                String url = null;
                                if (photo != null && !photo.equals("null")) {
                                    url = object.getJSONObject("photo").getString("url");
                                }
                                String username = object.getString("username");
                                String phone = object.getString("phone");

                                DeliveryBean deliveryBean = new DeliveryBean();
                                deliveryBean.setId(id);
                                deliveryBean.setUrl(url);
                                deliveryBean.setName(username);
                                deliveryBean.setPhone(phone);

                                deliveryBeanList.add(deliveryBean);
                            }

                        } else {
                            Toast.makeText(getActivity(), "商户暂无专属配送员", Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if (deliveryBeanList.size() > 0) {
                            alertChooseDelivery(deliveryBeanList, ofid);
                        }
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

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                pUtil.showProgress(true);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });


    }

    private void alertChooseDelivery(final List<DeliveryBean> data, final int ofid) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        final Dialog d = b.create();
        b.setCancelable(false);
        b.setTitle("商户专送");
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.shop_delivery_layout, null);
        RecyclerView rv = (RecyclerView) v.findViewById(R.id.shop_delivery_layout_rv);
        final DeliveryAdapter adapter = new DeliveryAdapter(data, getActivity());
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(adapter);

        b.setView(v);
        b.setPositiveButton("确认派送", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //发布给指定的配送员
                int posi = adapter.getChoosedPosition();
                if (-1 == posi) {
                    Toast.makeText(getActivity(), "请先选择指定的配送员", Toast.LENGTH_SHORT).show();
                } else {
                    sendToDelivery(ofid, data.get(posi).getId());
                }


            }
        });
        b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                d.dismiss();
            }
        });

        b.show();
    }


    public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryHolder> {
        private List<DeliveryBean> data;
        private LayoutInflater inflator;
        private int choosedPosition = -1;
        private Context con;

        public DeliveryAdapter(List<DeliveryBean> data, Context con) {
            this.data = data;
            this.con = con;
            if (inflator == null) {
                inflator = LayoutInflater.from(con);
            }
        }

        public int getChoosedPosition() {
            return choosedPosition;
        }

        @Override
        public DeliveryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DeliveryHolder(inflator.inflate(R.layout.item_shop_delivery_layout, null));
        }

        @Override
        public void onBindViewHolder(final DeliveryHolder holder, final int position) {
            if (data.get(position).getUrl() != null) {
                Glide.with(con).load(data.get(position).getUrl()).
                        error(R.mipmap.icon_default_portrait).into(holder.mIVLogo);
            }
            holder.mTVName.setText(data.get(position).getName());
            holder.mTVTel.setText(data.get(position).getPhone());
            holder.cbx.setChecked(data.get(position).isChecked());

            holder.cbx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < data.size(); i++) {
                        data.get(i).setChecked(false);
                    }
                    data.get(position).setChecked(true);
                    choosedPosition = position;
                    notifyDataSetChanged();
                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    class DeliveryHolder extends RecyclerView.ViewHolder {
        ImageView mIVLogo;
        CheckBox cbx;
        TextView mTVName, mTVTel;

        public DeliveryHolder(View itemView) {
            super(itemView);
            mIVLogo = (ImageView) itemView.findViewById(R.id.shop_delivery_layout_iv_logo);
            cbx = (CheckBox) itemView.findViewById(R.id.shop_delivery_layout_cbx);
            mTVName = (TextView) itemView.findViewById(R.id.shop_delivery_layout_tv_name);
            mTVTel = (TextView) itemView.findViewById(R.id.shop_delivery_layout_tv_tel);
        }
    }

    /**
     * 派单给指定的配送员
     *
     * @param ofid
     * @param userid
     */
    private void sendToDelivery(int ofid, int userid) {
        RequestParams params = new RequestParams(BnUrl.sendToShopDeliveryByIdUrl);
        params.addBodyParameter("orderId", String.valueOf(ofid));
        params.addBodyParameter("userId", String.valueOf(userid));
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject res) {
                Log.e("指定派单", res.toString());
                if (res != null) {
                    try {
                        boolean result = res.getBoolean("result");
                        if (result) {
                            Toast.makeText(getActivity(), "订单已分配给指定配送员", Toast.LENGTH_SHORT).show();
                            initData();
                        } else {
                            Toast.makeText(getActivity(), "订单分配失败", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("指定派单", ex.toString());
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
                pUtil.showProgress(true);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });
    }


    private void initRVAdapter() {
        if (adapter == null) {
            adapter = new MyAdapter();
            mRV.setAdapter(adapter);
            mRV.setLayoutManager(mLayoutManager);
        }
        adapter.notifyDataSetChanged();
    }

}
