package com.revolo.lock.bean.respone;

/**
 * author :
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class GainKeyBeanRsp {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1614160330
     * data : {"shareUrl":"https://127.0.0.1:8090/wpflock/share/claimKey/1a6334f1-c8a1-471e-b8f4-2f8d9e966e74"}
     */

    private String code;
    private String msg;
    private int nowTime;
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

    public int getNowTime() {
        return nowTime;
    }

    public void setNowTime(int nowTime) {
        this.nowTime = nowTime;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * shareUrl : https://127.0.0.1:8090/wpflock/share/claimKey/1a6334f1-c8a1-471e-b8f4-2f8d9e966e74
         */

        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
