package com.revolo.lock.bean.request;

/**
 * author : zhougm
 * time   : 2021/7/8
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class DeviceTokenBeanReq {

    private String uid;
    private String deviceToken;
    private int type;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
