package com.revolo.lock.manager;

import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockBaseResponseBean;

/**
 * 数据返回实体类
 */
public class LockMessageRes {
    private int MessgaeType;//消息类型：0 WiFi，1 ble
    private int messageCode;//信息操作
    private int resultCode;//返回的code 200正常 ，其他异常
    private String mac; //蓝牙mac
    private String sn;//设备sn码
    private BleResultBean bleResultBea;
    private WifiLockBaseResponseBean wifiLockBaseResponseBean;

    public LockMessageRes(int messgaeType, String mac, BleResultBean bleResultBea) {
        MessgaeType = messgaeType;
        this.mac = mac;
        this.bleResultBea = bleResultBea;
    }

    public LockMessageRes() {
    }

    public int getMessgaeType() {
        return MessgaeType;
    }

    public void setMessgaeType(int messgaeType) {
        MessgaeType = messgaeType;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public BleResultBean getBleResultBea() {
        return bleResultBea;
    }

    public void setBleResultBea(BleResultBean bleResultBea) {
        this.bleResultBea = bleResultBea;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public WifiLockBaseResponseBean getWifiLockBaseResponseBean() {
        return wifiLockBaseResponseBean;
    }

    public void setWifiLockBaseResponseBean(WifiLockBaseResponseBean wifiLockBaseResponseBean) {
        this.wifiLockBaseResponseBean = wifiLockBaseResponseBean;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public int getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }
}
