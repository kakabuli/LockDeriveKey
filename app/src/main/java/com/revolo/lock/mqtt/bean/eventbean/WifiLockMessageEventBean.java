package com.revolo.lock.mqtt.bean.eventbean;

public class WifiLockMessageEventBean {

    /**
     * {
     * "msgtype":"event",
     * "func":"wfevent",
     * "msgId":4,
     * "devtype":" kdswflock ",
     * "lockId":"lockId",
     * "eventtype":"lockInf",
     * "eventparams":{
     * "sn":"xxxxxx",   //序列号
     * "firmware":"xxxxxx",   //门锁固件版本
     * "software":"xxxxxx",   //门锁软件版本
     * "FACEversion":"xxxxxx",   //人脸固件版本
     * "BLEversion":"xxxxxx",  //BLE版本
     * "WIFIversion":"xxxxxx",  //WIFI版本
     * "MQTTversion":"xxxxxx", //MQTT协议版本
     * "functionSet":100,   //功能集
     * "lockModel":"xxxxxx",   //锁型号
     * "power":55,			//电量
     *  },
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
    private String wfId;
    private String timestamp;


    public static class EventparamsBean {
        private String sn;
        private String firmware;
        private String software;
        private String FACEversion;
        private String BLEversion;
        private String WIFIversion;
        private String MQTTversion;
        private int functionSet;
        private String lockModel;
        private int power;

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public String getFirmware() {
            return firmware;
        }

        public void setFirmware(String firmware) {
            this.firmware = firmware;
        }

        public String getSoftware() {
            return software;
        }

        public void setSoftware(String software) {
            this.software = software;
        }

        public String getFACEversion() {
            return FACEversion;
        }

        public void setFACEversion(String FACEversion) {
            this.FACEversion = FACEversion;
        }

        public String getBLEversion() {
            return BLEversion;
        }

        public void setBLEversion(String BLEversion) {
            this.BLEversion = BLEversion;
        }

        public String getWIFIversion() {
            return WIFIversion;
        }

        public void setWIFIversion(String WIFIversion) {
            this.WIFIversion = WIFIversion;
        }

        public String getMQTTversion() {
            return MQTTversion;
        }

        public void setMQTTversion(String MQTTversion) {
            this.MQTTversion = MQTTversion;
        }

        public int getFunctionSet() {
            return functionSet;
        }

        public void setFunctionSet(int functionSet) {
            this.functionSet = functionSet;
        }

        public String getLockModel() {
            return lockModel;
        }

        public void setLockModel(String lockModel) {
            this.lockModel = lockModel;
        }

        public int getPower() {
            return power;
        }

        public void setPower(int power) {
            this.power = power;
        }

        @Override
        public String toString() {
            return "EventparamsBean{" +
                    "sn='" + sn + '\'' +
                    ", firmware='" + firmware + '\'' +
                    ", software='" + software + '\'' +
                    ", FACEversion='" + FACEversion + '\'' +
                    ", BLEversion='" + BLEversion + '\'' +
                    ", WIFIversion='" + WIFIversion + '\'' +
                    ", MQTTversion='" + MQTTversion + '\'' +
                    ", functionSet=" + functionSet +
                    ", lockModel='" + lockModel + '\'' +
                    ", power=" + power +
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
        return "WifiLockInfoEventBean{" +
                "msgtype='" + msgtype + '\'' +
                ", func='" + func + '\'' +
                ", msgId=" + msgId +
                ", devtype='" + devtype + '\'' +
                ", lockId='" + lockId + '\'' +
                ", eventtype='" + eventtype + '\'' +
                ", eventparams=" + eventparams +
                ", wfId='" + wfId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
