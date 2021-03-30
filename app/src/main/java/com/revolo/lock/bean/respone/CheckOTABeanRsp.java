package com.revolo.lock.bean.respone;

/**
 * author :
 * time   : 2021/2/9
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class CheckOTABeanRsp {


    /**
     * code : 200
     * msg : null
     * data : {"fileLen":20850,"fileUrl":"http://121.201.57.214/otaUpgradeFile/2ddf2f0dde8d406c840207a8d3c3006b.png","fileMd5":"48d6d2a38e295611aa68959ccd6b3771","devNum":1,"fileVersion":"1.1.0"}
     */

    private String code;
    private String msg;
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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * fileLen : 20850
         * fileUrl : http://121.201.57.214/otaUpgradeFile/2ddf2f0dde8d406c840207a8d3c3006b.png
         * fileMd5 : 48d6d2a38e295611aa68959ccd6b3771
         * devNum : 1
         * fileVersion : 1.1.0
         */

        private int fileLen;
        private String fileUrl;
        private String fileMd5;
        private int devNum;
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

        public String getFileMd5() {
            return fileMd5;
        }

        public void setFileMd5(String fileMd5) {
            this.fileMd5 = fileMd5;
        }

        public int getDevNum() {
            return devNum;
        }

        public void setDevNum(int devNum) {
            this.devNum = devNum;
        }

        public String getFileVersion() {
            return fileVersion;
        }

        public void setFileVersion(String fileVersion) {
            this.fileVersion = fileVersion;
        }
    }
}
