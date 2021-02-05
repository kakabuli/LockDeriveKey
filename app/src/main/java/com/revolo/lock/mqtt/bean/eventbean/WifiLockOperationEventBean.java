package com.revolo.lock.mqtt.bean.eventbean;

public class WifiLockOperationEventBean {

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
     *
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
        private int eventType;
        private int eventSource;
        private int eventCode;
        private int userID;
        private int appID;

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
            return "RecordParam{" +
                    "eventType=" + eventType +
                    ", eventSource=" + eventSource +
                    ", eventCode=" + eventCode +
                    ", userID=" + userID +
                    ", appID=" + appID +
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
        return "WifiLockRecordResult{" +
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
