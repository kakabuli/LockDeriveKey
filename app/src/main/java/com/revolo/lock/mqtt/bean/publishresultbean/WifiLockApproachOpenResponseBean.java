package com.revolo.lock.mqtt.bean.publishresultbean;

public class WifiLockApproachOpenResponseBean extends WifiLockBaseResponseBean{
    /**
     * msgtype : respone
     * msgId : 4
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF123456789
     * func : approachOpen
     * code : 200
     * params : {" broadcast ":5}
     * timestamp : 13433333333
     */

    private ParamsBean params;
    private String timestamp;

    public static class ParamsBean {
        /**
         *  broadcast  : 5
         */

        private int broadcast;
        private int ibeacon;

        public int getBroadcast() {
            return broadcast;
        }

        public void setBroadcast(int broadcast) {
            this.broadcast = broadcast;
        }

        public int getIbeacon() {
            return ibeacon;
        }

        public void setIbeacon(int ibeacon) {
            this.ibeacon = ibeacon;
        }

        @Override
        public String toString() {
            return "ParamsBean{" +
                    "broadcast=" + broadcast +
                    ", ibeacon=" + ibeacon +
                    '}';
        }
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

    @Override
    public String toString() {
        return "WifiLockApproachOpenResponseBean{" +
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
