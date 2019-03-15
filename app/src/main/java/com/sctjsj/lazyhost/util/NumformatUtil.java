package com.sctjsj.lazyhost.util;

import java.text.DecimalFormat;

/**
 * Created by mayikang on 17/1/20.
 */

/**
 * 数字格式处理
 */
public class NumformatUtil {

    /**
     * 保留两位小数
     */
    public  static double save2(Double ori){
        DecimalFormat    df   = new DecimalFormat("#0.00");
        String x=df.format(ori);
        return  Double.valueOf(x);
    }




}
