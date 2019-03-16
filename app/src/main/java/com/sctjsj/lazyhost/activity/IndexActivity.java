package com.sctjsj.lazyhost.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.bean.CityBean;
import com.sctjsj.lazyhost.bean.DeliveryAddressBean;
import com.sctjsj.lazyhost.bean.DistrictBean;
import com.sctjsj.lazyhost.bean.GoodsBean;
import com.sctjsj.lazyhost.bean.OrderBean;
import com.sctjsj.lazyhost.bean.ProvinceBean;
import com.sctjsj.lazyhost.bean.ShopBean;
import com.sctjsj.lazyhost.constant.JumpCode;
import com.sctjsj.lazyhost.constant.OtherConstant;
import com.sctjsj.lazyhost.event.btEvent.BTStateChangeEvent;
import com.sctjsj.lazyhost.event.btEvent.ToConnectBTEvent;
import com.sctjsj.lazyhost.fragment.OrderQueryFg;
import com.sctjsj.lazyhost.fragment.ProcessOrderFg;
import com.sctjsj.lazyhost.fragment.SettingFg;
import com.sctjsj.lazyhost.fragment.ShopMagFg;

import com.sctjsj.lazyhost.receiver.BluetoothReceiver;
import com.sctjsj.lazyhost.receiver.JPushReveivedHostReceiver;
import com.sctjsj.lazyhost.receiver.MyPushReceiver;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.LogUtil;
import com.sctjsj.lazyhost.util.RingtoneUtil;
import com.sctjsj.lazyhost.util.bt.BluetoothService;
import com.sctjsj.lazyhost.widget.PopBTScanDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.sctjsj.lazyhost.constant.OtherConstant.DEVICE_NAME;
import static com.sctjsj.lazyhost.constant.OtherConstant.MESSAGE_DEVICE_NAME;
import static com.sctjsj.lazyhost.constant.OtherConstant.MESSAGE_READ;
import static com.sctjsj.lazyhost.constant.OtherConstant.MESSAGE_STATE_CHANGE;
import static com.sctjsj.lazyhost.constant.OtherConstant.MESSAGE_WRITE;
import static com.sctjsj.lazyhost.constant.OtherConstant.REQUEST_ENABLE_BT;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_CONNECTED;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_CONNECTING;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_LISTEN;
import static com.sctjsj.lazyhost.constant.OtherConstant.STATE_NONE;

public class IndexActivity extends AppCompatActivity implements MyApp.onBTStateChangedListener {
    //图标
    @Bind(R.id.activity_index_iv_process_order)
    ImageView mIVProcessOrder;
    @Bind(R.id.activity_index_iv_search_order)
    ImageView mIVSearchOrder;
    @Bind(R.id.activity_index_iv_shop_mag)
    ImageView mIVShopMag;
    @Bind(R.id.activity_index_iv_setting)
    ImageView mIVSetting;
    //文字
    @Bind(R.id.activity_index_tv_process_order)
    TextView mTVProcessOrder;
    @Bind(R.id.activity_index_tv_search_order)
    TextView mTVSearchOrder;
    @Bind(R.id.activity_index_tv_shop_mag)
    TextView mTVShopMag;
    @Bind(R.id.activity_index_tv_setting)
    TextView mTVSetting;

    //等待打印的队列
    private volatile LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

    private boolean isExited = false;//标志是否已经退出
    private MyApp app;

    private Handler mExitHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    isExited = false;
                    break;
            }
        }
    };

    /**
     * 碎片
     *
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

    private BluetoothReceiver btReceiver;
    private IntentFilter intentFilter;

    private List<BluetoothDevice> pairedList=new ArrayList<>();
    private List<BluetoothDevice> unPairedList=new ArrayList<>();
    private PopBTScanDialog popBTScanDialog =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置保持屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_index);
        ButterKnife.bind(this);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        app = (MyApp) getApplication();
        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, null);
        EventBus.getDefault().register(this);
        //检查更新
       // UpdateUtil.getInstance(this).checkVersion(BnUrl.updateUrl, 2);

        //初始化第一个碎片
        fgManager = getSupportFragmentManager();
        //默认加载首页
        if (processOrderFg == null) {
            processOrderFg = new ProcessOrderFg();
        }
        fgManager.beginTransaction().add(R.id.activity_index_rl_content, processOrderFg).show(processOrderFg).commit();


        //初始化蓝牙广播接收器
        btReceiver = new BluetoothReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_STARTED");
        intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        intentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED");
        intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");


        //初始化蓝牙
        initBT();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //页面销毁，取消注册
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (fgManager == null) {
            fgManager = getSupportFragmentManager();
        }
        //注册极光推送监听
        JPushReveivedHostReceiver.setOnGetPushMessageListener(new MyPushReceiver.OnGetPushMessageListener() {
            @Override
            public void onReceivedMessage(int type, String message) {

                //只处理透传式消息
                if (2 == type) {
                    final RingtoneUtil ringtoneUtil = new RingtoneUtil(IndexActivity.this);
                    //判断是否开启振动
                    if (app.getSpf().getBoolean("vibrateOn", true)) {
                        ringtoneUtil.vibriate();
                    }

                    /**
                     * 判断提示音类型
                     */
                    int ringType = 0;//播放的声音类型
                    //强制下线
                    if (message != null && message.contains("您的账号在其他地方登陆")) {
                        final SweetAlertDialog dialog = new SweetAlertDialog(IndexActivity.this, SweetAlertDialog.WARNING_TYPE);
                        dialog.setTitleText("登录异常");
                        dialog.setConfirmText("确认");
                        dialog.setCancelable(false);
                        dialog.setContentText("您的账号在其他地方登陆，请确认密码是否泄露。");
                        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                ((MyApp) getApplicationContext()).saveCurrentUser(null);
                                Intent intent = new Intent(IndexActivity.this, LoginActivity.class);
                                startActivity(intent);
                                dialog.dismiss();
                                finish();
                            }
                        });
                        dialog.show();
                    }

                    //新订单提醒
                    if (message != null && message.contains("您有新订单")) {
                        ringType = 1;
                        if (message.contains("rid")) {
                            try {
                                JSONObject jsonObject = new JSONObject(message);
                                int rid = jsonObject.getInt("rid");
                                if (processOrderFg != null) {
                                    //如果开启了自动打印
                                    if (app.getSpf().getBoolean("printer", true)) {
                                        processOrderFg.setPrintId(rid);
                                    }

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(IndexActivity.this, "您有新订单", Toast.LENGTH_SHORT).show();
                    }

                    //买家已收货
                    if (message != null && message.contains("买家已确认收货")) {
                        ringType = 5;
                        Toast.makeText(IndexActivity.this, "买家已确认收货", Toast.LENGTH_SHORT).show();
                    }

                    if (message != null && message.contains("配送员已接单")) {
                        ringType = 3;
                        Toast.makeText(IndexActivity.this, "配送员已接单", Toast.LENGTH_SHORT).show();
                    }

                    //配送员已送达
                    if (message != null && message.contains("配送员已送达")) {
                        ringType = 4;
                        Toast.makeText(IndexActivity.this, "配送员已送达", Toast.LENGTH_SHORT).show();
                    }

                    //支付成功
                    if (message != null && message.contains("买家支付成功")) {
                        ringType = 2;
                        Toast.makeText(IndexActivity.this, "买家支付成功", Toast.LENGTH_SHORT).show();
                    }
                    ringtoneUtil.playRing(ringType);
                }
            }
        });
    }

    /**
     * 清除导航栏所有的选中状态
     */
    private void setNavbarNotSelected() {
        //灰色字体
        int colorGray = Color.parseColor("#80000000");
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
    private void hideAllFgIfNotNull() {
        if (fgManager == null) {
            finish();
            return;
        }
        FragmentTransaction transaction = fgManager.beginTransaction();
        if (processOrderFg != null) {
            transaction.hide(processOrderFg);
        }
        if (orderQueryFg != null) {
            transaction.hide(orderQueryFg);
        }
        if (shopMagFg != null) {
            transaction.hide(shopMagFg);
        }
        if (settingFg != null) {
            transaction.hide(settingFg);
        }

        //提交
        transaction.commit();
    }

    @OnClick({R.id.activity_index_ll_process_order, R.id.activity_index_ll_search_order,
            R.id.activity_index_ll_order_shop_mag, R.id.activity_index_ll_setting})
    public void IndexActivityClick(View view) {
        int colorGreen = Color.parseColor("#FF4081");
        //1.隐藏所有碎片
        hideAllFgIfNotNull();
        //2.图片和文字设置为未选中
        setNavbarNotSelected();
        //3.设置选中导航状态
        switch (view.getId()) {
            //处理订单
            case R.id.activity_index_ll_process_order:
                try {
                    mIVProcessOrder.setImageResource(R.mipmap.icon_process_order_selected);
                    mTVProcessOrder.setTextColor(colorGreen);
                    if (processOrderFg == null) {
                        processOrderFg = new ProcessOrderFg();
                        fgManager.beginTransaction().add(R.id.activity_index_rl_content, processOrderFg).
                                show(processOrderFg).commit();
                    } else {
                        fgManager.beginTransaction().show(processOrderFg).commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            //查询订单
            case R.id.activity_index_ll_search_order:
                try {
                    mIVSearchOrder.setImageResource(R.mipmap.icon_search_order_selected);
                    mTVSearchOrder.setTextColor(colorGreen);
                    if (orderQueryFg == null) {
                        orderQueryFg = new OrderQueryFg();
                        fgManager.beginTransaction().add(R.id.activity_index_rl_content, orderQueryFg).
                                show(orderQueryFg).commit();
                    } else {
                        fgManager.beginTransaction().show(orderQueryFg).commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //店铺管理
            case R.id.activity_index_ll_order_shop_mag:
                try {
                    mIVShopMag.setImageResource(R.mipmap.icon_shop_mag_selected);
                    mTVShopMag.setTextColor(colorGreen);

                    if (shopMagFg == null) {
                        shopMagFg = new ShopMagFg();
                        fgManager.beginTransaction().add(R.id.activity_index_rl_content, shopMagFg).
                                show(shopMagFg).commit();
                    } else {
                        fgManager.beginTransaction().show(shopMagFg).commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //设置
            case R.id.activity_index_ll_setting:
                try {
                    mIVSetting.setImageResource(R.mipmap.icon_setting_selected);
                    mTVSetting.setTextColor(colorGreen);

                    if (settingFg == null) {
                        settingFg = new SettingFg();
                        fgManager.beginTransaction().add(R.id.activity_index_rl_content, settingFg).
                                show(settingFg).commit();
                    } else {
                        fgManager.beginTransaction().show(settingFg).commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    /**
     * 重写返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            //物理返回键值
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                //连续点击两次返回键退出程序
                exit();
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_HOME) {
                moveTaskToBack(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出程序
     */
    private void exit() {
        if (!isExited) {
            isExited = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            Message msg = new Message();
            msg.what = 0;
            mExitHandler.sendMessageDelayed(msg, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }


    private Handler mBTHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:

                    switch (msg.arg1) {
                        //连接成功
                        case STATE_CONNECTED:
                            //关闭连接进度
                            if (connectProgress != null) {
                                connectProgress.dismiss();
                            }
                            Toast.makeText(IndexActivity.this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();

                            break;
                        //正在连接
                        case STATE_CONNECTING:
                            if (connectProgress == null) {
                                connectProgress = new SweetAlertDialog(IndexActivity.this, SweetAlertDialog.PROGRESS_TYPE);
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
                            if (connectProgress != null) {
                                connectProgress.dismiss();
                            }
                            break;
                        case STATE_NONE:
                            Toast.makeText(IndexActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
                            if (connectProgress != null) {
                                connectProgress.dismiss();
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    playNoConnection();
                                }
                            }, 1000);

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

    private void playNoConnection() {
        try {
            MediaPlayer player = MediaPlayer.create(this, R.raw.bt_disconnect);
            if (player != null && !player.isPlaying()) {
                player.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印
     *
     * @param message
     */
    public void sendPrintMessage(BluetoothService bluetoothService, String message) {

        if (bluetoothService == null) {
            return;
        }

        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != STATE_CONNECTED) {
            Toast.makeText(this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //申请开启蓝牙结果回调
            case OtherConstant.REQUEST_ENABLE_BT:
                //表示用于已经同意开启蓝牙
                if(resultCode == RESULT_OK){
                    invokeAutoLinkDevice();
                }else {
                    //用户拒绝开启蓝牙
                    SweetAlertDialog dialog = new SweetAlertDialog(IndexActivity.this,SweetAlertDialog.WARNING_TYPE);
                    dialog.setTitleText("蓝牙开启失败");
                    dialog.setContentText("您已拒绝开启手机蓝牙，订单打印功能将无法正常使用");
                    dialog.setCancelText("取消");
                    dialog.setConfirmText("确定");
                    dialog.show();
                }
                break;
            //从蓝牙搜索页面返回
            case JumpCode.JUMP_FROM_Index_TO_BTSCANActivity:
                if (RESULT_OK == resultCode) {
                    //获取
                    String address = data.getStringExtra("device");
                    if (bluetoothAdapter == null) {
                        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    }

                    if (bluetoothService == null) {
                        bluetoothService = new BluetoothService(IndexActivity.this, mBTHandler);
                    }

                    if (bluetoothAdapter.isEnabled()) {
                        //判断是和三种设备的哪一种连接了
                        //todo 如何判断是否连接

                        //发起连接
                        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                        if (device != null) {
                            bluetoothService.connect(device);
                        }


                    }
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

        if (processOrderFg != null) {
            getSupportFragmentManager().putFragment(outState, "1", processOrderFg);
        }
        if (orderQueryFg != null) {
            getSupportFragmentManager().putFragment(outState, "2", orderQueryFg);
        }
        if (shopMagFg != null) {
            getSupportFragmentManager().putFragment(outState, "3", shopMagFg);
        }
        if (settingFg != null) {
            getSupportFragmentManager().putFragment(outState, "3", settingFg);
        }

        super.onSaveInstanceState(outState, outPersistentState);

    }

    public BluetoothService getBTService() {
        return bluetoothService;
    }

    //加入等待打印的订单
    public synchronized void joinQueue(int rid) {
        try {
            if (!queue.contains(rid)) {
                queue.put(rid);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class PrintThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (queue.size() > 0) {
                    try {
                        //取出一个rid
                        Integer rid = queue.take();
                        if (rid != null && rid.intValue() != 0) {

                            //通过订单id，查询订单信息，自动打印
                            queryOrderInfo(rid);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //通过rid查询订单信息
    private void queryOrderInfo(int id) {
        RequestParams params = new RequestParams(BnUrl.singleSearchUrl);
        params.addBodyParameter("id", String.valueOf(id));
        params.addBodyParameter("ctype", "orderform");
        params.addBodyParameter("jf", "store|goodscart|goods|addr|area|parent");
        x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(JSONObject response) {

                if (response != null) {
                    try {
                        JSONObject x = response.getJSONObject("data");
                        if (x != null) {
                            //每一条订单购买商品的集合
                            List<GoodsBean> gbList = new ArrayList<GoodsBean>();
                            String StoreOrderNo = x.getString("storeOrderNo");
                            //洗衣取送件时间
                            String distributionTime = x.getString("distributionTime");
                            String takeTime = x.getString("takeTime");

                            int typeIn = x.getInt("type");
                            //店铺名
                            String storeName = x.getJSONObject("store").getString("storeName");
                            int storeId = x.getJSONObject("store").getInt("id");
                            String storeAddress = x.getJSONObject("store").getString("storeAddress");
                            int type = x.getJSONObject("store").getInt("type");
                            ShopBean sb = new ShopBean();
                            sb.setId(storeId);
                            sb.setAddress(storeAddress);
                            sb.setName(storeName);
                            sb.setType(type);
                            //订单信息
                            int id = x.getInt("id");//订单 id
                            String name = x.getString("name");//给用户看的订单号
                            String buyerRemark = x.getString("buyerRemark");//买家备注
                            String paytime = x.getString("paytime");//下单时间
                            double shipPrice = x.getDouble("shipPrice");//运费

                            double totalPrice = x.getDouble("totalprice");//总价
                            double disValue = x.getDouble("disValue");//优惠金额
                            double packagePrice = x.getDouble("packagePrice");//包装费

                            int num = x.getInt("num");//商品总数量
                            int state = x.getInt("state");//订单状态
                            //购买的商品列表
                            JSONArray goodscart = x.getJSONArray("goodscart");
                            for (int j = 0; j < goodscart.length(); j++) {
                                JSONObject g = goodscart.getJSONObject(j);
                                int goodsId = g.getJSONObject("goods").getInt("id");//商品 id
//                                        String property = g.getJSONObject("goods").getString("name");//商品名称
//                                        String goodsName = g.getJSONObject("goods").getJSONObject("parent").getString("name");
                                int goodsNum = g.getInt("num");//商品数量
                                double price = g.getDouble("price");//单价
                                GoodsBean gb = new GoodsBean();
                                gb.setId(goodsId);

                                String goodsName = g.getJSONObject("goods").getInt("level") == 2 ? g.getJSONObject("goods").getString("name") : g.getJSONObject("goods").getString("name");
                                String specInfo = g.has("specInfo") ? g.getString("specInfo") : "";
                                gb.setName(goodsName + "    " + specInfo);
                                gb.setCount(goodsNum);
                                gb.setPrice(price);
                                gbList.add(gb);
                            }
                            DeliveryAddressBean dab = null;
                            String strAddr = x.getString("addr");
                            if (strAddr != null && !strAddr.equals("null")) {
                                //收货信息
                                JSONObject addr = new JSONObject(strAddr);

                                int deliveryId = addr.getInt("id");//收货地址 id
                                String recName = addr.getString("recName");//收货人
                                String consigneeTel = addr.getString("mobile");//收货人电话
                                String areaInfo = addr.getString("addressDetail");//详细地址

                                JSONObject area = addr.getJSONObject("area");//区
                                String districtName = area.getString("areaname");//区县名称
                                int districtId = area.getInt("id");//区县 id
                                DistrictBean districtBean = new DistrictBean();
                                districtBean.setId(districtId);
                                districtBean.setName(districtName);

                                JSONObject city = area.has("parent") ? area.getJSONObject("parent") : new JSONObject("{}");//市
                                String cityName = city.getString("areaname");//城市名
                                int cityId = city.getInt("id");//城市 id
                                CityBean cityBean = new CityBean();
                                cityBean.setId(cityId);
                                cityBean.setName(cityName);

                                JSONObject province = city.getJSONObject("parent");//省
                                String provinceName = province.getString("areaname");//省名
                                int provinceId = province.getInt("id");//省 id
                                ProvinceBean provinceBean = new ProvinceBean();
                                provinceBean.setId(provinceId);
                                provinceBean.setName(provinceName);

                                dab = new DeliveryAddressBean();
                                dab.setId(deliveryId);
                                dab.setProvinceBean(provinceBean);
                                dab.setCityBean(cityBean);
                                dab.setDistrictBean(districtBean);
                                dab.setConsignee(recName);
                                dab.setDetail(areaInfo);
                                dab.setTel(consigneeTel);
                            }

                            OrderBean ob = new OrderBean();
                            ob.setId(id);
                            ob.setName(name);
                            ob.setDeliveryAddressBean(dab);
                            ob.setState(state);

                            ob.setTotalprice(totalPrice);
                            ob.setDisvalue(disValue);
                            ob.setShopBean(sb);
                            ob.setNum(num);
                            ob.setType(typeIn);
                            ob.setBuyerRemark(buyerRemark);
                            ob.setShipPrice(shipPrice);
                            ob.setInsertTime(paytime);
                            ob.setGoodsBeanList(gbList);
                            ob.setSendTime(distributionTime);
                            ob.setTakeTime(takeTime);
                            ob.setPackagePrice(packagePrice);
                            ob.setStoreOrderNo(StoreOrderNo);

                            //自动打印
                            printOrder(1, ob);

                            //printOrder(((MyApp)context).getBluetoothService(),1,ob);
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

    private void printOrder(int count, OrderBean orderBean) {
        if (orderBean != null) {
            BluetoothService service = getBTService();
            if (service == null) {
                app.setSocketState(STATE_NONE);
                return;
            }

            for (int x = 0; x < count; x++) {
                //title
                String start = "*** 懒购外卖 ***\n\n";
                service.printCenter();
                service.printSize(1);
                sendPrintMessage(service, start);

                //订单小号
                String StoreOrderNo = orderBean.getStoreOrderNo() + "\n\n";
                service.printCenter();
                service.printSize(1);
                sendPrintMessage(service, StoreOrderNo);

                //店铺名字
                String storeName = orderBean.getShopBean().getName() + "\n\n";
                service.printCenter();
                service.printSize(1);
                sendPrintMessage(service, storeName);

                //订单号
                String orderNum = "订单号:" + orderBean.getName() + "\n\n";
                service.printLeft();
                service.printSize(0);
                sendPrintMessage(service, orderNum);

                //下单时间
                String payTime = "支付时间:" + orderBean.getInsertTime() + "\n\n";
                service.printLeft();
                service.printSize(0);
                sendPrintMessage(service, payTime);

                //循环商品信息
                List<GoodsBean> gbList = orderBean.getGoodsBeanList();
                double oriAll = 0;
                if (gbList != null && gbList.size() > 0) {
                    for (int i = 0; i < gbList.size(); i++) {
                        //商品名称
                        String goodsName = gbList.get(i).getName();
                        //购买数量
                        int goodsNum = gbList.get(i).getCount();
                        //原价
                        double goodsPrice = gbList.get(i).getPrice();
                        oriAll = oriAll + (goodsNum * goodsPrice);
                        //优惠价
                        double goodsDisPrice = gbList.get(i).getDisPrice();
                        String goods = goodsName + "    " + "*" + " " + goodsNum + "  " + goodsPrice + "\n\n";
                        service.printLeft();
                        service.printSize(0);
                        sendPrintMessage(service, goods);
                    }
                }

                //配送费
                String shipPrice = "配送费:" + orderBean.getShipPrice() + "\n\n";
                service.printLeft();
                service.printSize(0);
                sendPrintMessage(service, shipPrice);

                //原本应付的总额
                String oriTotal = "总额:" + (oriAll + orderBean.getShipPrice()) + "\n\n";
                service.printLeft();
                service.printSize(0);
                sendPrintMessage(service, oriTotal);

                //优惠信息
                String disInfo = "优惠:" + orderBean.getDisvalue() + "\n\n";
                service.printLeft();
                service.printSize(0);
                sendPrintMessage(service, disInfo);


                //总价
                String totalPay = "实付:" + orderBean.getTotalprice() + "\n\n";
                service.printLeft();
                service.printSize(2);
                sendPrintMessage(service, totalPay);

                //收货信息
                String address = orderBean.getDeliveryAddressBean().getProvinceBean().getName() +
                        orderBean.getDeliveryAddressBean().getCityBean().getName() +
                        orderBean.getDeliveryAddressBean().getDistrictBean().getName() +
                        orderBean.getDeliveryAddressBean().getDetail() + "\n\n";

                service.printLeft();
                service.printSize(2);
                sendPrintMessage(service, address);

                String consignee = "收货人:" + orderBean.getDeliveryAddressBean().getConsignee() + "\n\n";
                service.printLeft();
                service.printSize(2);
                sendPrintMessage(service, consignee);

                String tel = "联系电话:" + orderBean.getDeliveryAddressBean().getTel() + "\n\n\n\n";
                service.printLeft();
                service.printSize(2);
                sendPrintMessage(service, tel);

                //备注信息
                String remarkInfo = "备注:" + orderBean.getBuyerRemark() + "\n\n\n\n";
                service.printLeft();
                service.printSize(2);
                sendPrintMessage(service, remarkInfo);

                String end = "*** END ***\n\n\n\n\n";
                service.printCenter();
                service.printSize(1);
                sendPrintMessage(service, end);
            }

        } else {
            Toast.makeText(this, "打印信息有误", Toast.LENGTH_SHORT).show();
        }

    }

    //监测蓝牙连接状态
    private BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.e("gg", "lianjie");
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.e("gg", "meilianjie");

        }
    };

    //初始化蓝牙
    private void initBT() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //设备不支持蓝牙模块
        if(bluetoothAdapter==null){
            //在本地保存一个状态吧
            Toast.makeText(this,"当前设备不支持蓝牙连接功能",Toast.LENGTH_LONG).show();
            return;
        }

        if (bluetoothService == null) {
            bluetoothService = new BluetoothService(IndexActivity.this, mBTHandler);
        }

        //开启自动打印监测队列线程
        new PrintThread().start();

        app.setOnBTStateChangedListener(this);

        /**监测蓝牙是否开启**/
        //蓝牙未开启
        if(!bluetoothAdapter.isEnabled()){
            //打开蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            //第一次进入app，没有开启过蓝牙，先申请开启蓝牙，后面的操作等待蓝牙开启之后再执行
            return;
        }

        //蓝牙已开启，执行自动连接操作
        invokeAutoLinkDevice();
    }

    //蓝牙连接状态的改变统统回调到这里来处理
    @Override
    public void stateChanged(int currentState) {

        if(OtherConstant.STATE_NONE ==currentState){

            Log.e("gg","蓝牙是断开的");
            return;
        }

        if(OtherConstant.STATE_LISTEN ==currentState){
            Log.e("gg","等待其他人链接我");

            return;
        }


        if(OtherConstant.STATE_CONNECTING ==currentState){
            Log.e("gg","蓝牙正在连接中");

            return;
        }


        if(OtherConstant.STATE_CONNECTED ==currentState){
            Log.e("gg","蓝牙已经连上了");

            return;
        }






    }

    //自动连接蓝牙设备
    private void invokeAutoLinkDevice() {
        /**1.获取之前是否有连接过蓝牙设备**/

        //获取上一次连接的蓝牙打印机设备mac地址
        String pairedMAC =app.getSpf().getString("pairedMAC",null);
        //获取已经配对的设备列表
        Set<BluetoothDevice> pairedDeviceList = bluetoothAdapter.getBondedDevices();

        //该设备之前连接过打印机 且 查询出了已配对的设备列表 则 直接连接之前的旧设备
        if(!TextUtils.isEmpty(pairedMAC) && pairedDeviceList!=null && pairedDeviceList.size()>0){
            BluetoothDevice targetDevice =null;//连接目标的mac地址
            for(BluetoothDevice device:pairedDeviceList){
                if(TextUtils.equals(device.getAddress(),pairedMAC)){
                    targetDevice =device;
                    //跳出for 循环
                    break;
                }
            }
            //当前蓝牙未连接、目标设备不为空、目标设备已配对 -->自动连接之前的设备
            if( app.getSocketState()!= STATE_CONNECTED  && targetDevice!=null && targetDevice.getBondState()==BluetoothDevice.BOND_BONDED ){
                //其实发起蓝牙连接
                bluetoothService.connect(targetDevice);
            }

            return;
        }


        //该设备之前没有连接过蓝牙打印机，主动开始搜寻附近的设备
        //提示正在搜寻附近的蓝牙设备
        if(popBTScanDialog==null){
            popBTScanDialog = new PopBTScanDialog(IndexActivity.this);
        }

        popBTScanDialog.show();

        scan();
    }

    private void scan() {


        //不支持蓝牙
        if (bluetoothAdapter == null) {
            return;
        }
        //蓝牙未打开
        if (!bluetoothAdapter.isEnabled()) {
            return;
        }

        try {
            registerReceiver(btReceiver, intentFilter);
        } catch (Exception e) {
            LogUtil.e("开启蓝牙注册广播异常", e.toString());
        }

        //开始扫描蓝牙设备
        boolean res=bluetoothAdapter.startDiscovery();
        Log.e("gg","蓝牙扫描开启"+res);
    }

    @Subscribe
    public void onEventMainThread(BTStateChangeEvent event) {
        if (event != null) {
            Intent intent = event.getIntent();
            String action = intent.getAction();
            //扫描开始
            if ("android.bluetooth.adapter.action.DISCOVERY_STARTED".equals(action)) {

                LogUtil.e("开始扫描");

                //蓝牙开始扫描，清空数据
                pairedList.clear();
                unPairedList.clear();
                if(popBTScanDialog!=null){
                    popBTScanDialog.setScanProgress(1);
                }

            }

            //扫描结束
            if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                //取消注册监听
                try {
                    unregisterReceiver(btReceiver);
                } catch (Exception e) {
                    LogUtil.e("扫描结束异常", e.toString());
                }finally{

                    if(popBTScanDialog!=null){
                        popBTScanDialog.setDeviceList(pairedList,unPairedList);
                        popBTScanDialog.setScanProgress(2);
                    }

                }

                return;
            }

            //找到设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //获取设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    int state = device.getBondState();

                    //已配对设备
                    if (12 == state && !pairedList.contains(device)) {
                        pairedList.add(device);
                    }
                    //未配对设备
                    if (10 == state && !unPairedList.contains(device)) {
                        unPairedList.add(device);
                    }

                }

                return;
            }

            //绑定设备（配对）
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);

                switch (state) {
                    case BluetoothDevice.BOND_NONE:

                        break;
                    case BluetoothDevice.BOND_BONDING:

                        break;
                    case BluetoothDevice.BOND_BONDED:

                        break;

                }

                return;
            }


            if (action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
                Log.e("gg","android.bluetooth.device.action.ACL_DISCONNECTED");
            }

            if(action.equals("android.bluetooth.device.action.ACL_CONNECTED")){
                Log.e("gg","android.bluetooth.device.action.ACL_CONNECTED");
            }

            if(action.equals("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED")){
                Log.e("gg","android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED");
            }


        }
    }


    @Subscribe
    public void onBTEvent(ToConnectBTEvent event){
        if(event!=null){
            //连接
            if(1==event.getOp()){
                if(bluetoothService!=null && event.getDevice()!=null){
                    bluetoothService.connect(event.getDevice());
                }
            }

            //断开连接
            if(2==event.getOp()){
                if(bluetoothService!=null){
                    //bluetoothService.ca
                }
            }
        }
    }



}
