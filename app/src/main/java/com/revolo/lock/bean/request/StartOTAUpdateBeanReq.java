package com.revolo.lock.bean.request;

/**
 * author : Jack
 * time   : 2021/2/9
 * E-mail : wengmaowei@kaadas.com
 * desc   : 确认升级（单设备单组件）请求实体
 */
public class StartOTAUpdateBeanReq {


    /**
     * wifiSN : WF01202010006
     * fileLen : 20850
     * fileUrl : http://121.201.57.214/otaUpgradeFile/2ddf2f0dde8d406c840207a8d3c3006b.png
     * fileMd5 : 48d6d2a38e295611aa68959ccd6b3771
     * devNum : 1
     * fileVersion : 1.1.0
     */

    private String wifiSN;        // WIFI设备SN
    private int fileLen;          // 文件长度
    private String fileUrl;       // 文件url
    private String fileMd5;       // 文件MD5值
    private int devNum;           // 升级编号。1为WIFI模块，2为WIFI锁。（具体固件未确定）
    private String fileVersion;   // 文件版本号

    public String getWifiSN() {
        return wifiSN;
    }

    public void setWifiSN(String wifiSN) {
        this.wifiSN = wifiSN;
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
