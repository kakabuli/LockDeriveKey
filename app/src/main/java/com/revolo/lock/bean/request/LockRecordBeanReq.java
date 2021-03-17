package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/16
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class LockRecordBeanReq {


    /**
     * wifiSN : WF132231004
     * uid :
     * page : 1
     */

    private String wifiSN;
    private String uid;
    private int page;

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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
