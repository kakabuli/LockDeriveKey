package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 设置胁迫密码邮箱请求实体
 */
public class SettingDuressPwdReceiveEMailBeanReq {


    /**
     * uid : 5dd79ca14089fb3d74b31fb9
     * type : 1
     * duressEmail : chason888@163.com
     */

    private String uid;             // 用户唯一编号
    private int type;               // 账号类型。 1 手机；2 邮箱
    private String duressEmail;     // 胁迫密码邮箱
    private String deviceSN;        //设备esn

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

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }
}
