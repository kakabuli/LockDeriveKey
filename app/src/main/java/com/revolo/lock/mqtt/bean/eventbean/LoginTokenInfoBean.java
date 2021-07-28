package com.revolo.lock.mqtt.bean.eventbean;

/**
 * author : zhougm
 * time   : 2021/7/28
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class LoginTokenInfoBean {

    private String uid;
    private String eventType;
    private int msgId;
    private String func;
    private String token;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
