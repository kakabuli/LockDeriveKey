package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class EnableSharedUserBeanReq {


    /**
     * shareId : 5def53704d3ee13934ffbeea
     * uid : 5c4fe492dc93897aa7d8600b
     * isEnable : 1
     */

    private String shareId;
    private String uid;
    private int isEnable;

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(int isEnable) {
        this.isEnable = isEnable;
    }
}
