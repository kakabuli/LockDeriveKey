package com.revolo.lock.bean.request;
/**
 * author : yi
 * time   : 2021/10/09
 * E-mail :
 * desc   : 获取操作记录请求实体  无时间筛选
 */
public class LockRecordNoTimeBeanReq {

    /**
     * wifiSN : WF132231004
     * uid :
     * page : 1
     */

    private String deviceSN;         // wifi模块SN
    private String uid;              // 用户id
    private int page;                // 第几页

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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
