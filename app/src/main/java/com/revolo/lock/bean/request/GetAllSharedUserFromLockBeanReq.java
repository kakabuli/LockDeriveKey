package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class GetAllSharedUserFromLockBeanReq {


    /**
     * uid : 5def586f4d3ee1156842868c
     * deviceSN : WPF00000123
     */

    private String uid;
    private String deviceSN;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }
}
