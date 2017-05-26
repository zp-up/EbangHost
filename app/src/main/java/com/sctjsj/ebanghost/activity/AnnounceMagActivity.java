package com.sctjsj.ebanghost.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.bean.AnnouncementBean;
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
import butterknife.OnClick;
import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.RefreshHandler;

/**
 * 商家公告管理
 */
public class AnnounceMagActivity extends AppCompatActivity {
    private List<AnnouncementBean>data=new ArrayList<>();
    @Bind(R.id.act_announce_mag_lv)ListView mLV;
    @Bind(R.id.act_announce_mag_refresh)QRefreshLayout refreshLayout;
    @Bind(R.id.act_announce_mag_ll_remind)LinearLayout mLLRemind;
    @Bind(R.id.activity_announce_mag)LinearLayout mLLParent;
    private MyApp app;
    private ProgressUtil pUtil;
    private int pageIndex=1;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announce_mag);
        ButterKnife.bind(this);
        app= (MyApp) getApplication();
        pUtil=new ProgressUtil(this);
        refreshLayout.setLoadMoreEnable(true);
        refreshLayout.setRefreshHandler(new RefreshHandler() {
            @Override
            public void onRefresh(QRefreshLayout refresh) {
                if(app.getCurrentUser()!=null){
                    initData(String.valueOf(app.getCurrentUser().getShopId()));
                }
            }

            @Override
            public void onLoadMore(QRefreshLayout refresh) {
                if(app.getCurrentUser()!=null){
                    loadMore(String.valueOf(app.getCurrentUser().getShopId()));
                }
            }
        });
        if(app.getCurrentUser()!=null){
            initData(String.valueOf(app.getCurrentUser().getShopId()));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.act_announce_mag_iv_back,R.id.act_announce_mag_iv_add})
    public void announceMagClick(View view){
        switch (view.getId()){
            case R.id.act_announce_mag_iv_back:
                finish();
                break;
            //点击添加公告
            case R.id.act_announce_mag_iv_add:
                AlertDialog.Builder builder=new AlertDialog.Builder(AnnounceMagActivity.this);
                builder.setTitle("发布公告");
                View vi=LayoutInflater.from(AnnounceMagActivity.this).inflate(R.layout.layout_add_classify,null);
                final EditText et= (EditText) vi.findViewById(R.id.layout_add_classify_et);
                builder.setView(vi);
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.setPositiveButton("确认发布", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!TextUtils.isEmpty(et.getText().toString())){
                            addAnnounce(et.getText().toString());
                        }else {
                            Snackbar.make(mLLParent,"请输入要发布的公告内容",Snackbar.LENGTH_SHORT).show();
                        }

                    }
                });
                builder.show();
                break;
        }
    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder;
            if(convertView==null){
                convertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement_layout,null);
                holder=new Holder();

                holder.mTVContent= (TextView) convertView.findViewById(R.id.tv_content);
                holder.mTVEdit= (TextView) convertView.findViewById(R.id.tv_edit);
                holder.mTVDelete= (TextView) convertView.findViewById(R.id.tv_delete);

                convertView.setTag(holder);
            }else {
                holder= (Holder) convertView.getTag();
            }

            //
            if(data.get(position).getContent()!=null){
                holder.mTVContent.setText(data.get(position).getContent());
            }

            //编辑公告
            holder.mTVEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(AnnounceMagActivity.this);
                    builder.setTitle("修改公告内容");
                    View vi=LayoutInflater.from(AnnounceMagActivity.this).inflate(R.layout.layout_add_classify,null);
                    final EditText et= (EditText) vi.findViewById(R.id.layout_add_classify_et);
                    if(data.get(position).getContent()!=null){
                        et.setText(data.get(position).getContent());
                    }

                    builder.setView(vi);
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    builder.setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(!TextUtils.isEmpty(et.getText().toString())){
                                ModifyAnnounce(et.getText().toString(), String.valueOf(data.get(position).getId()));
                            }else {
                                Snackbar.make(mLLParent,"请输入修改后的公告内容",Snackbar.LENGTH_SHORT).show();
                            }

                        }
                    });
                    builder.show();
                }
            });

            //删除公告
            holder.mTVDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteAnnounce(String.valueOf(data.get(position).getId()));
                }
            });

            return convertView;
        }
    }


    class Holder{
        TextView mTVContent,mTVEdit,mTVDelete;
    }

    private void initData(String storeId){
        data.clear();
        pageIndex=1;
        final RequestParams params=new RequestParams(BnUrl.getAnnouceUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie",""));
        params.addBodyParameter("pageIndex",String.valueOf(pageIndex));
        params.addBodyParameter("pageSize","8");
        params.addBodyParameter("ctype","sp");
        params.addBodyParameter("cond","{state:1,storeId:{id:"+storeId+"}}");
        params.addBodyParameter("jf","storeId");
        //http://www.lp-kd.com/pageSearch$ajax.htm?ctype=sp&cond={stata:1,storeId:{id:24}}
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        JSONArray resultList=response.getJSONArray("resultList");
                        for (int i=0;i<resultList.length();i++){

                            JSONObject x=resultList.getJSONObject(i);
                            String content=x.getString("content");//公告内容
                            int id=x.getInt("id");//id
                            int state=x.getInt("state");//状态
                            if(1==state){
                                AnnouncementBean ab=new AnnouncementBean();
                                ab.setId(id);
                                ab.setContent(content);
                                ab.setState(state);
                                data.add(ab);
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(data.size()>0){
                            pageIndex++;
                            mLLRemind.setVisibility(View.GONE);
                        }else {
                            mLLRemind.setVisibility(View.VISIBLE);
                        }

                        if(adapter==null){
                            adapter=new MyAdapter();
                            mLV.setAdapter(adapter);
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

    private void loadMore(String storeId){
        final RequestParams params=new RequestParams(BnUrl.getAnnouceUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie",""));
        params.addBodyParameter("pageIndex",String.valueOf(pageIndex));
        params.addBodyParameter("pageSize","8");
        params.addBodyParameter("ctype","sp");
        params.addBodyParameter("cond","{state:1,storeId:{id:"+storeId+"}}");
        params.addBodyParameter("jf","storeId");
        //http://www.lp-kd.com/pageSearch$ajax.htm?ctype=sp&cond={stata:1,storeId:{id:24}}
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        JSONArray resultList=response.getJSONArray("resultList");
                        for (int i=0;i<resultList.length();i++){

                            JSONObject x=resultList.getJSONObject(i);
                            String content=x.getString("content");//公告内容
                            int id=x.getInt("id");//id
                            int state=x.getInt("state");//状态
                            if(1==state){
                                AnnouncementBean ab=new AnnouncementBean();
                                ab.setId(id);
                                ab.setContent(content);
                                ab.setState(state);
                                data.add(ab);
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(data.size()>0){
                            pageIndex++;
                            mLLRemind.setVisibility(View.GONE);
                        }else {
                            mLLRemind.setVisibility(View.VISIBLE);
                        }

                        if(adapter==null){
                            adapter=new MyAdapter();
                            mLV.setAdapter(adapter);
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

    private void addAnnounce(String content){
        //http://www.lp-kd.com/user/dataSave$ajax.htm?ctype=sp&data={state:1,storeId"{id: },content:""}
        RequestParams params=new RequestParams(BnUrl.releaseAnnounceUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie",""));
        params.addBodyParameter("ctype","sp");
        params.addBodyParameter("data","{state:1,storeId:{id:"+app.getCurrentUser().getShopId()+"},content:\""+content+"\"}");

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            Snackbar.make(mLLParent,"公告发布成功",Snackbar.LENGTH_SHORT).show();
                            if(app.getCurrentUser()!=null){
                                initData(String.valueOf(app.getCurrentUser().getShopId()));
                            }
                        }else {
                            Snackbar.make(mLLParent,"公告发布失败",Snackbar.LENGTH_SHORT).show();
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

    private void deleteAnnounce(String aid){
        RequestParams params=new RequestParams(BnUrl.modifyAnnouceUrl);
        params.addHeader("Cookie",app.getSpf().getString("cookie",""));
        params.addBodyParameter("ctype","sp");
        params.addBodyParameter("data","{id:"+aid+",state:2}");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            Snackbar.make(mLLParent,"公告删除成功",Snackbar.LENGTH_SHORT).show();
                            if(app.getCurrentUser()!=null){
                                initData(String.valueOf(app.getCurrentUser().getShopId()));
                            }
                        }else {
                            Snackbar.make(mLLParent,"公告删除失败",Snackbar.LENGTH_SHORT).show();
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

    private void ModifyAnnounce(String content,String aid){
        RequestParams params=new RequestParams(BnUrl.modifyAnnouceUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie",""));
        params.addBodyParameter("ctype","sp");
        params.addBodyParameter("data","{id:"+aid+",state:1,storeId:{id:"+app.getCurrentUser().getShopId()+"},content:\""+content+"\"}");

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            Snackbar.make(mLLParent,"公告修改成功",Snackbar.LENGTH_SHORT).show();
                            if(app.getCurrentUser()!=null){
                                initData(String.valueOf(app.getCurrentUser().getShopId()));
                            }
                        }else {
                            Snackbar.make(mLLParent,"公告修改失败",Snackbar.LENGTH_SHORT).show();
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
