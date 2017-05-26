package com.sctjsj.ebanghost.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.support.v7.widget.RecyclerView.ViewHolder;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.InvokeParam;
import com.jph.takephoto.model.TContextWrap;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.permission.InvokeListener;
import com.jph.takephoto.permission.PermissionManager;
import com.jph.takephoto.permission.TakePhotoInvocationHandler;
import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.bean.GalleryBean;
import com.sctjsj.ebanghost.decoration.DividerItemDecoration;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.DpUtils;
import com.sctjsj.ebanghost.util.LogUtil;
import com.sctjsj.ebanghost.util.ProgressUtil;
import com.sctjsj.ebanghost.widget.BigPicDialog;
import com.sctjsj.ebanghost.widget.PopupDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GalleryMagActivity extends Activity implements TakePhoto.TakeResultListener,InvokeListener {

    @Bind(R.id.activity_gallery_mag_rl_none_remind)RelativeLayout mRLNone;
    @Bind(R.id.activity_gallery_mag_rv)RecyclerView mRV;
    @Bind(R.id.activity_gallery_mag)LinearLayout mLLParent;
    private ProgressUtil pUtil;
    private MyAdapter adapter;
    private List<GalleryBean> galleryList=new ArrayList<>();
    private MyApp app;
    private TakePhoto takePhoto;
    private InvokeParam invokeParam;
    private File file=null;
    private int picId=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_mag);
        ButterKnife.bind(this);
        app= (MyApp) getApplication();
        pUtil=new ProgressUtil(this);
        initData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getTakePhoto().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.TPermissionType type=PermissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
        PermissionManager.handlePermissionsResult(this,type,invokeParam,this);
    }

    /**
     *  获取TakePhoto实例
     * @return
     */
    public TakePhoto getTakePhoto(){
        if (takePhoto==null){
            takePhoto= (TakePhoto) TakePhotoInvocationHandler.of(this).bind(new TakePhotoImpl(this,this));
        }
        return takePhoto;
    }

    /**
     * 图片保存路径
     * @return
     */
    private Uri getUri(){
        File file=new File(Environment.getExternalStorageDirectory(), "/ebanghost/images/"+System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists())file.getParentFile().mkdirs();
        Uri imageUri = Uri.fromFile(file);
        return imageUri;
    }


    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }


    @OnClick({R.id.activity_gallery_mag_rl_back,R.id.activity_gallery_iv_add_new})
    public void galleryMagClick(View view){
        switch (view.getId()){
            //返回
            case R.id.activity_gallery_mag_rl_back:
                 finish();
                break;

            //添加新照片
            //最多允许添加九张图片
            case R.id.activity_gallery_iv_add_new:
                if(galleryList.size()<9){
                    createPop();
                }else {
                    Snackbar.make(mLLParent,"商家最多允许上传9张图片",Snackbar.LENGTH_LONG).show();
                }
                break;

        }
    }


    private void initData(){
        if(galleryList!=null){
            galleryList.clear();
        }

        RequestParams params=new RequestParams(BnUrl.getGalleryListUrl);
        params.addBodyParameter("ctype","store");
        params.addBodyParameter("size","9");
        params.addBodyParameter("cond","{id:"+app.getCurrentUser().getShopId()+"}");
        params.addBodyParameter("jf","photo|accessory");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                try {
                    JSONArray resultList=response.getJSONArray("resultList");
                    if(resultList.length()>0){
                        JSONArray accessory=resultList.getJSONObject(0).getJSONArray("accessory");
                        if(accessory.length()>0){
                            for (int x=0;x<accessory.length();x++){
                                int id=accessory.getJSONObject(x).getInt("id");
                                String url=accessory.getJSONObject(x).getString("url");

                                GalleryBean gb=new GalleryBean();
                                gb.setAcyId(id);
                                gb.setUrl(url);
                                gb.setShopId(app.getCurrentUser().getShopId());
                                galleryList.add(gb);
                            }

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                finally {
                    if(galleryList.size()>0){
                        mRLNone.setVisibility(View.GONE);
                        if(adapter==null){
                            adapter=new MyAdapter();
                            mRV.setAdapter(adapter);
                            mRV.setLayoutManager(new GridLayoutManager(GalleryMagActivity.this,3));
                        }
                        adapter.notifyDataSetChanged();
                    }else {
                        if(adapter!=null){
                            adapter.notifyDataSetChanged();
                        }
                        mRLNone.setVisibility(View.VISIBLE);
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

    /**
     * 弹出头像修改提示框
     */
    public void createPop(){
        List<String> data=new ArrayList<>();
        data.add("拍照");
        data.add("手机相册");
        final PopupDialog popDialog=new PopupDialog(GalleryMagActivity.this,data);
        popDialog.setOnItemClickListener(new PopupDialog.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                switch (position){
                    //拍照
                    case 0:
                        //裁剪参数
                        CropOptions cropOptions=new CropOptions.Builder().
                                setWithOwnCrop(false).
                                create();
                        getTakePhoto().onPickFromCaptureWithCrop(getUri(),cropOptions);
                        break;
                    //手機相冊
                    case 1:
                        //裁剪参数 长宽 8：5
                        CropOptions cropOptions1=new CropOptions.Builder()
                                .setWithOwnCrop(false).
                                        create();

                        //系统ui
                        getTakePhoto().onPickFromGalleryWithCrop(getUri(),cropOptions1);


                        break;
                    //取消
                    case 2:
                        popDialog.dismiss();
                        break;
                }
            }
        });
        popDialog.show();
    }

    @Override
    public void takeSuccess(TResult result) {
        Log.e("图片选取-success",result.toString());
        file=new File(result.getImage().getPath());
        Bitmap bmp= BitmapFactory.decodeFile(file.getPath());
        AlertDialog.Builder builder=new AlertDialog.Builder(GalleryMagActivity.this);
        builder.setTitle("图片选择");
        ImageView img=new ImageView(GalleryMagActivity.this);
        builder.setView(img);
        img.setImageBitmap(bmp);
        builder.setPositiveButton("上传", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(file!=null){
                    upload(file);
                }else {
                    Snackbar.make(mLLParent,"图片文件为空",Snackbar.LENGTH_LONG).show();
                }
            }
        });
        builder.show();

    }

    @Override
    public void takeFail(TResult result, String msg) {
        Log.e("图片选取-失败",result.toString()+msg);
        Snackbar.make(mLLParent,"图片选取失败",Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void takeCancel() {
        Log.e("图片选取取消","cancel");

    }

    @Override
    public PermissionManager.TPermissionType invoke(InvokeParam invokeParam) {
        PermissionManager.TPermissionType type=PermissionManager.checkPermission(TContextWrap.of(this),invokeParam.getMethod());
        if(PermissionManager.TPermissionType.WAIT.equals(type)){
            this.invokeParam=invokeParam;
        }
        return type;
    }


    class MyAdapter extends RecyclerView.Adapter<XH>{

        @Override
        public XH onCreateViewHolder(ViewGroup parent, int viewType) {
            XH h=new XH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_gallery_mag,null));
            return h;
        }

        @Override
        public void onBindViewHolder(XH holder, final int position) {
            Picasso.with(GalleryMagActivity.this).load(galleryList.get(position).getUrl()).resize(DpUtils.dpToPx(GalleryMagActivity.this,160),DpUtils.dpToPx(GalleryMagActivity.this,80)).error(R.mipmap.icon_portrait_load_failed).into(holder.mIV);
            holder.mIV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    AlertDialog.Builder b=new AlertDialog.Builder(GalleryMagActivity.this);
                    b.setTitle("删除提示");
                    b.setMessage("确认删除该图片");
                    b.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deletePic(String.valueOf(galleryList.get(position).getAcyId()));
                        }
                    });
                    b.show();

                    return true;
                }
            });
            holder.mIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(GalleryMagActivity.this,GalleryScanActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("gallery", (Serializable) galleryList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return galleryList.size();
        }
    }

    class XH extends ViewHolder{
        ImageView mIV;
        public XH(View itemView) {
            super(itemView);
            mIV= (ImageView) itemView.findViewById(R.id.item_activity_gallery_mag_iv);
        }
    }


    /**
     * 上传文件
     * @param file
     */
    public void upload(File file){
        RequestParams params=new RequestParams(BnUrl.uploadFileUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
        params.addBodyParameter("file",file);
        params.setMultipart(true);
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                LogUtil.e(response.toString());
                try {
                    boolean result=response.getBoolean("result");
                    if(result){
                        String resultData=response.getString("resultData");
                        JSONArray ja=new JSONArray(resultData);
                        if(ja!=null && ja.length()>0){
                            JSONObject jo=ja.getJSONObject(0);
                            picId=jo.getInt("acyId");
                            modify();
                        }
                    }else{
                        Snackbar.make(mLLParent,"图片上传失败",Snackbar.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                pUtil.dismissProgress();
                Snackbar.make(mLLParent,"图片上传错误",Snackbar.LENGTH_SHORT).show();

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

    public void modify(){
        RequestParams params=new RequestParams(BnUrl.modifyGalleryUrl);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
        params.addBodyParameter("acyId",String.valueOf(picId));
        params.addBodyParameter("storeId",String.valueOf(app.getCurrentUser().getShopId()));
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                try {
                    boolean result=response.getBoolean("result");
                    if(result){
                        Snackbar.make(mLLParent,"图片上传成功",Snackbar.LENGTH_SHORT).show();
                        initData();
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

    public void deletePic(String id){
        RequestParams params=new RequestParams(BnUrl.deleteGalleryUrl);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
        params.addBodyParameter("acyId",id);

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                try {
                    boolean result=response.getBoolean("result");
                    if(result){
                        Snackbar.make(mLLParent,"图片删除成功",Snackbar.LENGTH_SHORT).show();
                        initData();
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
}
