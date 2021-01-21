package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class GetPwd1BeanRsp implements Parcelable {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1553158379
     * data : {"password1":"adfsadtrewrwqefewwef"}
     */

    private String code;
    private String msg;
    private int nowTime;
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

    public int getNowTime() {
        return nowTime;
    }

    public void setNowTime(int nowTime) {
        this.nowTime = nowTime;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean implements Parcelable {
        /**
         * password1 : adfsadtrewrwqefewwef
         */

        private String password1;

        public String getPassword1() {
            return password1;
        }

        public void setPassword1(String password1) {
            this.password1 = password1;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.password1);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this.password1 = in.readString();
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
        dest.writeInt(this.nowTime);
        dest.writeParcelable(this.data, flags);
    }

    public GetPwd1BeanRsp() {
    }

    protected GetPwd1BeanRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.nowTime = in.readInt();
        this.data = in.readParcelable(DataBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<GetPwd1BeanRsp> CREATOR = new Parcelable.Creator<GetPwd1BeanRsp>() {
        @Override
        public GetPwd1BeanRsp createFromParcel(Parcel source) {
            return new GetPwd1BeanRsp(source);
        }

        @Override
        public GetPwd1BeanRsp[] newArray(int size) {
            return new GetPwd1BeanRsp[size];
        }
    };
}
