package com.revolo.lock.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.revolo.lock.room.dao.BleDeviceDao;
import com.revolo.lock.room.dao.UserDao;
import com.revolo.lock.room.entity.BleDevice;
import com.revolo.lock.room.entity.User;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
//@Database(entities = {BleDevice.class, User.class}, version = 1, exportSchema = true)
//public abstract class AppDatabase extends RoomDatabase {
//
//    public abstract BleDeviceDao getBleDeviceDao();
//    public abstract UserDao getUserDao();
//    private static AppDatabase INSTANCE;
//    private static final Object sLock = new Object();
//
//    public static AppDatabase getInstance(Context context) {
//        synchronized (sLock) {
//            if (INSTANCE == null) {
//                INSTANCE =
//                        Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "rev.db")
//                                .allowMainThreadQueries() // TODO: 2021/2/3 后续需要把这些操作放到非UI线程里
//                                .build();
//            }
//            return INSTANCE;
//        }
//    }
//}
