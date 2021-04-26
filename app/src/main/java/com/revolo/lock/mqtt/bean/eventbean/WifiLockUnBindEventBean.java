package com.revolo.lock.mqtt.bean.eventbean;

public class WifiLockUnBindEventBean {

    /**
     * {
     * "msgtype":"event",
     * "func":"wfevent",
     * "msgId":4,
     * "devtype":"kdswflock",
     * "eventtype":"unbindNotify",
     * "unbindResult": 1
     * "eventparams":{
     *
     *  },
     * "wfId":"wfuuid",
     * "timestamp":"1541468973342"
     * }
     */

    private String msgtype;
    private String func;
    private int msgId;
    private String devtype;
    private EventParamsBean eventparams;
    private String wfId;
    private String timestamp;

    public static class EventParamsBean{

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

    public EventParamsBean getEventparams() {
        return eventparams;
    }

    public void setEventparams(EventParamsBean eventparams) {
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

    @Override
    public String toString() {
        return "WifiLockUnBindEventBean{" +
                "msgtype='" + msgtype + '\'' +
                ", func='" + func + '\'' +
                ", msgId=" + msgId +
                ", devtype='" + devtype + '\'' +
                ", eventparams=" + eventparams +
                ", wfId='" + wfId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
