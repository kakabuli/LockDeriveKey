package com.revolo.lock.bean.request;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/3/17
 * E-mail : wengmaowei@kaadas.com
 * desc   : 确认升级（多组件）请求实体
 */
public class StartAllOTAUpdateBeanReq {


    /**
     * wifiSN : WF03201210027
     * upgradeTask : [{"devNum":1,"fileLen":42,"fileUrl":"47.106.83.60/otaFiles/73c4e82adcd84ab6a337603abfb0ad84?filename=test.txt","fileMd5":"a5dcbcd00801bc8637c6882560a325a7","fileVersion":"test-1.0"}]
     */

    private String wifiSN;                         // WIFI设备SN
    private List<UpgradeTaskBean> upgradeTask;     // 设备各组件升级任务，跟检查升级响应内容一样

    public String getWifiSN() {
        return wifiSN;
    }

    public void setWifiSN(String wifiSN) {
        this.wifiSN = wifiSN;
    }

    public List<UpgradeTaskBean> getUpgradeTask() {
        return upgradeTask;
    }

    public void setUpgradeTask(List<UpgradeTaskBean> upgradeTask) {
        this.upgradeTask = upgradeTask;
    }

    public static class UpgradeTaskBean {
        /**
         * devNum : 1
         * fileLen : 42
         * fileUrl : 47.106.83.60/otaFiles/73c4e82adcd84ab6a337603abfb0ad84?filename=test.txt
         * fileMd5 : a5dcbcd00801bc8637c6882560a325a7
         * fileVersion : test-1.0
         */

        private int devNum;                // 升级编号。1为WIFI模块，2为WIFI锁，3为人脸模组，4为视频模组，5为视频模组微控制器，6为前面板，7为后面板。
        private int fileLen;               // 文件长度
        private String fileUrl;            // 文件url
        private String fileMd5;            // 文件MD5值
        private String fileVersion;        // 文件版本号

        public int getDevNum() {
            return devNum;
        }

        public void setDevNum(int devNum) {
            this.devNum = devNum;
        }

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

        public String getFileVersion() {
            return fileVersion;
        }

        public void setFileVersion(String fileVersion) {
            this.fileVersion = fileVersion;
        }
    }
}
