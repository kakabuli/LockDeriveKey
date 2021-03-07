package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class ChangeUserPwdBeanReq {


    /**
     * uid : 5c4fe492dc93897aa7d8600b
     * newpwd : dd81211
     * oldpwd : bds4545
     */

    private String uid;
    private String newpwd;
    private String oldpwd;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNewpwd() {
        return newpwd;
    }

    public void setNewpwd(String newpwd) {
        this.newpwd = newpwd;
    }

    public String getOldpwd() {
        return oldpwd;
    }

    public void setOldpwd(String oldpwd) {
        this.oldpwd = oldpwd;
    }
}
