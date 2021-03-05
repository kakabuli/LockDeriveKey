package com.revolo.lock.mqtt.bean.publishbean.attrparams;

/**
 * author :
 * time   : 2021/3/2
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class ElecFenceSensitivityParams {

    private int elecFenceSensitivity;       // 电子围栏灵敏度  1:灵敏度低  2：灵敏度中  3：灵敏度高

    public int getElecFenceSensitivity() {
        return elecFenceSensitivity;
    }

    public void setElecFenceSensitivity(int elecFenceSensitivity) {
        this.elecFenceSensitivity = elecFenceSensitivity;
    }
}
