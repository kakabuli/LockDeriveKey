package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 修改分享用户昵称请求实体
 */
public class UpdateSharedUserNickNameBeanReq {

    private String adminUId;
    private String shareUId;
    private String firstName;
    private String lastName;

    public String getAdminUId() {
        return adminUId;
    }

    public void setAdminUId(String adminUId) {
        this.adminUId = adminUId;
    }

    public String getShareUId() {
        return shareUId;
    }

    public void setShareUId(String shareUId) {
        this.shareUId = shareUId;
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
