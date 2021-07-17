package com.revolo.lock.bean.respone;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 接收邀请回调实体
 */
public class AcceptShareBeanRsp {


    /**
     * code : 200
     * msg : success
     * nowTime : 1614854327
     * data : {}
     */

    private String code;
    private String msg;
    private int nowTime;

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

}
