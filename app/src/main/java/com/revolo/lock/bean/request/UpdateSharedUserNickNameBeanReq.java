package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 修改分享用户昵称请求实体
 */
public class UpdateSharedUserNickNameBeanReq {


    /**
     * shareId : 5def53704d3ee13934ffbeea
     * nickname : 老二
     * uid : 5c4fe492dc93897aa7d8600b
     */

    private String shareId;      // 分享用户-设备关联ID
    private String nickname;     // 分享用户昵称
    private String uid;          // 管理员ID

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
