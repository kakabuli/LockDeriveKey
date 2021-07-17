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
        private ShareDataBean shareData;
        private int msgType;
        private long pushAt;

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

        public ShareDataBean getShareData() {
            return shareData;
        }

        public void setShareData(ShareDataBean shareData) {
            this.shareData = shareData;
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

        public static class ShareDataBean {
            private String sharekey;
            private String adminUid;
            private String adminName;
            private String shareUid;
            private int isAgree;
            private int isEnable;
            private int deadTime;
            private int createTime;
            private String agreeTime;
            private String lastName;
            private String firstName;
            private String deviceSN;
            private String shareAccount;
            private int shareUserType;

            public String getSharekey() {
                return sharekey;
            }

            public void setSharekey(String sharekey) {
                this.sharekey = sharekey;
            }

            public String getAdminUid() {
                return adminUid;
            }

            public void setAdminUid(String adminUid) {
                this.adminUid = adminUid;
            }

            public String getAdminName() {
                return adminName;
            }

            public void setAdminName(String adminName) {
                this.adminName = adminName;
            }

            public String getShareUid() {
                return shareUid;
            }

            public void setShareUid(String shareUid) {
                this.shareUid = shareUid;
            }

            public int getIsAgree() {
                return isAgree;
            }

            public void setIsAgree(int isAgree) {
                this.isAgree = isAgree;
            }

            public int getIsEnable() {
                return isEnable;
            }

            public void setIsEnable(int isEnable) {
                this.isEnable = isEnable;
            }

            public int getDeadTime() {
                return deadTime;
            }

            public void setDeadTime(int deadTime) {
                this.deadTime = deadTime;
            }

            public int getCreateTime() {
                return createTime;
            }

            public void setCreateTime(int createTime) {
                this.createTime = createTime;
            }

            public String getAgreeTime() {
                return agreeTime;
            }

            public void setAgreeTime(String agreeTime) {
                this.agreeTime = agreeTime;
            }

            public String getLastName() {
                return lastName;
            }

            public void setLastName(String lastName) {
                this.lastName = lastName;
            }

            public String getFirstName() {
                return firstName;
            }

            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            public String getDeviceSN() {
                return deviceSN;
            }

            public void setDeviceSN(String deviceSN) {
                this.deviceSN = deviceSN;
            }

            public String getShareAccount() {
                return shareAccount;
            }

            public void setShareAccount(String shareAccount) {
                this.shareAccount = shareAccount;
            }

            public int getShareUserType() {
                return shareUserType;
            }

            public void setShareUserType(int shareUserType) {
                this.shareUserType = shareUserType;
            }
        }
    }
}
