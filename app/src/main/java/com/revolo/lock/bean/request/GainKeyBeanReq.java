package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 创建分享链接请求实体
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

    private String uid;             // 用户uid
    private String deviceSN;        // 设备SN
    private int shareUserType;      // 分享用户类型。1为 family 用户，2为 guest 用户
    private int openPurview;        // 分享权限类型。1：一次性开锁，2：时间段开锁，3：无限开锁
    private long startTime;         // 开始时间
    private long endTime;           // 结束结束
    private String shareNickName;   // 用户昵称


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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getShareNickName() {
        return shareNickName;
    }

    public void setShareNickName(String shareNickName) {
        this.shareNickName = shareNickName;
    }

}
