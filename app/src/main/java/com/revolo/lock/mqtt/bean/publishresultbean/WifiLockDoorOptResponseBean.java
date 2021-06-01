package com.revolo.lock.mqtt.bean.publishresultbean;

public class WifiLockDoorOptResponseBean extends WifiLockBaseResponseBean{
    /**
     * msgtype : respone
     * msgId : 4
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF123456789
     * func :  setLock
     * code : 200
     * params : {"dooropt":1}
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
         * dooropt : 1
         */

        private int dooropt;

        public int getDooropt() {
            return dooropt;
        }

        public void setDooropt(int dooropt) {
            this.dooropt = dooropt;
        }

        @Override
        public String toString() {
            return "ParamsBean{" +
                    "dooropt=" + dooropt +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "WifiLockDoorOptResponseBean{" +
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
