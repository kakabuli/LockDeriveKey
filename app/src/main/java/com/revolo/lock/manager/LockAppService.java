package com.revolo.lock.manager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.revolo.lock.App;
import com.revolo.lock.LocalState;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.manager.ble.BleManager;
import com.revolo.lock.manager.ble.BleResultAnalysis;
import com.revolo.lock.manager.mqtt.MQTTDataLinstener;
import com.revolo.lock.manager.mqtt.MQTTErrCode;
import com.revolo.lock.manager.mqtt.MQTTManager;
import com.revolo.lock.manager.mqtt.MQTTReply;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.LocalState.DEVICE_CONNECT_TYPE_BLE;
import static com.revolo.lock.LocalState.DEVICE_CONNECT_TYPE_WIFI;

/**
 * 1、MQTT 通讯模块管理
 * 2、ble  通讯模块管理
 * 3、http 数据请求管理
 * 4、发送数据接口【MQTT、ble、http】情景
 * 5、数据回复接口
 * 6、数据重发、超时、等机制
 * 7、账号数据缓存
 * 8、当前即时通讯设备（焦点设备）
 * 9、当前即时设备 数据
 * 10、连接设备list（所有设备）
 * 11、公共数据缓存
 * 12、ota升级模块
 * 13、根据APP生命周期，初始化、更新、上传、同步等数据
 */
public class LockAppService extends Service {
    private static String TAG = "LockAppService";
    //MQTT 消息
    public static int LOCK_MESSAGE_MQTT = 2;
    //Ble 消息
    public static int LOCK_MESSAGE_BLE = 3;
    //设备列表
    private List<BleDeviceLocal> mDeviceLists = new ArrayList<>();
    //同步锁
    private Lock lock = new ReentrantLock();
    //5、数据回复接口
    //6、数据重发、超时、等机制
    //7、账号数据缓存
    //8、当前即时通讯设备（焦点设备）
    // 9、当前即时设备 数据
    //10、连接设备list（所有设备）
    //11、公共数据缓存
    // 12、ota升级模块 */
    //13、根据APP生命周期，初始化、更新、上传、同步等数据 */

    public class lockBinder extends Binder {
        public LockAppService getService() {
            return LockAppService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //服务创建时，初始化MQTT Ble
        initMode();
        //registerNetReceive();
        Timber.d("attachView   mqtt 启动了");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new lockBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //NotificationManager.silentForegroundNotification(this);//TODO:未知revolo是否要做通知，用FCM就不用自己做通知，先注释
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unRegisterNetReceive();
        Timber.d("mqtt MqttService 被杀死");
        //System.exit(0); 退出并没finish activity 因此增加了这一步
        ActivityUtils.finishAllActivities();
        System.exit(0);
    }

    public static final int MSG_MESSAGE_SEND_OUT_TIME = 234;//发送message超时
    private int messageIndex = 0;

    private int getMessageIndex() {
        return messageIndex++;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_MESSAGE_SEND_OUT_TIME:
                    //发送message超时
                    outTimeNextMessage();
                    break;
            }
        }
    };


    //*****************************************************网络监听**********************************************************************
   /* private void registerNetReceive() {
        IntentFilter netIntent = new IntentFilter();
        netIntent.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(netReceiver, netIntent);
    }

    private void unRegisterNetReceive() {
        unregisterReceiver(netReceiver);
    }

    private BroadcastReceiver netReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                //有网络
            } else {
                // 无网络
            }
        }
    };*/

    //*****************************************************网络监听 end**********************************************************************

    //******************************************************设备管理**********************************************************************

    /**
     * 清理连接设备list
     */
    private void clearDeviceList() {
        lock.lock();
        if (null != mDeviceLists) {
            mDeviceLists.clear();
        }
        lock.unlock();
    }

    /**
     * 获取当前的设备状态
     * @return
     */
    public List<BleDeviceLocal> getUserDeviceList(){
        return mDeviceLists;
    }

    /**
     * 添加设备
     *
     * @param bleDeviceLocal 设备对象
     */
    private void addDevice(BleDeviceLocal bleDeviceLocal) {
        lock.lock();
        if (null == mDeviceLists) {
            mDeviceLists = new ArrayList<>();
        }
        int index = checkDeviceList(bleDeviceLocal.getEsn(), bleDeviceLocal.getMac());
        boolean bleState = onGetConnectedState(bleDeviceLocal.getMac());//当前蓝牙设的设备的状态
        boolean mqttState = bleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI ? true : false;//当前锁与服务器的连接状态
        boolean appMqttState = MQTTManager.getInstance().onGetMQTTConnectedState();//当前APP与MQTT服务器端的连接状态
        bleDeviceLocal.setConnectedType(checkDeviceState(bleState, mqttState, appMqttState));//ble
        if (index < 0) {
            //因设备添加是从服务端获取，从而需要更新当前的ble连接状态
            mDeviceLists.add(bleDeviceLocal);
        } else {
            //当前list中存在设备，更新当前的状态
            mDeviceLists.remove(index);
            mDeviceLists.add(bleDeviceLocal);
        }
        lock.unlock();
    }

    /**
     * 获取连接状态
     *
     * @param bleState
     * @param mqttState
     * @param appMqttState
     * @return
     */
    private int checkDeviceState(boolean bleState, boolean mqttState, boolean appMqttState) {
        if (bleState && mqttState && appMqttState) {
            return LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE;//双连接
        } else if (!bleState && mqttState && appMqttState) {
            return LocalState.DEVICE_CONNECT_TYPE_WIFI;//wifi
        } else if ((bleState && !mqttState || (bleState && !appMqttState))) {
            return LocalState.DEVICE_CONNECT_TYPE_BLE;//ble
        } else {
            return LocalState.DEVICE_CONNECT_TYPE_DIS;//ble
        }
    }

    /**
     * 解绑 清理队列中的数据
     *
     * @param esn
     * @param mac
     */
    public void removeDevice(String esn, String mac) {
        lock.lock();
        if (null != mDeviceLists) {
            for (int i = 0; i < mDeviceLists.size(); i++) {
                if (esn.equals(mDeviceLists.get(i).getEsn()) || mac.equals(mDeviceLists.get(i).getMac())) {
                    mDeviceLists.remove(i);
                    break;
                }
            }
        }
        lock.unlock();
    }

    /**
     * 根据sn码或是mac 情景：1、MQTT断开【1、App断开，2、设备断开】，2、WIFI或者网络关闭 ，3、设备蓝牙断开，4，蓝牙关闭
     *
     * @param sn
     * @param mac
     * @param state
     */
    public void updateDeviceConnnected(String sn, String mac, int type, boolean state) {
        switch (type) {
            case LocalState.DEVICE_STATE_UPDATE_MQTT_APP:
                //MQTT App断开
                break;
            case LocalState.DEVICE_STATE_UPDATE_MQTT_DEVICE:
                //MQTT 设备断开
                break;
            case LocalState.DEVICE_STATE_UPDATE_WIFI:
                //网络状态变化 仅网络开关状态更新
                if (state) {
                    //MQTT连接
                    MQTTManager.getInstance().mqttConnection();
                } else {
                    //关闭MQTT
                    MQTTManager.getInstance().mqttDisconnect();
                }
                break;
            case LocalState.DEVICE_STATE_UPDATE_DEVICE_BLUETOOTH_DIS:
                //设备蓝牙断开
                break;
            case LocalState.DEVICE_STATE_UPDATE_BLUETOOTH_OFF:
                //蓝牙关闭
                break;
        }

    }

    /**
     * 检查list中设备重复性
     *
     * @param esn
     * @param mac 蓝牙地址
     * @return
     */
    private int checkDeviceList(String esn, String mac) {
        LogUtils.e(TAG, "esn:" + esn + ";mac:" + mac);
        if (0 == mDeviceLists.size()) {
            return -1;
        }
        for (int i = 0; i < mDeviceLists.size(); i++) {
            //判断蓝牙mac和sn码
            if (esn.equals(mDeviceLists.get(i).getEsn()) || mac.equals(mDeviceLists.get(i).getMac())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 更新设备状态
     *
     * @param esn
     * @param mac
     * @return
     */
    public void updateDevice(String esn, String mac) {
        lock.lock();
        LogUtils.e(TAG, "esn:" + esn + ";mac:" + mac);
        if (0 == mDeviceLists.size()) return;
        for (int i = 0; i < mDeviceLists.size(); i++) {
            //判断蓝牙mac和sn码
            if (esn.equals(mDeviceLists.get(i).getEsn()) || mac.equals(mDeviceLists.get(i).getMac())) {
                boolean bleState = onGetConnectedState(mDeviceLists.get(i).getMac());//当前蓝牙设的设备的状态
                boolean mqttState = mDeviceLists.get(i).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI ? true : false;//当前锁与服务器的连接状态
                boolean appMqttState = MQTTManager.getInstance().onGetMQTTConnectedState();//当前APP与MQTT服务器端的连接状态
                mDeviceLists.get(i).setConnectedType(checkDeviceState(bleState, mqttState, appMqttState));//ble
                break;
            }
        }
        lock.unlock();

    }

    /**
     * 更新全部设备的状态
     */
    public void updateAllDevice() {
        lock.lock();
        for (int i = 0; i < mDeviceLists.size(); i++) {
            //判断蓝牙mac和sn码
            boolean bleState = onGetConnectedState(mDeviceLists.get(i).getMac());//当前蓝牙设的设备的状态
            boolean mqttState = mDeviceLists.get(i).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI ? true : false;//当前锁与服务器的连接状态
            boolean appMqttState = MQTTManager.getInstance().onGetMQTTConnectedState();//当前APP与MQTT服务器端的连接状态
            mDeviceLists.get(i).setConnectedType(checkDeviceState(bleState, mqttState, appMqttState));//ble
            break;
        }
        lock.unlock();

    }

    /**
     * 根据 sn或是mac获取设备对象
     *
     * @param esn
     * @param mac
     * @return
     */
    public BleDeviceLocal getDevice(String esn, String mac) {
        LogUtils.e(TAG, "esn:" + esn + ";mac:" + mac);
        if (0 == mDeviceLists.size()) {
            return null;
        }
        for (BleDeviceLocal local : mDeviceLists) {
            //判断蓝牙mac和sn码
            if (esn.equals(local.getEsn()) || mac.equals(local.getMac())) {
                return local;
            }
        }
        return null;
    }

    //******************************************************设备管理 end******************************************************************
    //******************************************************MQTT与BLE 初始化******************************************************************
    // 通讯模块管理 初始化
    public void initMode() {
        //给MQTT连接添加监听器
        MQTTManager.getInstance().setMqttDataLinstener(mqttDataLinstener);
        //初始蓝牙机制
        BleManager.getInstance().setOnBleDeviceListener(onBleDeviceListener);
        BleResultAnalysis.setOnReceivedProcess(receivedProcess);

    }
    //******************************************************MQTT与BLE 初始化 end******************************************************************

    //******************************************************蓝牙管理***************************************************************************

    /**
     * 蓝牙连接
     *
     * @param bleScanResult
     * @param pwd1
     * @param pwd2
     */
    public void onBleConnect(BLEScanResult bleScanResult, byte[] pwd1, byte[] pwd2) {
        //isAppPair 设备列表中有当前连接设备 isAppPair是true,则isAppPair=false  用于配网操作
        String mac = bleScanResult.getBluetoothDevice().getAddress();
        if (null != getDevice("", mac)) {
            BleManager.getInstance().connectDevice(bleScanResult, pwd1, pwd2, true);
        } else {
            BleManager.getInstance().connectDevice(bleScanResult, pwd1, pwd2, false);
        }

    }

    /**
     * 蓝牙断开
     *
     * @param mac
     */
    public void removeBleConnect(String mac) {
        BleManager.getInstance().removeConnectedBleBeanAndDisconnect(mac);
    }

    /**
     * 获取当前设备的连接状态
     *
     * @param mac
     * @return
     */
    public boolean onGetConnectedState(String mac) {
        return BleManager.getInstance().getBleBeanCoonectedState(mac);
    }

    BleResultAnalysis.OnReceivedProcess receivedProcess = new BleResultAnalysis.OnReceivedProcess() {
        @Override
        public void processResult(String mac,BleResultBean bleResultBean) {
            //蓝牙收到回复，做相应的处理或是上报UI
            switch (bleResultBean.getCMD()) {
                case BleProtocolState.CMD_LOCK_OPEN_RECORD:// 0x04;                // 锁开锁记录查询响应
                    break;
                case BleProtocolState.CMD_LOCK_UPLOAD:// 0x05;                     // 锁操作上报
                    break;
                case BleProtocolState.CMD_LOCK_ALARM_UPLOAD:// 0x07;               // 锁报警上报
                    break;
                case BleProtocolState.CMD_ENCRYPT_KEY_UPLOAD:// 0x08;              // 加密密钥上报
                    break;
                case BleProtocolState.CMD_USER_TYPE:// 0x0A;                       // 用户类型查询
                    break;
                case BleProtocolState.CMD_WEEKLY_PLAN_CHECK:// 0x0C;               // 周计划查询
                    break;
                case BleProtocolState.CMD_YEAR_MON_DAY_PLAN_CHECK:// 0x0F;         // 年月日计划查询
                    break;
                case BleProtocolState.CMD_LOCK_ALARM_RECORD_CHECK:// 0x14;         // 锁报警记录查询
                    break;
                case BleProtocolState.CMD_LOCK_NUM_CHECK:// 0x15;                  // 锁序列号查询
                    break;
                case BleProtocolState.CMD_LOCK_OPEN_COUNT_CHECK:// 0x16;           // 锁开锁次数查询
                    break;
                case BleProtocolState.CMD_LOCK_PARAMETER_CHECK:// 0x17;            // 锁参数主动查询
                    break;
                case BleProtocolState.CMD_LOCK_OP_RECORD:// 0x18;                  // 锁操作记录查询
                    break;
                case BleProtocolState.CMD_PAIR_ACK:// 0x1b;                        // 配对确认帧
                    break;
                case BleProtocolState.CMD_KEY_ATTRIBUTES_SET:// 0x1C;              // 密钥属性设置
                    break;
                case BleProtocolState.CMD_KEY_ATTRIBUTES_READ:// 0x1D;             // 密钥属性读
                    break;
                case BleProtocolState.CMD_KEY_ADD:// 0x1E;                         // 密钥添加
                    break;
                case BleProtocolState.CMD_DOOR_SENSOR_CALIBRATION:// 0x1F;         // 门磁校准
                    break;
                case BleProtocolState.CMD_SET_AUTO_LOCK_TIME:// 0x21;              // 设置关门自动上锁时间
                    break;
                case BleProtocolState.CMD_KNOCK_DOOR_AND_UNLOCK_TIME:// 0x22;      // 敲门开锁指令
                    break;
                case BleProtocolState.CMD_SY_LOCK_TIME:// 0x23;                    // 与锁同步时间
                    break;
                case BleProtocolState.CMD_GET_ALL_RECORD:// 0x24;                  // 获取混合记录
                    break;
                case BleProtocolState.CMD_DURESS_PWD_SWITCH:// 0x25;               // 胁迫密码开关
                    break;
                case BleProtocolState.CMD_WIFI_SWITCH:// 0x26;                     // wifi功能开关
                    break;
                case BleProtocolState.CMD_HEART_ACK:// 0x00;                       // 心跳包确认帧
                    break;
                case BleProtocolState.CMD_AUTHENTICATION_ACK:// 0x01;              // 鉴权确认帧
                    break;
                case BleProtocolState.CMD_LOCK_CONTROL_ACK:// 0x02;               // 锁控制确认帧
                    break;
                case BleProtocolState.CMD_LOCK_KEY_MANAGER_ACK:// 0x03;            // 锁密钥管理确认帧
                    break;
                case BleProtocolState.CMD_LOCK_PARAMETER_CHANGED:// 0x06;          // 锁参数修改
                    break;
                case BleProtocolState.CMD_USER_TYPE_SETTING_ACK:// 0x09;           // 用户类型设置确认帧
                    break;
                case BleProtocolState.CMD_WEEKLY_PLAN_SETTING_ACK:// 0x0B;         // 周计划设置确认帧
                    break;
                case BleProtocolState.CMD_WEEKLY_PLAN_DELETE_ACK:// 0x0D;          // 周计划删除确认帧
                    break;
                case BleProtocolState.CMD_YEAR_MON_DAY_PLAN_SETTING_ACK:// 0x0E;   // 年月日计划设置确认帧
                    break;
                case BleProtocolState.CMD_YEAR_MON_DAY_PLAN_DELETE_ACK:// 0x10;    // 年月日计划删除确认帧
                    break;
                case BleProtocolState.CMD_SY_KEY_STATE:// 0x11;                    // 同步门锁密钥状态响应
                    break;
                case BleProtocolState.CMD_LOCK_INFO:// 0x12;                       // 查询门锁基本信息
                    break;
                case BleProtocolState.CMD_REQUEST_BIND_ACK:// 0x13;                // APP绑定请求帧
                    break;
                case BleProtocolState.CMD_CHECK_HARD_VER:// 0x27;                  // 查询硬件版本

                    break;
                case BleProtocolState.CMD_SS_ID_ACK:// 0x90;                       // SS ID响应
                    break;
                case BleProtocolState.CMD_PWD_ACK:// 0x91;                         // PWD响应
                    break;
                case BleProtocolState.CMD_UPLOAD_PAIR_NETWORK:// 0x92;             // 上报配网因子
                    break;
                case BleProtocolState.CMD_UPLOAD_PAIR_NETWORK_STATE:// 0x93;       // BLE上报配网状态
                    break;
                case BleProtocolState.CMD_KEY_VERIFY_RESULT_ACK:// 0x94;           // 秘钥因子校验结果
                    break;
                case BleProtocolState.CMD_UPLOAD_REMAIN_COUNT:// 0x95;             // 上报剩余校验次数
                    break;
                case BleProtocolState.CMD_PAIR_NETWORK_ACK:// 0x96;                // App下发配网状态响应
                    break;
                case BleProtocolState.CMD_BLE_UPLOAD_PAIR_NETWORK_STATE:// 0x97;   // BLE上报联网状态
                    break;
                case BleProtocolState.CMD_WIFI_LIST_CHECK:// 0x98;                 // WIFI热点列表查询响应
                    break;

                case BleProtocolState.CMD_NOTHING:// 0xFF;                         // 无用
                    break;

            }
        }
    };

    //蓝牙状态监听
    OnBleDeviceListener onBleDeviceListener = new OnBleDeviceListener() {
        @Override
        public void onConnected(@NotNull String mac) {
            //蓝牙鉴定连接
            //更新队列中对象的连接状态
            updateDevice("", mac);

        }

        @Override
        public void onDisconnected(@NotNull String mac) {
            //蓝牙断开
            //更新队列中对象的连接状态
            updateDevice("", mac);
        }

        @Override
        public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
            if (value == null) {
                return;
            }
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mac);
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

            BleResultAnalysis.processReceivedData(
                    bleBean.getOKBLEDeviceImp().getMacAddress(),
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


    //******************************************************蓝牙管理 end***********************************************************************
    // **********************************************************MQTT*******************************************************************************

    /**
     * 登录成功获取token后，MQTT连接
     */
    public void connectMQTT() {
        //MQTT 连接
        MQTTManager.getInstance().mqttConnection();
    }

    /**
     * MQTT 状态监听
     */
    MQTTDataLinstener mqttDataLinstener = new MQTTDataLinstener() {
        @Override
        public void MQTTException(int exceptionCode) {
            // 连接异常处理
            switch (exceptionCode) {
                case MQTTErrCode.MQTT_USER_ERR://用户信息错误
                    break;

                case MQTTErrCode.MQTT_USER_USETID_NULL_CODE://token==null
                    break;

                case MQTTErrCode.MQTT_USER_TOKEN_NULL_CODE://token==null
                    break;

                case MQTTErrCode.MQTT_CONNECTION_CODE://已连接
                    break;

                case MQTTErrCode.MQTT_CONNECTION_GET_ERR_CODE://获取当前连接状态失败
                    break;

                case MQTTErrCode.MQTT_CONNECTION_LOST_CODE://连接丢失
                    break;
                case MQTTErrCode.MQTT_CONNECTED_ERR://MQTT连接异常
                    break;
            }
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            /**
             *mqtt 连接完成"
             */
            updateAllDevice();
        }

        @Override
        public void connectionLost(Throwable cause) {
            /**
             *mqtt 连接失败
             */
            updateAllDevice();
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            /**
             *mqtt 连接成功
             */
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            //MQTT 收到消息  ，做相应处理或是上报ui
            try {
                String payload = new String(message.getPayload());
                Timber.d("收到MQtt消息" + payload + "---topic" + topic + "  messageID  " + message.getId());
                JSONObject jsonObject = new JSONObject(payload);
                int messageId = -1;
                String returnCode = "";
                String msgtype = "";
                try {
                    if (payload.contains("returnCode")) {
                        returnCode = jsonObject.getString("returnCode");
                    }

                    if (payload.contains("msgId")) {
                        messageId = jsonObject.getInt("msgId");
                    }

                    if (messageId == -1) {
                        if (payload.contains("msgid")) {
                            messageId = jsonObject.getInt("msgid");
                        }
                    }
                    if (payload.contains("msgtype")) {
                        msgtype = jsonObject.getString("msgtype");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MqttData mqttData = new MqttData(jsonObject.getString("func"), topic, payload, message, messageId);
                mqttData.setReturnCode(returnCode);
                mqttData.setMsgtype(msgtype);
                //MQTT数据处理
                MQTTReply.getInstance().setMqttDataLinstener(mqttDataLinstener);
                MQTTReply.getInstance().onReply(mqttData);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            /*** 连接失败*/
            //TODO:中文下的连接返回，需测试一下，在英文下返回的连接异常
            //可能出现无权连接（5）---用户在其他手机登录

        }

        @Override
        public void onAddDevice(BleDeviceLocal bleDeviceLocal) {
            addDevice(bleDeviceLocal);
        }
    };
    // **********************************************************MQTT end*******************************************************************************

    /**
     * 发送数据接口  根据当前连接状态发送命令  优先WiFi
     *
     * @param message
     */
    public void sendMessage(LockMessage message) {
        //获取当前信息的mac
        String mac = message.getMac();
        //获取当前信息的sn
        String sn = message.getSn();
        BleDeviceLocal bleDeviceLocal = getDevice(sn, mac);
        if (null == bleDeviceLocal) {
            //数据发送异常
        } else {


            if (bleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE
                    || bleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {//wifi}
                sendMessage(message, LOCK_MESSAGE_MQTT);
            } else if (bleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {//ble
                sendMessage(message, LOCK_MESSAGE_BLE);
            } else {
                //设备断开  掉线
            }
        }
    }

    /**
     * 发送数据接口 ，指定发送通道
     *
     * @param message
     * @param MessageType
     */
    public void sendMessage(LockMessage message, int MessageType) {
        message.setMessageType(MessageType);
        addMessage(message);
    }
//    private SendDataThread


    /**
     * * 4、发送数据接口【MQTT、ble、http】情景
     */
  /*  private class SendDataThread extends Thread {
        private boolean isRun = true;
        private List<LockMessage> lockMessageList = new ArrayList<>();
        private Lock lock = new ReentrantLock();

        public void addMessage(LockMessage lockMessage) {
            lock.lock();
            if (null == lockMessageList) {
                lockMessageList = new ArrayList<>();
            }
            lockMessageList.add(lockMessage);
            lock.unlock();
        }

        @Override
        public void run() {
            while (isRun) {
                lock.lock();
                if (null != lockMessageList && lockMessageList.size() > 0) {
                    LockMessage message = lockMessageList.get(0);
                    if (LOCK_MESSAGE_MQTT == message.getMessageType()) {
                        sendMessageMQTT(message);
                    } else if (LOCK_MESSAGE_BLE == message.getMessageType()) {
                        sendMessageBle(message);
                    }
                    lockMessageList.remove(0);
                }
                lock.unlock();
            }
        }
    }*/
    /**
     * 待发的消息队列
     */
    private List<LockMessage> lockMessageList = new ArrayList<>();
    /**
     * 消息队列 锁
     */
    private Lock messageLock = new ReentrantLock();

    private void addMessage(LockMessage lockMessage) {
        messageLock.lock();
        lockMessageList.add(lockMessage);
        messageLock.unlock();
        sendMessage();
    }

    private void nextMessage() {
        messageLock.lock();
        if (lockMessageList.size() > 0) {
            lockMessageList.remove(0);
        }
        messageLock.unlock();
        nextSendMessage();
    }

    private void outTimeNextMessage() {
        //
        nextMessage();
        /*if (lockMessageList.size() > 0) {
            if (lockMessageList.get(0).getSendFrequency() > 3) {
                nextMessage();
            } else {
                if (LOCK_MESSAGE_MQTT == lockMessageList.get(0).getMessageType()) {
                    sendMessageMQTT(lockMessageList.get(0));
                } else if (LOCK_MESSAGE_BLE == lockMessageList.get(0).getMessageType()) {
                    sendMessageBle(lockMessageList.get(0));
                }
            }
        }*/
    }

    private void sendMessage() {
        if (lockMessageList.size() == 1) {
            if (LOCK_MESSAGE_MQTT == lockMessageList.get(0).getMessageType()) {
                sendMessageMQTT(lockMessageList.get(0));
            } else if (LOCK_MESSAGE_BLE == lockMessageList.get(0).getMessageType()) {
                sendMessageBle(lockMessageList.get(0));
            }
        }
    }

    private void nextSendMessage() {
        if (lockMessageList.size() > 0) {
            if (LOCK_MESSAGE_MQTT == lockMessageList.get(0).getMessageType()) {
                sendMessageMQTT(lockMessageList.get(0));
            } else if (LOCK_MESSAGE_BLE == lockMessageList.get(0).getMessageType()) {
                sendMessageBle(lockMessageList.get(0));
            }
        }
    }

    /***
     * 发送 MQTT
     * @param message
     */
    private void sendMessageMQTT(LockMessage message) {
        message.addSendFre();
        MQTTManager.getInstance().mqttPublish(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()), message.getMqttMessage()
        )
                .filter(mqttData -> mqttData.getFunc().equals(MQttConstant.SET_LOCK))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .subscribe(mqttData -> {

                }, e -> {
                    if (e instanceof TimeoutException) {
                    }
                    Timber.e(e);
                });
        mHandler.sendEmptyMessageDelayed(MSG_MESSAGE_SEND_OUT_TIME, 500);
    }

    /**
     * 发送ble
     *
     * @param message
     */
    private void sendMessageBle(LockMessage message) {
        message.addSendFre();
        BleManager.getInstance().write(message.getMac(), message.getBytes());
        mHandler.sendEmptyMessageDelayed(MSG_MESSAGE_SEND_OUT_TIME, 500);
    }
}
