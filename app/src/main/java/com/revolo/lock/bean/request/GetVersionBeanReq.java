package com.revolo.lock.bean.request;

/**
 * author : zhougm
 * time   : 2021/7/12
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class GetVersionBeanReq {

    private String uid;
    private int phoneSysType;

    public int getPhoneSysType() {
        return phoneSysType;
    }

    public void setPhoneSysType(int phoneSysType) {
        this.phoneSysType = phoneSysType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
