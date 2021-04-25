package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class CheckDoorSensorStateBeanRsp implements Parcelable {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1610093348
     * data : {"_id":"5ff6ad2c2a292e5fbc913708","magneticStatus":1}
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
         * _id : 5ff6ad2c2a292e5fbc913708
         * magneticStatus : 1
         */

        private String _id;
        private int magneticStatus;          // 门磁状态：1开 2关 todo showDoc上字段名字不一样，需要确认

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public int getMagneticStatus() {
            return magneticStatus;
        }

        public void setMagneticStatus(int magneticStatus) {
            this.magneticStatus = magneticStatus;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this._id);
            dest.writeInt(this.magneticStatus);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this._id = in.readString();
            this.magneticStatus = in.readInt();
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

    public CheckDoorSensorStateBeanRsp() {
    }

    protected CheckDoorSensorStateBeanRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.nowTime = in.readInt();
        this.data = in.readParcelable(DataBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<CheckDoorSensorStateBeanRsp> CREATOR = new Parcelable.Creator<CheckDoorSensorStateBeanRsp>() {
        @Override
        public CheckDoorSensorStateBeanRsp createFromParcel(Parcel source) {
            return new CheckDoorSensorStateBeanRsp(source);
        }

        @Override
        public CheckDoorSensorStateBeanRsp[] newArray(int size) {
            return new CheckDoorSensorStateBeanRsp[size];
        }
    };
}
