package com.revolo.lock.mqtt.bean.eventbean;

public class WifiLockOTAEventBean {
    /**
     * msgId : 1
     * msgtype : event
     * wfId : WF01201010006
     * func : wfevent
     * devtype :  kdswflock
     * eventtype : otaResult
     * timestamp : 1594522326174
     * eventparams : {"devNum":"1","SW":"orangeiot-2.4.3","oldversion":"orangeiot-2.4.2","returnCode":200,"hwerrcode":0,"status":3}
     */

    private int msgId;
    private String msgtype;
    private String wfId;
    private String func;
    private String devtype;
    private String eventtype;
    private String timestamp;
    private EventparamsBean eventparams;

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public EventparamsBean getEventparams() {
        return eventparams;
    }

    public void setEventparams(EventparamsBean eventparams) {
        this.eventparams = eventparams;
    }

    public static class EventparamsBean {
        /**
         * devNum : 1
         * SW : orangeiot-2.4.3
         * oldversion : orangeiot-2.4.2
         * returnCode : 200
         * hwerrcode : 0
         * status : 3
         */

        private String devNum;
        private String SW;
        private String oldversion;
        private int returnCode;
        private int hwerrcode;
        private int status;

        public String getDevNum() {
            return devNum;
        }

        public void setDevNum(String devNum) {
            this.devNum = devNum;
        }

        public String getSW() {
            return SW;
        }

        public void setSW(String SW) {
            this.SW = SW;
        }

        public String getOldversion() {
            return oldversion;
        }

        public void setOldversion(String oldversion) {
            this.oldversion = oldversion;
        }

        public int getReturnCode() {
            return returnCode;
        }

        public void setReturnCode(int returnCode) {
            this.returnCode = returnCode;
        }

        public int getHwerrcode() {
            return hwerrcode;
        }

        public void setHwerrcode(int hwerrcode) {
            this.hwerrcode = hwerrcode;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "EventparamsBean{" +
                    "devNum='" + devNum + '\'' +
                    ", SW='" + SW + '\'' +
                    ", oldversion='" + oldversion + '\'' +
                    ", returnCode=" + returnCode +
                    ", hwerrcode=" + hwerrcode +
                    ", status=" + status +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "WifiLockOTAEventResultBean{" +
                "msgId=" + msgId +
                ", msgtype='" + msgtype + '\'' +
                ", wfId='" + wfId + '\'' +
                ", func='" + func + '\'' +
                ", devtype='" + devtype + '\'' +
                ", eventtype='" + eventtype + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", eventparams=" + eventparams +
                '}';
    }
}
