package com.revolo.lock.mqtt.bean.publishbean;

import com.revolo.lock.mqtt.bean.publishbean.attrparams.BaseParamsBean;

/**
 * author :
 * time   : 2021/3/2
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class WifiLockSetLockAttrPublishBean {


    /**
     * msgtype : request
     * msgId : 4
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF123456789
     * func : setLockAttr
     * timestamp : 13433333333
     */

    private String msgtype;
    private int msgId;
    private String userId;
    private String wfId;
    private String func;
    private BaseParamsBean mBaseParamsBean;
    private String timestamp;

    public WifiLockSetLockAttrPublishBean(String msgtype, int msgId, String userId, String wfId, String func, BaseParamsBean baseParamsBean, String timestamp) {
        this.msgtype = msgtype;
        this.msgId = msgId;
        this.userId = userId;
        this.wfId = wfId;
        this.func = func;
        mBaseParamsBean = baseParamsBean;
        this.timestamp = timestamp;
    }

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

    public BaseParamsBean getBaseParamsBean() {
        return mBaseParamsBean;
    }

    public void setBaseParamsBean(BaseParamsBean baseParamsBean) {
        mBaseParamsBean = baseParamsBean;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
