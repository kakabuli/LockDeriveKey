package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class DelSharedUserBeanReq {


    /**
     * shareId : 5def53704d3ee13934ffbeea
     * deviceSN : WPF00000123
     * uid : 5c4fe492dc93897aa7d8600b
     */

    private String shareId;
    private String deviceSN;
    private String uid;

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
