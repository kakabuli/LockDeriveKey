package com.revolo.lock.bean.respone;

/**
 * author : Jack
 * time   : 2021/3/26
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户反馈接口回调实体
 */
public class FeedBackBeanRsp {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1576119176
     * data : null
     */

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
