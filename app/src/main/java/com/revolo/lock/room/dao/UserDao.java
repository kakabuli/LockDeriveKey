package com.revolo.lock.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.revolo.lock.room.entity.User;

import java.util.List;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
//@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<User> users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User... user);

    @Delete
    void delete(User user);

    @Delete
    void delete(User... user);

    @Delete
    void delete(List<User> users);

    @Update
    void update(User user);

    @Query("SELECT * FROM User WHERE u_mail=:mail")
    User findBleDeviceFromEsn(String mail);

    @Query("SELECT * FROM User")
    List<User> findAllUser();

    @Query("SELECT * FROM User WHERE u_id=:id ")
    User findBleUserFromId(long id);

    @Query("SELECT * FROM User WHERE u_id in(:ids)")
    List<User> findUsersFromIds(long... ids);

}
