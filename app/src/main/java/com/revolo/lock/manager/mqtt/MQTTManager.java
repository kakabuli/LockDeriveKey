package com.revolo.lock.manager.mqtt;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.revolo.lock.App;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttExceptionHandle;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import timber.log.Timber;

import static com.revolo.lock.manager.mqtt.MQTTErrCode.MQTT_CONNECTED_ERR;
import static com.revolo.lock.manager.mqtt.MQTTErrCode.MQTT_CONNECTION_LOST_CODE;

/**
 * MQTT 协议管理
 */
public class MQTTManager {
    private MQTTDataLinstener mqttDataLinstener;
    private static MQTTManager mqttManager;

    public static MQTTManager getInstance() {
        if (null == mqttManager) {
            mqttManager = new MQTTManager();
        }
        return mqttManager;
    }

    public void setMqttDataLinstener(MQTTDataLinstener mqttDataLinstener) {
        this.mqttDataLinstener = mqttDataLinstener;
    }

    //请勿添加static
    private MqttAndroidClient mqttClient;
    //重连次数10
    public int reconnectionNum = 10;
    private String userId;
    private String token;

    /**
     * mqtt连接参数设置
     *
     * @param user_id
     * @param user_token
     * @return
     */
    private MqttConnectOptions connectOption(String user_id, String user_token) {
        //连接
        MqttConnectOptions connOpts = new MqttConnectOptions();
        //断开后，是否自动连接
        connOpts.setAutomaticReconnect(true);
        //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
        connOpts.setCleanSession(MQttConstant.MQTT_CLEANSE_SSION);
        //设置超时时间，单位为秒 10
        connOpts.setConnectionTimeout(MQttConstant.MQTT_CONNECTION_TIMEOUT);
        //设置心跳时间，单位为秒 20
        connOpts.setKeepAliveInterval(MQttConstant.MQTT_KEEP_ALIVE_INTERVAL);
        //允许同时发送几条消息（未收到broker确认信息）
        connOpts.setMaxInflight(MQttConstant.MQTT_MAX_INFLIGHT);
        //用户的id,和token
        if (!TextUtils.isEmpty(user_id) && !TextUtils.isEmpty(user_token)) {
            connOpts.setUserName(user_id);
            connOpts.setPassword(user_token.toCharArray());
            Timber.d("Mqtt设置token" + user_token + "     connopt" + connOpts.getPassword());
        }
        return connOpts;
    }

    /**
     * 连接
     */
    public void mqttConnection() {
        Timber.e("mqttConnection");
        if (null != mqttClient) {
            if (mqttClient.isConnected()) {
                Timber.d("mqttConnection  mqtt已连接");
                if (null != mqttDataLinstener) {
                    mqttDataLinstener.MQTTException(MQTTErrCode.MQTT_CONNECTION_CODE);
                }
                return;
            }
        }
        if (App.getInstance().getUserBean() == null) {
            if (null != mqttDataLinstener) {
                mqttDataLinstener.MQTTException(MQTTErrCode.MQTT_USER_ERR);
            }
            return;
        }
        userId = App.getInstance().getUserBean().getUid();
        token = App.getInstance().getUserBean().getToken();
        Timber.d("userId-->" + userId + " ----token--->" + token);
        //TODO: 2019/4/25  此处为空   应该重新读取一下本地文件，延时100ms吧，如果再读取不到？直接退出   mqtt不能不登录的  不登录  这个APP就废了
        if (TextUtils.isEmpty(userId)) {
            if (null != mqttDataLinstener) {
                mqttDataLinstener.MQTTException(MQTTErrCode.MQTT_USER_USETID_NULL_CODE);
            }
            return;
        }
        if (TextUtils.isEmpty(token)) {
            if (null != mqttDataLinstener) {
                mqttDataLinstener.MQTTException(MQTTErrCode.MQTT_USER_TOKEN_NULL_CODE);
            }
            return;
        }
        if (mqttClient == null) {
            mqttClient = new MqttAndroidClient(App.getInstance(), MQttConstant.MQTT_BASE_URL, "app:" + userId);
        }
        //已经连接
        try {
            if (mqttClient.isConnected()) {
                Timber.d("mqttConnection  mqtt已连接");
                if (null != mqttDataLinstener) {
                    mqttDataLinstener.MQTTException(MQTTErrCode.MQTT_CONNECTION_CODE);
                }
                return;
            }
        } catch (Exception e) {
            Timber.e("获取客户端连接状况失败   " + e.getMessage());
            if (null != mqttDataLinstener) {
                mqttDataLinstener.MQTTException(MQTTErrCode.MQTT_CONNECTION_GET_ERR_CODE);
            }
            return;
        }
        MqttConnectOptions mqttConnectOptions = connectOption(userId, token);
        //设置回调
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                //连接完成
                Timber.d("mqtt 连接完成");
                DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                disconnectedBufferOptions.setBufferEnabled(true);
                disconnectedBufferOptions.setBufferSize(100);
                disconnectedBufferOptions.setPersistBuffer(false);
                disconnectedBufferOptions.setDeleteOldestMessages(false);
                if (mqttClient != null) {
                    if (disconnectedBufferOptions != null) {
                        mqttClient.setBufferOpts(disconnectedBufferOptions);
                    }
                    //连接成功之后订阅主题
                    mqttSubscribe(mqttClient, MQttConstant.getSubscribeTopic(userId), 2);

                    Log.e("topic:", MQttConstant.getSubscribeTopic(userId));
                    reconnectionNum = 10;
                }
                if (null != mqttDataLinstener) {
                    mqttDataLinstener.connectComplete(reconnect, serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                //连接丢失
                if (null == cause) {
                    if (null != mqttDataLinstener) {
                        mqttDataLinstener.MQTTException(MQTT_CONNECTION_LOST_CODE);
                    }
                    return;
                }
//                //连接丢失--需要进行重连
                Timber.d("connectionLost 连接丢失需要重连");
                String userId = App.getInstance().getUserBean().getUid();
                String userToken = App.getInstance().getUserBean().getUid();
                Timber.d(userId + "用户id" + "用户tonken" + userToken);
                if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userToken)) {
                    Timber.d("connectionLost 用户id或者token为空无法重连");
                    return;
                }
                if (null != mqttDataLinstener) {
                    mqttDataLinstener.connectionLost(cause);
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (null != mqttDataLinstener) {
                    mqttDataLinstener.messageArrived(topic, message);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //交互完成
                if (null != mqttDataLinstener) {
                    mqttDataLinstener.deliveryComplete(token);
                }
            }
        });
        try {
            mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Timber.d("mqtt连接成功");
                    if (null != mqttDataLinstener) {
                        mqttDataLinstener.onSuccess(asyncActionToken);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    if (null != mqttDataLinstener) {
                        mqttDataLinstener.onFailure(asyncActionToken, exception);
                    }
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            if (null != mqttDataLinstener) {
                mqttDataLinstener.MQTTException(MQTT_CONNECTED_ERR);
            }
        }
        //设置MQTT数据回复
    }

    //订阅
    public void mqttSubscribe(MqttAndroidClient mqttClient, String topic, int qos) {
        Timber.d("订阅    " + topic + "   " + (mqttClient != null));
        try {
            if (mqttClient != null) {
                if (!TextUtils.isEmpty(topic) && mqttClient.isConnected()) {
                    mqttClient.subscribe(topic, qos, null, new
                            IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Timber.d("mqttSubscribe " + "订阅成功");
                                    //TODO:订阅成功，立即拿设备列表,此时拿设备列表，从mqtt转成以http方式

                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    Timber.d("mqttSubscribe " + "订阅失败");
                                    MqttExceptionHandle.onFail(MqttExceptionHandle.SubscribeException, asyncActionToken, exception);
                                }
                            });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void mqttSubscribe(String topic, int qos) {
        Timber.d("订阅    " + topic + "   " + (mqttClient != null));
        try {
            if (mqttClient != null) {
                if (!TextUtils.isEmpty(topic) && mqttClient.isConnected()) {
                    mqttClient.subscribe(topic, qos, null, new
                            IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Timber.d("mqttSubscribe " + "订阅成功");
                                    //TODO:订阅成功，立即拿设备列表,此时拿设备列表，从mqtt转成以http方式

                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    Timber.d("mqttSubscribe " + "订阅失败");
                                    MqttExceptionHandle.onFail(MqttExceptionHandle.SubscribeException, asyncActionToken, exception);
                                }
                            });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //发布
    public void mqttPublish(String topic, MqttMessage mqttMessage) throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            LogUtils.e("发布mqtt消息 " + "topic: " + topic + "  mqttMessage: " + mqttMessage.toString() + "qos = " + mqttMessage.getQos());
            mqttClient.publish(topic, mqttMessage, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    LogUtils.e("发布消息成功  ", topic + "  消息Id  " + mqttMessage.getId());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    LogUtils.e("发布消息失败 " + topic + "    fail");
                    MqttExceptionHandle.onFail(MqttExceptionHandle.PublishException, asyncActionToken, exception);
                }
            });
        } else {
            mqttConnection();
        }
    }

    //mqtt先断开，后退出http
    public void mqttDisconnect() {
        String token = App.getInstance().getUserBean().getUid();
        reconnectionNum = 10;
        if (mqttClient == null) {
            Timber.d("mqttClient为空");
            return;
        }
        if (TextUtils.isEmpty(token)) {
            return;
        }

        if (mqttClient.isConnected()) {
            try {
                //退出登录
                mqttClient.disconnect(null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Timber.d("正在断开连接正常情况");
                        mqttClient.unregisterResources();
                        mqttClient = null;
                        App.getInstance().tokenInvalid(true);
                        Timber.d("mqttDisconnect " + "断开连接成功");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Timber.d("正在断开连接失败");
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            //被挤出
            Timber.d("正在断开连接被挤出");
            mqttClient.unregisterResources();
            mqttClient = null;
            App.getInstance().tokenInvalid(true);
        }
    }

    //http退出，mqtt断开
    public void httpMqttDisconnect() {
        String token = App.getInstance().getUserBean().getUid();
        reconnectionNum = 10;
        if (mqttClient == null) {
            Timber.d("mqttClient为空");
            return;
        }
        if (TextUtils.isEmpty(token)) {
            return;
        }

        if (mqttClient.isConnected()) {
            try {
                //退出登录
                mqttClient.disconnect(null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Timber.d("正在断开连接正常情况");
                        mqttClient.unregisterResources();
                        mqttClient = null;
                        Timber.d("mqttDisconnect " + "断开连接成功");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Timber.d("正在断开连接失败");
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            //被挤出
            Timber.d("正在断开连接被挤出");
            mqttClient.unregisterResources();
            mqttClient = null;
        }


    }

    /**
     * 获取当前MQTT连接的状态
     *
     * @return
     */
    public boolean onGetMQTTConnectedState() {
        if (null != mqttClient) {
            return mqttClient.isConnected();
        }
        return false;
    }
}
