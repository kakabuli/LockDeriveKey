package com.revolo.lock.ui.device;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.google.gson.reflect.TypeToken;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.HomeLockListAdapter;
import com.revolo.lock.bean.showBean.WifiShowBean;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.ui.TitleBar;
import com.revolo.lock.ui.device.add.AddDeviceActivity;
import com.revolo.lock.ui.device.lock.DeviceDetailActivity;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
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
    private final String WIFI_SHOW_BEAN_LIST = "WifiShowBeanList";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mDeviceViewModel =
                new ViewModelProvider(this).get(DeviceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_device, container, false);
        mClNoDevice = root.findViewById(R.id.clNoDevice);
        mClHadDevice = root.findViewById(R.id.clHadDevice);
        mDeviceViewModel.getWifiShowBeans().observe(getViewLifecycleOwner(), this::updateData);
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
            mHomeLockListAdapter.setOnItemChildClickListener((adapter, view, position) -> {
                if(view.getId() == R.id.ivLockState) {
                    // TODO: 2021/2/6 要选择来切换发送对应的指令
                    // 发送查询状态
//                    App.getInstance().writeControlMsg(BleCommandFactory.checkLockBaseInfoCommand(mBleBean.getPwd1(), mBleBean.getPwd3()));
//                    // TODO: 2021/2/7 看看是否接收了再发指令还是如何处理
//                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                        // 蓝牙发送开关门指令
//                        App.getInstance().writeControlMsg(BleCommandFactory
//                                .lockControlCommand((byte) 0x00, (byte) 0x04, (byte) 0x01, mBleBean.getPwd1(), mBleBean.getPwd3()));
//                    }, 100);

                        publishOpenOrCloseDoor(mHomeLockListAdapter.getItem(position).getWifiListBean().getWifiSN(), 1);
                }
            });
            rvLockList.setAdapter(mHomeLockListAdapter);
            if(getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).setStatusBarColor(R.color.white);
            }
        }
        initBleListener();
        initData();
        initDataFromCache();
        return root;
    }

    private void updateData(List<WifiShowBean> wifiShowBeans) {
        if(wifiShowBeans != null) {
            if(wifiShowBeans.isEmpty()) {
                mClNoDevice.setVisibility(View.VISIBLE);
                mClHadDevice.setVisibility(View.GONE);
            } else {
                mClNoDevice.setVisibility(View.GONE);
                mClHadDevice.setVisibility(View.VISIBLE);
            }
            String json = GsonUtils.toJson(wifiShowBeans);
            Timber.d("updateData json: %1s", json);
            App.getInstance().getCacheDiskUtils().put(WIFI_SHOW_BEAN_LIST, json);
            mHomeLockListAdapter.setList(wifiShowBeans);
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
            isAuth = true;
            App.getInstance().setAutoAuth(true);
            // TODO: 2021/1/26 鉴权成功
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
    }

    private void syNowTime() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            long nowTime = TimeUtils.getNowMills()/1000;
            App.getInstance().writeControlMsg(BleCommandFactory
                    .syLockTime(nowTime, mPwd1, mPwd3));
        }, 20);
    }

    private boolean isAuth = false;

    private void initBleListener() {
        App.getInstance().setOnBleDeviceListener(new OnBleDeviceListener() {
            @Override
            public void onConnected() {
                if(isAuth) {
                    return;
                }
                Timber.d("initBleListener 连接成功 发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2));
                mBleBean.setPwd1(mPwd1);
                mBleBean.setPwd2(mPwd2);
                mBleBean.setEsn(mEsn);
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

    // TODO: 2021/2/10 后续需要修改,String数据量大会存在问题，后续替换成数据库缓存
    private void initDataFromCache() {
        String json = App.getInstance().getCacheDiskUtils().getString(WIFI_SHOW_BEAN_LIST);
        if(TextUtils.isEmpty(json)) {
            return;
        }
        Timber.d("initDataFromCache Json: %1s", json);
        Type type = new TypeToken<List<WifiShowBean>>(){}.getType();
        ArrayList<WifiShowBean> wifiShowBeans = GsonUtils.fromJson(json, type);
        if(wifiShowBeans == null || wifiShowBeans.isEmpty()) {
            return;
        }
        updateData(wifiShowBeans);
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

}