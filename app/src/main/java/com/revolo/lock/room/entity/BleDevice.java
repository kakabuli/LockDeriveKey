package com.revolo.lock.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
//@Entity
public class BleDevice {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "d_user_id")
    private int userId;

    @ColumnInfo(name = "d_pwd1")
    private String pwd1;

    @ColumnInfo(name = "d_pwd2")
    private String pwd2;

    @ColumnInfo(name = "d_esn")
    private String esn;

    @ColumnInfo(name = "d_mac")
    private String mac;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPwd1() {
        return pwd1;
    }

    public void setPwd1(String pwd1) {
        this.pwd1 = pwd1;
    }

    public String getPwd2() {
        return pwd2;
    }

    public void setPwd2(String pwd2) {
        this.pwd2 = pwd2;
    }

    public String getEsn() {
        return esn;
    }

    public void setEsn(String esn) {
        this.esn = esn;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
