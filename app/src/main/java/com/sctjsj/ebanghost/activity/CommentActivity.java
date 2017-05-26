package com.sctjsj.ebanghost.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.adapter.CommentListRVAdapter;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.bean.CommentBean;
import com.sctjsj.ebanghost.bean.UserBean;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.ProgressUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindInt;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.RefreshHandler;

public class CommentActivity extends Activity {
    private MyApp app;
    private int pageIndex=1;
    private CommentListRVAdapter commentListAdapter;
    private List<CommentBean> commentList=new ArrayList<>();
    private ProgressUtil pUtil;
    @Bind(R.id.act_comment_ll_remind)LinearLayout mLLRemind;
    @Bind(R.id.activity_comment_refresh)QRefreshLayout refresh;
    @Bind(R.id.activity_comment_rv)RecyclerView mRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ButterKnife.bind(this);

        app= (MyApp) getApplication();
        pUtil=new ProgressUtil(this);

        refresh.setLoadMoreEnable(true);
        refresh.setRefreshHandler(new RefreshHandler() {
            @Override
            public void onRefresh(QRefreshLayout refresh) {
                pullCommentInfo();
            }

            @Override
            public void onLoadMore(QRefreshLayout refresh) {
                loadMore();
            }
        });
        //加载评论信息
        pullCommentInfo();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    /**
     * 查询评论信息
     */
    private void pullCommentInfo(){
        pageIndex=1;
        commentList.clear();
        //www.lp-kd.com/pageSearch$ajax.htm?ctype=evaluate&jf=store|user1|photo&cond={store:{id:}}
        RequestParams params=new RequestParams(BnUrl.pullCommentsUrl);
        params.setUseCookie(false);
        params.addBodyParameter("orderby","id desc");
        params.addBodyParameter("ctype","evaluate");
        params.addBodyParameter("jf","store|user1|photo");
        params.addBodyParameter("pageIndex",String.valueOf(pageIndex));
        params.addBodyParameter("cond","{store:{id:"+app.getCurrentUser().getShopId()+"}}");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            JSONArray resultList=response.getJSONArray("resultList");
                            if(resultList.length()>0){
                                for (int x=0;x<resultList.length();x++){
                                    JSONObject comment=resultList.getJSONObject(x);
                                    int id=comment.getInt("id");//评论表 id
                                    String goodsEvaluateInfo=comment.getString("goodsEvaluateInfo");//对商品的评价
                                    String serviceEvaluateInfo=comment.getString("serviceEvaluateInfo");//服务评价
                                    int goodsEvaluate=comment.getInt("goodsEvaluate");
                                    int serviceEvaluate=comment.getInt("serviceEvaluate");
                                    String insertTime=comment.getString("insertTime");//发表时间

                                    JSONObject user=comment.getJSONObject("user1");
                                    int userId=user.getInt("id");
                                    String username=user.getString("username");

                                    String photoStr=user.getString("photo");
                                    String url=null;
                                    if(photoStr!=null && !"null".equals(photoStr) && !TextUtils.isEmpty(photoStr)){
                                        url =user.getJSONObject("photo").getString("url");
                                    }

                                    CommentBean cb=new CommentBean();
                                    cb.setId(id);
                                    cb.setGoodsEvaluate(goodsEvaluate);
                                    cb.setGoodsEvaluateInfo(goodsEvaluateInfo);
                                    cb.setServiceEvaluateInfo(serviceEvaluateInfo);
                                    cb.setServiceEvaluate(serviceEvaluate);
                                    cb.setInsertTime(insertTime);
                                    UserBean ub=new UserBean();
                                    ub.setUserId(userId);
                                    ub.setAccount(username);
                                    ub.setPortraiturl(url);
                                    cb.setUserBean(ub);

                                    commentList.add(cb);
                                }
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(commentList.size()>0){
                            pageIndex++;
                            mLLRemind.setVisibility(View.GONE);
                        }else {
                            mLLRemind.setVisibility(View.VISIBLE);
                        }
                        initCommentListAdapter();
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

    private void loadMore(){

        RequestParams params=new RequestParams(BnUrl.pullCommentsUrl);
        params.setUseCookie(false);
        params.addBodyParameter("ctype","evaluate");
        params.addBodyParameter("orderby","id desc");
        params.addBodyParameter("jf","store|user1|photo");
        params.addBodyParameter("pageIndex",String.valueOf(pageIndex));
        params.addBodyParameter("cond","{store:{id:"+app.getCurrentUser().getShopId()+"}}");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            JSONArray resultList=response.getJSONArray("resultList");
                            if(resultList.length()>0){
                                for (int x=0;x<resultList.length();x++){
                                    JSONObject comment=resultList.getJSONObject(x);
                                    int id=comment.getInt("id");//评论表 id
                                    String goodsEvaluateInfo=comment.getString("goodsEvaluateInfo");//对商品的评价
                                    String serviceEvaluateInfo=comment.getString("serviceEvaluateInfo");//服务评价
                                    int goodsEvaluate=comment.getInt("goodsEvaluate");
                                    int serviceEvaluate=comment.getInt("serviceEvaluate");
                                    String insertTime=comment.getString("insertTime");//发表时间

                                    JSONObject user=comment.getJSONObject("user1");
                                    int userId=user.getInt("id");
                                    String username=user.getString("username");
                                    String photoStr=user.getString("photo");
                                    String url=null;
                                    if(photoStr!=null && !"null".equals(photoStr) && !TextUtils.isEmpty(photoStr)){
                                        url =user.getJSONObject("photo").getString("url");
                                    }
                                    CommentBean cb=new CommentBean();
                                    cb.setId(id);
                                    cb.setGoodsEvaluate(goodsEvaluate);
                                    cb.setGoodsEvaluateInfo(goodsEvaluateInfo);
                                    cb.setServiceEvaluateInfo(serviceEvaluateInfo);
                                    cb.setServiceEvaluate(serviceEvaluate);
                                    cb.setInsertTime(insertTime);
                                    UserBean ub=new UserBean();
                                    ub.setUserId(userId);
                                    ub.setAccount(username);
                                    ub.setPortraiturl(url);
                                    cb.setUserBean(ub);

                                    commentList.add(cb);
                                }
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(commentList.size()>0){
                            pageIndex++;
                            mLLRemind.setVisibility(View.GONE);
                        }else {
                            mLLRemind.setVisibility(View.VISIBLE);
                        }
                        initCommentListAdapter();
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
     * 初始化评论列表适配器
     */
    private void initCommentListAdapter() {
        if(commentListAdapter==null){
            mRV.setLayoutManager(new LinearLayoutManager(this));
            commentListAdapter=new CommentListRVAdapter(this,commentList);
            mRV.setAdapter(commentListAdapter);
        }
        commentListAdapter.notifyDataSetChanged();

    }

    @OnClick({R.id.activity_comment_iv_back})
    public void commentClick(View v){
        switch (v.getId()){
            case R.id.activity_comment_iv_back:
                finish();
                break;
        }
    }
}
