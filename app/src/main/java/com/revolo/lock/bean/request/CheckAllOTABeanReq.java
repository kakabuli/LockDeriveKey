package com.revolo.lock.bean.request;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/3/17
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户检查升级（多组件）请求实体
 */
public class CheckAllOTABeanReq {


    /**
     * customer : 1
     * deviceName : WF03201210027
     * versions : [{"devNum":1,"version":"1.0"},{"devNum":2,"version":"1.3"}]
     */

    private int customer;                   // 客户。16为Revolo。
    private String deviceName;              // WIFI设备SN
    private List<VersionsBean> versions;    // 设备各组件当前版本

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

    public List<VersionsBean> getVersions() {
        return versions;
    }

    public void setVersions(List<VersionsBean> versions) {
        this.versions = versions;
    }

    public static class VersionsBean {
        /**
         * devNum : 1
         * version : 1.0
         */

        private int devNum;       // 升级编号。1为WIFI模块，2为WIFI锁，3为人脸模组，4为视频模组，5为视频模组微控制器，6为前面板，7为后面板。
        private String version;   // 当前组件版本号

        public int getDevNum() {
            return devNum;
        }

        public void setDevNum(int devNum) {
            this.devNum = devNum;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
