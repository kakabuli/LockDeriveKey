package com.revolo.lock.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.adapter.HomeLockListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.SignalWeakDialog;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.eventbean.WifiLockOperationEventBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockDoorOptResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockGetAllBindDeviceRspBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.ui.TitleBar;
import com.revolo.lock.ui.device.add.AddDeviceActivity;
import com.revolo.lock.ui.device.lock.DeviceDetailActivity;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.ble.BleCommandState.LOCK_SETTING_CLOSE;
import static com.revolo.lock.ble.BleCommandState.LOCK_SETTING_OPEN;

public class DeviceFragment extends Fragment {

    private DeviceViewModel mDeviceViewModel;
    private HomeLockListAdapter mHomeLockListAdapter;
    private ConstraintLayout mClNoDevice, mClHadDevice;
    private CustomerLoadingDialog mLoadingDialog;
    private RefreshLayout mRefreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        mDeviceViewModel =
//                new ViewModelProvider(this).get(DeviceViewModel.class);

        View root = inflater.inflate(R.layout.fragment_device, container, false);
        mClNoDevice = root.findViewById(R.id.clNoDevice);
        mClHadDevice = root.findViewById(R.id.clHadDevice);
//        mDeviceViewModel.getWifiListBeans().observe(getViewLifecycleOwner(), this::updateDataFromNet);
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
                if(adapter.getItem(position) instanceof BleDeviceLocal) {
                    if(position < 0 || position >= adapter.getData().size()) return;
                    BleDeviceLocal deviceLocal = (BleDeviceLocal) adapter.getItem(position);
                    Intent intent = new Intent(getContext(), DeviceDetailActivity.class);
                    App.getInstance().setBleDeviceLocal(deviceLocal);
                    startActivity(intent);
                }
            });
            mHomeLockListAdapter.addChildClickViewIds(R.id.ivLockState);
            mHomeLockListAdapter.setOnItemChildClickListener((adapter, view, position) -> {
                if(view.getId() == R.id.ivLockState) {
                    @LocalState.LockState int state = mHomeLockListAdapter.getItem(position).getLockState();
                    if(mBleDeviceLocals.get(position).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
                        openOrCloseDoorFromBle(position, state);
                    } else if(mBleDeviceLocals.get(position).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                        // TODO: 2021/4/1 主编号是0，分享用户再使用分享用户的编号
                        publishOpenOrCloseDoor(
                                mHomeLockListAdapter.getItem(position).getEsn(),
                                state==LocalState.LOCK_STATE_OPEN?LocalState.DOOR_STATE_CLOSE:LocalState.DOOR_STATE_OPEN,
                                mBleDeviceLocals.get(position), 0);
                    }
                }
            });
            rvLockList.setAdapter(mHomeLockListAdapter);
            if(getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).setStatusBarColor(R.color.white);
            }
        }

        mRefreshLayout = root.findViewById(R.id.refreshLayout);
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
//            mDeviceViewModel.refreshGetAllBindDevicesFromMQTT();
            refreshGetAllBindDevicesFromMQTT();
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshGetAllBindDevicesFromMQTT();
        initBaseData();
        initSignalWeakDialog();
        initData(mBleDeviceLocals);
        mHomeLockListAdapter.setList(mBleDeviceLocals);
//        mDeviceViewModel.refreshGetAllBindDevicesFromMQTT();
    }

    private void openOrCloseDoorFromBle(int position, @LocalState.LockState int state) {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocals.get(position).getMac());
        if(bleBean == null) {
            Timber.e("openOrCloseDoorFromBle bleBean == null");
            return;
        }
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("openOrCloseDoorFromBle bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if(bleBean.getPwd1() == null) {
            Timber.e("openOrCloseDoorFromBle bleBean.getPwd1() == null");
            return;
        }
        if(bleBean.getPwd3() == null) {
            Timber.e("openOrCloseDoorFromBle bleBean.getPwd3() == null");
            return;
        }
        App.getInstance().writeControlMsg(
                BleCommandFactory.lockControlCommand(
                        (byte) (state== LocalState.LOCK_STATE_OPEN?LOCK_SETTING_CLOSE:LOCK_SETTING_OPEN),
                        (byte) 0x04,
                        (byte) 0x01,
                        bleBean.getPwd1(),
                        bleBean.getPwd3()),
                bleBean.getOKBLEDeviceImp());
    }

    private Disposable mBindDevicesDisposable;

    public void refreshGetAllBindDevicesFromMQTT() {
        if(App.getInstance().getUserBean() == null) {
            return;
        }
        if(getActivity() == null) {
            Timber.e("refreshGetAllBindDevicesFromMQTT getActivity() == null");
            return;
        }
        BaseActivity baseActivity = ((BaseActivity) getActivity());
        if(baseActivity.mMQttService == null) {
            Timber.e("refreshGetAllBindDevicesFromMQTT baseActivity.mMQttService == null");
            return;
        }
        baseActivity.toDisposable(mBindDevicesDisposable);
        Timber.d("执行获取设备信息");
        mBindDevicesDisposable = baseActivity.mMQttService
                .mqttPublish(MqttConstant.PUBLISH_TO_SERVER,
                        MqttCommandFactory.getAllBindDevices(App.getInstance().getUserBean().getUid()))
                .filter(mqttData -> mqttData.getFunc().equals(MqttConstant.GET_ALL_BIND_DEVICE))
                .subscribe(mqttData -> {
                    baseActivity.toDisposable(mBindDevicesDisposable);
                    processDevices(mqttData);
                }, Timber::e);
        baseActivity.mCompositeDisposable.add(mBindDevicesDisposable);
    }

    private void processDevices(MqttData mqttData) {
        if(TextUtils.isEmpty(mqttData.getFunc())) {
            return;
        }
        if(!mqttData.getFunc().equals(MqttConstant.GET_ALL_BIND_DEVICE)) {
            return;
        }
        WifiLockGetAllBindDeviceRspBean bean;
        try {
            bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockGetAllBindDeviceRspBean.class);
        } catch (JsonSyntaxException e) {
            // TODO: 2021/2/6 解析失败的处理
            Timber.e(e);
            return;
        }
        if(bean == null) {
            Timber.e("WifiLockGetAllBindDeviceRspBean is null");
            return;
        }
        if(TextUtils.isEmpty(bean.getMsgtype())) {
            return;
        }
        if(!bean.getMsgtype().equals("response")) {
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
        updateDataFromNet(bean.getData().getWifiList());
    }

    private void updateDataFromNet(List<WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean> wifiListBeans) {
        if(mRefreshLayout != null) {
            mRefreshLayout.finishRefresh();
        }
        mBleDeviceLocals.clear();
        for (WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean : wifiListBeans) {
            // TODO: 2021/2/26 后期再考虑是否需要多条件合并查询
            BleDeviceLocal bleDeviceLocal = AppDatabase
                    .getInstance(getContext()).bleDeviceDao().findBleDeviceFromEsnAndUserId(
                            wifiListBean.getWifiSN(),
                            App.getInstance().getUser().getId());
            if(bleDeviceLocal == null) {
                Timber.e("updateDataFromNet bleDeviceLocal == null");
                bleDeviceLocal = createDeviceToLocal(wifiListBean);
            }
            bleDeviceLocal.setName(wifiListBean.getLockNickname());
            String firmwareVer = wifiListBean.getLockFirmwareVersion();
            if(!TextUtils.isEmpty(firmwareVer)) {
                bleDeviceLocal.setLockVer(firmwareVer);
            }
            String wifiVer = wifiListBean.getWifiVersion();
            if(!TextUtils.isEmpty(wifiVer)) {
                bleDeviceLocal.setWifiVer(wifiVer);
            }
            Timber.d("wifiESN: %1s, 电量：%2d", wifiListBean.getWifiSN(), wifiListBean.getPower());
            bleDeviceLocal.setLockPower(wifiListBean.getPower());
            // 0 锁端wifi没有与服务器连接   1 锁端wifi与服务器连接成功
            boolean isWifiConnected = (wifiListBean.getWifiStatus().equals("1"));
            bleDeviceLocal.setConnectedType(isWifiConnected?
                    LocalState.DEVICE_CONNECT_TYPE_WIFI:LocalState.DEVICE_CONNECT_TYPE_BLE);
            bleDeviceLocal.setRandomCode(wifiListBean.getRandomCode());
            AppDatabase.getInstance(getContext()).bleDeviceDao().update(bleDeviceLocal);
            mBleDeviceLocals.add(bleDeviceLocal);
        }
        if(mBleDeviceLocals.isEmpty()) {
            Timber.e("updateDataFromNet locals.isEmpty()");
            return;
        }
        updateData(mBleDeviceLocals);
    }

    private BleDeviceLocal createDeviceToLocal(WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean) {
        // TODO: 2021/3/16 存储数据
        BleDeviceLocal bleDeviceLocal;
        bleDeviceLocal = new BleDeviceLocal();
        bleDeviceLocal.setRandomCode(wifiListBean.getRandomCode());
        bleDeviceLocal.setWifiVer(wifiListBean.getWifiVersion());
        bleDeviceLocal.setLockVer(wifiListBean.getLockFirmwareVersion());
        bleDeviceLocal.setName(wifiListBean.getLockNickname());
//        bleDeviceLocal.setOpenDoorSensor(wifiListBean.getDoorSensor()==1);
//        bleDeviceLocal.setDoNotDisturbMode(wifiListBean.get);
//        bleDeviceLocal.setSetAutoLockTime(wifiListBean.getAutoLockTime());
//        bleDeviceLocal.setMute();
        // TODO: 2021/3/18 修改为从服务器获取数据
        bleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_WIFI);
//        bleDeviceLocal.setLockPower();
        bleDeviceLocal.setLockState(wifiListBean.getOpenStatus());
//        bleDeviceLocal.setSetElectricFenceSensitivity();
//        bleDeviceLocal.setSetElectricFenceTime();
//        bleDeviceLocal.setDetectionLock();
//        bleDeviceLocal.setAutoLock();
//        bleDeviceLocal.setDuress();
        bleDeviceLocal.setConnectedWifiName(wifiListBean.getWifiName());
        bleDeviceLocal.setCreateTime(wifiListBean.getCreateTime());
        bleDeviceLocal.setPwd2(wifiListBean.getPassword2());
        bleDeviceLocal.setPwd1(wifiListBean.getPassword1());
        bleDeviceLocal.setMac(wifiListBean.getBleMac());
        bleDeviceLocal.setEsn(wifiListBean.getWifiSN());
//        bleDeviceLocal.setDoorSensor();
//        bleDeviceLocal.setFunctionSet();
//        bleDeviceLocal.setOpenElectricFence();
        bleDeviceLocal.setType(wifiListBean.getModel());
        bleDeviceLocal.setUserId(App.getInstance().getUser().getId());
        long id = AppDatabase.getInstance(getContext()).bleDeviceDao().insert(bleDeviceLocal);
        bleDeviceLocal.setId(id);
        return bleDeviceLocal;
    }

    private void updateData(List<BleDeviceLocal> locals) {
        if(locals != null) {
            if(locals.isEmpty()) {
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
        if(bean.getCMD() == BleProtocolState.CMD_LOCK_INFO) {
            lockInfo(mac, bean);
        } else if(bean.getCMD() == BleProtocolState.CMD_LOCK_UPLOAD) {
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
        long realTime = (BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time)) + Constant.WILL_ADD_TIME)*1000;
        Timber.d("CMD: %1d, lockFunBytes: bit0_7: %2s, bit8_15: %3s, bit16_23: %4s, lockStateBit0_7: %5s, lockStateBit8_15: %6s, soundVolume: %7d, language: %8s, battery: %9d, time: %10d",
                bean.getCMD(), ConvertUtils.bytes2HexString(bit0_7), ConvertUtils.bytes2HexString(bit8_15),
                ConvertUtils.bytes2HexString(bit16_23), ConvertUtils.bytes2HexString(lockStateBit0_7),
                ConvertUtils.bytes2HexString(lockStateBit8_15), soundVolume, languageStr, battery, realTime);

    }

    private int getPositionFromMac(@NotNull String mac) {
        if(mBleDeviceLocals.isEmpty()) {
            return -1;
        }
        for (int i=0; i<mBleDeviceLocals.size(); i++) {
            if(mac.equals(mBleDeviceLocals.get(i).getMac())) {
                return i;
            }
        }
        return -1;
    }

    private int getPositionFromWifiId(@NotNull String wifiID) {
        if(mBleDeviceLocals.isEmpty()) {
            return -1;
        }
        for (int i=0; i<mBleDeviceLocals.size(); i++) {
            if(wifiID.equals(mBleDeviceLocals.get(i).getEsn())) {
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
        long realTime = (BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time)))*1000;
        Timber.d("lockUpdateInfo CMD: %1d, eventType: %2d, eventSource: %3d, eventCode: %4d, userID: %5d, time: %6d",
                bean.getCMD(), eventType, eventSource, eventCode, userID, realTime);

        // TODO: 2021/2/10 后期需要移植修改
        if(getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            if(eventType == 0x01) {
                if(eventCode == 0x01) {
                    // 上锁
                    setLockState(getPositionFromMac(mac), LocalState.LOCK_STATE_CLOSE);
                } else if(eventCode == 0x02) {
                    // 开锁
                    setLockState(getPositionFromMac(mac), LocalState.LOCK_STATE_OPEN);
                }
//                else {
//                    // TODO: 2021/2/10 其他处理
//                }
            } else if(eventType == 0x04) {
                // sensor附加状态，门磁
                if(eventCode == LocalState.DOOR_SENSOR_OPEN) {
                    // 开门
                    setDoorState(getPositionFromMac(mac), LocalState.DOOR_SENSOR_OPEN);
                } else if(eventCode == LocalState.DOOR_SENSOR_CLOSE) {
                    // 关门
                    setDoorState(getPositionFromMac(mac), LocalState.DOOR_SENSOR_CLOSE);
                } else if(eventCode == LocalState.DOOR_SENSOR_EXCEPTION) {
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
        if(getActivity() == null) {
            Timber.e("setLockState getActivity() == null");
            return;
        }
        if(index == -1) {
            Timber.e("setLockState index == -1");
            return;
        }
        getActivity().runOnUiThread(() -> {
            BleDeviceLocal local = mHomeLockListAdapter.getData().get(index);
            local.setLockState(state);
            AppDatabase.getInstance(getContext()).bleDeviceDao().update(local);
            mHomeLockListAdapter.notifyDataSetChanged();
        });
    }

    private void setDoorState(int index, @LocalState.DoorSensor int state) {
        if(getActivity() == null) {
            Timber.e("setDoorState getActivity() == null");
            return;
        }
        if(index == -1) {
            Timber.e("setDoorState index == -1");
            return;
        }
        getActivity().runOnUiThread(() -> {
            BleDeviceLocal local = mHomeLockListAdapter.getData().get(index);
            local.setDoorSensor(state);
            AppDatabase.getInstance(getContext()).bleDeviceDao().update(local);
            mHomeLockListAdapter.notifyDataSetChanged();
        });
    }

    private List<BleDeviceLocal> mBleDeviceLocals;

    private void initBaseData() {
        User user = App.getInstance().getUser();
        mBleDeviceLocals = App.getInstance().getBleDeviceLocals();
        if(user == null) {
            return;
        }
        List<BleDeviceLocal> locals = AppDatabase.getInstance(App.getInstance()).bleDeviceDao().findBleDevicesFromUserIdByCreateTimeDesc(user.getId());
        if(locals == null) {
            return;
        }
        if(locals.isEmpty()) {
            return;
        }
        App.getInstance().addBleDeviceLocals(locals);
        updateData(mBleDeviceLocals);

    }

    private void initData(List<BleDeviceLocal> bleDeviceLocals) {
        if(bleDeviceLocals == null) {
            Timber.e("initData bleDeviceLocals == null");
            return;
        }
        if(bleDeviceLocals.isEmpty()) {
            Timber.e("initData bleDeviceLocals is Empty");
            return;
        }
        // TODO: 2021/3/7 暂时所有设备都连接上，后续得考虑如何优化，因为暂时只有很少设备测试，大规模设备需要另一套机制处理
        for (BleDeviceLocal local : bleDeviceLocals) {
            if(local.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                BleBean bleBean = App.getInstance().getBleBeanFromMac(local.getMac());
                OnBleDeviceListener onBleDeviceListener = new OnBleDeviceListener() {
                    @Override
                    public void onConnected(@NotNull String mac) {

                    }

                    @Override
                    public void onDisconnected(@NotNull String mac) {

                    }

                    @Override
                    public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
                        if(value == null) {
                            return;
                        }
                        if(!local.getMac().equals(mac)) {
                            return;
                        }
                        BleBean bleBean = App.getInstance().getBleBeanFromMac(local.getMac());
                        if(bleBean == null) {
                            return;
                        }
                        if(bleBean.getOKBLEDeviceImp() == null) {
                            return;
                        }
                        if(bleBean.getPwd1() == null) {
                            return;
                        }
                        if(bleBean.getPwd2() == null) {
                            return;
                        }
                        BleResultProcess.setOnReceivedProcess(bleResultBean -> {
                            if(bleResultBean == null) {
                                Timber.e("%1s mOnReceivedProcess bleResultBean == null", local.getMac());
                                return;
                            }
                            processBleResult(local.getMac(), bleResultBean);
                        });
                        BleResultProcess.processReceivedData(
                                value,
                                bleBean.getPwd1(),
                                (bleBean.getPwd3() == null)?bleBean.getPwd2():bleBean.getPwd3(),
                                bleBean.getOKBLEDeviceImp().getBleScanResult());
                    }

                    @Override
                    public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

                    }

                    @Override
                    public void onAuthSuc(@NotNull String mac) {

                    }

                };
                if(bleBean == null) {
                    BLEScanResult bleScanResult = ConvertUtils.bytes2Parcelable(local.getScanResultJson(), BLEScanResult.CREATOR);
                    if(bleScanResult != null) {
                        bleBean = App.getInstance().connectDevice(
                                bleScanResult,
                                ConvertUtils.hexString2Bytes(local.getPwd1()),
                                ConvertUtils.hexString2Bytes(local.getPwd2()),
                                onBleDeviceListener,false);
                        bleBean.setEsn(local.getEsn());
                    } else {
                        // TODO: 2021/1/26 处理为空的情况
                    }
                } else {
                    if(bleBean.getOKBLEDeviceImp() != null) {
                        bleBean.setOnBleDeviceListener(onBleDeviceListener);
                        if(!bleBean.getOKBLEDeviceImp().isConnected()) {
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
                if(bleBean != null && bleBean.getOKBLEDeviceImp() != null) {
                    if(bleBean.getOKBLEDeviceImp().isConnected()) {
                        bleBean.getOKBLEDeviceImp().disConnect(false);
                    }
                }
            }
        }

    }

    private void dismissLoading() {
        if(getActivity() == null) {
            Timber.e("dismissLoading getActivity() == null");
            return;
        }
        getActivity().runOnUiThread(() -> {
            if(mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        });
    }

    private void showLoading(@NotNull String message) {
        if(getActivity() == null) {
            Timber.e("showLoading getActivity() == null");
            return;
        }
        getActivity().runOnUiThread(() -> {
            if(mLoadingDialog != null) {
                if(mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
            }
            // TODO: 2021/2/25 抽离文字
            mLoadingDialog = new CustomerLoadingDialog.Builder(getContext())
                    .setMessage(message)
                    .setCancelable(true)
                    .setCancelOutside(false)
                    .create();
            mLoadingDialog.show();
        });
    }

    private Disposable mOpenOrCloseDoorDisposable;

    /**
     *  开关门
     * @param wifiId wifi的id
     * @param doorOpt 1:表示开门，0表示关门
     */
    public void publishOpenOrCloseDoor(String wifiId,
                                       @LocalState.DoorState int doorOpt,
                                       BleDeviceLocal bleDeviceLocal, int num) {
        if(App.getInstance().getUserBean() == null) {
            Timber.e("publishOpenOrCloseDoor App.getInstance().getUserBean() == null");
            return;
        }
        if(bleDeviceLocal == null) {
            Timber.e("publishOpenOrCloseDoor bleDeviceLocal == null");
            return;
        }
        if(doorOpt == LocalState.DOOR_STATE_OPEN) {
            showLoading("Lock Opening...");
        } else if(doorOpt == LocalState.DOOR_STATE_CLOSE) {
            showLoading("Lock Closing...");
        }
        if(getActivity() == null) {
            Timber.e("refreshGetAllBindDevicesFromMQTT getActivity() == null");
            return;
        }
        BaseActivity baseActivity = ((BaseActivity) getActivity());
        if(baseActivity.mMQttService == null) {
            Timber.e("refreshGetAllBindDevicesFromMQTT baseActivity.mMQttService == null");
            return;
        }
        mCount++;
        baseActivity.toDisposable(mOpenOrCloseDoorDisposable);
        mOpenOrCloseDoorDisposable = baseActivity.mMQttService
                .mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLock(
                        wifiId,
                        doorOpt,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(bleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(bleDeviceLocal.getPwd2())),
                        bleDeviceLocal.getRandomCode(),
                        num))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .subscribe(mqttData -> {
                    baseActivity.toDisposable(mOpenOrCloseDoorDisposable);
                    mCount = 0;
                    processMQttMsg(mqttData, wifiId);
                }, e -> {
                    dismissLoading();
                    if(e instanceof TimeoutException) {
                        if(mCount == 3) {
                            // 3次机会,超时失败开始连接蓝牙
                            mCount = 0;
                            mBleDeviceLocal = bleDeviceLocal;
                            if(getActivity() == null) return;
                            getActivity().runOnUiThread(() -> {
                                if(mSignalWeakDialog != null) {
                                    mSignalWeakDialog.show();
                                }
                            });
                        }
                    }
                    Timber.e(e);
                });
        baseActivity.mCompositeDisposable.add(mOpenOrCloseDoorDisposable);
    }

    private void processMQttMsg(@NotNull MqttData mqttData, String wifiId) {
        if(TextUtils.isEmpty(mqttData.getFunc())) {
            return;
        }
        if(mqttData.getFunc().equals(MqttConstant.SET_LOCK)) {
            processSetLock(mqttData);
        } else if(mqttData.getFunc().equals(MqttConstant.WF_EVENT)) {
            processRecord(mqttData, wifiId);
        }
    }

    private void processRecord(@NotNull MqttData mqttData, String wifiId) {
        WifiLockOperationEventBean bean;
        try {
            bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockOperationEventBean.class);
        } catch (JsonSyntaxException e) {
            Timber.e(e);
            return;
        }
        if(bean == null) {
            Timber.e("processRecord RECORD bean == null");
            return;
        }
        if(bean.getWfId() == null) {
            Timber.e("processRecord RECORD bean.getWfId() == null");
            return;
        }
        if(!bean.getWfId().equals(wifiId)) {
            Timber.e("processRecord RECORD wifiId: %1s current esn: %2s",
                    bean.getWfId(), wifiId);
            return;
        }
        if(bean.getEventparams() == null) {
            Timber.e("processRecord RECORD bean.getEventparams() == null");
            return;
        }
        if(bean.getEventtype() == null) {
            Timber.e("processRecord RECORD bean.getEventtype() == null");
            return;
        }
        if(!bean.getEventtype().equals(MqttConstant.RECORD)) {
            Timber.e("processRecord RECORD eventType: %1s", bean.getEventtype());
            return;
        }
        if(bean.getEventparams().getEventType() == 1) {
            // 动作操作
            if(bean.getEventparams().getEventCode() == 1) {
                // 上锁
                setLockState(getPositionFromWifiId(wifiId), LocalState.LOCK_STATE_CLOSE);
            } else if(bean.getEventparams().getEventCode() == 2) {
                // 开锁
                setLockState(getPositionFromWifiId(wifiId), LocalState.LOCK_STATE_OPEN);
            }
        } else if(bean.getEventparams().getEventType() == 4) {
            // 传感器上报，门磁
            if(bean.getEventparams().getEventCode() == 1) {
                // 门磁开门
                setDoorState(getPositionFromWifiId(wifiId), LocalState.DOOR_SENSOR_OPEN);
            } else if(bean.getEventparams().getEventCode() == 2) {
                // 门磁关门
                setDoorState(getPositionFromWifiId(wifiId), LocalState.DOOR_SENSOR_CLOSE);
            } else if(bean.getEventparams().getEventCode() == 3) {
                // 门磁异常
                Timber.e("processRecord 门磁异常");
            }
        }
    }

    private void processSetLock(@NotNull MqttData mqttData) {
        dismissLoading();
        WifiLockDoorOptResponseBean bean;
        try {
            bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockDoorOptResponseBean.class);
        } catch (JsonSyntaxException e) {
            Timber.e(e);
            return;
        }
        if(bean == null) {
            Timber.e("processSetLock bean == null");
            return;
        }
        if(bean.getParams() == null) {
            Timber.e("processSetLock bean.getParams() == null");
            return;
        }
        if(bean.getCode() != 200) {
            Timber.e("processSetLock code : %1d", bean.getCode());
        }
    }

    /*-------------------------- 多次失败，弹出UI连接蓝牙 --------------------------*/
    private SignalWeakDialog mSignalWeakDialog;
    private int mCount = 0;
    private BleDeviceLocal mBleDeviceLocal;

    private void initSignalWeakDialog() {
        mSignalWeakDialog = new SignalWeakDialog(getContext());
        mSignalWeakDialog.setOnCancelClickListener(v -> {
            if(mSignalWeakDialog != null) {
                mSignalWeakDialog.dismiss();
            }
        });
        mSignalWeakDialog.setOnConfirmListener(v -> {
            if(mSignalWeakDialog != null) {
                mSignalWeakDialog.dismiss();
            }
            connectBle();
        });
    }

    private boolean isRestartConnectingBle = false;
    private BleBean mBleBean;

    private void connectBle() {
        if(mBleDeviceLocal == null) {
            return;
        }
        showLoading("Loading...");
        isRestartConnectingBle = true;
        OnBleDeviceListener onBleDeviceListener = new OnBleDeviceListener() {
            @Override
            public void onConnected(@NotNull String mac) {

            }

            @Override
            public void onDisconnected(@NotNull String mac) {

            }

            @Override
            public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
                if(value == null) {
                    return;
                }
                if(!mBleDeviceLocal.getMac().equals(mac)) {
                    return;
                }
                BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
                if(bleBean == null) {
                    return;
                }
                if(bleBean.getOKBLEDeviceImp() == null) {
                    return;
                }
                if(bleBean.getPwd1() == null) {
                    return;
                }
                if(bleBean.getPwd2() == null) {
                    return;
                }
                BleResultProcess.setOnReceivedProcess(bleResultBean -> {
                    if(bleResultBean == null) {
                        Timber.e("%1s mOnReceivedProcess bleResultBean == null", mBleDeviceLocal.getMac());
                        return;
                    }
                    processBleResult(mBleDeviceLocal.getMac(), bleResultBean);
                });
                BleResultProcess.processReceivedData(
                        value,
                        bleBean.getPwd1(),
                        (bleBean.getPwd3() == null)?bleBean.getPwd2():bleBean.getPwd3(),
                        bleBean.getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

            }

            @Override
            public void onAuthSuc(@NotNull String mac) {
                // 配对成功
                if(mac.equals(mBleDeviceLocal.getMac())) {
                    isRestartConnectingBle = false;
                }
            }

        };
        mBleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_BLE);
        mBleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(mBleBean == null) {
            BLEScanResult bleScanResult = ConvertUtils.bytes2Parcelable(mBleDeviceLocal.getScanResultJson(), BLEScanResult.CREATOR);
            if(bleScanResult != null) {
                mBleBean = App.getInstance().connectDevice(
                        bleScanResult,
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()),
                        onBleDeviceListener,false);
                mBleBean.setEsn(mBleDeviceLocal.getEsn());
            } else {
                // TODO: 2021/1/26 处理为空的情况
            }
        } else {
            if(mBleBean.getOKBLEDeviceImp() != null) {
                mBleBean.setOnBleDeviceListener(onBleDeviceListener);
                if(!mBleBean.getOKBLEDeviceImp().isConnected()) {
                    mBleBean.getOKBLEDeviceImp().connect(true);
                }
                mBleBean.setPwd1(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()));
                mBleBean.setPwd2(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()));
                mBleBean.setEsn(mBleDeviceLocal.getEsn());
            } else {
                // TODO: 2021/1/26 为空的处理
            }
        }
        // 1分钟后判断设备是否连接成功，否就恢复wifi状态，每秒判断一次是否配对设备成功
        mCountDownTimer.start();
    }

    private final CountDownTimer mCountDownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            if(mBleBean != null) {
                if(!isRestartConnectingBle) {
                    mBleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_BLE);
                    AppDatabase.getInstance(getContext()).bleDeviceDao().update(mBleDeviceLocal);
                    mCountDownTimer.cancel();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        mHomeLockListAdapter.notifyDataSetChanged();
                        dismissLoading();
                    }, 50);
                }
            }
        }

        @Override
        public void onFinish() {
            isRestartConnectingBle = false;
            if(mBleBean != null && mBleBean.getOKBLEDeviceImp() != null) {
                mBleBean.getOKBLEDeviceImp().disConnect(false);
            }
            dismissLoading();
        }
    };

}