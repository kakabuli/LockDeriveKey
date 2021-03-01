package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.AutoMeasureLinearLayoutManager;
import com.revolo.lock.adapter.OperationRecordsAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.showBean.RecordState;
import com.revolo.lock.bean.test.TestOperationRecords;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.LockRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_GET_ALL_RECORD;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 操作记录
 */
public class OperationRecordsActivity extends BaseActivity {

    // TODO: 2021/2/25 后续添加超时操作

    private OperationRecordsAdapter mRecordsAdapter;
    private BleBean mBleBean;
    private long mDeviceId;
    private CustomerLoadingDialog mLoadingDialog;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleBean = App.getInstance().getBleBean();
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.DEVICE_ID)) {
            mDeviceId = intent.getLongExtra(Constant.DEVICE_ID, -1L);
            Timber.d("initData Device Id: %1d", mDeviceId);
        }
        if(mDeviceId == -1L) {
            // TODO: 2021/2/24 处理异常情况
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_operation_records;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_operation_records))
                .setRight(ContextCompat.getDrawable(this, R.drawable.ic_icon_date), v -> {
                    // TODO: 2021/1/13 打开日历筛选
                });
        RecyclerView rvRecords = findViewById(R.id.rvOperationRecords);
        AutoMeasureLinearLayoutManager linearLayoutManager = new AutoMeasureLinearLayoutManager(this);
        rvRecords.setLayoutManager(linearLayoutManager);
        mRecordsAdapter = new OperationRecordsAdapter(R.layout.item_operation_record_list_rv);
        rvRecords.setAdapter(mRecordsAdapter);
        // TODO: 2021/2/25 抽离文字
        mLoadingDialog = new CustomerLoadingDialog.Builder(this)
                .setMessage("Loading...")
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
    }

    @Override
    public void doBusiness() {
        showLoading();
        initDevice();
        searchRecordFromLocalFirst();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private final OnBleDeviceListener mOnBleDeviceListener = new OnBleDeviceListener() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onReceivedValue(String uuid, byte[] value) {
            if(value == null) {
                return;
            }
            BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
            BleResultProcess.processReceivedData(value, mBleBean.getPwd1(), mBleBean.getPwd3(),
                    mBleBean.getOKBLEDeviceImp().getBleScanResult());
        }

        @Override
        public void onWriteValue(String uuid, byte[] value, boolean success) {

        }

        @Override
        public void onAuthSuc() {

        }
    };

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        if(bleResultBean.getCMD() == CMD_GET_ALL_RECORD) {
            updateRecordFormBle(bleResultBean);
        }
    };

    private void initDevice() {
        if (mBleBean.getOKBLEDeviceImp() != null) {
            App.getInstance().openPairNotify();
            App.getInstance().setOnBleDeviceListener(mOnBleDeviceListener);
        }
    }

    private void searchRecordFromBle(short start, short end) {
        if(mBleBean.getOKBLEDeviceImp() != null) {
            if (mBleBean.getOKBLEDeviceImp().isConnected()) {
                // 因为shortToBytes转出来就是小端模式，所以调用直接使用小端模式的方法
                App.getInstance().writeControlMsg(BleCommandFactory
                        .readAllRecordFromSmallEndian(BleByteUtil.shortToBytes(start), BleByteUtil.shortToBytes(end),
                                mBleBean.getPwd1(), mBleBean.getPwd3()));
            } else {
                // TODO: 2021/1/26 没有连接上，需要连接上才能发送指令
            }
        }
    }

    private boolean isNeedToReceiveRecord = true;
    private short mBleTotalRecord = 0;

    private void updateRecordFormBle(BleResultBean bean) {
        if(!isNeedToReceiveRecord) {
            // 不再接收数据的标志位, 不再处理数据
            return;
        }
        byte[] total = new byte[2];
        System.arraycopy(bean.getPayload(), 0, total, 0, total.length);
        short totalShort = BleByteUtil.bytesToShortToBigEndianFromLittleEndian(total);
        mBleTotalRecord = totalShort;
        byte[] index = new byte[2];
        System.arraycopy(bean.getPayload(), 2, index, 0, index.length);
        short indexShort = BleByteUtil.bytesToShortToBigEndianFromLittleEndian(index);
        int eventType = bean.getPayload()[4];
        int eventSource = bean.getPayload()[5];
        int eventCode = bean.getPayload()[6];
        int userId = bean.getPayload()[7];
        int appId = bean.getPayload()[8];
        byte[] time = new byte[4];
        System.arraycopy(bean.getPayload(), 9, time, 0, time.length);
        long realTime = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time));
        Timber.d("记录 total: %1d, index: %2d, eventType: %3d, eventSource: %4d, eventCode: %5d, userId: %6d, appId: %7d, time: %8d",
                totalShort, indexShort, eventType, eventSource, eventCode, userId, appId, realTime * 1000);

        if(mLastLockRecord != null) {
            if(realTime == mLastLockRecord.getCreateTime()) {
                isNeedToReceiveRecord = false;
                dismissLoading();
                ThreadUtils.getSinglePool().execute(this::searchAllRecordFromLocal);
                return;
            }
        }
        LockRecord lockRecord = new LockRecord();
        lockRecord.setDeviceId(mDeviceId);
        lockRecord.setAppId(appId);
        lockRecord.setUserId(userId);
        lockRecord.setCreateTime(realTime);
        lockRecord.setEventCode(eventCode);
        lockRecord.setEventSource(eventSource);
        lockRecord.setEventType(eventType);
        AppDatabase.getInstance(this).lockRecordDao().insert(lockRecord);

        if((totalShort-1) == indexShort) {
            searchRecordFromLocal(indexShort, 1);
            refreshUIFromFinalData();
        }
    }

    private LockRecord mLastLockRecord;
    private ArrayList<LockRecord> mLockRecords = new ArrayList<>();

    private short mBleSearchStart = 0;
    private short mBleSearchEnd = 100;

    private int mBleSearchPage = 1;

    private void searchRecordFromLocalFirst() {
        LockRecord lockRecord = AppDatabase.getInstance(this).lockRecordDao().findLastCreateTimeLockRecordFromDeviceId(mDeviceId);
        if(lockRecord == null) {
            searchRecordFromBle(mBleSearchStart, mBleSearchEnd);
            return;
        }
        mLastLockRecord = lockRecord;
        searchRecordFromBle(mBleSearchStart, mBleSearchEnd);
    }

    private void searchRecordFromLocal(int num, int page) {
        List<LockRecord> lockRecords = AppDatabase
                .getInstance(this)
                .lockRecordDao()
                .findLockRecordsFromDeviceId(mDeviceId, num, page);
        if(lockRecords == null) {
            return;
        }
        if(lockRecords.isEmpty()) {
            return;
        }
        mLockRecords.addAll(lockRecords);
    }

    private void searchRecordFromLocal(int page) {
        searchRecordFromLocal(10, page);
    }

    private void searchAllRecordFromLocal() {
        List<LockRecord> lockRecords = AppDatabase
                .getInstance(this)
                .lockRecordDao()
                .findLockRecordsFromDeviceId(mDeviceId);
        if(lockRecords == null) {
            return;
        }
        if(lockRecords.isEmpty()) {
            return;
        }
        mLockRecords.addAll(lockRecords);
        refreshUIFromFinalData();
    }

    private void showLoading() {
        runOnUiThread(() -> {
            if(mLoadingDialog != null) {
                mLoadingDialog.show();
            }
        });
    }

    private void dismissLoading() {
        runOnUiThread(() -> {
            if(mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        });
    }

    private void refreshUIFromFinalData() {
        dismissLoading();
        // TODO: 2021/2/25 后面要做的是要代理处理的数据 
        if(mLockRecords.isEmpty()) {
            return;
        }
        List<TestOperationRecords.TestOperationRecord> records = new ArrayList<>();
        for (LockRecord lockRecord : mLockRecords) {
            // eventCode 1:上锁 2:开锁
            TestOperationRecords.TestOperationRecord record;
            @RecordState.OpRecordState int state = RecordState.NOTHING;
            boolean isAlarmRecord = false;
            String message = "";
            if(lockRecord.getEventType() == 1) {
                if(lockRecord.getEventSource() == 0) {
                    // 键盘
                    if(lockRecord.getEventCode() == 2) {
                        message = "Unlocked by password";
                        state = RecordState.SOMEONE_USE_A_PWD_TO_UNLOCK;
                    }
                } else if(lockRecord.getEventSource() == 8) {
                    // APP
                    if(lockRecord.getEventCode() == 1) {
                        // TODO: 2021/2/25 后期改掉
                        message = "locked the door by APP ";
                        state = RecordState.SOMEONE_LOCKED_THE_DOOR_BY_APP;
                    } else if(lockRecord.getEventCode() == 2) {
                        message = "uses the APP to unlock";
                        state = RecordState.SOMEONE_USE_THE_APP_TO_UNLOCK;
                    }
                } else if(lockRecord.getEventSource() == 9) {
                    // 机械钥匙
                    if(lockRecord.getEventCode() == 1) {
                        message = "Locked the door by mechanical key";
                        state = RecordState.SOMEONE_LOCKED_THE_DOOR_BY_MECHANICAL_KEY;
                    } else if(lockRecord.getEventCode() == 2) {
                        message = "Unlocked by mechanical key ";
                        state = RecordState.SOMEONE_USE_MECHANICAL_KEY_TO_UNLOCK;
                    }
                } else if(lockRecord.getEventSource() == -1) {
                    // 不确定，todo 有可能是MQTT，需要定义一个值
                    if(lockRecord.getEventCode() == 1) {
                        // TODO: 2021/2/25 后期改掉
                        message = "locked the door by APP ";
                        state = RecordState.SOMEONE_LOCKED_THE_DOOR_BY_APP;
                    } else if(lockRecord.getEventCode() == 2) {
                        message = "uses the APP to unlock";
                        state = RecordState.SOMEONE_USE_THE_APP_TO_UNLOCK;
                    }
                }
            }
            if(TextUtils.isEmpty(message)) {
                Timber.e("本地记录 eventType: %3d, eventSource: %4d, eventCode: %5d, userId: %6d, appId: %7d, time: %8d",
                        lockRecord.getEventType(), lockRecord.getEventSource(), lockRecord.getEventCode(),
                        lockRecord.getUserId(), lockRecord.getAppId(), lockRecord.getCreateTime() * 1000);
                break;
            }
            record = new TestOperationRecords.TestOperationRecord(lockRecord.getCreateTime()*1000, message, state, isAlarmRecord);
            records.add(record);
        }
        // 日期分类筛选
        Map<String, List<TestOperationRecords.TestOperationRecord>> collect = records
                .stream().collect(Collectors.groupingBy(TestOperationRecords.TestOperationRecord::getDate));
        List<TestOperationRecords> recordsList = new ArrayList<>();
        for (String key : collect.keySet()) {
            Timber.d("refreshUIFromFinalData key: %1s", key);
            TestOperationRecords operationRecords = new TestOperationRecords(TimeUtils.string2Millis(key, "yyyy-MM-dd"), collect.get(key));
            recordsList.add(operationRecords);
        }
        // 时间降序排列
        recordsList.sort((o1, o2) -> ((int) o2.getTitleOperationTime() - (int) o1.getTitleOperationTime()));
        runOnUiThread(() -> mRecordsAdapter.setList(recordsList));
    }


    // TODO: 2021/2/7
//        case 1:
//        message = getContext().getString(R.string.log_uses_a_password_to_unlock, "Ming");
//        case 2:
//        message = getContext().getString(R.string.log_uses_geo_fence_to_unlock, "John");
//        case 3:
//        message = getContext().getString(R.string.log_uses_the_app_to_unlock, "Han");
//        case 4:
//        message = getContext().getString(R.string.log_uses_mechanical_key_to_unlock, "Jack");
//        case 5:
//        message = getContext().getString(R.string.log_locking_inside_the_door);
//        case 6:
//        message = getContext().getString(R.string.log_double_lock_inside_the_door);
//        case 7:
//        message = getContext().getString(R.string.log_multi_functional_button_locking);
//        case 8:
//        message = getContext().getString(R.string.log_one_touch_lock_outside_the_door);
//        case 9:
//        message = getContext().getString(R.string.log_locked_the_door_by_app, "Amy");
//        case 10:
//        message = getContext().getString(R.string.log_locked_the_door_by_mechanical_key, "Ming");
//        case 11:
//        message = getContext().getString(R.string.log_duress_password_unlock);
//        case 12:
//        message = getContext().getString(R.string.log_lock_down_alarm);
//        case 13:
//        message = getContext().getString(R.string.log_low_battery_alarm);
//        case 14:
//        message = getContext().getString(R.string.log_jam_alarm);
//        case 15:
//        message = getContext().getString(R.string.log_door_opened_detected);
//        case 16:
//        message = getContext().getString(R.string.log_door_closed_detected);
//        case 17:
//        message = getContext().getString(R.string.log_the_user_added_a_password);
//        case 18:
//        message = getContext().getString(R.string.log_the_user_deleted_a_password);
//        case 19:
//        message = getContext().getString(R.string.log_the_user_added_someone_in_family_group, "Ming");
//        case 20:
//        message = getContext().getString(R.string.log_the_user_removed_someone_from_family_group, "Ming");
//        case 21:
//        message = getContext().getString(R.string.log_user_added_aa_as_guest_user, "Ming");
//        case 22:
//        message = getContext().getString(R.string.log_user_removed_aa_from_guest_user, "Tai");

}
