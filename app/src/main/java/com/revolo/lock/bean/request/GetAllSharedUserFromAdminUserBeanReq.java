package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 获取管理员下的所有分享用户请求实体
 */
public class GetAllSharedUserFromAdminUserBeanReq {


    /**
     * uid : 5c4fe492dc93897aa7d8600b
     */

    private String uid;       // 管理员ID

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
