package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/3/26
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户反馈接口请求实体
 */
public class FeedBackBeanReq {


    /**
     * uid : 5c4fe492dc93897aa7d8600b
     * suggest : 这个APP很完美，没毛病
     */

    private String uid;          // 用户id
    private String suggest;      // 反馈内容

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSuggest() {
        return suggest;
    }

    public void setSuggest(String suggest) {
        this.suggest = suggest;
    }
}
