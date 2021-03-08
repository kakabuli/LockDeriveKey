package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class SettingDuressPwdReceiveEMailBeanReq {


    /**
     * uid : 5dd79ca14089fb3d74b31fb9
     * type : 1
     * duressEmail : chason888@163.com
     */

    private String uid;
    private int type;
    private String duressEmail;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDuressEmail() {
        return duressEmail;
    }

    public void setDuressEmail(String duressEmail) {
        this.duressEmail = duressEmail;
    }

}
