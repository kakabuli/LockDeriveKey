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
                    if(mBleDeviceLocals.get(0).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
                        App.getInstance().writeControlMsg(BleCommandFactory
                                .lockControlCommand((byte) (state==1?LOCK_SETTING_CLOSE:LOCK_SETTING_OPEN), (byte) 0x04, (byte) 0x01, mBleBean.getPwd1(), mBleBean.getPwd3()));
                    } else if(mBleDeviceLocals.get(0).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                        publishOpenOrCloseDoor(mHomeLockListAdapter.getItem(position).getEsn(), state==1?1:0, App.getInstance().getRandomCode());
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
        if(mBleDeviceLocals.get(0).getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            initBleListener();
            initData(mBleDeviceLocals);
        }
        return root;
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
                // 上锁
                @LocalState.LockState int state = mHomeLockListAdapter.getData().get(0).getLockState();
                if(state == LocalState.LOCK_STATE_OPEN) {
                    state = LocalState .LOCK_STATE_CLOSE;
                } else if(state == LocalState.LOCK_STATE_CLOSE) {
                    state = LocalState.LOCK_STATE_OPEN;
                }

                setLockState(0, state);
            }
        });

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
                if(mBleBean != null && mBleBean.getPwd3() != null) {
                    mPwd3 = mBleBean.getPwd3();
                }
                BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
                BleResultProcess.processReceivedData(value, mPwd1, (mPwd3 == null)?mPwd2:mPwd3,
                        mBleBean.getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(String uuid, byte[] value, boolean success) {

            }

            @Override
            public void onAuthSuc() {
                // TODO: 2021/3/1 通过了才给处理
                mPwd3 = mBleBean.getPwd3();
            }
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
        // TODO: 2021/2/21 暂时选择第一个，后续整合成列表，然后做选择
        mEsn = mBleDeviceLocals.get(0).getEsn();
        mPwd1 = ConvertUtils.hexString2Bytes(mBleDeviceLocals.get(0).getPwd1());
        mPwd2 = ConvertUtils.hexString2Bytes(mBleDeviceLocals.get(0).getPwd2());
        updateData(mBleDeviceLocals);

    }

    private void initData(List<BleDeviceLocal> bleDeviceLocals) {
        if(bleDeviceLocals == null) {
            return;
        }
        if(bleDeviceLocals.isEmpty()) {
            return;
        }
        if(App.getInstance().getBleBean() == null) {
            BLEScanResult bleScanResult = ConvertUtils.bytes2Parcelable(bleDeviceLocals.get(0).getScanResultJson(), BLEScanResult.CREATOR);
            if(bleScanResult != null) {
                App.getInstance().connectDevice(bleScanResult);
                mBleBean = App.getInstance().getBleBean();
                mBleBean.setPwd1(mPwd1);
                mBleBean.setPwd2(mPwd2);
                mBleBean.setEsn(mEsn);
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
                mBleBean.setEsn(mEsn);
            } else {
                // TODO: 2021/1/26 为空的处理
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
    public void publishOpenOrCloseDoor(String wifiId, int doorOpt, String randomCode) {
        // TODO: 2021/2/6 发送开门或者关门的指令
        if(doorOpt == 1) {
            showLoading("Lock Opening...");
        } else if(doorOpt == 0) {
            showLoading("Lock Closing...");
        }
        App.getInstance().getMqttService().mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLock(wifiId, doorOpt, BleCommandFactory.getPwd(mPwd1, mPwd2), randomCode))
                .timeout(10, TimeUnit.SECONDS).safeSubscribe(new Observer<MqttData>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull MqttData mqttData) {
                dismissLoading();
                if(TextUtils.isEmpty(mqttData.getFunc())) {
                    return;
                }
                // TODO: 2021/3/3 处理开关门的回调信息
                if(mqttData.getFunc().equals(MqttConstant.SET_LOCK)) {
                    Timber.d("开关门信息: %1s", mqttData);
                }
                Timber.d("%1s", mqttData.toString());
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