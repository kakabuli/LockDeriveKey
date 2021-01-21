package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class AdminAddDeviceBeanRsp implements Parcelable {


    /**
     * code : 202
     * msg : 已绑定
     * nowTime : 1561465086
     * data : {"uname":"8613786399316"}
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
         * uname : 8613786399316
         */

        private String uname;

        public String getUname() {
            return uname;
        }

        public void setUname(String uname) {
            this.uname = uname;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.uname);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this.uname = in.readString();
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

    public AdminAddDeviceBeanRsp() {
    }

    protected AdminAddDeviceBeanRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.nowTime = in.readInt();
        this.data = in.readParcelable(DataBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<AdminAddDeviceBeanRsp> CREATOR = new Parcelable.Creator<AdminAddDeviceBeanRsp>() {
        @Override
        public AdminAddDeviceBeanRsp createFromParcel(Parcel source) {
            return new AdminAddDeviceBeanRsp(source);
        }

        @Override
        public AdminAddDeviceBeanRsp[] newArray(int size) {
            return new AdminAddDeviceBeanRsp[size];
        }
    };
}
