package com.sctjsj.lazyhost.url;

import com.sctjsj.lazyhost.BuildConfig;

/**
 * Created by mayikang on 16/12/10.
 */
//www.lp-kd.com
public class BnUrl {
    public static String ServerIp = BuildConfig.DEBUG? "http://app.ilangou.com":"http://www.ilangou.com";//www.lp-kd.com
    //登录接口
    public static String loginUrl=ServerIp+"/admin/login.htm?";
    //userName=用户名&password=icon_pwd_blue

    //退出
    public static String logoutUrl=ServerIp+"/admin/logOutToApp$ajax.htm?";

    //发送图片验证码
    public static String getPicCodeUrl=ServerIp+"/verify_output$ajax.htm?";

    //发送短信验证码
    public static String getSmsCodeUrl=ServerIp+"/verify_sms$ajax.htm?";
    //code：图片验证码 mobile：手机号

    //找回密码
    public static  String resetPwdUrl=ServerIp+"/forgetpassword$ajax.htm?";
    //mobile=电话号码&code=验证码&password=密码

    //根据店铺id查询店铺所有信息
    public static String getShopInfoById=ServerIp+"/pageSearch$ajax.htm?";
    //size=99999
    //ctype=store
    //cond={id:1}  店铺 id
    //jf=storeLogo|usergoosclass|goods1|photo|ugc
    //http://demo.sctjsj.com:30000/lpkd/pageSearch$ajax.htm?size=99999&ctype=store&cond={id:1}&jf=storeLogo|usergoosclass|goods1|photo|ugc

    //判断是否在线
    public static String isOnlineUrl=ServerIp+"/intf/isOnline$ajax.htm?";

    //http://www.lp-kd.com/user/pageSearch$ajax.htm?ctype=user&jf=store&cond={id:8}
    //查询用户信息
    public static String pullUserInfo=ServerIp+"/user/pageSearch$ajax.htm?";
    //ctype=user
    // jf=photo
    // size=999999
    // cond={id:1}

    //根据店铺 id 查询订单
    public static String pullShopOrderById=ServerIp+"/pageSearch$ajax.htm?";
    //ctype=orderform&cond={store:{id:1}}&jf=user|addr

    //修改店铺营业状态
    public static String changeShopStateUrl=ServerIp+"/user/dataModify$ajax.htm?";
    //ctype=store&data={id:1,storeStatus:1}

    //根据店铺 id 查询店铺营业状态
    public static String queryShopStateUrl=ServerIp+"/obtion_storeStatus$ajax.htm?";
    //StoreId=

    //根据店铺 id 查询店铺下的商品列表
    public static String queryGoodsListofShopUrl=ServerIp+"/pageSearch$ajax.htm?";
    //ctype=goods&cond={store:{id:1}}&jf=photo|userGoodsClass&size=1

    //上传文件
    public static String uploadFileUrl=ServerIp+"/upload/uploadImgByAccessory.htm?";

    //查询商品状态
    public static String queryGoodsStatusUrl=ServerIp+"/obtion_goods_status$ajax.htm?";
    //goodsId=商品ID

    //修改商品状态
    public static String modifyGoodsStatusUrl=ServerIp+"/user/dataModify$ajax.htm?";
    //ctype=goods1&data={id:1,status:1}

    //根据 id 查询商品详情
    public static String queryGoodsDetailByIdUrl=ServerIp+"/pageSearch$ajax.htm?";
    //ctype=goods&cond={id:1}&jf=photo|userGoodsClass

    //查询店铺下的所有自定义分类
    public static String queryAllClassifyInShop=ServerIp+"/pageSearch$ajax.htm?";
    //ctype=ugc&cond={store:{id:24},display:1}



    //添加或修改商品属性
    public static String addOrModifyGoodsUrl=ServerIp+"/user/addOrUpdateGoods$ajax.htm?";
    //good

    //添加商品分类
    public static String addNewClassifyUrl=ServerIp+"/add_usergoodsClass$ajax.htm?";
    //ClassName=李林盖饭系列 &store.id=1  添加商品分类

    //修改或删除商品分类
    public static String modifyClassifyUrl=ServerIp+"/user/dataModify$ajax.htm?";
    //ctype=ugc&data={id:分类ID,display:是否展示 1 是 2 否}

    //查询搜索固有属性
    public static String getAllTypesUrl=ServerIp+"/pageSearch$ajax.htm?";
    //ctype=goodtype

    //删除商品
    public static String deleteGoodsUrl=ServerIp+"/user/dataModify$ajax.htm?";
    //ctype=goods&data={id:商品ID,isDelete:2}

    //店家自送
   public static String deliveryBySelfUrl=ServerIp+"/user/deliveryBySelf$ajax.htm?";
    //ofId=订单ID    改变状态为已接单
    //商家派单给配送员
    public static  String sendOrderToDeliveryUrl=ServerIp+"/user/orderfromDeal$ajax.htm?";
    //ofId=订单ID

    //商家接单或者不接单
    public static String acceptOrderUrl=ServerIp+"/updateOrderStatu$ajax.htm?";
    //ofId=订单ID&statu=1:接  2:不接 3:配送员接单 4：配送员到店

    //商家派单成功后，推送消息给配送员
    public static String pushMessageToDeliveryUrl=ServerIp+"/user/pushToExpress$ajax.htm?";
    //ofId=订单ID   推送给配送端接口

    //商家处理订单后，推送给买家端
    public static String pushMessageToBuyerUrl=ServerIp+"/user/pushToBuyers$ajax.htm?";
    //ofId=订单ID&type=1:已接单 2:拒绝接单 3:配送中 4:已送达    推送消息给买家家接口

    //版本更新
    public static String updateUrl=ServerIp+"/last_version$ajax.htm?appTerminal=2";
    //appTerminal 用户端1；商家端2；配送端3

    //用户使用反馈
    public static String feedbackUrl=ServerIp+"/sendEmil$ajax.htm?";
    //content=内容&url=邮箱地址

    //统计营业额
    public static String countTurnoverUrl=ServerIp+"/user/countTurnover$ajax.htm?";

    //统计订单
    public static String countOrderUrl=ServerIp+"/user/ObtionOrderformSum$ajax.htm?";
    //=25

    //查询评论内容
    public static String pullCommentsUrl=ServerIp+"/pageSearch$ajax.htm?";
    //ctype=evaluate&jf=store|user1|photo&cond={store:{id:1}}

    //今日订单查询
    //http://www.lp-kd.com/user/queryOrderformByStore$ajax.htm? storeId=29&pageSize=8&pageIndex=1
    public static String queryTodayOrder=ServerIp+"/user/queryOrderformByStore$ajax.htm?";
    //storeId=1

    //检查二维码是否可以核销
    public static String checkQRCode=ServerIp+"/user/isQRcodeAvailable$ajax.htm?";
    //code=172331158702&jf=goods|store

    //核销二维码接口
    public static String writeOffQRUrl=ServerIp+"/user/QRcodeOff$ajax.htm?";
    //code=二维码的code

    //查询商家相册
    public static String getGalleryListUrl=ServerIp+"/pageSearch$ajax.htm?";
    //ctype=store&cond={id:***}&jf=photo|accessory查相册

    //修改店铺 gallery
    public static String modifyGalleryUrl=ServerIp+"/user/addStoreGallery$ajax.htm?";
    //acyId=照片ID&storeId=店铺ID

    //删除店铺相册图片
    public static String deleteGalleryUrl=ServerIp+"/user/delStoreGallery$ajax.htm?";
    //acyId=照片

    //发布店家公告
     public static String releaseAnnounceUrl=ServerIp+"/user/dataSave$ajax.htm?";
    //ctype=sp&content=内容&state=1（展示）2（不展示）&storeId={id:店铺ID}

    //修改店家公告
    public static String modifyAnnouceUrl=ServerIp+"/user/dataModify$ajax.htm?";
    //ctype=sp&content=内容&state=1（展示）2（不展示）&storeId={id:店铺ID}

    //查询店家公告
    public static String getAnnouceUrl=ServerIp+"/pageSearch$ajax.htm?";
    //http://www.lp-kd.com/pageSearch$ajax.htm?ctype=sp&cond={stata:1,storeId:{id:24}}

    //通过订单 ID 查询买家信息
    public static String getBuyerByOrderId=ServerIp+"/user/ObtionUserByofId$ajax.htm?";
    //ofId

    //通用修改
    public static String dataModifyUrl=ServerIp+"/user/dataModify$ajax.htm?";

    //根据店铺 id 来查询店铺下 的配送员
    //http://www.lp-kd.com/user/pageSearch$ajax.htm?ctype=user&cond={shipInStore:{id:20},isDelete:0,isLocked:0}&jf=photo|area|shipInStore
    public static String getDeliveryByStoreUrl=ServerIp+"/store/obtain/store_rider$ajax.htm";//user/pageSearch$ajax.htm
    //ctype=user&cond={shipInStore:{id:20},isDelete:0,isLocked:0}&jf=photo|area|shipInStore

    //店家根据配送员 id 分配给指定配送员
    public static String sendToShopDeliveryByIdUrl=ServerIp+"/store_AppointShipUser$ajax.htm";
    //orderId=订单iD&userId=配送员ID

    public static String getSendToShopDeliveryByPlatformUrl = ServerIp+"/store/system/send$ajax.htm";
    //orderId=订单Id
    //提现

    public static String depositUrl = ServerIp + "/user/encashment/add.htm";

    //修改商家自动接单状态
    public static String modifyAutoReceiveOrder = ServerIp + "/user/modify/store$ajax.htm";

    //查询全部订单
    public static String getAllOrderUrl = ServerIp + "/obtain/storeOrder$ajax.htm";

    public static String singleSearchUrl = ServerIp +"/singleSearch$ajax.htm";

}
