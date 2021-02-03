package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.PasswordListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.test.TestPwdBean;
import com.revolo.lock.ble.BleCommandFactory;
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
        testInitPwd();
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
                App.getInstance().writeControlMsg(BleCommandFactory
                        .synchronizeLockKeyStatusCommand((byte) 0x01,
                                App.getInstance().getBleBean().getPwd1(),
                                App.getInstance().getBleBean().getPwd3()));
                Timber.d("发送了请求密钥列表指令");
            }
        }, 100);

    }

    private void getPwdListFormBle(BleResultBean bean) {
        // TODO: 2021/2/3 可能存在100条数据以上，后续需要做100条数据以上的测试
        if(bean.getCMD() == 0x11) {
            byte[] value = bean.getPayload();
            int index = value[0] & 0xff;
            int codeType = value[1] & 0xff;
            int codeNumber = value[2] & 0xff;
            Timber.d("秘钥的帧数是  %1d, 秘钥类型是  %2d  秘钥总数是   %3d", index, codeType, codeNumber);
            // TODO: 2021/2/3 密钥列表的解析，有疑问，后续需要增加解析并显示
        }
    }

//    private int[] temp = new int[]{0b10000000, 0b01000000, 0b00100000, 0b00010000, 0b00001000, 0b00000100, 0b00000010, 0b00000001};
//
//    private void getAllPasswordNumber(int codeNumber, byte[] deValue) {
//        int passwordNumber = 10;
//        if (BleLockUtils.isSupport20Passwords(bleLockInfo.getServerLockInfo().getFunctionSet())) {  //支持20个密码的锁
//            passwordNumber = 20;  //永久密码的最大编号   小凯锁都是5个  0-5
//        }
//        for (int index = 0; index * 8 < passwordNumber; index++) {
//            if (index > 13) {
//                return;
//            }
//            for (int j = 0; j < 8 && index * 8 + j < passwordNumber; j++) {
//                if (((deValue[3 + index] & temp[j])) == temp[j] && index * 8 + j < passwordNumber) {
//                    bleNumber.add(index * 8 + j);
//                }
//                if (index * 8 + j >= passwordNumber) {
//                    return;
//                }
//            }
//        }
//    }

}
