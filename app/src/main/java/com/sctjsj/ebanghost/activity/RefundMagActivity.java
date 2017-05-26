package com.sctjsj.ebanghost.activity;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.bean.CityBean;
import com.sctjsj.ebanghost.bean.DeliveryAddressBean;
import com.sctjsj.ebanghost.bean.DistrictBean;
import com.sctjsj.ebanghost.bean.GoodsBean;
import com.sctjsj.ebanghost.bean.OrderBean;
import com.sctjsj.ebanghost.bean.ProvinceBean;
import com.sctjsj.ebanghost.bean.ShopBean;
import com.sctjsj.ebanghost.fragment.ProcessOrderFg;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.DpUtils;
import com.sctjsj.ebanghost.util.ListViewUtil;
import com.sctjsj.ebanghost.util.NumformatUtil;
import com.sctjsj.ebanghost.util.ProgressUtil;
import com.sctjsj.ebanghost.util.PushNotifyUtil;
import com.squareup.picasso.Picasso;

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

public class RefundMagActivity extends AppCompatActivity {

    private List<OrderBean> data=new ArrayList<>();
    private int pageIndex=1;
    private MyApp app;
    private ProgressUtil pUtil;
    private MyAdapter adapter;

    @Bind(R.id.act_refund_mag_rv)RecyclerView mRV;
    @Bind(R.id.act_refund_ll_remind)LinearLayout mLLRemind;
    @Bind(R.id.act_refund_refresh)QRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund_mag);
        ButterKnife.bind(this);

        app= (MyApp) getApplication();
        pUtil=new ProgressUtil(this);

        initData();
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

    }

    @OnClick({R.id.act_refund_iv_back})
    public void refundClick(View view){
        switch (view.getId()){
            case R.id.act_refund_iv_back:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    /**
     * 初始化订单列表
     */
    public void initData(){
        data.clear();
        pageIndex=1;
        //http://www.lp-kd.com/pageSearch$ajax.htm?ctype=orderform&cond={store:{id:},state:11}&jf=user1|store|goodscart|goods|addr|area|parent
        RequestParams params=new RequestParams(BnUrl.pullShopOrderById);
        params.setUseCookie(false);
        params.addBodyParameter("size","4");
        params.addBodyParameter("pageIndex",String.valueOf(pageIndex));
        params.addBodyParameter("ctype","orderform");
        params.addBodyParameter("orderby","id desc");
        params.addBodyParameter("cond","{store:{id:"+app.getCurrentUser().getShopId()+"},state:11}");
        params.addBodyParameter("jf","user1|store|goodscart|goods|addr|area|parent");
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

                if(response!=null){
                    try {
                        org.json.JSONArray resultList=response.getJSONArray("resultList");
                        if(resultList.length()>0){
                            //循环订单的条数
                            for (int i=0;i<resultList.length();i++){
                                //每一条订单购买商品的集合
                                List<GoodsBean> gbList=new ArrayList<GoodsBean>();
                                JSONObject x=resultList.getJSONObject(i);
                                //店铺名
                                String storeName=x.getJSONObject("store").getString("storeName");
                                int storeId=x.getJSONObject("store").getInt("id");
                                String storeAddress=x.getJSONObject("store").getString("storeAddress");
                                ShopBean sb=new ShopBean();
                                sb.setId(storeId);
                                sb.setAddress(storeAddress);
                                sb.setName(storeName);
                                //订单信息
                                int id=x.getInt("id");//订单 id
                                String name=x.getString("name");//给用户看的订单号
                                String buyerRemark=x.getString("buyerRemark");//买家备注
                                String paytime=x.getString("insertTime");//下单时间
                                double shipPrice=x.getDouble("shipPrice");//运费

                                double totalPrice=x.getDouble("totalprice");//总价
                                double disValue=x.getDouble("disValue");//优惠金额

                                int num=x.getInt("num");//商品总数量
                                int state=x.getInt("state");//订单状态
                                //购买的商品列表
                                JSONArray goodscart=x.getJSONArray("goodscart");
                                for (int j=0;j<goodscart.length();j++){
                                    JSONObject g=goodscart.getJSONObject(j);
                                    int goodsId=g.getJSONObject("goods").getInt("id");//商品 id
                                    String goodsName=g.getJSONObject("goods").getString("name");//商品名称
                                    int goodsNum=g.getInt("num");//商品数量
                                    double price=g.getDouble("price");//单价
                                    GoodsBean gb=new GoodsBean();
                                    gb.setId(goodsId);
                                    gb.setName(goodsName);
                                    gb.setCount(goodsNum);
                                    gb.setPrice(price);
                                    gbList.add(gb);
                                }





                                DeliveryAddressBean dab=null;
                                String strAddr=x.getString("addr");
                                if(strAddr!=null && !strAddr.equals("null")){
                                    //收货信息
                                    JSONObject addr=new JSONObject(strAddr);

                                    int deliveryId=addr.getInt("id");//收货地址 id
                                    String recName=addr.getString("recName");//收货人
                                    String consigneeTel=addr.getString("mobile");//收货人电话
                                    String areaInfo=addr.getString("areaInfo");//详细地址

                                    JSONObject area=addr.getJSONObject("area");//区
                                    String districtName=area.getString("areaname");//区县名称
                                    int districtId=area.getInt("id");//区县 id
                                    DistrictBean districtBean=new DistrictBean();
                                    districtBean.setId(districtId);
                                    districtBean.setName(districtName);

                                    JSONObject city=area.getJSONObject("parent");//市
                                    String cityName=city.getString("areaname");//城市名
                                    int cityId=city.getInt("id");//城市 id
                                    CityBean cityBean=new CityBean();
                                    cityBean.setId(cityId);
                                    cityBean.setName(cityName);

                                    JSONObject province =city.getJSONObject("parent");//省
                                    String provinceName=province.getString("areaname");//省名
                                    int provinceId=province.getInt("id");//省 id
                                    ProvinceBean provinceBean=new ProvinceBean();
                                    provinceBean.setId(provinceId);
                                    provinceBean.setName(provinceName);

                                    dab=new DeliveryAddressBean();
                                    dab.setId(deliveryId);
                                    dab.setProvinceBean(provinceBean);
                                    dab.setCityBean(cityBean);
                                    dab.setDistrictBean(districtBean);
                                    dab.setConsignee(recName);
                                    dab.setDetail(areaInfo);
                                    dab.setTel(consigneeTel);
                                }

                                OrderBean ob=new OrderBean();
                                ob.setId(id);
                                ob.setName(name);
                                ob.setDeliveryAddressBean(dab);
                                ob.setState(state);

                                ob.setTotalprice(totalPrice);
                                ob.setDisvalue(disValue);
                                ob.setShopBean(sb);
                                ob.setNum(num);
                                ob.setBuyerRemark(buyerRemark);
                                ob.setShipPrice(shipPrice);
                                ob.setInsertTime(paytime);
                                ob.setGoodsBeanList(gbList);
                                data.add(ob);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }finally {
                        if(data.size()<=0){
                            mLLRemind.setVisibility(View.VISIBLE);
                        }else {
                            pageIndex++;
                            mLLRemind.setVisibility(View.GONE);
                        }

                        if(adapter==null){
                            adapter=new MyAdapter();
                            mRV.setAdapter(adapter);
                            mRV.setLayoutManager(new LinearLayoutManager(RefundMagActivity.this));
                        }
                        adapter.notifyDataSetChanged();
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
                refreshLayout.LoadMoreComplete();
                refreshLayout.refreshComplete();
                pUtil.dismissProgress();
            }
        });
    }


    /**
     * 上拉加载
     */
    public void loadMoreData(){
        RequestParams params=new RequestParams(BnUrl.pullShopOrderById);
        params.setUseCookie(false);
        params.addBodyParameter("size","4");
        params.addBodyParameter("pageIndex",String.valueOf(pageIndex));
        params.addBodyParameter("ctype","orderform");
        params.addBodyParameter("orderby","id desc");
        params.addBodyParameter("cond","{store:{id:"+app.getCurrentUser().getShopId()+"},state:11}");
        params.addBodyParameter("jf","store|goodscart|goods|addr|area|parent");
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

                if(response!=null){
                    try {
                        org.json.JSONArray resultList=response.getJSONArray("resultList");
                        if(resultList.length()>0){
                            //循环订单的条数
                            for (int i=0;i<resultList.length();i++){
                                //每一条订单购买商品的集合
                                List<GoodsBean> gbList=new ArrayList<GoodsBean>();
                                JSONObject x=resultList.getJSONObject(i);
                                //店铺名
                                String storeName=x.getJSONObject("store").getString("storeName");
                                int storeId=x.getJSONObject("store").getInt("id");
                                String storeAddress=x.getJSONObject("store").getString("storeAddress");
                                ShopBean sb=new ShopBean();
                                sb.setId(storeId);
                                sb.setAddress(storeAddress);
                                sb.setName(storeName);
                                //订单信息
                                int id=x.getInt("id");//订单 id

                                String name=x.getString("name");//给用户看的订单号
                                String buyerRemark=x.getString("buyerRemark");//买家备注
                                String paytime=x.getString("insertTime");//下单时间
                                double shipPrice=x.getDouble("shipPrice");//运费
                                double totalPrice=x.getDouble("totalprice");//总价
                                int num=x.getInt("num");//商品总数量
                                int state=x.getInt("state");//订单状态
                                //购买的商品列表
                                JSONArray goodscart=x.getJSONArray("goodscart");
                                for (int j=0;j<goodscart.length();j++){
                                    JSONObject g=goodscart.getJSONObject(j);
                                    int goodsId=g.getJSONObject("goods").getInt("id");//商品 id
                                    String goodsName=g.getJSONObject("goods").getString("name");//商品名称
                                    int goodsNum=g.getInt("num");//商品数量
                                    double price=g.getDouble("price");//单价
                                    GoodsBean gb=new GoodsBean();
                                    gb.setId(goodsId);
                                    gb.setName(goodsName);
                                    gb.setCount(goodsNum);
                                    gb.setPrice(price);
                                    gbList.add(gb);
                                }
                                DeliveryAddressBean dab=null;
                                String strAddr=x.getString("addr");
                                if(strAddr!=null && !strAddr.equals("null")){
                                    //收货信息
                                    JSONObject addr=new JSONObject(strAddr);
                                    int deliveryId=addr.getInt("id");//收货地址 id
                                    String recName=addr.getString("recName");//收货人
                                    String consigneeTel=addr.getString("mobile");//收货人电话
                                    String areaInfo=addr.getString("areaInfo");//详细地址

                                    JSONObject area=addr.getJSONObject("area");//区
                                    String districtName=area.getString("areaname");//区县名称
                                    int districtId=area.getInt("id");//区县 id
                                    DistrictBean districtBean=new DistrictBean();
                                    districtBean.setId(districtId);
                                    districtBean.setName(districtName);

                                    JSONObject city=area.getJSONObject("parent");//市
                                    String cityName=city.getString("areaname");//城市名
                                    int cityId=city.getInt("id");//城市 id
                                    CityBean cityBean=new CityBean();
                                    cityBean.setId(cityId);
                                    cityBean.setName(cityName);

                                    JSONObject province =city.getJSONObject("parent");//省
                                    String provinceName=province.getString("areaname");//省名
                                    int provinceId=province.getInt("id");//省 id
                                    ProvinceBean provinceBean=new ProvinceBean();
                                    provinceBean.setId(provinceId);
                                    provinceBean.setName(provinceName);

                                    dab=new DeliveryAddressBean();
                                    dab.setId(deliveryId);
                                    dab.setProvinceBean(provinceBean);
                                    dab.setCityBean(cityBean);
                                    dab.setDistrictBean(districtBean);
                                    dab.setConsignee(recName);
                                    dab.setDetail(areaInfo);
                                    dab.setTel(consigneeTel);


                                }

                                OrderBean ob=new OrderBean();
                                ob.setId(id);
                                ob.setInsertTime(paytime);
                                ob.setName(name);
                                ob.setDeliveryAddressBean(dab);
                                ob.setShopBean(sb);
                                ob.setState(state);
                                ob.setTotalprice(totalPrice);
                                ob.setNum(num);
                                ob.setBuyerRemark(buyerRemark);
                                ob.setShipPrice(shipPrice);
                                ob.setInsertTime(paytime);
                                ob.setGoodsBeanList(gbList);
                                data.add(ob);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }finally {
                        if(data.size()<=0){
                            mLLRemind.setVisibility(View.VISIBLE);
                        }else {
                            pageIndex++;
                            mLLRemind.setVisibility(View.GONE);
                        }

                        if(adapter==null){
                            adapter=new MyAdapter();
                            mRV.setAdapter(adapter);
                            mRV.setLayoutManager(new LinearLayoutManager(RefundMagActivity.this));
                        }
                        adapter.notifyDataSetChanged();
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


    class MyAdapter extends RecyclerView.Adapter<XHolder>{

        @Override
        public XHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item= LayoutInflater.from(RefundMagActivity.this).inflate(R.layout.item_refund_order_item,null);
            XHolder holder=new XHolder(item);
            return holder;
        }

        @Override
        public void onBindViewHolder(XHolder holder, final int position) {

            //同意退款
            holder.mBtnAgree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(data!=null && data.size()>0 && data.get(position).getId()!=0){
                        refund(String.valueOf(data.get(position).getId()),"12");
                    }
                }
            });

            //拒绝退款
            holder.mBtnDeny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(data!=null && data.size()>0 && data.get(position).getId()!=0){
                        refund(String.valueOf(data.get(position).getId()),"13");
                    }
                }
            });

            //异步查看买家信息
            holder.mTVQueryBuyer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(data!=null && data.size()>0 && data.get(position).getId()!=0){
                        queryBuyerDetail(String.valueOf(data.get(position).getId()));
                    }
                }
            });


            //设置下单时间
            holder.mTVPaytime.setText(data.get(position).getInsertTime());
            //设置订单编号
            holder.mTVOrderNum.setText(data.get(position).getName());
            //设置购买的商品列表
            MyLVAdapter myLVAdapter=new MyLVAdapter(data.get(position).getGoodsBeanList());
            holder.mLV.setAdapter(myLVAdapter);
            ListViewUtil.setListViewHeightBasedOnChildren(holder.mLV);

            /**
             * 小计
             */
            holder.mTVgoodspay.setText("￥"+NumformatUtil.save2(  data.get(position).getTotalprice()+data.get(position).getDisvalue()-data.get(position).getShipPrice()  ));

            /**
             * 配送费
             */
            holder.mTVShipprice.setText("￥"+NumformatUtil.save2(data.get(position).getShipPrice()));

            /**
             * 优惠
             */
            holder.mTVDisprice.setText("￥"+NumformatUtil.save2(data.get(position).getDisvalue()));

            /**
             * 实付
             */
            holder.mTVtotalPay.setText("￥"+NumformatUtil.save2(data.get(position).getTotalprice()));
            //设置订单状态
            switch (data.get(position).getState()){
                //订单已经失效
                case 1:
                    holder.mTVState.setText("订单已经失效");
                    break;
                //等待买家付款
                case 5:
                    holder.mTVState.setText("等待买家付款");
                    break;
                //买家已付款
                case 10:
                   holder.mTVState.setText("买家已付款");
                    break;

                //买家申请退款
                case 11:
                    holder.mTVState.setText("买家申请退款");
                    break;

                //店家同意付款
                case 12:
                    holder.mTVState.setText("商家同意退款");
                    break;

                //退款失败
                case 13:
                    holder.mTVState.setText("退款失败");
                    break;

                //退款成功
                case 14:

                    holder.mTVState.setText("退款成功");
                    break;

                //等待商家接单
                case 15:
                    holder.mTVState.setText("等待商家接单");
                    break;
                //商家拒绝接单
                case 16:
                    holder.mTVState.setText("已拒绝接单");
                    break;

                //商家已接单
                case 17:
                   holder.mTVState.setText("商家已接单");

                    break;

                //商品配送中
                case 18:
                    holder.mTVState.setText("正在配送中");
                    break;

                //商品已送达
                case 19:
                    holder.mTVState.setText("已送达");
                    break;
                //已消费
                case 20:

                    holder.mTVState.setText("已核销");
                    break;
                //等待骑手接单
                case 21:
                    holder.mTVState.setText("等待骑手接单");
                    break;
                //等待配送
                case 22:
                    holder.mTVState.setText("等待配送");
                    break;

                //已下单,送水/洗衣 提交订单后的状态
                case 30:

                    holder.mTVState.setText("买家已下单，等待送达");
                    break;

                //已收件,店家更新，录入衣服信息到订单
                case 40:

                    holder.mTVState.setText("已收件");
                    break;

                default:

                    holder.mTVState.setText("订单状态错误");
                    break;

            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    class XHolder extends RecyclerView.ViewHolder {
        TextView mTVPaytime,mTVOrderNum,mTVState,
                mTVDisprice,mTVQueryBuyer,mTVShipprice,mTVtotalPay,mTVgoodspay;
        Button mBtnAgree,mBtnDeny;
        ListView mLV;

        public XHolder(View itemView) {
            super(itemView);
            mBtnAgree= (Button) itemView.findViewById(R.id.item_refund_order_btn_agree);
            mBtnDeny= (Button) itemView.findViewById(R.id.item_refund_order_btn_deny);
            mTVPaytime= (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_paytime);
            mTVOrderNum= (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_order_num);
            mLV= (ListView) itemView.findViewById(R.id.item_of_fg_dispose_lv_goods_list);
            mTVDisprice= (TextView) itemView.findViewById(R.id.item_of_act_refund_tv_disprice);
            mTVQueryBuyer= (TextView) itemView.findViewById(R.id.item_dispose_tv_query_buyer);
            mTVState= (TextView) itemView.findViewById(R.id.item_refund_order_tv_state);
            mTVShipprice= (TextView) itemView.findViewById(R.id.item_of_act_refund_tv_shipprice);
            mTVtotalPay= (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_total_pay);
            mTVgoodspay= (TextView) itemView.findViewById(R.id.item_of_fg_dispose_tv_goods_pay);
        }
    }

    class MyLVAdapter extends BaseAdapter {
        List <GoodsBean> gbList;
        MyLVAdapter(List<GoodsBean> gbList){
            this.gbList=gbList;
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

            MyLVAdapter.Holder h;

            if(convertView==null){
                convertView=LayoutInflater.from(RefundMagActivity.this).inflate(R.layout.item_goods_list_in_order,null);
                h=new MyLVAdapter.Holder();
                h.mTVName= (TextView) convertView.findViewById(R.id.item_goods_list_in_order_tv_name);
                h.mTVCount= (TextView) convertView.findViewById(R.id.item_goods_list_in_order_tv_count);
                h.mTVPrice= (TextView) convertView.findViewById(R.id.item_goods_list_in_order_tv_price);
                convertView.setTag(h);
            }else{
                h= (MyLVAdapter.Holder) convertView.getTag();
            }

            h.mTVName.setText(gbList.get(position).getName());
            h.mTVCount.setText("*"+gbList.get(position).getCount());
            h.mTVPrice.setText("￥"+NumformatUtil.save2(gbList.get(position).getCount()*gbList.get(position).getPrice()));

            return convertView;
        }

        class Holder{
            TextView mTVName,mTVCount,mTVPrice;
        }

    }

    /**
     * 查看买家信息
     * @param ofId
     */
    public void queryBuyerDetail(String ofId){
        RequestParams params=new RequestParams(BnUrl.getBuyerByOrderId);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie",""));
        params.addBodyParameter("ofId",ofId);
        params.addBodyParameter("jf","photo");
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        JSONObject user=response.getJSONObject("user");
                        int id=user.getInt("id");//用户 id
                        String username=user.getString("username");//用户名
                        String phone=user.getString("phone");//电话
                        String url=user.getJSONObject("photo").getString("url");

                        View v=LayoutInflater.from(RefundMagActivity.this).inflate(R.layout.layout_order_of_buyer,null);
                        ImageView mIV= (ImageView) v.findViewById(R.id.layout_order_of_buyer_iv_icon);
                        TextView mTVName= (TextView) v.findViewById(R.id.layout_order_of_buyer_tv_name);
                        TextView mTVTel= (TextView) v.findViewById(R.id.layout_order_of_buyer_tv_tel);

                        mTVName.setText(username);
                        mTVTel.setText("Tel:"+phone);
                        Picasso.with(RefundMagActivity.this).load(url).resize(DpUtils.dpToPx(RefundMagActivity.this,60),DpUtils.dpToPx(RefundMagActivity.this,60)).error(R.mipmap.icon_portrait_load_failed).into(mIV);

                        android.app.AlertDialog.Builder b=new android.app.AlertDialog.Builder(RefundMagActivity.this);
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
     * 处理退款
     * @param oId
     * @param state 12同意退款 13拒绝退款，退款失败
     */
    private void refund(final String oId, final String state){
        RequestParams params=new RequestParams(BnUrl.dataModifyUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie",""));
        params.addBodyParameter("ctype","orderform");
        params.addBodyParameter("data","{id:"+oId+",state:"+state+"}");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                          initData();
                            //通知用户，商家同意退款
                            if("12".equals(state)){
                                PushNotifyUtil.ShopPushToBuyer(oId, "8", new PushNotifyUtil.PushResultCallack() {
                                    @Override
                                    public void pushSuccess() {

                                    }

                                    @Override
                                    public void pushFaild() {

                                    }
                                });
                            }
                            //通知用户，商家拒绝退款
                            if("13".equals(state)){
                                PushNotifyUtil.ShopPushToBuyer(oId, "9", new PushNotifyUtil.PushResultCallack() {
                                    @Override
                                    public void pushSuccess() {

                                    }

                                    @Override
                                    public void pushFaild() {

                                    }
                                });
                            }
                        }else {
                            android.app.AlertDialog.Builder b=new android.app.AlertDialog.Builder( RefundMagActivity.this);
                            b.setTitle("提示");
                            b.setCancelable(true);
                            b.setMessage("退款处理失败");
                            b.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PushNotifyUtil.ShopPushToBuyer(oId, "9", new PushNotifyUtil.PushResultCallack() {
                                        @Override
                                        public void pushSuccess() {

                                        }

                                        @Override
                                        public void pushFaild() {

                                        }
                                    });
                                }
                            });
                            b.show();
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

}
