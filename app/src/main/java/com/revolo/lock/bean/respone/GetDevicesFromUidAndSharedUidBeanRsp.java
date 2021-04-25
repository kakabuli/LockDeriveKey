package com.revolo.lock.bean.respone;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/3/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 获取分享用户的设备列表回调实体
 */
public class GetDevicesFromUidAndSharedUidBeanRsp {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1576225646
     * data : [{"_id":"5def586f4d3ee1156842868c","adminUid":"5d0c9aa322916bfd695cb123","uid":"5d0c9aa322916bfd695cbae3","userNickname":"萝卜头","shareUserType":1,"shareType":1,"isEnable":1,"createTime":1576225646}]
     */

    private String code;
    private String msg;
    private int nowTime;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * _id : 5def586f4d3ee1156842868c
         * adminUid : 5d0c9aa322916bfd695cb123
         * uid : 5d0c9aa322916bfd695cbae3
         * userNickname : 萝卜头
         * shareUserType : 1
         * shareType : 1
         * isEnable : 1
         * createTime : 1576225646
         */

        private String _id;               // 分享用户-设备关联ID（自增id）
        private String adminUid;          // 管理员id
        private String uid;               // 用户id
        private String userNickname;      // 邀请用户昵称
        private int shareUserType;        // 邀请用户类型。 1 family； 2 guest
        private int shareType;            // 分享状态。 1 等待；2 接收；3 超时；4 删除；5 失效
        private int isEnable;             // 启用状态。 1 启用； 0 未启用
        private int createTime;           // 创建时间

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getAdminUid() {
            return adminUid;
        }

        public void setAdminUid(String adminUid) {
            this.adminUid = adminUid;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getUserNickname() {
            return userNickname;
        }

        public void setUserNickname(String userNickname) {
            this.userNickname = userNickname;
        }

        public int getShareUserType() {
            return shareUserType;
        }

        public void setShareUserType(int shareUserType) {
            this.shareUserType = shareUserType;
        }

        public int getShareType() {
            return shareType;
        }

        public void setShareType(int shareType) {
            this.shareType = shareType;
        }

        public int getIsEnable() {
            return isEnable;
        }

        public void setIsEnable(int isEnable) {
            this.isEnable = isEnable;
        }

        public int getCreateTime() {
            return createTime;
        }

        public void setCreateTime(int createTime) {
            this.createTime = createTime;
        }
    }
}
