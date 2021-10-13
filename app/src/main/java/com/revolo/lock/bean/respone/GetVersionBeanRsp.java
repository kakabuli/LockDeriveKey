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
        private String forceFlag;
        private long timeStamp;
        private String versionDesc;
        private String phoneSysType;
        private String appVersions;
        private String note;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getForceFlag() {
            return forceFlag;
        }

        public void setForceFlag(String forceFlag) {
            this.forceFlag = forceFlag;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getVersionDesc() {
            return versionDesc;
        }

        public void setVersionDesc(String versionDesc) {
            this.versionDesc = versionDesc;
        }

        public String getPhoneSysType() {
            return phoneSysType;
        }

        public void setPhoneSysType(String phoneSysType) {
            this.phoneSysType = phoneSysType;
        }

        public String getAppVersions() {
            return appVersions;
        }

        public void setAppVersions(String appVersions) {
            this.appVersions = appVersions;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}
