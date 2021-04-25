package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加密码回调实体
 */
public class LockKeyAddBeanRsp implements Parcelable {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1610003529
     * data : {"createTime":1610003529}
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
         * createTime : 1610003529
         */

        private int createTime;    // 添加时间（s）

        public int getCreateTime() {
            return createTime;
        }

        public void setCreateTime(int createTime) {
            this.createTime = createTime;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.createTime);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this.createTime = in.readInt();
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

    public LockKeyAddBeanRsp() {
    }

    protected LockKeyAddBeanRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.nowTime = in.readInt();
        this.data = in.readParcelable(DataBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<LockKeyAddBeanRsp> CREATOR = new Parcelable.Creator<LockKeyAddBeanRsp>() {
        @Override
        public LockKeyAddBeanRsp createFromParcel(Parcel source) {
            return new LockKeyAddBeanRsp(source);
        }

        @Override
        public LockKeyAddBeanRsp[] newArray(int size) {
            return new LockKeyAddBeanRsp[size];
        }
    };
}
