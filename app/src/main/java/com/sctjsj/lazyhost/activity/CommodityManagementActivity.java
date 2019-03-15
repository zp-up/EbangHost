package com.sctjsj.lazyhost.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.amap.api.maps.model.Poi;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import android.support.v7.widget.RecyclerView.ViewHolder;

import com.jaeger.library.StatusBarUtil;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.bean.ClassifyBean;
import com.sctjsj.lazyhost.bean.GoodsBean;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.DpUtils;
import com.sctjsj.lazyhost.util.HorizontalListView;
import com.sctjsj.lazyhost.util.LogUtil;
import com.sctjsj.lazyhost.util.NumformatUtil;
import com.sctjsj.lazyhost.util.ProgressUtil;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.zcw.togglebutton.ToggleButton;


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

/**
 * Created by Admin on 2016/11/16.
 */

public class CommodityManagementActivity extends AppCompatActivity {
    private MyApp app;
    private ProgressUtil pUtil;
    private List<GoodsBean> data;
    private int pageIndex = 1;
    private MyRVAdapter adapter;

    @Bind(R.id.activity_commodity_manage_ll_parent)
    LinearLayout mLLParent;
    @Bind(R.id.activity_commodity_rv)
    RecyclerView mRecyclerview;
    @Bind(R.id.activity_commodity_refresh)
    QRefreshLayout refresh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity_manangement);
        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, null);

        app = (MyApp) getApplication();

        ButterKnife.bind(this);
        pUtil = new ProgressUtil(this);
        data = new ArrayList<>();

        refresh.setLoadMoreEnable(true);
        refresh.setRefreshHandler(new RefreshHandler() {
            @Override
            public void onRefresh(QRefreshLayout refresh) {
                initData();
            }

            @Override
            public void onLoadMore(QRefreshLayout refresh) {
                loadMoreData();
            }
        });
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.activity_commodity_btn_add_goods, R.id.activity_commodity_btn_classify_mag,
            R.id.activity_commodity_mag_iv_back})
    public void buttonListen(View view) {
        switch (view.getId()) {
            //添加商品
            case R.id.activity_commodity_btn_add_goods:
                Intent intent = new Intent(this, AddCommodityActivity.class);
                startActivity(intent);
                this.overridePendingTransition(R.anim.activity_anim_start, R.anim.activity_anim_end);
                break;
            //分类管理
            case R.id.activity_commodity_btn_classify_mag:
                Intent intent1 = new Intent(this, ClassifyManagementActivity.class);
                startActivity(intent1);
                this.overridePendingTransition(R.anim.activity_anim_start, R.anim.activity_anim_end);
                break;
            //返回
            case R.id.activity_commodity_mag_iv_back:
                finish();
                this.overridePendingTransition(R.anim.activity_anim_start, R.anim.activity_anim_end);
                break;
        }
    }

    public class MyRVAdapter extends RecyclerView.Adapter<MyRVAdapter.MyXHolder> {

        @Override
        public MyXHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_commod_manager_item, null);
            MyXHolder holder = new MyXHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyXHolder holder, final int position) {
            //短按修改商品
            holder.mLLParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder b = new AlertDialog.Builder(CommodityManagementActivity.this);
                    b.setMessage("前往修改商品信息？");
                    b.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(CommodityManagementActivity.this, ModifyCommodityActivity.class);
                            intent.putExtra("id", data.get(position).getId());
                            intent.putExtra("index", position);
                            startActivityForResult(intent, 2018);
                        }
                    });
                    b.show();

                }
            });

            //长按删除商品
            holder.mLLParent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CommodityManagementActivity.this);
                    builder.setTitle("删除提示");
                    builder.setMessage("确认删除该商品？");
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteGoods(data.get(position).getId());
                        }
                    });
                    builder.show();
                    return true;
                }
            });


            Glide.with(CommodityManagementActivity.this).load(data.get(position).getLogoUrl()).error(R.mipmap.icon_portrait_load_failed).into(holder.mIVLogo);
            holder.mTVName.setText(data.get(position).getName());
            holder.mTVPrice.setText("￥" + NumformatUtil.save2(data.get(position).getPrice()));

            //1.审核中  2.审核通过
            if (1 == data.get(position).getIsCertify()) {
                holder.toggle.setVisibility(View.GONE);
                holder.mTVStatue.setVisibility(View.GONE);
                holder.mTVCertify.setVisibility(View.VISIBLE);
                holder.mTVCertify.setText("正在审核中");
            }

            if (2 == data.get(position).getIsCertify()) {
                holder.toggle.setVisibility(View.VISIBLE);
                holder.mTVStatue.setVisibility(View.VISIBLE);
                holder.mTVCertify.setVisibility(View.GONE);

                /**
                 * 2-已上架
                 * 1-仓库中
                 * -1-已下架
                 * -2-违规下架
                 */

                switch (data.get(position).getStatus()) {
                    case 2:
                        holder.mTVStatue.setText("已上架");
                        holder.toggle.setToggleOn();
                        holder.toggle.setEnabled(true);
                        break;
                    case -1:
                        holder.mTVStatue.setText("已下架");
                        holder.toggle.setToggleOff();
                        holder.toggle.setEnabled(true);
                        break;
                    case 1:
                        holder.mTVStatue.setText("仓库中");
                        holder.toggle.setToggleOff();
                        holder.toggle.setEnabled(true);
                        break;
                    case -2:
                        holder.mTVStatue.setText("违规下架");
                        holder.toggle.setToggleOff();
                        holder.toggle.setEnabled(false);
                        break;
                }

                holder.toggle.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
                    @Override
                    public void onToggle(boolean on) {
                        //商品上架
                        if (on) {
                            changeState(String.valueOf(data.get(position).getId()), "2", position);

                        } else {

                            //商品下架
                            changeState(String.valueOf(data.get(position).getId()), "-1", position);
                        }
                    }
                });

            }

            holder.mTVDetails.setText(data.get(position).getDetails());
            holder.listView.setAdapter(new MyRVAdapter.HmyAdapter(data.get(position).getClassifyBeanList()));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyXHolder extends ViewHolder {
            ImageView mIVLogo;
            LinearLayout mLLParent;
            TextView mTVName, mTVPrice, mTVStatue, mTVDetails, mTVCertify;
            com.zcw.togglebutton.ToggleButton toggle;
            HorizontalListView listView;

            public MyXHolder(View itemView) {
                super(itemView);
                mIVLogo = (ImageView) itemView.findViewById(R.id.item_commodity_mag_iv_goods_logo);
                mTVName = (TextView) itemView.findViewById(R.id.item_commodity_mag_tv_goods_name);
                mTVPrice = (TextView) itemView.findViewById(R.id.item_commodity_mag_tv_price);
                mTVStatue = (TextView) itemView.findViewById(R.id.item_commodity_mag_tv_status);
                mTVDetails = (TextView) itemView.findViewById(R.id.item_commodity_mag_tv_details);
                toggle = (ToggleButton) itemView.findViewById(R.id.item_commodity_mag_toggle);
                listView = (HorizontalListView) itemView.findViewById(R.id.my_horizontal_lv);
                mLLParent = (LinearLayout) itemView.findViewById(R.id.activity_commodity_ll_parent);
                mTVCertify = (TextView) itemView.findViewById(R.id.item_activity_commend_mag_tv_certify);
            }
        }

        //分类适配器
        private class HmyAdapter extends BaseAdapter {
            private List<ClassifyBean> classifyBeanList;
            Context context;

            public HmyAdapter(List<ClassifyBean> classifyBeanList) {
                this.classifyBeanList = classifyBeanList;
            }

            @Override
            public int getCount() {
                return classifyBeanList == null ? 0 : classifyBeanList.size();
            }

            @Override
            public Object getItem(int position) {
                return classifyBeanList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                XHolder xh;
                if (view == null) {
                    view = LayoutInflater.from(CommodityManagementActivity.this).inflate(R.layout.activity_commod_manage_item_item, null);
                    xh = new HmyAdapter.XHolder();
                    xh.mTVClassify = (TextView) view.findViewById(R.id.activity_commodity_manage_item_tv_classify);
                    view.setTag(xh);
                } else {
                    xh = (XHolder) view.getTag();
                }
                xh.mTVClassify.setText(classifyBeanList.get(position).getName());
                return view;
            }

            class XHolder {
                TextView mTVClassify;
            }
        }

    }

    /**
     * 初始化店铺下的商品列表
     */
    private void initData() {
        pageIndex = 1;
        data.clear();
        final RequestParams params = new RequestParams(BnUrl.queryGoodsListofShopUrl);
        params.setUseCookie(false);
        params.addBodyParameter("pageIndex", String.valueOf(pageIndex));
        params.addBodyParameter("size", "8");
        params.addHeader("Cookie", "none-cookie");
        params.addBodyParameter("orderby", "id desc");
        params.addBodyParameter("ctype", "goods");
        params.addBodyParameter("cond", "{store:{id:" + app.getCurrentUser().getShopId() + "},isDelete:1}");
        params.addBodyParameter("jf", "photo|userGoodsClass");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                if (response != null) {
                    try {
                        Log.e("TAG", "商品:" + response.getJSONArray("resultList").length());
                        boolean result = response.getBoolean("result");
                        if (result) {
                            //所有商品集合
                            JSONArray resultList = response.getJSONArray("resultList");
                            if (resultList.length() > 0) {
                                for (int i = 0; i < resultList.length(); i++) {
                                    //完整的商品信息
                                    JSONObject g = resultList.getJSONObject(i);
                                    int isDelete = g.getInt("isDelete");//是否删除 1，没有删除，2已经删除
                                    int isCertify = g.getInt("isCertify");//商品是否审核通过 1，审核中 2审核通过
                                    //只显示没删除的商品
                                    if (1 == isDelete) {
                                        int goodsId = g.getInt("id");//商品 id 
                                        String name = g.getString("name");//商品名称 
                                        double price = g.getDouble("price");//商品价格 
                                        String details = g.getString("details");//商品描述 
                                        String logo = g.getJSONObject("photo").getString("url");//商品 logo 
                                        int status = g.getInt("status");//商品状态 
                                        /**
                                         * ugc 分类
                                         */
                                        JSONArray userGoodsClass = g.getJSONArray("userGoodsClass");
                                        GoodsBean gb = new GoodsBean();
                                        gb.setId(goodsId);
                                        gb.setName(name);
                                        gb.setPrice(price);
                                        gb.setDetails(details);
                                        gb.setLogoUrl(logo);
                                        gb.setStatus(status);
                                        gb.setIsCertify(isCertify);
                                        //循环自定义分类
                                        if (userGoodsClass.length() > 0) {
                                            List<ClassifyBean> classifyBeenList = new ArrayList<ClassifyBean>();
                                            for (int j = 0; j < userGoodsClass.length(); j++) {
                                                int display = userGoodsClass.getJSONObject(j).getInt("display");
                                                //只获取要显示的分类
                                                if (1 == display) {
                                                    int id = userGoodsClass.getJSONObject(j).getInt("id");//分类 id
                                                    String className = userGoodsClass.getJSONObject(j).getString("className");//分类名字
                                                    int level = userGoodsClass.getJSONObject(j).getInt("level");
                                                    //设置分类实体
                                                    ClassifyBean cfb = new ClassifyBean();
                                                    cfb.setDisplay(display);
                                                    cfb.setId(id);
                                                    cfb.setName(className);
                                                    cfb.setLevel(level);
                                                    classifyBeenList.add(cfb);
                                                }
                                            }
                                            //设置商品实体
                                            gb.setClassifyBeanList(classifyBeenList);
                                        }
                                        data.add(gb);

                                    }

                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if (data.size() > 0) {
                            pageIndex++;
                            if (adapter == null) {
                                adapter = new MyRVAdapter();
                                mRecyclerview.setAdapter(adapter);
                                mRecyclerview.setLayoutManager(new LinearLayoutManager(CommodityManagementActivity.this));
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            AlertDialog.Builder b = new AlertDialog.Builder(CommodityManagementActivity.this);
                            b.setTitle("提示");
                            b.setMessage("当前店铺中暂无商品，立即上传商品？");
                            b.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(CommodityManagementActivity.this, AddCommodityActivity.class);
                                    startActivity(intent);
                                }
                            });
                            b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            b.show();
                        }


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
                refresh.refreshComplete();
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

    private void loadMoreData() {

        final RequestParams params = new RequestParams(BnUrl.queryGoodsListofShopUrl);
        params.setUseCookie(false);
        params.addBodyParameter("pageIndex", String.valueOf(pageIndex));
        params.addBodyParameter("size", "8");
        params.addHeader("Cookie", "none-cookie");
        params.addBodyParameter("ctype", "goods");
        params.addBodyParameter("orderby", "id desc");
        params.addBodyParameter("cond", "{store:{id:" + app.getCurrentUser().getShopId() + "},isDelete:1}");
        params.addBodyParameter("jf", "photo|userGoodsClass");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if (response != null) {
                    try {
                        JSONArray resultList = response.getJSONArray("resultList");
                        if (resultList.length() > 0) {
                            for (int i = 0; i < resultList.length(); i++) {
                                //完整的商品信息
                                JSONObject g = resultList.getJSONObject(i);
                                int isDelete = g.getInt("isDelete");//是否删除 1，没有删除，2已经删除
                                int isCertify = g.getInt("isCertify");//商品是否审核通过 1，审核中 2审核通过
                                //只显示没删除的商品
                                if (1 == isDelete) {
                                    int goodsId = g.getInt("id");//商品 id 
                                    String name = g.getString("name");//商品名称 
                                    double price = g.getDouble("price");//商品价格 
                                    String details = g.getString("details");//商品描述 
                                    String logo = g.getJSONObject("photo").getString("url");//商品 logo 
                                    int status = g.getInt("status");//商品状态 
                                    /**
                                     * ugc 分类
                                     */
                                    JSONArray userGoodsClass = g.getJSONArray("userGoodsClass");
                                    GoodsBean gb = new GoodsBean();
                                    gb.setId(goodsId);
                                    gb.setName(name);
                                    gb.setPrice(price);
                                    gb.setDetails(details);
                                    gb.setLogoUrl(logo);
                                    gb.setStatus(status);
                                    gb.setIsCertify(isCertify);
                                    //循环自定义分类
                                    if (userGoodsClass.length() > 0) {
                                        List<ClassifyBean> classifyBeenList = new ArrayList<ClassifyBean>();
                                        for (int j = 0; j < userGoodsClass.length(); j++) {
                                            int display = userGoodsClass.getJSONObject(j).getInt("display");
                                            //只获取要显示的分类
                                            if (1 == display) {
                                                int id = userGoodsClass.getJSONObject(j).getInt("id");//分类 id
                                                String className = userGoodsClass.getJSONObject(j).getString("className");//分类名字
                                                int level = userGoodsClass.getJSONObject(j).getInt("level");
                                                //设置分类实体
                                                ClassifyBean cfb = new ClassifyBean();
                                                cfb.setDisplay(display);
                                                cfb.setId(id);
                                                cfb.setName(className);
                                                cfb.setLevel(level);
                                                classifyBeenList.add(cfb);
                                            }
                                        }
                                        //设置商品实体

                                        gb.setClassifyBeanList(classifyBeenList);
                                    }
                                    data.add(gb);

                                }//if

                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if (data.size() > 0) {
                            pageIndex++;
                            refresh.LoadMoreComplete();
                            if (adapter == null) {
                                adapter = new MyRVAdapter();
                                mRecyclerview.setAdapter(adapter);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            AlertDialog.Builder b = new AlertDialog.Builder(CommodityManagementActivity.this);
                            b.setTitle("提示");
                            b.setMessage("当前店铺中暂无商品，立即上传商品？");
                            b.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(CommodityManagementActivity.this, AddCommodityActivity.class);
                                    startActivity(intent);
                                }
                            });
                            b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            b.show();
                        }

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
                refresh.LoadMoreComplete();
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
     * 改变商品状态
     * 2-正在营业
     * 3.关闭
     */
    private void changeState(final String goodsId, final String s, final int posi) {
        RequestParams params = new RequestParams(BnUrl.modifyGoodsStatusUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie", app.getSpf().getString("cookie", "none-cookie"));
        params.addBodyParameter("ctype", "goods");
        params.addBodyParameter("data", "{id:" + goodsId + ",status:" + s + "}");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if (response != null) {
                    try {
                        LogUtil.e(response.toString());
                        boolean result = response.getBoolean("result");
                        //操作成功
                        if (result) {
                            Log.e("上下架", s);
                            //上架成功
                            if ("2".equals(s)) {
                                data.get(posi).setStatus(2);
                                adapter.notifyItemChanged(posi);
                            }
                            //下架成功
                            if ("-1".equals(s)) {
                                data.get(posi).setStatus(-1);
                                adapter.notifyItemChanged(posi);
                            }
                        } else {
                            adapter.notifyItemChanged(posi);
                            Snackbar.make(mLLParent, "商品状态修改失败", Snackbar.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                pUtil.dismissProgress();
                adapter.notifyItemChanged(posi);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                adapter.notifyItemChanged(posi);
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
     * 查询当前商品状态
     */
    private void queryGoodsState(String goodsId) {
        RequestParams paramas = new RequestParams(BnUrl.queryGoodsStatusUrl);
        paramas.setUseCookie(false);
        paramas.addHeader("Cookie", app.getSpf().getString("cookie", "none-cookie"));
        paramas.addBodyParameter("goodsId", goodsId);
        x.http().post(paramas, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                LogUtil.e(result.toString());
                try {
                    int status = result.getInt("status");

                    if (2 == status) {
                        Snackbar.make(mLLParent, "商品已上架", Snackbar.LENGTH_SHORT).show();
                    }
                    if (-1 == status) {

                        Snackbar.make(mLLParent, "商品已下架", Snackbar.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
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

            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });
    }

    private void deleteGoods(int goodsId) {
        RequestParams params = new RequestParams(BnUrl.deleteGoodsUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie", app.getSpf().getString("cookie", "none-cookie"));
        params.addBodyParameter("ctype", "goods");
        params.addBodyParameter("data", "{id:" + goodsId + ",isDelete:2}");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if (response != null) {
                    try {
                        boolean result = response.getBoolean("result");
                        if (result) {
                            Snackbar.make(mLLParent, "商品删除成功", Snackbar.LENGTH_SHORT).show();
                            initData();
                        } else {
                            Snackbar.make(mLLParent, "商品删除失败", Snackbar.LENGTH_SHORT).show();
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
                pUtil.dismissProgress();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2018 && resultCode == 12 && data != null) {
            Log.e("TAG","执行");
            if (data.getStringExtra("data") != null) {
                int index = data.getIntExtra("index",-1);
                String goodsBeanStr = data.getStringExtra("data");
                Gson gson = new Gson();
                GoodsBean goodsBean = gson.fromJson(goodsBeanStr,GoodsBean.class);
                Log.e("TAG","图片:"+goodsBean.getLogoUrl());
                if (CommodityManagementActivity.this.data.size() > index){
                    CommodityManagementActivity.this.data.set(index,goodsBean);
                    adapter.notifyItemChanged(index);
                }
            }
        }
    }
}

