package com.revolo.lock.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.revolo.lock.room.entity.LockRecord;

import java.util.List;

/**
 * author :
 * time   : 2021/2/25
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
@Dao
public interface LockRecordDao {

    @Insert
    long insert(LockRecord lockRecord);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<LockRecord> lockRecords);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LockRecord... lockRecords);

    @Delete
    void delete(LockRecord lockRecord);

    @Delete
    void delete(LockRecord... lockRecords);

    @Delete
    void delete(List<LockRecord> lockRecords);

    @Update
    void update(LockRecord lockRecord);

    @Query("SELECT * FROM LockRecord WHERE lr_device_id=:id ORDER BY lr_create_time DESC")
    List<LockRecord> findLockRecordsFromDeviceId(long id);

    @Query("SELECT * FROM LockRecord WHERE lr_device_id=:id ORDER BY lr_create_time DESC LIMIT :num")
    List<LockRecord> findLockRecordsFromDeviceId(long id, int num);

    @Query("SELECT * FROM LockRecord WHERE lr_id=:id ")
    LockRecord findLockRecordFromId(long id);

    @Query("SELECT * FROM LockRecord WHERE lr_id in(:ids)")
    List<LockRecord> findLockRecordsFromIds(long... ids);

    /**
     * 通过日期时间筛选消息记录
     * @param id           设备id
     * @param startTime    设备记录时间范围-开始时间
     * @param endTime      设备记录时间范围-结束时间
     */
    @Query("SELECT * FROM LockRecord WHERE lr_device_id=:id AND lr_create_time>=:startTime AND lr_create_time<=:endTime")
    List<LockRecord> findLockRecordsFromDeviceIdAndDay(long id, long startTime, long endTime);

    /**
     * 查询对应设备的消息记录
     * @param id     设备ID
     * @param num    每页查询的数量
     * @param page   页数，页数从1开始
     */
    @Query("SELECT * FROM LockRecord WHERE lr_device_id=:id ORDER BY lr_create_time DESC LIMIT ((:page-1)*:num), :num")
    List<LockRecord> findLockRecordsFromDeviceId(long id, int num, int page);

    /**
     * 查询对应设备最后创建时间的数据记录
     * @param id    设备id
     */
    @Query("SELECT * FROM LockRecord WHERE lr_device_id=:id ORDER BY lr_create_time DESC LIMIT 1")
    LockRecord findLastCreateTimeLockRecordFromDeviceId(long id);

}
