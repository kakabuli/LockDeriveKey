package com.revolo.lock.mqtt.bean.publishresultbean;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PublishResult {

    private boolean isPublishSuccess;
    private IMqttToken asyncActionToken;
    private MqttMessage mqttMessage;


    public PublishResult(boolean publishResult, IMqttToken asyncActionToken, MqttMessage mqttMessage) {
        this.isPublishSuccess = publishResult;
        this.asyncActionToken = asyncActionToken;
        this.mqttMessage = mqttMessage;
    }

    public PublishResult() {

    }


    @Override
    public String toString() {
        return "PublishResult{" +
                "isPublishSuccess=" + isPublishSuccess +
                ", asyncActionToken=" + asyncActionToken +
                ", mqttMessage=" + mqttMessage +
                '}';
    }

    public boolean isPublishSuccess() {
        return isPublishSuccess;
    }

    public void setPublishSuccess(boolean publishSuccess) {
        this.isPublishSuccess = publishSuccess;
    }

    public IMqttToken getAsyncActionToken() {
        return asyncActionToken;
    }

    public void setAsyncActionToken(IMqttToken asyncActionToken) {
        this.asyncActionToken = asyncActionToken;
    }

    public MqttMessage getMqttMessage() {
        return mqttMessage;
    }

    public void setMqttMessage(MqttMessage mqttMessage) {
        this.mqttMessage = mqttMessage;
    }
}
