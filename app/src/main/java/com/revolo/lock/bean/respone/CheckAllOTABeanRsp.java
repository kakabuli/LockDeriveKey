package com.revolo.lock.bean.respone;

import java.util.List;

/**
 * author :
 * time   : 2021/3/17
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class CheckAllOTABeanRsp {


    /**
     * code : 200
     * msg : 成功
     * nowTime : 1587954859
     * data : {"upgradeTask":[{"fileLen":42,"fileUrl":"47.106.83.60/otaFiles/73c4e82adcd84ab6a337603abfb0ad84?filename=test.txt","devNum":1,"fileMd5":"a5dcbcd00801bc8637c6882560a325a7","fileVersion":"test-1.0"},{"fileLen":42,"fileUrl":"47.106.83.60/otaFiles/73c4e82adcd84ab6a337603abfb0ad84?filename=test.txt","devNum":2,"fileMd5":"a5dcbcd00801bc8637c6882560a325a7","fileVersion":"test-1.0"}]}
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

    public static class DataBean {
        private List<UpgradeTaskBean> upgradeTask;

        public List<UpgradeTaskBean> getUpgradeTask() {
            return upgradeTask;
        }

        public void setUpgradeTask(List<UpgradeTaskBean> upgradeTask) {
            this.upgradeTask = upgradeTask;
        }

        public static class UpgradeTaskBean {
            /**
             * fileLen : 42
             * fileUrl : 47.106.83.60/otaFiles/73c4e82adcd84ab6a337603abfb0ad84?filename=test.txt
             * devNum : 1
             * fileMd5 : a5dcbcd00801bc8637c6882560a325a7
             * fileVersion : test-1.0
             */

            private int fileLen;
            private String fileUrl;
            private int devNum;
            private String fileMd5;
            private String fileVersion;

            public int getFileLen() {
                return fileLen;
            }

            public void setFileLen(int fileLen) {
                this.fileLen = fileLen;
            }

            public String getFileUrl() {
                return fileUrl;
            }

            public void setFileUrl(String fileUrl) {
                this.fileUrl = fileUrl;
            }

            public int getDevNum() {
                return devNum;
            }

            public void setDevNum(int devNum) {
                this.devNum = devNum;
            }

            public String getFileMd5() {
                return fileMd5;
            }

            public void setFileMd5(String fileMd5) {
                this.fileMd5 = fileMd5;
            }

            public String getFileVersion() {
                return fileVersion;
            }

            public void setFileVersion(String fileVersion) {
                this.fileVersion = fileVersion;
            }
        }
    }
}
