package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/14
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class GetDevicesFromUidAndSharedUidBeanReq {


    /**
     * uid : 5c4fe492dc93897aa7d8600b
     * shareId : 5c4fe492dc93897aa7d8123456
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
