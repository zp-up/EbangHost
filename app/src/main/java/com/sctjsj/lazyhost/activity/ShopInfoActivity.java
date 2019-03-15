package com.sctjsj.lazyhost.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bumptech.glide.Glide;
import com.jaeger.library.StatusBarUtil;
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
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.bean.Dish;
import com.sctjsj.lazyhost.bean.DishMenu;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.ImageUtils;
import com.sctjsj.lazyhost.util.LogUtil;
import com.sctjsj.lazyhost.util.ProgressUtil;
import com.sctjsj.lazyhost.widget.PopupDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShopInfoActivity extends Activity implements TakePhoto.TakeResultListener,InvokeListener {
    private MyApp app;
    private TakePhoto takePhoto;
    private InvokeParam invokeParam;
    private File file;
    private int logoId=-1;//图片上传后获得的 id
    private ProgressUtil pUtil;
    private boolean photoChanged=false;//是否选择过新的图片

    @Bind(R.id.activity_shop_info_et_shop_name)EditText mETShopName;
    @Bind(R.id.activity_shop_info_et_shop_address)EditText mETShopAddress;
    @Bind(R.id.activity_shop_info_et_tel)EditText mETTel;


    @Bind(R.id.activity_shop_info_ll_parent)LinearLayout mLLParent;
    @Bind(R.id.activity_shop_info_iv_preview)ImageView mIVPreview;//预览图片
    @Bind(R.id.activity_shop_info_tv_remind)TextView mTVRemind;//提示更换图片
    @Bind(R.id.activity_shop_info_tv_begin_time)TextView mTVTimeBegin;//配送开始时间
    @Bind(R.id.activity_shop_info_tv_end_time)TextView mTVTimeEnd;//配送结束时间

    @Bind(R.id.activity_shop_info_et_least_to_delivery)EditText mETLeastToDelivery;//多少起送
    private int beginHour,beginMinute,endHour,endMinute;//开始结束配送时间

    @Bind(R.id.act_shop_info_least_delivery_ll_parent)LinearLayout mLLLeastDeliveryParent;
    @Bind(R.id.act_shop_info_delivery_time_ll_parent)LinearLayout mLLDeliveryTimeParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_info);
        StatusBarUtil.setTranslucentForImageViewInFragment(this,0,null);
        ButterKnife.bind(this);
        app= (MyApp) getApplication();
        pUtil=new ProgressUtil(this);

        if(app.getCurrentUser()!=null){
            if(6==app.getCurrentUser().getShopType()){
                mLLLeastDeliveryParent.setVisibility(View.GONE);
                mLLDeliveryTimeParent.setVisibility(View.GONE);
            }
        }

        if(app.getCurrentUser()!=null){
            pullShopInfoById(String.valueOf(app.getCurrentUser().getShopId()));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getTakePhoto().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.activity_shop_info_tv_begin_time,R.id.activity_shop_info_tv_end_time,
            R.id.activity_shop_info_iv_add_pic,R.id.activity_shop_info_tv_save,R.id.activity_shop_info_iv_back})
    public void shopInfoClick(View v){
        switch (v.getId()){
            //返回
            case R.id.activity_shop_info_iv_back:
                finish();
                break;

            //选择图片
            case R.id.activity_shop_info_iv_add_pic:
                createPop();
                break;
            //选择开始配送时间
            case R.id.activity_shop_info_tv_begin_time:
                AlertDialog.Builder builder=new AlertDialog.Builder(ShopInfoActivity.this);
                builder.setTitle("选择开始时间");
                View dialog1= LayoutInflater.from(ShopInfoActivity.this).inflate(R.layout.dialog_begin_send_time_layout,null);
                final TimePicker pickerBegin= (TimePicker) dialog1.findViewById(R.id.dialog_begin_send_time_timepicker);
                pickerBegin.setIs24HourView(true);
                builder.setView(dialog1);
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String beginHStr="";
                        String beginMStr="";
                        beginHour=pickerBegin.getCurrentHour();
                        beginMinute=pickerBegin.getCurrentMinute();

                        if(beginHour<10){
                            beginHStr="0"+beginHour;
                        }else {
                            beginHStr=""+beginHour;
                        }

                        if(beginMinute<10){
                            beginMStr="0"+beginMinute;
                        }else {
                            beginMStr=""+beginMinute;
                        }

                        mTVTimeBegin.setText(beginHStr+":"+beginMStr);
                    }
                });
                builder.show();
                break;
            //选择结束配送时间
            case R.id.activity_shop_info_tv_end_time:
                AlertDialog.Builder builder2=new AlertDialog.Builder(ShopInfoActivity.this);
                builder2.setTitle("选择结束时间");
                View dialog2= LayoutInflater.from(ShopInfoActivity.this).inflate(R.layout.dialog_end_send_time_layout,null);
                final TimePicker pickerEnd= (TimePicker) dialog2.findViewById(R.id.dialog_end_send_time_timepicker);
                pickerEnd.setIs24HourView(true);
                builder2.setView(dialog2);
                builder2.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder2.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String endHStr="";
                        String endMStr="";
                        endHour=pickerEnd.getCurrentHour();
                        endMinute=pickerEnd.getCurrentMinute();
                        if(endHour<10){
                            endHStr="0"+endHour;
                        }else {
                            endHStr=""+endHour;
                        }
                        if(endMinute<10){
                            endMStr="0"+endMinute;
                        }else {
                            endMStr=""+endMinute;
                        }
                        mTVTimeEnd.setText(endHStr+":"+endMStr);
                    }
                });
                builder2.show();
                break;

            // 保存修改的信息
            case R.id.activity_shop_info_tv_save:
                if(checkBeforeSave()){
                   //判断是否有图片修改
                    if(-1!=logoId&&file!=null){
                        try {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);//自定义的code
                                return;
                            }
                            Bitmap bitmap = ImageUtils.getBitmapByPath(file.getPath());
                            bitmap = ImageUtils.compressImage(bitmap);
                            ImageUtils.saveBitmap(bitmap,file.getPath());
                            upload(file);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        save();
                    }
                }
                break;
        }
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
     * 弹出头像修改提示框
     */
    public void createPop(){
        List<String> data=new ArrayList<>();
        data.add("拍照");
        data.add("手机相册");
        final PopupDialog popDialog=new PopupDialog(ShopInfoActivity.this,data);
        popDialog.setOnItemClickListener(new PopupDialog.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                switch (position){
                    //拍照
                    case 0:
                        //裁剪参数
                        CropOptions cropOptions=new CropOptions.Builder()
                                .setWithOwnCrop(false).create();
                        getTakePhoto().onPickFromCaptureWithCrop(getUri(),cropOptions);
                        break;
                    //手機相冊
                    case 1:
                        //裁剪参数
                        CropOptions cropOptions1=new CropOptions.Builder().
                                setWithOwnCrop(false).create();
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
                            logoId=jo.getInt("acyId");
                            save();
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
                LogUtil.e("error："+ex.toString());
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
     * 图片保存路径
     * @return
     */

    private Uri getUri(){
        File file=new File(Environment.getExternalStorageDirectory(), "/lazyhost/images/"+System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists())file.getParentFile().mkdirs();
        Uri imageUri = Uri.fromFile(file);
        return imageUri;
    }

    @Override
    public void takeSuccess(TResult result) {
        file=new File(result.getImage().getPath());
        Bitmap bmp= BitmapFactory.decodeFile(file.getPath());
        mIVPreview.setImageBitmap(bmp);
        mTVRemind.setText("重新选择");
        //标志需要重新上传 photo
        photoChanged=true;
    }

    @Override
    public void takeFail(TResult result, String msg) {
        LogUtil.e("takeFail");
        Snackbar.make(mLLParent,"图片选取失败",Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void takeCancel() {
        LogUtil.e("takecancel");
        Snackbar.make(mLLParent,"取消选取图片",Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public PermissionManager.TPermissionType invoke(InvokeParam invokeParam) {
        PermissionManager.TPermissionType type=PermissionManager.checkPermission(TContextWrap.of(this),invokeParam.getMethod());
        if(PermissionManager.TPermissionType.WAIT.equals(type)){
            this.invokeParam=invokeParam;
        }
        return type;
    }

    /**
     * 保存修改信息之前先检查
     * @return
     */
    private boolean checkBeforeSave(){
        //店铺名非空判断
        if(TextUtils.isEmpty(mETShopName.getText().toString())){
            Snackbar.make(mLLParent,"店铺名为空",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        //店铺地址非空判断
        if(TextUtils.isEmpty(mETShopAddress.getText().toString())){
            Snackbar.make(mLLParent,"店铺地址为空",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        //店铺联系电话非空判断
        if(TextUtils.isEmpty(mETTel.getText().toString())){
            Snackbar.make(mLLParent,"店铺联系电话为空",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(mETLeastToDelivery.getText().toString())){
            Snackbar.make(mLLParent,"请设置多少起送",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if("起送时间".equals(mTVTimeBegin.getText())){
            Snackbar.make(mLLParent,"开始配送时间为空",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if("终止时间".equals(mTVTimeEnd.getText())){
            Snackbar.make(mLLParent,"终止配送时间为空",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        //判断开始时间不能大于结束时间
        if( beginHour*60+beginMinute>=endHour*60+endMinute){
            Snackbar.make(mLLParent,"开始时间不能大于终止时间",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if(logoId==-1){
            Snackbar.make(mLLParent,"图片错误",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * 获取店铺所有信息
     */
    public void pullShopInfoById(String id){
        RequestParams paramas=new RequestParams(BnUrl.getShopInfoById);
        paramas.setUseCookie(false);
        paramas.addBodyParameter("size","9999");
        paramas.addBodyParameter("ctype","store");
        //已经上架的商品
        paramas.addBodyParameter("cond","{id:"+id+"}");
        paramas.addBodyParameter("jf","storeLogo");
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

                    try {
                        JSONArray resultList=response.getJSONArray("resultList");
                        if(resultList.length()>0){
                            JSONObject store=resultList.getJSONObject(0);
                            logoId=store.getJSONObject("storeLogo").getInt("id");
                            //店铺 logo
                            String logoUrl=store.getJSONObject("storeLogo").getString("url");
                            Glide.with(ShopInfoActivity.this).load(logoUrl).error(R.mipmap.icon_portrait_load_failed).into(mIVPreview);

                            //店铺名
                            String storeName=store.getString("storeName");
                            mETShopName.setText(TextUtils.isEmpty(storeName)?"":storeName);

                            //店铺地址
                            String storeAddress=store.getString("storeAddress");
                            mETShopAddress.setText(TextUtils.isEmpty(storeAddress)?"":storeAddress);

                            String shipThr=store.getString("shipThr");//多少起送
                            if(shipThr!=null && !TextUtils.isEmpty(shipThr)){
                                mETLeastToDelivery.setText(shipThr);
                            }else {
                                mETLeastToDelivery.setText("0");
                            }

                            //联系电话
                            String telephone=store.getString("telephone");
                            mETTel.setText(TextUtils.isEmpty(telephone)?"":telephone);

                            //起送时间
                            String sendBeginTime=store.getString("sendBeginTime");
                            String[]begin=sendBeginTime.split(":");
                            beginHour=Integer.valueOf(begin[0]);
                            beginMinute=Integer.valueOf(begin[1]);
                            String beginHStr="";
                            String beginMStr="";
                            if(beginHour<10){
                                beginHStr="0"+beginHour;
                            }else {
                                beginHStr=""+beginHour;
                            }
                            if(beginMinute<10){
                                beginMStr="0"+beginMinute;
                            }else {
                                beginMStr=""+beginMinute;
                            }
                            mTVTimeBegin.setText(beginHStr+":"+beginMStr);

                            //结束时间
                            String sendEndTime=store.getString("sendEndTime");
                            mTVTimeEnd.setText(TextUtils.isEmpty(sendEndTime)?"":sendEndTime);
                            String[]end=sendEndTime.split(":");
                            endHour=Integer.valueOf(end[0]);
                            endMinute=Integer.valueOf(end[1]);
                            String endHStr="";
                            String endMStr="";
                            if(endHour<10){
                                endHStr="0"+endHour;
                            }else {
                                endHStr=""+endHour;
                            }
                            if(endMinute<10){
                                endMStr="0"+endMinute;
                            }else {
                                endMStr=""+endMinute;
                            }
                            mTVTimeEnd.setText(endHStr+":"+endMStr);

                        }else {
                            Snackbar.make(mLLParent,"店铺信息加载失败", Snackbar.LENGTH_LONG).show();
                            pUtil.dismissProgress();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Snackbar.make(mLLParent,"店铺信息加载失败", Snackbar.LENGTH_LONG).show();
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
        });
    }


    /**
     * 保存修改信息
     */
    public void save(){
        RequestParams params=new RequestParams(BnUrl.changeShopStateUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie",""));
        params.addBodyParameter("ctype","store");

        params.addBodyParameter("data","{id:"+app.getCurrentUser().getShopId()+",shipThr:"+mETLeastToDelivery.getText().toString()+",storeName:"+"\""+mETShopName.getText().toString()+"\""+",storeLogo:{id:"+logoId+"},storeAddress:"+"\""+mETShopAddress.getText().toString()+"\""+",telephone:"+"\""+mETTel.getText().toString()+"\""+",sendBeginTime:"+"\""+mTVTimeBegin.getText().toString()+"\""+",sendEndTime:"+"\""+mTVTimeEnd.getText().toString()+"\""+"}");

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            Snackbar.make(mLLParent,"店铺信息修改成功",Snackbar.LENGTH_SHORT).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            },1500);
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
