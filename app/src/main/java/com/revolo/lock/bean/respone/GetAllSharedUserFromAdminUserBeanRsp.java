package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 获取管理员下的所有分享用户回调实体
 */
public class GetAllSharedUserFromAdminUserBeanRsp {

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

        private String nickName;
        private String avatarPath;
        private String shareUid;
        private String userMail;
        private String deviceCount;
        private String firstName;
        private String lastName;

        protected DataBean(Parcel in) {
            nickName = in.readString();
            avatarPath = in.readString();
            shareUid = in.readString();
            userMail = in.readString();
            deviceCount = in.readString();
            firstName = in.readString();
            lastName = in.readString();
        }

        public static final Creator<DataBean> CREATOR = new Creator<DataBean>() {
            @Override
            public DataBean createFromParcel(Parcel in) {
                return new DataBean(in);
            }

            @Override
            public DataBean[] newArray(int size) {
                return new DataBean[size];
            }
        };

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getAvatarPath() {
            return avatarPath;
        }

        public void setAvatarPath(String avatarPath) {
            this.avatarPath = avatarPath;
        }

        public String getShareUid() {
            return shareUid;
        }

        public void setShareUid(String shareUid) {
            this.shareUid = shareUid;
        }

        public String getUserMail() {
            return userMail;
        }

        public void setUserMail(String userMail) {
            this.userMail = userMail;
        }

        public String getDeviceCount() {
            return deviceCount;
        }

        public void setDeviceCount(String deviceCount) {
            this.deviceCount = deviceCount;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(nickName);
            dest.writeString(avatarPath);
            dest.writeString(shareUid);
            dest.writeString(userMail);
            dest.writeString(deviceCount);
            dest.writeString(firstName);
            dest.writeString(lastName);
        }
    }
}
