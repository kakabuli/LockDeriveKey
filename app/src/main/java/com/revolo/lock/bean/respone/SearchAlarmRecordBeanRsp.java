package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class SearchAlarmRecordBeanRsp implements Parcelable {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1576656504
     * data : [{"_id":"5df0abf54d27d6da12fb4c71","time":"1541468973342","type":4,"wifiSN":"WF132231004","createTime":"1576054908866"}]
     */

    private String code;
    private String msg;
    private int nowTime;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean implements Parcelable {
        /**
         * _id : 5df0abf54d27d6da12fb4c71
         * time : 1541468973342
         * type : 4
         * wifiSN : WF132231004
         * createTime : 1576054908866
         */

        private String _id;
        private String time;
        private int type;
        private String wifiSN;
        private String createTime;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getWifiSN() {
            return wifiSN;
        }

        public void setWifiSN(String wifiSN) {
            this.wifiSN = wifiSN;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this._id);
            dest.writeString(this.time);
            dest.writeInt(this.type);
            dest.writeString(this.wifiSN);
            dest.writeString(this.createTime);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this._id = in.readString();
            this.time = in.readString();
            this.type = in.readInt();
            this.wifiSN = in.readString();
            this.createTime = in.readString();
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
        dest.writeTypedList(this.data);
    }

    public SearchAlarmRecordBeanRsp() {
    }

    protected SearchAlarmRecordBeanRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.nowTime = in.readInt();
        this.data = in.createTypedArrayList(DataBean.CREATOR);
    }

    public static final Parcelable.Creator<SearchAlarmRecordBeanRsp> CREATOR = new Parcelable.Creator<SearchAlarmRecordBeanRsp>() {
        @Override
        public SearchAlarmRecordBeanRsp createFromParcel(Parcel source) {
            return new SearchAlarmRecordBeanRsp(source);
        }

        @Override
        public SearchAlarmRecordBeanRsp[] newArray(int size) {
            return new SearchAlarmRecordBeanRsp[size];
        }
    };
}
