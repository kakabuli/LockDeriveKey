package com.revolo.lock.bean.respone;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class SystemMessageListBeanRsp implements Parcelable {

    private String code;
    private String msg;
    private int nowTime;
    private List<DataBean> data;

    protected SystemMessageListBeanRsp(Parcel in) {
        code = in.readString();
        msg = in.readString();
        nowTime = in.readInt();
        data = in.createTypedArrayList(DataBean.CREATOR);
    }

    public static final Creator<SystemMessageListBeanRsp> CREATOR = new Creator<SystemMessageListBeanRsp>() {
        @Override
        public SystemMessageListBeanRsp createFromParcel(Parcel in) {
            return new SystemMessageListBeanRsp(in);
        }

        @Override
        public SystemMessageListBeanRsp[] newArray(int size) {
            return new SystemMessageListBeanRsp[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(msg);
        dest.writeInt(nowTime);
        dest.writeTypedList(data);
    }

    public static class DataBean implements Parcelable {
        private String _id;
        private String uid;
        private String alertTitle;
        private String alertBody;
        private String msgType;
        private String clientType;
        private String pushAt;

        protected DataBean(Parcel in) {
            _id = in.readString();
            uid = in.readString();
            alertTitle = in.readString();
            alertBody = in.readString();
            msgType = in.readString();
            clientType = in.readString();
            pushAt = in.readString();
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

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getAlertTitle() {
            return alertTitle;
        }

        public void setAlertTitle(String alertTitle) {
            this.alertTitle = alertTitle;
        }

        public String getAlertBody() {
            return alertBody;
        }

        public void setAlertBody(String alertBody) {
            this.alertBody = alertBody;
        }

        public String getMsgType() {
            return msgType;
        }

        public void setMsgType(String msgType) {
            this.msgType = msgType;
        }

        public String getClientType() {
            return clientType;
        }

        public void setClientType(String clientType) {
            this.clientType = clientType;
        }

        public String getPushAt() {
            return pushAt;
        }

        public void setPushAt(String pushAt) {
            this.pushAt = pushAt;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(_id);
            dest.writeString(uid);
            dest.writeString(alertTitle);
            dest.writeString(alertBody);
            dest.writeString(msgType);
            dest.writeString(clientType);
            dest.writeString(pushAt);
        }
    }
}
