package com.revolo.lock.bean.respone;

/**
 * author : zhougm
 * time   : 2021/7/12
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class GetVersionBeanRsp {

    private String code;
    private String msg;
    private int nowTime;
    private DataBean data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getNowTime() {
        return nowTime;
    }

    public void setNowTime(int nowTime) {
        this.nowTime = nowTime;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private String _id;
        private String appVersions;
        private String phoneSysType;
        private String versionDesc;
        private String forceFlag;
        private int timeStamp;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getAppVersions() {
            return appVersions;
        }

        public void setAppVersions(String appVersions) {
            this.appVersions = appVersions;
        }

        public String getPhoneSysType() {
            return phoneSysType;
        }

        public void setPhoneSysType(String phoneSysType) {
            this.phoneSysType = phoneSysType;
        }

        public String getVersionDesc() {
            return versionDesc;
        }

        public void setVersionDesc(String versionDesc) {
            this.versionDesc = versionDesc;
        }

        public String getForceFlag() {
            return forceFlag;
        }

        public void setForceFlag(String forceFlag) {
            this.forceFlag = forceFlag;
        }

        public int getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(int timeStamp) {
            this.timeStamp = timeStamp;
        }
    }
}
