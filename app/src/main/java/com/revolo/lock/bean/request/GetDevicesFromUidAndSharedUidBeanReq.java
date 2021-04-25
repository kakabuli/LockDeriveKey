package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 获取分享用户的设备列表请求实体
 */
public class GetDevicesFromUidAndSharedUidBeanReq {


    /**
     * uid : 5c4fe492dc93897aa7d8600b
     * shareId : 5c4fe492dc93897aa7d8123456
     */

    private String uid;           // 管理员ID
    private String shareId;       // 分享用户ID

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
