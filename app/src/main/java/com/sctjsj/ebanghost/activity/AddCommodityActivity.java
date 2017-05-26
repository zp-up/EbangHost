package com.sctjsj.ebanghost.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airsaid.pickerviewlibrary.OptionsPickerView;
import com.airsaid.pickerviewlibrary.listener.OnDismissListener;
import com.google.gson.Gson;
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
import com.sctjsj.ebanghost.bean.ClassifyBean;
import com.sctjsj.ebanghost.bean.GoodsBean;
import com.sctjsj.ebanghost.bean.TypeBean;
import com.sctjsj.ebanghost.constant.JumpCode;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.HorizontalListView;
import com.sctjsj.ebanghost.util.LogUtil;
import com.sctjsj.ebanghost.util.ProgressUtil;
import com.sctjsj.ebanghost.widget.PopupDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddCommodityActivity extends Activity implements TakePhoto.TakeResultListener,InvokeListener {
    private MyApp app;
    private TakePhoto takePhoto;
    private InvokeParam invokeParam;
    private File file;
    private boolean photoChanged;
    //滚轮选择器
    private OptionsPickerView<String> optionsPickerView;
    private ArrayList<TypeBean>typeBeanList=new ArrayList<>();
    private ArrayList<String> typeNameList=new ArrayList<>();
    private int typeId=-1;
    private String typeName="type";


    private ProgressUtil pUtil;
    private HmyAdapter adapter;
    private int logoId=-1;//上传了图片之后的 id
    private List<ClassifyBean> cfbList=new ArrayList<>();//分类集合
    @Bind(R.id.activity_add_commodity_ll_parent)LinearLayout mLLParent;
    @Bind(R.id.activity_add_commodity_et_name)EditText mETName;
    @Bind(R.id.activity_add_commodity_et_details)EditText mETDetails;
    @Bind(R.id.activity_add_commodity_et_price)EditText mETPrice;
    @Bind(R.id.activity_add_commodity_et_disprice)EditText mETDisprice;
    @Bind(R.id.activity_add_commodity_tv_remind_to_add_img)TextView mTVAddPicRemind;
    @Bind(R.id.activity_add_commodity_iv_preview)ImageView mIVPreview;//预览

    @Bind(R.id.activity_add_commodity_tv_type_belong_right)TextView mTVType;
    @Bind(R.id.activity_add_commodity_hlv)HorizontalListView mHLV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_commodity);
        ButterKnife.bind(this);
        app= (MyApp) getApplication();
        pUtil=new ProgressUtil(this);


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getTakePhoto().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * 页面跳转回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case JumpCode.AddCommodityActivity_TO_ClassifyManageentActivity:
                if(RESULT_OK==resultCode){
                    cfbList= (List<ClassifyBean>) data.getSerializableExtra("classifyList");
                    if(adapter==null){
                        adapter=new HmyAdapter();
                        mHLV.setAdapter(adapter);
                    }
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

    /**
     * onClick
     * @param view
     */
    @OnClick({R.id.activity_add_commodity_iv_back,R.id.activity_add_commodity_btn_confirm,
            R.id.activity_add_commodity_iv_add_pic,R.id.activity_add_commodity_btn_add_classify,
    R.id.activity_add_commodity_btn_choose_type})
    public void addCommodityClick(View view){
        switch (view.getId()){
            //返回
            case R.id.activity_add_commodity_iv_back:
                finish();
                break;
            //添加图片
            case R.id.activity_add_commodity_iv_add_pic:
                createPop();
                break;
            //选择固有类型
            case R.id.activity_add_commodity_btn_choose_type:
                getAllTypes();
                break;

            //选择分类
            case R.id.activity_add_commodity_btn_add_classify:
                Intent intent=new Intent(AddCommodityActivity.this,ClassifyManagementActivity.class);
                intent.putExtra("ori","add");
                startActivityForResult(intent,JumpCode.AddCommodityActivity_TO_ClassifyManageentActivity);
                break;
            //确认添加商品
            case R.id.activity_add_commodity_btn_confirm:

                if(check()){
                    if(photoChanged){
                        upload(file);
                    }else{
                        if(-1!=logoId){
                            submit();
                        }
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
            Snackbar.make(mLLParent,"请输入优惠金额",Snackbar.LENGTH_SHORT).show();
            return false;
        }


        if(Double.valueOf(mETDisprice.getText().toString())>Double.valueOf(mETPrice.getText().toString())){
            Snackbar.make(mLLParent,"商品价格必须大于商品优惠金额",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(mTVType.getText().toString())){
            Snackbar.make(mLLParent,"请选择商品类型",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if(cfbList.size()<=0){
            Snackbar.make(mLLParent,"请选择商品分类",Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    @Override
    public void takeSuccess(TResult result) {
        file=new File(result.getImage().getPath());
        Bitmap bmp= BitmapFactory.decodeFile(file.getPath());
        mIVPreview.setImageBitmap(bmp);
        mTVAddPicRemind.setText("重新选择");
        //标志需要重新上传 photo
        photoChanged=true;
    }

    @Override
    public void takeFail(TResult result, String msg) {

    }

    @Override
    public void takeCancel() {

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
     * 弹出头像修改提示框
     */
    public void createPop(){
        List<String> data=new ArrayList<>();
        data.add("拍照");
        data.add("手机相册");
        final PopupDialog popDialog=new PopupDialog(AddCommodityActivity.this,data);
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
                        CropOptions cropOptions1=new CropOptions.Builder()
                                .setWithOwnCrop(false).create();
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
                view= LayoutInflater.from(AddCommodityActivity.this).inflate(R.layout.activity_commod_manage_item_item,null);
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
     * 提交修改、添加操作
     * @param
     */
    private void submit(){
        GoodsBean gb=new GoodsBean();
        /**
         * 封装数据参数
         */
        gb.setLogoId(logoId);//设置 photo 的 id
        gb.setPrice(Double.parseDouble(mETPrice.getText().toString()));//设置价格
        gb.setDisPrice(Double.valueOf(mETDisprice.getText().toString()));//优惠金额
        gb.setName(mETName.getText().toString());//设置商品名称
        //商品固有属性
        gb.setGoodstypeId(typeId);
        gb.setDetails(mETDetails.getText().toString());//设置描述
        gb.setStatus(1);//商品状态
        for (int i=0;i<cfbList.size();i++){
            cfbList.get(i).setDisplay(1);
        }
        //设置商家自定义分类集合
        gb.setClassifyBeanList(cfbList);
        String json=new Gson().toJson(gb);

        RequestParams params=new RequestParams(BnUrl.addOrModifyGoodsUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
        params.addBodyParameter("good",json);

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                LogUtil.e(response.toString());
                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            Snackbar.make(mLLParent,"商品添加成功",Snackbar.LENGTH_SHORT).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Intent intent=new Intent(AddCommodityActivity.this,CommodityManagementActivity.class);
                                    //startActivity(intent);
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
                                optionsPickerView=new OptionsPickerView<String>(AddCommodityActivity.this);
                            }
                            optionsPickerView.setPicker(typeNameList);
                            optionsPickerView.setOnOptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
                                @Override
                                public void onOptionsSelect(int option1, int option2, int option3) {
                                    //Toast.makeText(AddCommodityActivity.this,typeBeanList.get(option1).getName(), Toast.LENGTH_SHORT).show();
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
