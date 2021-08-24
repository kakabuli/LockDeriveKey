package com.revolo.lock.ui.device.lock;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.contrarywind.view.WheelView;
import com.revolo.lock.App;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.adapter.OpRecordsAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.OperationRecords;
import com.revolo.lock.bean.request.LockRecordBeanReq;
import com.revolo.lock.bean.respone.LockRecordBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.LockRecord;
import com.revolo.lock.ui.view.SmartClassicsHeaderView;
import com.revolo.lock.util.ZoneUtil;
import com.revolo.lock.widget.MyTimePickBuilder;
import com.revolo.lock.widget.MyTimePickerView;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
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

    private OpRecordsAdapter mOpRecordsAdapter;
    private BleBean mBleBean;
    private LinearLayout mllNoRecord;
    private ExpandableListView mElOperationRecords;
    private BleDeviceLocal mBleDeviceLocal;

    private RefreshLayout mRefreshLayout;

    private int mPage = 1;

    private long startTime = 0;
    private long endTime = 0;
    private boolean isCheckTime = false;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
        mBleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_operation_records;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        isCheckTime = false;
        useCommonTitleBar(getString(R.string.title_operation_records))
                .setRight(R.drawable.ic_icon_date, v -> {
                    showDatePicker();
                });
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        mElOperationRecords = findViewById(R.id.elOperationRecords);
        mElOperationRecords.setGroupIndicator(null);
        mllNoRecord = findViewById(R.id.llNoRecord);
        mOpRecordsAdapter = new OpRecordsAdapter(new ArrayList<>(), this);
        mOpRecordsAdapter.setTimeZone(mBleDeviceLocal.getTimeZone());
        mElOperationRecords.setAdapter(mOpRecordsAdapter);
        initLoading(getString(R.string.t_load_content_loading));

        mRefreshLayout = findViewById(R.id.refreshLayout);
        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setRefreshHeader(new SmartClassicsHeaderView(this));
        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            mPage = 1;
            isCheckTime = false;
            searchRecord(0);
        });
        mRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
            searchRecord(1);
        });
        onRegisterEventBus();
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            mWillShowRecords = App.getInstance().getLockRecords(mBleDeviceLocal.getEsn());
            if (null == mWillShowRecords) {
                mWillShowRecords = new ArrayList<>();
            }
            refreshUIFromFinalData();
        }
        if (null == mWillShowRecords) {
            mWillShowRecords = new ArrayList<>();
        }

    }

    /**
     * 获取记录
     */
    private void searchRecord(long startTime, long endTime) {
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            //蓝牙模式
            searchBle();
        } else {
            //WiFi模式 、WiFi和蓝牙同时连接、掉线模式
            searchRecordFromNet(mPage, startTime, endTime);
        }

    }

    private void searchRecord(int getType) {
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            //蓝牙模式
            if (getType == 0) {
                mBleSearchStart = 0;
                mBleSearchEnd = 14;
                searchBle();
            } else {
                mBleSearchStart = (short) (mWillShowRecords.size() - 1);
                mBleSearchEnd = (short) (mWillShowRecords.size() + 13);
                searchBle();
            }
        } else {
            //WiFi模式 、WiFi和蓝牙同时连接、掉线模式
            getNowDateTime();
            searchRecordFromNet(mPage, startTime, endTime);
        }

    }

    /**
     * 从蓝牙端获取记录
     */
    private void searchBle() {
        // 开始检索, 每次检索5条
        searchRecordFromBle(mBleSearchStart, mBleSearchEnd);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_USER) {

        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            if (null != lockMessage.getBleResultBea()) {
                if (lockMessage.getBleResultBea().getCMD() == CMD_GET_ALL_RECORD) {
                    updateRecordFormBle(lockMessage.getBleResultBea());
                }
            }
        } else {
            //MQTT
        }
    }

    @Override
    public void doBusiness() {
        searchRecord(0);
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            App.getInstance().addLockRecords(mBleDeviceLocal.getEsn(), mWillShowRecords);
        }
    }

    private void searchRecordFromBle(short start, short end) {
        isNeedToReceiveRecord = true;
        mWillUploadRecord.clear();
        if (mBleBean.getOKBLEDeviceImp() != null) {
            if (mBleBean.getOKBLEDeviceImp().isConnected()) {
                // 因为shortToBytes转出来就是小端模式，所以调用直接使用小端模式的方法
                LockMessage message = new LockMessage();
                message.setBytes(BleCommandFactory
                        .readAllRecordFromSmallEndian(BleByteUtil.shortToBytes(start), BleByteUtil.shortToBytes(end),
                                mBleBean.getPwd1(), mBleBean.getPwd3()));
                message.setMac(mBleBean.getOKBLEDeviceImp().getMacAddress());
                message.setMessageType(3);
                EventBus.getDefault().post(message);
            } else {
                // TODO: 2021/1/26 没有连接上，需要连接上才能发送指令
            }
        }
    }

    private boolean isNeedToReceiveRecord = true;
    private static final short mWillAddCheckCount = 15;

    private void updateRecordFormBle(BleResultBean bean) {
        if (mRefreshLayout != null) {
            mRefreshLayout.finishLoadMore(true);
            mRefreshLayout.finishRefresh(true);
        }
        if (!isNeedToReceiveRecord) {
            // 不再接收数据的标志位, 不再处理数据
            return;
        }
        byte[] total = new byte[2];
        System.arraycopy(bean.getPayload(), 0, total, 0, total.length);
        short totalShort = BleByteUtil.bytesToShortFromLittleEndian(total);
        if (totalShort == 0) {
            // 数据是0条，不再处理
            return;
        }
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

        if (totalShort == mWillShowRecords.size()) {
            Timber.e("数据相等");
            return;
        }
        LockRecord lockRecord = new LockRecord();
        lockRecord.setDeviceId(mBleDeviceLocal.getId());
        lockRecord.setAppId(appId);
        lockRecord.setUserId(userId);
        lockRecord.setCreateTime(realTime);
        lockRecord.setEventCode(eventCode);
        lockRecord.setEventSource(eventSource);
        lockRecord.setEventType(eventType);
        mWillUploadRecord.add(lockRecord);
        // 因为index下标从0开始，所以要+1
        if ((totalShort - (indexShort + 1)) > 0) {
            if (indexShort == mBleSearchEnd) {
                // 当前下标的是预设最后的数据记录 预示着读取记录结束了, 开始下一次的记录读取
                if (!isNextSean(mBleSearchStart, mWillUploadRecord)) {
                    return;
                }
                mBleSearchStart = (short) (mBleSearchStart + mWillAddCheckCount);
                mBleSearchEnd = (totalShort - (indexShort + 1)) >= mWillAddCheckCount ? ((short) (mBleSearchEnd + mWillAddCheckCount)) : ((short) (totalShort - 1));
                searchRecordFromBle(mBleSearchStart, mBleSearchEnd);
            }
        } else {
            // 最后一包的数据
            isNextSean(mBleSearchStart, mWillUploadRecord);
        }

    }

    /**
     * @param startIndex 开始index位置
     * @param records    待添加的数据集
     * @return true 继续加载、false停止加载
     */
    private boolean isNextSean(int startIndex, List<LockRecord> records) {
        boolean isNext = true;
        for (int i = 0; i < records.size(); i++) {
            if (i + startIndex < mWillShowRecords.size()) {
                //当前index 存在
                if (records.get(i).getContentStr().equals(mWillShowRecords.get(i + startIndex).getContentStr())) {
                    isNext = false;
                    break;
                } else {
                    mWillShowRecords.add(i + startIndex, records.get(i));
                }
            } else {
                //当前index 不存在，直接添加
                mWillShowRecords.add(records.get(i));
            }
        }
        refreshUIFromFinalData();
        Timber.e("will len:" + mWillShowRecords.size() + ";;;;;;;;;" + isNext);
        return isNext;
    }

    private void getNowDateTime() {
        String s = TimeUtils.date2String(new Date(), "yyyy-MM-dd");
        startTime = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), s + " 00:00:00") / 1000;
        endTime = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), s + " 23:59:59") / 1000;
    }

    private final ArrayList<LockRecord> mWillUploadRecord = new ArrayList<>();

    private short mBleSearchStart = 0;
    private short mBleSearchEnd = 14;

    /*--------------------------------- 本地数据库 -------------------------------------*/

    private void searchRecordFromLocal(int num, int page) {
        List<LockRecord> lockRecords = AppDatabase
                .getInstance(this)
                .lockRecordDao()
                .findLockRecordsFromDeviceId(mBleDeviceLocal.getId(), num, page);
        if (lockRecords == null) {
            return;
        }
        if (lockRecords.isEmpty()) {
            return;
        }
        if (mPage == 1) {
            mWillShowRecords.clear();
        }
        mWillShowRecords.addAll(lockRecords);
        refreshUIFromFinalData();
    }

    private void searchRecordFromLocal(int page) {
        searchRecordFromLocal(20, page);
    }

    private void searchAllRecordFromLocal() {
        List<LockRecord> lockRecords = AppDatabase
                .getInstance(this)
                .lockRecordDao()
                .findLockRecordsFromDeviceId(mBleDeviceLocal.getId());
        if (lockRecords == null) {
            return;
        }
        if (lockRecords.isEmpty()) {
            return;
        }
        mWillShowRecords.addAll(lockRecords);
        refreshUIFromFinalData();
    }

    /*--------------------------------- 服务器 -------------------------------------*/

    private void searchRecordFromNet(int page, long startTime, long endTime) {
        if (!checkNetConnectFail()) {
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("searchRecordFromNet App.getInstance().getUserBean()  == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("searchRecordFromNet token is empty");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("searchRecordFromNet uid is empty");
            return;
        }
        String esn = mBleDeviceLocal.getEsn();
        if (TextUtils.isEmpty(esn)) {
            Timber.e("searchRecordFromNet esn is empty");
            return;
        }
        if (!isCheckTime) {
            // 不是筛选日期 开始时间从零开始
            startTime = 0;
        }

        LockRecordBeanReq req = new LockRecordBeanReq();
        req.setPage(page);
        req.setUid(uid);
        req.setDeviceSN(esn);
        req.setStartTime(startTime);
        req.setEndTime(endTime);
        showLoading();
        Observable<LockRecordBeanRsp> observable = HttpRequest.getInstance().getLockRecordList(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<LockRecordBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull LockRecordBeanRsp lockRecordBeanRsp) {
                dismissLoading();
                String code = lockRecordBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("searchRecordFromNet code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, OperationRecordsActivity.this);
                        return;
                    }
                    String msg = lockRecordBeanRsp.getMsg();
                    Timber.e("searchRecordFromNet code: %1s, msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                List<LockRecordBeanRsp.DataBean> beans = lockRecordBeanRsp.getData();
                processRecordFromNet(beans);
                if (mRefreshLayout != null) {
                    mRefreshLayout.finishLoadMore(true);
                    mRefreshLayout.finishRefresh(true);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                // TODO: 2021/3/18 如果是第一页加载本地数据库
                dismissLoading();
                Timber.e(e);
                if (mRefreshLayout != null) {
                    mRefreshLayout.finishLoadMore(false);
                    mRefreshLayout.finishRefresh(false);
                }
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private long mLatestCreateTime = 0L;
    private List<LockRecord> mWillShowRecords;

    private void processRecordFromNet(List<LockRecordBeanRsp.DataBean> beans) {
        // TODO: 2021/3/18 时间错误就不能存储
        if (beans.isEmpty()) {
            Timber.e("processRecordFromNet beans is empty");
            ToastUtils.showShort(getString(R.string.data_no_records));
            return;
        }
        if (beans.size() == 0) {
            ToastUtils.showShort(getString(R.string.data_no_records));
            return;
        }
        // 不做校验，直接做存储并数据库做了插入去重
        List<LockRecord> list = new ArrayList<>();
        for (LockRecordBeanRsp.DataBean bean : beans) {
            LockRecord lockRecord = new LockRecord();
            lockRecord.setUserId(bean.getUserId());
            lockRecord.setEventType(bean.getEventType());
            lockRecord.setEventSource(bean.getEventSource());
            lockRecord.setEventCode(bean.getEventCode());
            lockRecord.setCreateTime(bean.getTimesTamp());
            lockRecord.setPwdNickname(bean.getPwdNickname());
            lockRecord.setLastName(bean.getLastName());
            lockRecord.setAppId(bean.getAppId());
            lockRecord.setDeviceId(mBleDeviceLocal.getId());
            list.add(lockRecord);
        }
        AppDatabase.getInstance(this).lockRecordDao().insert(list);

        if (mPage == 1) {
            mWillShowRecords.clear();
        }
        mWillShowRecords.addAll(list);
        refreshUIFromFinalData();
        mPage++;

    }
    /*--------------------------------- UI更新 -------------------------------------*/

    private void showOrDismissNoRecord(boolean isShow) {
        runOnUiThread(() -> {
            if (isShow) {
                mllNoRecord.setVisibility(View.VISIBLE);
            } else {
                mllNoRecord.setVisibility(View.GONE);
            }
        });
    }

    private void showOrDismissRecords(boolean isShow) {
        runOnUiThread(() -> {
            if (isShow) {
                mElOperationRecords.setVisibility(View.VISIBLE);
            } else {
                mElOperationRecords.setVisibility(View.GONE);
            }
        });
    }

    /*--------------------------------- 数据清洗 -------------------------------------*/

    private void refreshUIFromFinalData() {
        // TODO: 2021/2/25 后面要做的是要代理处理的数据
        if (mWillShowRecords.isEmpty()) {
            showOrDismissNoRecord(true);
            showOrDismissRecords(false);
            return;
        } else {
            showOrDismissNoRecord(false);
            showOrDismissRecords(true);
        }
        List<OperationRecords.OperationRecord> records = new ArrayList<>();
        for (LockRecord lockRecord : mWillShowRecords) {
            // eventCode 1:上锁 2:开锁
            OperationRecords.OperationRecord record;
            boolean isAlarmRecord = false;
            String lockName = mBleDeviceLocal.getName();
            if (TextUtils.isEmpty(lockName)) {
                lockName = mBleDeviceLocal.getEsn();
            }
            String lastName = lockRecord.getLastName();
            if (TextUtils.isEmpty(lastName)) {
                lastName = "";
            }
            String pwdName = lockRecord.getPwdNickname();
            if (TextUtils.isEmpty(pwdName)) {
                pwdName = "";
            }
            String message = "";
            @DrawableRes int drawableId = R.drawable.ic_home_log_icon_password;
            if (lockRecord.getEventType() == 1) {
                // 操作类
                switch (lockRecord.getEventCode()) {
                    case 0x07:
                        // 一键上锁
                        message = "One-touch lock";
                        drawableId = R.drawable.ic_home_log_icon_door_lock;
                        break;
                    case 0x08:
                    case 0x12:
                        // 反锁
                        // 手动上锁
                        message = lockName + " Locked Manually";
                        drawableId = R.drawable.ic_home_log_icon_door_lock;
                        break;
                    case 0x09:
                        // 机械钥匙开锁
                        message = lockName + " Unlocked Manually";
                        drawableId = R.drawable.ic_home_log_icon_key;
                        break;
                    case 0x0A:
                        // 自动上锁
                        message = lockName + " Auto-Locked";
                        drawableId = R.drawable.ic_home_log_icon_door_lock;
                        break;
                    case 0x10:
                        // 敲击开锁
                        // TODO: 2021/3/29 通过编号识别对应用户, 下面记录还有
                        message = lockName + " Auto-Unlock";
                        drawableId = R.drawable.ic_home_log_icon_geo_fence;
                        break;
                    case 0x11:
                        // 触摸开锁
                        message = "One-touch Unlock";
                        drawableId = R.drawable.ic_home_log_icon_door_open;
                        break;
                    case 0x13:
                        // 门磁检测开门
                        message = "Detection of door opened";
                        drawableId = R.drawable.ic_home_log_icon_door_open;
                        break;
                    case 0x14:
                        // 门磁检测关门
                        message = "Detection of closed door";
                        drawableId = R.drawable.ic_home_log_icon_door_close;
                        break;
                    case 0x01:
                        // 上锁
                        switch (lockRecord.getEventSource()) {
                            case 0x00:
                                // 键盘
                                message = lastName + " Locked " + lockName + " via PIN Key";
                                drawableId = R.drawable.ic_home_log_icon_password;
                                break;
                            case 0x08:
                                // App
                                // TODO: 2021/3/29 通过编号识别对应用户
                                message = lastName + " Locked " + lockName + " via App";
                                drawableId = R.drawable.ic_home_log_icon_door_lock;
                                break;
                            default:
                                Timber.e("event code: %1d, event source: %2d",
                                        lockRecord.getEventCode(), lockRecord.getEventSource());
                                break;
                        }
                        break;
                    case 0x02:
                        // 开锁
                        switch (lockRecord.getEventSource()) {
                            case 0x00:
                                // 键盘
                                message = lastName + " Unlocked " + lockName + " via PIN Key";
                                drawableId = R.drawable.ic_home_log_icon_password;
                                break;
                            case 0x08:
                                // App
                                message = lastName + " Unlocked " + lockName + " via App";
                                drawableId = R.drawable.ic_home_log_icon_iphone;
                                break;
                            default:
                                Timber.e("event code: %1d, event source: %2d",
                                        lockRecord.getEventCode(), lockRecord.getEventSource());
                                break;
                        }
                        break;
                    default:
                        Timber.e("event code: %1d", lockRecord.getEventCode());
                        break;
                }
            } else if (lockRecord.getEventType() == 2) {
                // 程序类
                switch (lockRecord.getEventCode()) {
                    case 0x02:
                        // 密码添加
                        message = lastName + " Added " + pwdName + " PIN Key";
                        drawableId = R.drawable.ic_home_log_icon_password;
                        break;
                    case 0x03:
                        // 密码删除
                        message = lastName + " Deleted " + pwdName + " PIN Key";
                        drawableId = R.drawable.ic_home_log_icon_password;
                        break;
                    case 0x0f:
                        // 恢复出厂设置
                        message = "Your Lock has been Reset to Factory Default Settings";
                        drawableId = R.drawable.ic_home_log_icon_restore;
                        break;
                    default:
                        Timber.e("");
                        break;
                }
            } else if (lockRecord.getEventType() == 3) {
                // 报警类
                switch (lockRecord.getEventCode()) {
                    case 0x01:
                        // 锁定报警
                        message = "Device Lookout Alarm";
                        drawableId = R.drawable.ic_home_log_icon_alert;
                        break;
                    case 0x02:
                        // 胁迫密码报警
                        message = "Emergency PIN Key Unlock Alarm";
                        drawableId = R.drawable.ic_home_log_icon_alert;
                        break;
                    case 0x03:
                        // 三次错误报警
                        message = "Three Input Error Alarm";
                        drawableId = R.drawable.ic_home_log_icon_alert;
                        break;
                    case 0x04:
                        // 防撬报警(锁被撬开)
                        message = "The lock was Picked Alarm";
                        drawableId = R.drawable.ic_home_log_icon_alert;
                        break;
                    case 0x08:
                        // 机械钥匙报警
                        message = "Mechanical Key Alarm";
                        drawableId = R.drawable.ic_home_log_icon_alert;
                        break;
                    case 0x10:
                        // 低电压报警
                        message = "Low Battery Alarm";
                        drawableId = R.drawable.ic_home_log_icon_alert;
                        break;
                    case 0x20:
                        // 锁体异常报警
                        message = "Abnormal Lock Body Alarm";
                        drawableId = R.drawable.ic_home_log_icon_alert;
                        break;
                    case 0x40:
                        // 门锁布防报警
                        message = "Door Lock Arming";
                        drawableId = R.drawable.ic_home_log_icon_alert;
                        break;
                    case 0x41:
                        // 堵转报警
                        message = "Door Ajar Alarm";
                        drawableId = R.drawable.ic_home_log_icon_alert;
                        break;
                    default:
                        break;
                }
            } else if (lockRecord.getEventType() == 4) {
                // 门磁开关门

            }

            if (TextUtils.isEmpty(message)) {
                Timber.e("本地记录 eventType: %3d, eventSource: %4d, eventCode: %5d, userId: %6d, appId: %7d, time: %8d",
                        lockRecord.getEventType(), lockRecord.getEventSource(), lockRecord.getEventCode(),
                        lockRecord.getUserId(), lockRecord.getAppId(), lockRecord.getCreateTime() * 1000);
                continue;
            }
            record = new OperationRecords.OperationRecord(lockRecord.getCreateTime() * 1000, message, drawableId, isAlarmRecord);
            records.add(record);
        }
        dismissLoading();
        processRightRecords(records);
    }

    /**
     * 使用 Map按key进行排序
     *
     * @param map
     * @return
     */
    public static Map<String, List<OperationRecords.OperationRecord>> sortMapByKey(Map<String,
            List<OperationRecords.OperationRecord>> map) {
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

    /*--------------------------------- 日期筛选 -------------------------------------*/

    private void processRightRecords(List<OperationRecords.OperationRecord> records) {
        // 日期分类筛选
        Map<String, List<OperationRecords.OperationRecord>> collect = records
                .stream().collect(Collectors.groupingBy(OperationRecords.OperationRecord::getDate));
        // 时间降序
        Map<String, List<OperationRecords.OperationRecord>> sortCollect = sortMapByKey(collect);
        List<OperationRecords> recordsList = new ArrayList<>();
        for (String key : sortCollect.keySet()) {
            Timber.d("processRightRecords key: %1s", key);
            OperationRecords operationRecords = new OperationRecords(ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), key, "yyyy-MM-dd"), collect.get(key));
            recordsList.add(operationRecords);
        }
        runOnUiThread(() -> {
            initAdapter(recordsList);
            if (mRefreshLayout != null) {
                mRefreshLayout.finishLoadMore(true);
            }
        });
    }

    private void initAdapter(List<OperationRecords> recordsList) {
        mOpRecordsAdapter.setOperationRecords(recordsList);
        int count = mOpRecordsAdapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            mElOperationRecords.expandGroup(i);
        }
    }

    private void showDatePicker() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        MyTimePickBuilder timePickerBuilder = new MyTimePickBuilder(this, (date, v) -> {
            OperationRecordsActivity.this.showLoading();
            String time = dateFormat.format(date);
            long startTime = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), time + " 00:00:00") / 1000;
            long endTime = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), time + " 23:59:59") / 1000;
            mPage = 1;
            isCheckTime = true;
            searchRecord(startTime, endTime);
        });

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse("2019-01-01", pos);
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(strtodate);

        timePickerBuilder.setLayoutRes(R.layout.dialog_picker_view_time, null)
                .setCancelColor(Color.parseColor("#999999"))
                .setDividerColor(Color.parseColor("#f7f7f7"))
                .setSubmitColor(Color.parseColor("#2c68ff"))
                .setTitleColor(Color.parseColor("#333333"))
                .setTextColorCenter(Color.parseColor("#333333"))
                .setTextColorOut(Color.parseColor("#999999"))
                .setDividerColor(Color.parseColor("#f7f7f7"))
                .isCenterLabel(false).setCancelText("cancel")
                .setSubmitText("confirm")
                .setBgColor(Color.parseColor("#ffffff"))
                .setItemVisibleCount(3).setContentTextSize(16)
                .setTitleBgColor(Color.parseColor("#ffffff"))
                .setLabel("", "", "", "", "", "")
                .setLineSpacingMultiplier(3f)
                .setDividerType(WheelView.DividerType.FILL)
                .setRangDate(startTime, Calendar.getInstance())
                .setTextXOffset(0, 0, 0, 0, 0, 0);

        MyTimePickerView timePickerView = timePickerBuilder.build();
        timePickerView.setDate(Calendar.getInstance());
        timePickerView.setTitleText("Date");
        timePickerView.show();

    }

    private void searchRecordsFromDate(String date, long startTime, long endTime) {
        List<LockRecord> lockRecords = AppDatabase
                .getInstance(OperationRecordsActivity.this)
                .lockRecordDao()
                .findLockRecordsFromDeviceIdAndDay(mBleDeviceLocal.getId(), startTime / 1000, endTime / 1000);
        Timber.d("showDatePicker startDate 选择的日期%1s, startTime：%2d, endTime: %3d", date, startTime, endTime);
        mWillShowRecords.clear();
        if (lockRecords == null || lockRecords.isEmpty()) {
            dismissLoading();
            Timber.e("showDatePicker lockRecords is empty");
        } else {
            mWillShowRecords.addAll(lockRecords);
        }
        refreshUIFromFinalData();
    }
}
