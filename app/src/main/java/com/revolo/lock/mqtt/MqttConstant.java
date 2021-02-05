package com.revolo.lock.mqtt;


/**
 * 常量
 *
 * @author FJH
 * <p>
 * created at 2019/3/27 17:09
 */
public class MqttConstant {

    //米米网参数
    public final static String APP_ID = "AIB1EITFX0DB75MCUIZR";
    public final static String PARTERN_ID = "HQQ8H3HJGJ2KPQJ7NXZY";
    public final static int DC_TEST = 6750465;


    public static final String MQTT_BASE_URL = "tcp://test1.juziwulian.com:1883";

    public static final String MQTT_REQUEST_APP = "/request/app/func";




    //断开后，是否自动连接
    public static final boolean MQTT_AUTOMATIC_RECONNECT = true;

    //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
    public static final boolean MQTT_CLEANSE_SSION = false;

    //设置超时时间，单位为秒
    public static final int MQTT_CONNECTION_TIMEOUT = 10;

    //设置心跳时间，单位为秒
    public static final int MQTT_KEEP_ALIVE_INTERVAL = 20;

    //允许同时发送几条消息（未收到broker确认信息）
    public static final int MQTT_MAX_INFLIGHT = 10;

    //msgtype---request
    public static final String MSG_TYPE_REQUEST = "request";

    //online
    public static final String ON_LINE = "online";

    //获取所有绑定的设备接口
    public static final String GET_ALL_BIND_DEVICE = "getAllBindDevice";


    public static String getSubscribeTopic(String userId) {
        String topic = "/" + userId + "/rpc/reply";
        return topic;
    }


    public static String getCallTopic(String userId) {
        return "/" + userId + "/rpc/call";
    }

    //发布给服务器消息的主题，格式为/request/app/func
    public static final String PUBLISH_TO_SERVER = "/request/app/func";
    //发布给服务器中转网关消息的主题，/clientid/rpc/call
    public static String PUBLISH_TO_GATEWAY = "/rpc/call";



    //==================================revolo============================

    //设置门磁
    public static final String SET_MAGNETIC = "setMagnetic";

    //无感开门
    public static final String APP_ROACHOPEN = "approachOpen";

    //关闭wifi
    public static final String CLOSE_WIFI = "closeWifi";

    //开关门指令
    public static final String SET_LOCK = "setLock";

    //8.秘钥属性添加
    public static final String ADD_PWD = "addPwd";

    //秘钥属性修改
    public static final String UPDATE_PWD = "updatePwd";

    //10.秘钥属性删除
    public static final String REMOVE_PWD = "removePwd";

    //3获取网关状态
    public static final String GATEWAY_STATE = "gatewayState";



}
