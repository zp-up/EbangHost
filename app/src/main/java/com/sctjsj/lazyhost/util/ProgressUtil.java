package com.sctjsj.lazyhost.util;

/**
 * Created by Chris-Jason on 2016/11/14.
 */

import android.app.Dialog;
import android.content.Context;
import com.sctjsj.lazyhost.R;


/**
 * 提示耗时操作的进度
 */
public class ProgressUtil {
    private  Context activity;
    private Dialog dialog;

    public ProgressUtil(Context context){
        this.activity=context;
    }

    /**
     * 显示进度
     * @param
     */
    public void showProgress(boolean cancelable){

        if(dialog==null) {
            dialog = new Dialog(activity, R.style.progress_dialog);
            dialog.setContentView(R.layout.progress_dialog);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCancelable(cancelable);
        dialog.show();
    }

    /**
     * 关闭进度
     */
    public void dismissProgress(){
        if(dialog!=null){
            dialog.dismiss();
        }
    }
}
