package com.sctjsj.lazyhost.util;

/**
 * Created by mayikang on 17/3/8.
 */

import android.support.design.widget.Snackbar;
import android.util.Log;

import com.sctjsj.lazyhost.url.BnUrl;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * 推送工具类
 *
 */
public class PushNotifyUtil {

    /**
     * 1:商家已接单 2:商家拒绝接单 3:正在配送中 4:已送达 5：等待配送员接单 6：骑手已接单 7：二维码已核销 8：商家同意退款 9：商家拒绝退款
     * @param orderId 订单号
     * @param type 推送字典值
     */
    public static void ShopPushToBuyer(String orderId, final String type, final PushResultCallack callack){

        RequestParams params=new RequestParams(BnUrl.pushMessageToBuyerUrl);
        params.setUseCookie(false);
        params.addBodyParameter("ofId",orderId);
        params.addBodyParameter("type",type);
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.e("商家推送给买家",response.toString());
                try {
                    boolean result=response.getBoolean("result");
                    if(result){
                       callack.pushSuccess();
                    } else {
                        callack.pushFaild();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
               callack.pushFaild();
            }

            @Override
            public void onCancelled(CancelledException cex) {
               callack.pushFaild();
            }

            @Override
            public void onFinished() {

            }

        });


    }

    /**
     * 商家推送给配送员
     * @param orderId
     * @param callback
     */
    public static void ShopPushToDelivery(String orderId,final PushResultCallack callback){
        RequestParams params=new RequestParams(BnUrl.pushMessageToDeliveryUrl);
        params.setUseCookie(false);
        params.addBodyParameter("ofId",orderId);

        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            callback.pushSuccess();
                        }else {
                            callback.pushFaild();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                callback.pushFaild();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                callback.pushFaild();
            }

            @Override
            public void onFinished() {

            }


        });
    }


    /**
     * 推送结果回调接口
     */
    public interface PushResultCallack{
        void pushSuccess();
        void pushFaild();
    }

}
