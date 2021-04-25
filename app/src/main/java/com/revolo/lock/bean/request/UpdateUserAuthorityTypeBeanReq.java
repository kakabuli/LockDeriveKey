package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 修改邀请用户类型
 */
public class UpdateUserAuthorityTypeBeanReq {


    /**
     * shareId : 5def53704d3ee13934ffbeea
     * uid : 5c4fe492dc93897aa7d8600b
     * shareUserType : 1
     */

    private String shareId;           // 分享用户-设备关联ID(分享用户自增id)
    private String uid;               // 管理员ID
    private int shareUserType;        // 分享用户类型。 1 family；2 guest

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

    public int getShareUserType() {
        return shareUserType;
    }

    public void setShareUserType(int shareUserType) {
        this.shareUserType = shareUserType;
    }
}
