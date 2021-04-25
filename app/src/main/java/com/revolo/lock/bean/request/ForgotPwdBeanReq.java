package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/3/6
 * E-mail : wengmaowei@kaadas.com
 * desc   : 忘记密码请求实体
 */
public class ForgotPwdBeanReq implements Parcelable {


    /**
     * name : 8618954359822
     * pwd : ll256808
     * type : 1
     * tokens : 673404
     */

    private String name;        // 账号
    private String pwd;         // 新密码
    private int type;           // 账号类型：1手机号码 2邮箱
    private String tokens;      // 验证码

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTokens() {
        return tokens;
    }

    public void setTokens(String tokens) {
        this.tokens = tokens;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.pwd);
        dest.writeInt(this.type);
        dest.writeString(this.tokens);
    }

    public ForgotPwdBeanReq() {
    }

    protected ForgotPwdBeanReq(Parcel in) {
        this.name = in.readString();
        this.pwd = in.readString();
        this.type = in.readInt();
        this.tokens = in.readString();
    }

    public static final Parcelable.Creator<ForgotPwdBeanReq> CREATOR = new Parcelable.Creator<ForgotPwdBeanReq>() {
        @Override
        public ForgotPwdBeanReq createFromParcel(Parcel source) {
            return new ForgotPwdBeanReq(source);
        }

        @Override
        public ForgotPwdBeanReq[] newArray(int size) {
            return new ForgotPwdBeanReq[size];
        }
    };
}
