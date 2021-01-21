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
public class GetLockKeyNickBeanRsp implements Parcelable {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1610003555
     * data : {"num":1,"nickName":"密码2","createTime":1610003529,"type":1,"startTime":1551774543,"endTime":1551774543,"items":["1","3"]}
     */

    private String code;
    private String msg;
    private int nowTime;
    private DataBean data;

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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean implements Parcelable {
        /**
         * num : 1
         * nickName : 密码2
         * createTime : 1610003529
         * type : 1
         * startTime : 1551774543
         * endTime : 1551774543
         * items : ["1","3"]
         */

        private int num;
        private String nickName;
        private int createTime;
        private int type;
        private int startTime;
        private int endTime;
        private List<String> items;

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public int getCreateTime() {
            return createTime;
        }

        public void setCreateTime(int createTime) {
            this.createTime = createTime;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getStartTime() {
            return startTime;
        }

        public void setStartTime(int startTime) {
            this.startTime = startTime;
        }

        public int getEndTime() {
            return endTime;
        }

        public void setEndTime(int endTime) {
            this.endTime = endTime;
        }

        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items = items;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.num);
            dest.writeString(this.nickName);
            dest.writeInt(this.createTime);
            dest.writeInt(this.type);
            dest.writeInt(this.startTime);
            dest.writeInt(this.endTime);
            dest.writeStringList(this.items);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this.num = in.readInt();
            this.nickName = in.readString();
            this.createTime = in.readInt();
            this.type = in.readInt();
            this.startTime = in.readInt();
            this.endTime = in.readInt();
            this.items = in.createStringArrayList();
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
        dest.writeParcelable(this.data, flags);
    }

    public GetLockKeyNickBeanRsp() {
    }

    protected GetLockKeyNickBeanRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.nowTime = in.readInt();
        this.data = in.readParcelable(DataBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<GetLockKeyNickBeanRsp> CREATOR = new Parcelable.Creator<GetLockKeyNickBeanRsp>() {
        @Override
        public GetLockKeyNickBeanRsp createFromParcel(Parcel source) {
            return new GetLockKeyNickBeanRsp(source);
        }

        @Override
        public GetLockKeyNickBeanRsp[] newArray(int size) {
            return new GetLockKeyNickBeanRsp[size];
        }
    };
}
