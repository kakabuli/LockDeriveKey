package com.revolo.lock.mqtt.bean.publishresultbean;

public class WifiLockDeviceStatusBean {

    /**
     * msgtype : event
     * func : wfevent
     * msgId : 4
     * devtype : kdswflock
     * eventtype : wifiState
     * state : 1
     * eventparams : {}
     * wfId : wfuuid
     * timestamp : 1541468973342
     */

    private String msgtype;
    private String func;
    private int msgId;
    private String devtype;
    private String eventtype;
    private String state;
    private EventparamsBean eventparams;
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

    public String getEventtype() {
        return eventtype;
    }

    public void setEventtype(String eventtype) {
        this.eventtype = eventtype;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public EventparamsBean getEventparams() {
        return eventparams;
    }

    public void setEventparams(EventparamsBean eventparams) {
        this.eventparams = eventparams;
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
    }

    @Override
    public String toString() {
        return "WifiLockDeviceStatusBean{" +
                "msgtype='" + msgtype + '\'' +
                ", func='" + func + '\'' +
                ", msgId=" + msgId +
                ", devtype='" + devtype + '\'' +
                ", eventtype='" + eventtype + '\'' +
                ", state='" + state + '\'' +
                ", eventparams=" + eventparams +
                ", wfId='" + wfId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
