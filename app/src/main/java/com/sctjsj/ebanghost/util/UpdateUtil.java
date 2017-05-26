package com.sctjsj.ebanghost.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.application.MyApp;


import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;
import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * Created by Chris-Jason on 2016/10/19.
 */
public class UpdateUtil {
    private String TAG="UpdateUtil";
    private static MyApp app;
    private static UpdateUtil instance;
    private static Activity context;
    //    下载文件保存路径
    private String downloadFilePath= Environment.getExternalStorageDirectory().getPath()+"/ebanghost/download";
    private String downLoadUrl;
    private AlertDialog.Builder builder,progressBuilder;
    private AlertDialog dialog,progressDialog;

    private AlertDialog.Builder downBuilder;
    private static AlertDialog downDialog;
    private NumberProgressBar numberProgressBar;



    public static UpdateUtil getInstance(Activity c){
        if(instance==null){
            instance=new UpdateUtil();
        }
        context=c;
        app= (MyApp) context.getApplicationContext();
        return instance;
    }


    /**
     * 检查服务端最新 app版本
     */
    //主动检查版本
    public  void checkVersion(String checkUrl,int type){
        RequestParams params=new RequestParams(checkUrl);
        params.addBodyParameter("appTerminal",String.valueOf(type));
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            String str=response.getString("resultData");
                            if(str!=null){
                                JSONObject info=new JSONObject(str);
                                if(info!=null){
                                    int appTerminal=info.getInt("appTerminal");//app 终端
                                    int latestVersion=info.getInt("appVersionId");//最新 app 版本
                                    String versionId=info.getString("versionId");//显示出来的版本号

                                    int isUpdate=info.getInt("isUpdate");//是否准备好更新了 1：审核通过了，2：审核没通过
                                    int isForce=info.getInt("isForce");//是否强制更新 1：强制更新，2：一般更新
                                    String downUrl=info.getString("downUrl");//更新地址
                                    String content=info.getString("contents");//更新内容
                                    //必须通过审核的新 apk，还要版本大于当前版本
                                    if(1==isUpdate && latestVersion>getLocalVersion()){

                                        if(1==isForce){
                                            alertImportant(versionId,content,downUrl);
                                        }

                                        if(2==isForce){
                                            alert(versionId,content,downUrl);
                                        }

                                    }

                                }
                            }
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
        });
    }

    //手动检查版本
    public  void checkVersionManual(String checkUrl,int type){
        RequestParams params=new RequestParams(checkUrl);
        params.addBodyParameter("appTerminal",String.valueOf(type));
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if(response!=null){
                    try {
                        boolean result=response.getBoolean("result");
                        if(result){
                            String str=response.getString("resultData");
                            if(str!=null){
                                JSONObject info=new JSONObject(str);
                                if(info!=null){
                                    int appTerminal=info.getInt("appTerminal");//app 终端
                                    int latestVersion=info.getInt("appVersionId");//最新 app 版本
                                    String versionId=info.getString("versionId");//显示出来的版本号

                                    int isUpdate=info.getInt("isUpdate");//是否准备好更新了 1：审核通过了，2：审核没通过
                                    int isForce=info.getInt("isForce");//是否强制更新 1：强制更新，2：一般更新
                                    String downUrl=info.getString("downUrl");//更新地址
                                    String content=info.getString("contents");//更新内容
                                    //必须通过审核的新 apk，还要版本大于当前版本
                                    if(1==isUpdate && latestVersion>getLocalVersion()){

                                        if(1==isForce){
                                            alertImportant(versionId,content,downUrl);
                                        }

                                        if(2==isForce){
                                            alert(versionId,content,downUrl);
                                        }

                                    } else {
                                        //主动检查更新时
                                        AlertDialog.Builder bb=new AlertDialog.Builder(context);
                                        bb.setTitle("检查更新");
                                        bb.setIcon(R.mipmap.icon_app_start);
                                        bb.setMessage("当前已是最新版本，无需更新");
                                        bb.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                        bb.show();

                                    }
                                }
                            }
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
        });
    }


    /**
     * 获取当前安装的版本号
     * @return
     */
    private int getLocalVersion(){
        int version=-1;
        try {
            version= context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }


    /**
     * 提示安装apk
     * @param context
     * @param path
     */
    private void installApp(Context context, String path){
        File file=new File(path);
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 下载apk
     * @param url
     */
    private void downloadApk(String url){
        checkSavePath();
        RequestParams params=new RequestParams(url);
        params.addHeader("platform","android_app");
        final String  filePath=downloadFilePath+"/x.apk";
        params.setSaveFilePath(filePath);//设置文件保存路径
        params.setAutoResume(true);//设置自动断点续传
        params.setConnectTimeout(5*1000);
        params.setAutoRename(false);
        final Callback.Cancelable cancelAble=x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(final File result) {

                downDialog.dismiss();

                if(result==null){
                    final SweetAlertDialog sad=new SweetAlertDialog(context,SweetAlertDialog.ERROR_TYPE);
                    sad.setTitleText("下载失败");
                    sad.setContentText("程序下载过程中遭遇未知异常！");
                    sad.setCancelable(false);
                    sad.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sad.dismiss();
                        }
                    });
                    sad.show();
                }


                SweetAlertDialog sad=new SweetAlertDialog(context,SweetAlertDialog.SUCCESS_TYPE);
                sad.setTitleText("下载完成");
                sad.setContentText("立即安装最新版本？");
                sad.setCancelable(false);
                sad.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        installApp(context,result.getAbsolutePath());
                    }
                });
                sad.show();


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

                if(downBuilder==null && downDialog==null){
                    downBuilder=new AlertDialog.Builder(context);
                    downBuilder.setTitle("下载中");
                    downBuilder.setCancelable(false);
                    View v=LayoutInflater.from(context).inflate(R.layout.dialog_download_progress_layout,null);
                    numberProgressBar= (NumberProgressBar) v.findViewById(R.id.number_progress_bar);
                    downBuilder.setView(v);
                    downDialog=downBuilder.create();
                    if(!downDialog.isShowing()) {
                        downDialog.show();
                    }
                }

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

                double a=Double.valueOf(total);
                double b=Double.valueOf(current);
                double c=b/a;
                java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#");
                final int p=Integer.valueOf(df.format(c*100));
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        numberProgressBar.setProgress(p);
                    }
                });


            }

        });

    }

    /**
     * 检查是否存在保存的文件夹
     */
    private void checkSavePath(){
        //创建下载文件夹
        File file=new File(downloadFilePath);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    private void alert(String version,String con, final String url){
        final SweetAlertDialog dialog=new SweetAlertDialog(context);
        dialog.setTitleText("更新提示");
        dialog.setCancelable(true);
        dialog.setContentText("最新版本："+version+"\n"+"更新内容："+con);
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                dialog.dismiss();
                downloadApk(url);
            }
        });
        dialog.show();
    }

    /**
     * 重大更新
     * @param con
     * @param url
     */
    private void alertImportant(String version,String con, final String url){
        final SweetAlertDialog dialog=new SweetAlertDialog(context);
        dialog.setTitleText("重大更新提示");
        dialog.setCancelable(false);
        dialog.setContentText("最新版本："+version+"\n"+"更新内容："+con);
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                dialog.dismiss();
                downloadApk(url);
            }
        });
        dialog.show();
    }


}
