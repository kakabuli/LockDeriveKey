package com.revolo.lock.mqtt;


/**
 * 常量
 *
 * @author FJH
 * <p>
 * created at 2019/3/27 17:09
 */
public class MQttConstant {

    private MQttConstant() {
    }

    public static final String MQTT_ALPHA = "tcp://mqtt.irevolohome.com:1883";      // alpha 生产服务器
    public static final String MQTT_ABROAD_URL = "tcp://revolotest.sfeiya.com:1883";     // 海外服务器
    public static final String MQTT_TEST_URL = "tcp://internal.irevolo.com:1883";   // 国内测试服务器
    public static final String MQTT_CHANGSHA_TEST_URL_248 = "tcp://192.168.118.248:1883";
    public static final String MQTT_CHANGSHA_TEST_URL_249 = "tcp://192.168.118.249:1883";
    public static final String MQTT_BASE_URL = MQTT_CHANGSHA_TEST_URL_249;

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
        return "/" + userId + "/rpc/reply";
    }

    public static String getCallTopic(String userId) {
        return "/" + userId + "/rpc/call";
    }

    //发布给服务器消息的主题，格式为/request/app/func
    public static final String PUBLISH_TO_SERVER = "/request/app/func";
    //发布给服务器中转网关消息的主题，/clientid/rpc/call
    public static String PUBLISH_TO_GATEWAY = "/rpc/call";
    public static final String PUBLISH_GET_RANDOM_CODE_TOPIC = "orangeiot/sn/encreport";

    //==================================revolo============================

    // 设置门磁
    public static final String SET_MAGNETIC = "setMagnetic";

    // 无感开门
    public static final String APP_ROACH_OPEN = "approachOpen";

    // 关闭wifi
    public static final String CLOSE_WIFI = "closeWifi";

    // 开关门指令
    public static final String SET_LOCK = "setLock";

    public static final String CREATE_PWD = "createPwd";

    // 秘钥属性添加
    public static final String ADD_PWD = "addPwd";

    // 秘钥属性修改
    public static final String UPDATE_PWD = "updatePwd";

    // 秘钥属性删除
    public static final String REMOVE_PWD = "removePwd";

    // 获取网关状态
    public static final String GATEWAY_STATE = "gatewayState";

    // 设置门锁属性
    public static final String SET_LOCK_ATTR = "setLockAttr";

    // 操作事件
    public static final String WF_EVENT = "wfevent";

    // 记录
    public static final String RECORD = "record";

}
