package com.revolo.lock.bean.respone;

/**
 * author :
 * time   : 2021/2/2
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class GetCodeBeanRsp {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1564043606
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
