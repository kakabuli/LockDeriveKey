package com.revolo.lock.mqtt.bean.publishbean.attrparams;

/**
 * author :
 * time   : 2021/3/2
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class AmModeParams {

    private int amMode;          // 0自动模式 1手动模式

    public int getAmMode() {
        return amMode;
    }

    public void setAmMode(int amMode) {
        this.amMode = amMode;
    }
}
