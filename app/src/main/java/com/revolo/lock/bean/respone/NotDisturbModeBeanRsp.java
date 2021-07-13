package com.revolo.lock.bean.respone;

/**
 * author : zhougm
 * time   : 2021/7/12
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class NotDisturbModeBeanRsp {
    private String code;
    private String msg;
    private Integer nowTime;
    private DataBean data;

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

    public Integer getNowTime() {
        return nowTime;
    }

    public void setNowTime(Integer nowTime) {
        this.nowTime = nowTime;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public class DataBean {

        private String uid;
        private boolean openlockPushSwitch;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public boolean isOpenlockPushSwitch() {
            return openlockPushSwitch;
        }

        public void setOpenlockPushSwitch(boolean openlockPushSwitch) {
            this.openlockPushSwitch = openlockPushSwitch;
        }
    }
}
