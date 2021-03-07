package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class UpdateSharedUserNickNameBeanReq {


    /**
     * shareId : 5def53704d3ee13934ffbeea
     * nickname : 老二
     * uid : 5c4fe492dc93897aa7d8600b
     */

    private String shareId;
    private String nickname;
    private String uid;

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
