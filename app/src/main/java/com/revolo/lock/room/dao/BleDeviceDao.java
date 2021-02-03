package com.revolo.lock.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.revolo.lock.room.entity.BleDevice;
import com.revolo.lock.room.entity.User;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
//@Dao
public interface BleDeviceDao {

    @Insert
    public void insertBleDevice(BleDevice bleDevice);

    @Update
    public void updateBleDevice(BleDevice bleDevice);

    @Delete
    public void delBleDevice(BleDevice bleDevice);

    @Query("SELECT * FROM bledevice WHERE d_esn=:esn")
    public void searchBleDeviceFromId(String esn);

}
