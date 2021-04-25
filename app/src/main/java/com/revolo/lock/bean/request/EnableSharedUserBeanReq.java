package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 启用/禁用分享用户权限请求实体
 */
public class EnableSharedUserBeanReq {


    /**
     * shareId : 5def53704d3ee13934ffbeea
     * uid : 5c4fe492dc93897aa7d8600b
     * isEnable : 1
     */

    private String shareId;   // 分享用户-设备关联ID(分享用户自增id)
    private String uid;       // 管理员ID
    private int isEnable;     // 分享用户权限启用/禁止。 1 启用（默认）； 0禁止

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
