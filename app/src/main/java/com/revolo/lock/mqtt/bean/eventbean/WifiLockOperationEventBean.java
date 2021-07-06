package com.revolo.lock.mqtt.bean.eventbean;

import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockBaseResponseBean;

public class WifiLockOperationEventBean extends WifiLockBaseResponseBean {

    /**
     {
     "msgtype": "event",
     "func": "wfevent",
     "msgId": 103,
     "devtype": "kdswflock",
     "lockId": "W5145C2004180AA1330607",
     "eventtype": "record",
     "eventparams": {
     "eventType": 1,
     "eventSource": 4,
     "eventCode": 1,
     "userID": 255,
     "appID": 0
     },
     "wfId": "TSF1201610173",
     "timestamp": "1588145640"
     }
     */

    /**
     * ①　eventType如下：
     * 0x01 Operation操作(动作类)
     * 0x02 Program程序(用户管理类)
     * 0x03 Mode 模式切换
     * ②　eventSource如下：
     * 0x00 Keypad键盘
     * 0x03 RFID卡片
     * 0x04 Fingerprint指纹
     * 0x07 Face 人脸
     * 0x08 App
     * 0x09 Key Unlock机械钥匙
     * 0x0A 室内OPEN键开锁
     * 0x0B 室内感应把手开锁
     * 0xFF不确定
     * ③　eventCode如下：
     * 当eventType为Operation时
     * 0x01 Lock上锁
     * 0x02 Unlock开锁
     * 0x03主锁舌关
     * 当eventType为Program时
     * 0x01：MasterCodeChanged管理员密码修改
     * 0x02: 添加
     * 0x03: 删除
     * 0x0F：恢复出厂设置
     * 当eventType为Mode 时
     * 0x01：  自动模式
     * 0x02：  手动模式
     * 0x03：  通用模式
     * 0x04：  安全模式
     * 0x05:	反锁模式
     * 0x06:	布防模式
     * 0x07:	节能模式
     * <p>
     * ④　"userID"如下：
     * 有用户编号的时候填入编号，没有时填入0xFF.
     * 标准密码：0~9
     * 指纹编号范围：0-99
     * 卡片编号范围：0-99
     * 机械钥匙编号：100
     * 一键开锁编号：102
     * APP指令编号：103
     * 管理密码254（一般模式用户密码）
     * 访客密码253
     * 一次性密码252
     * 离线密码250
     * ⑤　"appID"如下：
     * APP用户编码：0-99
     */


    private String devtype;
    private String lockId;
    private String eventtype;
    private EventparamsBean eventparams;
    private String timestamp;
    private int state;

    public static class EventparamsBean {
        private int eventType;
        private int eventSource;
        private int eventCode;
        private int userID;
        private int appID;
        private int amMode;
        private int safeMode;
        private int defences;
        private String language;
        private int operatingMode;
        private int volume;
        private int powerSave;
        private int duress;
        private int doorSensor;
        private int autoLockTime;
        private int elecFenceSensitivity;
        private int returnCode;
        private int status;
        private int devNum;
        private String SW;
        private int hwerrcode;

        public int getDevNum() {
            return devNum;
        }

        public void setDevNum(int devNum) {
            this.devNum = devNum;
        }

        public String getSW() {
            return SW;
        }

        public void setSW(String SW) {
            this.SW = SW;
        }

        public int getReturnCode() {
            return returnCode;
        }

        public void setReturnCode(int returnCode) {
            this.returnCode = returnCode;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getHwerrcode() {
            return hwerrcode;
        }

        public void setHwerrcode(int hwerrcode) {
            this.hwerrcode = hwerrcode;
        }

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

        public int getPowerSave() {
            return powerSave;
        }

        public void setPowerSave(int powerSave) {
            this.powerSave = powerSave;
        }

        public int getDuress() {
            return duress;
        }

        public void setDuress(int duress) {
            this.duress = duress;
        }

        public int getDoorSensor() {
            return doorSensor;
        }

        public void setDoorSensor(int doorSensor) {
            this.doorSensor = doorSensor;
        }

        public int getAutoLockTime() {
            return autoLockTime;
        }

        public void setAutoLockTime(int autoLockTime) {
            this.autoLockTime = autoLockTime;
        }

        public int getElecFenceSensitivity() {
            return elecFenceSensitivity;
        }

        public void setElecFenceSensitivity(int elecFenceSensitivity) {
            this.elecFenceSensitivity = elecFenceSensitivity;
        }

        public int getEventType() {
            return eventType;
        }

        public void setEventType(int eventType) {
            this.eventType = eventType;
        }

        public int getEventSource() {
            return eventSource;
        }

        public void setEventSource(int eventSource) {
            this.eventSource = eventSource;
        }

        public int getEventCode() {
            return eventCode;
        }

        public void setEventCode(int eventCode) {
            this.eventCode = eventCode;
        }

        public int getUserID() {
            return userID;
        }

        public void setUserID(int userID) {
            this.userID = userID;
        }

        public int getAppID() {
            return appID;
        }

        public void setAppID(int appID) {
            this.appID = appID;
        }

        @Override
        public String toString() {
            return "EventparamsBean{" +
                    "eventType=" + eventType +
                    ", eventSource=" + eventSource +
                    ", eventCode=" + eventCode +
                    ", userID=" + userID +
                    ", appID=" + appID +
                    ", amMode=" + amMode +
                    ", safeMode=" + safeMode +
                    ", defences=" + defences +
                    ", language='" + language + '\'' +
                    ", operatingMode=" + operatingMode +
                    ", volume=" + volume +
                    ", powerSave=" + powerSave +
                    ", duress=" + duress +
                    ", doorSensor=" + doorSensor +
                    ", autoLockTime=" + autoLockTime +
                    ", elecFenceSensitivity=" + elecFenceSensitivity +
                    '}';
        }
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "WifiLockRecordResult{" +
                "msgtype='" + getMsgtype() + '\'' +
                ", func='" + getFunc() + '\'' +
                ", msgId=" + getMsgId() +
                ", devtype='" + devtype + '\'' +
                ", lockId='" + lockId + '\'' +
                ", eventtype='" + eventtype + '\'' +
                ", eventparams=" + eventparams +
                ", wfId='" + getWfId() + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

}
