package com.revolo.lock.bean.respone;

import java.util.List;

/**
 * author : zhougm
 * time   : 2021/8/9
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class Oauth2AccountBeanRsp {

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
        private List<AccountListBean> accountList;
        private List<AccountListBean> allowList;

        public List<AccountListBean> getAccountList() {
            return accountList;
        }

        public void setAccountList(List<AccountListBean> accountList) {
            this.accountList = accountList;
        }

        public List<AccountListBean> getAllowList() {
            return allowList;
        }

        public void setAllowList(List<AccountListBean> allowList) {
            this.allowList = allowList;
        }

        public static class AccountListBean {
            private String _id;
            private String uid;
            private String accountType;
            private String skillStatus;
            private String accountStatus;
            private long createTime;

            public String get_id() {
                return _id;
            }

            public void set_id(String _id) {
                this._id = _id;
            }

            public String getUid() {
                return uid;
            }

            public void setUid(String uid) {
                this.uid = uid;
            }

            public String getAccountType() {
                return accountType;
            }

            public void setAccountType(String accountType) {
                this.accountType = accountType;
            }

            public String getSkillStatus() {
                return skillStatus;
            }

            public void setSkillStatus(String skillStatus) {
                this.skillStatus = skillStatus;
            }

            public String getAccountStatus() {
                return accountStatus;
            }

            public void setAccountStatus(String accountStatus) {
                this.accountStatus = accountStatus;
            }

            public long getCreateTime() {
                return createTime;
            }

            public void setCreateTime(long createTime) {
                this.createTime = createTime;
            }
        }
    }
}
