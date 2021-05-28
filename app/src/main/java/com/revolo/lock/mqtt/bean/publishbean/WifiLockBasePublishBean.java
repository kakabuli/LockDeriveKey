package com.revolo.lock.mqtt.bean.publishbean;

import java.io.Serializable;

public class WifiLockBasePublishBean implements Serializable {
    private String msgtype;

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }
}
