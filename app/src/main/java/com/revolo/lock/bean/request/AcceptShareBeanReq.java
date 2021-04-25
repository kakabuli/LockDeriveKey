package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 接收邀请请求实体
 */
public class AcceptShareBeanReq {


    /**
     * uid : 5f5a0002c36a8525eb648432
     * shareKey : 8f34e1b2-e6ea-4cb5-90be-c1d0dfa287d6
     */

    private String uid;          // 用户uid
    private String shareKey;     // 分享钥匙

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
