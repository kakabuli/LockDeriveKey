package com.revolo.lock.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.HomeLockListAdapter;
import com.revolo.lock.bean.BleBean;
import com.revolo.lock.bean.test.TestLockBean;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.ui.TitleBar;
import com.revolo.lock.ui.device.add.AddDeviceActivity;
import com.revolo.lock.ui.device.lock.DeviceDetailActivity;

import java.nio.charset.StandardCharsets;

import timber.log.Timber;

public class DeviceFragment extends Fragment {

    private DeviceViewModel mDeviceViewModel;
    private HomeLockListAdapter mHomeLockListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mDeviceViewModel =
                new ViewModelProvider(this).get(DeviceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_device, container, false);
        ConstraintLayout clNoDevice = root.findViewById(R.id.clNoDevice);
        ConstraintLayout clHadDevice = root.findViewById(R.id.clHadDevice);
        mDeviceViewModel.getTestLockBeans().observe(getViewLifecycleOwner(), testLockBeans -> {
            if(testLockBeans != null) {
                if(testLockBeans.isEmpty()) {
                    clNoDevice.setVisibility(View.VISIBLE);
                    clHadDevice.setVisibility(View.GONE);
                } else {
                    clNoDevice.setVisibility(View.GONE);
                    clHadDevice.setVisibility(View.VISIBLE);
                }
                mHomeLockListAdapter.setList(testLockBeans);
            }
        });
        // 无设备的时候控件UI
        ImageView ivAdd = root.findViewById(R.id.ivAdd);
        ivAdd.setOnClickListener(v -> startActivity(new Intent(getContext(), AddDeviceActivity.class)));

        // 有设备的时候控件UI
        if(getContext() != null) {
            new TitleBar(root).setTitle(getString(R.string.title_my_devices))
                    .setRight(ContextCompat.getDrawable(getContext(), R.drawable.ic_home_icon_add),
                            v -> startActivity(new Intent(getContext(), AddDeviceActivity.class)));
            RecyclerView rvLockList = root.findViewById(R.id.rvLockList);
            rvLockList.setLayoutManager(new LinearLayoutManager(getContext()));
            mHomeLockListAdapter = new HomeLockListAdapter(R.layout.item_home_lock_list_rv);
            mHomeLockListAdapter.setOnItemClickListener((adapter, view, position) -> {
                if(adapter.getItem(position) instanceof TestLockBean) {
                    if(position < 0 || position >= adapter.getData().size()) return;
                    TestLockBean testLockBean = (TestLockBean) adapter.getItem(position);
                    Intent intent = new Intent(getContext(), DeviceDetailActivity.class);
                    intent.putExtra(Constant.LOCK_DETAIL, testLockBean);
                    startActivity(intent);
                }
            });
            rvLockList.setAdapter(mHomeLockListAdapter);
            if(getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).setStatusBarColor(R.color.white);
            }
        }
        initBleListener();
        initData();

        return root;
    }

    private BleBean mBleBean;
    private byte[]  mPwd1;

    private boolean isHavePwd2Or3 = false;
    private final byte[] mPwd2Or3 = new byte[4];
    private String mMac;
    private String mEsn;
    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        auth(bleResultBean);
    };

    private void auth(BleResultBean bleResultBean) {
        if(bleResultBean.getCMD() == BleProtocolState.CMD_PAIR_ACK) {
            if(bleResultBean.getPayload()[0] != 0x00) {
                // 校验失败
                Timber.e("校验失败 CMD: %1s, 回复的数据：%2s",
                        ConvertUtils.int2HexString(bleResultBean.getCMD()), ConvertUtils.bytes2HexString(bleResultBean.getPayload()));
                // TODO: 2021/1/26 校验失败
            }
            return;
        }
        if(bleResultBean.getCMD() == BleProtocolState.CMD_ENCRYPT_KEY_UPLOAD) {
            byte[] data = bleResultBean.getPayload();
            if(data[0] == 0x01) {
                // 入网时
                // 获取pwd2
                System.arraycopy(data, 1, mPwd2Or3, 0, mPwd2Or3.length);
                // TODO: 2021/1/21 打包数据上传到服务器后再发送确认指令
                isHavePwd2Or3 = true;
                App.getInstance().writeControlMsg(BleCommandFactory.ackCommand(bleResultBean.getTSN(), (byte)0x00, bleResultBean.getCMD()));
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Timber.d("auth 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2Or3));
                    App.getInstance().writeControlMsg(BleCommandFactory
                            .authCommand(mPwd1, mPwd2Or3, mEsn.getBytes(StandardCharsets.UTF_8)));
                }, 50);
            } else if(data[0] == 0x02) {
                // 获取pwd3
                System.arraycopy(data, 1, mPwd2Or3, 0, mPwd2Or3.length);
                Timber.d("鉴权成功, pwd3: %1s\n", ConvertUtils.bytes2HexString(mPwd2Or3));
                // 本地存储
                App.getInstance().getCacheDiskUtils().put(Constant.KEY_PWD1, mPwd1);
                App.getInstance().getCacheDiskUtils().put(Constant.BLE_MAC, mMac);
                App.getInstance().getCacheDiskUtils().put(Constant.LOCK_ESN, mEsn);
                // 内存存储
                App.getInstance().getBleBean().setPwd1(mPwd1);
                App.getInstance().getBleBean().setPwd2or3(mPwd2Or3);
                App.getInstance().writeControlMsg(BleCommandFactory.ackCommand(bleResultBean.getTSN(), (byte)0x00, bleResultBean.getCMD()));
                // TODO: 2021/1/26 校验成功
            }
        }
    }

    private void initBleListener() {
        App.getInstance().setOnBleDeviceListener(new OnBleDeviceListener() {
            @Override
            public void onConnected() {
                App.getInstance().writeControlMsg(BleCommandFactory
                        .pairCommand(mPwd1, mEsn.getBytes(StandardCharsets.UTF_8)));
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
                BleResultProcess.processReceivedData(value, mPwd1, isHavePwd2Or3?mPwd2Or3:null,
                        mBleBean.getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(String uuid, byte[] value, boolean success) {

            }
        });
    }

    private void initData() {
        if(App.getInstance().getBleBean() == null) {
            mEsn = App.getInstance().getCacheDiskUtils().getString(Constant.LOCK_ESN);
            mMac = App.getInstance().getCacheDiskUtils().getString(Constant.BLE_MAC);
            mPwd1 = App.getInstance().getCacheDiskUtils().getBytes(Constant.KEY_PWD1);
            BLEScanResult bleScanResult = App.getInstance().getCacheDiskUtils().getParcelable(Constant.BLE_DEVICE, BLEScanResult.CREATOR, null);
            if(bleScanResult != null) {
                App.getInstance().connectDevice(bleScanResult);
                mBleBean = App.getInstance().getBleBean();
            } else {
                // TODO: 2021/1/26 处理为空的情况
            }
        } else {
            mBleBean = App.getInstance().getBleBean();
            if(mBleBean.getOKBLEDeviceImp() != null) {
                if(!mBleBean.getOKBLEDeviceImp().isConnected()) {
                    mBleBean.getOKBLEDeviceImp().connect(true);
                }
            } else {
                // TODO: 2021/1/26 为空的处理
            }
        }
    }

}