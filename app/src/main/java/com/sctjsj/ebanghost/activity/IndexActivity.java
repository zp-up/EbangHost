package com.sctjsj.ebanghost.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.igexin.sdk.PushManager;
import com.sctjsj.ebanghost.R;
import com.sctjsj.ebanghost.application.MyApp;
import com.sctjsj.ebanghost.constant.JumpCode;
import com.sctjsj.ebanghost.fragment.OrderQueryFg;
import com.sctjsj.ebanghost.fragment.ProcessOrderFg;
import com.sctjsj.ebanghost.fragment.SettingFg;
import com.sctjsj.ebanghost.fragment.ShopMagFg;

import com.sctjsj.ebanghost.url.BnUrl;
import com.sctjsj.ebanghost.util.UpdateUtil;
import com.sctjsj.ebanghost.util.bt.BluetoothService;

import java.io.UnsupportedEncodingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.sctjsj.ebanghost.constant.OtherConstant.DEVICE_NAME;
import static com.sctjsj.ebanghost.constant.OtherConstant.MESSAGE_DEVICE_NAME;
import static com.sctjsj.ebanghost.constant.OtherConstant.MESSAGE_READ;
import static com.sctjsj.ebanghost.constant.OtherConstant.MESSAGE_STATE_CHANGE;
import static com.sctjsj.ebanghost.constant.OtherConstant.MESSAGE_WRITE;
import static com.sctjsj.ebanghost.constant.OtherConstant.REQUEST_ENABLE_BT;
import static com.sctjsj.ebanghost.constant.OtherConstant.STATE_CONNECTED;
import static com.sctjsj.ebanghost.constant.OtherConstant.STATE_CONNECTING;
import static com.sctjsj.ebanghost.constant.OtherConstant.STATE_LISTEN;
import static com.sctjsj.ebanghost.constant.OtherConstant.STATE_NONE;

public class IndexActivity extends AppCompatActivity {
    //图标
    @Bind(R.id.activity_index_iv_process_order)ImageView mIVProcessOrder;
    @Bind(R.id.activity_index_iv_search_order)ImageView mIVSearchOrder;
    @Bind(R.id.activity_index_iv_shop_mag)ImageView mIVShopMag;
    @Bind(R.id.activity_index_iv_setting)ImageView mIVSetting;
    //文字
    @Bind(R.id.activity_index_tv_process_order)TextView mTVProcessOrder;
    @Bind(R.id.activity_index_tv_search_order)TextView mTVSearchOrder;
    @Bind(R.id.activity_index_tv_shop_mag)TextView mTVShopMag;
    @Bind(R.id.activity_index_tv_setting)TextView mTVSetting;


    private String TAG="IndexActivity";
    private boolean isExited=false;//标志是否已经退出
    private MyApp app;

    private Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    isExited=false;
                    break;
            }
        }
    };

    /**
     * 碎片
     * @param savedInstanceState
     */
    private FragmentManager fgManager;
    private ProcessOrderFg processOrderFg;
    private OrderQueryFg orderQueryFg;
    private ShopMagFg shopMagFg;
    private SettingFg settingFg;

    //连接进度
    private SweetAlertDialog connectProgress;
    private BluetoothService bluetoothService;
    private BluetoothAdapter bluetoothAdapter;
    private String mConnectedDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置保持屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_index);
        //注册个推
        PushManager.getInstance().initialize(this.getApplicationContext());
        ButterKnife.bind(this);
        app= (MyApp) getApplication();

        UpdateUtil.getInstance(this).checkVersion(BnUrl.updateUrl,2);


        //实例碎片管理器
        fgManager=getSupportFragmentManager();
        //默认加载首页
        if(processOrderFg==null){
            processOrderFg=new ProcessOrderFg();
        }
        fgManager.beginTransaction().add(R.id.activity_index_rl_content,processOrderFg).show(processOrderFg).commit();

        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();

        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    /**
     * 清除导航栏所有的选中状态
     */
    private void setNavbarNotSelected(){
        //灰色字体
        int colorGray= Color.parseColor("#80000000");
        //1.所有图标设置为未选中状态
        mIVProcessOrder.setImageResource(R.mipmap.icon_process_order);
        mIVSearchOrder.setImageResource(R.mipmap.icon_search_order);
        mIVShopMag.setImageResource(R.mipmap.icon_shop_mag);
        mIVSetting.setImageResource(R.mipmap.icon_setting);

        //2.设置所有文字为未选中的颜色
        mTVProcessOrder.setTextColor(colorGray);
        mTVSearchOrder.setTextColor(colorGray);
        mTVShopMag.setTextColor(colorGray);
        mTVSetting.setTextColor(colorGray);

    }

    /**
     * 隐藏所有fragment
     */
    private void hideAllFgIfNotNull(){
        FragmentTransaction transaction=fgManager.beginTransaction();
        if(processOrderFg!=null){
            transaction.hide(processOrderFg);
        }
        if(orderQueryFg!=null){
            transaction.hide(orderQueryFg);
        }
        if(shopMagFg !=null){
            transaction.hide(shopMagFg);
        }
        if(settingFg!=null){
            transaction.hide(settingFg);
        }

        //提交
        transaction.commit();
    }

    @OnClick({R.id.activity_index_ll_process_order,R.id.activity_index_ll_search_order,
            R.id.activity_index_ll_order_shop_mag,R.id.activity_index_ll_setting})
    public void IndexActivityClick(View view){
        int colorGreen=Color.parseColor("#FF4081");
        //1.隐藏所有碎片
        hideAllFgIfNotNull();
        //2.图片和文字设置为未选中
        setNavbarNotSelected();
        //3.设置选中导航状态
        switch (view.getId()){
            //处理订单
            case R.id.activity_index_ll_process_order:
                mIVProcessOrder.setImageResource(R.mipmap.icon_process_order_selected);
                mTVProcessOrder.setTextColor(colorGreen);
                if(processOrderFg==null){
                    processOrderFg=new ProcessOrderFg();
                    fgManager.beginTransaction().add(R.id.activity_index_rl_content,processOrderFg).
                            show(processOrderFg).commit();
                }else{
                    fgManager.beginTransaction().show(processOrderFg).commit();
                }

                break;
            //查询订单
            case R.id.activity_index_ll_search_order:
                mIVSearchOrder.setImageResource(R.mipmap.icon_search_order_selected);
                mTVSearchOrder.setTextColor(colorGreen);
                if(orderQueryFg==null){
                    orderQueryFg=new OrderQueryFg();
                    fgManager.beginTransaction().add(R.id.activity_index_rl_content,orderQueryFg).
                            show(orderQueryFg).commit();
                }else{
                    fgManager.beginTransaction().show(orderQueryFg).commit();
                }
                break;
            //店铺管理
            case R.id.activity_index_ll_order_shop_mag:
                mIVShopMag.setImageResource(R.mipmap.icon_shop_mag_selected);
                mTVShopMag.setTextColor(colorGreen);

                if(shopMagFg==null){
                    shopMagFg=new ShopMagFg();
                    fgManager.beginTransaction().add(R.id.activity_index_rl_content,shopMagFg).
                            show(shopMagFg).commit();
                }else{
                    fgManager.beginTransaction().show(shopMagFg).commit();
                }
                break;
            //设置
            case R.id.activity_index_ll_setting:
                mIVSetting.setImageResource(R.mipmap.icon_setting_selected);
                mTVSetting.setTextColor(colorGreen);

                if(settingFg==null){
                    settingFg=new SettingFg();
                    fgManager.beginTransaction().add(R.id.activity_index_rl_content,settingFg).
                            show(settingFg).commit();
                }else{
                    fgManager.beginTransaction().show(settingFg).commit();
                }
                break;

        }
    }

    /**
     * 重写返回键
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //物理返回键值
        if(keyCode==KeyEvent.KEYCODE_BACK){
            //连续点击两次返回键退出程序
            exit();
            return false;
        }
        return super.onKeyDown(keyCode,event);
    }
    /**
     * 退出程序
     */
    private void exit(){
        if (!isExited) {
            isExited = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            Message msg=new Message();
            msg.what=0;
            mHandler.sendMessageDelayed(msg,2000);
        } else {
            finish();
            System.exit(0);
        }
    }



    private Handler mxHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:

                    switch (msg.arg1) {
                        //连接成功
                        case STATE_CONNECTED:
                            //关闭连接进度
                            if(connectProgress!=null){
                                connectProgress.dismiss();
                            }
                            Toast.makeText(IndexActivity.this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();

                            break;
                        //正在连接
                        case STATE_CONNECTING:
                            if(connectProgress==null){
                                connectProgress=new SweetAlertDialog(IndexActivity.this,SweetAlertDialog.PROGRESS_TYPE);
                            }
                            connectProgress.setTitleText("");
                            connectProgress.setCancelable(false);
                            connectProgress.setCancelText("取消连接");
                            connectProgress.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    connectProgress.dismiss();
                                    bluetoothService.stop();
                                }
                            });
                            connectProgress.setContentText("正在连接设备");
                            connectProgress.show();
                            break;

                        //连接失败
                        case STATE_LISTEN:
                            if(connectProgress!=null){
                                connectProgress.dismiss();
                            }
                            break;
                        case STATE_NONE:
                            Toast.makeText(IndexActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
                            if(connectProgress!=null){
                                connectProgress.dismiss();
                            }

                            break;
                    }
                    break;

                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    break;

                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // 获取连接设备的名字
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(IndexActivity.this, "连接至"
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };



    /**
     * 打印
     * @param message
     */
    public void sendPrintMessage(Context context, BluetoothService bluetoothService, String message) {

        if(bluetoothService==null){
            return;
        }

        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != STATE_CONNECTED) {
            Toast.makeText(context, "蓝牙未连接", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send;
            try {
                send = message.getBytes("GB2312");
            } catch (UnsupportedEncodingException e) {
                send = message.getBytes();
            }

            bluetoothService.write(send);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            //打开蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        if(bluetoothService==null){
            bluetoothService=new BluetoothService(IndexActivity.this,mxHandler);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case JumpCode.JUMP_FROM_Index_TO_BTSCANActivity:
                if(RESULT_OK==resultCode){
                    //获取
                    String address=data.getStringExtra("device");

                    if(bluetoothAdapter==null){
                        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
                    }

                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    bluetoothService.connect(device);
                }
                break;
        }
    }


    public BluetoothService getBTService(){

        return bluetoothService;
    }
}
