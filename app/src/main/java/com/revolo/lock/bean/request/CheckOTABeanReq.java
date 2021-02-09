package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/2/9
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class CheckOTABeanReq {


    /**
     * customer : 1
     * deviceName : WF01202010006
     * version : 1.3
     * devNum : 1
     */

    private int customer;
    private String deviceName;
    private String version;
    private int devNum;

    public int getCustomer() {
        return customer;
    }

    public void setCustomer(int customer) {
        this.customer = customer;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getDevNum() {
        return devNum;
    }

    public void setDevNum(int devNum) {
        this.devNum = devNum;
    }
}
