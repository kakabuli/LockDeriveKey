package com.revolo.lock.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.revolo.lock.room.entity.BleDeviceLocal;

import java.util.List;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
//@Dao
public interface BleDeviceDao {

    @Insert
    void insert(BleDeviceLocal bleDeviceLocal);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<BleDeviceLocal> bleDeviceLocals);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BleDeviceLocal... bleDeviceLocal);

    @Delete
    void delete(BleDeviceLocal bleDeviceLocal);

    @Delete
    void delete(BleDeviceLocal... bleDeviceLocal);

    @Delete
    void delete(List<BleDeviceLocal> bleDeviceLocal);

    @Update
    void update(BleDeviceLocal bleDeviceLocal);

    @Query("SELECT * FROM BleDeviceLocal WHERE d_esn=:esn")
    BleDeviceLocal findBleDeviceFromEsn(String esn);

    @Query("SELECT * FROM BleDeviceLocal")
    List<BleDeviceLocal> findAllBleDevice();

    @Query("SELECT * FROM BleDeviceLocal WHERE d_user_id=:id")
    List<BleDeviceLocal> findBleDevicesFromUserId(long id);

    @Query("SELECT * FROM BleDeviceLocal WHERE d_id=:id ")
    BleDeviceLocal findBleDeviceFromId(long id);

    @Query("SELECT * FROM BleDeviceLocal WHERE d_id in(:ids)")
    List<BleDeviceLocal> findBleDeviceFromIds(long... ids);

}
