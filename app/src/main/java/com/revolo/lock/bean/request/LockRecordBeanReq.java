package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 获取操作记录请求实体
 */
public class LockRecordBeanReq {


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
