package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/5/25
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class SystemMessageListReq {

    private String uid;
    private int pageNum;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
}
