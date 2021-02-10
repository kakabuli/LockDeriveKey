package com.revolo.lock.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.revolo.lock.room.entity.DevicePwd;

import java.util.List;

/**
 * author :
 * time   : 2021/2/10
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
//@Dao
public interface DevicePwdDao {

    @Insert
    void insert(DevicePwd devicePwd);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<DevicePwd> devicePwdList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DevicePwd... devicePwd);

    @Delete
    void delete(DevicePwd devicePwd);

    @Delete
    void delete(DevicePwd... devicePwd);

    @Delete
    void delete(List<DevicePwd> devicePwdList);

    @Update
    void update(DevicePwd devicePwd);

    @Query("SELECT * FROM DevicePwd")
    List<DevicePwd> findAllDevicePwd();

    @Query("SELECT * FROM DevicePwd WHERE dp_device_id=:id")
    List<DevicePwd> findDevicePwdListFromDeviceId(long id);

    @Query("SELECT * FROM DevicePwd WHERE dp_id=:id ")
    DevicePwd findDevicePwdFromId(long id);

    @Query("SELECT * FROM DevicePwd WHERE dp_id in(:ids)")
    List<DevicePwd> findDevicePwdFromIds(long... ids);

}
