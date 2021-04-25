package com.revolo.lock.bean.respone;

import java.util.List;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class SearchKeyListBeanRsp {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1610005487
     * data : {"_id":"5ff6ad2c2a292e5fbc913708","pwdList":[{"num":1,"nickName":"密码2","createTime":1610003529,"type":1,"startTime":1551774543,"endTime":1551774543,"items":["1","3"]}],"faceList":[],"fingerprintList":[],"cardList":[]}
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

    // TODO: 2021/1/21 要确认是什么数据
    public static class DataBean {
        /**
         * _id : 5ff6ad2c2a292e5fbc913708
         * pwdList : [{"num":1,"nickName":"密码2","createTime":1610003529,"type":1,"startTime":1551774543,"endTime":1551774543,"items":["1","3"]}]
         * faceList : []
         * fingerprintList : []
         * cardList : []
         */

        private String _id;
        private List<PwdListBean> pwdList;        // 密码列表
        private List<?> faceList;
        private List<?> fingerprintList;
        private List<?> cardList;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public List<PwdListBean> getPwdList() {
            return pwdList;
        }

        public void setPwdList(List<PwdListBean> pwdList) {
            this.pwdList = pwdList;
        }

        public List<?> getFaceList() {
            return faceList;
        }

        public void setFaceList(List<?> faceList) {
            this.faceList = faceList;
        }

        public List<?> getFingerprintList() {
            return fingerprintList;
        }

        public void setFingerprintList(List<?> fingerprintList) {
            this.fingerprintList = fingerprintList;
        }

        public List<?> getCardList() {
            return cardList;
        }

        public void setCardList(List<?> cardList) {
            this.cardList = cardList;
        }

        public static class PwdListBean {
            /**
             * num : 1
             * nickName : 密码2
             * createTime : 1610003529
             * type : 1
             * startTime : 1551774543
             * endTime : 1551774543
             * items : ["1","3"]
             */

            private int num;                      // 密钥编号
            private String nickName;              // 密钥昵称
            private int createTime;               // 添加时间
            private int type;                     // 密钥周期类型：永久密钥：00,时间策略密钥：01,胁迫密钥02,管理员密钥：03,无权限密钥：04,周策略密钥：05,一次性密钥：FE
            private int startTime;                // 时间段密钥开始时间
            private int endTime;                  // 时间段密钥结束时间
            private List<String> items;           // 周期密码星期几

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


        }
    }
}
