package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/3/6
 * E-mail : wengmaowei@kaadas.com
 * desc   : 忘记密码回调实体
 */
public class ForgotPwdRsp implements Parcelable {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1576119176
     * data :
     */

    private String code;
    private String msg;
    private int nowTime;
    private String data;

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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.msg);
        dest.writeInt(this.nowTime);
        dest.writeString(this.data);
    }

    public ForgotPwdRsp() {
    }

    protected ForgotPwdRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.nowTime = in.readInt();
        this.data = in.readString();
    }

    public static final Parcelable.Creator<ForgotPwdRsp> CREATOR = new Parcelable.Creator<ForgotPwdRsp>() {
        @Override
        public ForgotPwdRsp createFromParcel(Parcel source) {
            return new ForgotPwdRsp(source);
        }

        @Override
        public ForgotPwdRsp[] newArray(int size) {
            return new ForgotPwdRsp[size];
        }
    };
}
