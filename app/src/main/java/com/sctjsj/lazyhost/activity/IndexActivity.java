package com.sctjsj.lazyhost.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import com.sctjsj.lazyhost.fragment.OrderQueryFg;
import com.sctjsj.lazyhost.fragment.ProcessOrderFg;
import com.sctjsj.lazyhost.fragment.SettingFg;
import com.sctjsj.lazyhost.fragment.ShopMagFg;

import com.sctjsj.lazyhost.receiver.JPushReveivedHostReceiver;
import com.sctjsj.lazyhost.receiver.MyPushReceiver;
import com.sctjsj.lazyhost.url.BnUrl;
import com.sctjsj.lazyhost.util.RingtoneUtil;
import com.sctjsj.lazyhost.util.UpdateUtil;
import com.sctjsj.lazyhost.util.bt.BluetoothService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
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

public class IndexActivity extends AppCompatActivity {
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

    private String TAG = "IndexActivity";
    private boolean isExited = false;//标志是否已经退出
    private MyApp app;
    private RefreshDeliveryStateBroadcastReceiver receiver;

    private Handler mHandler = new Handler() {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置保持屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_index);
        ButterKnife.bind(this);
        int flag = getIntent().getFlags();
        //首次启动 Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT 为 0
        //再次点击图标启动时就不为零了
        System.out.println("getFlags---" + flag + "------" + (flag & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, null);
        initReceiver();
        //注册个推
        app = (MyApp) getApplication();

        UpdateUtil.getInstance(this).checkVersion(BnUrl.updateUrl, 2);

        fgManager = getSupportFragmentManager();
        //默认加载首页
        if (processOrderFg == null) {
            processOrderFg = new ProcessOrderFg();
        }
        fgManager.beginTransaction().add(R.id.activity_index_rl_content, processOrderFg).show(processOrderFg).commit();
//        }


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //延迟1秒执行自动连接
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //如果设置了自动打印，并且没有连接蓝牙，启动自动扫描
                if (app.getSocketState() != STATE_CONNECTED && app.getSpf().getBoolean("printer", false)) {
                    Intent intent = new Intent(IndexActivity.this, BTScanActivity.class);
                    startActivityForResult(intent, JumpCode.JUMP_FROM_Index_TO_BTSCANActivity);
                }
            }
        }, 1000);

        new PrintThread().start();

    }

    @Override
    protected void onDestroy() {
        Log.e("TAG", "页面销毁");
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    private void initReceiver() {
        receiver = new RefreshDeliveryStateBroadcastReceiver();
        IntentFilter filter = new IntentFilter("ACTION_REFRESH");
        registerReceiver(receiver, filter);
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
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            Message msg = new Message();
            msg.what = 0;
            mHandler.sendMessageDelayed(msg, 2000);
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

        //请求开启蓝牙
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            //打开蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        if (bluetoothService == null) {
            bluetoothService = new BluetoothService(IndexActivity.this, mxHandler);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case JumpCode.JUMP_FROM_Index_TO_BTSCANActivity:
                if (RESULT_OK == resultCode) {
                    //获取
                    String address = data.getStringExtra("device");
                    if (bluetoothAdapter == null) {
                        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    }

                    if (bluetoothService == null) {
                        bluetoothService = new BluetoothService(IndexActivity.this, mxHandler);
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

        //private ProcessOrderFg processOrderFg;
        //private OrderQueryFg orderQueryFg;
        //private ShopMagFg shopMagFg;
        //private SettingFg settingFg;

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

    class RefreshDeliveryStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("ACTION_REFRESH_HOST")) {
                if (processOrderFg != null) {
                    processOrderFg.initData();
                }
            }
        }
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


}
