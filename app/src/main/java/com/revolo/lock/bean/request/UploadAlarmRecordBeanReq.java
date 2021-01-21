package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class UploadAlarmRecordBeanReq implements Parcelable {


    private List<AlarmListBean> alarmList;

    public List<AlarmListBean> getAlarmList() {
        return alarmList;
    }

    public void setAlarmList(List<AlarmListBean> alarmList) {
        this.alarmList = alarmList;
    }

    public static class AlarmListBean implements Parcelable {
        /**
         * sn : KV51203710172
         * type : 2
         * time : 1578377588
         * content :
         */

        private String sn;
        private int type;
        private int time;
        private String content;

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.sn);
            dest.writeInt(this.type);
            dest.writeInt(this.time);
            dest.writeString(this.content);
        }

        public AlarmListBean() {
        }

        protected AlarmListBean(Parcel in) {
            this.sn = in.readString();
            this.type = in.readInt();
            this.time = in.readInt();
            this.content = in.readString();
        }

        public static final Parcelable.Creator<AlarmListBean> CREATOR = new Parcelable.Creator<AlarmListBean>() {
            @Override
            public AlarmListBean createFromParcel(Parcel source) {
                return new AlarmListBean(source);
            }

            @Override
            public AlarmListBean[] newArray(int size) {
                return new AlarmListBean[size];
            }
        };
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.alarmList);
    }

    public UploadAlarmRecordBeanReq() {
    }

    protected UploadAlarmRecordBeanReq(Parcel in) {
        this.alarmList = in.createTypedArrayList(AlarmListBean.CREATOR);
    }

    public static final Parcelable.Creator<UploadAlarmRecordBeanReq> CREATOR = new Parcelable.Creator<UploadAlarmRecordBeanReq>() {
        @Override
        public UploadAlarmRecordBeanReq createFromParcel(Parcel source) {
            return new UploadAlarmRecordBeanReq(source);
        }

        @Override
        public UploadAlarmRecordBeanReq[] newArray(int size) {
            return new UploadAlarmRecordBeanReq[size];
        }
    };
}
