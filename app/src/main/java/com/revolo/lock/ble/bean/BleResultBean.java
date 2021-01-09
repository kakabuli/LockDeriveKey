package com.revolo.lock.ble.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;

import org.jetbrains.annotations.NotNull;

/**
 * author : Jack
 * time   : 2021/1/9
 * E-mail : wengmaowei@kaadas.com
 * desc   : 蓝牙返回数据通用bean
 */
public class BleResultBean implements Parcelable {

    private int mControl;
    private int mTSN;
    private int mCMD;
    private byte[] mSrcPayload;
    private byte[] mPayload;
    private BLEScanResult mScanResult;

    public BleResultBean(int control, int TSN, int CMD, byte[] srcPayload, byte[] payload, BLEScanResult scanResult) {
        mControl = control;
        mTSN = TSN;
        mCMD = CMD;
        mSrcPayload = srcPayload;
        mPayload = payload;
        mScanResult = scanResult;
    }

    public int getControl() {
        return mControl;
    }

    public void setControl(int control) {
        mControl = control;
    }

    public int getTSN() {
        return mTSN;
    }

    public void setTSN(int TSN) {
        mTSN = TSN;
    }

    public int getCMD() {
        return mCMD;
    }

    public void setCMD(int CMD) {
        mCMD = CMD;
    }

    public byte[] getSrcPayload() {
        return mSrcPayload;
    }

    public void setSrcPayload(byte[] srcPayload) {
        mSrcPayload = srcPayload;
    }

    public byte[] getPayload() {
        return mPayload;
    }

    public void setPayload(byte[] payload) {
        mPayload = payload;
    }

    public BLEScanResult getScanResult() {
        return mScanResult;
    }

    public void setScanResult(BLEScanResult scanResult) {
        mScanResult = scanResult;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mControl);
        dest.writeInt(this.mTSN);
        dest.writeInt(this.mCMD);
        dest.writeByteArray(this.mSrcPayload);
        dest.writeByteArray(this.mPayload);
        dest.writeParcelable(this.mScanResult, flags);
    }

    protected BleResultBean(Parcel in) {
        this.mControl = in.readInt();
        this.mTSN = in.readInt();
        this.mCMD = in.readInt();
        this.mSrcPayload = in.createByteArray();
        this.mPayload = in.createByteArray();
        this.mScanResult = in.readParcelable(BLEScanResult.class.getClassLoader());
    }

    public static final Parcelable.Creator<BleResultBean> CREATOR = new Parcelable.Creator<BleResultBean>() {
        @Override
        public BleResultBean createFromParcel(Parcel source) {
            return new BleResultBean(source);
        }

        @Override
        public BleResultBean[] newArray(int size) {
            return new BleResultBean[size];
        }
    };

    @Override
    public @NotNull String toString() {
        return "BleResultBean{" +
                "mControl=" + mControl +
                ", mTSN=" + mTSN +
                ", mCMD=" + mCMD +
                ", mSrcPayload=" + ConvertUtils.bytes2HexString(mSrcPayload) +
                ", mPayload=" + ConvertUtils.bytes2HexString(mPayload) +
                ", mScanResult=" + mScanResult +
                '}';
    }
}
