package com.revolo.lock.ui.device.lock;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

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
import com.revolo.lock.bean.test.OperationRecords;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.LockRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_GET_ALL_RECORD;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 操作记录
 */
// TODO: 2021/3/6 数据存在日期筛选后无法滑动的bug
public class OperationRecordsActivity extends BaseActivity {

    // TODO: 2021/2/25 后续添加超时操作

    private OperationRecordsAdapter mRecordsAdapter;
    private BleBean mBleBean;
    private long mDeviceId;
    private LinearLayout mllNoRecord;
    private RecyclerView mRvOperationRecords;

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
                    showDatePicker();
                });
        mRvOperationRecords = findViewById(R.id.rvOperationRecords);
        AutoMeasureLinearLayoutManager linearLayoutManager = new AutoMeasureLinearLayoutManager(this);
        mRvOperationRecords.setLayoutManager(linearLayoutManager);
        mllNoRecord = findViewById(R.id.llNoRecord);
        mRecordsAdapter = new OperationRecordsAdapter(R.layout.item_operation_record_list_rv);
        mRvOperationRecords.setAdapter(mRecordsAdapter);
        initLoading("Loading...");
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
        if(mBleBean == null) {
            Timber.e("initDevice mBleBean == null");
            return;
        }
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
        short totalShort = BleByteUtil.bytesToShortFromLittleEndian(total);
        mBleTotalRecord = totalShort;
        byte[] index = new byte[2];
        System.arraycopy(bean.getPayload(), 2, index, 0, index.length);
        short indexShort = BleByteUtil.bytesToShortFromLittleEndian(index);
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

    private void showOrDismissNoRecord(boolean isShow) {
        runOnUiThread(() -> {
            if(isShow) {
                mllNoRecord.setVisibility(View.VISIBLE);
            } else {
                mllNoRecord.setVisibility(View.GONE);
            }
        });
    }

    private void showOrDismissRecords(boolean isShow) {
        runOnUiThread(() -> {
            if(isShow) {
                mRvOperationRecords.setVisibility(View.VISIBLE);
            } else {
                mRvOperationRecords.setVisibility(View.GONE);
            }
        });
    }

    private void refreshUIFromFinalData() {
        // TODO: 2021/2/25 后面要做的是要代理处理的数据 
        if(mLockRecords.isEmpty()) {
            showOrDismissNoRecord(true);
            showOrDismissRecords(false);
            return;
        } else {
            showOrDismissNoRecord(false);
            showOrDismissRecords(true);
        }
        List<OperationRecords.OperationRecord> records = new ArrayList<>();
        for (LockRecord lockRecord : mLockRecords) {
            // eventCode 1:上锁 2:开锁
            OperationRecords.OperationRecord record;
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
                        message = "Unlocked by mechanical key";
                        state = RecordState.SOMEONE_USE_MECHANICAL_KEY_TO_UNLOCK;
                    }
                } else if(lockRecord.getEventSource() == -1) {
                    // 不确定，todo 有可能是MQTT，需要定义一个值
                    if(lockRecord.getEventCode() == 1) {
                        // TODO: 2021/2/25 后期改掉
                        message = "locked the door by APP";
                        state = RecordState.SOMEONE_LOCKED_THE_DOOR_BY_APP;
                    } else if(lockRecord.getEventCode() == 2) {
                        message = "uses the APP to unlock";
                        state = RecordState.SOMEONE_USE_THE_APP_TO_UNLOCK;
                    }
                }
            } else if(lockRecord.getEventType() == 2) {
                // 增删改记录
                // TODO: 2021/3/2 后面替换文字信息
                if(lockRecord.getEventCode() == 1) {
                    // 修改
                } else if(lockRecord.getEventCode() == 2) {
                    // 添加
                    message = "The user added a password";
                    state = RecordState.THE_USER_ADDED_A_PWD;
                } else if(lockRecord.getEventCode() == 3) {
                    // 删除
                    message = "The user deleted a password";
                    state = RecordState.THE_USER_DELETED_A_PWD;
                } else if(lockRecord.getEventCode() == 0x0f) {
                    // 恢复出厂设置
                    message = "The door lock has been restored to factory Settings";
                    state = RecordState.LOCK_RESTORE;
                }
            } else if(lockRecord.getEventType() == 3) {
                // 报警
                // TODO: 2021/3/2 后面替换文字信息
                if(lockRecord.getEventCode() == 1) {
                    // 锁定报警（输入错误密码或指纹或卡片超过5次就会系统锁定报警）
                    message = "lockdown alarm";
                    state = RecordState.LOCK_DOWN_ALARM;
                } else if(lockRecord.getEventCode() == 2) {
                    // 劫持报警（输入防劫持密码或防劫持指纹开锁就报警）
                    message = "Duress password unlock";
                    state = RecordState.DURESS_PASSWORD_UNLOCK;
                } else if(lockRecord.getEventCode() == 3) {
                    // 三次错误，上报提醒
                } else if(lockRecord.getEventCode() == 4) {
                    // 撬锁报警（锁被撬开）
                } else if(lockRecord.getEventCode() == 8) {
                    // 机械钥匙报警（使用机械钥匙开锁）
                } else if(lockRecord.getEventCode() == 0x10) {
                    // 低电压报警（电池电量不足）
                    message = "Low battery alarm";
                    state = RecordState.LOW_BATTERY_ALARM;
                } else if(lockRecord.getEventCode() == 0x20) {
                    // 门锁异常报警
                } else if(lockRecord.getEventCode() == 0x40) {
                    // 门锁布防报警（门外布防后，从门内开锁了就会报警）
                }
            }
            if(TextUtils.isEmpty(message)) {
                Timber.e("本地记录 eventType: %3d, eventSource: %4d, eventCode: %5d, userId: %6d, appId: %7d, time: %8d",
                        lockRecord.getEventType(), lockRecord.getEventSource(), lockRecord.getEventCode(),
                        lockRecord.getUserId(), lockRecord.getAppId(), lockRecord.getCreateTime() * 1000);
                continue;
            }
            record = new OperationRecords.OperationRecord(lockRecord.getCreateTime()*1000, message, state, isAlarmRecord);
            records.add(record);
        }
        dismissLoading();
        processRightRecords(records);
    }

    private void processRightRecords(List<OperationRecords.OperationRecord> records) {
        // 日期分类筛选
        Map<String, List<OperationRecords.OperationRecord>> collect = records
                .stream().collect(Collectors.groupingBy(OperationRecords.OperationRecord::getDate));
        // 时间降序
        Map<String, List<OperationRecords.OperationRecord>> sortCollect = sortMapByKey(collect);
        List<OperationRecords> recordsList = new ArrayList<>();
        for (String key : sortCollect.keySet()) {
            Timber.d("processRightRecords key: %1s", key);
            OperationRecords operationRecords = new OperationRecords(TimeUtils.string2Millis(key, "yyyy-MM-dd"), collect.get(key));
            recordsList.add(operationRecords);
        }
        runOnUiThread(() -> mRecordsAdapter.setList(recordsList));
    }

    /**
     * 使用 Map按key进行排序
     * @param map
     * @return
     */
    public static Map<String, List<OperationRecords.OperationRecord>> sortMapByKey(Map<String, List<OperationRecords.OperationRecord>> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, List<OperationRecords.OperationRecord>> sortMap = new TreeMap<>(
                new MapKeyComparator());

        sortMap.putAll(map);

        return sortMap;
    }

    private static class MapKeyComparator implements Comparator<String> {

        @Override
        public int compare(String str1, String str2) {
            // 升序
//            return str1.compareTo(str2);
            // 降序
            return str2.compareTo(str1);
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyTimePickerDialogTheme);
        datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            // TODO: 2021/2/4 date
            showLoading();
            month+=1;
            String date = year+"-"+(month<10?"0"+month:month)+"-"+(dayOfMonth<10?"0"+dayOfMonth:dayOfMonth);
            long startTime = TimeUtils.string2Millis(date+" 00:00:00");
            long endTime = TimeUtils.string2Millis(date+" 23:59:59");
            ThreadUtils.getSinglePool().execute(() -> {
                searchRecordsFromDate(date, startTime, endTime);
            });

        });
        datePickerDialog.setCancelable(true);
        datePickerDialog.show();
    }

    private void searchRecordsFromDate(String date, long startTime, long endTime) {
        List<LockRecord> lockRecords = AppDatabase
                .getInstance(OperationRecordsActivity.this)
                .lockRecordDao()
                .findLockRecordsFromDeviceIdAndDay(mDeviceId, startTime/1000, endTime/1000);
        Timber.d("showDatePicker startDate 选择的日期%1s, startTime：%2d, endTime: %3d",date, startTime, endTime);
        mLockRecords.clear();
        if(lockRecords == null || lockRecords.isEmpty()) {
            dismissLoading();
            Timber.e("showDatePicker lockRecords is empty");
        } else {
            mLockRecords.addAll(lockRecords);
        }
        refreshUIFromFinalData();
    }

}
