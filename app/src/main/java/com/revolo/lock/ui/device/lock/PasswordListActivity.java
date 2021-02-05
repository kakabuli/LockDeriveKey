package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
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

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_password_list;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.password))
                .setRight(ContextCompat.getDrawable(this, R.drawable.ic_home_icon_add),
                        v -> startActivity(new Intent(this, AddInputNewPwdActivity.class)));
        RecyclerView rvPwdList = findViewById(R.id.rvPwdList);
        rvPwdList.setLayoutManager(new LinearLayoutManager(this));
        mPasswordListAdapter = new PasswordListAdapter(R.layout.item_pwd_list_rv);
        mPasswordListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if(position >= 0 && adapter.getItem(position) instanceof TestPwdBean) {
                    Intent intent = new Intent(PasswordListActivity.this, PasswordDetailActivity.class);
                    TestPwdBean testPwdBean  = (TestPwdBean) adapter.getItem(position);
                    intent.putExtra(Constant.PWD_DETAIL, testPwdBean);
                    startActivity(intent);
                }
            }
        });
        rvPwdList.setAdapter(mPasswordListAdapter);
    }

    @Override
    public void doBusiness() {
//        testInitPwd();
        initBleListener();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void testInitPwd() {
        List<TestPwdBean> list = new ArrayList<>();
        TestPwdBean testPwdBean1 = new TestPwdBean("Password name",
                "Permanent password", 1, "***********",
                "Permanence", "12,28,2020 12:00");
        list.add(testPwdBean1);
        TestPwdBean testPwdBean2 = new TestPwdBean("Password name", "Sun、Mon、Tues、Wed、Thure、Tir\n" +
                "15:00-17:00", 1, "***********",
                "Sun、Mon、Tues、Wed、Thur、Fir \n" +
                        "14:00-17:00 ", "12,28,2020 12:00");
        list.add(testPwdBean2);
        TestPwdBean testPwdBean3 = new TestPwdBean("Password name", "start: 12,28,2020   12:00 \n" +
                "end:  12,28,2020   16:00", 1, "***********",
                "12,28,2020 12:00 - 12,29,2020  10:30", "12,28,2020 12:00");
        list.add(testPwdBean3);
        TestPwdBean testPwdBean4 = new TestPwdBean("Password name", "start: 12,28,2020   12:00 \n" +
                "end:  12,28,2020   16:00", 2, "***********",
                "Permanence", "12,28,2020 12:00");
        list.add(testPwdBean4);
        mPasswordListAdapter.setList(list);

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
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mWillSearchList.clear();
                mPasswordListAdapter.setList(mTestPwdBeans);
                App.getInstance().writeControlMsg(BleCommandFactory
                        .synchronizeLockKeyStatusCommand((byte) 0x01,
                                App.getInstance().getBleBean().getPwd1(),
                                App.getInstance().getBleBean().getPwd3()));
                Timber.d("发送了请求密钥列表指令");
            }
        }, 100);

    }

    private final ArrayList<Byte> mWillSearchList = new ArrayList<>();
    private ArrayList<TestPwdBean> mTestPwdBeans = new ArrayList<>();

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
                        1, "***********", "Permanence", "02,04,2020 12:00");
                mTestPwdBeans.add(testPwdBean);
            } else if(attribute == KEY_SET_ATTRIBUTE_TIME_KEY) {
                // TODO: 2021/2/5 时间策略密码
                byte[] weekBytes = BleByteUtil.byteToBit(bean.getPayload()[1]);
                Timber.d("getPwdListFormBle num: %1s week: %1s", mCurrentSearchNum, ConvertUtils.bytes2HexString(weekBytes));
//                String weekly = "";
//                TestPwdBean testPwdBean = new TestPwdBean(name, "Sun、Mon、Tues、Wed、Thur、Tir\n" +
//                        "15:00-17:00", 1, "***********",
//                        "Sun、Mon、Tues、Wed、Thur、Fir \n" +
//                                "14:00-17:00 ", "12,28,2020 12:00");
//                mTestPwdBeans.add(testPwdBean);
            } else if(attribute == KEY_SET_ATTRIBUTE_WEEK_KEY) {
                // TODO: 2021/2/5 周策略密码
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPasswordListAdapter.setList(mTestPwdBeans);
                }
            });
            mHandler.postDelayed(mSearchPwdListRunnable, 20);
        }
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

}
