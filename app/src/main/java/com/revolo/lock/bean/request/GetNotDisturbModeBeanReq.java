package com.revolo.lock.bean.request;

/**
 * author : zhougm
 * time   : 2021/7/12
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class GetNotDisturbModeBeanReq {
    private String uid;
    private String wifiSN;

    public String getWifiSN() {
        return wifiSN;
    }

    public void setWifiSN(String wifiSN) {
        this.wifiSN = wifiSN;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
