package com.sctjsj.lazyhost.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.application.MyApp;
import com.sctjsj.lazyhost.event.btScanEvent.BTStateChangeEvent;
import com.sctjsj.lazyhost.receiver.BluetoothReceiver;
import com.sctjsj.lazyhost.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * 蓝牙扫描连接页面
 */
public class BTScanActivity extends AppCompatActivity {
    private MyApp app;
    /**
     * 常量
     */
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    //扫描进度
    private SweetAlertDialog scanProgress;
    private SweetAlertDialog bindProgress;
    private BluetoothReceiver bluetoothReceiver;
    private BluetoothAdapter bluetoothAdapter;

    //数据
    private List<BluetoothDevice> extraDevice = new ArrayList<>();
    private List<BluetoothDevice> linkedDevice = new ArrayList<>();

    //Filter
//    private IntentFilter stateFilter;
//    private IntentFilter foundFilter;
//    private IntentFilter bindFilter;
//    private IntentFilter startDiscoveryFilter;
//    private IntentFilter finishDiscoveryFilter;

    //适配器
    private LinkedAdapter linkedAdapter;
    private ExtraAdapter extraAdapter;
    private IntentFilter intentFilter = new IntentFilter();
    //Listview
    @Bind(R.id.lv_extra_device_list)
    ListView mLVExtra;
    @Bind(R.id.lv_linked_device_list)
    ListView mLVLinked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btscan);
        ButterKnife.bind(this);
        StatusBarUtil.setTranslucentForImageViewInFragment(this,0,null);
        //注册事件总线
        EventBus.getDefault().register(this);
        /**
         * 注册蓝牙适配器状态变化监听器
         */
        bluetoothReceiver = new BluetoothReceiver();

        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_STARTED");
        intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        intentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED");
        intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");

        //registerReceiver(bluetoothReceiver,intentFilter);

        linkedAdapter = new LinkedAdapter();
        mLVLinked.setAdapter(linkedAdapter);

        //配对设备去连接
        mLVLinked.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BluetoothDevice device =linkedDevice.get(position);

                //address
                String address = linkedDevice.get(position).getAddress();
                Intent intent = new Intent(BTScanActivity.this, IndexActivity.class);
                intent.putExtra("device", address);
                setResult(RESULT_OK, intent);

                finish();
            }
        });

        //长按已配对的设备删除配对
        mLVLinked.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                final SweetAlertDialog dialog = new SweetAlertDialog(BTScanActivity.this, SweetAlertDialog.WARNING_TYPE);
                dialog.setTitleText("");
                dialog.setContentText("取消和该设备的配对？");
                dialog.setConfirmText("确认");
                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String address = extraDevice.get(position).getAddress();
                        if (bluetoothAdapter == null) {
                            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        }
                        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                        if (device != null) {
                            Log.e("gg", "device!=null");
                            boolean result = removeBond(device);
                            if (result) {
                                Log.e("gg", "device移除成功");
                                gotoScan();
                            }
                        } else {
                            Log.e("gg", "device=null");
                        }
                        dialog.dismiss();
                    }
                });
                dialog.show();

                return true;
            }
        });

        //未配对设备去配对
        mLVExtra.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {


                final SweetAlertDialog dialog = new SweetAlertDialog(BTScanActivity.this, SweetAlertDialog.WARNING_TYPE);
                dialog.setTitleText("");
                dialog.setContentText("确认和该设备的配对？");
                dialog.setConfirmText("确认");
                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String address = extraDevice.get(position).getAddress();
                        if (bluetoothAdapter == null) {
                            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        }
                        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                        boolean result = device.createBond();

                        Toast.makeText(BTScanActivity.this, "创建蓝牙配对" + (result == true ? "成功" : "失败"), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });

        extraAdapter = new ExtraAdapter();
        mLVExtra.setAdapter(extraAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        gotoScan();
    }

    @Override
    protected void onDestroy() {
        //注销广播
        try {
            this.unregisterReceiver(bluetoothReceiver);
        } catch (Exception e) {
            LogUtil.e("destroy 异常", e.toString());
        }

        ButterKnife.unbind(this);
        bluetoothAdapter.cancelDiscovery();
        super.onDestroy();
    }

    @OnClick({R.id.act_bt_scan_back, R.id.act_bt_scan_rv_scan_device})
    public void btScanClick(View v) {
        switch (v.getId()) {
            case R.id.act_bt_scan_back:
                finish();
                break;
            case R.id.act_bt_scan_rv_scan_device:
                Toast.makeText(this, "主动扫描", Toast.LENGTH_SHORT).show();
                gotoScan();
                break;

        }
    }

    /**
     * 回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (RESULT_OK == resultCode) {
                    gotoScan();
                }
                break;
        }
    }

    /**
     * 扫描蓝牙设备
     */
    private void gotoScan() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //判断是否支持蓝牙
        if (bluetoothAdapter == null) {
            SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
            dialog.setTitleText("错误");
            dialog.setContentText("当前设备不支持蓝牙功能");
            dialog.setConfirmText("确认");
            dialog.show();
            return;
        }

        //判断蓝牙是否开启
        if (!bluetoothAdapter.isEnabled()) {
            //申请去开启蓝牙，回调处理
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);
            return;
        }

        try {
            registerReceiver(bluetoothReceiver, intentFilter);
        } catch (Exception e) {
            LogUtil.e("开启蓝牙注册广播异常", e.toString());
        }
        //开启扫描设备
        boolean res = bluetoothAdapter.startDiscovery();
        LogUtil.e("扫描开启结果", res + "");

    }

    /**
     * 利用反射取消蓝牙绑定
     *
     * @param device
     * @return
     */
    private boolean removeBond(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        Class btDeviceCls = BluetoothDevice.class;
        Method removeBond = null;
        try {

            removeBond = btDeviceCls.getMethod("removeBond");

            removeBond.setAccessible(true);
            boolean result = (boolean) removeBond.invoke(device);
            Log.e("gg", "调用结果" + result);
            return result;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * 适配器
     */
    class LinkedAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return linkedDevice.size();
        }

        @Override
        public Object getItem(int position) {
            return linkedDevice.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinkedHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(BTScanActivity.this).inflate(R.layout.item_bluetooth_device_list, null);
                holder = new LinkedHolder();

                holder.mTVName = (TextView) convertView.findViewById(R.id.item_bluetooth_device_list_tv_name);
                holder.mTVMac = (TextView) convertView.findViewById(R.id.item_bluetooth_device_list_tv_mac);
                holder.mTVState = (TextView) convertView.findViewById(R.id.item_bluetooth_device_list_tv_state);

                convertView.setTag(holder);
            } else {
                holder = (LinkedHolder) convertView.getTag();
            }

            holder.mTVName.setText("设备名：" + linkedDevice.get(position).getName());
            holder.mTVMac.setText("MAC：" + linkedDevice.get(position).getAddress());

            if (BluetoothDevice.BOND_BONDED == linkedDevice.get(position).getBondState()) {
                holder.mTVState.setText("已配对");
            }

            if (BluetoothDevice.BOND_NONE == linkedDevice.get(position).getBondState()) {
                holder.mTVState.setText("未配对");
            }

            return convertView;
        }

        class LinkedHolder {
            TextView mTVName, mTVMac, mTVState;
        }

    }

    class ExtraAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return extraDevice.size();
        }

        @Override
        public Object getItem(int position) {
            return extraDevice.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinkedHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(BTScanActivity.this).inflate(R.layout.item_bluetooth_device_list, null);
                holder = new LinkedHolder();

                holder.mTVName = (TextView) convertView.findViewById(R.id.item_bluetooth_device_list_tv_name);
                holder.mTVMac = (TextView) convertView.findViewById(R.id.item_bluetooth_device_list_tv_mac);
                holder.mTVState = (TextView) convertView.findViewById(R.id.item_bluetooth_device_list_tv_state);

                convertView.setTag(holder);
            } else {
                holder = (LinkedHolder) convertView.getTag();
            }

            holder.mTVName.setText("设备名：" + extraDevice.get(position).getName());
            holder.mTVMac.setText("MAC：" + extraDevice.get(position).getAddress());

            if (BluetoothDevice.BOND_BONDED == extraDevice.get(position).getBondState()) {
                holder.mTVState.setText("已配对");
            }

            if (BluetoothDevice.BOND_NONE == extraDevice.get(position).getBondState()) {
                holder.mTVState.setText("未配对");
            }

            return convertView;
        }

        class LinkedHolder {
            TextView mTVName, mTVMac, mTVState;
        }

    }


    /**
     * 订阅消息
     *
     * @param event
     */
    @Subscribe
    public void onEventMainThread(BTStateChangeEvent event) {
        if (event != null) {
            Intent intent = event.getIntent();
            String action = intent.getAction();
            //扫描开始
            if ("android.bluetooth.adapter.action.DISCOVERY_STARTED".equals(action)) {
                LogUtil.e("开始扫描");
                //清除原来的数据
                linkedDevice.clear();
                extraDevice.clear();
                //扫描进度
                if (scanProgress == null) {
                    scanProgress = new SweetAlertDialog(BTScanActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                }

                scanProgress.setTitleText("正在扫描可用设备");
                scanProgress.setCancelText("取消扫描");
                scanProgress.setContentText("正在扫描中");
                scanProgress.setCancelable(false);
                scanProgress.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        scanProgress.dismiss();
                        bluetoothAdapter.cancelDiscovery();
                    }
                });
                scanProgress.show();
            }
            //扫描结束
            if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                LogUtil.e("结束扫描");
                if (scanProgress != null) {
                    scanProgress.dismiss();
                }

                bluetoothAdapter.cancelDiscovery();

                linkedAdapter.notifyDataSetChanged();
                extraAdapter.notifyDataSetChanged();
                try {
                    this.unregisterReceiver(bluetoothReceiver);
                } catch (Exception e) {
                    LogUtil.e("扫描结束异常", e.toString());
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
                    if (12 == state) {
                        if (!linkedDevice.contains(device)) {
                            linkedDevice.add(device);
                        }
                    }
                    //未配对设备
                    if (10 == state) {
                        if (!extraDevice.contains(device)) {
                            extraDevice.add(device);
                        }
                    }

                }

                return;
            }
            //绑定设备
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                LogUtil.e("绑定设备");
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                if (bindProgress == null) {
                    bindProgress = new SweetAlertDialog(BTScanActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                }
                switch (state) {
                    case BluetoothDevice.BOND_NONE:
                        bindProgress.dismiss();
                        Toast.makeText(BTScanActivity.this, "删除配对", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        bindProgress.setContentText("正在配对中");
                        bindProgress.setTitleText("");
                        bindProgress.show();
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        bindProgress.dismiss();
                        Toast.makeText(BTScanActivity.this, "配对成功", Toast.LENGTH_SHORT).show();
                        //重新扫描设备
                        gotoScan();
                        break;

                }

                return;
            }


            if(action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")){

            }

        }
    }

}

