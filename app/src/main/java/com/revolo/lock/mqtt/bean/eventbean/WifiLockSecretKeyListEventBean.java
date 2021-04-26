package com.revolo.lock.mqtt.bean.eventbean;

public class WifiLockSecretKeyListEventBean {

    /**
     * {
     * "msgtype":"event",
     * "func":"wfevent",
     * "msgId":4,
     * "devtype":"kdswflock",
     * "lockId":"lockId",
     * "eventtype":"list",
     * "eventparams":{
     * "c":“xx xx xx xx ” , 	//(15字节Base64转20字节)
     * "f": “xx xx xx xx ” , 	//(15字节Base64转20字节)
     * "p": “xx xx xx xx ”	//(15字节Base64转20字节)
     * “r”:”xx xx xx xx”		//(15字节Base64转20字节)
     *  },
     * "eventcode":2,
     * "wfId":"wfuuid",
     * "timestamp":"1541468973342"
     * }
     */
    private String  msgtype;
    private String func;
    private int msgId;
    private String devtype;
    private String lockId;
    private String eventtype;
    private EventparamsBean eventparams;
    private int eventcode;
    private String wfId;
    private String timestamp;

    public static class EventparamsBean {
        private String c;
        private String f;
        private String p;
        private String r;

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public String getF() {
            return f;
        }

        public void setF(String f) {
            this.f = f;
        }

        public String getP() {
            return p;
        }

        public void setP(String p) {
            this.p = p;
        }

        public String getR() {
            return r;
        }

        public void setR(String r) {
            this.r = r;
        }

        @Override
        public String toString() {
            return "EventparamsBean{" +
                    "c='" + c + '\'' +
                    ", f='" + f + '\'' +
                    ", p='" + p + '\'' +
                    ", r='" + r + '\'' +
                    '}';
        }
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getDevtype() {
        return devtype;
    }

    public void setDevtype(String devtype) {
        this.devtype = devtype;
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public String getEventtype() {
        return eventtype;
    }

    public void setEventtype(String eventtype) {
        this.eventtype = eventtype;
    }

    public EventparamsBean getEventparams() {
        return eventparams;
    }

    public void setEventparams(EventparamsBean eventparams) {
        this.eventparams = eventparams;
    }

    public int getEventcode() {
        return eventcode;
    }

    public void setEventcode(int eventcode) {
        this.eventcode = eventcode;
    }

    public String getWfId() {
        return wfId;
    }

    public void setWfId(String wfId) {
        this.wfId = wfId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "WifiLockSecretKeyListEventBean{" +
                "msgtype='" + msgtype + '\'' +
                ", func='" + func + '\'' +
                ", msgId=" + msgId +
                ", devtype='" + devtype + '\'' +
                ", lockId='" + lockId + '\'' +
                ", eventtype='" + eventtype + '\'' +
                ", eventparams=" + eventparams +
                ", eventcode=" + eventcode +
                ", wfId='" + wfId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
