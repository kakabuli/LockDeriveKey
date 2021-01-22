package com.revolo.lock.ble.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class WifiSnBean implements Parcelable {

    private int rssi;
    private String wifiSn;
    private int wifiIndex;
    private List<WifiSnBytesBean> mWifiSnBytesBeans;

    public static class WifiSnBytesBean implements Parcelable {
        private int index;
        private byte[] bytes;
        private int snLenTotal;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public int getSnLenTotal() {
            return snLenTotal;
        }

        public void setSnLenTotal(int snLenTotal) {
            this.snLenTotal = snLenTotal;
        }

        public WifiSnBytesBean(int index, int snLenTotal) {
            this.index = index;
            this.snLenTotal = snLenTotal;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.index);
            dest.writeByteArray(this.bytes);
            dest.writeInt(this.snLenTotal);
        }

        protected WifiSnBytesBean(Parcel in) {
            this.index = in.readInt();
            this.bytes = in.createByteArray();
            this.snLenTotal = in.readInt();
        }

        public static final Parcelable.Creator<WifiSnBytesBean> CREATOR = new Parcelable.Creator<WifiSnBytesBean>() {
            @Override
            public WifiSnBytesBean createFromParcel(Parcel source) {
                return new WifiSnBytesBean(source);
            }

            @Override
            public WifiSnBytesBean[] newArray(int size) {
                return new WifiSnBytesBean[size];
            }
        };
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getWifiSn() {
        if(wifiSn == null) {
            if(mWifiSnBytesBeans == null || mWifiSnBytesBeans.isEmpty()) return wifiSn;
            byte[] bytes = new byte[mWifiSnBytesBeans.get(0).getSnLenTotal()];
            for (WifiSnBytesBean wifi : mWifiSnBytesBeans) {
                if(wifi.getIndex() == (mWifiSnBytesBeans.size()-1)) {
                    // 最后一包
                    int remainLen = wifi.getSnLenTotal()%11;
                    System.arraycopy(wifi.getBytes(), 0, bytes, wifi.getIndex()*11, remainLen);
                } else {
                    System.arraycopy(wifi.getBytes(), 0, bytes, wifi.getIndex()*11, 11);
                }
            }
            setWifiSn(new String(bytes));
        }
        return wifiSn;
    }

    public void setWifiSn(String wifiSn) {
        this.wifiSn = wifiSn;
    }

    public int getWifiIndex() {
        return wifiIndex;
    }

    public void setWifiIndex(int wifiIndex) {
        this.wifiIndex = wifiIndex;
    }

    public List<WifiSnBytesBean> getWifiSnBytesBeans() {
        return mWifiSnBytesBeans;
    }

    public void setWifiSnBytesBeans(List<WifiSnBytesBean> wifiSnBytesBeans) {
        mWifiSnBytesBeans = wifiSnBytesBeans;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.rssi);
        dest.writeString(this.wifiSn);
        dest.writeInt(this.wifiIndex);
        dest.writeTypedList(this.mWifiSnBytesBeans);
    }

    public WifiSnBean() {
    }

    protected WifiSnBean(Parcel in) {
        this.rssi = in.readInt();
        this.wifiSn = in.readString();
        this.wifiIndex = in.readInt();
        this.mWifiSnBytesBeans = in.createTypedArrayList(WifiSnBytesBean.CREATOR);
    }

    public static final Parcelable.Creator<WifiSnBean> CREATOR = new Parcelable.Creator<WifiSnBean>() {
        @Override
        public WifiSnBean createFromParcel(Parcel source) {
            return new WifiSnBean(source);
        }

        @Override
        public WifiSnBean[] newArray(int size) {
            return new WifiSnBean[size];
        }
    };
}
