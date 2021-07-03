package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   : 判断锁是否被绑定
 */
public class AdminAddDeviceBeanReq implements Parcelable {


    /**
     * devmac : 0C:B2:B7:3F:A6:63
     * user_id : 5c70ac053c554639ea93cc85
     * password1 : 83D80FF2300435B0A3A4B074
     * password2 : 00d2a841
     * model : k9
     * deviceSN : BT01201410001
     * peripheralId : 5c70ac053c554639ea931111
     * bleVersionType : 1
     * bleVersion : 1.1.0
     * functionSet : 00
     * systemID : ss
     */

    private String devmac;                       // 设备mac
    private String user_id;                      // 用户ID
    private String password1;                    // 密码1
    private String password2;                    // 密码2
    private String model;                        // 型号
    private String deviceSN;                     // 设备SN,和wifiSN保持一致
    private String peripheralId;                 // ios蓝牙UUID
    private String bleVersionType;               // 蓝牙版本号：1 2 3
    private String bleVersion;                   // 固件版本号
    private String functionSet;                  // 功能集
    private String systemID;                     // 参数，扫描所得(现在的锁已经取消了这个)
    private String timeZone;                     //时区绑定

    public String getDevmac() {
        return devmac;
    }

    public void setDevmac(String devmac) {
        this.devmac = devmac;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getPeripheralId() {
        return peripheralId;
    }

    public void setPeripheralId(String peripheralId) {
        this.peripheralId = peripheralId;
    }

    public String getBleVersionType() {
        return bleVersionType;
    }

    public void setBleVersionType(String bleVersionType) {
        this.bleVersionType = bleVersionType;
    }

    public String getBleVersion() {
        return bleVersion;
    }

    public void setBleVersion(String bleVersion) {
        this.bleVersion = bleVersion;
    }

    public String getFunctionSet() {
        return functionSet;
    }

    public void setFunctionSet(String functionSet) {
        this.functionSet = functionSet;
    }

    public String getSystemID() {
        return systemID;
    }

    public void setSystemID(String systemID) {
        this.systemID = systemID;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.devmac);
        dest.writeString(this.user_id);
        dest.writeString(this.password1);
        dest.writeString(this.password2);
        dest.writeString(this.model);
        dest.writeString(this.deviceSN);
        dest.writeString(this.peripheralId);
        dest.writeString(this.bleVersionType);
        dest.writeString(this.bleVersion);
        dest.writeString(this.functionSet);
        dest.writeString(this.systemID);
        dest.writeString(this.timeZone);
    }

    public AdminAddDeviceBeanReq() {
    }

    protected AdminAddDeviceBeanReq(Parcel in) {
        this.devmac = in.readString();
        this.user_id = in.readString();
        this.password1 = in.readString();
        this.password2 = in.readString();
        this.model = in.readString();
        this.deviceSN = in.readString();
        this.peripheralId = in.readString();
        this.bleVersionType = in.readString();
        this.bleVersion = in.readString();
        this.functionSet = in.readString();
        this.systemID = in.readString();
        this.timeZone=in.readString();
    }

    public static final Parcelable.Creator<AdminAddDeviceBeanReq> CREATOR = new Parcelable.Creator<AdminAddDeviceBeanReq>() {
        @Override
        public AdminAddDeviceBeanReq createFromParcel(Parcel source) {
            return new AdminAddDeviceBeanReq(source);
        }

        @Override
        public AdminAddDeviceBeanReq[] newArray(int size) {
            return new AdminAddDeviceBeanReq[size];
        }
    };

    @Override
    public String toString() {
        return "AdminAddDeviceBeanReq{" +
                "devmac='" + devmac + '\'' +
                ", user_id='" + user_id + '\'' +
                ", password1='" + password1 + '\'' +
                ", password2='" + password2 + '\'' +
                ", model='" + model + '\'' +
                ", deviceSN='" + deviceSN + '\'' +
                ", peripheralId='" + peripheralId + '\'' +
                ", bleVersionType='" + bleVersionType + '\'' +
                ", bleVersion='" + bleVersion + '\'' +
                ", functionSet='" + functionSet + '\'' +
                ", systemID='" + systemID + '\'' +
                ", timeZone='" + timeZone + '\'' +
                '}';
    }
}
