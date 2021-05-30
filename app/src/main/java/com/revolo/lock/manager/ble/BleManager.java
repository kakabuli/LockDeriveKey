package com.revolo.lock.manager.ble;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.a1anwang.okble.client.core.OKBLEDeviceImp;
import com.a1anwang.okble.client.core.OKBLEDeviceListener;
import com.a1anwang.okble.client.core.OKBLEOperation;
import com.a1anwang.okble.client.scan.BLEScanResult;
import com.a1anwang.okble.client.scan.DeviceScanCallBack;
import com.a1anwang.okble.client.scan.OKBLEScanManager;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.revolo.lock.App;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_ENCRYPT_KEY_UPLOAD;
import static com.revolo.lock.ble.BleResultProcess.CONTROL_ENCRYPTION;
import static com.revolo.lock.ble.BleResultProcess.checksum;
import static com.revolo.lock.ble.BleResultProcess.pwdDecrypt;

public class BleManager {
    private final ArrayList<BleBean> mConnectedBleBeanList = new ArrayList<>();
    private static final int DEFAULT_CONNECTED_CAPACITY = 3;
    private static BleManager bleManager;
    private OnBleDeviceListener onBleDeviceListener;

    public static BleManager getInstance() {
        if (null == bleManager) {
            bleManager = new BleManager();
        }
        return bleManager;
    }

    public void setOnBleDeviceListener(OnBleDeviceListener onBleDeviceListener) {
        this.onBleDeviceListener = onBleDeviceListener;
    }

    /*-------------------------------- 蓝牙搜索 --------------------------------*/
    private OKBLEScanManager mScanManager;

    public OKBLEScanManager getScanManager() {
        if (mScanManager == null) {
            mScanManager = new OKBLEScanManager(App.getInstance().getApplicationContext());
        }
        return mScanManager;
    }

    public void setScanCallBack(DeviceScanCallBack scanCallBack) {
        mScanManager.setScanCallBack(scanCallBack);
        mScanManager.setScanDuration(20 * 1000);
    }

    /*-------------------------------- 蓝牙搜索 end --------------------------------*/
    public BleBean connectDevice(String sn, BLEScanResult bleScanResult, byte[] pwd1, byte[] pwd2, boolean isAppPair) {
        OKBLEDeviceImp deviceImp = new OKBLEDeviceImp(App.getInstance().getApplicationContext(), bleScanResult);
        Log.e("test_ble:", bleScanResult.getMacAddress());
        deviceImp.setDeviceTAG(bleScanResult.getMacAddress().toLowerCase());
        BleBean bleBean = new BleBean(deviceImp);
        bleBean.setPwd1(pwd1);
        bleBean.setPwd2(pwd2);
        bleBean.setEsn(sn);
        bleBean.setOnBleDeviceListener(onBleDeviceListener);
        bleBean.setAppPair(isAppPair);
        bleBean.setAuth(false);
        addConnectedBleBean(bleBean);
        deviceImp.addDeviceListener(okbleDeviceListener);
        // 自动重连
        deviceImp.connect(true);
        return bleBean;
    }

    private void addConnectedBleBean(BleBean bleBean) {
        if (mConnectedBleBeanList.size() == DEFAULT_CONNECTED_CAPACITY) {
            Timber.d("连接的蓝牙设备数量： %1d", mConnectedBleBeanList.size());
            for (BleBean ble : mConnectedBleBeanList) {
                Timber.d("已连接的蓝牙设备mac：%1s, esn: %2s",
                        ble.getOKBLEDeviceImp().getMacAddress(), ble.getEsn());
            }
            BleBean willRemoveBleBean = mConnectedBleBeanList.get(0);
            willRemoveBleBean.getOKBLEDeviceImp().disConnect(false);
            mConnectedBleBeanList.remove(0);
        }
        mConnectedBleBeanList.add(bleBean);
    }

    public void removeConnectedBleBeanAndDisconnect(@NotNull BleBean bean) {
        if (mConnectedBleBeanList.isEmpty()) {
            return;
        }
        if (bean.getOKBLEDeviceImp() != null) {
            bean.getOKBLEDeviceImp().disConnect(false);
        }
        for (BleBean bleBean : mConnectedBleBeanList) {
            if (bleBean.getEsn().equals(bean.getEsn())) {
                mConnectedBleBeanList.remove(bleBean);
            }
        }
    }

    public void removeConnectedBleBeanAndDisconnect(String mac) {
        if (null != mConnectedBleBeanList) {
            BleBean removeBle = null;
            for (BleBean bleBean : mConnectedBleBeanList) {
                if (null != bleBean.getOKBLEDeviceImp()) {
                    if (mac.equals(bleBean.getOKBLEDeviceImp().getMacAddress())) {
                        removeBle = bleBean;
                        break;
                    }
                }
            }
            if (null != removeBle) {
                removeConnectedBleBeanAndDisconnect(removeBle);
            }
        }
    }

    public List<BleBean> getBleBeans() {
        return mConnectedBleBeanList;
    }

    public BleBean getBleBeanFromMac(@NotNull String mac) {
        for (BleBean bleBean : mConnectedBleBeanList) {
            if (bleBean.getOKBLEDeviceImp().getMacAddress().equals(mac)) {
                return bleBean;
            }
        }
        return null;
    }

    public boolean getBleBeanCoonectedState(@NotNull String mac) {
        for (BleBean bleBean : mConnectedBleBeanList) {
            if (bleBean.getOKBLEDeviceImp().getMacAddress().equals(mac)) {
                return bleBean.getOKBLEDeviceImp().isConnected();
            }
        }
        return false;
    }


    OKBLEDeviceListener okbleDeviceListener = new OKBLEDeviceListener() {
        @Override
        public void onConnected(String deviceTAG) {
            if (null != deviceTAG)
                deviceTAG = deviceTAG.toUpperCase();
            Timber.d("onConnected deviceTAG: %1s", deviceTAG);
            BleBean bleBean = getBleBeanFromMac(deviceTAG);
            if (null != bleBean) {
                openControlNotify(bleBean.getOKBLEDeviceImp());
                if (bleBean.isAppPair()) {
                    // 正在蓝牙本地配网，所以不走自动鉴权
                    bleConnectedCallback(bleBean, bleBean.getOKBLEDeviceImp().getMacAddress());
                    return;
                }
                // 连接后都走自动鉴权流程
                bleBean.setAuth(true);
                if (null == bleBean.getPwd2()) {
                    //发送配网指令，并校验ESN
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (null != bleBean.getOKBLEDeviceImp()) {
                            Timber.d("%1s 发送配网指令，并校验ESN", bleBean.getOKBLEDeviceImp());
                        }
                        writeControlMsg(BleCommandFactory
                                .pairCommand(bleBean.getPwd1(), bleBean.getEsn().getBytes(StandardCharsets.UTF_8)), bleBean.getOKBLEDeviceImp());
                    }, 100);
                } else {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> writeControlMsg(BleCommandFactory
                                    .authCommand(bleBean.getPwd1(), bleBean.getPwd2(), bleBean.getEsn().getBytes(StandardCharsets.UTF_8)),
                            bleBean.getOKBLEDeviceImp()), 50);
                }
                bleConnectedCallback(bleBean, bleBean.getOKBLEDeviceImp().getMacAddress());
            }
        }

        @Override
        public void onDisconnected(String deviceTAG) {
            if (null != deviceTAG)
                deviceTAG = deviceTAG.toUpperCase();
            Timber.d("onDisconnected deviceTAG: %1s", deviceTAG);
            BleBean bleBean = getBleBeanFromMac(deviceTAG);
            if (null != bleBean) {
                if (bleBean.getOnBleDeviceListener() == null) {
                    Timber.e("onDisconnected bleBean.getOnBleDeviceListener() == null");
                    return;
                }
                bleBean.getOKBLEDeviceImp().remove();
                removeConnectedBleBeanAndDisconnect(deviceTAG);//清理当前队列中的连接ble对象
                bleBean.getOnBleDeviceListener().onDisconnected(bleBean.getOKBLEDeviceImp().getMacAddress());
            }
        }

        @Override
        public void onReadBattery(String deviceTAG, int battery) {
            if (null != deviceTAG)
                deviceTAG = deviceTAG.toUpperCase();
        }

        @Override
        public void onReceivedValue(String deviceTAG, String uuid, byte[] value) {
            if (null != deviceTAG)
                deviceTAG = deviceTAG.toUpperCase();
            Timber.d("onReceivedValue value: %1s", ConvertUtils.bytes2HexString(value));
            BleBean bleBean = getBleBeanFromMac(deviceTAG);
            if (null != bleBean) {
                int cmd = BleByteUtil.byteToInt(value[3]);
                if (cmd == CMD_ENCRYPT_KEY_UPLOAD) {
                    authProcess(value, bleBean, bleBean.getOKBLEDeviceImp().getMacAddress());
                }
                if (bleBean.getOnBleDeviceListener() == null) {
                    Timber.e("mOnBleDeviceListener == null");
                    return;
                }
                bleBean.getOnBleDeviceListener().onReceivedValue(bleBean.getOKBLEDeviceImp().getMacAddress(), uuid, value);
            }
        }


        @Override
        public void onWriteValue(String deviceTAG, String uuid, byte[] value, boolean success) {
            if (null != deviceTAG)
                deviceTAG = deviceTAG.toUpperCase();
            Timber.d("onWriteValue uuid: %1s, value: %2s, success: %3b",
                    uuid, ConvertUtils.bytes2HexString(value), success);
            BleBean bleBean = getBleBeanFromMac(deviceTAG);
            if (null != bleBean) {
                if (bleBean.getOnBleDeviceListener() == null) {
                    Timber.e("mOnBleDeviceListener == null");
                    return;
                }
                bleBean.getOnBleDeviceListener().onWriteValue(bleBean.getOKBLEDeviceImp().getMacAddress(), uuid, value, success);
            }
        }

        @Override
        public void onReadValue(String deviceTAG, String uuid, byte[] value, boolean success) {
            if (null != deviceTAG)
                deviceTAG = deviceTAG.toUpperCase();
        }

        @Override
        public void onNotifyOrIndicateComplete(String deviceTAG, String uuid, boolean enable, boolean success) {
            if (null != deviceTAG)
                deviceTAG = deviceTAG.toUpperCase();
        }
    };

    private void bleConnectedCallback(@NotNull BleBean bleBean, @NotNull String mac) {
        if (bleBean.getOnBleDeviceListener() == null) {
            Timber.e("mOnBleDeviceListener == null");
            return;
        }
        bleBean.getOnBleDeviceListener().onConnected(mac);
    }

    private void authProcess(byte[] value, @NotNull BleBean bleBean, @NotNull String mac) {
        if (value[0] == 0x01) {
            byte[] mPwd2 = new byte[4];
            System.arraycopy(value, 1, mPwd2, 0, mPwd2.length);
            // TODO: 2021/1/21 打包数据上传到服务器后再发送确认指令
            bleBean.setPwd2_copy(mPwd2);
            Timber.e("getPwd2AndSendAuthCommand 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2) + ";;;;;;SN:" + bleBean.getEsn());
          /* new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Timber.d("getPwd2AndSendAuthCommand 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2));
                writeControlMsg(BleCommandFactory
                        .authCommand(bleBean.getPwd1(), bleBean.getPwd2(), bleBean.getEsn().getBytes(StandardCharsets.UTF_8)), bleBean.getOKBLEDeviceImp());
            }, 50);*/
        } else if (value[0] == 0x02) {
            bleBean.setPwd2(bleBean.getPwd2_copy());
            byte[] pwd1 = bleBean.getPwd1();
            byte[] pwd2Or3 = bleBean.isAuth() ? bleBean.getPwd2() : bleBean.getPwd3();
            boolean isEncrypt = (value[0] == CONTROL_ENCRYPTION);
            byte[] payload = new byte[16];
            System.arraycopy(value, 4, payload, 0, payload.length);
            byte[] decryptPayload = isEncrypt ? pwdDecrypt(payload, pwd1, pwd2Or3) : payload;
            byte sum = checksum(decryptPayload);
            if (value[2] != sum) {
                Timber.d("authProcess 校验和失败，接收数据中的校验和：%1s，\n接收数据后计算的校验和：%2s",
                        ConvertUtils.int2HexString(value[2]), ConvertUtils.int2HexString(sum));
                return;
            }
            int cmd = BleByteUtil.byteToInt(value[3]);
            if (decryptPayload[0] == 0x02) {
                if (bleBean.getOnBleDeviceListener() != null) {
                    bleBean.getOnBleDeviceListener().onAuthSuc(mac);
                }
                // 获取pwd3
                getPwd3(BleCommandFactory.ackCommand(BleByteUtil.byteToInt(value[1]), (byte) 0x00, cmd), decryptPayload, bleBean);
                bleBean.setAuth(false);
                // 鉴权成功后，同步当前时间
                syNowTime(bleBean);
            }
        }
    }

    private void getPwd3(byte[] bytes, byte[] decryptPayload, @NotNull BleBean bleBean) {
        byte[] pwd3 = new byte[4];
        System.arraycopy(decryptPayload, 1, pwd3, 0, pwd3.length);
        Timber.d("鉴权成功, pwd3: %1s\n", ConvertUtils.bytes2HexString(pwd3));
        bleBean.getOnBleDeviceListener().onConnected(bleBean.getOKBLEDeviceImp().getMacAddress());
        // 内存存储
        bleBean.setPwd3(pwd3);
        writeControlMsg(bytes, bleBean.getOKBLEDeviceImp());

    }

    private void syNowTime(@NotNull BleBean bleBean) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            long nowTime = TimeUtils.getNowMills() / 1000;
            writeControlMsg(BleCommandFactory
                    .syLockTime(nowTime, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
        }, 20);
    }

    private static final String sControlWriteCharacteristicUUID = "FFE9";
    private static final String sControlNotifyCharacteristicUUID = "FFE4";

    private static final String sPairWriteCharacteristicUUID = "FFC1";
    private static final String sPairNotifyCharacteristicUUID = "FFC6";
    private final OKBLEOperation.WriteOperationListener mWriteOperationListener = new OKBLEOperation
            .WriteOperationListener() {
        @Override
        public void onWriteValue(byte[] value) {
        }

        @Override
        public void onFail(int code, String errMsg) {
            Timber.e("onFail errMsg: %1s", errMsg);
        }

        @Override
        public void onExecuteSuccess(OKBLEOperation.OperationType type) {

        }
    };

    public void write(String mac, byte[] bytes) {
        BleBean bleBean = getBleBeanFromMac(mac);
        if (null != bleBean && null != bleBean.getOKBLEDeviceImp()) {
            writeControlMsg(bytes, bleBean.getOKBLEDeviceImp());
        }

    }

    public void writeControlMsg(byte[] bytes, OKBLEDeviceImp deviceImp) {
        if (deviceImp != null) {
            deviceImp.addWriteOperation(sControlWriteCharacteristicUUID, bytes, mWriteOperationListener);
        }
    }

    public void writePairMsg(byte[] bytes, OKBLEDeviceImp deviceImp) {
        if (deviceImp != null) {
            deviceImp.addWriteOperation(sPairWriteCharacteristicUUID, bytes, mWriteOperationListener);
        }
    }

    private void openControlNotify(OKBLEDeviceImp deviceImp) {
        if (deviceImp != null) {
            deviceImp.addNotifyOrIndicateOperation(sControlNotifyCharacteristicUUID,
                    true, new OKBLEOperation.NotifyOrIndicateOperationListener() {
                        @Override
                        public void onNotifyOrIndicateComplete() {
                            Timber.d("openControlNotify onNotifyOrIndicateComplete 打开控制命令通知成功");
                        }

                        @Override
                        public void onFail(int code, String errMsg) {
                            Timber.e("openControlNotify onFail errMsg: %1s", errMsg);
                        }

                        @Override
                        public void onExecuteSuccess(OKBLEOperation.OperationType type) {

                        }
                    });
        }
    }

    public void openPairNotify(OKBLEDeviceImp deviceImp) {
        if (deviceImp != null) {
            deviceImp.addNotifyOrIndicateOperation(sPairNotifyCharacteristicUUID,
                    true, new OKBLEOperation.NotifyOrIndicateOperationListener() {
                        @Override
                        public void onNotifyOrIndicateComplete() {
                            Timber.d("openControlNotify onNotifyOrIndicateComplete 打开配网通知成功");
                        }

                        @Override
                        public void onFail(int code, String errMsg) {
                            Timber.e("openControlNotify onFail errMsg: %1s", errMsg);
                        }

                        @Override
                        public void onExecuteSuccess(OKBLEOperation.OperationType type) {

                        }
                    });
        }
    }

}
