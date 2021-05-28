package com.revolo.lock.mqtt.bean.publishresultbean;

public class WifiLockSetMagneticResponseBean extends WifiLockBaseResponseBean{

    /**
     * msgtype : respone
     * msgId : 4
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF123456789
     * func : setMagnetic
     * code : 200
     * params : {"mode":1}
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
         * mode : 1
         */

        private int mode;

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        @Override
        public String toString() {
            return "ParamsBean{" +
                    "mode=" + mode +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "WifiLockSetMagneticResponseBean{" +
                "msgtype='" + getMsgtype() + '\'' +
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
