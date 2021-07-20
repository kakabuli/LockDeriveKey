package com.revolo.lock.bean.respone;

import java.util.List;

public class SystemMessageListBeanRsp {

    private String code;
    private String msg;
    private int nowTime;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        private String _id;
        private int isShowAgreeShare;
        private String uid;
        private String alertTitle;
        private String alertBody;
        private int msgType;
        private long pushAt;
        private int isAgree;
        private String shareKey;
        private String adminUid;
        private String timeZone;

        public String getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }

        public String getShareKey() {
            return shareKey;
        }

        public void setShareKey(String shareKey) {
            this.shareKey = shareKey;
        }

        public String getAdminUid() {
            return adminUid;
        }

        public void setAdminUid(String adminUid) {
            this.adminUid = adminUid;
        }

        public int getIsAgree() {
            return isAgree;
        }

        public void setIsAgree(int isAgree) {
            this.isAgree = isAgree;
        }

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public int getIsShowAgreeShare() {
            return isShowAgreeShare;
        }

        public void setIsShowAgreeShare(int isShowAgreeShare) {
            this.isShowAgreeShare = isShowAgreeShare;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getAlertTitle() {
            return alertTitle;
        }

        public void setAlertTitle(String alertTitle) {
            this.alertTitle = alertTitle;
        }

        public String getAlertBody() {
            return alertBody;
        }

        public void setAlertBody(String alertBody) {
            this.alertBody = alertBody;
        }

        public int getMsgType() {
            return msgType;
        }

        public void setMsgType(int msgType) {
            this.msgType = msgType;
        }

        public long getPushAt() {
            return pushAt;
        }

        public void setPushAt(long pushAt) {
            this.pushAt = pushAt;
        }
    }
}
