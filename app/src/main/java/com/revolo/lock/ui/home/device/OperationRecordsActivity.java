package com.revolo.lock.ui.home.device;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.revolo.lock.R;
import com.revolo.lock.adapter.AutoMeasureLinearLayoutManager;
import com.revolo.lock.adapter.OperationRecordsAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.TestOperationRecords;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 操作记录
 */
public class OperationRecordsActivity extends BaseActivity {

    private OperationRecordsAdapter mRecordsAdapter;

    @Override
    public void initData(@Nullable Bundle bundle) {

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
        initData();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void initData() {
        List<TestOperationRecords> testOperationRecordsList = new ArrayList<>();
        List<TestOperationRecords.TestOperationRecord> records1 = new ArrayList<>();
        TestOperationRecords.TestOperationRecord record1 =
                new TestOperationRecords.TestOperationRecord(1610506800000L, "password unlock");
        records1.add(record1);
        TestOperationRecords.TestOperationRecord record2 =
                new TestOperationRecords.TestOperationRecord(1610505000000L, "Geo-fence unlock");
        records1.add(record2);
        TestOperationRecords.TestOperationRecord record3 =
                new TestOperationRecords.TestOperationRecord(1610504880000L, "APP unlock");
        records1.add(record3);
        TestOperationRecords.TestOperationRecord record4 =
                new TestOperationRecords.TestOperationRecord(1610504880000L, "Manual unlock");
        records1.add(record4);
        TestOperationRecords testOperationRecords1 = new TestOperationRecords(1610504880000L, records1);
        testOperationRecordsList.add(testOperationRecords1);

        List<TestOperationRecords.TestOperationRecord> records2 = new ArrayList<>();
        TestOperationRecords.TestOperationRecord record5 =
                new TestOperationRecords.TestOperationRecord(1610418480000L, "Locking inside the door");
        records2.add(record5);
        TestOperationRecords.TestOperationRecord record6 =
                new TestOperationRecords.TestOperationRecord(1610418480000L, "Double lock inside the door");
        records2.add(record6);
        TestOperationRecords.TestOperationRecord record7 =
                new TestOperationRecords.TestOperationRecord(1610418480000L, "Multi-functional button locking");
        records2.add(record7);
        TestOperationRecords.TestOperationRecord record8 =
                new TestOperationRecords.TestOperationRecord(1610418480000L, "One-touch lock outside the door ");
        records2.add(record8);
        TestOperationRecords testOperationRecords2 = new TestOperationRecords(1610418480000L, records2);
        testOperationRecordsList.add(testOperationRecords2);

        List<TestOperationRecords.TestOperationRecord> records3 = new ArrayList<>();
        TestOperationRecords.TestOperationRecord record9 =
                new TestOperationRecords.TestOperationRecord(1608863280000L, "Duress password unlock");
        records3.add(record9);
        TestOperationRecords.TestOperationRecord record10 =
                new TestOperationRecords.TestOperationRecord(1608863280000L, "lock down alarm");
        records3.add(record10);
        TestOperationRecords.TestOperationRecord record11 =
                new TestOperationRecords.TestOperationRecord(1608863280000L, "Low battery alarm");
        records3.add(record11);
        TestOperationRecords.TestOperationRecord record12 =
                new TestOperationRecords.TestOperationRecord(1608863280000L, "Jam alarm");
        records3.add(record12);
        TestOperationRecords testOperationRecords3 = new TestOperationRecords(1608863280000L, records3);
        testOperationRecordsList.add(testOperationRecords3);

        mRecordsAdapter.setList(testOperationRecordsList);
    }

}
