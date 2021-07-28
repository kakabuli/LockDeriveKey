package com.revolo.lock.bean.request;

/**
 * 上报鉴权
 */
public class AuthenticationBeanReq {
    private String wifiSN;
    private String passWord;

    public String getWifiSN() {
        return wifiSN;
    }

    public void setWifiSN(String wifiSN) {
        this.wifiSN = wifiSN;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
    /*   "wifiSN": {
        "type": "string",
                "title": "设备SN",
                "maxLength": 100,
                "pattern": "^(?=.*\\S).+$"
    },
            "passWord": {
        "type": "string",
                "title": "password2",
                "maxLength": 100,
                "pattern": "^(?=.*\\S).+$"
    }
},*/
}
