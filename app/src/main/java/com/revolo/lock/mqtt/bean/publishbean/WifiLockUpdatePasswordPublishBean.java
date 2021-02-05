package com.revolo.lock.mqtt.bean.publishbean;

public class WifiLockUpdatePasswordPublishBean {
    /**
     * msgtype : request
     * msgId : 4
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF123456789
     * func : updatePwd
     * params : {"keyType":1,"keyNum":1,"attribute":1,"week":1,"startTime":13433333333,"endTime":13433333333}
     * timestamp : 13433333333
     */

    private String msgtype;
    private int msgId;
    private String userId;
    private String wfId;
    private String func;
    private ParamsBean params;
    private String timestamp;

    public WifiLockUpdatePasswordPublishBean(String msgtype, int msgId, String userId, String wfId, String func, ParamsBean params, String timestamp) {
        this.msgtype = msgtype;
        this.msgId = msgId;
        this.userId = userId;
        this.wfId = wfId;
        this.func = func;
        this.params = params;
        this.timestamp = timestamp;
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
         * keyType : 1
         * keyNum : 1
         * attribute : 1
         * week : 1
         * startTime : 13433333333
         * endTime : 13433333333
         */

        private int keyType;
        private int keyNum;
        private int attribute;
        private int week;
        private long startTime;
        private long endTime;

        public int getKeyType() {
            return keyType;
        }

        public void setKeyType(int keyType) {
            this.keyType = keyType;
        }

        public int getKeyNum() {
            return keyNum;
        }

        public void setKeyNum(int keyNum) {
            this.keyNum = keyNum;
        }

        public int getAttribute() {
            return attribute;
        }

        public void setAttribute(int attribute) {
            this.attribute = attribute;
        }

        public int getWeek() {
            return week;
        }

        public void setWeek(int week) {
            this.week = week;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        @Override
        public String toString() {
            return "ParamsBean{" +
                    "keyType=" + keyType +
                    ", keyNum=" + keyNum +
                    ", attribute=" + attribute +
                    ", week=" + week +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "WifiLockUpdatePasswordPublishBean{" +
                "msgtype='" + msgtype + '\'' +
                ", msgId=" + msgId +
                ", userId='" + userId + '\'' +
                ", wfId='" + wfId + '\'' +
                ", func='" + func + '\'' +
                ", params=" + params +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
