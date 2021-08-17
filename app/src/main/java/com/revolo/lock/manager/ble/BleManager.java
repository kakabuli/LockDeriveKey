package com.revolo.lock.manager.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import com.a1anwang.okble.client.core.OKBLEDeviceImp;
import com.a1anwang.okble.client.core.OKBLEDeviceListener;
import com.a1anwang.okble.client.core.OKBLEOperation;
import com.a1anwang.okble.client.scan.BLEScanResult;
import com.a1anwang.okble.client.scan.DeviceScanCallBack;
import com.a1anwang.okble.client.scan.OKBLEScanManager;
import com.blankj.utilcode.util.ConvertUtils;
import com.revolo.lock.App;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.manager.LockMessageReplyErrCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.util.ZoneUtil;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_ENCRYPT_KEY_UPLOAD;
import static com.revolo.lock.ble.BleResultProcess.CONTROL_ENCRYPTION;
import static com.revolo.lock.ble.BleResultProcess.checksum;
import static com.revolo.lock.ble.BleResultProcess.pwdDecrypt;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_BLE;

public class BleManager {
    private final ArrayList<BleBean> mConnectedBleBeanList = new ArrayList<>();
    private static final int DEFAULT_CONNECTED_CAPACITY = 7;
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
    public void connectDevice(String sn, BLEScanResult bleScanResult, BluetoothDevice device, byte[] pwd1, byte[] pwd2, boolean isAppPair) {
        String mac = "";
        if (null == bleScanResult && null != device) {
            mac = device.getAddress().toUpperCase();
        } else {
            mac = bleScanResult.getMacAddress().toUpperCase();
        }
        if (!checkConnectedBle(mac)) {
            Timber.e("当前设备设备正在连接或是已连接：%s", mac);
            return;
        }

        OKBLEDeviceImp deviceImp = null;
        if (null == bleScanResult && null != device) {
            Timber.e("connectDevice: %1s", "device:" + device.getAddress());
            deviceImp = new OKBLEDeviceImp(App.getInstance().getApplicationContext());
            deviceImp.setBluetoothDevice(device);
            mac = device.getAddress().toUpperCase();
            deviceImp.setDeviceTAG(device.getAddress().toLowerCase());
        } else {
            Timber.e("connectDevice: %1s", "deviceImp:" + bleScanResult.getMacAddress());
            deviceImp = new OKBLEDeviceImp(App.getInstance().getApplicationContext(), bleScanResult);
            mac = bleScanResult.getMacAddress().toUpperCase();
            deviceImp.setDeviceTAG(bleScanResult.getMacAddress().toLowerCase());
        }
        BleBean bleBean = new BleBean(deviceImp);
        bleBean.setPwd1(pwd1);
        if (null != pwd2) {
            bleBean.setHavePwd2Or3(true);
        }
        bleBean.setMac(mac);
        bleBean.setPwd2(pwd2);
        bleBean.setEsn(sn);
        bleBean.setOnBleDeviceListener(onBleDeviceListener);
        bleBean.setAppPair(isAppPair);
        bleBean.setAuth(false);
        deviceImp.addDeviceListener(okbleDeviceListener);
        if (addConnectedBleBean(bleBean)) {
            // 自动重连
            if (null != onBleDeviceListener) {
                onBleDeviceListener.onAddConnect(mac.toUpperCase());
            }
            bleBean.setBleConning(1);
            deviceImp.connect(false);
        }
    }

    /**
     * 添加到设备列表中
     *
     * @param bleBean
     * @return
     */
    private boolean addConnectedBleBean(BleBean bleBean) {
        for (BleBean conDe : mConnectedBleBeanList) {
            if (conDe.getMac().equals(bleBean.getMac())) {
                //重复设备  未连接或是断开中  就连接
                if (conDe.getBleConning() == 0 || conDe.getBleConning() == 3) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        if (mConnectedBleBeanList.size() == DEFAULT_CONNECTED_CAPACITY) {
            return false;
        }
        mConnectedBleBeanList.add(bleBean);
        return true;
    }

    private boolean checkConnectedBle(String mac) {
        for (BleBean conDe : mConnectedBleBeanList) {
            if (conDe.getMac().equals(mac)) {
                //重复设备  未连接或是断开中  就连接
                if (conDe.getBleConning() == 0 || conDe.getBleConning() == 3) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        if (mConnectedBleBeanList.size() == DEFAULT_CONNECTED_CAPACITY) {
            return false;
        }
        return true;
    }


        /*if (mConnectedBleBeanList.size() == DEFAULT_CONNECTED_CAPACITY) {



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
    }*/

   /* public void removeConnectedBleBeanAndDisconnect(@NotNull BleBean bean) {
        Timber.d("removeConnectedBleBeanAndDisconnect device: %1s", bean.getMac());
        if (mConnectedBleBeanList.isEmpty()) {
            return;
        }
        if (null == mConnectedBleBeanList) {
            return;
        }
        for (int i = 0; i < mConnectedBleBeanList.size(); i++) {
            if (mConnectedBleBeanList.get(i).getEsn().equals(bean.getEsn())) {
                mConnectedBleBeanList.remove(i);
            }
        }
        if (bean.getOKBLEDeviceImp() != null) {
            bean.getOKBLEDeviceImp().disConnect(false);
        }

        Timber.d("removeConnectedBleBeanAndDisconnect device: %1s", mConnectedBleBeanList.size() + "");
    }*/

    /**
     * 主动断开ble
     *
     * @param mac
     */
    public void removeConnectDevice(String mac) {
        Timber.e("准备主动断开ble:" + mac);
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
                if (removeBle.getOKBLEDeviceImp() != null) {
                    Timber.e("主动断开ble:" + mac);
                    removeBle.getOKBLEDeviceImp().disConnect(false);
                } else {
                    clearConnetctList(removeBle);
                    Timber.e("准备主动断开异常2ble:" + mac);
                }
            } else {
                Timber.e("准备主动断开异常ble:" + mac);
            }
        }
    }

    /**
     * 清理列表中的的设备
     *
     * @param bean
     */
    public void clearConnetctList(BleBean bean) {
        Timber.d("removeConnectedBleBeanAndDisconnect device: %1s", bean.getMac());
        if (null == bean) {
            Timber.e("clear bean=null");
            return;
        }
        if (mConnectedBleBeanList.isEmpty()) {
            return;
        }
        if (null == mConnectedBleBeanList) {
            return;
        }
        for (int i = 0; i < mConnectedBleBeanList.size(); i++) {
            if (mConnectedBleBeanList.get(i).getEsn().equals(bean.getEsn())) {
                mConnectedBleBeanList.remove(i);
            }
        }
    }

    /**
     * 断开所有设备的连接
     */
   /* public void disConnect() {
        if (null != mConnectedBleBeanList) {
            for (int i = 0; i < mConnectedBleBeanList.size(); i++) {
                if (null != mConnectedBleBeanList.get(i).getOKBLEDeviceImp()) {
                    removeConnectedBleBeanAndDisconnect(mConnectedBleBeanList.get(i));
                }
            }
        }
    }*/
    public void removeBleConnected() {
        if (null != mConnectedBleBeanList) {
            for (int i = 0; i < mConnectedBleBeanList.size(); i++) {
                if (null != mConnectedBleBeanList.get(i).getOKBLEDeviceImp()) {
                    mConnectedBleBeanList.get(i).getOKBLEDeviceImp().disConnect(false);
                    Timber.d("removeBleConnected device: %1s", mConnectedBleBeanList.size() + "");
                }
            }
            mConnectedBleBeanList.clear();
        }
    }

    public List<BleBean> getBleBeans() {
        return mConnectedBleBeanList;
    }

    public BleBean getBleBeanFromMac(@NotNull String mac) {
        for (BleBean bleBean : mConnectedBleBeanList) {
            if (null == bleBean.getOKBLEDeviceImp() || null == bleBean.getOKBLEDeviceImp().getMacAddress()) {
                continue;
            }
            if (bleBean.getOKBLEDeviceImp().getMacAddress().equals(mac)) {
                return bleBean;
            }
        }
        return null;
    }

    /**
     * 鉴权是否初始化pwd2、pwd3；
     *
     * @param mac
     * @return
     */
    public BleBean setBleFromMacInitPwd(@NotNull String mac) {
        for (int i = 0; i < mConnectedBleBeanList.size(); i++) {
            if (null == mConnectedBleBeanList.get(i).getOKBLEDeviceImp() || null == mConnectedBleBeanList.get(i).getOKBLEDeviceImp().getMacAddress()) {
                continue;
            }
            if (mConnectedBleBeanList.get(i).getOKBLEDeviceImp().getMacAddress().equals(mac)) {
                mConnectedBleBeanList.get(i).setPwd3(null);
                mConnectedBleBeanList.get(i).setPwd2(null);
                mConnectedBleBeanList.get(i).setPwd2_copy(null);
                return mConnectedBleBeanList.get(i);
            }
        }
        return null;
    }

    public boolean getBleBeanCoonectedState(@NotNull String mac) {
        for (BleBean bleBean : mConnectedBleBeanList) {
            if (null == bleBean.getOKBLEDeviceImp() || null == bleBean.getOKBLEDeviceImp().getMacAddress()) {
                continue;
            }
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
                bleBean.setBleConning(2);
                openControlNotify(bleBean.getOKBLEDeviceImp());
                if (bleBean.isAppPair() && null != bleBean.getPwd2() && !"00000000".equals(ConvertUtils.bytes2HexString(bleBean.getPwd2()))) {
                    // 正在蓝牙本地配网，所以不走自动鉴权
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Timber.d("getPwd2AndSendAuthCommand 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(bleBean.getPwd2()));
                        writeControlMsg(BleCommandFactory
                                .authCommand(bleBean.getPwd1(), bleBean.getPwd2(), bleBean.getEsn().getBytes(StandardCharsets.UTF_8)), bleBean.getOKBLEDeviceImp());
                    }, 100);

                    bleConnectedCallback(bleBean, bleBean.getOKBLEDeviceImp().getMacAddress());
                    return;
                } else {
                    if ("00000000".equals(ConvertUtils.bytes2HexString(bleBean.getPwd2()))) {
                        setBleFromMacInitPwd(bleBean.getMac());
                        bleBean.setPwd2_copy(null);
                        bleBean.setPwd2(null);
                        bleBean.setPwd3(null);
                    }
                }
                // 连接后都走自动鉴权流程
                bleBean.setAuth(true);
                //发送配网指令，并校验ESN
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (null != bleBean.getOKBLEDeviceImp()) {
                        Timber.d("%1s 发送配网指令，并校验ESN", bleBean.getOKBLEDeviceImp());
                    }
                    writeControlMsg(BleCommandFactory
                            .pairCommand(bleBean.getPwd1(), bleBean.getEsn().getBytes(StandardCharsets.UTF_8)), bleBean.getOKBLEDeviceImp());
                }, 100);
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
                bleBean.setBleConning(3);
                if (bleBean.getOnBleDeviceListener() == null) {
                    Timber.e("onDisconnected bleBean.getOnBleDeviceListener() == null");
                    return;
                }
                //removeConnectedBleBeanAndDisconnect(deviceTAG);//清理当前队列中的连接ble对象
                clearConnetctList(bleBean);
                bleBean.getOnBleDeviceListener().onDisconnected(bleBean.getOKBLEDeviceImp().getMacAddress());

                LockMessageRes message = new LockMessageRes();
                message.setMessgaeType(MSG_LOCK_MESSAGE_BLE);//蓝牙消息
                message.setResultCode(LockMessageReplyErrCode.LOCK_BLE_ERR_CODE_BLE_DIS_ERR);//校验和失败
                EventBus.getDefault().post(message);
                if (null != bleBean.getOKBLEDeviceImp()) {
                    bleBean.getOKBLEDeviceImp().remove();
                }
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
            LockMessageRes message = new LockMessageRes();
            message.setMessgaeType(MSG_LOCK_MESSAGE_BLE);//蓝牙消息
            message.setResultCode(LockMessageReplyErrCode.LOCK_BLE_ERR_CODE_DATA_CHECK_ERR);//校验和失败
            EventBus.getDefault().post(message);
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
            // syNowTime(bleBean);
        }
    }

    /*private void authProcess(byte[] value, @NotNull BleBean bleBean, @NotNull String mac) {
        if (value[0] == 0x01) {
            byte[] mPwd2 = new byte[4];
            System.arraycopy(value, 1, mPwd2, 0, mPwd2.length);
            // TODO: 2021/1/21 打包数据上传到服务器后再发送确认指令
            bleBean.setPwd2_copy(mPwd2);
            Timber.e("getPwd2AndSendAuthCommand 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2) + ";;;;;;SN:" + bleBean.getEsn());
          *//* new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Timber.d("getPwd2AndSendAuthCommand 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2));
                writeControlMsg(BleCommandFactory
                        .authCommand(bleBean.getPwd1(), bleBean.getPwd2(), bleBean.getEsn().getBytes(StandardCharsets.UTF_8)), bleBean.getOKBLEDeviceImp());
            }, 50);*//*
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
*/
    private void getPwd3(byte[] bytes, byte[] decryptPayload, @NotNull BleBean bleBean) {
        byte[] pwd3 = new byte[4];
        System.arraycopy(decryptPayload, 1, pwd3, 0, pwd3.length);
        Timber.d("鉴权成功, pwd3: %1s\n", ConvertUtils.bytes2HexString(pwd3));
        bleBean.getOnBleDeviceListener().onConnected(bleBean.getOKBLEDeviceImp().getMacAddress());
        // 内存存储
        //bleBean.setPwd3(pwd3);
        writeControlMsg(bytes, bleBean.getOKBLEDeviceImp());

    }

    private void syNowTime(@NotNull BleBean bleBean) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            long nowTime = ZoneUtil.getTime() / 1000;
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
            Timber.e("onFail errMsg: %1s", (errMsg + ";code:" + code));
            LockMessageRes message = new LockMessageRes();
            message.setMessgaeType(MSG_LOCK_MESSAGE_BLE);//蓝牙消息
            message.setResultCode(LockMessageReplyErrCode.LOCK_BLE_ERR_CODE_DATA_WRITE_ERR);//校验和失败
            EventBus.getDefault().post(message);
        }

        @Override
        public void onExecuteSuccess(OKBLEOperation.OperationType type) {

        }
    };

    public void write(int writeType, String mac, byte[] bytes) {
        BleBean bleBean = getBleBeanFromMac(mac);
        if (null != bleBean && null != bleBean.getOKBLEDeviceImp()) {
            if (writeType == 0) {
                writeControlMsg(bytes, bleBean.getOKBLEDeviceImp());
            } else {
                writePairMsg(bytes, bleBean.getOKBLEDeviceImp());
            }
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
    //....

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
                            LockMessageRes message = new LockMessageRes();
                            message.setMessgaeType(MSG_LOCK_MESSAGE_BLE);//蓝牙消息
                            message.setResultCode(LockMessageReplyErrCode.LOCK_BLE_ERR_CODE_DATA_NOTIFY_ERR);//校验和失败
                            EventBus.getDefault().post(message);
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
