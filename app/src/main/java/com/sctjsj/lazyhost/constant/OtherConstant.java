package com.sctjsj.lazyhost.constant;

/**
 * Created by mayikang on 16/12/16.
 */

public class OtherConstant {

    public static final int TOKEN_FLUSH_GET_PENDING_INTENT=101;

    public static final int REQUEST_CAMERA_AUTHORITY=102;
    //请求打开蓝牙
    public static final int REQUEST_ENABLE_BT=103;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 6;       // 初始状态
    public static final int STATE_LISTEN = 7;     //  等待其他设备连接我
    public static final int STATE_CONNECTING = 8; // 正在连接其他设备
    public static final int STATE_CONNECTED = 9;  // 连接成功
    public static final int STATE_DISCONNECTED =10;//蓝牙断开连接
    public static final int STATE_CONNECT_FAILED=11;//蓝牙连接失败


}
