package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/3/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 获取分享用户的设备列表回调实体
 */
public class GetDevicesFromUidAndSharedUidBeanRsp {

    private String code;
    private String msg;
    private int nowTime;
    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

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

    public static class DataBean implements Parcelable, MultiItemEntity {

        private String lockNickname;
        private String deviceSN;
        private int shareUserType;
        private String shareId;
        private String lastName;
        private String firstName;
        private int isAgree;
        private int isEnable;
        private String shareState;

        public DataBean() {

        }

        protected DataBean(Parcel in) {
            lockNickname = in.readString();
            deviceSN = in.readString();
            shareUserType = in.readInt();
            shareId = in.readString();
            lastName = in.readString();
            firstName = in.readString();
            isAgree = in.readInt();
            isEnable = in.readInt();
            shareState = in.readString();
        }

        public final Creator<DataBean> CREATOR = new Creator<DataBean>() {
            @Override
            public DataBean createFromParcel(Parcel in) {
                return new DataBean(in);
            }

            @Override
            public DataBean[] newArray(int size) {
                return new DataBean[size];
            }
        };

        public String getLockNickname() {
            return lockNickname;
        }

        public void setLockNickname(String lockNickname) {
            this.lockNickname = lockNickname;
        }

        public String getDeviceSN() {
            return deviceSN;
        }

        public void setDeviceSN(String deviceSN) {
            this.deviceSN = deviceSN;
        }

        public int getShareUserType() {
            return shareUserType;
        }

        public void setShareUserType(int shareUserType) {
            this.shareUserType = shareUserType;
        }

        public String getShareId() {
            return shareId;
        }

        public void setShareId(String shareId) {
            this.shareId = shareId;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public int getIsAgree() {
            return isAgree;
        }

        public void setIsAgree(int isAgree) {
            this.isAgree = isAgree;
        }

        public int getIsEnable() {
            return isEnable;
        }

        public void setIsEnable(int isEnable) {
            this.isEnable = isEnable;
        }

        public String getShareState() {
            return shareState;
        }

        public void setShareState(String shareState) {
            this.shareState = shareState;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(lockNickname);
            dest.writeString(deviceSN);
            dest.writeInt(shareUserType);
            dest.writeString(shareId);
            dest.writeString(lastName);
            dest.writeString(firstName);
            dest.writeInt(isAgree);
            dest.writeInt(isEnable);
            dest.writeString(shareState);
        }

        @Override
        public int getItemType() {
            if (shareUserType == -1) {
                return 1;
            }
            return 0;
        }
    }
}
