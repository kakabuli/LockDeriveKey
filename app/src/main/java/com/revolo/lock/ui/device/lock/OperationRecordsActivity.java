package com.revolo.lock.ui.device.lock;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.adapter.AutoMeasureLinearLayoutManager;
import com.revolo.lock.adapter.OperationRecordsAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.BleBean;
import com.revolo.lock.bean.test.TestOperationRecords;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 操作记录
 */
public class OperationRecordsActivity extends BaseActivity {

    private OperationRecordsAdapter mRecordsAdapter;
    private BleBean mBleBean;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleBean = App.getInstance().getBleBean();
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
    }

    @Override
    public void doBusiness() {
        initDevice();
        initDataFromLock();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    protected void onDestroy() {
        App.getInstance().clearBleDeviceListener();
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
            BleResultProcess.processReceivedData(value, mBleBean.getPwd1(), null,
                    mBleBean.getOKBLEDeviceImp().getBleScanResult());
        }

        @Override
        public void onWriteValue(String uuid, byte[] value, boolean success) {

        }
    };

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        if(bleResultBean.getCMD() == BleProtocolState.CMD_LOCK_OP_RECORD) {
            @BleProtocolState.LockRecordOpEventType int event = bleResultBean.getPayload()[5];
            switch (event) {
                case BleProtocolState.LOCK_RECORD_OP_EVENT_TYPE_ALARM:
                    processAlarmRecord(bleResultBean);
                    break;
                case BleProtocolState.LOCK_RECORD_OP_EVENT_TYPE_OP:
                    processOpRecord(bleResultBean);
                    break;
                case BleProtocolState.LOCK_RECORD_OP_EVENT_TYPE_PROGRAM:
                    processProgramRecord(bleResultBean);
                    break;
                default:
                    // TODO: 2021/1/27 类型错误，其实可以什么都不处理
                    break;
            }
        }
    };

    private void processAlarmRecord(BleResultBean bean) {

    }

    private void processProgramRecord(BleResultBean bean) {

    }

    private void processOpRecord(BleResultBean bean) {

    }

    private void initDevice() {
        mBleBean = App.getInstance().getBleBean();
        if (mBleBean.getOKBLEDeviceImp() != null) {
            App.getInstance().openPairNotify();
            App.getInstance().setOnBleDeviceListener(mOnBleDeviceListener);
        }
    }

    private void initDataFromLock() {
        if(mBleBean.getOKBLEDeviceImp() != null) {
            if (mBleBean.getOKBLEDeviceImp().isConnected()) {
                // 查询20条记录
                byte[] start = new byte[2];
                byte[] end = new byte[2];
                end[0] = 0x14;
                App.getInstance().writeControlMsg(BleCommandFactory
                        .lockOperateRecordCommand(start, end, mBleBean.getPwd1(), mBleBean.getPwd2or3()));
            } else {
                // TODO: 2021/1/26 没有连接上，需要连接上才能发送指令
            }
        }
    }
    

    private void initData() {
        List<TestOperationRecords> testOperationRecordsList = new ArrayList<>();
        List<TestOperationRecords.TestOperationRecord> records = new ArrayList<>();
        TestOperationRecords.TestOperationRecord record13 =
                new TestOperationRecords.TestOperationRecord(1611062222000L, "password unlock", 13);
        records.add(record13);
        TestOperationRecords.TestOperationRecord record14 =
                new TestOperationRecords.TestOperationRecord(1611062222000L, "Geo-fence unlock",14);
        records.add(record14);
        TestOperationRecords.TestOperationRecord record15 =
                new TestOperationRecords.TestOperationRecord(1611062222000L, "APP unlock",15);
        records.add(record15);
        TestOperationRecords.TestOperationRecord record16 =
                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",16);
        records.add(record16);
        TestOperationRecords.TestOperationRecord record17 =
                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",17);
        records.add(record17);
        TestOperationRecords.TestOperationRecord record18 =
                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",18);
        records.add(record18);
        TestOperationRecords.TestOperationRecord record19 =
                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",19);
        records.add(record19);
        TestOperationRecords.TestOperationRecord record20 =
                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",20);
        records.add(record20);
        TestOperationRecords.TestOperationRecord record21 =
                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",21);
        records.add(record21);
        TestOperationRecords.TestOperationRecord record22 =
                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",22);
        records.add(record22);
        TestOperationRecords testOperationRecords = new TestOperationRecords(1611062222000L, records);
        testOperationRecordsList.add(testOperationRecords);

        List<TestOperationRecords.TestOperationRecord> records1 = new ArrayList<>();
        TestOperationRecords.TestOperationRecord record1 =
                new TestOperationRecords.TestOperationRecord(1610506800000L, "password unlock", 1);
        records1.add(record1);
        TestOperationRecords.TestOperationRecord record2 =
                new TestOperationRecords.TestOperationRecord(1610505000000L, "Geo-fence unlock",2);
        records1.add(record2);
        TestOperationRecords.TestOperationRecord record3 =
                new TestOperationRecords.TestOperationRecord(1610504880000L, "APP unlock",3);
        records1.add(record3);
        TestOperationRecords.TestOperationRecord record4 =
                new TestOperationRecords.TestOperationRecord(1610504880000L, "Manual unlock",4);
        records1.add(record4);
        TestOperationRecords testOperationRecords1 = new TestOperationRecords(1610504880000L, records1);
        testOperationRecordsList.add(testOperationRecords1);

        List<TestOperationRecords.TestOperationRecord> records2 = new ArrayList<>();
        TestOperationRecords.TestOperationRecord record5 =
                new TestOperationRecords.TestOperationRecord(1610418480000L, "Locking inside the door",5);
        records2.add(record5);
        TestOperationRecords.TestOperationRecord record6 =
                new TestOperationRecords.TestOperationRecord(1610418480000L, "Double lock inside the door",6);
        records2.add(record6);
        TestOperationRecords.TestOperationRecord record7 =
                new TestOperationRecords.TestOperationRecord(1610418480000L, "Multi-functional button locking",7);
        records2.add(record7);
        TestOperationRecords.TestOperationRecord record8 =
                new TestOperationRecords.TestOperationRecord(1610418480000L, "One-touch lock outside the door ",8);
        records2.add(record8);
        TestOperationRecords testOperationRecords2 = new TestOperationRecords(1610418480000L, records2);
        testOperationRecordsList.add(testOperationRecords2);

        List<TestOperationRecords.TestOperationRecord> records3 = new ArrayList<>();
        TestOperationRecords.TestOperationRecord record9 =
                new TestOperationRecords.TestOperationRecord(1608863280000L, "Duress password unlock",9);
        records3.add(record9);
        TestOperationRecords.TestOperationRecord record10 =
                new TestOperationRecords.TestOperationRecord(1608863280000L, "lock down alarm",10);
        records3.add(record10);
        TestOperationRecords.TestOperationRecord record11 =
                new TestOperationRecords.TestOperationRecord(1608863280000L, "Low battery alarm",11);
        records3.add(record11);
        TestOperationRecords.TestOperationRecord record12 =
                new TestOperationRecords.TestOperationRecord(1608863280000L, "Jam alarm",12);
        records3.add(record12);
        TestOperationRecords testOperationRecords3 = new TestOperationRecords(1608863280000L, records3);
        testOperationRecordsList.add(testOperationRecords3);

        mRecordsAdapter.setList(testOperationRecordsList);
    }

}
