package com.sctjsj.lazyhost.fragment;

import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.activity.AnnounceMagActivity;
import com.sctjsj.lazyhost.activity.BTScanActivity;
import com.sctjsj.lazyhost.activity.CommentActivity;
import com.sctjsj.lazyhost.activity.CommodityManagementActivity;

import com.sctjsj.lazyhost.activity.GalleryMagActivity;
import com.sctjsj.lazyhost.activity.IndexActivity;
import com.sctjsj.lazyhost.activity.LoginActivity;
import com.sctjsj.lazyhost.activity.MainShopActivity;
import com.sctjsj.lazyhost.activity.MywalletActivity;
import com.sctjsj.lazyhost.activity.RefundMagActivity;
import com.sctjsj.lazyhost.activity.SetStateActivity;
import com.sctjsj.lazyhost.activity.ShopInfoActivity;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.bean.TurnoverBean;
import com.sctjsj.lazyhost.constant.JumpCode;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.NumformatUtil;
import com.sctjsj.lazyhost.util.ProgressUtil;
import com.sctjsj.lazyhost.util.bt.BluetoothService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.RefreshHandler;

import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_CONNECTED;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_CONNECTING;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_LISTEN;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_NONE;


/**
 * Created by Chris-Jason on 2016/11/9.
 */
public class ShopMagFg extends Fragment {
    private MyApp app;
    private ProgressUtil pUtil;

    //店铺营业状态
    @Bind(R.id.fg_store_manager_state_tv)TextView mTVShopStatus;

    @Bind(R.id.fg_store_manager_printer_tv)TextView mTVBTState;
    //今日营业额统计
    @Bind(R.id.fg_shop_mag_tv_today_turnover)TextView mTVTodayTurnover;
    //历史营业额统计
    @Bind(R.id.fg_shop_mag_tv_total_turnover)TextView mTVTotalTurnover;
    //今日订单数量
    @Bind(R.id.fg_shop_mag_tv_today_order_count)TextView mtVTodayOrderCount;
    //历史总订单数量
    @Bind(R.id.fg_shop_mag_tv_total_order_count)TextView mTVTotalCount;

    @Bind(R.id.fg_shop_mag_refresh)QRefreshLayout refreshLayout;

    @Bind(R.id.fg_shop_mag_ll_refund_mag)LinearLayout mLLRefund;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fg_store_management,null);
        ButterKnife.bind(this,view);
        app= (MyApp) getActivity().getApplication();
        pUtil=new ProgressUtil(getActivity());
        app.setOnBTStateChangedListener(new MyApp.onBTStateChangedListener() {
            @Override
            public void stateChanged(final int currentState) {
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (currentState){
                            case STATE_NONE:
                                mTVBTState.setText("蓝牙未连接");
                                break;
                            case STATE_LISTEN:
                                mTVBTState.setText("等待连接蓝牙");
                                break;
                            case STATE_CONNECTING:
                                mTVBTState.setText("蓝牙连接中");
                                break;
                            case STATE_CONNECTED:
                                mTVBTState.setText("蓝牙已连接");
                                break;
                            default:
                                //mTVBTState.setText("蓝牙未连接");
                                break;

                        }
                    }
                });

            }
        });

        refreshLayout.setRefreshHandler(new RefreshHandler() {
            @Override
            public void onRefresh(QRefreshLayout refresh) {
                queryOrderCount();
                queryTurnover();
            }

            @Override
            public void onLoadMore(QRefreshLayout refresh) {

            }
        });
        view.findViewById(R.id.tv_to_package).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MywalletActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //判断店铺营业状态
        queryShopState();
        //查询店铺营业额
        queryTurnover();
        //店铺统计订单
        queryOrderCount();

        if(5==app.getCurrentUser().getShopType()){
            mLLRefund.setVisibility(View.GONE);
        }else {
            mLLRefund.setVisibility(View.VISIBLE);
        }
    }

    //
    @OnClick({R.id.fg_store_manager_printer_ll,R.id.fg_store_manager_evalutation_ll,R.id.fg_store_manager_state_ll,
    R.id.fg_store_manager_msg_ll,R.id.fg_store_manager_commod_ll,R.id.fg_store_manager_preview_ll,
            R.id.fg_store_manager_msg_ll_gallery,R.id.announce_mag,R.id.fg_shop_mag_ll_refund_mag})
    public void shopMsgListen(View view){
        switch (view.getId()){
            //连接打印机
            case R.id.fg_store_manager_printer_ll:
                //用 IndexActivity 来接收
                 Intent intent1=new Intent(getActivity(), BTScanActivity.class);
                 getActivity().startActivityForResult(intent1, JumpCode.JUMP_FROM_Index_TO_BTSCANActivity);
                break;
            //评价管理
            case R.id.fg_store_manager_evalutation_ll:
                Intent intent2=new Intent(getActivity(), CommentActivity.class);
                startActivity(intent2);
                break;
            //营业状态
            case R.id.fg_store_manager_state_ll:
                Intent intent3=new Intent(getActivity(), SetStateActivity.class);
                startActivity(intent3);
                getActivity().overridePendingTransition(R.anim.activity_anim_start,R.anim.activity_anim_end);
                break;
            //店铺信息
            case R.id.fg_store_manager_msg_ll:
                Intent intent8=new Intent(getActivity(), ShopInfoActivity.class);
                startActivity(intent8);
                break;
            //商品管理
            case R.id.fg_store_manager_commod_ll:
                Intent intent5=new Intent(getActivity(), CommodityManagementActivity.class);
                startActivity(intent5);
                getActivity().overridePendingTransition(R.anim.activity_anim_start,R.anim.activity_anim_end);
                break;
            //店铺预览
            case R.id.fg_store_manager_preview_ll:
                Intent intent=new Intent(getActivity(), MainShopActivity.class);
                intent.putExtra("shopId",app.getCurrentUser().getShopId());
                intent.putExtra("shopType",app.getCurrentUser().getShopType());
                startActivity(intent);
                break;

            //相册管理
            case R.id.fg_store_manager_msg_ll_gallery:
                Intent intent9=new Intent(getActivity(), GalleryMagActivity.class);
                startActivity(intent9);
                break;
            //商家公告管理
            case R.id.announce_mag:
                Intent intent4=new Intent(getActivity(), AnnounceMagActivity.class);
                startActivity(intent4);
                break;
            //退款管理
            case R.id.fg_shop_mag_ll_refund_mag:
                Intent intent6=new Intent(getActivity(), RefundMagActivity.class);
                startActivity(intent6);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 查询当前店铺状态
     */
    private void queryShopState(){
        try {
            if (app == null){
                if (getActivity() == null){
                    return;
                }
                app = (MyApp) getActivity().getApplication();
            }
            if (app.getCurrentUser() == null) {
                Toast.makeText(getActivity(), "登录超时,请重新登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return;
            }
            RequestParams paramas=new RequestParams(BnUrl.queryShopStateUrl);
            paramas.setUseCookie(false);
            paramas.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
            paramas.addBodyParameter("storeId",String.valueOf(app.getCurrentUser().getShopId()));
            x.http().post(paramas, new Callback.ProgressCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject result) {
                    try {
                        int storeStatus=result.getInt("storeStatus");
                        app.getSpf().edit().putInt("shopStatus",storeStatus).commit();

                        if(2==storeStatus){
                           mTVShopStatus.setText("正在营业中");
                        }
                        if(3==storeStatus){
                           mTVShopStatus.setText("已打烊");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询店铺营业额统计
     */
    private void queryTurnover(){
        try {
            if (app == null){
                if (getActivity() == null){
                    return;
                }
                app = (MyApp) getActivity().getApplication();
            }
            if (app.getCurrentUser() == null) {
                Toast.makeText(getActivity(), "登录超时,请重新登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return;
            }
            //http://www.lp-kd.com/user/countTurnover$ajax.htm?user_id=
            RequestParams paramas=new RequestParams(BnUrl.countTurnoverUrl);
            paramas.setUseCookie(false);
            paramas.addHeader("Cookie",app.getSpf().getString("cookie","none-cookie"));
            paramas.addBodyParameter("user_id",String.valueOf(app.getCurrentUser().getUserId()));
            x.http().post(paramas, new Callback.ProgressCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.e("营业额统计",response.toString());
                    if(response!=null){
                        try {
                            String countTurnover=response.getString("countTurnover");
                            if(countTurnover!=null && !TextUtils.isEmpty(countTurnover)){
                                double todayBalance = response.getJSONObject("countTurnover").getDouble("todayBalance");
                                double historyBalance = response.getJSONObject("countTurnover").getString("historyBalance") == null || response.getJSONObject("countTurnover").getString("historyBalance").equals("null")?0.00:response.getJSONObject("countTurnover").getDouble("historyBalance");
                                if (historyBalance<=0){
                                    mTVTotalTurnover.setText("￥0.0");
                                }else {
                                    mTVTotalTurnover.setText("￥"+NumformatUtil.save2(historyBalance));
                                }
                                if (todayBalance <= 0){
                                    mTVTodayTurnover.setText("￥0.0");
                                }else {
                                    mTVTodayTurnover.setText("￥"+NumformatUtil.save2(todayBalance));
                                }
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    Log.e("TAG","错误:"+ex.toString());
                    pUtil.dismissProgress();
                }

                @Override
                public void onCancelled(CancelledException cex) {
                    pUtil.dismissProgress();
                }

                @Override
                public void onFinished() {
                    pUtil.dismissProgress();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 订单数量统计
     */
    private void queryOrderCount(){
        try {
            if (app == null){
                if (getActivity() == null){
                    return;
                }
                app = (MyApp) getActivity().getApplication();
            }
            if (app.getCurrentUser() == null) {
                Toast.makeText(getActivity(), "登录超时,请重新登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return;
            }
            RequestParams params=new RequestParams(BnUrl.countOrderUrl);
            params.setUseCookie(false);
            //http://www.lp-kd.com/user/ObtionOrderformSum$ajax.htm?userId=
            params.addHeader("Cookie",app.getSpf().getString("cookie",""));
            params.addBodyParameter("userId",String.valueOf(app.getCurrentUser().getUserId()));
            x.http().post(params, new Callback.CommonCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.e("订单数量统计",response.toString());
                    if(response!=null){
                        try {
                            int count=response.getInt("count");
                            if(count > 0){
                                mTVTotalCount.setText(count+"单");
                            }else {
                                mTVTotalCount.setText("暂无历史订单");
                            }
                            /**
                             * 今日订单
                             */
                            int todayCount=response.getInt("todayCount");
                            if(todayCount>0){
                                mtVTodayOrderCount.setText("今日"+todayCount+"单");
                            }else {
                                mtVTodayOrderCount.setText("今日暂无订单");
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
                        refreshLayout.refreshComplete();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 101:
                //连接成功
                if(resultCode==getActivity().RESULT_OK){

                }else {

                }
                break;
        }
    }


}
