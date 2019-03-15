package com.sctjsj.lazyhost.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airsaid.pickerviewlibrary.OptionsPickerView;
import com.airsaid.pickerviewlibrary.listener.OnDismissListener;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
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
import com.sctjsj.lazyhost.bean.ClassifyBean;
import com.sctjsj.lazyhost.bean.GoodsBean;
import com.sctjsj.lazyhost.bean.TypeBean;
import com.sctjsj.lazyhost.constant.JumpCode;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.HorizontalListView;
import com.sctjsj.lazyhost.util.ImageUtils;
import com.sctjsj.lazyhost.util.LogUtil;
import com.sctjsj.lazyhost.util.ProgressUtil;
import com.sctjsj.lazyhost.widget.PopupDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Admin on 2016/11/16.
 */

public class ModifyCommodityActivity extends Activity implements TakePhoto.TakeResultListener,InvokeListener{
    private boolean photoChanged;
    private MyApp app;
    private ProgressUtil pUtil;
    private TakePhoto takePhoto;
    private InvokeParam invokeParam;
    private GoodsBean gbData;

    private int logoId=-1;//图片上传后获得的 id

    private int goodsId=-1;//商品 id

    private File file;
    private List<ClassifyBean>cfbList=new ArrayList<>();
    private HmyAdapter adapter;
    @Bind(R.id.activity_modify_commodity_iv_add_pic)ImageView mIVAddIcon;

    @Bind(R.id.activity_modify_commodity_iv_preview)ImageView mIVPreview;
    @Bind(R.id.activity_modify_commodity_tv_remind_to_add_img)TextView mTVAddPicRemind;
    @Bind(R.id.activity_modify_commodity_ll_parent)LinearLayout mLLParent;

    @Bind(R.id.activity_modify_commodity_et_name)EditText mETName;
    @Bind(R.id.activity_modify_commodity_et_details)EditText mETDetails;
    @Bind(R.id.activity_modify_commodity_et_price)EditText mETPrice;
    @Bind(R.id.activity_modify_commodity_et_disprice)EditText mETDisprice;
    @Bind(R.id.activity_modify_commodity_hlv)HorizontalListView mHLV;

    //固有属性
    @Bind(R.id.activity_modify_commodity_tv_type_belong_right)TextView mTVType;
    @Bind(R.id.activity_modify_commodity_btn_choose_type)Button mBtnChooseType;

    private OptionsPickerView<String> optionsPickerView;
    private ArrayList<TypeBean>typeBeanList=new ArrayList<>();
    private ArrayList<String> typeNameList=new ArrayList<>();
    private int typeId=-1;
    private String typeName="type";
    private int index = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_commodity);
        StatusBarUtil.setTranslucentForImageViewInFragment(this,0,null);
        app= (MyApp) getApplication();
        app.addActivity(this);
        ButterKnife.bind(this);
        pUtil=new ProgressUtil(this);
        goodsId=getIntent().getIntExtra("id",-1);
        index = getIntent().getIntExtra("index",-1);

        if(-1!=goodsId){
            pullGoodsInfo(String.valueOf(goodsId));
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getTakePhoto().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    /**
     * 页面回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case JumpCode.ModifyCommodityActivity_TO_ClassifyManageentActivity:

                if(RESULT_OK==resultCode){
                    cfbList= (List<ClassifyBean>) data.getSerializableExtra("classifyList");
                    mHLV.setSelection(0);
                    adapter.notifyDataSetChanged();
                }

                break;
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        app.removeActivity(this);
    }


    /**
     * onClick 事件
     * @param view
     */
    @OnClick({R.id.activity_modify_commodity_iv_back,R.id.activity_modify_commodity_iv_add_pic,
            R.id.activity_modify_commodity_btn_confirm,R.id.activity_modify_commodity_btn_add_classify,
            R.id.activity_modify_commodity_btn_choose_type})
    public void modifyCommodityClick(View view){
        switch (view.getId()){
            //返回
            case R.id.activity_modify_commodity_iv_back:
                finish();
                this.overridePendingTransition(R.anim.activity_anim_start,R.anim.activity_anim_end);
                break;
            //添加商品照片
            case R.id.activity_modify_commodity_iv_add_pic:
                 createPop();
                break;
            //选择固有属性
            case R.id.activity_modify_commodity_btn_choose_type:
                getAllTypes();
                break;

            //选择分类
            case R.id.activity_modify_commodity_btn_add_classify:
                Intent intent=new Intent(ModifyCommodityActivity.this,ClassifyManagementActivity.class);
                intent.putExtra("ori","modify");
                startActivityForResult(intent, JumpCode.ModifyCommodityActivity_TO_ClassifyManageentActivity);
                break;
            //添加商品
            case R.id.activity_modify_commodity_btn_confirm:
                if(check()){
                    if(photoChanged){
                        try {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                //申请WRITE_EXTERNAL_STORAGE权限
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
                    }else{
                        //直接提交
                        submit();
                    }

                }
                break;
        }
    }


    private boolean check(){

        if(TextUtils.isEmpty(mETName.getText().toString())){
            Snackbar.make(mLLParent,"请输入商品名称",Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(mETDetails.getText().toString())){
            Snackbar.make(mLLParent,"请输入商品描述",Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(mETPrice.getText().toString())){
            Snackbar.make(mLLParent,"请输入商品价格",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(mETDisprice.getText().toString())){
            Snackbar.make(mLLParent,"请输入商品优惠金额",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if(Double.valueOf(mETDisprice.getText().toString())>Double.valueOf(mETPrice.getText().toString())){
            Snackbar.make(mLLParent,"商品价格必须大于商品优惠金额",Snackbar.LENGTH_SHORT).show();
            return false;
        }


        if(cfbList.size()<=0){
            Snackbar.make(mLLParent,"请选择商品分类",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * 提交修改、添加操作
     * @param
     */
    private void submit(){
        /**
         * 封装数据参数
         */
        gbData.setLogoId(logoId);//设置 photo 的 id
        gbData.setPrice(Double.parseDouble(mETPrice.getText().toString()));//设置价格
        gbData.setName(mETName.getText().toString());//设置商品名称
        gbData.setId(goodsId);//商品 id
        gbData.setGoodstypeId(typeId);//固有属性 id

        gbData.setDetails(mETDetails.getText().toString());//设置描述
        gbData.setStatus(1);//商品状态
        gbData.setDisPrice(Double.valueOf(mETDisprice.getText().toString()));//商品优惠金额
        for (int i=0;i<gbData.getClassifyBeanList().size();i++){
            gbData.getClassifyBeanList().get(i).setDisplay(1);
        }
        gbData.setClassifyBeanList(cfbList);//设置分类集合
        final String json=new Gson().toJson(gbData);

        RequestParams params=new RequestParams(BnUrl.addOrModifyGoodsUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
        params.addBodyParameter("good",json);

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
               if(response!=null){
                   Log.e("TAG","修改结果:"+response.toString());
                   try {
                       boolean result=response.getBoolean("result");
                       if(result){
                           Snackbar.make(mLLParent,"商品信息修改成功",Snackbar.LENGTH_SHORT).show();
                           new Handler().postDelayed(new Runnable() {
                               @Override
                               public void run() {
                                   Intent intent = new Intent(ModifyCommodityActivity.this,CommodityManagementActivity.class);
                                   intent.putExtra("data",json);
                                   intent.putExtra("index",index);
                                   setResult(12,intent);
                                   finish();
                               }
                           },1000);
                       }else{
                           Snackbar.make(mLLParent,"商品信息修改失败",Snackbar.LENGTH_SHORT).show();
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
     * 弹出头像修改提示框
     */
    public void createPop(){
        List<String> data=new ArrayList<>();
        data.add("拍照");
        data.add("手机相册");
        final PopupDialog popDialog=new PopupDialog(ModifyCommodityActivity.this,data);
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
                LogUtil.e("TAG",response.toString());
                try {
                    boolean result=response.getBoolean("result");
                    if(result){
                        String resultData=response.getString("resultData");
                        JSONArray ja=new JSONArray(resultData);
                        if(ja!=null && ja.length()>0){
                            JSONObject jo=ja.getJSONObject(0);
                            String photoUrl = jo.getString("url");
                            gbData.setLogoUrl(photoUrl);
                            Log.e("TAG","图片地址:"+photoUrl);
                            logoId=jo.getInt("acyId");
                            submit();
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
        Bitmap bmp=BitmapFactory.decodeFile(file.getPath());
        mIVPreview.setImageBitmap(bmp);
        mTVAddPicRemind.setText("重新选择");
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
     * 根据商品 id 查询商品详情
     * @param goodsId
     */
    private void pullGoodsInfo(final String goodsId){
        RequestParams params=new RequestParams(BnUrl.queryGoodsDetailByIdUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
        params.addBodyParameter("ctype","goods");
        params.addBodyParameter("cond","{id:"+goodsId+"}");
        params.addBodyParameter("jf","photo|userGoodsClass|goodstype");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.e("查询修改",response.toString());
                if(response!=null){
                    try {
                        JSONArray resultList=response.getJSONArray("resultList");
                        if(resultList.length()>0){
                            JSONObject X=resultList.getJSONObject(0);
                            int id=X.getInt("id");//商品 id
                            String name=X.getString("name");//商品名称
                            double price=X.getDouble("price");//商品价格
                            double disprice=X.getDouble("disPrice");
                            String details=X.getString("details");//商品描述
                            String logo=X.getJSONObject("photo").getString("url");//logo
                            int logosId=X.getJSONObject("photo").getInt("id");
                            int goodsTypeId=X.getJSONObject("goodstype").getInt("id");//所属类型 id
                            typeId=goodsTypeId;
                            String goodsType=X.getJSONObject("goodstype").getString("name");//所属类型
                            mTVType.setText(goodsType);

                            logoId=logosId;
                            JSONArray userGoodsClass=X.getJSONArray("userGoodsClass");//自定义分类
                            List<ClassifyBean> cfbList=new ArrayList<ClassifyBean>();
                            if(userGoodsClass.length()>0){
                                for (int j=0;j<userGoodsClass.length();j++){
                                    int classifyId=userGoodsClass.getJSONObject(j).getInt("id");
                                    String classifyName=userGoodsClass.getJSONObject(j).getString("className");
                                    ClassifyBean cfb=new ClassifyBean();
                                    cfb.setId(classifyId);
                                    cfb.setName(classifyName);
                                    cfbList.add(cfb);
                                }
                            }
                            if(gbData==null){
                                gbData=new GoodsBean();
                            }
                            gbData.setId(id);
                            gbData.setClassifyBeanList(cfbList);
                            gbData.setName(name);
                            gbData.setDetails(details);
                            gbData.setGoodstypeId(goodsTypeId);
                            gbData.setPrice(price);
                            gbData.setDisPrice(disprice);
                            gbData.setLogoUrl(logo);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(gbData!=null){
                            Glide.with(ModifyCommodityActivity.this).load(gbData.getLogoUrl()).error(R.mipmap.icon_portrait_load_failed).into(mIVPreview);
                            mETName.setText(gbData.getName());
                            mETDetails.setText(gbData.getDetails());
                            mETPrice.setText(String.valueOf(gbData.getPrice()));
                            mETDisprice.setText(String.valueOf(gbData.getDisPrice()));
                            cfbList=gbData.getClassifyBeanList();
                           if(adapter==null){
                               adapter=new HmyAdapter();
                               mHLV.setAdapter(adapter);
                           }
                            adapter.notifyDataSetChanged();
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
                pUtil.showProgress(false);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });


    }


    private class HmyAdapter extends BaseAdapter {



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
        public View getView(int position, View view, ViewGroup parent) {
            XHolder xh;
            if(view==null){
                view= LayoutInflater.from(ModifyCommodityActivity.this).inflate(R.layout.activity_commod_manage_item_item,null);
                xh=new XHolder();
                xh.mTVClassify= (TextView) view.findViewById(R.id.activity_commodity_manage_item_tv_classify);
                view.setTag(xh);
            }else{
                xh= (XHolder) view.getTag();
            }
            xh.mTVClassify.setText(cfbList.get(position).getName());
            return view;
        }
        class XHolder{
            TextView mTVClassify;
        }
    }

    /**
     * 获取所有固有属性
     */
    private void getAllTypes(){
        RequestParams params=new RequestParams(BnUrl.getAllTypesUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
        params.addBodyParameter("size","99999");
        params.addBodyParameter("ctype","goodstype");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        JSONArray resultList=response.getJSONArray("resultList");

                        if(resultList.length()>0){
                            if(typeBeanList==null){
                                typeBeanList=new ArrayList<TypeBean>();
                            }
                            typeNameList.clear();
                            typeBeanList.clear();
                            for (int i=0;i<resultList.length();i++){

                                int id=resultList.getJSONObject(i).getInt("id");
                                String name=resultList.getJSONObject(i).getString("name");
                                TypeBean tb=new TypeBean();
                                tb.setId(id);
                                tb.setName(name);
                                typeNameList.add(name);
                                typeBeanList.add(tb);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(typeBeanList.size()>0){
                            if(optionsPickerView==null){
                                optionsPickerView=new OptionsPickerView<String>(ModifyCommodityActivity.this);
                            }
                            optionsPickerView.setPicker(typeNameList);
                            optionsPickerView.setOnOptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
                                @Override
                                public void onOptionsSelect(int option1, int option2, int option3) {
                                    typeId=typeBeanList.get(option1).getId();
                                    typeName=typeBeanList.get(option1).getName();
                                    mTVType.setText(typeName);
                                }
                            });

                            optionsPickerView.setOnDismissListener(new OnDismissListener() {
                                @Override
                                public void onDismiss(Object o) {

                                    if(typeBeanList!=null){
                                        typeBeanList.clear();
                                    }

                                    if(typeName!=null){
                                        typeNameList.clear();
                                    }

                                }
                            });
                            optionsPickerView.show();
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                pUtil.dismissProgress();
                Snackbar.make(mLLParent,"类型加载失败",Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                pUtil.dismissProgress();
                Snackbar.make(mLLParent,"类型加载失败",Snackbar.LENGTH_SHORT).show();
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
