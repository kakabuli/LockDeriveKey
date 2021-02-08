package com.revolo.lock.ble.bean;

import com.a1anwang.okble.client.core.OKBLEDeviceImp;

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
    private OKBLEDeviceImp mOKBLEDeviceImp;

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

    public BleBean(OKBLEDeviceImp OKBLEDeviceImp) {
        mOKBLEDeviceImp = OKBLEDeviceImp;
    }



}
