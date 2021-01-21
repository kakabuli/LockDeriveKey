package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class DelKeyBeanReq implements Parcelable {


    /**
     * uid : 5c70d9493c554639ea93cc90
     * sn : GI132231004
     * pwdList : [{"pwdType":1,"num":"01"}]
     */

    private String uid;
    private String sn;
    private List<PwdListBean> pwdList;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
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
         * num : 01
         */

        private int pwdType;
        private String num;

        public int getPwdType() {
            return pwdType;
        }

        public void setPwdType(int pwdType) {
            this.pwdType = pwdType;
        }

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.pwdType);
            dest.writeString(this.num);
        }

        public PwdListBean() {
        }

        protected PwdListBean(Parcel in) {
            this.pwdType = in.readInt();
            this.num = in.readString();
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
        dest.writeString(this.uid);
        dest.writeString(this.sn);
        dest.writeTypedList(this.pwdList);
    }

    public DelKeyBeanReq() {
    }

    protected DelKeyBeanReq(Parcel in) {
        this.uid = in.readString();
        this.sn = in.readString();
        this.pwdList = in.createTypedArrayList(PwdListBean.CREATOR);
    }

    public static final Parcelable.Creator<DelKeyBeanReq> CREATOR = new Parcelable.Creator<DelKeyBeanReq>() {
        @Override
        public DelKeyBeanReq createFromParcel(Parcel source) {
            return new DelKeyBeanReq(source);
        }

        @Override
        public DelKeyBeanReq[] newArray(int size) {
            return new DelKeyBeanReq[size];
        }
    };
}
