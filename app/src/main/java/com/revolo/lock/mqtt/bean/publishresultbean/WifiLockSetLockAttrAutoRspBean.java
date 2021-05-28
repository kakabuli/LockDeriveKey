package com.revolo.lock.mqtt.bean.publishresultbean;

import com.revolo.lock.mqtt.bean.publishbean.attrparams.AmModeParams;

/**
 * author :
 * time   : 2021/3/4
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class WifiLockSetLockAttrAutoRspBean extends WifiLockBaseResponseBean{


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

    private AmModeParams params;
    private String timestamp;

    public AmModeParams getParams() {
        return params;
    }

    public void setParams(AmModeParams params) {
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
