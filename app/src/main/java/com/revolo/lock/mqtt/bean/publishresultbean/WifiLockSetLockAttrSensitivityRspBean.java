package com.revolo.lock.mqtt.bean.publishresultbean;

import com.revolo.lock.mqtt.bean.publishbean.attrparams.AutoLockTimeParams;

/**
 * author :
 * time   : 2021/3/4
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class WifiLockSetLockAttrSensitivityRspBean {


    /**
     * msgtype : respone
     * msgId : 4
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF123456789
     * func : setLockAttr
     * code : 200
     * params : {}
     * timestamp : 13433333333
     */

    private String msgtype;
    private int msgId;
    private String userId;
    private String wfId;
    private String func;
    private int code;
    private AutoLockTimeParams params;
    private String timestamp;

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public AutoLockTimeParams getParams() {
        return params;
    }

    public void setParams(AutoLockTimeParams params) {
        this.params = params;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static class ParamsBean {
    }
}
