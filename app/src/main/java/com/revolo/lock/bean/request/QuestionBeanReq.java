package com.revolo.lock.bean.request;

public class QuestionBeanReq {

    /**
     * "_id": "5c807e6db170903740a02ff6",
     * "question": "怎么开始",
     * "answer": "打APP呀",
     * "sortNum": 1,
     * "createTime": 1551882060
     */

    private String _id;
    private String question;
    private String answer;
    private int sortNum;
    private long createTime;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getSortNum() {
        return sortNum;
    }

    public void setSortNum(int sortNum) {
        this.sortNum = sortNum;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
