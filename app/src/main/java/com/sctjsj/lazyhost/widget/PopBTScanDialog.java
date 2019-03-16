package com.sctjsj.lazyhost.widget;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.sctjsj.lazyhost.R;
import com.sctjsj.lazyhost.bean.NotPairedEntity;
import com.sctjsj.lazyhost.bean.PairedEntity;
import com.sctjsj.lazyhost.multype.Items;
import com.sctjsj.lazyhost.multype.MultiTypeAdapter;
import com.sctjsj.lazyhost.multype.MultiTypeViewBinder;
import com.sctjsj.lazyhost.multype.ViewHolder;
import com.sctjsj.lazyhost.receiver.BluetoothReceiver;
import com.sctjsj.lazyhost.util.DimenUtil;


/**
 * Author:chris - jason
 * Date:2019/3/16.
 * Package:com.sctjsj.lazyhost.widget
 * 用于非蓝牙搜索页面的蓝牙搜索功能
 */
public class PopBTScanDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private TextView mTVCancel;
    private RecyclerView mRV;
    private Items data;
    private MultiTypeAdapter adapter;




    public PopBTScanDialog(@NonNull Context context) {
        super(context, R.style.full_width_dialog);
        this.mContext = context;
        //设置不可取消
        setCancelable(true);
        setCanceledOnTouchOutside(false);

        initAttr();
    }

    //初始化属性
    private void initAttr() {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View rootView = inflater.inflate(R.layout.pop_bt_scan_dialog_layout, null);

        //set view
        setContentView(rootView);

        setCancelable(false);//点击外部区域可取消
        setCanceledOnTouchOutside(false);//点击返回键可取消
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);//显示在底部

        //设置宽高
        WindowManager.LayoutParams params = window.getAttributes();

        Display display = getWindow().getWindowManager().getDefaultDisplay();

        params.width = display.getWidth();
        //设置高度为屏幕高度的80%
        params.height = DimenUtil.getScreenHeight(mContext) * 8 / 10;
        window.setAttributes(params);

        //初始化控件
        bindView(rootView);
    }

    //绑定控件
    private void bindView(View root) {
        mTVCancel = root.findViewById(R.id.tv_cancel);
        mTVCancel.setOnClickListener(this);
        mRV = root.findViewById(R.id.rv);

        initRV();

    }

    private void initRV() {

        data = new Items();
        data.add(new PairedEntity());

        adapter = new MultiTypeAdapter(data);

        MultiTypeViewBinder<NotPairedEntity> binder_notPaired = new MultiTypeViewBinder<NotPairedEntity>(mContext, R.layout.item_not_paired_layout) {
            @Override
            protected void convert(ViewHolder holder, NotPairedEntity notPairedEntity, int position) {

            }
        };

        MultiTypeViewBinder<PairedEntity> binder_paired = new MultiTypeViewBinder<PairedEntity>(mContext, R.layout.item_paired_layout) {
            @Override
            protected void convert(ViewHolder holder, PairedEntity pairedEntity, int position) {

            }
        };

        MultiTypeViewBinder<BluetoothDevice> binder_device = new MultiTypeViewBinder<BluetoothDevice>(mContext, R.layout.item_bt_device_layout) {
            @Override
            protected void convert(ViewHolder holder, BluetoothDevice bluetoothDevice, int position) {

            }
        };

        adapter.register(PairedEntity.class, binder_paired);
        adapter.register(NotPairedEntity.class, binder_notPaired);
        adapter.register(BluetoothDevice.class, binder_device);
        mRV.setLayoutManager(new LinearLayoutManager(mContext));
        mRV.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
        }
    }





}
