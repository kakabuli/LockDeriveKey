package com.revolo.lock.room.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
@Entity
public class User implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "u_id")
    private long id;                                // 自增长id

    @ColumnInfo(name = "u_mail")
    private String mail;                            // 用户邮箱

    @ColumnInfo(name = "u_user_name")
    private String userName;                        // 用户名字

    @ColumnInfo(name = "u_register_time")
    private long registerTime;                      // 注册时间

    @ColumnInfo(name = "u_first_name")
    private String firstName;                       // 名

    @ColumnInfo(name = "u_last_name")
    private String lastName;                        // 姓

    @ColumnInfo(name = "u_avatar_url")
    private String avatarUrl;                       // 头像地址

    @ColumnInfo(name = "u_is_use_gesture_password", defaultValue = "false")
    private boolean isUseGesturePassword;           // 是否使用手势密码

    @ColumnInfo(name = "u_is_use_face_id", defaultValue = "false")
    private boolean isUseFaceId;                    // 是否使用faceId

    @ColumnInfo(name = "u_is_use_touch_id", defaultValue = "false")
    private boolean isUseTouchId;                   // 是否使用指纹

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isUseGesturePassword() {
        return isUseGesturePassword;
    }

    public void setUseGesturePassword(boolean useGesturePassword) {
        isUseGesturePassword = useGesturePassword;
    }

    public boolean isUseFaceId() {
        return isUseFaceId;
    }

    public void setUseFaceId(boolean useFaceId) {
        isUseFaceId = useFaceId;
    }

    public boolean isUseTouchId() {
        return isUseTouchId;
    }

    public void setUseTouchId(boolean useTouchId) {
        isUseTouchId = useTouchId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.mail);
        dest.writeString(this.userName);
        dest.writeLong(this.registerTime);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.avatarUrl);
        dest.writeByte(this.isUseGesturePassword ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isUseFaceId ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isUseTouchId ? (byte) 1 : (byte) 0);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.id = in.readLong();
        this.mail = in.readString();
        this.userName = in.readString();
        this.registerTime = in.readLong();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.avatarUrl = in.readString();
        this.isUseGesturePassword = in.readByte() != 0;
        this.isUseFaceId = in.readByte() != 0;
        this.isUseTouchId = in.readByte() != 0;
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
