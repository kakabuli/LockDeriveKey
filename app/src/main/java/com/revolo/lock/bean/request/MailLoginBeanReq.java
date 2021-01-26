package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/26
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class MailLoginBeanReq implements Parcelable {


    /**
     * mail : chason666@163.com
     * password : ll123654
     */

    private String mail;
    private String password;

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mail);
        dest.writeString(this.password);
    }

    public MailLoginBeanReq() {
    }

    protected MailLoginBeanReq(Parcel in) {
        this.mail = in.readString();
        this.password = in.readString();
    }

    public static final Parcelable.Creator<MailLoginBeanReq> CREATOR = new Parcelable.Creator<MailLoginBeanReq>() {
        @Override
        public MailLoginBeanReq createFromParcel(Parcel source) {
            return new MailLoginBeanReq(source);
        }

        @Override
        public MailLoginBeanReq[] newArray(int size) {
            return new MailLoginBeanReq[size];
        }
    };
}
