package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class LockIsBindBeanRsp implements Parcelable {


    /**
     * code : 202
     * msg : 已绑定
     * nowTime : 1561630357
     * data : {"_id":"5cf34165457411492b74da11","adminname":"8613786399316"}
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
         * _id : 5cf34165457411492b74da11
         * adminname : 8613786399316
         */

        private String _id;
        private String adminname;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getAdminname() {
            return adminname;
        }

        public void setAdminname(String adminname) {
            this.adminname = adminname;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this._id);
            dest.writeString(this.adminname);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this._id = in.readString();
            this.adminname = in.readString();
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

    public LockIsBindBeanRsp() {
    }

    protected LockIsBindBeanRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.nowTime = in.readInt();
        this.data = in.readParcelable(DataBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<LockIsBindBeanRsp> CREATOR = new Parcelable.Creator<LockIsBindBeanRsp>() {
        @Override
        public LockIsBindBeanRsp createFromParcel(Parcel source) {
            return new LockIsBindBeanRsp(source);
        }

        @Override
        public LockIsBindBeanRsp[] newArray(int size) {
            return new LockIsBindBeanRsp[size];
        }
    };
}
