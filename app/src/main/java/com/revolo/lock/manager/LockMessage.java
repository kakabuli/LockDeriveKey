package com.revolo.lock.manager;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 数据
 */
public class LockMessage implements Serializable {

    private int index;  //消息id
    private int sendFrequency = 0;//发送次数
    public String sn;  //设备sn 码
    public String mac;  //蓝牙mac
    private int messageType;  //消息类型
    private String mqtt_message_code;//mqtt消息code
    private String mqtt_topic;//mqtt消息头部数据
    private MqttMessage mqttMessage;   //mqtt消息数据
    private byte[] bytes;   //ble消息数据
    private int messageCode;//信息操作
    private int bleChr = 0;//0 默认ble写的通道 1、PairMsg

    public void addSendFre() {
        sendFrequency++;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public MqttMessage getMqttMessage() {
        return mqttMessage;
    }

    public void setMqttMessage(MqttMessage mqttMessage) {
        this.mqttMessage = mqttMessage;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSendFrequency() {
        return sendFrequency;
    }

    public void setSendFrequency(int sendFrequency) {
        this.sendFrequency = sendFrequency;
    }

    public int getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }

    public String getMqtt_topic() {
        return mqtt_topic;
    }

    public void setMqtt_topic(String mqtt_topic) {
        this.mqtt_topic = mqtt_topic;
    }

    public int getBleChr() {
        return bleChr;
    }

    public void setBleChr(int bleChr) {
        this.bleChr = bleChr;
    }

    public String getMqtt_message_code() {
        return mqtt_message_code;
    }

    public void setMqtt_message_code(String mqtt_message_code) {
        this.mqtt_message_code = mqtt_message_code;
    }

    public LockMessage() {
    }

    public LockMessage(LockMessage message) {
        this.index = message.getIndex();
        this.sendFrequency = message.getSendFrequency();
        this.sn = message.getSn();
        this.mac = message.getMac();
        this.messageType = message.getMessageType();
        this.mqtt_message_code = message.getMqtt_message_code();
        this.mqtt_topic = message.getMqtt_topic();
        this.mqttMessage = message.getMqttMessage();
        this.bytes = message.getBytes();
        this.messageCode = message.getMessageCode();
        this.bleChr = message.getBleChr();
    }

    @Override
    public String toString() {
        return "LockMessage{" +
                "index=" + index +
                ", sendFrequency=" + sendFrequency +
                ", sn='" + sn + '\'' +
                ", mac='" + mac + '\'' +
                ", messageType=" + messageType +
                ", mqtt_message_code='" + mqtt_message_code + '\'' +
                ", mqtt_topic='" + mqtt_topic + '\'' +
                ", mqttMessage=" + mqttMessage +
                ", bytes=" + Arrays.toString(bytes) +
                ", messageCode=" + messageCode +
                ", bleChr=" + bleChr +
                '}';
    }
}
