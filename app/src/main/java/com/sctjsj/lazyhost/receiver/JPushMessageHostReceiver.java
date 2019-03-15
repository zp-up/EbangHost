package com.sctjsj.lazyhost.receiver;

import android.content.Context;
import android.util.Log;

import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * Created by wuqaing on 2018/11/26.
 */

public class JPushMessageHostReceiver extends JPushMessageReceiver {
    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onAliasOperatorResult(context, jPushMessage);
        Log.e("TAG","onAliasOperatorResult:"+jPushMessage.getTags());
    }

    @Override
    public void onCheckTagOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onCheckTagOperatorResult(context, jPushMessage);
        Log.e("TAG","onCheckTagOperatorResult:"+jPushMessage.getTags());
    }

    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onMobileNumberOperatorResult(context, jPushMessage);
        Log.e("TAG","onMobileNumberOperatorResult:"+jPushMessage.getTags());
    }

    @Override
    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onTagOperatorResult(context, jPushMessage);
        Log.e("TAG","onTagOperatorResult:"+jPushMessage.getTags());
    }
}
