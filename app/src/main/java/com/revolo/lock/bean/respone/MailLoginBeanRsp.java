package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/26
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class MailLoginBeanRsp implements Parcelable {


    /**
     * code : 200
     * msg : 成功
     * data : {"uid":"5c6fb4d014fd214910b33e80","token":"eyJfaWQiOiI1YzZmYjRkMDE0ZmQyMTQ5MTBiMzNlODAiLCJ1c2VybmFtZSI6Ijg2MTg5NTQzNTk4MjIiLCJpYXQiOjE1NTA4ODgyMDl9","meUsername":"","mePwd":"","storeToken":"eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NDgiLCJpc3MiOiJodHRwczovL3d3dy5rYW5nYXJvb2JhYnljYXIuY29tIiwiaWF0IjoxNTY5ODA4MTk2fQ.nim1clpDSCInVFekcoy9ZfUK5fzgJWik85RietVaRlc"}
     */

    private String code;
    private String msg;
    private DataBean data;

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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean implements Parcelable {
        /**
         * uid : 5c6fb4d014fd214910b33e80
         * token : eyJfaWQiOiI1YzZmYjRkMDE0ZmQyMTQ5MTBiMzNlODAiLCJ1c2VybmFtZSI6Ijg2MTg5NTQzNTk4MjIiLCJpYXQiOjE1NTA4ODgyMDl9
         * meUsername :
         * mePwd :
         * storeToken : eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NDgiLCJpc3MiOiJodHRwczovL3d3dy5rYW5nYXJvb2JhYnljYXIuY29tIiwiaWF0IjoxNTY5ODA4MTk2fQ.nim1clpDSCInVFekcoy9ZfUK5fzgJWik85RietVaRlc
         */

        private String uid;
        private String token;
        private String meUsername;
        private String mePwd;
        private String storeToken;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getMeUsername() {
            return meUsername;
        }

        public void setMeUsername(String meUsername) {
            this.meUsername = meUsername;
        }

        public String getMePwd() {
            return mePwd;
        }

        public void setMePwd(String mePwd) {
            this.mePwd = mePwd;
        }

        public String getStoreToken() {
            return storeToken;
        }

        public void setStoreToken(String storeToken) {
            this.storeToken = storeToken;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.uid);
            dest.writeString(this.token);
            dest.writeString(this.meUsername);
            dest.writeString(this.mePwd);
            dest.writeString(this.storeToken);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this.uid = in.readString();
            this.token = in.readString();
            this.meUsername = in.readString();
            this.mePwd = in.readString();
            this.storeToken = in.readString();
        }

        public static final Parcelable.Creator<DataBean> CREATOR = new Parcelable.Creator<DataBean>() {
            @Override
            public DataBean createFromParcel(Parcel source) {
                return new DataBean(source);
            }

            @Override
            public DataBean[] newArray(int size) {
                return new DataBean[size];
            }
        };
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.msg);
        dest.writeParcelable(this.data, flags);
    }

    public MailLoginBeanRsp() {
    }

    protected MailLoginBeanRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.data = in.readParcelable(DataBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<MailLoginBeanRsp> CREATOR = new Parcelable.Creator<MailLoginBeanRsp>() {
        @Override
        public MailLoginBeanRsp createFromParcel(Parcel source) {
            return new MailLoginBeanRsp(source);
        }

        @Override
        public MailLoginBeanRsp[] newArray(int size) {
            return new MailLoginBeanRsp[size];
        }
    };
}
