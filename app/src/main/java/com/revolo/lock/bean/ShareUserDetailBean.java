package com.revolo.lock.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : zhougm
 * time   : 2021/7/28
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class ShareUserDetailBean implements Parcelable {

    private int shareUserType;
    private int isEnable;
    private String name;
    private String avatar;
    private String shareId;

    public ShareUserDetailBean() {

    }

    protected ShareUserDetailBean(Parcel in) {
        shareUserType = in.readInt();
        isEnable = in.readInt();
        name = in.readString();
        avatar = in.readString();
        shareId = in.readString();
    }

    public static final Creator<ShareUserDetailBean> CREATOR = new Creator<ShareUserDetailBean>() {
        @Override
        public ShareUserDetailBean createFromParcel(Parcel in) {
            return new ShareUserDetailBean(in);
        }

        @Override
        public ShareUserDetailBean[] newArray(int size) {
            return new ShareUserDetailBean[size];
        }
    };

    public int getShareUserType() {
        return shareUserType;
    }

    public void setShareUserType(int shareUserType) {
        this.shareUserType = shareUserType;
    }

    public int getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(int isEnable) {
        this.isEnable = isEnable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(shareUserType);
        dest.writeInt(isEnable);
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeString(shareId);
    }
}
