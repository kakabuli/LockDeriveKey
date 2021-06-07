package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/5/25
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class DeleteSystemMessageReq {

    private String uid;
    private String mid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }
}
