package com.sctjsj.lazyhost.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.igexin.sdk.PushManager;
import com.jaeger.library.StatusBarUtil;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.bean.ClassifyBean;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.LogUtil;
import com.sctjsj.lazyhost.util.ProgressUtil;
import com.sctjsj.lazyhost.util.SmoothCheckBox;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Admin on 2016/11/16.
 */

public class ClassifyManagementActivity extends AppCompatActivity {
    private MyApp app;
    private ProgressUtil pUtil;
    private MyAdapter adapter;
    private String ori;
    private List<ClassifyBean> cfbList=new ArrayList<>();
    private List<ClassifyBean> data=new ArrayList<>();

    @Bind(R.id.activity_add_classfiy_lv) ListView mListView;
    @Bind(R.id.activity_classify_mag_ll_parent)LinearLayout mLLParent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classify_management);
        StatusBarUtil.setTranslucentForImageViewInFragment(this,0,null);
        app= (MyApp) getApplication();
        ButterKnife.bind(this);
        pUtil=new ProgressUtil(this);
        initClassify();
        //标识从哪个页面跳过来
        ori=getIntent().getStringExtra("ori");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    private void initClassify(){
        cfbList.clear();
        RequestParams params=new RequestParams(BnUrl.queryAllClassifyInShop);
        params.setUseCookie(false);
        params.addHeader("Cookie","none-cookie");
        params.addBodyParameter("ctype","ugc");
        params.addBodyParameter("size","99999");
        params.addBodyParameter("cond","{store:{id:"+app.getCurrentUser().getShopId()+"}"+",display:1}");

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        JSONArray resultList=response.getJSONArray("resultList");
                        if(resultList!=null && resultList.length()>0){
                            for (int i=0;i<resultList.length();i++){
                                JSONObject x=resultList.getJSONObject(i);
                                int id=x.getInt("id");
                                String className=x.getString("className");
                                int display=x.getInt("display");
                                if(1==display){
                                    ClassifyBean cfb=new ClassifyBean();
                                    cfb.setId(id);
                                    cfb.setName(className);
                                    cfb.setDisplay(display);
                                    cfbList.add(cfb);
                                }

                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(adapter==null){
                            adapter=new MyAdapter();
                            mListView.setAdapter(adapter);
                        }
                        adapter.notifyDataSetChanged();
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

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return cfbList.size();
        }

        @Override
        public Object getItem(int position) {
            return cfbList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            XHolder h;
            if(view==null){
                view = LayoutInflater.from(ClassifyManagementActivity.this).inflate(R.layout.item_of_activity_change_classify, null);
                h=new XHolder();
                h.mTVName= (TextView) view.findViewById(R.id.item_of_activity_classify_mag_tv_name);
                h.mCB= (SmoothCheckBox) view.findViewById(R.id.item_of_activity_classify_mag_cb_selected);
                h.mLL= (LinearLayout) view.findViewById(R.id.item_of_activity_change_classify_ll_parent);
                h.mLLParent= (LinearLayout) view.findViewById(R.id.item_classify_ll_parent);
                view.setTag(h);
            }else{
                h= (XHolder) view.getTag();
            }
            h.mTVName.setText(cfbList.get(position).getName());

            //点击修改分类名称
            h.mLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(ClassifyManagementActivity.this);
                    builder.setTitle("请输入修改后的分类名称");
                    View vi=LayoutInflater.from(ClassifyManagementActivity.this).inflate(R.layout.layout_add_classify,null);
                    final EditText et= (EditText) vi.findViewById(R.id.layout_add_classify_et);
                    builder.setView(vi);
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    builder.setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(!TextUtils.isEmpty(et.getText().toString()) && et.getText().toString().length() <= 8){
                                modifyUgc(String.valueOf(cfbList.get(position).getId()),et.getText().toString());
                            }else {
                                Snackbar.make(mLLParent,"请输入修改后的类型名称(最多8个字/符号)",Snackbar.LENGTH_SHORT).show();
                            }

                        }
                    });
                    builder.show();
                }
            });

            h.mCB.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                    if(isChecked){
                        cfbList.get(position).setDisplay(1);
                        data.add(cfbList.get(position));
                    }
                }
            });

            //长按删除分类
            h.mLL.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder b=new AlertDialog.Builder(ClassifyManagementActivity.this);
                    b.setTitle("删除提示");
                    b.setMessage("删除分类后，该分类下的所有商品都将被删除！");
                    b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    b.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteClassify(String.valueOf(cfbList.get(position).getId()));
                        }
                    });
                    b.show();
                    return true;
                }
            });

            return view;
        }

        class XHolder{
            TextView mTVName;
            LinearLayout mLL;
            SmoothCheckBox mCB;
            LinearLayout mLLParent;
        }

    }

    @OnClick({R.id.activity_add_classfiy_back_ll,R.id.activity_classify_mag_btn_choose_ok,
            R.id.activity_classify_mag_btn_add_new_classify})
    public void thisActivtyListen(View view) {
        switch (view.getId()) {
            //返回
            case R.id.activity_add_classfiy_back_ll:
                setResult(RESULT_CANCELED);
                finish();
                this.overridePendingTransition(R.anim.activity_anim_start, R.anim.activity_anim_end);
                break;
            //选好了分类
            case R.id.activity_classify_mag_btn_choose_ok:
                LogUtil.e(ori);
                switch (ori){

                    //返回添加商品
                    case "add":
                        Intent in1=new Intent(ClassifyManagementActivity.this,AddCommodityActivity.class);
                        Bundle b1=new Bundle();
                        b1.putSerializable("classifyList", (Serializable) data);
                        in1.putExtras(b1);
                        setResult(RESULT_OK,in1);
                        finish();
                        break;
                    //返回修改商品
                    case "modify":
                        Intent in2=new Intent(ClassifyManagementActivity.this,ModifyCommodityActivity.class);
                        Bundle b2=new Bundle();
                        b2.putSerializable("classifyList", (Serializable) data);
                        in2.putExtras(b2);
                        setResult(RESULT_OK,in2);
                        finish();
                        break;
                }


                break;
            //创建新分类
            case R.id.activity_classify_mag_btn_add_new_classify:
                AlertDialog.Builder builder=new AlertDialog.Builder(ClassifyManagementActivity.this);
                builder.setTitle("请输入分类名称");
                View v=LayoutInflater.from(ClassifyManagementActivity.this).inflate(R.layout.layout_add_classify,null);
                final EditText et= (EditText) v.findViewById(R.id.layout_add_classify_et);
                builder.setView(v);
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.setPositiveButton("确认添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!TextUtils.isEmpty(et.getText().toString()) && et.getText().toString().length()<=8){
                            addNewClassify(et.getText().toString());
                        }else{
                            Snackbar.make(mLLParent,"请输入类型名称(最多8个字/符号)",Snackbar.LENGTH_SHORT).show();
                        }

                    }
                });
                builder.show();

                break;
        }
    }

    private void addNewClassify(String str){
        RequestParams params=new RequestParams(BnUrl.addNewClassifyUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie","none-cookie");
        params.addBodyParameter("ClassName",str);
        params.addBodyParameter("store.id",String.valueOf(app.getCurrentUser().getShopId()));

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            //JSONObject bnUsergoodsclassTbl=response.getJSONObject("bnUsergoodsclassTbl");
                            Snackbar.make(mLLParent,"添加分类成功",Snackbar.LENGTH_LONG).show();
                            initClassify();
                        }else{
                            Snackbar.make(mLLParent,"添加分类失败",Snackbar.LENGTH_LONG).show();
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

    private void deleteClassify(String id){
        RequestParams params=new RequestParams(BnUrl.modifyClassifyUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie","none-cookie");
        params.addBodyParameter("ctype","ugc");
        params.addBodyParameter("data","{id:"+id+",display:2}");

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");

                        if(result){
                            Snackbar.make(mLLParent,"类型删除成功",Snackbar.LENGTH_SHORT).show();
                            initClassify();
                        }else{
                            Snackbar.make(mLLParent,"类型删除失败",Snackbar.LENGTH_SHORT).show();
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

    private void modifyUgc(String id,String classify){
        RequestParams params=new RequestParams(BnUrl.changeShopStateUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie",""));
        params.addBodyParameter("ctype","ugc");
        params.addBodyParameter("data","{id:"+id+",className:\""+classify+"\"}");//{id:1,storeStatus:1}

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.e("ugc-修改",response.toString());
                if (response != null) {
                    try {
                        boolean result = response.getBoolean("result");

                        if (result) {
                            Snackbar.make(mLLParent, "类型修改成功", Snackbar.LENGTH_SHORT).show();
                            initClassify();
                        } else {
                            Snackbar.make(mLLParent, "类型修改失败", Snackbar.LENGTH_SHORT).show();
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

}
