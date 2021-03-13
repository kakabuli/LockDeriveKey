package com.revolo.lock.bean.respone;

/**
 * author :
 * time   : 2021/3/12
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class UpdateUserFirstLastNameBeanRsp {


    /**
     * code : 200
     * msg : success
     * nowTime : 1615272928
     * data : null
     */

    private String code;
    private String msg;
    private int nowTime;
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

    public int getNowTime() {
        return nowTime;
    }

    public void setNowTime(int nowTime) {
        this.nowTime = nowTime;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
