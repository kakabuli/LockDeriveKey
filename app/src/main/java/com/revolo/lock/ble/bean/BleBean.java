package com.revolo.lock.ble.bean;

import com.a1anwang.okble.client.core.OKBLEDeviceImp;
import com.revolo.lock.ble.OnBleDeviceListener;

import java.io.Serializable;

/**
 * author :
 * time   : 2021/1/26
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class BleBean implements Serializable {

    private byte[] pwd1;
    private byte[] pwd2;
    private byte[] pwd3;
    private String esn;
    private boolean isAppPair;
    private boolean isAuth;
    private OKBLEDeviceImp mOKBLEDeviceImp;
    private OnBleDeviceListener mOnBleDeviceListener;
    private byte[] pwd2_copy;
    private boolean isHavePwd2Or3=false;

    public byte[] getPwd1() {
        return pwd1;
    }

    public void setPwd1(byte[] pwd1) {
        this.pwd1 = pwd1;
    }

    public byte[] getPwd2() {
        return pwd2;
    }

    public void setPwd2(byte[] pwd2) {
        this.pwd2 = pwd2;
    }

    public byte[] getPwd3() {
        return pwd3;
    }

    public void setPwd3(byte[] pwd3) {
        this.pwd3 = pwd3;
    }

    public OKBLEDeviceImp getOKBLEDeviceImp() {
        return mOKBLEDeviceImp;
    }

    public void setOKBLEDeviceImp(OKBLEDeviceImp OKBLEDeviceImp) {
        mOKBLEDeviceImp = OKBLEDeviceImp;
    }

    public String getEsn() {
        return esn;
    }

    public void setEsn(String esn) {
        this.esn = esn;
    }

    public boolean isAppPair() {
        return isAppPair;
    }

    /**
     * 是否进行APP与蓝牙的配对
     * @param appPair 是否进行配对
     */
    public void setAppPair(boolean appPair) {
        isAppPair = appPair;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public OnBleDeviceListener getOnBleDeviceListener() {
        return mOnBleDeviceListener;
    }

    public void setOnBleDeviceListener(OnBleDeviceListener onBleDeviceListener) {
        mOnBleDeviceListener = onBleDeviceListener;
    }

    public BleBean(OKBLEDeviceImp OKBLEDeviceImp) {
        mOKBLEDeviceImp = OKBLEDeviceImp;
    }

    public byte[] getPwd2_copy() {
        return pwd2_copy;
    }

    public void setPwd2_copy(byte[] pwd2_copy) {
        this.pwd2_copy = pwd2_copy;
    }

    public boolean isHavePwd2Or3() {
        return isHavePwd2Or3;
    }

    public void setHavePwd2Or3(boolean havePwd2Or3) {
        isHavePwd2Or3 = havePwd2Or3;
    }
}
