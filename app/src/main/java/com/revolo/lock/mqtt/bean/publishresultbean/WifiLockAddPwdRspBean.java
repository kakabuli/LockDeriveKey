package com.revolo.lock.mqtt.bean.publishresultbean;

/**
 * author :
 * time   : 2021/3/4
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class WifiLockAddPwdRspBean extends WifiLockBaseResponseBean{


    /**
     * msgtype : respone
     * msgId : 4
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF123456789
     * func : createPwd
     * code : 200
     * params : {"keyNum":1}
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
        /**
         * keyNum : 1
         */

        private int keyNum;

        public int getKeyNum() {
            return keyNum;
        }

        public void setKeyNum(int keyNum) {
            this.keyNum = keyNum;
        }
    }
}
