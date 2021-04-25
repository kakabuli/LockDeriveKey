package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/26
 * E-mail : wengmaowei@kaadas.com
 * desc   : 邮箱登录回调实体
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

        private String uid;                        // 用户ID
        private String token;                      // 用户权限码
        /**
         * insertTime : 2021-03-08 03:30:17
         * firstName : 666
         * lastName : 888
         * userHead :
         */

        private String insertTime;                 // 存储时间
        private String firstName;                  // 名
        private String lastName;                   // 姓
        private String userHead;                   // 用户头像地址 todo 确认字段名是否变化了


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

        public String getInsertTime() {
            return insertTime;
        }

        public void setInsertTime(String insertTime) {
            this.insertTime = insertTime;
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

        public String getUserHead() {
            return userHead;
        }

        public void setUserHead(String userHead) {
            this.userHead = userHead;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.uid);
            dest.writeString(this.token);
            dest.writeString(this.insertTime);
            dest.writeString(this.firstName);
            dest.writeString(this.lastName);
            dest.writeString(this.userHead);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this.uid = in.readString();
            this.token = in.readString();
            this.insertTime = in.readString();
            this.firstName = in.readString();
            this.lastName = in.readString();
            this.userHead = in.readString();
        }

        public static final Creator<DataBean> CREATOR = new Creator<DataBean>() {
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
