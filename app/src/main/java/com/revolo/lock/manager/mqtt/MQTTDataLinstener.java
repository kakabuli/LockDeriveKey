package com.revolo.lock.manager.mqtt;

import com.revolo.lock.mqtt.bean.eventbean.WifiLockOperationEventBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockBaseResponseBean;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MQTTDataLinstener {
    /**
     * MQTT异常
     *
     * @param exceptionCode
     */
    void MQTTException(int exceptionCode);

    void connectComplete(boolean reconnect, String serverURI);

    void connectionLost(Throwable cause);

    void onSuccess(IMqttToken asyncActionToken);

    void messageArrived(String topic, MqttMessage message);

    void deliveryComplete(IMqttDeliveryToken token);

    void onFailure(IMqttToken asyncActionToken, Throwable exception);

    /**
     * @param bleDeviceLocal
     */
    void onAddDevice(boolean isdelete, BleDeviceLocal bleDeviceLocal);

    /**
     * 操作回调
     *
     * @param what
     * @param bean
     */
    void onOperationCallback(int what, WifiLockBaseResponseBean bean);

    /**
     * 无感开门  之蓝牙操作
     *
     * @param wfId
     */
    void onDoorSensorAlignmen(String wfId);

    /**
     * 更新的状态
     *
     * @param bean
     */
    void updateLockState(WifiLockOperationEventBean bean);

    /**
     * 将鉴权异常的数据上传到服务器同步
     */
    void updateToService(String esn, String pass,int createTime);
}
