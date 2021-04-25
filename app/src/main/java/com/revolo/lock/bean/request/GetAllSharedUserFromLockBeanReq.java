package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 获取锁下的所有分享用户列表请求实体
 */
public class GetAllSharedUserFromLockBeanReq {


    /**
     * uid : 5def586f4d3ee1156842868c
     * deviceSN : WPF00000123
     */

    private String uid;            // 用户uid
    private String deviceSN;       // 设备SN

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
