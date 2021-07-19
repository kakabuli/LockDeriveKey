package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 删除分享用户请求实体
 */
public class DelSharedUserBeanReq {

    private String shareUid;        // 分享用户-设备关联ID（分享的自增id）
    private String uid;            // 用户id

    public String getShareUid() {
        return shareUid;
    }

    public void setShareUid(String shareUid) {
        this.shareUid = shareUid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
