package com.revolo.lock.mqtt.bean.publishbean;

/**
 * author :
 * time   : 2021/3/4
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class WifiLockAddPwdPublishBean extends WifiLockBasePublishBean{


    /**
     * msgtype : request
     * msgId : 4
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF123456789
     * func : createPwd
     * params : {"keyType":1,"key":"xxxxxx"}
     * timestamp : 13433333333
     */

    private int msgId;
    private String userId;
    private String wfId;
    private String func;
    private ParamsBean params;
    private String timestamp;

    public WifiLockAddPwdPublishBean(String msgtype, int msgId, String userId, String wfId, String func, ParamsBean params, String timestamp) {
        this.setMsgtype(msgtype);
        this.msgId = msgId;
        this.userId = userId;
        this.wfId = wfId;
        this.func = func;
        this.params = params;
        this.timestamp = timestamp;
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
         * keyType : 1
         * key : xxxxxx
         */

        private int keyType;
        private String key;

        public int getKeyType() {
            return keyType;
        }

        public void setKeyType(int keyType) {
            this.keyType = keyType;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
