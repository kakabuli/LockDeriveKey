package com.revolo.lock.mqtt.bean.publishbean.attrparams;

/**
 * author :
 * time   : 2021/3/2
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class AutoLockTimeParams extends BaseParamsBean {

    private int autoLockTime;         // 自动上锁时间，默认30s

    public int getAutoLockTime() {
        return autoLockTime;
    }

    public void setAutoLockTime(int autoLockTime) {
        this.autoLockTime = autoLockTime;
    }
}
