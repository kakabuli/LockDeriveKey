package com.revolo.lock.mqtt.bean.eventbean;

public class WifiLockSwitchActionEventBean  {

    /**
     * {
     * "msgtype":"event",
     * "func":"wfevent",
     * "msgId":4,
     * "devtype":" kdswflock ",
     * "lockId":"lockId",
     * "eventtype":"action",
     * "eventparams":{
     * "amMode":0/1,  		//0自动模式1手动模式
     * "safeMode":0/1,		 //0通用模式1安全模式
     * "defences":0/1, 		//0撤防 1布防 0xFF
     * "language":"zh/en",	//语言
     * "operatingMode":0/1,	//0解除反锁1反锁
     * "volume":0/1,			//0语音模式 1静音模式
     * “faceStatus”: 0/1		// 0 面容识别开启 1 面容识别关闭
     * “powerSave”: 0/1		// 1 节能模式开启 0 节能模式关闭
     * },
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

    public class EventparamsBean {
        private int amMode;
        private int safeMode;
        private int defences;
        private String language;
        private int operatingMode;
        private int volume;
        @Deprecated
        private int faceStatus;
        private int powerSave;

        public int getAmMode() {
            return amMode;
        }

        public void setAmMode(int amMode) {
            this.amMode = amMode;
        }

        public int getSafeMode() {
            return safeMode;
        }

        public void setSafeMode(int safeMode) {
            this.safeMode = safeMode;
        }

        public int getDefences() {
            return defences;
        }

        public void setDefences(int defences) {
            this.defences = defences;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public int getOperatingMode() {
            return operatingMode;
        }

        public void setOperatingMode(int operatingMode) {
            this.operatingMode = operatingMode;
        }

        public int getVolume() {
            return volume;
        }

        public void setVolume(int volume) {
            this.volume = volume;
        }

        public int getFaceStatus() {
            return faceStatus;
        }

        public void setFaceStatus(int faceStatus) {
            this.faceStatus = faceStatus;
        }

        public int getPowerSave() {
            return powerSave;
        }

        public void setPowerSave(int powerSave) {
            this.powerSave = powerSave;
        }

        @Override
        public String toString() {
            return "EventparamsBean{" +
                    "amMode=" + amMode +
                    ", safeMode=" + safeMode +
                    ", defences=" + defences +
                    ", language='" + language + '\'' +
                    ", operatingMode=" + operatingMode +
                    ", volume=" + volume +
                    ", faceStatus=" + faceStatus +
                    ", powerSave=" + powerSave +
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
        return "WifiLockSwitchActionEventBean{" +
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
