package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class DelInvalidShareBeanReq {


    /**
     * uid : 5def586f4d3ee1156842868c
     * shareId : 5def586f4d3ee1156123456
     */

    private String uid;
    private String shareId;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }
}
