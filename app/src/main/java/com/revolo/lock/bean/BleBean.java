package com.revolo.lock.bean;

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
    private byte[] pwd2or3;
    private OKBLEDeviceImp mOKBLEDeviceImp;

    public byte[] getPwd1() {
        return pwd1;
    }

    public void setPwd1(byte[] pwd1) {
        this.pwd1 = pwd1;
    }

    public byte[] getPwd2or3() {
        return pwd2or3;
    }

    public void setPwd2or3(byte[] pwd2or3) {
        this.pwd2or3 = pwd2or3;
    }

    public OKBLEDeviceImp getOKBLEDeviceImp() {
        return mOKBLEDeviceImp;
    }

    public void setOKBLEDeviceImp(OKBLEDeviceImp OKBLEDeviceImp) {
        mOKBLEDeviceImp = OKBLEDeviceImp;
    }

    public BleBean(OKBLEDeviceImp OKBLEDeviceImp) {
        mOKBLEDeviceImp = OKBLEDeviceImp;
    }

}
