package com.revolo.lock.bean.respone;

/**
 * author : Jack
 * time   : 2021/3/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 上传头像回调实体
 */
public class AlexaSkillEnableBeanRsp {

    /**
     * code : 200
     * msg : success
     * nowTime : 1616407340
     */

    private String code;
    private String msg;
    private long nowTime;
    private Object data;

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

    public long getNowTime() {
        return nowTime;
    }

    public void setNowTime(long nowTime) {
        this.nowTime = nowTime;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
