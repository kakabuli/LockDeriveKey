package com.revolo.lock.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.adapter.HomeLockListAdapter;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
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
import java.util.List;

import timber.log.Timber;

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
    private TitleBar titleBar;

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
                    //判断隐私模式
                    @LocalState.LockState int mLockstate = ((BleDeviceLocal) adapter.getItem(position)).getLockState();
                    if (mLockstate == LocalState.LOCK_STATE_PRIVATE) {
                        return;
                    }
                    @LocalState.LockState int connectedState = ((BleDeviceLocal) adapter.getItem(position)).getConnectedType();
                    if (LocalState.DEVICE_CONNECT_TYPE_DIS == connectedState) {
                        return;
                    }
                    if (adapter.getItem(position) instanceof BleDeviceLocal) {
                        if (position < 0 || position >= adapter.getData().size()) return;
                        BleDeviceLocal deviceLocal = (BleDeviceLocal) adapter.getItem(position);
                        if (deviceLocal.getLockState() == LocalState.LOCK_STATE_PRIVATE)
                            return; // 隐私模式
                        Intent intent = new Intent(getContext(), DeviceDetailActivity.class);
                        App.getInstance().setmCurrMac(deviceLocal.getMac());
                        App.getInstance().setmCurrSn(deviceLocal.getEsn());
                        App.getInstance().setBleDeviceLocal(deviceLocal);
                        startActivity(intent);
                    }
                });
                mHomeLockListAdapter.addChildClickViewIds(R.id.ivLockState);
                mHomeLockListAdapter.setOnItemChildClickListener((adapter, view, position) -> {
                    if (view.getId() == R.id.ivLockState) {
                        //判断隐私模式
                        //@LocalState.LockState int mLockstate = ((BleDeviceLocal) adapter.getItem(position)).getLockState();
                        @LocalState.LockState int state = ((BleDeviceLocal) adapter.getItem(position)).getLockState();
                        if (state == LocalState.LOCK_STATE_PRIVATE) {
                            return;
                        }
                        //判断设备是否掉线
                        @LocalState.LockState int connectedState = ((BleDeviceLocal) adapter.getItem(position)).getConnectedType();
                        if (LocalState.DEVICE_CONNECT_TYPE_DIS == connectedState) {
                            return;
                        }

                        openOrCloseDoor(((BleDeviceLocal) adapter.getItem(position)).getEsn(),
                                state == LocalState.LOCK_STATE_OPEN ? LocalState.DOOR_STATE_CLOSE : LocalState.DOOR_STATE_OPEN,
                                (BleDeviceLocal) adapter.getItem(position), 0, position, state, connectedState);
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
        titleBar = new TitleBar(root).setTitle(getString(R.string.title_my_devices))
                .setRight(R.drawable.ic_home_icon_add,
                        v -> startActivity(new Intent(getContext(), AddDeviceActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        initBaseData();
        refreshGetAllBindDevicesFromMQTT();
        /*initData(mBleDeviceLocals);
        initSignalWeakDialog();
        initWfEven();*/
//        mDeviceViewModel.refreshGetAllBindDevicesFromMQTT();
        boolean b = NetworkUtils.isConnected();
        if (titleBar != null) {
            titleBar.setNetError(b);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventBus(LockMessageRes lockMessage) {

        mRefreshLayout.finishRefresh(true);
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_USER) {
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //数据正常
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_UPDATE_DEVICE_STATE:
                        //  mBleDeviceLocals = App.getInstance().getDeviceLists();
                        updateData(App.getInstance().getDeviceLists());
                        break;
                }
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
                        //   mBleDeviceLocals = App.getInstance().getDeviceLists();
                        updateData(App.getInstance().getDeviceLists());
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK://开关锁
                        if (null != lockMessage.getWifiLockBaseResponseBean()) {
                            if (MQttConstant.SET_LOCK.equals(lockMessage.getWifiLockBaseResponseBean().getFunc())) {
                                dismissLoading();
                                // processSetLock((WifiLockDoorOptResponseBean) lockMessage.getWifiLockBaseResponseBean());
                            }
                        }
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_WF_EVEN:
                        dismissLoading();
                        updateData(App.getInstance().getDeviceLists());
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
                        dismissLoading();
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
                                BleDeviceLocal bleDeviceLocal, int num, int position, @LocalState.LockState int state, int connectState) {
        LockMessage message = new LockMessage();
        if (connectState == LocalState.DEVICE_CONNECT_TYPE_BLE || connectState == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            //ble
            BleBean bleBean = App.getInstance().getUserBleBean(bleDeviceLocal.getMac());
            if (bleBean == null || bleBean.getOKBLEDeviceImp() == null || bleBean.getPwd1() == null || bleBean.getPwd3() == null) {
                Timber.e("openOrCloseDoorFromBle bleBean.getPwd3() == null");
                message.setBytes(null);
            } else {
                message.setMessageType(3);
                message.setBytes(BleCommandFactory.lockControlCommand(
                        (byte) (state == LocalState.LOCK_STATE_OPEN ? LOCK_SETTING_CLOSE : LOCK_SETTING_OPEN),
                        (byte) 0x04,
                        (byte) 0x01,
                        bleBean.getPwd1(),
                        bleBean.getPwd3()));
                message.setMac(bleDeviceLocal.getMac());
            }
        } else {
            //wifi
            if (App.getInstance().getUserBean() == null || bleDeviceLocal == null
                    || getActivity() == null) {
                message.setMqttMessage(null);
            } else {
                message.setSn(bleDeviceLocal.getEsn());
                message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
                message.setMqtt_message_code(MQttConstant.SET_LOCK);
                message.setMessageType(2);
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

    private void updateData(List<BleDeviceLocal> locals) {
        if (locals != null) {
            for (BleDeviceLocal local : locals) {
                Timber.e("data state:%s", local.toString());
            }
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
        Timber.e("rocessBleResult");
        if (bean == null) {
            return;
        }
        if (bean.getCMD() == BleProtocolState.CMD_LOCK_INFO) {
            lockInfo(mac, bean);
            dismissLoading();
        } else if (bean.getCMD() == BleProtocolState.CMD_LOCK_UPLOAD) {
            mHomeLockListAdapter.notifyDataSetChanged();
            dismissLoading();
        }
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

    private void initBaseData() {
        User user = App.getInstance().getUser();
        //   mBleDeviceLocals = App.getInstance().getDeviceLists();
        updateData(App.getInstance().getDeviceLists());
        if (user == null) {
            return;
        }
        List<BleDeviceLocal> locals = AppDatabase.getInstance(App.getInstance()).bleDeviceDao().findBleDevicesFromUserIdByCreateTimeDesc(user.getAdminUid());
        if (locals == null) {
            return;
        }
        if (locals.isEmpty()) {
            return;
        }
        App.getInstance().setDeviceLists(locals);
    }

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
}