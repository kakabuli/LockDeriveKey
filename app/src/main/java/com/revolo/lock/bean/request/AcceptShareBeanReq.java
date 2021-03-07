package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class AcceptShareBeanReq {


    /**
     * uid : 5f5a0002c36a8525eb648432
     * shareKey : 8f34e1b2-e6ea-4cb5-90be-c1d0dfa287d6
     */

    private String uid;
    private String shareKey;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getShareKey() {
        return shareKey;
    }

    public void setShareKey(String shareKey) {
        this.shareKey = shareKey;
    }
}
