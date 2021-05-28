package com.revolo.lock.mqtt.bean.publishresultbean;

import com.revolo.lock.mqtt.bean.publishbean.attrparams.VolumeParams;

/**
 * author :
 * time   : 2021/3/4
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class WifiLockSetLockAttrVolumeRspBean extends WifiLockBaseResponseBean{


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


    private VolumeParams params;
    private String timestamp;



    public VolumeParams getParams() {
        return params;
    }

    public void setParams(VolumeParams params) {
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
        return "WifiLockSetLockAttrVolumeRspBean{" +
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
