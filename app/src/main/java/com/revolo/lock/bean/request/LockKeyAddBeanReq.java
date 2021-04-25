package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加密码请求实体
 */
public class LockKeyAddBeanReq implements Parcelable {


    /**
     * sn : KV51203710172
     * uid : 5f5eff1294a83e85c41d4ca3
     * pwdList : [{"pwdType":1,"num":1,"nickName":"密码1","type":1,"startTime":1551774543,"endTime":1551774543,"items":["1","3"]}]
     */

    private String sn;                         // 设备唯一编号
    private String uid;                        // 管理员用户ID
    private List<PwdListBean> pwdList;         // 密钥列表

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<PwdListBean> getPwdList() {
        return pwdList;
    }

    public void setPwdList(List<PwdListBean> pwdList) {
        this.pwdList = pwdList;
    }

    public static class PwdListBean implements Parcelable {
        /**
         * pwdType : 1
         * num : 1
         * nickName : 密码1
         * type : 1
         * startTime : 1551774543
         * endTime : 1551774543
         * items : ["1","3"]
         */

        private int pwdType;                // 密钥类型：1密码 2指纹密码 3卡片密码 4人脸（暂时没有）
        private int num;                    // 密钥编号
        private String nickName;            // 密钥昵称
        private int type;                   // 永久密钥：00,时间策略密钥：01,胁迫密钥02,管理员密钥：03,无权限密钥：04,周策略密钥：05,一次性密钥：FE
        private long startTime;             // 时间段密钥开始时间
        private long endTime;               // 时间段密钥结束时间
        private List<String> items;         // 周期密码星期几

        public int getPwdType() {
            return pwdType;
        }

        public void setPwdType(int pwdType) {
            this.pwdType = pwdType;
        }

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

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
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
            dest.writeInt(this.pwdType);
            dest.writeInt(this.num);
            dest.writeString(this.nickName);
            dest.writeInt(this.type);
            dest.writeLong(this.startTime);
            dest.writeLong(this.endTime);
            dest.writeStringList(this.items);
        }

        public PwdListBean() {
        }

        protected PwdListBean(Parcel in) {
            this.pwdType = in.readInt();
            this.num = in.readInt();
            this.nickName = in.readString();
            this.type = in.readInt();
            this.startTime = in.readLong();
            this.endTime = in.readLong();
            this.items = in.createStringArrayList();
        }

        public static final Parcelable.Creator<PwdListBean> CREATOR = new Parcelable.Creator<PwdListBean>() {
            @Override
            public PwdListBean createFromParcel(Parcel source) {
                return new PwdListBean(source);
            }

            @Override
            public PwdListBean[] newArray(int size) {
                return new PwdListBean[size];
            }
        };
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sn);
        dest.writeString(this.uid);
        dest.writeTypedList(this.pwdList);
    }

    public LockKeyAddBeanReq() {
    }

    protected LockKeyAddBeanReq(Parcel in) {
        this.sn = in.readString();
        this.uid = in.readString();
        this.pwdList = in.createTypedArrayList(PwdListBean.CREATOR);
    }

    public static final Parcelable.Creator<LockKeyAddBeanReq> CREATOR = new Parcelable.Creator<LockKeyAddBeanReq>() {
        @Override
        public LockKeyAddBeanReq createFromParcel(Parcel source) {
            return new LockKeyAddBeanReq(source);
        }

        @Override
        public LockKeyAddBeanReq[] newArray(int size) {
            return new LockKeyAddBeanReq[size];
        }
    };
}
