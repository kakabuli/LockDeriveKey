package com.revolo.lock.bean.showBean;

import android.os.Parcel;
import android.os.Parcelable;

import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockGetAllBindDeviceRspBean;

/**
 * author :
 * time   : 2021/2/6
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class WifiShowBean implements Parcelable {

    private int doorState;        // 门的状态 1: 开门, 2: 关门
    private int internetState;    // 网络状态 1: Wifi, 2: 蓝牙
    private int modeState;        // 权限状态 1: 全模式 2: 隐私模式
    private WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean  mWifiListBean;

    public int getDoorState() {
        return doorState;
    }

    public void setDoorState(int doorState) {
        this.doorState = doorState;
    }

    public int getInternetState() {
        return internetState;
    }

    public void setInternetState(int internetState) {
        this.internetState = internetState;
    }

    public WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean getWifiListBean() {
        return mWifiListBean;
    }

    public void setWifiListBean(WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean) {
        mWifiListBean = wifiListBean;
    }

    public int getModeState() {
        return modeState;
    }

    public void setModeState(int modeState) {
        this.modeState = modeState;
    }

    public WifiShowBean(int doorState, int internetState, int modeState, WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean) {
        this.doorState = doorState;
        this.internetState = internetState;
        this.modeState = modeState;
        mWifiListBean = wifiListBean;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.doorState);
        dest.writeInt(this.internetState);
        dest.writeInt(this.modeState);
        dest.writeParcelable(this.mWifiListBean, flags);
    }

    protected WifiShowBean(Parcel in) {
        this.doorState = in.readInt();
        this.internetState = in.readInt();
        this.modeState = in.readInt();
        this.mWifiListBean = in.readParcelable(WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<WifiShowBean> CREATOR = new Parcelable.Creator<WifiShowBean>() {
        @Override
        public WifiShowBean createFromParcel(Parcel source) {
            return new WifiShowBean(source);
        }

        @Override
        public WifiShowBean[] newArray(int size) {
            return new WifiShowBean[size];
        }
    };
}
