package com.revolo.lock.bean.request;

/**
 * author :
 * time   : 2021/5/25
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class UpdateLockInfoReq {


    private String sn;
    private String wifiName;
    private Integer safeMode;
    private String language;
    private Integer volume;
    private Integer amMode;
    private Integer duress;
    private Integer doorSensor;
    private Integer elecFence;
    private Integer autoLockTime;
    private Integer elecFenceTime;
    private Integer elecFenceSensitivity;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public Integer getSafeMode() {
        return safeMode;
    }

    public void setSafeMode(Integer safeMode) {
        this.safeMode = safeMode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public Integer getAmMode() {
        return amMode;
    }

    public void setAmMode(Integer amMode) {
        this.amMode = amMode;
    }

    public Integer getDuress() {
        return duress;
    }

    public void setDuress(Integer duress) {
        this.duress = duress;
    }

    public Integer getDoorSensor() {
        return doorSensor;
    }

    public void setDoorSensor(Integer doorSensor) {
        this.doorSensor = doorSensor;
    }

    public Integer getElecFence() {
        return elecFence;
    }

    public void setElecFence(Integer elecFence) {
        this.elecFence = elecFence;
    }

    public Integer getAutoLockTime() {
        return autoLockTime;
    }

    public void setAutoLockTime(Integer autoLockTime) {
        this.autoLockTime = autoLockTime;
    }

    public Integer getElecFenceTime() {
        return elecFenceTime;
    }

    public void setElecFenceTime(Integer elecFenceTime) {
        this.elecFenceTime = elecFenceTime;
    }

    public Integer getElecFenceSensitivity() {
        return elecFenceSensitivity;
    }

    public void setElecFenceSensitivity(Integer elecFenceSensitivity) {
        this.elecFenceSensitivity = elecFenceSensitivity;
    }
}
