package com.revolo.lock.bean.respone;

/**
 * author :
 * time   : 2021/3/14
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class UploadUserAvatarBeanRsp {


    /**
     * code : 200
     * msg : success
     * nowTime : 1616407340
     * data : {"path":"http://test.irevolo.com:83/avatarFiles/60459a490423e437d2c01ccc/p.png"}
     */

    private String code;
    private String msg;
    private Integer nowTime;
    private DataBean data;

    public static class DataBean {
        /**
         * path : http://test.irevolo.com:83/avatarFiles/60459a490423e437d2c01ccc/p.png
         */

        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
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
