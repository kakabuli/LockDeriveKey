package com.revolo.lock.bean.test;

import android.os.Parcel;
import android.os.Parcelable;

import com.blankj.utilcode.util.TimeUtils;
import com.revolo.lock.bean.showBean.RecordState;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 锁操作记录
 */
public class OperationRecords implements Parcelable {

    private long titleOperationTime;
    private List<OperationRecord> operationRecords;

    public static class OperationRecord implements Parcelable {
        private long operationTime;
        private String message;
        // TODO: 2021/2/25 后面修改字段用于显示哪张图片
        @RecordState.OpRecordState
        private int state;
        private boolean isAlarmRecord;
        private String date;

        public OperationRecord(long operationTime, String message, @RecordState.OpRecordState int state, boolean isAlarmRecord) {
            this.operationTime = operationTime;
            this.message = message;
            this.state = state;
            this.isAlarmRecord = isAlarmRecord;
            date = TimeUtils.millis2String(operationTime, "yyyy-MM-dd");
        }

        public long getOperationTime() {
            return operationTime;
        }

        public void setOperationTime(long operationTime) {
            this.operationTime = operationTime;
            date = TimeUtils.millis2String(operationTime, "yyyy-MM-dd");
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getState() {
            return state;
        }

        public void setState(@RecordState.OpRecordState int state) {
            this.state = state;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public boolean isAlarmRecord() {
            return isAlarmRecord;
        }

        public void setAlarmRecord(boolean alarmRecord) {
            isAlarmRecord = alarmRecord;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.operationTime);
            dest.writeString(this.message);
            dest.writeInt(this.state);
            dest.writeByte(this.isAlarmRecord ? (byte) 1 : (byte) 0);
            dest.writeString(this.date);
        }

        protected OperationRecord(Parcel in) {
            this.operationTime = in.readLong();
            this.message = in.readString();
            this.state = in.readInt();
            this.isAlarmRecord = in.readByte() != 0;
            this.date = in.readString();
        }

        public static final Creator<OperationRecord> CREATOR = new Creator<OperationRecord>() {
            @Override
            public OperationRecord createFromParcel(Parcel source) {
                return new OperationRecord(source);
            }

            @Override
            public OperationRecord[] newArray(int size) {
                return new OperationRecord[size];
            }
        };
    }

    public OperationRecords(long titleOperationTime, List<OperationRecord> operationRecords) {
        this.titleOperationTime = titleOperationTime;
        this.operationRecords = operationRecords;
    }

    public long getTitleOperationTime() {
        return titleOperationTime;
    }

    public void setTitleOperationTime(long titleOperationTime) {
        this.titleOperationTime = titleOperationTime;
    }

    public List<OperationRecord> getOperationRecords() {
        return operationRecords;
    }

    public void setOperationRecords(List<OperationRecord> operationRecords) {
        this.operationRecords = operationRecords;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.titleOperationTime);
        dest.writeTypedList(this.operationRecords);
    }

    protected OperationRecords(Parcel in) {
        this.titleOperationTime = in.readLong();
        this.operationRecords = in.createTypedArrayList(OperationRecord.CREATOR);
    }

    public static final Parcelable.Creator<OperationRecords> CREATOR = new Parcelable.Creator<OperationRecords>() {
        @Override
        public OperationRecords createFromParcel(Parcel source) {
            return new OperationRecords(source);
        }

        @Override
        public OperationRecords[] newArray(int size) {
            return new OperationRecords[size];
        }
    };
}
