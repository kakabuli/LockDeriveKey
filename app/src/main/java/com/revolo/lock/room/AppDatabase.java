package com.revolo.lock.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.revolo.lock.room.dao.BleDeviceDao;
import com.revolo.lock.room.dao.DevicePwdDao;
import com.revolo.lock.room.dao.LockRecordDao;
import com.revolo.lock.room.dao.UserDao;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.DevicePwd;
import com.revolo.lock.room.entity.LockRecord;
import com.revolo.lock.room.entity.User;

import net.sqlcipher.database.SupportFactory;

import java.nio.charset.StandardCharsets;

import timber.log.Timber;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
@Database(entities = {BleDeviceLocal.class, User.class, DevicePwd.class, LockRecord.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BleDeviceDao bleDeviceDao();
    public abstract UserDao userDao();
    public abstract DevicePwdDao devicePwdDao();
    public abstract LockRecordDao lockRecordDao();
    private static AppDatabase INSTANCE;
    private static final Object sLock = new Object();

    private static final String PASSPHRASE = "ka^222diSHi";
    private static final SupportFactory FACTORY = new SupportFactory(PASSPHRASE.getBytes(StandardCharsets.UTF_8));

    // TODO: 2021/2/24 后续把操作放在独立线程里执行
    public static AppDatabase getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE =
                        Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "rev.db")
                                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                                .allowMainThreadQueries() // TODO: 2021/2/3 后续需要把这些操作放到非UI线程里
                                .openHelperFactory(FACTORY)
                                .addCallback(new Callback() {
                                    @Override
                                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                        super.onCreate(db);
                                        Timber.i("数据库创建成功");
                                    }

                                    @Override
                                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                        super.onOpen(db);
                                        Timber.i("数据库打开");
                                    }

                                    @Override
                                    public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                                        super.onDestructiveMigration(db);
                                    }
                                })
                                .build();
            }
            return INSTANCE;
        }
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建了消息记录表
            database.execSQL("CREATE TABLE IF NOT EXISTS LockRecord " +
                    "(lr_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "lr_event_type INTEGER NOT NULL, lr_event_source INTEGER NOT NULL, " +
                    "lr_event_code INTEGER NOT NULL, lr_user_id INTEGER NOT NULL, " +
                    "lr_app_id INTEGER NOT NULL, lr_device_id INTEGER NOT NULL, " +
                    "lr_create_time INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_LockRecord_lr_device_id ON LockRecord(lr_device_id)");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // BleDeviceLocal新增一个字段randomCode
            database.execSQL("ALTER TABLE BleDeviceLocal ADD COLUMN d_random_code TEXT");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // User新增一个字段u_gesture_code
            database.execSQL("ALTER TABLE User ADD COLUMN u_gesture_code TEXT");
        }
    };


}
