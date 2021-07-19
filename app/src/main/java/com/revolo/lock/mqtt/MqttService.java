package com.revolo.lock.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.revolo.lock.App;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.PublishResult;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class MqttService extends Service {

    //请勿添加static
    private MqttAndroidClient mqttClient;

    private final Handler mHandler = new Handler();

    //重连次数10
    public int reconnectionNum = 10;

    /**
     * 判断是否订阅成功
     */
    private final PublishSubject<Boolean> mSubscribe = PublishSubject.create();
    private final PublishSubject<MqttData> onReceiverDataObservable = PublishSubject.create();
    private final PublishSubject<Boolean> connectStateObservable = PublishSubject.create();
    private final PublishSubject<PublishResult> publishObservable = PublishSubject.create();
    private final PublishSubject<Boolean> disconnectObservable = PublishSubject.create();
    private final PublishSubject<MqttData> notifyEventObservable = PublishSubject.create();


    /**
     * 订阅状态
     */
    public PublishSubject<Boolean> subscribeStatus() {
        return mSubscribe;
    }

    public class MyBinder extends Binder {
        public MqttService getService() {
            return MqttService.this;
        }
    }


    public Observable<MqttData> listenerDataBack() {
        return onReceiverDataObservable;
    }

    public Observable<MqttData> listenerNotifyData() {
        return notifyEventObservable;
    }


    private String userId;
    private String token;

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.d("attachView   mqtt 启动了");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //NotificationManager.silentForegroundNotification(this);//TODO:未知revolo是否要做通知，用FCM就不用自己做通知，先注释
        return super.onStartCommand(intent, flags, startId);
    }


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
        connOpts.setCleanSession(MQttConstant.MQTT_CLEANSE_SION);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("mqtt MqttService 被杀死");
        //System.exit(0); 退出并没finish activity 因此增加了这一步
        ActivityUtils.finishAllActivities();
        System.exit(0);
    }

    public MqttAndroidClient getMqttClient() {
        return mqttClient;
    }

    //连接
    public void mqttConnection() {
        if (App.getInstance().getUserBean() == null) {
            return;
        }
        userId = App.getInstance().getUserBean().getUid();
        token = App.getInstance().getUserBean().getToken();
        Timber.d("userId-->" + userId + " ----token--->" + token);
        //TODO: 2019/4/25  此处为空   应该重新读取一下本地文件，延时100ms吧，如果再读取不到？直接退出   mqtt不能不登录的  不登录  这个APP就废了
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(token)) {
            return;
        }
        //

        if (mqttClient == null) {
            mqttClient = new MqttAndroidClient(App.getInstance(), MQttConstant.MQTT_BASE_URL, "app:" + userId);
        }

        //已经连接
        try {
            if (mqttClient.isConnected()) {
                Timber.d("mqttConnection  mqtt已连接");
                return;
            }
        } catch (Exception e) {
            Timber.e("获取客户端连接状况失败   " + e.getMessage());
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
                    reconnectionNum = 10;
                    connectStateObservable.onNext(true);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                //连接丢失
                if (null == cause) {
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
                // App.getInstance().getMQttService().mqttConnection();


            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // TODO: 2021/3/31 消息处理机制存在问题，需要修复,这种分发机制会导致使用超时的话一直都超时
                if (message == null) {
                    return;
                }
                //收到消息
                String payload = new String(message.getPayload());
                LogUtils.d("收到MQtt消息" + payload + "---topic" + topic + "  messageID  " + message.getId());
                //String func, String topic, String payload, MqttMessage mqttMessage
                JSONObject jsonObject = new JSONObject(payload);
                int messageId = -1;
                String returnCode = "";
                String msgtype = "";
                try {
                    if (payload.contains("returnCode")) {
                        returnCode = jsonObject.getString("returnCode");
                    }

                    if (payload.contains("msgId")) {
                        messageId = jsonObject.getInt("msgId");
                    }

                    if (messageId == -1) {
                        if (payload.contains("msgid")) {
                            messageId = jsonObject.getInt("msgid");
                        }
                    }
                    if (payload.contains("msgtype")) {
                        msgtype = jsonObject.getString("msgtype");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MqttData mqttData = new MqttData(jsonObject.getString("func"), topic, payload, message, messageId);
                mqttData.setReturnCode(returnCode);
                mqttData.setMsgtype(msgtype);

                onReceiverDataObservable.onNext(mqttData);

                if (MQttConstant.WF_EVENT.equals(mqttData.getFunc())) {
                    notifyEventObservable.onNext(mqttData);
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //交互完成
            }
        });
        try {
            mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Timber.d("mqtt连接成功");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //TODO:中文下的连接返回，需测试一下，在英文下返回的连接异常
                    //可能出现无权连接（5）---用户在其他手机登录
                    if (reconnectionNum > 0) {
                        Timber.d("mqtt连接" + " 连接失败1     " + exception.toString() + "token是" + token + "用户名" + userId);
                        MqttExceptionHandle.onFail(MqttExceptionHandle.ConnectException, asyncActionToken, exception);
                        if (exception.toString().endsWith(" (5)")) {
                            // TODO: 2019/4/1  该用户在其他手机登录(清除所有数据）---暂时未处理

                            if (mqttClient != null) {
                                mqttDisconnect();
                            }
                            return;
                        }
                        if (exception.toString().endsWith(" (4)")) {
                            Timber.d("mqtt的用户名或密码错误");
                            if (mqttClient != null) {
                                mqttDisconnect();
                            }
                            return;
                        }
                        //两秒后进行重连
                        Runnable reconncetRunnable = new Runnable() {
                            @Override
                            public void run() {
                                reconnectionNum--;
                                mqttConnection();
                            }
                        };
                        mHandler.postDelayed(reconncetRunnable, 6000);

                    } else {
//                        ToastUtil.getInstance().showShort(R.string.mqtt_connection_fail);

                        if (exception.toString().equals("无权连接 (5)")) {
                            // TODO: 2019/4/1  该用户在其他手机登录(清除所有数据）---暂时未处理
                            if (mqttClient != null) {
                                mqttDisconnect();
                            }
                            return;
                        }
                        if ("错误的用户名或密码 (4)".equals(exception.toString())) {
                            Timber.d("mqtt的用户名或密码错误");
                            if (mqttClient != null) {
                                mqttDisconnect();
                            }
                            return;
                        }
                        connectStateObservable.onNext(false);

                    }

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    //订阅
    private void mqttSubscribe(MqttAndroidClient mqttClient, String topic, int qos) {
        Timber.d("订阅    " + topic + "   " + (mqttClient != null));
        try {
            if (mqttClient != null) {
                if (!TextUtils.isEmpty(topic) && mqttClient.isConnected()) {
                    mqttClient.subscribe(topic, qos, null, new
                            IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    mSubscribe.onNext(true);
                                    Timber.d("mqttSubscribe " + "订阅成功");
                                    //TODO:订阅成功，立即拿设备列表,此时拿设备列表，从mqtt转成以http方式

                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    mSubscribe.onNext(false);
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
    public Observable<MqttData> mqttEventNotifyPublishListener() {
        return notifyEventObservable;
    }

    //发布
    public Observable<MqttData> mqttPublish(String topic, MqttMessage mqttMessage) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                Timber.d("发布mqtt消息 " + "topic: " + topic + "  mqttMessage: " + mqttMessage.toString());
                mqttClient.publish(topic, mqttMessage, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
//                        LogUtils.e("发布消息成功  ", topic + "  消息Id  " + mqttMessage.getId() );
                        publishObservable.onNext(new PublishResult(true, asyncActionToken, mqttMessage));
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Timber.d("发布消息失败 " + topic + "    fail");
                        MqttExceptionHandle.onFail(MqttExceptionHandle.PublishException, asyncActionToken, exception);
                        publishObservable.onNext(new PublishResult(false, asyncActionToken, mqttMessage));
                    }
                });
            } else {
                mqttConnection();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return onReceiverDataObservable;
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
                        disconnectObservable.onNext(true);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Timber.d("正在断开连接失败");
                        disconnectObservable.onNext(false);
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            //被挤出
            Timber.d("正在断开连接被挤出");
            disconnectObservable.onNext(true);
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
                        disconnectObservable.onNext(true);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Timber.d("正在断开连接失败");
                        disconnectObservable.onNext(false);
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            //被挤出
            Timber.d("正在断开连接被挤出");
            disconnectObservable.onNext(true);
            mqttClient.unregisterResources();
            mqttClient = null;
        }
    }
}
