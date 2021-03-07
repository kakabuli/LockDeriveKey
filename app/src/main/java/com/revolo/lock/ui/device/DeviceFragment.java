package com.revolo.lock.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.adapter.HomeLockListAdapter;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockDoorOptResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockGetAllBindDeviceRspBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.ui.TitleBar;
import com.revolo.lock.ui.device.add.AddDeviceActivity;
import com.revolo.lock.ui.device.lock.DeviceDetailActivity;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mDeviceViewModel =
                new ViewModelProvider(this).get(DeviceViewModel.class);

        View root = inflater.inflate(R.layout.fragment_device, container, false);
        mClNoDevice = root.findViewById(R.id.clNoDevice);
        mClHadDevice = root.findViewById(R.id.clHadDevice);
        mDeviceViewModel.getWifiListBeans().observe(getViewLifecycleOwner(), this::updateDataFromNet);
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
                    intent.putExtra(Constant.LOCK_DETAIL, deviceLocal);
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
                        publishOpenOrCloseDoor(
                                mHomeLockListAdapter.getItem(position).getEsn(),
                                state==LocalState.LOCK_STATE_OPEN?LocalState.DOOR_STATE_CLOSE:LocalState.DOOR_STATE_OPEN,
                                App.getInstance().getRandomCode(), mBleDeviceLocals.get(position));
                    }
                }
            });
            rvLockList.setAdapter(mHomeLockListAdapter);
            if(getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).setStatusBarColor(R.color.white);
            }
        }

        initBaseData();
        // TODO: 2021/3/4 按列表来处理
        if(mBleDeviceLocals == null || mBleDeviceLocals.isEmpty()) {
            initData(mBleDeviceLocals);
        } else {
            if(mBleDeviceLocals.get(0).getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                initData(mBleDeviceLocals);
            }
        }
        return root;
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

    private void updateDataFromNet(List<WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean> wifiListBeans) {
        List<BleDeviceLocal> locals = new ArrayList<>();
        for (WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean : wifiListBeans) {
            // TODO: 2021/2/26 后期再考虑是否需要多条件合并查询
            BleDeviceLocal bleDeviceLocal = AppDatabase.getInstance(getContext()).bleDeviceDao().findBleDeviceFromEsn(wifiListBean.getWifiSN());
            if(bleDeviceLocal == null) {
                Timber.e("updateDataFromNet bleDeviceLocal == null");
                continue;
            }
            locals.add(bleDeviceLocal);
        }
        if(locals.isEmpty()) {
            Timber.e("updateDataFromNet locals.isEmpty()");
            return;
        }
        // TODO: 2021/3/2 暂时使用第一个值，后续通过选择
        App.getInstance().setRandomCode(wifiListBeans.get(0).getRandomCode());
        updateData(locals);
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

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        if(bean.getCMD() == BleProtocolState.CMD_LOCK_INFO) {
            lockInfo(bean);
        } else if(bean.getCMD() == BleProtocolState.CMD_LOCK_CONTROL_ACK) {
            controlOpenOrCloseDoorAck(bean);
        } else if(bean.getCMD() == BleProtocolState.CMD_LOCK_UPLOAD) {
            lockUpdateInfo(bean);
        }
    }

    private void lockInfo(BleResultBean bean) {
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

    private void controlOpenOrCloseDoorAck(BleResultBean bean) {
        // TODO: 2021/2/7 处理控制开关锁确认帧
        if(getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            if(bean.getPayload()[0] == 0x00) {
                refreshLockState();
            }
        });

    }

    private void refreshLockState() {
        @LocalState.LockState int state = mHomeLockListAdapter.getData().get(0).getLockState();
        if(state == LocalState.LOCK_STATE_OPEN) {
            state = LocalState .LOCK_STATE_CLOSE;
        } else if(state == LocalState.LOCK_STATE_CLOSE) {
            state = LocalState.LOCK_STATE_OPEN;
        }
        setLockState(0, state);
    }

    private void lockUpdateInfo(BleResultBean bean) {
        // TODO: 2021/2/7 锁操作上报
        int eventType = bean.getPayload()[0];
        int eventSource = bean.getPayload()[1];
        int eventCode = bean.getPayload()[2];
        int userID = bean.getPayload()[3];
        byte[] time = new byte[4];
        System.arraycopy(bean.getPayload(), 4, time, 0, time.length);
        // TODO: 2021/2/8 要做时间都是ffffffff的处理判断
        long realTime = (BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time)) + Constant.WILL_ADD_TIME)*1000;
        Timber.d("CMD: %1d, eventType: %2d, eventSource: %3d, eventCode: %4d, userID: %5d, time: %6d",
                bean.getCMD(), eventType, eventSource, eventCode, userID, realTime);

        // TODO: 2021/2/10 后期需要移植修改
        if(getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            if(eventType == 0x01) {
                if(eventSource == 0x01) {
                    // 上锁
                    setLockState(0, LocalState.LOCK_STATE_CLOSE);
                } else if(eventCode == 0x02) {
                    // 开锁
                    setLockState(0, LocalState.LOCK_STATE_OPEN);
                } else {
                    // TODO: 2021/2/10 其他处理
                }
            }
        });
    }

    private void setLockState(int index, @LocalState.LockState int state) {
        if(getActivity() == null) {
            Timber.e("setLockState getActivity() == null");
            return;
        }
        getActivity().runOnUiThread(() -> {
            BleDeviceLocal local = mHomeLockListAdapter.getData().get(index);
            local.setLockState(state);
            AppDatabase.getInstance(getContext()).bleDeviceDao().update(local);
            mHomeLockListAdapter.notifyDataSetChanged();
        });
    }

    private List<BleDeviceLocal> mBleDeviceLocals;

    private void initBaseData() {
        User user = App.getInstance().getUser();
        if(user == null) {
            return;
        }
        mBleDeviceLocals = AppDatabase.getInstance(App.getInstance()).bleDeviceDao().findBleDevicesFromUserIdByCreateTimeDesc(user.getId());
        if(mBleDeviceLocals == null) {
            return;
        }
        if(mBleDeviceLocals.isEmpty()) {
            return;
        }
        updateData(mBleDeviceLocals);

    }

    private void initData(List<BleDeviceLocal> bleDeviceLocals) {
        if(bleDeviceLocals == null) {
            return;
        }
        if(bleDeviceLocals.isEmpty()) {
            return;
        }
        // TODO: 2021/3/7 暂时所有设备都连接上，后续得考虑如何优化，因为暂时只有很少设备测试，大规模设备需要另一套机制处理
        for (BleDeviceLocal local : bleDeviceLocals) {
            if(local.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                BleBean bleBean = App.getInstance().getBleBeanFromMac(local.getMac());
                if(bleBean == null) {
                    BLEScanResult bleScanResult = ConvertUtils.bytes2Parcelable(local.getScanResultJson(), BLEScanResult.CREATOR);
                    if(bleScanResult != null) {
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
                                BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
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

    /**
     *  开关门
     * @param wifiId wifi的id
     * @param doorOpt 1:表示开门，0表示关门
     */
    public void publishOpenOrCloseDoor(String wifiId, @LocalState.DoorState int doorOpt, String randomCode, BleDeviceLocal bleDeviceLocal) {
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
        App.getInstance().getMqttService().mqttPublish(
                MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLock(
                        wifiId,
                        doorOpt,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(bleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(bleDeviceLocal.getPwd2())),
                        randomCode))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS).safeSubscribe(new Observer<MqttData>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull MqttData mqttData) {
                if(TextUtils.isEmpty(mqttData.getFunc())) {
                    return;
                }
                // TODO: 2021/3/3 处理开关门的回调信息
                if(mqttData.getFunc().equals(MqttConstant.SET_LOCK)) {
                    dismissLoading();
                    Timber.d("publishOpenOrCloseDoor 开关门信息: %1s", mqttData);
                    WifiLockDoorOptResponseBean bean;
                    try {
                        bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockDoorOptResponseBean.class);
                    } catch (JsonSyntaxException e) {
                        Timber.e(e);
                        return;
                    }
                    if(bean == null) {
                        Timber.e("publishOpenOrCloseDoor bean == null");
                        return;
                    }
                    if(bean.getParams() == null) {
                        Timber.e("publishOpenOrCloseDoor bean.getParams() == null");
                        return;
                    }
                    if(bean.getCode() != 200) {
                        Timber.e("publishOpenOrCloseDoor code : %1d", bean.getCode());
                        return;
                    }
                    refreshLockState();
                }
                Timber.d("publishOpenOrCloseDoor %1s", mqttData.toString());
            }

            @Override
            public void onError(@NotNull Throwable e) {
                dismissLoading();
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

}