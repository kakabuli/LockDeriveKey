package com.revolo.lock.bean.request;

import java.util.List;

/**
 * author :
 * time   : 2021/3/17
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class CheckAllOTABeanReq {


    /**
     * customer : 1
     * deviceName : WF03201210027
     * versions : [{"devNum":1,"version":"1.0"},{"devNum":2,"version":"1.3"}]
     */

    private int customer;
    private String deviceName;
    private List<VersionsBean> versions;

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

        private int devNum;
        private String version;

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
