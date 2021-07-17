package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 创建分享链接请求实体
 */
public class GainKeyBeanReq {


    /**
     * uid : 5c4fe492dc93897aa7d8600b
     * deviceSN : WPF00000123
     * shareUserType : 1
     * openPurview : 1
     * startTime : 2019-12-12 14:00:00
     * endTime : 2019-12-12 15:00:00
     */

    private String adminUid;             // 用户uid
    private String deviceSN;        // 设备SN
    private int shareUserType;      // 分享用户类型。1为 family 用户，2为 guest 用户
    private String shareAccount;   // 用户昵称
    private String firstName;
    private String lastName;

    public String getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(String adminUid) {
        this.adminUid = adminUid;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public int getShareUserType() {
        return shareUserType;
    }

    public void setShareUserType(int shareUserType) {
        this.shareUserType = shareUserType;
    }

    public String getShareAccount() {
        return shareAccount;
    }

    public void setShareAccount(String shareAccount) {
        this.shareAccount = shareAccount;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
