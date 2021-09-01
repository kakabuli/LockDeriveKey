package com.revolo.lock.bean.request;

/**
 * author : zhougm
 * time   : 2021/7/12
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class PostNotDisturbModeBeanReq {
    private String uid;
    private boolean openlockPushSwitch;
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

    public boolean isOpenlockPushSwitch() {
        return openlockPushSwitch;
    }

    public void setOpenlockPushSwitch(boolean openlockPushSwitch) {
        this.openlockPushSwitch = openlockPushSwitch;
    }
}
