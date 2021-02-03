package com.revolo.lock.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.revolo.lock.room.entity.User;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
//@Dao
public interface UserDao {

    @Insert
    public void insertUser(User user);

    @Update
    public void updateUser(User user);

    @Delete
    public void delUser(User user);

    @Query("SELECT * FROM user WHERE id=:id")
    public void searchUserFromId(int id);


}
