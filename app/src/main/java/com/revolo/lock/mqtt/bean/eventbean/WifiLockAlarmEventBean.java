package com.revolo.lock.mqtt.bean.eventbean;

public class WifiLockAlarmEventBean  {

    /**
     * msgtype : event
     * func : wfevent
     * msgId : 4
     * devtype : kdswflock
     * lockId :  lockId
     * eventtype : alarm
     * eventparams : {"alarmCode":4,"clusterID":257}
     * eventcode : 2
     * wfId :  wfuuid
     * timestamp : 1541468973342
     */

    private String msgtype;
    private String func;
    private int msgId;
    private String devtype;
    private String lockId;
    private String eventtype;
    private EventparamsBean eventparams;
    private int eventcode;
    private String wfId;
    private String timestamp;


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

    public static class EventparamsBean {
        /**
         * alarmCode : 4
         * clusterID : 257
         */

        private int alarmCode;
        private int clusterID;

        public int getAlarmCode() {
            return alarmCode;
        }

        public void setAlarmCode(int alarmCode) {
            this.alarmCode = alarmCode;
        }

        public int getClusterID() {
            return clusterID;
        }

        public void setClusterID(int clusterID) {
            this.clusterID = clusterID;
        }

        @Override
        public String toString() {
            return "EventparamsBean{" +
                    "alarmCode=" + alarmCode +
                    ", clusterID=" + clusterID +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "WifiLockAlarmEventBean{" +
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
