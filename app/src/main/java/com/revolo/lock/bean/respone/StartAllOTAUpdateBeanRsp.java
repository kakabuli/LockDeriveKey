package com.revolo.lock.bean.respone;

/**
 * author : Jack
 * time   : 2021/3/17
 * E-mail : wengmaowei@kaadas.com
 * desc   : 确认升级（多组件）回调实体
 */
public class StartAllOTAUpdateBeanRsp {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1603863357
     * data : {}
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
    }
}
