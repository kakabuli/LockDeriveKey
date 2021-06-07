package com.revolo.lock.manager.mqtt;

/**
 * 用来标注MQTT模块中报错code及原因
 */
public class MQTTErrCode {


    public static final int MQTT_USER_ERR=0;//用户信息错误
    /**
     * token==null
     */
    public static final int MQTT_USER_USETID_NULL_CODE=1;
    /**
     * token==null
     */
    public static final int MQTT_USER_TOKEN_NULL_CODE=2;
    /**
     * 已连接
     */
    public static final int MQTT_CONNECTION_CODE=3;
    /**
     * 获取当前连接状态失败
     */
    public static final int MQTT_CONNECTION_GET_ERR_CODE=4;
    /**
     * 连接丢失
     */
    public static final int MQTT_CONNECTION_LOST_CODE=5;
    /**
     * MQTT连接异常
     */
    public static final int MQTT_CONNECTED_ERR=6;

}
