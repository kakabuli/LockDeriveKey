package com.revolo.lock.manager;

import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockBaseResponseBean;

/**
 * 数据返回实体类
 */
public class LockMessageRes {
    private int MessgaeType;//0 WiFi，1 ble
    private String mac;
    private BleResultBean bleResultBea;
    private String sn;
    private WifiLockBaseResponseBean wifiLockBaseResponseBean;

    public LockMessageRes(int messgaeType, String mac, BleResultBean bleResultBea) {
        MessgaeType = messgaeType;
        this.mac = mac;
        this.bleResultBea = bleResultBea;
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
}
