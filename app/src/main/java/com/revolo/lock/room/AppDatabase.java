package com.revolo.lock.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.revolo.lock.room.dao.BleDeviceDao;
import com.revolo.lock.room.dao.DevicePwdDao;
import com.revolo.lock.room.dao.UserDao;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.DevicePwd;
import com.revolo.lock.room.entity.User;

import timber.log.Timber;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
//@Database(entities = {BleDeviceLocal.class, User.class, DevicePwd.class}, version = 1)
//public abstract class AppDatabase extends RoomDatabase {
//
//    public abstract BleDeviceDao bleDeviceDao();
//    public abstract UserDao userDao();
//    public abstract DevicePwdDao devicePwdDao();
//    private static AppDatabase INSTANCE;
//    private static final Object sLock = new Object();
//
//    public static AppDatabase getInstance(Context context) {
//        synchronized (sLock) {
//            if (INSTANCE == null) {
//                INSTANCE =
//                        Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "rev.db")
//                                .allowMainThreadQueries() // TODO: 2021/2/3 后续需要把这些操作放到非UI线程里
//                                .addCallback(new Callback() {
//                                    @Override
//                                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
//                                        super.onCreate(db);
//                                        Timber.i("数据库创建成功");
//                                    }
//
//                                    @Override
//                                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
//                                        super.onOpen(db);
//                                        Timber.i("数据库打开");
//                                    }
//
//                                    @Override
//                                    public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
//                                        super.onDestructiveMigration(db);
//                                    }
//                                })
//                                .build();
//            }
//            return INSTANCE;
//        }
//    }
//}
