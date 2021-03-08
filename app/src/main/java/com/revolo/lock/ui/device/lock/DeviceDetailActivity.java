package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockDoorOptResponseBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.setting.DeviceSettingActivity;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.ble.BleCommandState.LOCK_SETTING_CLOSE;
import static com.revolo.lock.ble.BleCommandState.LOCK_SETTING_OPEN;

/**
 * author : Jack
 * time   : 2021/1/12
 * E-mail : wengmaowei@kaadas.com
 * desc   : 设备详情页面
 */
public class DeviceDetailActivity extends BaseActivity {

    private BleDeviceLocal mBleDeviceLocal;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.LOCK_DETAIL)) {
            mBleDeviceLocal = intent.getParcelableExtra(Constant.LOCK_DETAIL);
        }
        if(mBleDeviceLocal == null) {
            // TODO: 2021/3/1 处理
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_device_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar("Homepage");
        initDevice();

    }

    @Override
    public void doBusiness() {
        if(mBleDeviceLocal.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if(bleBean != null) {
                bleBean.setOnBleDeviceListener(mOnBleDeviceListener);
            }
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.llNotification) {
            Intent intent = new Intent(this, OperationRecordsActivity.class);
            intent.putExtra(Constant.DEVICE_ID, mBleDeviceLocal.getId());
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.llPwd) {
            Intent intent = new Intent(this, PasswordListActivity.class);
            intent.putExtra(Constant.DEVICE_ID, mBleDeviceLocal.getId());
            intent.putExtra(Constant.LOCK_ESN, mBleDeviceLocal.getEsn());
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.llUser) {
            Intent intent = new Intent(this, UserManagementActivity.class);
            intent.putExtra(Constant.LOCK_DETAIL, mBleDeviceLocal);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.llSetting) {
            gotoDeviceSettingAct();
            return;
        }
        if(view.getId() == R.id.ivLockState) {
            openDoor();
        }
    }

    private void gotoDeviceSettingAct() {
        if(App.getInstance().getUserBean() == null) {
            Timber.e("gotoDeviceSettingAct App.getInstance().getUserBean() == null");
            return;
        }
        Intent intent = new Intent(this, DeviceSettingActivity.class);
        DeviceUnbindBeanReq req = new DeviceUnbindBeanReq();
        req.setUid(App.getInstance().getUserBean().getUid());
        req.setWifiSN(mBleDeviceLocal.getEsn());
        intent.putExtra(Constant.UNBIND_REQ, req);
        intent.putExtra(Constant.LOCK_DETAIL, mBleDeviceLocal);
        startActivity(intent);
    }

    private OnBleDeviceListener mOnBleDeviceListener = new OnBleDeviceListener() {
        @Override
        public void onConnected(@NotNull String mac) {

        }

        @Override
        public void onDisconnected(@NotNull String mac) {

        }

        @Override
        public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
            if(value == null) {
                Timber.e("initBleListener value == null");
                return;
            }
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if(bleBean == null) {
                Timber.e("initBleListener bleBean == null");
                return;
            }
            if(bleBean.getOKBLEDeviceImp() == null) {
                Timber.e("initBleListener bleBean.getOKBLEDeviceImp() == null");
                return;
            }
            if(bleBean.getPwd1() == null) {
                Timber.e("initBleListener bleBean.getPwd1() == null");
                return;
            }
            if(bleBean.getPwd3() == null) {
                Timber.e("initBleListener bleBean.getPwd3() == null");
                return;
            }
            BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
            BleResultProcess.processReceivedData(value, bleBean.getPwd1(), bleBean.getPwd3(),
                    bleBean.getOKBLEDeviceImp().getBleScanResult());
        }

        @Override
        public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

        }

        @Override
        public void onAuthSuc(@NotNull String mac) {

        }

    };

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
        runOnUiThread(() -> {
            if(bean.getPayload()[0] == 0x00) {
                // 上锁
                refreshLockState();
                initDevice();
            }
        });

    }

    private void refreshLockState() {
        @LocalState.LockState int state = mBleDeviceLocal.getLockState();
        if(state == LocalState.LOCK_STATE_OPEN) {
            state = LocalState.LOCK_STATE_CLOSE;
        } else if(state == LocalState.LOCK_STATE_CLOSE) {
            state = LocalState.LOCK_STATE_OPEN;
        }
        mBleDeviceLocal.setLockState(state);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    private void setLockState(@LocalState.LockState int state) {
        mBleDeviceLocal.setLockState(state);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
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
        runOnUiThread(() -> {
            if(eventType == 0x01) {
                if(eventSource == 0x01) {
                    // 上锁
                    setLockState(LocalState.LOCK_STATE_CLOSE);
                    initDevice();
                } else if(eventCode == 0x02) {
                    // 开锁
                    setLockState(LocalState.LOCK_STATE_OPEN);
                    initDevice();
                } else {
                    // TODO: 2021/2/10 其他处理
                }
            }
        });
    }


    private void initDevice() {
        ImageView ivLockState = findViewById(R.id.ivLockState);
        ImageView ivNetState = findViewById(R.id.ivNetState);
        ImageView ivDoorState = findViewById(R.id.ivDoorState);
        TextView tvNetState = findViewById(R.id.tvNetState);
        TextView tvDoorState = findViewById(R.id.tvDoorState);
        LinearLayout llLowBattery = findViewById(R.id.llLowBattery);
        LinearLayout llNotification = findViewById(R.id.llNotification);
        LinearLayout llPwd = findViewById(R.id.llPwd);
        LinearLayout llUser = findViewById(R.id.llUser);
        LinearLayout llSetting = findViewById(R.id.llSetting);
        LinearLayout llDoorState = findViewById(R.id.llDoorState);
        TextView tvPrivateMode = findViewById(R.id.tvPrivateMode);

        applyDebouncingClickListener(llNotification, llPwd, llUser, llSetting, ivLockState);

        if(mBleDeviceLocal.getLockState() == LocalState.LOCK_STATE_PRIVATE) {
            ivLockState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_img_lock_privacymodel));
            tvPrivateMode.setVisibility(View.VISIBLE);
            llDoorState.setVisibility(View.GONE);
        } else {
            tvPrivateMode.setVisibility(View.GONE);
            llDoorState.setVisibility(View.VISIBLE);
            if(mBleDeviceLocal.getLockState() == LocalState.LOCK_STATE_OPEN) {
                ivLockState.setImageResource(R.drawable.ic_home_img_lock_open);
                ivDoorState.setImageResource(R.drawable.ic_home_icon_door_open);
                tvDoorState.setText(R.string.tip_opened);
            } else if(mBleDeviceLocal.getLockState() == LocalState.LOCK_STATE_CLOSE) {
                ivLockState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_img_lock_close));
                ivDoorState.setImageResource(R.drawable.ic_home_icon_door_closed);
                tvDoorState.setText(R.string.tip_closed);
            } else {

            }
        }
        if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            ivNetState.setImageResource(R.drawable.ic_home_icon_wifi);
        } else if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            ivNetState.setImageResource(R.drawable.ic_home_icon_bluetooth);
        } else {
            // TODO: 2021/3/2 其他选择
        }
        tvNetState.setText(getString(R.string.tip_online));

    }

    private void openDoor() {
        @LocalState.LockState int state = mBleDeviceLocal.getLockState();
        if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            publishOpenOrCloseDoor(
                    mBleDeviceLocal.getEsn(),
                    state==LocalState.LOCK_STATE_OPEN?LocalState.DOOR_STATE_CLOSE:LocalState.DOOR_STATE_OPEN,
                    mBleDeviceLocal.getRandomCode());
        } else {
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if(bleBean == null) {
                Timber.e("openDoor bleBean == null");
                return;
            }
            if(bleBean.getPwd1() == null) {
                Timber.e("openDoor bleBean.getPwd1() == null");
                return;
            }
            if(bleBean.getPwd3() == null) {
                Timber.e("openDoor bleBean.getPwd3() == null");
                return;
            }
            if(bleBean.getOKBLEDeviceImp() == null) {
                Timber.e("openDoor bleBean.getOKBLEDeviceImp() == null");
                return;
            }
            App.getInstance().writeControlMsg(BleCommandFactory
                    .lockControlCommand((byte) (mBleDeviceLocal.getLockState()==LocalState.LOCK_STATE_OPEN?LOCK_SETTING_CLOSE:LOCK_SETTING_OPEN),
                            (byte) 0x04, (byte) 0x01, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
        }
    }

    /**
     *  开关门
     * @param wifiId wifi的id
     * @param doorOpt 1:表示开门，0表示关门
     */
    public void publishOpenOrCloseDoor(String wifiId, @LocalState.DoorState int doorOpt, String randomCode) {
        if(App.getInstance().getUserBean() == null) {
            Timber.e("publishOpenOrCloseDoor App.getInstance().getUserBean() == null");
            return;
        }
        if(doorOpt == LocalState.DOOR_STATE_OPEN) {
            showLoading("Lock Opening...");
        } else if(doorOpt == LocalState.DOOR_STATE_CLOSE) {
            showLoading("Lock Closing...");
        }
        App.getInstance().getMqttService().mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLock(wifiId,
                        doorOpt,
                        BleCommandFactory.getPwd(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()), ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2())),
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
