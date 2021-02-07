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
import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.HomeLockListAdapter;
import com.revolo.lock.bean.showBean.WifiShowBean;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.bean.test.TestLockBean;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockGetAllBindDeviceRspBean;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.ui.TitleBar;
import com.revolo.lock.ui.device.add.AddDeviceActivity;
import com.revolo.lock.ui.device.lock.DeviceDetailActivity;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class DeviceFragment extends Fragment {

    private DeviceViewModel mDeviceViewModel;
    private HomeLockListAdapter mHomeLockListAdapter;
    private ConstraintLayout mClNoDevice, mClHadDevice;
    

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mDeviceViewModel =
                new ViewModelProvider(this).get(DeviceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_device, container, false);
        mClNoDevice = root.findViewById(R.id.clNoDevice);
        mClHadDevice = root.findViewById(R.id.clHadDevice);
//        mDeviceViewModel.getTestLockBeans().observe(getViewLifecycleOwner(), testLockBeans -> {
//            updateData(clNoDevice, clHadDevice, testLockBeans);
//        });
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
                if(adapter.getItem(position) instanceof WifiShowBean) {
                    if(position < 0 || position >= adapter.getData().size()) return;
                    WifiShowBean wifiShowBean = (WifiShowBean) adapter.getItem(position);
                    Intent intent = new Intent(getContext(), DeviceDetailActivity.class);
                    intent.putExtra(Constant.LOCK_DETAIL, wifiShowBean);
                    startActivity(intent);
                }
            });
            mHomeLockListAdapter.addChildClickViewIds(R.id.ivLockState);
            mHomeLockListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
                @Override
                public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                    if(view.getId() == R.id.ivLockState) {
                        // TODO: 2021/2/6 要选择来切换发送对应的指令
                        // 发送查询状态
                        App.getInstance().writeControlMsg(BleCommandFactory.checkLockBaseInfoCommand(mPwd1, mPwd3));
                        // 蓝牙发送开关门指令
//                           App.getInstance().writeControlMsg(BleCommandFactory
//                                   .lockControlCommand((byte) 0x00, (byte) 0x04, (byte) 0x01, mPwd1, mPwd3));
//                        publishOpenOrCloseDoor(mHomeLockListAdapter.getItem(position).getWifiListBean().getWifiSN(), 1);
                    }
                }
            });
            rvLockList.setAdapter(mHomeLockListAdapter);
            if(getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).setStatusBarColor(R.color.white);
            }
        }
        initBleListener();
        initData();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                initGetAllBindDevicesFromMQTT();
            }
        }, 500);

        return root;
    }

    private void updateData(List<WifiShowBean> testLockBeans) {
        if(testLockBeans != null) {
            if(testLockBeans.isEmpty()) {
                mClNoDevice.setVisibility(View.VISIBLE);
                mClHadDevice.setVisibility(View.GONE);
            } else {
                mClNoDevice.setVisibility(View.GONE);
                mClHadDevice.setVisibility(View.VISIBLE);
            }
            mHomeLockListAdapter.setList(testLockBeans);
        }
    }

    private BleBean mBleBean;
    private byte[] mPwd1;
    private byte[] mPwd2;

    private byte[] mPwd3;
    private String mEsn;
    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        if(bean.getCMD() == BleProtocolState.CMD_ENCRYPT_KEY_UPLOAD) {
            auth(bean);
        } else if(bean.getCMD() == BleProtocolState.CMD_LOCK_INFO) {
            lockInfo(bean);
        } else if(bean.getCMD() == BleProtocolState.CMD_LOCK_CONTROL_ACK) {
            controlOpenOrCloseDoorAck(bean);
        } else if(bean.getCMD() == BleProtocolState.CMD_LOCK_UPLOAD) {
            lockUpdateInfo(bean);
        }
    }

    private void auth(BleResultBean bean) {
        byte[] data = bean.getPayload();
        if(data[0] == 0x02) {
            // 获取pwd3
            mPwd3 = new byte[4];
            System.arraycopy(data, 1, mPwd3, 0, mPwd3.length);
            Timber.d("鉴权成功, pwd3: %1s\n", ConvertUtils.bytes2HexString(mPwd3));
            // 内存存储
            App.getInstance().getBleBean().setPwd3(mPwd3);
            App.getInstance().writeControlMsg(BleCommandFactory.ackCommand(bean.getTSN(), (byte)0x00, bean.getCMD()));
            // 鉴权成功后，同步当前时间
            syNowTime();
            // TODO: 2021/1/26 鉴权成功
        }
    }

    private void lockInfo(BleResultBean bean) {

    }

    private void controlOpenOrCloseDoorAck(BleResultBean bean) {
        // TODO: 2021/2/7 处理控制开关锁确认帧
    }

    private void lockUpdateInfo(BleResultBean bean) {
        // TODO: 2021/2/7 锁操作上报
    }

    private void syNowTime() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            long nowTime = TimeUtils.getNowMills()/1000;
            App.getInstance().writeControlMsg(BleCommandFactory
                    .syLockTime(nowTime, mPwd1, mPwd3));
        }, 20);
    }

    private void initBleListener() {
        App.getInstance().setOnBleDeviceListener(new OnBleDeviceListener() {
            @Override
            public void onConnected() {
                Timber.d("initBleListener 连接成功 发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2));
                mBleBean.setPwd1(mPwd1);
                mBleBean.setPwd2(mPwd2);
                App.getInstance().writeControlMsg(BleCommandFactory
                        .authCommand(mPwd1, mPwd2, mEsn.getBytes(StandardCharsets.UTF_8)));
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
                BleResultProcess.processReceivedData(value, mPwd1, (mPwd3 == null)?mPwd2:mPwd3,
                        mBleBean.getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(String uuid, byte[] value, boolean success) {

            }
        });
    }

    private void initData() {
        mEsn = App.getInstance().getCacheDiskUtils().getString(Constant.LOCK_ESN);
        mPwd1 = App.getInstance().getCacheDiskUtils().getBytes(Constant.KEY_PWD1);
        mPwd2 = App.getInstance().getCacheDiskUtils().getBytes(Constant.KEY_PWD2);
        if(App.getInstance().getBleBean() == null) {
            BLEScanResult bleScanResult = App.getInstance().getCacheDiskUtils()
                    .getParcelable(Constant.BLE_DEVICE, BLEScanResult.CREATOR, null);
            if(bleScanResult != null) {
                App.getInstance().connectDevice(bleScanResult);
                mBleBean = App.getInstance().getBleBean();
                mBleBean.setPwd1(mPwd1);
                mBleBean.setPwd2(mPwd2);
            } else {
                // TODO: 2021/1/26 处理为空的情况
            }
        } else {
            mBleBean = App.getInstance().getBleBean();
            if(mBleBean.getOKBLEDeviceImp() != null) {
                if(!mBleBean.getOKBLEDeviceImp().isConnected()) {
                    mBleBean.getOKBLEDeviceImp().connect(true);
                }
                mBleBean.setPwd1(mPwd1);
                mBleBean.setPwd2(mPwd2);
            } else {
                // TODO: 2021/1/26 为空的处理
            }
        }
    }

    // TODO: 2021/2/6 后面想办法写的更好
    private byte[] getPwd(byte[] pwd1, byte[] pwd2) {
        byte[] pwd = new byte[16];
        for (int i=0; i < pwd.length; i++) {
            if(i <= 11) {
                pwd[i]=pwd1[i];
            } else {
                pwd[i]=pwd2[i-12];
            }
        }
        return pwd;
    }

    /**
     *  开关门
     * @param wifiId wifi的id
     * @param doorOpt 1:表示开门，0表示关门
     */
    public void publishOpenOrCloseDoor(String wifiId, int doorOpt) {
        // TODO: 2021/2/6 发送开门或者关门的指令
        App.getInstance().getMqttService().mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLock(wifiId,doorOpt, getPwd(mPwd1, mPwd2))).subscribe(new Observer<MqttData>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull MqttData mqttData) {
                if(mqttData.getFunc().equals("setLock")) {
                    Timber.d("开关门信息: %1s", mqttData);
                }
                Timber.d("%1s", mqttData.toString());
            }

            @Override
            public void onError(@NotNull Throwable e) {
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void initGetAllBindDevicesFromMQTT() {

        Timber.d("执行获取设备信息");
        App.getInstance().getMqttService()
                .mqttPublish(MqttConstant.PUBLISH_TO_SERVER,
                        MqttCommandFactory.getAllBindDevices(App.getInstance().getUserBean().getUid()))
                .safeSubscribe(new Observer<MqttData>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull MqttData mqttData) {
                        Gson gson = new Gson();
                        WifiLockGetAllBindDeviceRspBean bean = null;
                        try {
                            bean = gson.fromJson(mqttData.getPayload(), WifiLockGetAllBindDeviceRspBean.class);
                        } catch (JsonSyntaxException e) {
                            // TODO: 2021/2/6 解析失败的处理 
                            Timber.e(e);
                        }
                        if(bean == null) {
                            Timber.e("WifiLockGetAllBindDeviceRspBean is null");
                            return;
                        }
                        if(bean.getData() == null) {
                            Timber.e("WifiLockGetAllBindDeviceRspBean.Data is null");
                            return;
                        }
                        if(bean.getData().getWifiList() == null) {
                            Timber.e("WifiLockGetAllBindDeviceRspBean..getData().getWifiList() is null");
                            return;
                        }
                        if(bean.getData().getWifiList().isEmpty()) {
                            Timber.e("WifiLockGetAllBindDeviceRspBean..getData().getWifiList().isEmpty()");
                            return;
                        }
                        List<WifiShowBean> showBeans = new ArrayList<>();
                        for (WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean : bean.getData().getWifiList()) {
                            showBeans.add(new WifiShowBean(2, 1,1, wifiListBean));
                        }
                        updateData(showBeans);
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}