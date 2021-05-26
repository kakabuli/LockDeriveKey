package com.revolo.lock.bean.respone;

/**
 * author :
 * time   : 2021/5/25
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class UpdateLockInfoRsp {


    private String code;
    private String msg;
    private Integer nowTime;
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

    public Integer getNowTime() {
        return nowTime;
    }

    public void setNowTime(Integer nowTime) {
        this.nowTime = nowTime;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
