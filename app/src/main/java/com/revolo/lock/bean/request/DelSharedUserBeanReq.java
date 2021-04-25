package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 删除分享用户请求实体
 */
public class DelSharedUserBeanReq {


    /**
     * shareId : 5def53704d3ee13934ffbeea
     * deviceSN : WPF00000123
     * uid : 5c4fe492dc93897aa7d8600b
     */

    private String shareId;        // 分享用户-设备关联ID（分享的自增id）
    private String deviceSN;       // 设备SN
    private String uid;            // 用户id
    private String shareNickName;  // 分享用户昵称


    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getShareNickName() {
        return shareNickName;
    }

    public void setShareNickName(String shareNickName) {
        this.shareNickName = shareNickName;
    }
}
