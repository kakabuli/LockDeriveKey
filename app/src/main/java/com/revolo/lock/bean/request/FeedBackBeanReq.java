package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/3/26
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class FeedBackBeanReq {


    /**
     * uid : 5c4fe492dc93897aa7d8600b
     * suggest : 这个APP很完美，没毛病
     */

    private String uid;
    private String suggest;

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
