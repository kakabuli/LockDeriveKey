package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/3/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 获取锁下的所有分享用户列表回调实体
 */
public class GetAllSharedUserFromLockBeanRsp {

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
        private String lastName;
        private String shareId;
        private String shareUid;
        private String firstName;
        private int shareUserType;
        private int isAgree;
        private int isEnable;
        private String shareState;

        protected DataBean(Parcel in) {
            nickName = in.readString();
            avatarPath = in.readString();
            lastName = in.readString();
            shareId = in.readString();
            shareUid = in.readString();
            firstName = in.readString();
            shareUserType = in.readInt();
            isAgree = in.readInt();
            isEnable = in.readInt();
            shareState = in.readString();
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

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getShareId() {
            return shareId;
        }

        public void setShareId(String shareId) {
            this.shareId = shareId;
        }

        public String getShareUid() {
            return shareUid;
        }

        public void setShareUid(String shareUid) {
            this.shareUid = shareUid;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public int getShareUserType() {
            return shareUserType;
        }

        public void setShareUserType(int shareUserType) {
            this.shareUserType = shareUserType;
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
            dest.writeString(nickName);
            dest.writeString(avatarPath);
            dest.writeString(lastName);
            dest.writeString(shareId);
            dest.writeString(shareUid);
            dest.writeString(firstName);
            dest.writeInt(shareUserType);
            dest.writeInt(isAgree);
            dest.writeInt(isEnable);
            dest.writeString(shareState);
        }
    }
}
