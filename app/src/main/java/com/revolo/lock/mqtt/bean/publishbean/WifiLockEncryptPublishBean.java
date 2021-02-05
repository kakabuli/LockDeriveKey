package com.revolo.lock.mqtt.bean.publishbean;

public class WifiLockEncryptPublishBean {
    /**
     * userId : 5cad4509dc938989e2f542c8
     * wfId : WF12345678
     * encrypt : aWDDkOy9mZe6QKA3oMlMWkoL8dyaIbwsz+4GHOesmQg+lAajSruhDXi7BirovbjgyQKVmZ8yymOP5Rtnosal4i/v2JT68w4SJO80wb0Kh065nmJVpwxwYHUfXhnP6439JFav+s+en9IfRHubUv2stG+AD0fD1k7kKOuiIn/G4ygLU7bGoKn+wAX7miC6zyRIQrrkmBHHgdEUvHLFbmJ+GUH//TQXHLKvbO1CMwKj6WS36zUFzj0GQNlITyLmejrW60QhKCOxm2p3p5+FrErkzQ==
     */

    private String userId;
    private String wfId;
    private String encrypt;

    public WifiLockEncryptPublishBean(String userId, String wfId, String encrypt) {
        this.userId = userId;
        this.wfId = wfId;
        this.encrypt = encrypt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWfId() {
        return wfId;
    }

    public void setWfId(String wfId) {
        this.wfId = wfId;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }

    @Override
    public String toString() {
        return "WifiLockEncryptPublishBean{" +
                "userId='" + userId + '\'' +
                ", wfId='" + wfId + '\'' +
                ", encrypt='" + encrypt + '\'' +
                '}';
    }
}
