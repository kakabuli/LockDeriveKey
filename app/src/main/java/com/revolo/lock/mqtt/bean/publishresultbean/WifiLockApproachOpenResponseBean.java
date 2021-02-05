package com.revolo.lock.mqtt.bean.publishresultbean;

public class WifiLockApproachOpenResponseBean {
    /**
     * msgtype : respone
     * msgId : 4
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF123456789
     * func : approachOpen
     * code : 200
     * params : {" broadcast ":5}
     * timestamp : 13433333333
     */

    private String msgtype;
    private int msgId;
    private String userId;
    private String wfId;
    private String func;
    private int code;
    private ParamsBean params;
    private String timestamp;

    public static class ParamsBean {
        /**
         *  broadcast  : 5
         */

        private int broadcast;

        public int getBroadcast() {
            return broadcast;
        }

        public void setBroadcast(int broadcast) {
            this.broadcast = broadcast;
        }

        @Override
        public String toString() {
            return "ParamsBean{" +
                    "broadcast=" + broadcast +
                    '}';
        }
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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

    @Override
    public String toString() {
        return "WifiLockApproachOpenResponseBean{" +
                "msgtype='" + msgtype + '\'' +
                ", msgId=" + msgId +
                ", userId='" + userId + '\'' +
                ", wfId='" + wfId + '\'' +
                ", func='" + func + '\'' +
                ", code=" + code +
                ", params=" + params +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
