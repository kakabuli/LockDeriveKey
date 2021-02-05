package com.revolo.lock.mqtt.bean;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttData {

    private String func;
    private String topic;
    private String payload;
    private MqttMessage mqttMessage;
    private int messageId = -1;
    private String returnCode;
    private String msgtype = "";

    public MqttData(String func, String topic, String payload, MqttMessage mqttMessage, int messageId) {
        this.func = func;
        this.topic = topic;
        this.payload = payload;
        this.mqttMessage = mqttMessage;
        this.messageId = messageId;
    }

    public MqttData() {
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public MqttMessage getMqttMessage() {
        return mqttMessage;
    }

    public void setMqttMessage(MqttMessage mqttMessage) {
        this.mqttMessage = mqttMessage;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public boolean isThisRequest(int messageId, String func) {
        //严格判断   数据不对  协议不通用
//        if (this.messageId == messageId && this.func.equalsIgnoreCase(func) && "respone".equals(msgtype)){
//            return true;
//        }

        if (this.func.equalsIgnoreCase(func)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MqttData{" +
                "func='" + func + '\'' +
                ", topic='" + topic + '\'' +
                ", payload='" + payload + '\'' +
                ", mqttMessage=" + mqttMessage +
                ", messageId=" + messageId +
                ", returnCode='" + returnCode + '\'' +
                ", msgtype='" + msgtype + '\'' +
                '}';
    }
}
