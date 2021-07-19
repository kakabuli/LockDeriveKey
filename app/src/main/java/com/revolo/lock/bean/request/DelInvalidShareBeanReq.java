package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 删除无效分享链接请求实体
 */
public class DelInvalidShareBeanReq {


    /**
     * shareId : 5def586f4d3ee1156123456
     */

    private String shareId;    // 分享用户-设备关联ID（分享的自增id）

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }
}
