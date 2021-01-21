package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class SearchProductNoBeanReq implements Parcelable {


    /**
     * productModel : 兰博基尼传奇
     */

    private String productModel;

    public String getProductModel() {
        return productModel;
    }

    public void setProductModel(String productModel) {
        this.productModel = productModel;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.productModel);
    }

    public SearchProductNoBeanReq() {
    }

    protected SearchProductNoBeanReq(Parcel in) {
        this.productModel = in.readString();
    }

    public static final Parcelable.Creator<SearchProductNoBeanReq> CREATOR = new Parcelable.Creator<SearchProductNoBeanReq>() {
        @Override
        public SearchProductNoBeanReq createFromParcel(Parcel source) {
            return new SearchProductNoBeanReq(source);
        }

        @Override
        public SearchProductNoBeanReq[] newArray(int size) {
            return new SearchProductNoBeanReq[size];
        }
    };
}
