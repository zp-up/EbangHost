package com.sctjsj.ebanghost.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.adapter.MainShopVPAdapter;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.bean.Dish;
import com.sctjsj.ebanghost.bean.DishMenu;
import com.sctjsj.ebanghost.bean.SettleCartBean;
import com.sctjsj.ebanghost.bean.ShopCart;
import com.sctjsj.ebanghost.fragment.CommentFgInShop;
import com.sctjsj.ebanghost.fragment.DetailFgInShop;
import com.sctjsj.ebanghost.fragment.GoodsFgInShop;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.ProgressUtil;
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

/**
 * Created by Chris-Jason on 2016/11/4.
 */
public class MainShopActivity extends AppCompatActivity {
    private String TAG="MainShopActivity";
    private MyApp app;
    private ProgressUtil pUtil;
    private List<String> tabList;
    private List<Fragment>fgList;
    private FragmentStatePagerAdapter vpAdapter;
    //商品
    private GoodsFgInShop goodsFgInShop;
    //评论
    private CommentFgInShop commentFgInShop;
    //详情
    private DetailFgInShop detailFgInShop;
    //接收店铺ID
    private int shopId;
    //店铺类型
    private int shopType;
    private List<DishMenu>dishMenuList=new ArrayList<>();

    @Bind(R.id.activity_main_coordinate)CoordinatorLayout mCor;
    @Bind(R.id.activity_main_shop_rl_parent)RelativeLayout mRLParent;
    //TabLayout
    @Bind(R.id.activity_main_shop_tab)TabLayout mTab;
    //ViewPager
    @Bind(R.id.activity_main_shop_view_pager)ViewPager viewPager;
    //底部LL
    @Bind(R.id.activity_main_ll_bottom)LinearLayout mLLBottom;
    @Bind(R.id.shopping_cart_layout)FrameLayout frameLayout;
    @Bind(R.id.main_layout)LinearLayout mMainLayout;

    //展示的信息
    @Bind(R.id.activity_main_shop_tv_shop_name)TextView mTVShopName;//店名
    @Bind(R.id.shop_merchants_iv)ImageView mIVShopIcon;//店铺 logo
    @Bind(R.id.activity_main_shop_iv_big_background)ImageView mIVBigBackground;//大背景

    /**
     * 购物车
     */
    @Bind(R.id.shopping_cart)ImageView shoppingCartView;//购物车
    @Bind(R.id.shopping_cart_total_tv)TextView totalPriceTextView;//总价
    @Bind(R.id.shopping_cart_total_num) TextView totalPriceNumTextView;//总数量


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_shop);
        ButterKnife.bind(this);
        app= (MyApp) getApplication();
        app.addActivity(this);

        //获取店铺 id
        shopId= getIntent().getIntExtra("shopId",-1);
        pUtil=new ProgressUtil(this);
        dishMenuList=new ArrayList<>();
        //查询店铺信息
        pullShopInfoById(String.valueOf(shopId));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        app.removeActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 初始化viewpager
     */
    private void initViewpager(){
        //1.初始化tab
        initTablayout();
        //2.初始化viewpager的数据
        initViewpagerData();
        //3.viewpager设置适配器
        initVPAdapter();
        //4.关联Viewpager和Tabayout
        mTab.setupWithViewPager(viewPager);
        mTab.setTabsFromPagerAdapter(vpAdapter);
        vpAdapter.notifyDataSetChanged();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==0){
                   // CoordinatorLayout.LayoutParams p= (CoordinatorLayout.LayoutParams) mCor.getLayoutParams();
                    //p.setMargins(0,0,0, DpUtils.pxToDp(MainShopActivity.this,50));
                    mLLBottom.setVisibility(View.VISIBLE);
                    frameLayout.setVisibility(View.VISIBLE);
                }else{
                    //mLLBottom.setVisibility(View.GONE);
                    //frameLayout.setVisibility(View.GONE);
                    //CoordinatorLayout.LayoutParams p= (CoordinatorLayout.LayoutParams) mCor.getLayoutParams();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    /*
    初始化Tab指示器
     */
    private void initTablayout(){
        if(tabList==null){
            tabList=new ArrayList<>();
            tabList.add("商品");
            tabList.add("评价");
            tabList.add("详情");
        }
        //指示器选中颜色
        mTab.setSelectedTabIndicatorColor(Color.parseColor("#33aab4"));
    }

    /**
     * 初始化viewpager数据
     */
    private void initViewpagerData(){
        if(fgList==null){
            fgList=new ArrayList<>();
        }
        goodsFgInShop=new GoodsFgInShop();
        commentFgInShop=new CommentFgInShop();
        detailFgInShop=new DetailFgInShop();

        fgList.add(goodsFgInShop);
        fgList.add(commentFgInShop);
        fgList.add(detailFgInShop);
    }

    /**
     * 初始化vp适配器
     */
    private void initVPAdapter(){
        if(vpAdapter==null){
            vpAdapter=new MainShopVPAdapter(getSupportFragmentManager(),tabList,fgList);
        }
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(vpAdapter);
    }

    @OnClick({R.id.shop_back_ll,R.id.main_shop_btn_settle,R.id.activity_main_shop_iv_add_collect})
    public void mainShopClick(View view){

        switch (view.getId()){
            //返回
            case R.id.shop_back_ll:
                finish();
                break;
            //结算
            case R.id.main_shop_btn_settle:
                Snackbar.make(mRLParent,"店铺预览不提供购买操作", Snackbar.LENGTH_SHORT).show();
                break;
            //收藏店铺
            case R.id.activity_main_shop_iv_add_collect:
                Snackbar.make(mRLParent,"店铺预览不提供收藏操作",Snackbar.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * MaiShopActivity 和 GoodsFgInShop 之间传递视图
     * @return
     */
    public View getMainLayout() {
      return mMainLayout;
    }

    public View getShoppingCartView(){
        return shoppingCartView;
    }

    public View getTotalPriceTextView(){
        return totalPriceTextView;
    }

    public View getTotalPriceNumTextView(){
        return  totalPriceNumTextView;
    }

    /**
     * 把店铺id传递给 GoodsFgInShop
     * @return
     */
    public int getShopId(){
       return shopId;
    }

    /**
     * 生成购物车对象
     * @return
     */
    public SettleCartBean settleCartGene(){
        //获取购物车对象
        ShopCart s=goodsFgInShop.getShopCart();
        //所购买的商品列表
        List<Dish>dishes=s.getDishList();
        SettleCartBean scb=new SettleCartBean(shopId,app.getCurrentUser().getUserId(),dishes);
        return scb;
    }

    /**
     * 获取店铺所有信息
     */

    public void pullShopInfoById(String id){
        RequestParams paramas=new RequestParams(BnUrl.getShopInfoById);
        paramas.setUseCookie(false);
        paramas.addBodyParameter("size","999999");
        paramas.addBodyParameter("ctype","store");
        //已经上架的商品
        paramas.addBodyParameter("cond","{id:"+id+"}");
        paramas.addBodyParameter("jf","storeLogo|usergoosclass|goodsUgc|photo");
        x.http().post(paramas, new Callback.ProgressCallback<JSONObject>() {
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
                    //店铺的所有信息
                    try {
                        JSONObject storeInfo=response.getJSONArray("resultList").getJSONObject(0);
                        //店铺地址
                        String storeAddress=storeInfo.getString("storeAddress");
                        //经度
                        String longitude=storeInfo.getString("longitude");
                        //纬度
                        String latitude=storeInfo.getString("latitude");
                        //店铺名称
                        String storeName=storeInfo.getString("storeName");
                        mTVShopName.setText(storeName);
                        //店铺 id
                        String id=storeInfo.getString("id");
                        //电话
                        String telephone=storeInfo.getString("telephone");
                        //店铺类型
                        int type=storeInfo.getInt("type");
                        shopType=type;
                        //店铺状态
                        int storeStatus=storeInfo.getInt("storeStatus");
                        //店铺 logo 地址
                        String storeLogoUrl=storeInfo.getJSONObject("storeLogo").getString("url");
                        Picasso.with(MainShopActivity.this).load(storeLogoUrl).into(mIVShopIcon);
                        Picasso.with(MainShopActivity.this).load(storeLogoUrl).into(mIVBigBackground);
                        /**
                         * 商品分类
                         */
                        JSONArray usergoosclass=storeInfo.getJSONArray("usergoosclass");
                        for(int j=0;j<usergoosclass.length();j++){
                            JSONObject dishmenuInfo=usergoosclass.getJSONObject(j);//一个分类和下面的所有商品
                            int display=dishmenuInfo.getInt("display");//分类是否展示
                            if(1==display){
                                int dishmenuId=dishmenuInfo.getInt("id");//分类 id
                                String className=dishmenuInfo.getString("className");//分类名称
                                JSONArray goodsUgc=dishmenuInfo.getJSONArray("goodsUgc");//分类下的商品列表
                                ArrayList<Dish>dList=new ArrayList<Dish>();
                                for(int x=0;x<goodsUgc.length();x++){
                                    JSONObject goodsInfo=goodsUgc.getJSONObject(x);
                                    int isDelete=goodsInfo.getInt("isDelete");
                                    int isCertify=goodsInfo.getInt("isCertify");//是否审核通过1,审核中 2，审核通过
                                    if(1==isDelete){
                                        int goodsId=goodsInfo.getInt("id");//商品 id
                                        String details=goodsInfo.getString("details");//商品描述
                                        double disPrice=goodsInfo.getDouble("disPrice");//商品折扣价
                                        String name=goodsInfo.getString("name");//商品名称
                                        double price=goodsInfo.getDouble("price");//原价
                                        int salenum=goodsInfo.getInt("salenum");//已售出
                                        int status=goodsInfo.getInt("status");//商品状态
                                        String logoUrl=goodsInfo.getJSONObject("photo").getString("url");
                                        if(2==isCertify){
                                            if(2==status){
                                                Dish dish=new Dish();
                                                dish.setId(goodsId);
                                                dish.setCurrentPrice(price);
                                                dish.setDiscount(disPrice);
                                                dish.setUrl(logoUrl);
                                                dish.setSalenum(salenum);
                                                dish.setDishName(name);
                                                dish.setDishRemain(10);
                                                dList.add(dish);

                                            }
                                        }

                                    }

                                }
                                DishMenu dm=new DishMenu();
                                dm.setDishList(dList);
                                dm.setDishMenuId(dishmenuId);
                                dm.setMenuName(className);
                                dishMenuList.add(dm);
                            }//display==1
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {

                    }

                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainShopActivity.this);
                builder.setCancelable(false);
                builder.setMessage("店铺信息加载失败");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.show();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                pUtil.dismissProgress();
                if(dishMenuList.size()>0){
                    //初始化 ViewPager
                    initViewpager();
                }else {
                    //店铺内无商品时隐藏购买结算栏
                    mLLBottom.setVisibility(View.GONE);
                    frameLayout.setVisibility(View.GONE);

                    AlertDialog.Builder builder=new AlertDialog.Builder(MainShopActivity.this);
                    builder.setMessage("店铺内暂无可售商品");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.show();
                }

            }

        });
    }

    /**
     * 返回封装好的数据集合--》goodsFgInShop
     * @return
     */
    public List<DishMenu> getDishMenuList(){
       return dishMenuList;
    }

}
