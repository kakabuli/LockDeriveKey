package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/3/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 获取分享用户的设备列表回调实体
 */
public class GetDevicesFromUidAndSharedUidBeanRsp {

    private String code;
    private String msg;
    private int nowTime;
    private List<GetAllSharedUserFromLockBeanRsp.DataBean> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getNowTime() {
        return nowTime;
    }

    public void setNowTime(int nowTime) {
        this.nowTime = nowTime;
    }

    public List<GetAllSharedUserFromLockBeanRsp.DataBean> getData() {
        return data;
    }

    public void setData(List<GetAllSharedUserFromLockBeanRsp.DataBean> data) {
        this.data = data;
    }

}
