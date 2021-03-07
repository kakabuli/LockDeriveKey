package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class GainKeyBeanReq {


    /**
     * uid : 5c4fe492dc93897aa7d8600b
     * deviceSN : WPF00000123
     * shareUserType : 1
     * openPurview : 1
     * startTime : 2019-12-12 14:00:00
     * endTime : 2019-12-12 15:00:00
     */

    private String uid;
    private String deviceSN;
    private int shareUserType;
    private int openPurview;
    private String startTime;
    private String endTime;

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

    public int getShareUserType() {
        return shareUserType;
    }

    public void setShareUserType(int shareUserType) {
        this.shareUserType = shareUserType;
    }

    public int getOpenPurview() {
        return openPurview;
    }

    public void setOpenPurview(int openPurview) {
        this.openPurview = openPurview;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
