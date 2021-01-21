package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class OpenDoorRecordSearchBeanRsp implements Parcelable {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1576655959
     * data : [{"_id":"5dde33754d27d6da12f51637","time":"1541468973","type":1,"wifiSN":"WF132231004","pwdNickname":"nickname","pwdType":4,"pwdNum":2,"createTime":157605853,"uid":"5c4fe492dc93897aa7d8600b","uname":"8618954359822","userNickname":"ahaha"}]
     */

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
        /**
         * _id : 5dde33754d27d6da12f51637
         * time : 1541468973
         * type : 1
         * wifiSN : WF132231004
         * pwdNickname : nickname
         * pwdType : 4
         * pwdNum : 2
         * createTime : 157605853
         * uid : 5c4fe492dc93897aa7d8600b
         * uname : 8618954359822
         * userNickname : ahaha
         */

        private String _id;
        private String time;
        private int type;
        private String wifiSN;
        private String pwdNickname;
        private int pwdType;
        private int pwdNum;
        private int createTime;
        private String uid;
        private String uname;
        private String userNickname;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getWifiSN() {
            return wifiSN;
        }

        public void setWifiSN(String wifiSN) {
            this.wifiSN = wifiSN;
        }

        public String getPwdNickname() {
            return pwdNickname;
        }

        public void setPwdNickname(String pwdNickname) {
            this.pwdNickname = pwdNickname;
        }

        public int getPwdType() {
            return pwdType;
        }

        public void setPwdType(int pwdType) {
            this.pwdType = pwdType;
        }

        public int getPwdNum() {
            return pwdNum;
        }

        public void setPwdNum(int pwdNum) {
            this.pwdNum = pwdNum;
        }

        public int getCreateTime() {
            return createTime;
        }

        public void setCreateTime(int createTime) {
            this.createTime = createTime;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getUname() {
            return uname;
        }

        public void setUname(String uname) {
            this.uname = uname;
        }

        public String getUserNickname() {
            return userNickname;
        }

        public void setUserNickname(String userNickname) {
            this.userNickname = userNickname;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this._id);
            dest.writeString(this.time);
            dest.writeInt(this.type);
            dest.writeString(this.wifiSN);
            dest.writeString(this.pwdNickname);
            dest.writeInt(this.pwdType);
            dest.writeInt(this.pwdNum);
            dest.writeInt(this.createTime);
            dest.writeString(this.uid);
            dest.writeString(this.uname);
            dest.writeString(this.userNickname);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this._id = in.readString();
            this.time = in.readString();
            this.type = in.readInt();
            this.wifiSN = in.readString();
            this.pwdNickname = in.readString();
            this.pwdType = in.readInt();
            this.pwdNum = in.readInt();
            this.createTime = in.readInt();
            this.uid = in.readString();
            this.uname = in.readString();
            this.userNickname = in.readString();
        }

        public static final Parcelable.Creator<DataBean> CREATOR = new Parcelable.Creator<DataBean>() {
            @Override
            public DataBean createFromParcel(Parcel source) {
                return new DataBean(source);
            }

            @Override
            public DataBean[] newArray(int size) {
                return new DataBean[size];
            }
        };
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.msg);
        dest.writeInt(this.nowTime);
        dest.writeTypedList(this.data);
    }

    public OpenDoorRecordSearchBeanRsp() {
    }

    protected OpenDoorRecordSearchBeanRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.nowTime = in.readInt();
        this.data = in.createTypedArrayList(DataBean.CREATOR);
    }

    public static final Parcelable.Creator<OpenDoorRecordSearchBeanRsp> CREATOR = new Parcelable.Creator<OpenDoorRecordSearchBeanRsp>() {
        @Override
        public OpenDoorRecordSearchBeanRsp createFromParcel(Parcel source) {
            return new OpenDoorRecordSearchBeanRsp(source);
        }

        @Override
        public OpenDoorRecordSearchBeanRsp[] newArray(int size) {
            return new OpenDoorRecordSearchBeanRsp[size];
        }
    };
}
