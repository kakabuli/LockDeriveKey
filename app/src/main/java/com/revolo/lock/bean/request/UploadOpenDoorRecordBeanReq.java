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
public class UploadOpenDoorRecordBeanReq implements Parcelable {


    private List<OpenLockListBean> openLockList;

    public List<OpenLockListBean> getOpenLockList() {
        return openLockList;
    }

    public void setOpenLockList(List<OpenLockListBean> openLockList) {
        this.openLockList = openLockList;
    }

    public static class OpenLockListBean implements Parcelable {
        /**
         * sn : WF132231004
         * uid : 5c4fe492dc93897aa7d8600b
         * pwdType : 2
         * pwdNum : 1
         * pwdNickname : ererereree
         * time : 1578377588
         */

        private String sn;
        private String uid;
        private int pwdType;
        private int pwdNum;
        private String pwdNickname;
        private int time;

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public int getPwdType() {
            return pwdType;
        }

        public void setPwdType(int pwdType) {
            this.pwdType = pwdType;
        }

        public int getPwdNum() {
            return pwdNum;
        }

        public void setPwdNum(int pwdNum) {
            this.pwdNum = pwdNum;
        }

        public String getPwdNickname() {
            return pwdNickname;
        }

        public void setPwdNickname(String pwdNickname) {
            this.pwdNickname = pwdNickname;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.sn);
            dest.writeString(this.uid);
            dest.writeInt(this.pwdType);
            dest.writeInt(this.pwdNum);
            dest.writeString(this.pwdNickname);
            dest.writeInt(this.time);
        }

        public OpenLockListBean() {
        }

        protected OpenLockListBean(Parcel in) {
            this.sn = in.readString();
            this.uid = in.readString();
            this.pwdType = in.readInt();
            this.pwdNum = in.readInt();
            this.pwdNickname = in.readString();
            this.time = in.readInt();
        }

        public static final Parcelable.Creator<OpenLockListBean> CREATOR = new Parcelable.Creator<OpenLockListBean>() {
            @Override
            public OpenLockListBean createFromParcel(Parcel source) {
                return new OpenLockListBean(source);
            }

            @Override
            public OpenLockListBean[] newArray(int size) {
                return new OpenLockListBean[size];
            }
        };
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.openLockList);
    }

    public UploadOpenDoorRecordBeanReq() {
    }

    protected UploadOpenDoorRecordBeanReq(Parcel in) {
        this.openLockList = in.createTypedArrayList(OpenLockListBean.CREATOR);
    }

    public static final Parcelable.Creator<UploadOpenDoorRecordBeanReq> CREATOR = new Parcelable.Creator<UploadOpenDoorRecordBeanReq>() {
        @Override
        public UploadOpenDoorRecordBeanReq createFromParcel(Parcel source) {
            return new UploadOpenDoorRecordBeanReq(source);
        }

        @Override
        public UploadOpenDoorRecordBeanReq[] newArray(int size) {
            return new UploadOpenDoorRecordBeanReq[size];
        }
    };
}
