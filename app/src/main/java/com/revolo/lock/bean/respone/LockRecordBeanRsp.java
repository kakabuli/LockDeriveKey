package com.revolo.lock.bean.respone;

import java.util.List;

/**
 * author :
 * time   : 2021/3/16
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class LockRecordBeanRsp {


    /**
     * code : 200
     * msg : success
     * nowTime : 1615800959
     * data : [{"_id":"604f29c18af521731595e177","timesTamp":1578377599,"wifiSN":"WF132231004","appId":"888888","createTime":1615800769,"eventCode":8,"eventSource":8,"eventType":8,"lastName":"888","userId":8},{"_id":"604f29c18af521731595e179","timesTamp":1578377566,"wifiSN":"WF132231004","appId":"00000","createTime":1615800769,"eventCode":0,"eventSource":0,"eventType":0,"lastName":"888","userId":0}]
     */

    private String code;
    private String msg;
    private int nowTime;
    private List<DataBean> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getNowTime() {
        return nowTime;
    }

    public void setNowTime(int nowTime) {
        this.nowTime = nowTime;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * _id : 604f29c18af521731595e177
         * timesTamp : 1578377599
         * wifiSN : WF132231004
         * appId : 888888
         * createTime : 1615800769
         * eventCode : 8
         * eventSource : 8
         * eventType : 8
         * lastName : 888
         * userId : 8
         */

        private String _id;
        private long timesTamp;
        private String wifiSN;
        private int appId;
        private long createTime;
        private int eventCode;
        private int eventSource;
        private int eventType;
        private String lastName;
        private int userId;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public long getTimesTamp() {
            return timesTamp;
        }

        public void setTimesTamp(long timesTamp) {
            this.timesTamp = timesTamp;
        }

        public String getWifiSN() {
            return wifiSN;
        }

        public void setWifiSN(String wifiSN) {
            this.wifiSN = wifiSN;
        }

        public int getAppId() {
            return appId;
        }

        public void setAppId(int appId) {
            this.appId = appId;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public int getEventCode() {
            return eventCode;
        }

        public void setEventCode(int eventCode) {
            this.eventCode = eventCode;
        }

        public int getEventSource() {
            return eventSource;
        }

        public void setEventSource(int eventSource) {
            this.eventSource = eventSource;
        }

        public int getEventType() {
            return eventType;
        }

        public void setEventType(int eventType) {
            this.eventType = eventType;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }
    }
}
