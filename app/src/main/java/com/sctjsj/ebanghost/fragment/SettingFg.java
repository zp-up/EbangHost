package com.sctjsj.ebanghost.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.activity.UseFeedbackActivity;
import com.sctjsj.ebanghost.activity.WarningToneSetActivity;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.DpUtils;
import com.sctjsj.ebanghost.util.LogUtil;
import com.sctjsj.ebanghost.util.ProgressUtil;
import com.sctjsj.ebanghost.util.RingtoneUtil;
import com.squareup.picasso.Picasso;
import com.zcw.togglebutton.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Chris-Jason on 2016/11/9.
 */
public class SettingFg extends Fragment{
    private MyApp app;
    private ProgressUtil pUtil;

    @Bind(R.id.fg_set_iv_logo)ImageView mIVLogo;
    @Bind(R.id.fg_set_tv_name)TextView mTVName;
    @Bind(R.id.fg_set_tv_address)TextView mTVAddress;
    @Bind(R.id.fg_set_tv_phone)TextView mTVPhone;
    @Bind(R.id.fg_set_btn_logout)Button mBtnLogout;

    @Bind(R.id.fg_set_toggle)ToggleButton vibrateToggle;

    @Bind(R.id.fg_set_tv_version)TextView mTVVersion;


    private  String storeName;//店家名字
    private String storeAddress;//店家地址
    private String telephone;//店家电话

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fg_set,null);
        ButterKnife.bind(this,view);
        app= (MyApp) getActivity().getApplication();

        /**
         * 设置版本
         */
        try {
            String version=getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            if(version!=null){
                mTVVersion.setText("v_"+version);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        pUtil=new ProgressUtil(getActivity());
        //设置toggle 开关
        if(app.getSpf().getBoolean("vibrateOn",true)){
            vibrateToggle.setToggleOn();
        }else{
            vibrateToggle.setToggleOff();
        }

        vibrateToggle.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if(on){
                    app.getSpf().edit().putBoolean("vibrateOn",true).commit();
                }else {
                    app.getSpf().edit().putBoolean("vibrateOn",false).commit();
                }
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        pullShopInfoById();
    }

    /**
     * 根据店铺 id 查询店铺信息
     */
    private void pullShopInfoById() {

        RequestParams params=new RequestParams(BnUrl.getShopInfoById);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none_cookie"));
        params.addBodyParameter("ctype","store");
        params.addBodyParameter("cond","{id:"+app.getCurrentUser().getShopId()+"}");
        params.addBodyParameter("jf","storeLogo");

        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){

                    try {
                        JSONArray resultList=response.getJSONArray("resultList");
                        if(resultList.length()>0){
                            storeName=resultList.getJSONObject(0).getString("storeName");//店铺名
                            storeAddress=resultList.getJSONObject(0).getString("storeAddress");//店铺地址
                            int storeStatus=resultList.getJSONObject(0).getInt("storeStatus");//店铺状态
                            telephone=resultList.getJSONObject(0).getString("telephone");//店家电话
                            String logo=resultList.getJSONObject(0).getJSONObject("storeLogo").getString("url");//logo 地址

                            Picasso.with(getActivity()).load(logo).resize(DpUtils.dpToPx(getActivity(),80),DpUtils.dpToPx(getActivity(),80)).error(R.mipmap.icon_portrait_load_failed).into(mIVLogo);
                            mTVName.setText(storeName);
                            mTVAddress.setText(storeAddress);
                            mTVPhone.setText(telephone);


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

    @OnClick({R.id.activity_set_music,R.id.activity_set_advice,R.id.fg_set_btn_logout,R.id.fg_set_rl_call_service})
    public void setFgListen(View view){
        switch (view.getId()){
            //设置提示音
            case R.id.activity_set_music:
                Intent intent=new Intent(getActivity(), WarningToneSetActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.activity_anim_start,R.anim.activity_anim_end);
                break;
            //使用反馈
            case R.id.activity_set_advice:
                Intent intent1=new Intent(getActivity(), UseFeedbackActivity.class);
                intent1.putExtra("storeName",storeName);
                intent1.putExtra("storeAddress",storeAddress);
                intent1.putExtra("telephone",telephone);
                startActivity(intent1);
                getActivity().overridePendingTransition(R.anim.activity_anim_start,R.anim.activity_anim_end);
                break;
            //退出
            case R.id.fg_set_btn_logout:
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder.setTitle("退出提示");
                builder.setMessage("确认退出？");
                builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                break;
            //联系客服
            case R.id.fg_set_rl_call_service:
                AlertDialog.Builder builder1=new AlertDialog.Builder( getActivity());
                builder1.setTitle("联系客服");
                builder1.setMessage("蓝胖商户将要拨打电话：400-100-5768");
                builder1.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        makeCall("400-100-5768");
                    }
                });
                builder1.show();
                break;
        }
    }

    /**
     *退出
     */
    private void logout(){
        RequestParams params=new RequestParams(BnUrl.logoutUrl);
        params.setUseCookie(false);
        params.addHeader("Cookie",app.getSpf().getString("cookie","none_cookie"));
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            //清除本地信息
                            app.getSpf().edit().clear().commit();
                            //退出成功
                            app.finishAllActivity();
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
     * 拨打电话
     * @param no
     */
    private void makeCall(String no){
        if(no!=null){
            Intent intent = new Intent(Intent.ACTION_DIAL);
            Uri data = Uri.parse("tel:"+no);
            intent.setData(data);
            startActivity(intent);
        }
    }

}
