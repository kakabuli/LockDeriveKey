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
public class SearchProductNoBeanRsp implements Parcelable {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1587954859
     * data : {"productInfoList":[{"productModel":"兰博基尼传奇","developmentModel":"K13"},{"productModel":"兰博基尼传奇3D人脸识别智能锁","developmentModel":"K13F"}]}
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
        private List<ProductInfoListBean> productInfoList;

        public List<ProductInfoListBean> getProductInfoList() {
            return productInfoList;
        }

        public void setProductInfoList(List<ProductInfoListBean> productInfoList) {
            this.productInfoList = productInfoList;
        }

        public static class ProductInfoListBean implements Parcelable {
            /**
             * productModel : 兰博基尼传奇
             * developmentModel : K13
             */

            private String productModel;
            private String developmentModel;

            public String getProductModel() {
                return productModel;
            }

            public void setProductModel(String productModel) {
                this.productModel = productModel;
            }

            public String getDevelopmentModel() {
                return developmentModel;
            }

            public void setDevelopmentModel(String developmentModel) {
                this.developmentModel = developmentModel;
            }


            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.productModel);
                dest.writeString(this.developmentModel);
            }

            public ProductInfoListBean() {
            }

            protected ProductInfoListBean(Parcel in) {
                this.productModel = in.readString();
                this.developmentModel = in.readString();
            }

            public static final Parcelable.Creator<ProductInfoListBean> CREATOR = new Parcelable.Creator<ProductInfoListBean>() {
                @Override
                public ProductInfoListBean createFromParcel(Parcel source) {
                    return new ProductInfoListBean(source);
                }

                @Override
                public ProductInfoListBean[] newArray(int size) {
                    return new ProductInfoListBean[size];
                }
            };
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(this.productInfoList);
        }

        public DataBean() {
        }

        protected DataBean(Parcel in) {
            this.productInfoList = in.createTypedArrayList(ProductInfoListBean.CREATOR);
        }

        public static final Creator<DataBean> CREATOR = new Creator<DataBean>() {
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

    public SearchProductNoBeanRsp() {
    }

    protected SearchProductNoBeanRsp(Parcel in) {
        this.code = in.readString();
        this.msg = in.readString();
        this.nowTime = in.readInt();
        this.data = in.readParcelable(DataBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<SearchProductNoBeanRsp> CREATOR = new Parcelable.Creator<SearchProductNoBeanRsp>() {
        @Override
        public SearchProductNoBeanRsp createFromParcel(Parcel source) {
            return new SearchProductNoBeanRsp(source);
        }

        @Override
        public SearchProductNoBeanRsp[] newArray(int size) {
            return new SearchProductNoBeanRsp[size];
        }
    };
}
