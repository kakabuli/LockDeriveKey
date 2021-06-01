package com.revolo.lock.bean.respone;

/**
 * author : Jack
 * time   : 2021/3/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 上传头像回调实体
 */
public class AlexaAppUrlAndWebUrlBeanRsp {


    /**
     * code : 200
     * msg : success
     * nowTime : 1616407340
     */

    private String code;
    private String msg;
    private Integer nowTime;
    private DataBean data;

    public static class DataBean {

        private String appUrl;

        private String webFallbackUrl;

        public String getAppUrl() {
            return appUrl;
        }

        public void setAppUrl(String appUrl) {
            this.appUrl = appUrl;
        }

        public String getWebFallbackUrl() {
            return webFallbackUrl;
        }

        public void setWebFallbackUrl(String webFallbackUrl) {
            this.webFallbackUrl = webFallbackUrl;
        }
    }

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
}
