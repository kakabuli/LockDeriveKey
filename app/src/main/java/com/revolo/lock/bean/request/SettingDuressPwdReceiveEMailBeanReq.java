package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class SettingDuressPwdReceiveEMailBeanReq {


    /**
     * name : chason666@163.com
     * type : 1
     * duressEmail : chason888@163.com
     */

    private String name;
    private int type;
    private String duressEmail;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
