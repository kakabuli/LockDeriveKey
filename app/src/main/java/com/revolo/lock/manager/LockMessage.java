package com.revolo.lock.manager;

import com.revolo.lock.mqtt.bean.publishbean.WifiLockBasePublishBean;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.Serializable;

/**
 * 数据
 */
public class LockMessage implements Serializable {

    private int index;
    private int sendFrequency = 0;
    public String sn;
    public String mac;
    private int messageType;
    private MqttMessage mqttMessage;
    private byte[] bytes;

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
}
