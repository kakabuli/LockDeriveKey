package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/2/9
 * E-mail : wengmaowei@kaadas.com
 * desc   : 检测升级文件（单组件）请求实体
 */
public class CheckOTABeanReq {


    /**
     * customer : 1
     * deviceName : WF01202010006
     * version : 1.3
     * devNum : 1
     */

    private int customer;             // 客户。16为Revolo。
    private String deviceName;        // WIFI设备SN
    private String version;           // 当前版本
    private int devNum;               // 升级编号。1为WIFI模块，2为WIFI锁。（具体固件未知）

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
