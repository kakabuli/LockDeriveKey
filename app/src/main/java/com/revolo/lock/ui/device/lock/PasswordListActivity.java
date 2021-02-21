package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.PasswordListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.test.TestPwdBean;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.DevicePwd;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_TIME_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_WEEK_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_TYPE_PWD;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ATTRIBUTES_READ;
import static com.revolo.lock.ble.BleProtocolState.CMD_SY_KEY_STATE;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 密码列表界面
 */
public class PasswordListActivity extends BaseActivity {

    private PasswordListAdapter mPasswordListAdapter;
    private long mDeviceId;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.DEVICE_ID)) {
            mDeviceId = intent.getLongExtra(Constant.DEVICE_ID, -1L);
            Timber.d("Device Id: %1d", mDeviceId);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_password_list;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.password))
                .setRight(ContextCompat.getDrawable(this, R.drawable.ic_home_icon_add),
                        v -> {
                    if(mDeviceId == -1) {
                        return;
                    }
                    Intent intent = new Intent(this, AddInputNewPwdActivity.class);
                    intent.putExtra(Constant.DEVICE_ID, mDeviceId);
                    startActivity(intent);
                });
        RecyclerView rvPwdList = findViewById(R.id.rvPwdList);
        rvPwdList.setLayoutManager(new LinearLayoutManager(this));
        mPasswordListAdapter = new PasswordListAdapter(R.layout.item_pwd_list_rv);
        mPasswordListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if(position >= 0 && adapter.getItem(position) instanceof DevicePwd) {
                Intent intent = new Intent(PasswordListActivity.this, PasswordDetailActivity.class);
                DevicePwd item  = (DevicePwd) adapter.getItem(position);
                intent.putExtra(Constant.PWD_ID, item.getId());
                startActivity(intent);
            }
        });
        rvPwdList.setAdapter(mPasswordListAdapter);
    }

    @Override
    public void doBusiness() {
//        initBleListener();
        searchPwdListFromLocal();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        getPwdListFormBle(bleResultBean);
    };

    private void initBleListener() {
        App.getInstance().setOnBleDeviceListener(new OnBleDeviceListener() {
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
                BleResultProcess.processReceivedData(value,
                        App.getInstance().getBleBean().getPwd1(),
                        App.getInstance().getBleBean().getPwd3(),
                        App.getInstance().getBleBean().getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(String uuid, byte[] value, boolean success) {

            }
        });
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mWillSearchList.clear();
//                mPasswordListAdapter.setList(mTestPwdBeans);
//                App.getInstance().writeControlMsg(BleCommandFactory
//                        .synchronizeLockKeyStatusCommand((byte) 0x01,
//                                App.getInstance().getBleBean().getPwd1(),
//                                App.getInstance().getBleBean().getPwd3()));
//                Timber.d("发送了请求密钥列表指令");
//            }
//        }, 100);

    }

    private final ArrayList<Byte> mWillSearchList = new ArrayList<>();
    private final ArrayList<TestPwdBean> mTestPwdBeans = new ArrayList<>();

    private void getPwdListFormBle(BleResultBean bean) {
        // TODO: 2021/2/3 可能存在100条数据以上，后续需要做100条数据以上的测试
        // TODO: 2021/2/4 后续需要做去重操作
        if(bean.getCMD() == CMD_SY_KEY_STATE) {
            checkPwdIsExist(bean);
            // 查询到密钥存在后，开始读取对应密钥
            mHandler.postDelayed(mSearchPwdListRunnable, 20);
        } else if(bean.getCMD() == CMD_KEY_ATTRIBUTES_READ) {
            String name = App.getInstance().getCacheDiskUtils().getString("pwdName"+mCurrentSearchNum);
            byte attribute = bean.getPayload()[0];
            if(attribute == KEY_SET_ATTRIBUTE_ALWAYS) {
                Timber.d("getPwdListFormBle num: %1s", mCurrentSearchNum);
                TestPwdBean testPwdBean = new TestPwdBean(name, "Permanent password",
                        1, "***********", "Permanence", "02,04,2020 12:00", mCurrentSearchNum);
                mTestPwdBeans.add(testPwdBean);
            } else if(attribute == KEY_SET_ATTRIBUTE_TIME_KEY) {
                // TODO: 2021/2/7 时间高低位反回来取
                addTimePwd(bean, name);
            } else if(attribute == KEY_SET_ATTRIBUTE_WEEK_KEY) {
                // TODO: 2021/2/7 时间高低位反回来取
                addWeeklyPwd(bean, name);
            }
//            runOnUiThread(() -> mPasswordListAdapter.setList(mTestPwdBeans));
            mHandler.postDelayed(mSearchPwdListRunnable, 20);
        }
    }

    private void addTimePwd(BleResultBean bean, String name) {
        byte[] startTimeBytes = new byte[4];
        byte[] endTimeBytes = new byte[4];
        System.arraycopy(bean.getPayload(), 2, startTimeBytes, 0, startTimeBytes.length);
        System.arraycopy(bean.getPayload(), 6, endTimeBytes, 0, endTimeBytes.length);
        long startTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(startTimeBytes))*1000;
        long endTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(endTimeBytes))*1000;
        String detail = "start: "
                + TimeUtils.millis2String(startTimeMill, "MM,dd,yyyy   HH:mm")
                + "\n" + "end: "
                + TimeUtils.millis2String(endTimeMill, "MM,dd,yyyy   HH:mm");
        String characteristic = TimeUtils.millis2String(startTimeMill, "MM,dd,yyyy   HH:mm")
                + "-" + TimeUtils.millis2String(endTimeMill, "MM,dd,yyyy   HH:mm");
        TestPwdBean testPwdBean = new TestPwdBean(name, detail, 1, "***********",
                characteristic, "02,05,2020 12:00",  mCurrentSearchNum);
        mTestPwdBeans.add(testPwdBean);
    }

    private void addWeeklyPwd(BleResultBean bean, String name) {
        byte[] weekBytes = BleByteUtil.byteToBit(bean.getPayload()[1]);
        Timber.d("getPwdListFormBle num: %1s week: %1s", mCurrentSearchNum, ConvertUtils.bytes2HexString(weekBytes));
        String weekly = "";
        if(weekBytes[0] == 0x01) {
            weekly += "Sun";
        }
        if(weekBytes[1] == 0x01) {
            weekly += TextUtils.isEmpty(weekly)?"Mon":"、Mon";
        }
        if(weekBytes[2] == 0x01) {
            weekly += TextUtils.isEmpty(weekly)?"Tues":"、Tues";
        }
        if(weekBytes[3] == 0x01) {
            weekly += TextUtils.isEmpty(weekly)?"Wed":"、Wed";
        }
        if(weekBytes[4] == 0x01) {
            weekly += TextUtils.isEmpty(weekly)?"Thur":"、Thur";
        }
        if(weekBytes[5] == 0x01) {
            weekly += TextUtils.isEmpty(weekly)?"Fri":"、Fri";
        }
        if(weekBytes[6] == 0x01) {
            weekly += TextUtils.isEmpty(weekly)?"Sat":"、Sat";
        }
        weekly += "\n";
        byte[] startTimeBytes = new byte[4];
        byte[] endTimeBytes = new byte[4];
        System.arraycopy(bean.getPayload(), 2, startTimeBytes, 0, startTimeBytes.length);
        System.arraycopy(bean.getPayload(), 6, endTimeBytes, 0, endTimeBytes.length);
        long startTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(startTimeBytes))*1000;
        long endTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(endTimeBytes))*1000;
        String detail = weekly
                + TimeUtils.millis2String(startTimeMill, "HH:mm")
                + " - "
                + TimeUtils.millis2String(endTimeMill, "HH:mm");
        TestPwdBean testPwdBean = new TestPwdBean(name,
                detail, 1, "***********",
                detail,
                "02,05,2020 12:00", mCurrentSearchNum);
        mTestPwdBeans.add(testPwdBean);
    }

    private void checkPwdIsExist(BleResultBean bean) {
        mWillSearchList.clear();
        byte[] value = bean.getPayload();
        int index = value[0] & 0xff;
        int codeType = value[1] & 0xff;
        int codeNumber = value[2] & 0xff;
        Timber.d("秘钥的帧数是  %1d, 秘钥类型是  %2d  秘钥总数是   %3d", index, codeType, codeNumber);
        // TODO: 2021/2/3 密钥列表的解析，有疑问，后续需要增加解析并显示
        // 暂时项目只有20条密码极限
        // 1-8 数据是倒着来计算的从byte[7]-byte[0]
        byte[] num1 = BleByteUtil.byteToBit(value[3]);
        // 9-16
        byte[] num2 = BleByteUtil.byteToBit(value[4]);
        // 17-20
        byte[] num3 = BleByteUtil.byteToBit(value[5]);
        Timber.d("1-8: %1s, 9-16: %2s, 17-20: %3s",
                ConvertUtils.bytes2HexString(num1), ConvertUtils.bytes2HexString(num2), ConvertUtils.bytes2HexString(num3));
        // 循环判断20个内有哪些编号是存在密码的
        for (int i=7; i>=0; i--) {
            if(num1[i] == 0x01) {
                mWillSearchList.add((byte) (7-i));
            }
        }
        for (int i=7; i>=0; i--) {
            if(num2[i] == 0x01) {
                mWillSearchList.add((byte) (15-i));
            }
        }
        for (int i=7; i>=4; i--) {
            if(num3[i] == 0x01) {
                mWillSearchList.add((byte) (23-i));
            }
        }
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mSearchPwdListRunnable = this::searchPwdList;
    private byte mCurrentSearchNum;

    private void searchPwdList() {
        if(mWillSearchList.isEmpty()) {
            Timber.d("searchPwdList 要搜索的密码列表是空");
            return;
        }
        mCurrentSearchNum = mWillSearchList.get(0);
        App.getInstance().writeControlMsg(BleCommandFactory
                .keyAttributesRead(KEY_SET_KEY_TYPE_PWD,
                        mCurrentSearchNum,
                        App.getInstance().getBleBean().getPwd1(),
                        App.getInstance().getBleBean().getPwd3()));
        mWillSearchList.remove(0);
    }

    private void searchPwdListFromLocal() {
        if(mDeviceId == -1) {
            // TODO: 2021/2/21 错误的数据如何处理
            return;
        }
        List<DevicePwd> devicePwds = AppDatabase.getInstance(this).devicePwdDao().findDevicePwdListFromDeviceId(mDeviceId);
        if(devicePwds == null) {
            return;
        }
        if(devicePwds.isEmpty()) {
            return;
        }
        // TODO: 2021/2/21 取得所有密码数据并进行显示
        runOnUiThread(() -> mPasswordListAdapter.setList(devicePwds));
    }

}
