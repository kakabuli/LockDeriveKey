package com.revolo.lock.bean.respone;

import com.revolo.lock.bean.request.QuestionBeanReq;

import java.util.List;

public class QuestionBeanRsp {

    private String code;
    private String msg;
    private List<QuestionBeanReq> data;

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

    public List<QuestionBeanReq> getData() {
        return data;
    }

    public void setData(List<QuestionBeanReq> data) {
        this.data = data;
    }
}
