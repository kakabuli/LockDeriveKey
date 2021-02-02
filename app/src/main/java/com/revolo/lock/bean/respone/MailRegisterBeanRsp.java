package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/2/2
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class MailRegisterBeanRsp implements Parcelable {


    /**
     * code : 200
     * msg : 成功
     * data : {"token":"eyJfaWQiOiI1YzcwYWMwNTNjNTU0NjM5ZWE5M2NjODUiLCJ1c2VybmFtZSI6Ijg2MTg5NTQzNTk4MjQiLCJpYXQiOjE1NTA4ODc5NDF9","uid":"5c70ac053c554639ea93cc85","storeToken":"eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NTAiLCJpc3MiOiJodHRwczovL3d3dy5rYW5nYXJvb2JhYnljYXIuY29tIiwiaWF0IjoxNTY5NzQ2MTQwfQ.3uoz632-uN20cpAnz9M2BqCVxysUwV2q6Jgma8y8FrM"}
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
         * token : eyJfaWQiOiI1YzcwYWMwNTNjNTU0NjM5ZWE5M2NjODUiLCJ1c2VybmFtZSI6Ijg2MTg5NTQzNTk4MjQiLCJpYXQiOjE1NTA4ODc5NDF9
         * uid : 5c70ac053c554639ea93cc85
         * storeToken : eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NTAiLCJpc3MiOiJodHRwczovL3d3dy5rYW5nYXJvb2JhYnljYXIuY29tIiwiaWF0IjoxNTY5NzQ2MTQwfQ.3uoz632-uN20cpAnz9M2BqCVxysUwV2q6Jgma8y8FrM
         */

        private String token;
        private String uid;
        private String storeToken;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
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
            dest.writeString(this.token);
            dest.writeString(this.uid);
            dest.writeString(this.storeToken);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this.token = in.readString();
            this.uid = in.readString();
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

    public MailRegisterBeanRsp() {
    }

    protected MailRegisterBeanRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.data = in.readParcelable(DataBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<MailRegisterBeanRsp> CREATOR = new Parcelable.Creator<MailRegisterBeanRsp>() {
        @Override
        public MailRegisterBeanRsp createFromParcel(Parcel source) {
            return new MailRegisterBeanRsp(source);
        }

        @Override
        public MailRegisterBeanRsp[] newArray(int size) {
            return new MailRegisterBeanRsp[size];
        }
    };
}
