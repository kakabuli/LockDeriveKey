package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 获取分享用户的设备列表请求实体
 */
public class GetDevicesFromUidAndSharedUidBeanReq {

    private String adminUid;           // 管理员ID
    private String shareUId;       // 分享用户ID

    public String getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(String adminUid) {
        this.adminUid = adminUid;
    }

    public String getShareUId() {
        return shareUId;
    }

    public void setShareUId(String shareUId) {
        this.shareUId = shareUId;
    }
}
