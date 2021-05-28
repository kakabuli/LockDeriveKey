package com.revolo.lock.mqtt.bean.publishbean;

public class WifiLockDoorOptPublishBean extends WifiLockBasePublishBean{


    public WifiLockDoorOptPublishBean(String msgtype, Integer msgId, String userId, String wfId, String func, ParamsBean params, String timestamp) {
        this.setMsgtype(msgtype);
        this.msgId = msgId;
        this.userId = userId;
        this.wfId = wfId;
        this.func = func;
        this.params = params;
        this.timestamp = timestamp;
    }

    /**
     * msgtype : request
     * msgId : 4
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF123456789
     * func : setLock
     * params : {"dooropt":1,"offlinePwd":"","userNumberId":1}
     * timestamp : 13433333333
     */

    private Integer msgId;
    private String userId;
    private String wfId;
    private String func;
    private ParamsBean params;
    private String timestamp;


    public Integer getMsgId() {
        return msgId;
    }

    public void setMsgId(Integer msgId) {
        this.msgId = msgId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWfId() {
        return wfId;
    }

    public void setWfId(String wfId) {
        this.wfId = wfId;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public ParamsBean getParams() {
        return params;
    }

    public void setParams(ParamsBean params) {
        this.params = params;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static class ParamsBean {
        /**
         * dooropt : 1
         * offlinePwd :
         * userNumberId : 1
         */

        private Integer dooropt;
        private String offlinePwd;
        private Integer userNumberId;

        public Integer getDooropt() {
            return dooropt;
        }

        public void setDooropt(Integer dooropt) {
            this.dooropt = dooropt;
        }

        public String getOfflinePwd() {
            return offlinePwd;
        }

        public void setOfflinePwd(String offlinePwd) {
            this.offlinePwd = offlinePwd;
        }

        public Integer getUserNumberId() {
            return userNumberId;
        }

        public void setUserNumberId(Integer userNumberId) {
            this.userNumberId = userNumberId;
        }
    }
}
