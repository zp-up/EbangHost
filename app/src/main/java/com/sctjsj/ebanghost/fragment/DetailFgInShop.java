package com.sctjsj.ebanghost.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.activity.MainShopActivity;
import com.sctjsj.ebanghost.adapter.ShopGalleryRVAdapter;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.url.BnUrl;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Chris-Jason on 2016/11/4.
 */

public class DetailFgInShop extends Fragment {
    private String TAG="DetailFgShop";
    //商家相册
    @Bind(R.id.fg_detail_in_shop_rv_gallery)RecyclerView mRVGallery;
    @Bind(R.id.fg_detail_in_shop_tv_shop_gallery) TextView mTVShopGallery;
    @Bind(R.id.fg_detail_in_shop_rl_gallery)RelativeLayout mRLGallery;
    //展示信息
    @Bind(R.id.fg_detail_in_shop_tv_tel)TextView mTVShopTel;
    @Bind(R.id.fg_detail_in_shop_tv_address)TextView mTVShopAddress;
    @Bind(R.id.fg_detail_in_shop_tv_delivery_time)TextView mTVDeliveryTime;

    //配送时间
    @Bind(R.id.fg_detail_in_shop_ll_delivery_time_parent)LinearLayout mLLDeliveryTimeParent;

    private ShopGalleryRVAdapter galleryAdapter;
    private List<String> picList;
    private MyApp app;
    private String shopId;//店铺 id
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fg_detail_in_shop,null);
        ButterKnife.bind(this,view);
        app= (MyApp) getActivity().getApplication();
        //隐藏配送时间
        if(6==app.getCurrentUser().getShopType()){
            mLLDeliveryTimeParent.setVisibility(View.GONE);
        }
        pullShopInfoById(String.valueOf(((MainShopActivity)getActivity()).getShopId()) );
        //initGallery();
        return view;
    }

    /**
     * 初始化商家相册
     */
    private void initGallery() {
        //1.初始化Gallery数据
        initGalleryData();
        //2.初始化Gallery适配器
        initGalleryAdapter();
    }

    /**
     * 初始化Gallery适配器
     */
    private void initGalleryAdapter() {
        if(galleryAdapter==null){
            galleryAdapter=new ShopGalleryRVAdapter(getActivity(),picList);
        }
        mRVGallery.setAdapter(galleryAdapter);
        LinearLayoutManager llManager=new LinearLayoutManager(getActivity());
        llManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRVGallery.setLayoutManager(llManager);
    }

    /**
     * 初始化Gallery数据{网络加载}
     */
    private void initGalleryData() {
        //1.解析数据
        if(picList==null){
            picList=new ArrayList<>();
        }
        picList.add("http://cs407831.userapi.com/v407831207/18f6/jBaVZFDhXRA.jpg");
        picList.add("http://cs407831.userapi.com/v407831207/1906/oxoP6URjFtA.jpg");
        picList.add("http://cs407831.userapi.com/v407831207/190e/2Sz9A774hUc.jpg");
        picList.add("http://cs407831.userapi.com/v407831207/1916/Ua52RjnKqjk.jpg");
        //2.判断商家是否有数据
        if(picList.size()==0){
            mTVShopGallery.setText("商家相册（商家相册暂无照片）");
            mRLGallery.setVisibility(View.GONE);
        }else{
            mTVShopGallery.setText("商家相册");
            mRLGallery.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    /**
     * 根据 id 查店铺信息
     * @param id
     */
    public void pullShopInfoById(String id){
        RequestParams paramas=new RequestParams(BnUrl.getShopInfoById);
        paramas.setUseCookie(false);
        paramas.addBodyParameter("size","999999");
        paramas.addBodyParameter("ctype","store");
        paramas.addBodyParameter("cond","{id:"+id+"}");
        paramas.addBodyParameter("jf","storeLogo|usergoosclass|goods|photo|ugc");
        x.http().post(paramas, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject results) {
                if(results!=null){
                    try {
                        boolean result=results.getBoolean("result");
                        //解析成功
                        if(result){
                            //店铺的所有信息
                            JSONObject storeInfo=results.getJSONArray("resultList").getJSONObject(0);
                            //店铺地址
                            String storeAddress=storeInfo.getString("storeAddress");
                            mTVShopAddress.setText(storeAddress);
                            //店铺 id
                            String id=storeInfo.getString("id");
                            //电话
                            String telephone=storeInfo.getString("telephone");
                            mTVShopTel.setText(telephone);
                            //店铺类型
                            String type=storeInfo.getString("type");
                            String sendBeginTime=storeInfo.getString("sendBeginTime");
                            String sendEndTime=storeInfo.getString("sendEndTime");
                            mTVDeliveryTime.setText(sendBeginTime+"-"+sendEndTime);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{

                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e(TAG,ex.toString());

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

}
