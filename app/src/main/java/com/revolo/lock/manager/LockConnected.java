package com.revolo.lock.manager;

import com.a1anwang.okble.client.scan.BLEScanResult;

public class LockConnected {
    private int connectType;
    private BLEScanResult bleScanResult;
    private byte[] pwd1;
    private byte[] pwd2;
    private boolean isAppPair;
    private String mEsn;

    public BLEScanResult getBleScanResult() {
        return bleScanResult;
    }

    public void setBleScanResult(BLEScanResult bleScanResult) {
        this.bleScanResult = bleScanResult;
    }

    public int getConnectType() {
        return connectType;
    }

    public void setConnectType(int connectType) {
        this.connectType = connectType;
    }

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

    public boolean isAppPair() {
        return isAppPair;
    }

    public void setAppPair(boolean appPair) {
        isAppPair = appPair;
    }

    public String getmEsn() {
        return mEsn;
    }

    public void setmEsn(String mEsn) {
        this.mEsn = mEsn;
    }
}
