package com.revolo.lock.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.adapter.HomeLockListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.SignalWeakDialog;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.eventbean.WifiLockOperationEventBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockBaseResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockDoorOptResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockGetAllBindDeviceRspBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.ui.TitleBar;
import com.revolo.lock.ui.device.add.AddDeviceActivity;
import com.revolo.lock.ui.device.lock.DeviceDetailActivity;
import com.revolo.lock.ui.view.SmartClassicsHeaderView;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.ble.BleCommandState.LOCK_SETTING_CLOSE;
import static com.revolo.lock.ble.BleCommandState.LOCK_SETTING_OPEN;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_MQTT;

public class DeviceFragment extends Fragment {
    private View root;
    private DeviceViewModel mDeviceViewModel;
    private HomeLockListAdapter mHomeLockListAdapter;
    private ConstraintLayout mClNoDevice, mClHadDevice;
    private CustomerLoadingDialog mLoadingDialog;
    private RefreshLayout mRefreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        mDeviceViewModel =
//                new ViewModelProvider(this).get(DeviceViewModel.class);
        if (null == root) {
            root = inflater.inflate(R.layout.fragment_device, container, false);
            mClNoDevice = root.findViewById(R.id.clNoDevice);
            mClHadDevice = root.findViewById(R.id.clHadDevice);
//        mDeviceViewModel.getWifiListBeans().observe(getViewLifecycleOwner(), this::updateDataFromNet);
            // 无设备的时候控件UI
            ImageView ivAdd = root.findViewById(R.id.ivAdd);
            ivAdd.setOnClickListener(v -> startActivity(new Intent(getContext(), AddDeviceActivity.class)));

            // 有设备的时候控件UI
            if (getContext() != null) {
                initTitleBar();
                RecyclerView rvLockList = root.findViewById(R.id.rvLockList);
                rvLockList.setLayoutManager(new LinearLayoutManager(getContext()));
                mHomeLockListAdapter = new HomeLockListAdapter(R.layout.item_home_lock_list_rv);
                mHomeLockListAdapter.setOnItemClickListener((adapter, view, position) -> {
                    if (adapter.getItem(position) instanceof BleDeviceLocal) {
                        if (position < 0 || position >= adapter.getData().size()) return;
                        BleDeviceLocal deviceLocal = (BleDeviceLocal) adapter.getItem(position);
                        Intent intent = new Intent(getContext(), DeviceDetailActivity.class);
                        App.getInstance().setBleDeviceLocal(deviceLocal);
                        startActivity(intent);
                    }
                });
                mHomeLockListAdapter.addChildClickViewIds(R.id.ivLockState);
                mHomeLockListAdapter.setOnItemChildClickListener((adapter, view, position) -> {
                    if (view.getId() == R.id.ivLockState) {
                        //判断隐私模式
                        @LocalState.LockState int mLockstate = mBleDeviceLocals.get(position).getLockState();
                        if (mLockstate == LocalState.LOCK_STATE_PRIVATE) {
                            return;
                        }
                        //判断设备是否掉线
                        @LocalState.LockState int connectedState = mHomeLockListAdapter.getItem(position).getConnectedType();
                        if (LocalState.DEVICE_CONNECT_TYPE_DIS == connectedState) {
                            return;
                        }
                        @LocalState.LockState int state = mHomeLockListAdapter.getItem(position).getLockState();
                        openOrCloseDoor(mHomeLockListAdapter.getItem(position).getEsn(),
                                state == LocalState.LOCK_STATE_OPEN ? LocalState.DOOR_STATE_CLOSE : LocalState.DOOR_STATE_OPEN,
                                mBleDeviceLocals.get(position), 0, position, state);
                    }
                });
                rvLockList.setAdapter(mHomeLockListAdapter);
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setStatusBarColor(R.color.white);
                }
            }

            mRefreshLayout = root.findViewById(R.id.refreshLayout);
            mRefreshLayout.setEnableLoadMore(false);
            mRefreshLayout.setRefreshHeader(new SmartClassicsHeaderView(getContext()));
            mRefreshLayout.setOnRefreshListener(refreshLayout -> {
//            mDeviceViewModel.refreshGetAllBindDevicesFromMQTT();
                refreshGetAllBindDevicesFromMQTT();
            });
            onRegisterEventBus();
        }
        return root;
    }
    public void onRegisterEventBus() {
        boolean registered = EventBus.getDefault().isRegistered(this);
        if (!registered) {
            EventBus.getDefault().register(this);
        }
    }
    private void initTitleBar() {
        new TitleBar(root).setTitle(getString(R.string.title_my_devices))
                .setRight(R.drawable.ic_home_icon_add,
                        v -> startActivity(new Intent(getContext(), AddDeviceActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshGetAllBindDevicesFromMQTT();
        initBaseData();
        initData(mBleDeviceLocals);
        initSignalWeakDialog();
        initWfEven();
//        mDeviceViewModel.refreshGetAllBindDevicesFromMQTT();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_USER) {
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //数据正常
            } else {
                //数据异常
            }

        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //数据正常
                processBleResult(lockMessage.getMac(), lockMessage.getBleResultBea());
            } else {
                //数据异常
            }

        } else if (lockMessage.getMessgaeType() == MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //数据正常
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_ADD_DEVICE://添加到设备到主页
                        Timber.e("getEventBus2");
                        //获取当前用户绑定设备返回
                        mBleDeviceLocals = App.getInstance().getDeviceLists();
                        updateData(mBleDeviceLocals);
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK://开关锁
                        processSetLock((WifiLockDoorOptResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_WF_EVEN:
                        processRecord((WifiLockOperationEventBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                    default:
                        processBleResult(lockMessage.getMac(), lockMessage.getBleResultBea());
                        break;

                }
            } else {
                //数据异常
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK:
                        //开关锁异常
                        //                    dismissLoading();
//                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(doorOpt == LocalState.DOOR_STATE_OPEN ? "Locking Failed" : "Unlocking Failed");
//                    if (mCount == 3) {
//                        // 3次机会,超时失败开始连接蓝牙
//                        mCount = 0;
//                        mBleDeviceLocal = bleDeviceLocal;
//                        if (getActivity() == null) return;
//                        getActivity().runOnUiThread(() -> {
//                            if (mSignalWeakDialog != null) {
//                                mSignalWeakDialog.show();
//                            }
//                        });
//                    }
                        break;
                }
            }
        } else {

        }
    }

    public void pushMessage(LockMessage message) {
        EventBus.getDefault().post(message);
    }

    public void openOrCloseDoor(String wifiId,
                                @LocalState.DoorState int doorOpt,
                                BleDeviceLocal bleDeviceLocal, int num, int position, @LocalState.LockState int state) {
        LockMessage message = new LockMessage();
        //wifi
        if (App.getInstance().getUserBean() == null || bleDeviceLocal == null
                || getActivity() == null) {
            message.setMqttMessage(null);
        } else {
            message.setSn(bleDeviceLocal.getEsn());
            message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
            message.setMqtt_message_code(MQttConstant.SET_LOCK);
            message.setMqttMessage(
                    MqttCommandFactory.setLock(
                            wifiId,
                            doorOpt,
                            BleCommandFactory.getPwd(
                                    ConvertUtils.hexString2Bytes(bleDeviceLocal.getPwd1()),
                                    ConvertUtils.hexString2Bytes(bleDeviceLocal.getPwd2())),
                            bleDeviceLocal.getRandomCode(),
                            num));
        }
        //ble
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocals.get(position).getMac());
        if (bleBean == null || bleBean.getOKBLEDeviceImp() == null || bleBean.getPwd1() == null || bleBean.getPwd3() == null) {
            Timber.e("openOrCloseDoorFromBle bleBean.getPwd3() == null");
            message.setBytes(null);
        } else {
            message.setBytes(BleCommandFactory.lockControlCommand(
                    (byte) (state == LocalState.LOCK_STATE_OPEN ? LOCK_SETTING_CLOSE : LOCK_SETTING_OPEN),
                    (byte) 0x04,
                    (byte) 0x01,
                    bleBean.getPwd1(),
                    bleBean.getPwd3()));
            message.setMac(bleDeviceLocal.getMac());
        }
        if (null == message.getBytes() && null == message.getMqttMessage()) {
            return;
        }
        if (doorOpt == LocalState.DOOR_STATE_OPEN) {
            showLoading("Lock Opening...");
        } else if (doorOpt == LocalState.DOOR_STATE_CLOSE) {
            showLoading("Lock Closing...");
        }
        //mCount++;
        pushMessage(message);
    }

    /**
     * 获取当前用户绑定的设备
     */
    public void refreshGetAllBindDevicesFromMQTT() {
        if (App.getInstance().getUserBean() == null || getActivity() == null) {
            Timber.e("refreshGetAllBindDevicesFromMQTT getActivity() == null");
            return;
        }
        Timber.e("执行获取设备信息");
        LockMessage lockMessage = new LockMessage();
        lockMessage.setMessageType(2);
        lockMessage.setMqtt_topic(MQttConstant.PUBLISH_TO_SERVER);
        lockMessage.setMqtt_message_code(MQttConstant.GET_ALL_BIND_DEVICE);
        lockMessage.setMqttMessage(MqttCommandFactory.getAllBindDevices(App.getInstance().getUserBean().getUid()));
        lockMessage.setSn("");
        lockMessage.setMessageType(MSG_LOCK_MESSAGE_MQTT);
        lockMessage.setBytes(null);
        pushMessage(lockMessage);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBleDeviceLocals = App.getInstance().getDeviceLists();
        mHomeLockListAdapter.setList(mBleDeviceLocals);
        initBaseData();
        refreshGetAllBindDevicesFromMQTT();
        // mHomeLockListAdapter.setList(mBleDeviceLocals);
        // initSignalWeakDialog();
        // initData(mBleDeviceLocals);

        //       initWfEven();
//        mDeviceViewModel.refreshGetAllBindDevicesFromMQTT();
    }


    private void updateData(List<BleDeviceLocal> locals) {
        if (locals != null) {
            if (locals.isEmpty()) {
                mClNoDevice.setVisibility(View.VISIBLE);
                mClHadDevice.setVisibility(View.GONE);
            } else {
                mClNoDevice.setVisibility(View.GONE);
                mClHadDevice.setVisibility(View.VISIBLE);
            }
            mHomeLockListAdapter.setList(locals);
        }
    }

    private void processBleResult(@NotNull String mac, BleResultBean bean) {
        if (bean.getCMD() == BleProtocolState.CMD_LOCK_INFO) {
            lockInfo(mac, bean);
        } else if (bean.getCMD() == BleProtocolState.CMD_LOCK_UPLOAD) {
            lockUpdateInfo(mac, bean);
        }
//        else if(bean.getCMD() == BleProtocolState.CMD_LOCK_CONTROL_ACK) {
////            controlOpenOrCloseDoorAck(mac, bean);
//        }
    }

    private void lockInfo(@NotNull String mac, BleResultBean bean) {
        // TODO: 2021/2/8 锁基本信息处理
        byte[] lockFunBytes = new byte[4];
        System.arraycopy(bean.getPayload(), 0, lockFunBytes, 0, lockFunBytes.length);
        // 以下标来命名区分 bit0~7
        byte[] bit0_7 = BleByteUtil.byteToBit(lockFunBytes[3]);
        // bit8~15
        byte[] bit8_15 = BleByteUtil.byteToBit(lockFunBytes[2]);
        // bit16~23
        byte[] bit16_23 = BleByteUtil.byteToBit(lockFunBytes[1]);

        byte[] lockState = new byte[4];
        System.arraycopy(bean.getPayload(), 4, lockState, 0, lockState.length);
        byte[] lockStateBit0_7 = BleByteUtil.byteToBit(lockState[3]);
        byte[] lockStateBit8_15 = BleByteUtil.byteToBit(lockState[2]);
        int soundVolume = bean.getPayload()[8];
        byte[] language = new byte[2];
        System.arraycopy(bean.getPayload(), 9, language, 0, language.length);
        String languageStr = new String(language, StandardCharsets.UTF_8);
        int battery = bean.getPayload()[11];
        byte[] time = new byte[4];
        System.arraycopy(bean.getPayload(), 12, time, 0, time.length);
        long realTime = (BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time)) + Constant.WILL_ADD_TIME) * 1000;
        Timber.d("CMD: %1d, lockFunBytes: bit0_7: %2s, bit8_15: %3s, bit16_23: %4s, lockStateBit0_7: %5s, lockStateBit8_15: %6s, soundVolume: %7d, language: %8s, battery: %9d, time: %10d",
                bean.getCMD(), ConvertUtils.bytes2HexString(bit0_7), ConvertUtils.bytes2HexString(bit8_15),
                ConvertUtils.bytes2HexString(bit16_23), ConvertUtils.bytes2HexString(lockStateBit0_7),
                ConvertUtils.bytes2HexString(lockStateBit8_15), soundVolume, languageStr, battery, realTime);

    }

    private int getPositionFromMac(@NotNull String mac) {
        if (mBleDeviceLocals.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < mBleDeviceLocals.size(); i++) {
            if (mac.equals(mBleDeviceLocals.get(i).getMac())) {
                return i;
            }
        }
        return -1;
    }

    private int getPositionFromWifiId(@NotNull String wifiID) {
        if (mBleDeviceLocals.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < mBleDeviceLocals.size(); i++) {
            if (wifiID.equals(mBleDeviceLocals.get(i).getEsn())) {
                return i;
            }
        }
        return -1;
    }

    private void lockUpdateInfo(@NotNull String mac, BleResultBean bean) {
        // TODO: 2021/2/7 锁操作上报
        int eventType = bean.getPayload()[0];
        int eventSource = bean.getPayload()[1];
        int eventCode = bean.getPayload()[2];
        int userID = bean.getPayload()[3];
        byte[] time = new byte[4];
        System.arraycopy(bean.getPayload(), 4, time, 0, time.length);
        // TODO: 2021/2/8 要做时间都是ffffffff的处理判断
        long realTime = (BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time))) * 1000;
        Timber.d("lockUpdateInfo CMD: %1d, eventType: %2d, eventSource: %3d, eventCode: %4d, userID: %5d, time: %6d",
                bean.getCMD(), eventType, eventSource, eventCode, userID, realTime);

        // TODO: 2021/2/10 后期需要移植修改
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {

            if (eventType == 0x01) {
                if (eventCode == 0x01) {
                    // 上锁
                    setLockState(getPositionFromMac(mac), LocalState.LOCK_STATE_CLOSE);
                } else if (eventCode == 0x02) {
                    // 开锁
                    setLockState(getPositionFromMac(mac), LocalState.LOCK_STATE_OPEN);
                }
//                else {
//                    // TODO: 2021/2/10 其他处理
//                }
            } else if (eventType == 0x04) {
                // sensor附加状态，门磁
                if (eventCode == LocalState.DOOR_SENSOR_OPEN) {
                    // 开门
                    setDoorState(getPositionFromMac(mac), LocalState.DOOR_SENSOR_OPEN);
                } else if (eventCode == LocalState.DOOR_SENSOR_CLOSE) {
                    // 关门
                    setDoorState(getPositionFromMac(mac), LocalState.DOOR_SENSOR_CLOSE);
                } else if (eventCode == LocalState.DOOR_SENSOR_EXCEPTION) {
                    // 门磁异常
                    // TODO: 2021/3/31 门磁异常的操作
                    Timber.d("lockUpdateInfo 门磁异常");
                }
//                else {
//                    // TODO: 2021/3/31 异常值
//                }
            }
        });
    }

    private void setLockState(int index, @LocalState.LockState int state) {
        if (getActivity() == null) {
            Timber.e("setLockState getActivity() == null");
            return;
        }
        if (index == -1) {
            Timber.e("setLockState index == -1");
            return;
        }
        getActivity().runOnUiThread(() -> {
            BleDeviceLocal local = mHomeLockListAdapter.getData().get(index);
            local.setLockState(state);
            Timber.d("setLockState wifiId: %1s %2s", local.getEsn(), state == LocalState.LOCK_STATE_OPEN ? "锁开了" : "锁关了");
            AppDatabase.getInstance(getContext()).bleDeviceDao().update(local);
            mHomeLockListAdapter.notifyDataSetChanged();
            dismissLoading();
        });
    }

    private void setDoorState(int index, @LocalState.DoorSensor int state) {
        if (getActivity() == null) {
            Timber.e("setDoorState getActivity() == null");
            return;
        }
        if (index == -1) {
            Timber.e("setDoorState index == -1");
            return;
        }
        getActivity().runOnUiThread(() -> {
            BleDeviceLocal local = mHomeLockListAdapter.getData().get(index);
            Timber.d("setDoorState wifiId: %1s %2s", local.getEsn(), state == LocalState.DOOR_SENSOR_OPEN ? "开门了" : "关门了");
            local.setDoorSensor(state);
            AppDatabase.getInstance(getContext()).bleDeviceDao().update(local);
            mHomeLockListAdapter.notifyDataSetChanged();
            dismissLoading();
        });
    }

    private List<BleDeviceLocal> mBleDeviceLocals;

    private void initBaseData() {
        User user = App.getInstance().getUser();
        mBleDeviceLocals = App.getInstance().getDeviceLists();
        if (user == null) {
            return;
        }
        List<BleDeviceLocal> locals = AppDatabase.getInstance(App.getInstance()).bleDeviceDao().findBleDevicesFromUserIdByCreateTimeDesc(user.getId());
        if (locals == null) {
            return;
        }
        if (locals.isEmpty()) {
            return;
        }
        App.getInstance().setDeviceLists(locals);
        /// updateData(mBleDeviceLocals);

    }

    /*private void initData(List<BleDeviceLocal> bleDeviceLocals) {
        if (bleDeviceLocals == null) {
            Timber.e("initData bleDeviceLocals == null");
            return;
        }
        if (bleDeviceLocals.isEmpty()) {
            Timber.e("initData bleDeviceLocals is Empty");
            return;
        }
        // TODO: 2021/3/7 暂时所有设备都连接上，后续得考虑如何优化，因为暂时只有很少设备测试，大规模设备需要另一套机制处理
        for (BleDeviceLocal local : bleDeviceLocals) {
            if (local.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                BleBean bleBean = App.getInstance().getUserBleBean(local.getMac());
                OnBleDeviceListener onBleDeviceListener = new OnBleDeviceListener() {
                    @Override
                    public void onConnected(@NotNull String mac) {

                    }

                    @Override
                    public void onDisconnected(@NotNull String mac) {

                    }

                    @Override
                    public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
                        if (value == null) {
                            return;
                        }
                        if (!local.getMac().equals(mac)) {
                            return;
                        }
                        BleBean bleBean = App.getInstance().getBleBeanFromMac(local.getMac());
                        if (bleBean == null) {
                            return;
                        }
                        if (bleBean.getOKBLEDeviceImp() == null) {
                            return;
                        }
                        if (bleBean.getPwd1() == null) {
                            return;
                        }
                        if (bleBean.getPwd2() == null) {
                            return;
                        }
                        BleResultProcess.setOnReceivedProcess(bleResultBean -> {
                            if (bleResultBean == null) {
                                Timber.e("%1s mOnReceivedProcess bleResultBean == null", local.getMac());
                                return;
                            }
                            processBleResult(local.getMac(), bleResultBean);
                        });
                        BleResultProcess.processReceivedData(
                                value,
                                bleBean.getPwd1(),
                                (bleBean.getPwd3() == null) ? bleBean.getPwd2() : bleBean.getPwd3(),
                                bleBean.getOKBLEDeviceImp().getBleScanResult());
                    }

                    @Override
                    public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

                    }

                    @Override
                    public void onAuthSuc(@NotNull String mac) {

                    }

                };
                if (bleBean == null) {
                    BLEScanResult bleScanResult = ConvertUtils.bytes2Parcelable(local.getScanResultJson(), BLEScanResult.CREATOR);
                    if (bleScanResult != null) {
                        bleBean = App.getInstance().connectDevice(
                                bleScanResult,
                                ConvertUtils.hexString2Bytes(local.getPwd1()),
                                ConvertUtils.hexString2Bytes(local.getPwd2()),
                                onBleDeviceListener, false);
                        bleBean.setEsn(local.getEsn());
                    } else {
                        // TODO: 2021/1/26 处理为空的情况
                    }
                } else {
                    if (bleBean.getOKBLEDeviceImp() != null) {
                        bleBean.setOnBleDeviceListener(onBleDeviceListener);
                        if (!bleBean.getOKBLEDeviceImp().isConnected()) {
                            bleBean.getOKBLEDeviceImp().connect(true);
                        }
                        bleBean.setPwd1(ConvertUtils.hexString2Bytes(local.getPwd1()));
                        bleBean.setPwd2(ConvertUtils.hexString2Bytes(local.getPwd2()));
                        bleBean.setEsn(local.getEsn());
                    } else {
                        // TODO: 2021/1/26 为空的处理
                    }
                }
            } else {
                // TODO: 2021/4/6 wifi 下是否蓝牙连接
                BleBean bleBean = App.getInstance().getBleBeanFromMac(local.getMac());
                if (bleBean != null && bleBean.getOKBLEDeviceImp() != null) {
                    if (bleBean.getOKBLEDeviceImp().isConnected()) {
                        bleBean.getOKBLEDeviceImp().disConnect(false);
                    }
                }
            }
        }

    }*/

    private void dismissLoading() {
        if (getActivity() == null) {
            Timber.e("dismissLoading getActivity() == null");
            return;
        }
        getActivity().runOnUiThread(() -> {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        });
    }

    private void showLoading(@NotNull String message) {
        if (getActivity() == null) {
            Timber.e("showLoading getActivity() == null");
            return;
        }
        getActivity().runOnUiThread(() -> {
            if (mLoadingDialog != null) {
                if (mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
            }
            mLoadingDialog = new CustomerLoadingDialog.Builder(getContext())
                    .setMessage(message)
                    .setCancelable(true)
                    .setCancelOutside(false)
                    .create();
            mLoadingDialog.show();
        });
    }

    private Disposable mOpenOrCloseDoorDisposable;


    private void processMQttMsg(@NotNull MqttData mqttData) {
        if (TextUtils.isEmpty(mqttData.getFunc())) {
            return;
        }
        if (mqttData.getFunc().equals(MQttConstant.SET_LOCK)) {

        }
    }

    private void processRecord(@NotNull WifiLockOperationEventBean bean) {
        if (bean == null) {
            Timber.e("processRecord RECORD bean == null");
            return;
        }
        if (bean.getWfId() == null) {
            Timber.e("processRecord RECORD bean.getWfId() == null");
            return;
        }
        if (bean.getEventparams() == null) {
            Timber.e("processRecord RECORD bean.getEventparams() == null");
            return;
        }
        if (bean.getEventtype() == null) {
            Timber.e("processRecord RECORD bean.getEventtype() == null");
            return;
        }
        if (!bean.getEventtype().equals(MQttConstant.RECORD)) {
            Timber.e("processRecord RECORD eventType: %1s", bean.getEventtype());
            return;
        }
        if (bean.getEventparams().getEventType() == 1) {
            // 动作操作
            int eventCode = bean.getEventparams().getEventCode();
            if (eventCode == 0x01 || eventCode == 0x08 || eventCode == 0x0D || eventCode == 0x0A) {
                // 上锁
                setLockState(getPositionFromWifiId(bean.getWfId()), LocalState.LOCK_STATE_CLOSE);
            } else if (eventCode == 2 || eventCode == 0x09 || eventCode == 0x0E) {
                // 开锁
                setLockState(getPositionFromWifiId(bean.getWfId()), LocalState.LOCK_STATE_OPEN);
            }
        } else if (bean.getEventparams().getEventType() == 3) {
            int eventCode = bean.getEventparams().getEventCode();
            if (eventCode == 5) {
                // 上锁
                setLockState(getPositionFromWifiId(bean.getWfId()), LocalState.LOCK_STATE_PRIVATE);
            }
        } else if (bean.getEventparams().getEventType() == 4) {
            // 传感器上报，门磁
            if (bean.getEventparams().getEventCode() == 1) {
                // 门磁开门
                setDoorState(getPositionFromWifiId(bean.getWfId()), LocalState.DOOR_SENSOR_OPEN);
            } else if (bean.getEventparams().getEventCode() == 2) {
                // 门磁关门
                setDoorState(getPositionFromWifiId(bean.getWfId()), LocalState.DOOR_SENSOR_CLOSE);
            } else if (bean.getEventparams().getEventCode() == 3) {
                // 门磁异常
                Timber.e("processRecord 门磁异常");
            }
        }
    }

    private void processSetLock(@NotNull WifiLockDoorOptResponseBean bean) {
        if (bean == null) {
            Timber.e("processSetLock bean == null");
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("processSetLock bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("processSetLock code : %1d", bean.getCode());
        }
    }

}