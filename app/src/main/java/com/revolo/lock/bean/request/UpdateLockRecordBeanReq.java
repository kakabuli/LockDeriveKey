package com.revolo.lock.bean.request;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/3/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 上传操作记录请求实体
 */
public class UpdateLockRecordBeanReq {


    /**
     * uid : 5c4fe492dc93897aa7d8600b
     * deviceSN : WF132231004
     * operationList : [{"eventType":1,"eventSource":8,"eventCode":1,"userId":1,"appId":"","timesTamp":1578377588}]
     */

    private String uid;                                 // 用户ID
    private String deviceSN;                            // 设备SN
    private List<OperationListBean> operationList;      // 操作记录

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public List<OperationListBean> getOperationList() {
        return operationList;
    }

    public void setOperationList(List<OperationListBean> operationList) {
        this.operationList = operationList;
    }

    public static class OperationListBean {
        /**
         * eventType : 1
         * eventSource : 8
         * eventCode : 1
         * userId : 1
         * appId : 1
         * timesTamp : 1578377588
         */

        private int eventType;       // 事件类型
        private int eventSource;     // 操作类型
        private int eventCode;       // 操作方式
        private int userId;          // 操作编码
        private int appId;           // app用户编码
        private long timesTamp;      // 时间

        public int getEventType() {
            return eventType;
        }

        public void setEventType(int eventType) {
            this.eventType = eventType;
        }

        public int getEventSource() {
            return eventSource;
        }

        public void setEventSource(int eventSource) {
            this.eventSource = eventSource;
        }

        public int getEventCode() {
            return eventCode;
        }

        public void setEventCode(int eventCode) {
            this.eventCode = eventCode;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getAppId() {
            return appId;
        }

        public void setAppId(int appId) {
            this.appId = appId;
        }

        public long getTimesTamp() {
            return timesTamp;
        }

        public void setTimesTamp(long timesTamp) {
            this.timesTamp = timesTamp;
        }
    }
}
