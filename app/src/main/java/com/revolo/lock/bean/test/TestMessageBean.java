package com.revolo.lock.bean.test;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 测试的信息数据
 */
public class TestMessageBean implements Parcelable {

    private String title;
    private String content;
    private long createTime;

    public TestMessageBean(String title, String content, long createTime) {
        this.title = title;
        this.content = content;
        this.createTime = createTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.content);
        dest.writeLong(this.createTime);
    }

    protected TestMessageBean(Parcel in) {
        this.title = in.readString();
        this.content = in.readString();
        this.createTime = in.readLong();
    }

    public static final Parcelable.Creator<TestMessageBean> CREATOR = new Parcelable.Creator<TestMessageBean>() {
        @Override
        public TestMessageBean createFromParcel(Parcel source) {
            return new TestMessageBean(source);
        }

        @Override
        public TestMessageBean[] newArray(int size) {
            return new TestMessageBean[size];
        }
    };
}
