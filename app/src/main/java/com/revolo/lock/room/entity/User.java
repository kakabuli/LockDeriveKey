package com.revolo.lock.room.entity;

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
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "u_id")
    private long id;                                 // 自增长id

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
}
