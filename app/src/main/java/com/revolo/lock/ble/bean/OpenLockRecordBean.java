package com.revolo.lock.ble.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/9
 * E-mail : wengmaowei@kaadas.com
 * desc   : 锁开锁记录
 */
public class OpenLockRecordBean implements Parcelable {

    private int mTotal;
    private int mIndex;
    private int mEventType;
    private int mEvenSource;
    private int mEventCode;
    private int mUserID;
    private long mLocalTime;

    public OpenLockRecordBean(int total, int index, int eventType, int evenSource, int eventCode, int userID, long localTime) {
        mTotal = total;
        mIndex = index;
        mEventType = eventType;
        mEvenSource = evenSource;
        mEventCode = eventCode;
        mUserID = userID;
        mLocalTime = localTime;
    }

    public int getTotal() {
        return mTotal;
    }

    public void setTotal(int total) {
        mTotal = total;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public int getEventType() {
        return mEventType;
    }

    public void setEventType(int eventType) {
        mEventType = eventType;
    }

    public int getEvenSource() {
        return mEvenSource;
    }

    public void setEvenSource(int evenSource) {
        mEvenSource = evenSource;
    }

    public int getEventCode() {
        return mEventCode;
    }

    public void setEventCode(int eventCode) {
        mEventCode = eventCode;
    }

    public int getUserID() {
        return mUserID;
    }

    public void setUserID(int userID) {
        mUserID = userID;
    }

    public long getLocalTime() {
        return mLocalTime;
    }

    public void setLocalTime(long localTime) {
        mLocalTime = localTime;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTotal);
        dest.writeInt(this.mIndex);
        dest.writeInt(this.mEventType);
        dest.writeInt(this.mEvenSource);
        dest.writeInt(this.mEventCode);
        dest.writeInt(this.mUserID);
        dest.writeLong(this.mLocalTime);
    }

    protected OpenLockRecordBean(Parcel in) {
        this.mTotal = in.readInt();
        this.mIndex = in.readInt();
        this.mEventType = in.readInt();
        this.mEvenSource = in.readInt();
        this.mEventCode = in.readInt();
        this.mUserID = in.readInt();
        this.mLocalTime = in.readLong();
    }

    public static final Parcelable.Creator<OpenLockRecordBean> CREATOR = new Parcelable.Creator<OpenLockRecordBean>() {
        @Override
        public OpenLockRecordBean createFromParcel(Parcel source) {
            return new OpenLockRecordBean(source);
        }

        @Override
        public OpenLockRecordBean[] newArray(int size) {
            return new OpenLockRecordBean[size];
        }
    };
}
