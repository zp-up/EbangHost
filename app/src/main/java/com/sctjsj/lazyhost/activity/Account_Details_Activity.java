package com.sctjsj.lazyhost.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.bean.AccountdetailBean;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.ProgressUtil;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.RefreshHandler;

public class Account_Details_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyRecyclerViewAdapter recyclerViewAdapter;
    private List<AccountdetailBean.ResultListBean> listBeans = new ArrayList<>();
    private int pageIndex = 1;
    private ProgressUtil pUtil;
    private QRefreshLayout zhanghumingxishuaxinkongjian;
    private MyApp app;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_account__details_);
        findViewById(R.id.kk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Account_Details_Activity.this.finish();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        app = (MyApp) getApplication();
        //初始化数据
        initData();


        zhanghumingxishuaxinkongjian = (QRefreshLayout) findViewById(R.id.zhanghumingxishuaxinkongjian);
        zhanghumingxishuaxinkongjian.setLoadMoreEnable(true);
        zhanghumingxishuaxinkongjian.setRefreshHandler(new RefreshHandler() {

            @Override
            public void onRefresh(QRefreshLayout refresh) {
                pageIndex = 1;
                initData();
            }

            @Override
            public void onLoadMore(QRefreshLayout refresh) {
                LoadMoreData();
            }
        });

        for (int e = 0; e <= 10; e++) {
            ArrayList<String> arrayList = new ArrayList<>();
        }
    }


    private void initData() {
        pageIndex = 1;
        if (listBeans.size() != 0){
            listBeans.clear();
            recyclerViewAdapter.notifyDataSetChanged();
        }
        RequestParams params = new RequestParams(BnUrl.ServerIp+"/user/orderDetailed$ajax.htm");
        params.setUseCookie(true);

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.e("账户明细页面返回的数据", "" + response.toString());
                if (response != null) {
                    try {
                        //Toast.makeText(Account_Details_Activity.this,"执行成功：",Toast.LENGTH_SHORT).show();
                        AccountdetailBean accountdetailBean = new Gson().fromJson(response.toString(), AccountdetailBean.class);
                        listBeans.addAll(accountdetailBean.getResultList());
                        recyclerView.setLayoutManager(new LinearLayoutManager(Account_Details_Activity.this, LinearLayoutManager.VERTICAL, false));
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        // jj.addItemDecoration( new DividerGridItemDecoration(this ));//设置条目分割线
                        if (recyclerViewAdapter == null) {
                            recyclerViewAdapter = new MyRecyclerViewAdapter(listBeans);
                            recyclerView.setAdapter(recyclerViewAdapter);
                        } else {
                            recyclerViewAdapter.notifyDataSetChanged();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("TAG", "错误:" + ex.toString());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                zhanghumingxishuaxinkongjian.LoadMoreComplete();
                zhanghumingxishuaxinkongjian.refreshComplete();
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


    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        List<AccountdetailBean.ResultListBean> ll;

        MyRecyclerViewAdapter(List<AccountdetailBean.ResultListBean> jj) {
            ll = jj;
        }

        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_account_details_item, parent, false);
            return new ViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
            holder.shouru.setText(ll.get(position).getContent());
            holder.riqi.setText(ll.get(position).getInsertTime());

            Log.e("适配器大小", "" + ll.size());
            if (ll.get(position).getInOut() == 1) {
                holder.shuzi.setText("+" + ll.get(position).getPrice());
            } else {
                holder.shuzi.setText("-" + ll.get(position).getPrice());
            }
        }

        @Override
        public int getItemCount() {
            return ll.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView shouru, shuzi, riqi;

            ViewHolder(View itemView) {
                super(itemView);
                shouru = (TextView) itemView.findViewById(R.id.zengjiashouru);
                shuzi = (TextView) itemView.findViewById(R.id.shuzizengjia);
                riqi = (TextView) itemView.findViewById(R.id.riqi);
            }
        }
    }


    //滑动加载更多
    private void LoadMoreData() {

        RequestParams params = new RequestParams(BnUrl.ServerIp + "/user/orderDetailed$ajax.htm");
        params.setUseCookie(false);
        params.addBodyParameter("size", "8");
        params.addBodyParameter("pageIndex", String.valueOf(pageIndex));
        params.addBodyParameter("ctype", "orderform");
        params.addBodyParameter("orderby", "id desc");
        params.addBodyParameter("cond", "{store:{id:" + app.getCurrentUser().getShopId() + "},in_state:\"10,11,15,17\"}");
        params.addBodyParameter("jf", "user1|store|goodscart|goods|addr|area|parent");
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
                AccountdetailBean ii = new Gson().fromJson(response.toString(), AccountdetailBean.class);
                if (ii.getResultList().size() <= 0) {
                    return;
                } else {
                    listBeans.addAll(ii.getResultList());
                    pageIndex++;
                }
                if (recyclerViewAdapter == null) {
                    recyclerViewAdapter = new MyRecyclerViewAdapter(listBeans);
                    recyclerView.setAdapter(recyclerViewAdapter);
                } else {
                    recyclerViewAdapter.notifyDataSetChanged();
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
                zhanghumingxishuaxinkongjian.LoadMoreComplete();
                zhanghumingxishuaxinkongjian.refreshComplete();
                pUtil.dismissProgress();
            }
        });


    }

}
