package com.revolo.lock.mqtt.bean.publishresultbean;

public class WifiLockAddPwdAttrResponseBean extends WifiLockBaseResponseBean{
    /**
     * msgtype : respone
     * msgId : 4
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF123456789
     * func : addPwd
     * code : 200
     * params : {}
     * timestamp : 13433333333
     */


    private ParamsBean params;
    private String timestamp;

    public ParamsBean getParams() {
        return params;
    }

    public void setParams(ParamsBean params) {
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

    @Override
    public String toString() {
        return "WifiLockAddPasswordResponseBean{" +
                "msgtype='" +getMsgtype() + '\'' +
                ", msgId=" + getMsgId() +
                ", userId='" + getUserId() + '\'' +
                ", wfId='" + getWfId() + '\'' +
                ", func='" + getFunc() + '\'' +
                ", code=" + getCode() +
                ", params=" + params +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
