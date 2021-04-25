package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/12
 * E-mail : wengmaowei@kaadas.com
 * desc   : 设置/修改用户名称请求实体
 */
public class UpdateUserFirstLastNameBeanReq {


    /**
     * uid : 60459a490423e437d2c01ccc
     * firstName : 666
     * lastName : 888
     */

    private String uid;          // 用户ID
    private String firstName;    // 名
    private String lastName;     // 姓

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
