package com.revolo.lock.mqtt.bean.publishbean.attrparams;

/**
 * author :
 * time   : 2021/3/2
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class duressParams extends BaseParamsBean {

    private int duress;        // 0/1 胁迫密码开关

    public int getDuress() {
        return duress;
    }

    public void setDuress(int duress) {
        this.duress = duress;
    }
}
