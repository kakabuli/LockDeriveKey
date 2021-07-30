
package com.revolo.lock.manager;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.bean.NetWorkStateBean;
import com.revolo.lock.LockAppManager;
import com.revolo.lock.bean.request.AuthenticationBeanReq;
import com.revolo.lock.bean.request.UpdateLocalBeanReq;
import com.revolo.lock.bean.respone.AuthenticationBeanRsp;
import com.revolo.lock.bean.respone.UpdateLocalBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
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
import com.revolo.lock.mqtt.bean.eventbean.WifiLockOperationEventBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockBaseResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrAutoRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrAutoTimeRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrDuressRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrSensitivityRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrVolumeRspBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.util.ZoneUtil;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.PING_RESULT;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_ADD_DEVICE_SERVICE;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_BLE;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_CLASE_DEVICE;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_MQTT;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_REMOVE_DEVICE;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_USER;

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
    //蓝牙
    private BluetoothAdapter bluetoothAdapter;
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

    private final Thread mRunnable = new Thread() {
        @Override
        public void run() {
            boolean ping = ping();
            Constant.pingResult = ping;
            sendBroadcast(new Intent().setAction(Constant.RECEIVE_ACTION_NETWORKS).putExtra(PING_RESULT, ping));
            mHandler.postDelayed(this, 10 * 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        onRegisterEventBus();
        //服务创建时，初始化MQTT Ble
        initMode();
        //registerNetReceive();
        startSendThread();
        Timber.d("attachView   mqtt 启动了");
        registerBluetoothState();
        mHandler.removeCallbacks(mRunnable);
        mHandler.post(mRunnable);
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
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unRegisterNetReceive();
        stopSendThread();
        Timber.d("mqtt MqttService 被杀死");
        //System.exit(0); 退出并没finish activity 因此增加了这一步
        ActivityUtils.finishAllActivities();
        System.exit(0);
        unRegisterBluetoothState();
        mHandler.removeCallbacks(mRunnable);
    }

    /**
     * 获取蓝牙适配器
     *
     * @return
     */
    private BluetoothAdapter getBluetoothAdapter() {
        if (null == bluetoothAdapter) {
            Timber.e("new  bluetoothAdapter get");
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (null == bluetoothAdapter) {
            BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
            if (null != bluetoothManager) {
                Timber.e("new  bluetoothAdapter manager");
                bluetoothAdapter = bluetoothManager.getAdapter();
            }
        }
        return bluetoothAdapter;
    }

    //监听手机蓝牙的状态
    // BluetoothAdapter.ACTION_STATE_CHANGED
    private void registerBluetoothState() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothState, intentFilter);
    }

    private void unRegisterBluetoothState() {
        unregisterReceiver(bluetoothState);
    }

    public BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                        switch (blueState) {
                            case BluetoothAdapter.STATE_TURNING_ON:
                                Timber.e("蓝牙正在打开");
                                break;
                            case BluetoothAdapter.STATE_ON:
                                Timber.e("蓝牙已经打开");
                                List<BleDeviceLocal> bleDe = new ArrayList<>();
                                if (null != mDeviceLists) {
                                    bleDe.addAll(mDeviceLists);
                                }

                                add(bleDe);
                                break;
                            case BluetoothAdapter.STATE_TURNING_OFF:
                                Timber.e("蓝牙正在关闭");
                                break;
                            case BluetoothAdapter.STATE_OFF:
                                Timber.e("蓝牙已经关闭");
                                //断开所有的连接
                                disBleConnect();
                                updateDeviceBleDis();
                                break;
                        }
                        break;

                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        Timber.e("蓝牙设备已连接");
                        break;

                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        Timber.e("蓝牙设备已断开");
                        break;
                }

            }
        }
    };

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void getEventBus(LockMessage lockMessage) {
        Timber.e("service cmd");
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessageType() == MSG_LOCK_MESSAGE_USER) {
            switch (lockMessage.getMessageCode()) {
                case MSG_LOCK_MESSAGE_REMOVE_DEVICE:
                    //解绑
                    removeDevice(lockMessage.getSn(), lockMessage.getMac());
                    break;
                case MSG_LOCK_MESSAGE_CLASE_DEVICE:
                    //清理蓝牙连接
                    removeDeviceList();
                    break;
            }

        } else {
            sendMessage(lockMessage);
        }
    }


    public static final int MSG_MESSAGE_SEND_OUT_TIME = 234;//发送message超时
    public static final int MSG_MESSAGE_SEND_BLE = 235;
    public static final int MSG_MESSAGE_NEXT_SEND = 236;
    private static final int MSG_BLE_INIT_CMD_OUT_TIME = 10000;//鉴权时间超时
    private static final int MSG_BLE_AUTO = 200;//鉴权时间超时
    private static final int MSG_BLE_CONNECT_TIME = 201;//BLE连接时间超时
    private int messageIndex = 0;

    private int getMessageIndex() {
        return messageIndex++;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            //根据arg2判断是否为鉴权类信息
            if (msg.arg2 == MSG_BLE_AUTO) {
                //鉴权超时处理
                Timber.e("鉴权超时处理：" + ((String) msg.obj));
                clearCmdBleInitOut((String) msg.obj);
                BleBean bleBean = BleManager.getInstance().setBleFromMacInitPwd(((String) msg.obj));
                if (null != bleBean) {
                    if (null != bleBean.getOKBLEDeviceImp()) {
                        Timber.d("%1s 发送配网指令，并校验ESN", bleBean.getOKBLEDeviceImp());
                        BleManager.getInstance().writeControlMsg(BleCommandFactory
                                .pairCommand(bleBean.getPwd1(), bleBean.getEsn().getBytes(StandardCharsets.UTF_8)), bleBean.getOKBLEDeviceImp());
                    }
                }
                return;
            } else if (msg.arg2 == MSG_BLE_CONNECT_TIME) {
                Timber.e("ble连接超时处理：" + msg.obj);
                clearBleOut((String) msg.obj);
                Timber.e("ble连接超时主动清理：" + msg.obj);
                removeBleConnect((String) msg.obj);
                return;
            }
            switch (msg.what) {
                case MSG_MESSAGE_SEND_OUT_TIME:
                    //发送message超时
                    LockMessage message = outTimeNextMessage();
                    if (null != message) {
                        if (message.getMessageType() == 2) {
                            pushErrMessage(message.getMqttMessage().getId() + "", message.getMqtt_message_code());
                        } else if (message.getMessageType() == 3) {

                        }
                    }
                    break;
                case MSG_MESSAGE_SEND_BLE:
                    sendMessageBle((LockMessage) msg.obj);
                    break;
                case MSG_MESSAGE_NEXT_SEND:
                    nextMessage();
                    break;
            }
        }
    };

    public void onRegisterEventBus() {
        boolean registered = EventBus.getDefault().isRegistered(this);
        if (!registered) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getConnected(LockConnected bleConnected) {
        if (null != bleConnected) {
            if (bleConnected.getConnectType() == LocalState.CONNECT_STATE_MQTT) {
                //MQTT连接
                Timber.e("connected type:0");
                connectMQTT();
            } else if (bleConnected.getConnectType() == LocalState.CONNECT_STATE_BIND_DEVICE) {
                Timber.e("connected type:1");
                //绑定设备，蓝牙连接
                onBleConnect(bleConnected.getmEsn(), bleConnected.getBleScanResult(), null, bleConnected.getPwd1(), bleConnected.getPwd2());
            } else if (bleConnected.getConnectType() == LocalState.CONNECT_STATE_MQTT_CONFIG_DOOR) {
                //配置门磁、WiFi时蓝牙连接
                BleDeviceLocal bleDeviceLocal = bleConnected.getBleDeviceLocal();
                if (null != bleDeviceLocal) {
                    connectDevice(bleDeviceLocal);
                  /*  BluetoothDevice device = null;
                    try {
                        device = getBluetoothAdapter().getRemoteDevice(bleDeviceLocal.getMac());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //bleScanResult.
                    byte[] mPwd1 = new byte[16];
                    byte[] bytes = ConvertUtils.hexString2Bytes(bleDeviceLocal.getPwd1());
                    System.arraycopy(bytes, 0, mPwd1, 0, bytes.length);
                    Timber.e("connected type:3");
                    onBleConnect(bleDeviceLocal.getEsn(), null, device, mPwd1, ConvertUtils.hexString2Bytes(bleDeviceLocal.getPwd2())
                    );*/
                }
            } else if (bleConnected.getConnectType() == LocalState.CONNECT_STATE_CHECK_BLE) {
                //离线状态下。检测所有设备的ble连接
                disStateCheckBleConnect(bleConnected.getBleDeviceLocalList());
            }

        }
    }

    private boolean beforeNetWork = true;

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void netWorkState(NetWorkStateBean netWorkStateBean) {
        if (netWorkStateBean != null) {
            boolean pingResult = netWorkStateBean.isPingResult();
            if (pingResult != beforeNetWork) {
                if (pingResult) {
                    LockMessage lockMessage = new LockMessage();
                    lockMessage.setMessageType(2);
                    lockMessage.setMqtt_topic(MQttConstant.PUBLISH_TO_SERVER);
                    lockMessage.setMqtt_message_code(MQttConstant.GET_ALL_BIND_DEVICE);
                    lockMessage.setMqttMessage(MqttCommandFactory.getAllBindDevices(App.getInstance().getUserBean().getUid()));
                    lockMessage.setSn("");
                    lockMessage.setMessageType(MSG_LOCK_MESSAGE_MQTT);
                    lockMessage.setBytes(null);
                    sendMessageMQTT(lockMessage);
                } else {
                    List<BleDeviceLocal> deviceLists = App.getInstance().getDeviceLists();
                    if (deviceLists != null && !deviceLists.isEmpty()) {
                        for (BleDeviceLocal bleDeviceLocal : deviceLists) {
                            if (bleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                                bleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_DIS);
                            }
                        }
                    }
                }
                beforeNetWork = pingResult;
            }
        }
    }

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
     *
     * @return
     */
    public List<BleDeviceLocal> getUserDeviceList() {
        return null != mDeviceLists ? mDeviceLists : new ArrayList<>();
    }
    /*
     * 获取BLe当前的设备状态*/

    public BleBean getUserBleBean(String mac) {
        return BleManager.getInstance().getBleBeanFromMac(mac);
    }

    public void add(List<BleDeviceLocal> bleDeviceLocalList) {
        if (null != bleDeviceLocalList) {
            for (BleDeviceLocal local : bleDeviceLocalList) {
                addDevice(false, local);
            }
        }
    }

    /**
     * 添加设备
     *
     * @param bleDeviceLocal 设备对象
     */
    private void addDevice(boolean isDelete, BleDeviceLocal bleDeviceLocal) {
        //lock.lock();
        if (isDelete) {
            if (null == bleDeviceLocal) {
                if (null != mDeviceLists) {
                    mDeviceLists.clear();
                }
                removeDeviceList();
            } else {
                removeDevice(bleDeviceLocal.getEsn(), bleDeviceLocal.getMac());
                removeBleConnect(bleDeviceLocal.getMac());
            }

        } else {
            if (null == mDeviceLists) {
                mDeviceLists = new ArrayList<>();
            }
            boolean bleState = onGetConnectedState(bleDeviceLocal.getMac());//当前蓝牙设的设备的状态
            boolean mqttState = bleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || bleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE;//当前锁与服务器的连接状态
//            boolean appMqttState = MQTTManager.getInstance().onGetMQTTConnectedState();//当前APP与MQTT服务器端的连接状态
            bleDeviceLocal.setConnectedType(checkDeviceState(bleState, mqttState));//ble
            Timber.d("add device：type:%s;bleState:%s;mqttState:%s", bleDeviceLocal.getConnectedType() + "", bleState, mqttState);
            Timber.d("add device：type:%s", bleDeviceLocal.getConnectedType() + "");
            int index = checkDeviceList(bleDeviceLocal.getEsn(), bleDeviceLocal.getMac());
            if (index < 0) {
                //因设备添加是从服务端获取，从而需要更新当前的ble连接状态
                mDeviceLists.add(bleDeviceLocal);
            } else {
                //当前list中存在设备，更新当前的状态
                //   bleDeviceLocal.set
                if (!bleState || mqttState) {
                    Timber.e("update device content:" + bleDeviceLocal.getEsn());
                    mDeviceLists.remove(index);
                    mDeviceLists.add(bleDeviceLocal);
                } else {
                    //蓝牙模式状态只更新分享装，其他状态以锁为准
                    for (int i = 0; i < mDeviceLists.size(); i++) {
                        if (bleDeviceLocal.getEsn().equals(mDeviceLists.get(i).getEsn())) {
                            Timber.e("update device share content:" + bleDeviceLocal.getEsn()+":"+bleDeviceLocal.getShareUserType());
                            mDeviceLists.get(i).setShareUserType(bleDeviceLocal.getShareUserType());
                            break;
                        }
                    }
                }
            }
            //判断当前ble的连接情况
            Timber.e("当前设备的连接状态：" + bleDeviceLocal.getConnectedType());
            if (!bleState && !mqttState) {
                connectDevice(bleDeviceLocal);
            }
        }
        //lock.unlock();
        LockMessageRes lockMessageRes = new LockMessageRes();
        lockMessageRes.setMessgaeType(LockMessageCode.MSG_LOCK_MESSAGE_MQTT);//蓝牙消息
        lockMessageRes.setResultCode(MSG_LOCK_MESSAGE_CODE_SUCCESS);
        lockMessageRes.setMessageCode(LockMessageCode.MSG_LOCK_MESSAGE_ADD_DEVICE);//添加到设备到主页
        EventBus.getDefault().post(lockMessageRes);
    }

    /**
     * 检查当前蓝牙是否连接
     *
     * @param mac
     */
    public void checkBleConnect(String mac) {
        Timber.e("电子围栏之蓝牙连接：" + mac);
        boolean bleState = onGetConnectedState(mac);//当前蓝牙设的设备的状态
        BleDeviceLocal bleDeviceLocal = null;
        if (null != mDeviceLists) {
            for (BleDeviceLocal bleDev : mDeviceLists) {
                if (bleDev.getMac().equals(mac)) {
                    bleDeviceLocal = bleDev;
                    break;
                }
            }
        }
        if (null == bleDeviceLocal) {
            return;
        }
        //判断当前ble的连接情况
        if (!bleState) {
            connectDevice(bleDeviceLocal);
        }

        //lock.unlock();
        LockMessageRes lockMessageRes = new LockMessageRes();
        lockMessageRes.setMessgaeType(LockMessageCode.MSG_LOCK_MESSAGE_MQTT);//蓝牙消息
        lockMessageRes.setResultCode(MSG_LOCK_MESSAGE_CODE_SUCCESS);
        lockMessageRes.setMessageCode(LockMessageCode.MSG_LOCK_MESSAGE_ADD_DEVICE);//添加到设备到主页
        EventBus.getDefault().post(lockMessageRes);
    }

    /**
     * 离线状态下，检测当前蓝牙是否连接
     */
    public void disStateCheckBleConnect(List<BleDeviceLocal> bleDeviceLocalList) {
        Timber.e("当前是离线状态下，检测当前的设备蓝牙连接状态");
        if (null != bleDeviceLocalList) {
            for (BleDeviceLocal bleDeviceLocal : bleDeviceLocalList) {
                Timber.e("当前是离线状态下，检测当前的设备蓝牙连接状态:"+bleDeviceLocal.getEsn());
                bleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_DIS);
                addDevice(false, bleDeviceLocal);
            }
        }
    }

    private void connectDevice(BleDeviceLocal bleDeviceLocal) {
        if (!getBluetoothAdapter().isEnabled()) {
            Timber.e("当前蓝牙已关闭，无法进行连接");
        } else {
            Timber.e("当前蓝牙正常，正进行连接");
            BluetoothDevice device = null;
            try {
                device = getBluetoothAdapter().getRemoteDevice(bleDeviceLocal.getMac());
            } catch (Exception e) {
                e.printStackTrace();
            }
            //bleScanResult.
            byte[] mPwd1 = new byte[16];
            byte[] bytes = ConvertUtils.hexString2Bytes(bleDeviceLocal.getPwd1());
            System.arraycopy(bytes, 0, mPwd1, 0, bytes.length);
            onBleConnect(bleDeviceLocal.getEsn(), null, device, mPwd1, ConvertUtils.hexString2Bytes(bleDeviceLocal.getPwd2())
            );
        }
    }

    /**
     * 获取连接状态
     *
     * @param bleState
     * @param mqttState
     * @return
     */
    private int checkDeviceState(boolean bleState, boolean mqttState) {
        if (bleState && mqttState) {
            return LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE;//双连接
        } else if (!bleState && mqttState) {
            return LocalState.DEVICE_CONNECT_TYPE_WIFI;//wifi
        } else if (bleState && !mqttState) {
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
        //   lock.lock();
        if (null != mDeviceLists) {
            for (int i = 0; i < mDeviceLists.size(); i++) {
                if (null != esn && esn.equals(mDeviceLists.get(i).getEsn()) || null != mac && mac.equals(mDeviceLists.get(i).getMac())) {
                    mDeviceLists.remove(i);
                    break;
                }
            }
        }
        // lock.unlock();

    }

    public void removeDeviceList() {
        if (null != mDeviceLists) {
            mDeviceLists.clear();
        }
        BleManager.getInstance().removeBleConnected();
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
                    Timber.e("网络状态变化 仅网络开关状态更新 mqtt 连接");
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
            if ((null != esn && esn.equals(mDeviceLists.get(i).getEsn())) || (null != mac && mac.equals(mDeviceLists.get(i).getMac()))) {
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
        //    lock.lock();
        LogUtils.e(TAG, "esn:" + esn + ";mac:" + mac);
        if (0 == mDeviceLists.size()) return;
        for (int i = 0; i < mDeviceLists.size(); i++) {
            //判断蓝牙mac和sn码
            if ((null != esn && esn.equals(mDeviceLists.get(i).getEsn())) || (null != mac && mac.equals(mDeviceLists.get(i).getMac()))) {
                boolean bleState = onGetConnectedState(mDeviceLists.get(i).getMac());//当前蓝牙设的设备的状态
                boolean mqttState = mDeviceLists.get(i).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mDeviceLists.get(i).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE;//当前锁与服务器的连接状态
//                boolean appMqttState = MQTTManager.getInstance().onGetMQTTConnectedState();//当前APP与MQTT服务器端的连接状态
                mDeviceLists.get(i).setConnectedType(checkDeviceState(bleState, mqttState));//ble
                Timber.d("add device：type:%s;bleState:%s;mqttState:%s;", mDeviceLists.get(i).getConnectedType() + "", bleState, mqttState);
                break;
            }
        }
        LockMessageRes messageRes = new LockMessageRes();
        messageRes.setMessgaeType(LockMessageCode.MSG_LOCK_MESSAGE_USER);
        messageRes.setResultCode(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS);
        messageRes.setMessageCode(LockMessageCode.MSG_LOCK_MESSAGE_UPDATE_DEVICE_STATE);

        EventBus.getDefault().post(messageRes);
        //lock.unlock();

    }

    /**
     * 检测到手机蓝牙关闭时，更新所有的ble连接都为断开
     */
    public void updateDeviceBleDis() {
        //    lock.lock();
        Timber.e("updateDeviceBleDis");
        if (0 == mDeviceLists.size()) return;
        for (int i = 0; i < mDeviceLists.size(); i++) {
            //判断蓝牙mac和sn码
            boolean bleState = false;//当前蓝牙设的设备的状态
            boolean mqttState = mDeviceLists.get(i).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mDeviceLists.get(i).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE;//当前锁与服务器的连接状态
//            boolean appMqttState = MQTTManager.getInstance().onGetMQTTConnectedState();//当前APP与MQTT服务器端的连接状态
            mDeviceLists.get(i).setConnectedType(checkDeviceState(bleState, mqttState));//ble
        }
        LockMessageRes messageRes = new LockMessageRes();
        messageRes.setMessgaeType(LockMessageCode.MSG_LOCK_MESSAGE_USER);
        messageRes.setResultCode(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS);
        messageRes.setMessageCode(LockMessageCode.MSG_LOCK_MESSAGE_UPDATE_DEVICE_STATE);

        EventBus.getDefault().post(messageRes);
        //lock.unlock();

    }

    /**
     * 更新全部设备的状态
     */
    public void updateAllDevice() {
        //  lock.lock();
        for (int i = 0; i < mDeviceLists.size(); i++) {
            //判断蓝牙mac和sn码
            boolean bleState = onGetConnectedState(mDeviceLists.get(i).getMac());//当前蓝牙设的设备的状态
            boolean mqttState = mDeviceLists.get(i).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mDeviceLists.get(i).getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE;//当前锁与服务器的连接状态
//            boolean appMqttState = MQTTManager.getInstance().onGetMQTTConnectedState();//当前APP与MQTT服务器端的连接状态
            mDeviceLists.get(i).setConnectedType(checkDeviceState(bleState, mqttState));//ble
        }
        //  lock.unlock();
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
            if (null != esn && esn.equals(local.getEsn()) || null != mac && mac.equals(local.getMac())) {
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
    public void onBleConnect(String sn, BLEScanResult bleScanResult, BluetoothDevice bluetoothDevice, byte[] pwd1, byte[] pwd2) {
        //isAppPair 设备列表中有当前连接设备 isAppPair是true,则isAppPair=false  用于配网操作
        String mac = "";
        if (null == bleScanResult && null == bluetoothDevice) {
            Timber.e("蓝牙连接 bleScanResult=null，bluetoothDevice=null");
            return;
        }
        if (null == bleScanResult) {
            mac = bluetoothDevice.getAddress();
            Timber.e("蓝牙连接 bluetoothDevice Mac：%s", mac);
        } else {
            mac = bleScanResult.getBluetoothDevice().getAddress();
            Timber.e("蓝牙连接 bleScanResult mac：%s", mac);
        }
        if (null != getDevice("", mac)) {
            BleManager.getInstance().connectDevice(sn, bleScanResult, bluetoothDevice, pwd1, pwd2, true);
        } else {
            BleManager.getInstance().connectDevice(sn, bleScanResult, bluetoothDevice, pwd1, pwd2, false);
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
     * 断开所有设备的连接
     */
    public void disBleConnect() {
        BleManager.getInstance().disConnect();
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
        public void processResult(String mac, BleResultBean bleResultBean) {
            //蓝牙收到回复，做相应的处理或是上报UI
            Timber.e("processResult 解析完成 mac: %1s\n", mac);
            Timber.e("processResult 解析完成 CMD: %1d",
                    bleResultBean.getCMD());
            bleResultBean.setmMac(mac);
            switch (bleResultBean.getCMD()) {
                case BleProtocolState.CMD_LOCK_OPEN_RECORD:// 0x04;                // 锁开锁记录查询响应
                    break;
                case BleProtocolState.CMD_LOCK_UPLOAD:// 0x05;
                    // 锁操作上报
                    updateLockState(mac, bleResultBean);
                    break;
                case BleProtocolState.CMD_LOCK_ALARM_UPLOAD:// 0x07;               // 锁报警上报
                    UpdateLockAlarm(mac, bleResultBean);
                    break;
                case BleProtocolState.CMD_ENCRYPT_KEY_UPLOAD:// 0x08;              // 加密密钥上报
                    processKey(bleResultBean);
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
                    if (bleResultBean.getPayload()[0] == 0) {
                        // 设置敲击开锁成功 清理当前地理位置监控
                        updateDeviceState(mac);
                        removeBleConnect(mac);
                    }
                    break;
                case BleProtocolState.CMD_SY_LOCK_TIME:// 0x23;                    // 与锁同步时间
                    break;
                case BleProtocolState.CMD_GET_ALL_RECORD:// 0x24;                  // 获取混合记录
                    break;
                case BleProtocolState.CMD_DURESS_PWD_SWITCH:// 0x25;               // 胁迫密码开关
                    break;
                case BleProtocolState.CMD_WIFI_SWITCH:// 0x26;                     // wifi功能开关
                    break;
                case BleProtocolState.CMD_TIME://0x28 时间同步返回
                {//获取设备信息
                    //
                    clearCmdBleInitOut(mac);
                    clearBleOut(mac.toUpperCase());
                    BleBean mBleBean = BleManager.getInstance().getBleBeanFromMac(mac);
                    if (null != mBleBean) {
                        BleManager.getInstance().writeControlMsg(BleCommandFactory
                                .checkLockBaseInfoCommand(mBleBean.getPwd1(), mBleBean.getPwd3()), mBleBean.getOKBLEDeviceImp());
                    }
                }
                break;
                case BleProtocolState.CMD_HEART_ACK:// 0x00;                       // 心跳包确认帧
                    break;
                case BleProtocolState.CMD_AUTHENTICATION_ACK:// 0x01;              // 鉴权确认帧
                    BleBean bleBean = BleManager.getInstance().getBleBeanFromMac(mac);
                    if (null != bleBean) {
                        BleManager.getInstance().writeControlMsg(BleCommandFactory
                                .ackCommand(bleResultBean.getTSN(), (byte) 0x00, bleResultBean.getCMD()), bleBean.getOKBLEDeviceImp());
                    }
                    addCmdBleInitOut(mac);
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
                    updateLockInfo(mac, bleResultBean);
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
                    Timber.e("CMD_BLE_UPLOAD_PAIR_NETWORK_STATE");
                    break;
                case BleProtocolState.CMD_WIFI_LIST_CHECK:// 0x98;                 // WIFI热点列表查询响应
                    break;

                case BleProtocolState.CMD_NOTHING:// 0xFF;                         // 无用
                    break;

            }
            LockMessageRes messageRes = new LockMessageRes(1, mac, bleResultBean);
            messageRes.setResultCode(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS);
            EventBus.getDefault().post(messageRes);
        }
    };
    private Map<String, Integer> cmdOutTimes = new HashMap<>();
    private Map<String, Integer> bleConnectTimes = new HashMap<>();

    /**
     * ble鉴权超时控制
     *
     * @param mac
     */
    private void addBleOut(String mac) {
        Timber.e("开始添加连接超时msg：" + mac);
        if (bleConnectTimes.containsKey(mac)) {
            //存在 handler
            Timber.e("开始添加ble连接存在超时msg：准备删除：" + mac);
            if (mHandler.hasMessages(bleConnectTimes.get(mac))) {
                Timber.e("开始添加ble连接存在超时msg：删除：" + mac);
                mHandler.removeMessages(bleConnectTimes.get(mac));
                bleConnectTimes.remove(mac);
            }
        }
        Timber.e("添加ble连接超时msg：" + mac);
        int msgWhat = bleConnectTimes.keySet().size() + 1000;
        bleConnectTimes.put(mac, msgWhat);
        Message msg = new Message();
        msg.what = msgWhat;
        msg.obj = mac;
        msg.arg2 = 201;
        mHandler.sendMessageDelayed(msg, 60000);

    }

    /**
     * 清理掉ble鉴权超时控制
     *
     * @param mac
     */
    private void clearBleOut(String mac) {
        Timber.e("开始清理ble连接超时msg：" + mac);
        if (bleConnectTimes.containsKey(mac)) {
            Timber.e("ble连接超时监听列表中存在：" + mac + ",开始清理");
            mHandler.removeMessages(bleConnectTimes.get(mac));
            bleConnectTimes.remove(mac);
        }
        clearCmdBleInitOut(mac);
    }

    /**
     * ble鉴权超时控制
     *
     * @param mac
     */
    private void addCmdBleInitOut(String mac) {
        Timber.e("开始添加ble鉴权超时msg：" + mac);
        if (cmdOutTimes.containsKey(mac)) {
            //存在 handler
            Timber.e("开始添加ble存在鉴权超时msg：" + mac);
        } else {
            Timber.e("添加ble鉴权超时msg：" + mac);
            int msgWhat = cmdOutTimes.keySet().size() + 600;
            cmdOutTimes.put(mac, msgWhat);
            Message msg = new Message();
            msg.what = msgWhat;
            msg.obj = mac;
            msg.arg2 = 200;
            mHandler.sendMessageDelayed(msg, MSG_BLE_INIT_CMD_OUT_TIME);
        }
    }

    /**
     * 清理掉ble鉴权超时控制
     *
     * @param mac
     */
    private void clearCmdBleInitOut(String mac) {
        Timber.e("开始清理ble鉴权超时msg：" + mac);
        if (cmdOutTimes.containsKey(mac)) {
            Timber.e("鉴权列表中存在：" + mac + ",开始清理");
            mHandler.removeMessages(cmdOutTimes.get(mac));
            cmdOutTimes.remove(mac);
        }
    }

    /**
     * 更新电子围栏状态
     *
     * @param mac
     */
    private void updateDeviceState(String mac) {
        BleBean bleBean = BleManager.getInstance().getBleBeanFromMac(mac);
        if (null != bleBean) {
            for (int index = 0; index < mDeviceLists.size(); index++) {
                if (null != mac && mac.equals(mDeviceLists.get(index).getMac())) {
                    if (mDeviceLists.get(index).setLockElecFenceState(false)) {
                        pushServiceGeoState(mDeviceLists.get(index));
                    }
                    if (null != App.getInstance().getLockGeoFenceService()) {
                        App.getInstance().getLockGeoFenceService().updateLockCmdState(mDeviceLists.get(index).getEsn(), 0);
                        App.getInstance().getLockGeoFenceService().updateLockLocalState(mDeviceLists.get(index).getEsn(), false);
                        App.getInstance().getLockGeoFenceService().clearDeviceS(mDeviceLists.get(index).getEsn());
                        // mDeviceLists.get(index).setOpenElectricFence(false);
                        AppDatabase.getInstance(getApplicationContext()).bleDeviceDao().update(mDeviceLists.get(index));
                        Timber.e("app service updateDeviceState curr mac: %s", App.getInstance().getmCurrMac());
                        Timber.e("app service updateDeviceState curr sn: %s", App.getInstance().getmCurrSn());
                        Timber.e("app service updateDeviceState  mac: %s", mDeviceLists.get(index).getMac());
                        Timber.e("app service updateDeviceState  sn: %s", mDeviceLists.get(index).getEsn());
                        if ((null != mDeviceLists.get(index).getMac() && mDeviceLists.get(index).getMac().equals(App.getInstance().getmCurrMac())) ||
                                (null != mDeviceLists.get(index).getEsn() && mDeviceLists.get(index).getEsn().equals(App.getInstance().getmCurrSn()))) {
                            Timber.e("app service updateDeviceState set BleDeviceLocal");
                            App.getInstance().setBleDeviceLocal(mDeviceLists.get(index));
                        }
                    }
                    break;
                }
            }

        }
    }

    /**
     * 更新地理围栏
     *
     * @param mac                        mac码
     * @param "isOpenElectricFence"      是否开启地理围栏
     * @param "electricFenceSensitivity" 灵敏度
     * @param "electricFenceTime"        蓝牙广播时间
     * @param "latitude"                 经纬度
     * @param "longitude"                经纬度
     * @param "elecFenceState"           是否从200米外进入
     */
    public void updateDeviceGeoState(String mac, BleDeviceLocal bleDeviceLocal) {
        if (null != bleDeviceLocal) {
            BleBean bleBean = BleManager.getInstance().getBleBeanFromMac(mac);
            if (null != bleBean) {
                for (int index = 0; index < mDeviceLists.size(); index++) {
                    if (null != mac && mac.equals(mDeviceLists.get(index).getMac())) {
                        if (null != App.getInstance().getLockGeoFenceService()) {
                            mDeviceLists.get(index).setOpenElectricFence(bleDeviceLocal.isOpenElectricFence());
                            mDeviceLists.get(index).setSetElectricFenceSensitivity(bleDeviceLocal.getSetElectricFenceSensitivity());
                            mDeviceLists.get(index).setSetElectricFenceTime(bleDeviceLocal.getSetElectricFenceTime());
                            mDeviceLists.get(index).setLongitude(bleDeviceLocal.getLongitude());
                            mDeviceLists.get(index).setLatitude(bleDeviceLocal.getLatitude());
                            mDeviceLists.get(index).setLockElecFenceState(bleDeviceLocal.getElecFenceState());
                            AppDatabase.getInstance(getApplicationContext()).bleDeviceDao().update(mDeviceLists.get(index));
                            Timber.e("app service updateDeviceGeo curr mac: %s", App.getInstance().getmCurrMac());
                            Timber.e("app service updateDeviceGeo curr sn: %s", App.getInstance().getmCurrSn());
                            Timber.e("app service updateDeviceGeo  mac: %s", mDeviceLists.get(index).getMac());
                            Timber.e("app service updateDeviceGeo  sn: %s", mDeviceLists.get(index).getEsn());
                            if ((null != mDeviceLists.get(index).getMac() && mDeviceLists.get(index).getMac().equals(App.getInstance().getmCurrMac())) ||
                                    (null != mDeviceLists.get(index).getEsn() && mDeviceLists.get(index).getEsn().equals(App.getInstance().getmCurrSn()))) {
                                Timber.e("app service updateDeviceGeo set BleDeviceLocal");
                                App.getInstance().setBleDeviceLocal(mDeviceLists.get(index));
                            }
                        }
                        break;
                    }
                }

            }
        }
    }

    public void pushServiceGeoState(BleDeviceLocal mBleDeviceLocal) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                UpdateLocalBeanReq lockLocal = new UpdateLocalBeanReq();
                lockLocal.setSn(mBleDeviceLocal.getEsn());
                lockLocal.setElecFence(mBleDeviceLocal.isOpenElectricFence() ? 1 : 0);
                lockLocal.setElecFenceSensitivity(mBleDeviceLocal.getSetElectricFenceSensitivity());
                lockLocal.setElecFenceTime(mBleDeviceLocal.getSetElectricFenceTime());
                lockLocal.setLatitude(mBleDeviceLocal.getLatitude() + "");
                lockLocal.setLongitude(mBleDeviceLocal.getLongitude() + "");
                lockLocal.setElecFenceState(mBleDeviceLocal.getElecFenceState() ? 0 : 1);
                String token = App.getInstance().getUserBean().getToken();
                Observable<UpdateLocalBeanRsp> observable = HttpRequest.getInstance().updateockeLecfence(token, lockLocal);
                ObservableDecorator.decorate(observable).safeSubscribe(new Observer<UpdateLocalBeanRsp>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull UpdateLocalBeanRsp changeKeyNickBeanRsp) {
                        Timber.e("上报地理围栏数据");
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        });
    }

    /**
     * 检测当前设备pwd
     *
     * @param mac
     */
    private void checkDevicePwd(String mac, String pass) {
        Timber.e("当前检测设备的pwd：" + mac);
        if (null != mDeviceLists) {
            for (int i = 0; i < mDeviceLists.size(); i++) {
                if (mDeviceLists.get(i).getMac().equals(mac)) {
                    if (mDeviceLists.get(i).getPwd2().equals(pass)) {
                        Timber.e("当前设备的pwd一致：" + mac);
                    } else {
                        Timber.e("当前设备的pwd不一致，需要同步：" + mac);
                        mDeviceLists.get(i).setPwd2(pass);
                        pushServicePwd(mDeviceLists.get(i).getEsn(), pass);
                    }
                    //同步当前设备
                    if ((null != mDeviceLists.get(i).getMac() && mDeviceLists.get(i).getMac().equals(App.getInstance().getmCurrMac())) ||
                            (null != mDeviceLists.get(i).getEsn() && mDeviceLists.get(i).getEsn().equals(App.getInstance().getmCurrSn()))) {
                        Timber.e("app service setLockState set BleDeviceLocal");
                        App.getInstance().setBleDeviceLocal(mDeviceLists.get(i));
                    }
                    break;
                }
            }
        }
    }

    /**
     * 上报鉴权 pwd2
     *
     * @param esn
     * @param pass
     */
    private void pushServicePwd(String esn, String pass) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AuthenticationBeanReq lockLocal = new AuthenticationBeanReq();
                lockLocal.setWifiSN(esn);
                lockLocal.setPassWord(pass);
                String token = App.getInstance().getUserBean().getToken();
                Observable<AuthenticationBeanRsp> observable = HttpRequest.getInstance().updateocAuthentication(token, lockLocal);
                ObservableDecorator.decorate(observable).safeSubscribe(new Observer<AuthenticationBeanRsp>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull AuthenticationBeanRsp changeKeyNickBeanRsp) {
                        String code = changeKeyNickBeanRsp.getCode();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        });
    }

    /**
     * 更新报警信息
     *
     * @param mac
     * @param bean
     */
    private void UpdateLockAlarm(@NotNull String mac, BleResultBean bean) {
        // TODO: 2021/4/8 后续需要生成对应的实体类再使用
        byte[] alarmCode = new byte[4];
        System.arraycopy(bean.getPayload(), 4, alarmCode, 0, alarmCode.length);
        byte[] alarmCodeBit7_0 = BleByteUtil.byteToBit(alarmCode[3]);
        if (alarmCodeBit7_0[7] == 1) {

        }
    }

    /**
     * 更新锁的基本信息
     *
     * @param bean
     */
    private void updateLockInfo(@NotNull String mac, BleResultBean bean) {
        // TODO: 2021/2/8 锁基本信息处理
        Timber.e("update lock info");
        int index = checkDeviceList(mac, mac);
        if (-1 == index) {
            Timber.e("update lock info return null");
            return;
        }
        byte[] lockFunBytes = new byte[4];
        System.arraycopy(bean.getPayload(), 0, lockFunBytes, 0, lockFunBytes.length);
        // 以下标来命名区分 bit0~7
        // bit8~15
        byte[] bit15_8 = BleByteUtil.byteToBit(lockFunBytes[2]);
        // bit16~23
        byte[] bit23_16 = BleByteUtil.byteToBit(lockFunBytes[1]);

        byte[] lockState = new byte[4];
        System.arraycopy(bean.getPayload(), 4, lockState, 0, lockState.length);
        byte[] lockStateBit7_0 = BleByteUtil.byteToBit(lockState[3]);
        byte[] lockStateBit15_8 = BleByteUtil.byteToBit(lockState[2]);
        byte[] bit7_0 = BleByteUtil.byteToBit(lockState[0]);
        int soundVolume = bean.getPayload()[8];
        byte[] language = new byte[2];
        System.arraycopy(bean.getPayload(), 9, language, 0, language.length);
        String languageStr = new String(language, StandardCharsets.UTF_8);
        int power = bean.getPayload()[11];
        byte[] time = new byte[4];
        System.arraycopy(bean.getPayload(), 12, time, 0, time.length);
        long realTime = (BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time)) + Constant.WILL_ADD_TIME) * 1000;
        Timber.d("lockInfo CMD: %1d, lockFunBytes: bit7_0: %2s, bit15_8: %3s, bit23_16: %4s, lockStateBit7_0: %5s, lockStateBit15_8: %6s, soundVolume: %7d, language: %8s, battery: %9d, time: %10d",
                bean.getCMD(), ConvertUtils.bytes2HexString(bit7_0), ConvertUtils.bytes2HexString(bit15_8),
                ConvertUtils.bytes2HexString(bit23_16), ConvertUtils.bytes2HexString(lockStateBit7_0),
                ConvertUtils.bytes2HexString(lockStateBit15_8), soundVolume, languageStr, power, realTime);

        mDeviceLists.get(index).setLockPower(power);
        boolean isMute = (soundVolume == LocalState.VOLUME_STATE_MUTE);
        mDeviceLists.get(index).setMute(isMute);
        byte doorSensorState = bit7_0[4];
        boolean isOpenDoorSensor = (doorSensorState == 0x01);
        byte privati = bit7_0[2];
        byte openState = bit7_0[1];
        boolean privat = privati == 0x01;
        // TODO: 2021/4/21 暂时屏蔽掉开始的基本信息检查
//        mBleDeviceLocal.setOpenDoorSensor(isOpenDoorSensor);
        Timber.d("电量：%1d, 是否静音 %2b, 门磁功能是否开启：%3b，隐私模式：%3b,门状态：%4d", power, isMute, isOpenDoorSensor, privat, openState);
        if (privat) {
            setLockState(checkDeviceList(mac, mac), LocalState.LOCK_STATE_PRIVATE);
        } else {
            if (openState == 0) {
                setLockState(checkDeviceList(mac, mac), LocalState.LOCK_STATE_CLOSE);
            } else {
                setLockState(checkDeviceList(mac, mac), LocalState.LOCK_STATE_OPEN);
            }

        }
        Timber.e("app service updateLockInfo curr mac: %s", App.getInstance().getmCurrMac());
        Timber.e("app service updateLockInfo curr sn: %s", App.getInstance().getmCurrSn());

        Timber.e("app service updateLockInfo  mac: %s", mDeviceLists.get(index).getMac());
        Timber.e("app service updateLockInfo  sn: %s", mDeviceLists.get(index).getEsn());
        if ((null != mDeviceLists.get(index).getMac() && mDeviceLists.get(index).getMac().equals(App.getInstance().getmCurrMac())) ||
                (null != mDeviceLists.get(index).getEsn() && mDeviceLists.get(index).getEsn().equals(App.getInstance().getmCurrSn()))) {
            Timber.e("app service updateLockInfo set BleDeviceLocal");
            App.getInstance().setBleDeviceLocal(mDeviceLists.get(index));
        }
        AppDatabase.getInstance(this).bleDeviceDao().update(mDeviceLists.get(index));
        Timber.e("devices %s:", mDeviceLists.get(index).toString());
        List<BleDeviceLocal> locals = AppDatabase.getInstance(App.getInstance()).bleDeviceDao().findBleDevicesFromUserIdByCreateTimeDesc(mDeviceLists.get(index).getUserId());
        if (locals.size() > 0) {
            Timber.e("locals %s:", locals.get(0).toString());
        }
    }

    /**
     * 更新锁的状态
     *
     * @param mac
     * @param bean
     */
    private void updateLockState(@NotNull String mac, BleResultBean bean) {
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

        if (eventType == 0x01) {
            //0x01：Operation操作(动作类)
            if (eventSource == 0x0a) {
                //震动方式
                updateDeviceState(mac);
                removeBleConnect(mac);
                Timber.e("震动方式：" + eventCode);
            } else if (eventSource == 0x0b) {
                //触摸方式
                Timber.e("触摸方式：" + eventCode);
                updateDeviceState(mac);
                removeBleConnect(mac);
            } else if (eventSource == 0x09) {
                byte[] code = BleByteUtil.byteToBit((byte) eventCode);
                Timber.e("dagaggg :%s", code[0] + "");
                if (code[0] == 0x01) {
                    setLockState(checkDeviceList(mac, mac), LocalState.LOCK_STATE_PRIVATE);
                }
            } else {
                if (eventCode == 0x01) {
                    // 上锁
                    setLockState(checkDeviceList(mac, mac), LocalState.LOCK_STATE_CLOSE);
                } else if (eventCode == 0x02) {
                    // 开锁
                    setLockState(checkDeviceList(mac, mac), LocalState.LOCK_STATE_OPEN);
                }
            }
        } else if (eventType == 0x02) {
            // 0x02：Program程序(用户管理类)
        } else if (eventType == 0x04) {
            //0x04：Sensor附加状态（其他传感器的状态值）
            // sensor附加状态，门磁
            if (eventCode == LocalState.DOOR_SENSOR_OPEN) {
                // 开门
                setDoorState(checkDeviceList(mac, mac), LocalState.DOOR_SENSOR_OPEN);
            } else if (eventCode == LocalState.DOOR_SENSOR_CLOSE) {
                // 关门
                setDoorState(checkDeviceList(mac, mac), LocalState.DOOR_SENSOR_CLOSE);
            } else if (eventCode == LocalState.DOOR_SENSOR_EXCEPTION) {
                // 门磁异常
                // TODO: 2021/3/31 门磁异常的操作
                Timber.d("lockUpdateInfo 门磁异常");
            } else if (eventCode == 3) {
                //电子围栏超时
                Timber.e("电子围栏超时");
                updateDeviceState(mac);
                removeBleConnect(mac);
            }
        }
    }

    /**
     * 设置门的状态
     *
     * @param index
     * @param state
     */
    private void setLockState(int index, @LocalState.LockState int state) {
        if (index == -1) {
            return;
        }
        if (null != mDeviceLists.get(index)) {
           /* if (mDeviceLists.get(index).getLockState() == LocalState.LOCK_STATE_PRIVATE && state != LocalState.LOCK_STATE_OPEN) {
                Timber.e("设置门的状态过滤：%s", state + "");
                return;
            }*/
            mDeviceLists.get(index).setLockState(state);
            Timber.d("setLockState wifiId: %1s %2s", mDeviceLists.get(index).getEsn(), state == LocalState.LOCK_STATE_OPEN ? "锁开了" : "锁关了");
            AppDatabase.getInstance(getApplicationContext()).bleDeviceDao().update(mDeviceLists.get(index));
            Timber.e("app service setLockState curr mac: %s", App.getInstance().getmCurrMac());
            Timber.e("app service setLockState curr sn: %s", App.getInstance().getmCurrSn());

            Timber.e("app service setLockState  mac: %s", mDeviceLists.get(index).getMac());
            Timber.e("app service setLockState  sn: %s", mDeviceLists.get(index).getEsn());
            if ((null != mDeviceLists.get(index).getMac() && mDeviceLists.get(index).getMac().equals(App.getInstance().getmCurrMac())) ||
                    (null != mDeviceLists.get(index).getEsn() && mDeviceLists.get(index).getEsn().equals(App.getInstance().getmCurrSn()))) {
                Timber.e("app service setLockState set BleDeviceLocal");
                App.getInstance().setBleDeviceLocal(mDeviceLists.get(index));
            }
        }
    }

    /**
     * 设置门磁状态
     *
     * @param index
     * @param state
     */
    private void setDoorState(int index, @LocalState.DoorSensor int state) {
        if (index == -1) {
            return;
        }
        if (null != mDeviceLists.get(index)) {
            Timber.d("setDoorState wifiId: %1s %2s", mDeviceLists.get(index).getEsn(), state == LocalState.DOOR_SENSOR_OPEN ? "开门了" : "关门了");
            mDeviceLists.get(index).setDoorSensor(state);
            AppDatabase.getInstance(getApplicationContext()).bleDeviceDao().update(mDeviceLists.get(index));
            Timber.e("app service setDoorState curr mac: %s", App.getInstance().getmCurrMac());
            Timber.e("app service setDoorState curr sn: %s", App.getInstance().getmCurrSn());
            Timber.e("app service setDoorState  mac: %s", mDeviceLists.get(index).getMac());
            Timber.e("app service setDoorState  sn: %s", mDeviceLists.get(index).getEsn());
            if ((null != mDeviceLists.get(index).getMac() && mDeviceLists.get(index).getMac().equals(App.getInstance().getmCurrMac())) ||
                    (null != mDeviceLists.get(index).getEsn() && mDeviceLists.get(index).getEsn().equals(App.getInstance().getmCurrSn()))) {
                Timber.e("app service setDoorState set BleDeviceLocal");
                App.getInstance().setBleDeviceLocal(mDeviceLists.get(index));
            }

        }
    }


    /**
     * 秘钥上报
     *
     * @param bleResultBean
     */
  /*  private void processKey(BleResultBean bleResultBean) {
        byte[] data = bleResultBean.getPayload();
        String mMac = bleResultBean.getScanResult().getMacAddress();
        BleBean bleBean = BleManager.getInstance().getBleBeanFromMac(mMac);
        if (bleBean == null) {
            Timber.e("processKey bleBean == null");
            return;
        }
        if (data[0] == 0x01) {
            // 入网时
            // 获取pwd2
            // bleBean.setPwd2(bleBean.getPwd2_copy());
            Timber.e("getPwd2AndSendAuthCommand 延时发送鉴权指令, pwd23: %1s\n", ConvertUtils.bytes2HexString(bleBean.getPwd2_copy()));
            BleManager.getInstance().writeControlMsg(BleCommandFactory
                    .ackCommand(bleResultBean.getTSN(), (byte) 0x00, bleResultBean.getCMD()), bleBean.getOKBLEDeviceImp());
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Timber.d("getPwd2AndSendAuthCommand 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(bleBean.getPwd2()));
                BleManager.getInstance().writeControlMsg(BleCommandFactory
                        .authCommand(bleBean.getPwd1(), bleBean.getPwd2(), bleBean.getEsn().getBytes(StandardCharsets.UTF_8)), bleBean.getOKBLEDeviceImp());
            }, 50);
        } else if (data[0] == 0x02) {
            // 获取pwd3
            Timber.d("getPwd2AndSendAuthCommand 延时发送鉴权指令, pwd2222: %1s\n", ConvertUtils.bytes2HexString(bleBean.getPwd2()));
            BleManager.getInstance().writeControlMsg(BleCommandFactory
                    .ackCommand(bleResultBean.getTSN(), (byte) 0x00, bleResultBean.getCMD()), bleBean.getOKBLEDeviceImp());

            EventBus.getDefault().post(bleResultBean);
        }
    }*/
    private void processKey(BleResultBean bleResultBean) {
        if (bleResultBean.getCMD() == BleProtocolState.CMD_ENCRYPT_KEY_UPLOAD) {
            byte[] data = bleResultBean.getPayload();
            String mMac = "";
            if (null == bleResultBean.getScanResult()) {
                mMac = bleResultBean.getmMac();
            } else {
                mMac = bleResultBean.getScanResult().getMacAddress();
            }
            BleBean bleBean = BleManager.getInstance().getBleBeanFromMac(mMac);
            if (bleBean == null) {
                Timber.e("processKey bleBean == null");
                return;
            }
            if (data[0] == 0x01) {
                // 入网时
                Timber.e("processKey data[0]==0x01:%1s\n", ConvertUtils.bytes2HexString(data));
                // 获取pwd2
                getPwd2AndSendAuthCommand(bleResultBean, data, bleBean);
            } else if (data[0] == 0x02) {
                // 获取pwd3
                Timber.e("processKey data[0]==0x02:%1s\n", ConvertUtils.bytes2HexString(data));
                getPwd3(bleResultBean, data, bleBean);
                // 鉴权成功后，同步当前时间
                syNowTime(bleBean);
                //鉴权成功后，将设备添加到服务器端中
                // 判断当前的蓝牙设备是否开启电子围栏
                senGeoFence(bleBean.getMac().toUpperCase());
                //更新//上报给服务器
                checkDevicePwd(bleBean.getMac(), ConvertUtils.bytes2HexString(bleBean.getPwd2()));

                LockMessageRes message = new LockMessageRes();
                message.setMessgaeType(MSG_LOCK_MESSAGE_BLE);//蓝牙消息
                message.setResultCode(MSG_LOCK_MESSAGE_CODE_SUCCESS);//操作成功
                message.setMessageCode(MSG_LOCK_MESSAGE_ADD_DEVICE_SERVICE);//添加设备到服务端
                message.setBleResultBea(bleResultBean);
                message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
                EventBus.getDefault().post(message);
                Timber.e("processKey EventBus.getDefault().post:%1s\n", ConvertUtils.bytes2HexString(data));
                //  addDeviceToService(bleResultBean);
            }
        }
    }

    private void getPwd2AndSendAuthCommand(BleResultBean bleResultBean, byte[] data, @NotNull BleBean bleBean) {
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("getPwd2AndSendAuthCommand bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        byte[] mPwd2 = new byte[4];
        System.arraycopy(data, 1, mPwd2, 0, mPwd2.length);
        // TODO: 2021/1/21 打包数据上传到服务器后再发送确认指令
        bleBean.setHavePwd2Or3(true);
        bleBean.setPwd2(mPwd2);
        // bleBean.setEsn(mEsn);
        BleManager.getInstance().writeControlMsg(BleCommandFactory
                .ackCommand(bleResultBean.getTSN(), (byte) 0x00, bleResultBean.getCMD()), bleBean.getOKBLEDeviceImp());
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Timber.d("getPwd2AndSendAuthCommand 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2));
            BleManager.getInstance().writeControlMsg(BleCommandFactory
                    .authCommand(bleBean.getPwd1(), mPwd2, bleBean.getEsn().getBytes(StandardCharsets.UTF_8)), bleBean.getOKBLEDeviceImp());
        }, 50);
    }

    private void getPwd3(BleResultBean bleResultBean, byte[] data, @NotNull BleBean bleBean) {
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("getPwd3 bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        byte[] mPwd3 = new byte[4];
        System.arraycopy(data, 1, mPwd3, 0, mPwd3.length);
        Timber.d("getPwd3 鉴权成功, pwd3: %1s\n", ConvertUtils.bytes2HexString(mPwd3));
        // 内存存储
        //bleBean.setPwd1(mPwd1);
        bleBean.setPwd3(mPwd3);
        BleManager.getInstance().writeControlMsg(BleCommandFactory
                .ackCommand(bleResultBean.getTSN(), (byte) 0x00, bleResultBean.getCMD()), bleBean.getOKBLEDeviceImp());
    }

    private void syNowTime(@NotNull BleBean bleBean) {
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("syNowTime bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            long nowTime = ZoneUtil.getTime() / 1000;
            Timber.e("**************************   nowTime = " + nowTime + " timeZone = +" + ZoneUtil.getZone() + "    **************************");
            BleManager.getInstance().writeControlMsg(BleCommandFactory
                    .syLockTime(nowTime, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
        }, 20);
    }

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
            Timber.e("onDisconnected 更新 %s:", mac);
            updateDevice("", mac);
        }

        @Override
        public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
            mHandler.removeMessages(MSG_MESSAGE_SEND_OUT_TIME);
            mHandler.sendEmptyMessage(MSG_MESSAGE_NEXT_SEND);
            Timber.e("onReceivedValue Value: %1s\n", ConvertUtils.bytes2HexString(value));
            if (value == null) {
                return;
            }
            BleBean bleBean = BleManager.getInstance().getBleBeanFromMac(mac);
            if (bleBean == null) {
                return;
            }
            if (null == bleBean.getOKBLEDeviceImp() || bleBean.getPwd1() == null) {
                LockMessageRes message = new LockMessageRes();
                message.setMessgaeType(MSG_LOCK_MESSAGE_BLE);//蓝牙消息
                message.setResultCode(LockMessageReplyErrCode.LOCK_BLE_ERR_CODE_BLE_VALUE_ERR);//值错误
                EventBus.getDefault().post(message);
                return;
            }
            byte[] pwd2Or3 = null;
            if (bleBean.isHavePwd2Or3()) {
                if (bleBean.getPwd3() == null) {
                    if (bleBean.getPwd2() != null) {
                        pwd2Or3 = bleBean.getPwd2();
                    }
                } else {
                    pwd2Or3 = bleBean.getPwd3();
                }
            }
            Timber.e("onReceivedValue pwd1: %1s\n", ConvertUtils.bytes2HexString(bleBean.getPwd1()));
            if (bleBean.getPwd2() == null) {
                Timber.e("onReceivedValue pwd2=null");
            } else {
                Timber.e("onReceivedValue pwd2: %1s\n", ConvertUtils.bytes2HexString(bleBean.getPwd2()));
            }
            if (bleBean.getPwd3() == null) {
                Timber.e("onReceivedValue pwd3=null");
            } else {
                Timber.e("onReceivedValue pwd3: %1s\n", ConvertUtils.bytes2HexString(bleBean.getPwd3()));
            }
            if (null != pwd2Or3) {
                Timber.e("onReceivedValue pwd2Or3: %1s\n", ConvertUtils.bytes2HexString(pwd2Or3));
            } else {
                Timber.e("onReceivedValue pwd2Or3=null");
            }
            Timber.e("onReceivedValue bleBean.isHavePwd2Or3():" + bleBean.isHavePwd2Or3());
            BleResultAnalysis.processReceivedData(bleBean.getOKBLEDeviceImp().getMacAddress(), value, bleBean.getPwd1(), bleBean.isHavePwd2Or3() ? pwd2Or3 : null, bleBean.getOKBLEDeviceImp().getBleScanResult());

            if (true) {
                return;
            }
            if (bleBean.getPwd2() == null) {
                Timber.e("onReceivedValue pwd2=null");
                BleResultAnalysis.processReceivedData(bleBean.getOKBLEDeviceImp().getMacAddress(), value, bleBean.getPwd1(),
                        null, bleBean.getOKBLEDeviceImp().getBleScanResult());
                return;
            }
            Timber.e("onReceivedValue pwd2: %1s\n", ConvertUtils.bytes2HexString(bleBean.getPwd2()));
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

        @Override
        public void onAddConnect(String mac) {
            //添加超时处理
            addBleOut(mac);
        }
    };

    /**
     * 发送电子围栏命令
     *
     * @param mac
     */
    private void senGeoFence(String mac) {
        BleDeviceLocal deviceLocal = getDevice(mac, mac);
        if (null != deviceLocal) {
            //1、是否开启电子围栏   2、是否从200米外进入
            if (deviceLocal.isOpenElectricFence()) {
                BleBean bleBean = getUserBleBean(mac);
                if (null != bleBean) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (bleBean == null) {
                            Timber.e("mOnBleDeviceListener bleBean == null");
                            return;
                        }
                        // TODO: 2021/4/7 抽离0x01
                        Timber.e("下发敲门开锁");
                        BleManager.getInstance().writeControlMsg(BleCommandFactory
                                .setKnockDoorAndUnlockTime(0x01, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
                    }, 200);

                }
            }
        }
    }


    //******************************************************蓝牙管理 end***********************************************************************
    // **********************************************************MQTT*******************************************************************************

    /**
     * 登录成功获取token后，MQTT连接
     */
    public void connectMQTT() {
        //MQTT 连接
        Timber.e("获取token后，MQTT连接");
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
            /**mqtt 连接完成"**/
            updateAllDevice();
            //获取用户绑定列表
            LockMessage lockMessage = new LockMessage();
            lockMessage.setMqtt_topic(MQttConstant.PUBLISH_TO_SERVER);
            lockMessage.setMqtt_message_code(MQttConstant.GET_ALL_BIND_DEVICE);
            lockMessage.setMessageType(2);
            lockMessage.setMqttMessage(MqttCommandFactory.getAllBindDevices(App.getInstance().getUserBean().getUid()));
            lockMessage.setSn("");
            lockMessage.setBytes(null);
            sendMessage(lockMessage);
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
                LogUtils.d("收到MQtt消息:" + payload + "---topic" + topic + "  messageID  " + message.getId());
                JSONObject jsonObject = new JSONObject(payload);
                int messageId = -1;
                String returnCode = "";
                String msgtype = "";
                try {
                    if (payload.contains("returnCode")) {
                        returnCode = (String) jsonObject.opt("returnCode");
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
                //判断处理回复超时
                //判断 消息类型
                if ("response".equals(msgtype)) {
                    LockMessage message1 = getCurrMessage();
                    if (null != message1 && null != message1.getMqttMessage() && messageId == message1.getMqttMessage().getId()) {
                        Timber.e("response: %1s\n", message1.getMqttMessage().getId());
                        mHandler.removeMessages(MSG_MESSAGE_SEND_OUT_TIME);
                        mHandler.sendEmptyMessage(MSG_MESSAGE_NEXT_SEND);
                    }
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
        public void onAddDevice(boolean isDelete, BleDeviceLocal bleDeviceLocal) {
            /*** 添加设备*/
            addDevice(isDelete, bleDeviceLocal);
        }

        @Override
        public void onOperationCallback(int what, WifiLockBaseResponseBean bean) {
            /*** 解析回调*/
            switch (what) {
                case LockMessageCode.MSG_LOCK_MESSAGE_WF_EVEN:
                    processRecord((WifiLockOperationEventBean) bean);
                    break;
            }
        }

        @Override
        public void onDoorSensorAlignmen(String wfId) {
            Timber.e("蓝牙广播开启成功回复");
            //WiFi模式下配网-》发送开启蓝牙广播命令-》
            //地理围栏-》发送开启蓝牙广播命令=>
            //门磁配置-》发送开启蓝牙广播命令=》
            BleDeviceLocal bleDeviceLoca = getDevice(wfId, wfId);
            if (null != bleDeviceLoca) {
                if (null != App.getInstance().getLockGeoFenceService()) {
                    App.getInstance().getLockGeoFenceService().updateLockCmdState(bleDeviceLoca.getEsn(), 1);
                }
                boolean bleState = onGetConnectedState(bleDeviceLoca.getMac());//当前蓝牙设的设备的状态
                if (bleState) {
                    Timber.e("已连接蓝牙，发送开门命令");
                    senGeoFence(bleDeviceLoca.getMac());
                } else {
                    Timber.e("未连接蓝牙，正在开始连接");
                    if (null != App.getInstance().getLockGeoFenceService()) {
                        App.getInstance().getLockGeoFenceService().addDeviceScan(bleDeviceLoca.getMac(), bleDeviceLoca.getSetElectricFenceTime());
                    }
                }
            }
        }

        @Override
        public void updateLockState(WifiLockOperationEventBean bean) {
            updateLockStatea(bean);
        }
    };

    /**
     * 更新锁状态
     *
     * @param bean
     */
    private void updateLockStatea(WifiLockOperationEventBean bean) {
        Timber.e("上锁的的状态：");
        WifiLockOperationEventBean.EventparamsBean eventparams = bean.getEventparams();
        String wfId = bean.getWfId();
        if (null != mDeviceLists) {
            for (int i = 0; i < mDeviceLists.size(); i++) {
                if (mDeviceLists.get(i).getEsn().equals(wfId) && eventparams != null) {
                    if (eventparams.getOperatingMode() == 1) {
                        mDeviceLists.get(i).setLockState(LocalState.LOCK_STATE_PRIVATE);
                    }
                    if (bean.getEventtype().equals("wifiState")) {
                        int state = bean.getState();
                        Timber.e("更新锁的上报状态：" + state);
                        // state=1;是WiFi模式  state=0;是未联网模式
                        BleBean bleBean = BleManager.getInstance().getBleBeanFromMac(mDeviceLists.get(i).getMac());
                        if (null != bleBean && null != bleBean.getOKBLEDeviceImp() && bleBean.getBleConning() == 2) {
                            Timber.e("更新锁的上报状态：蓝牙在线" + state);
                            //ble在线
                            if (state == 1) {
                                mDeviceLists.get(i).setConnectedType(LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE);
                                if (null != App.getInstance().getLockAppService()) {
                                    // 主动去断开蓝牙
                                    Timber.e("更新锁的上报状态：蓝牙在线，主动断开蓝牙" + state);
                                    //removeBleConnect(mDeviceLists.get(i).getMac());
                                }
                            } else {
                                mDeviceLists.get(i).setConnectedType(LocalState.DEVICE_CONNECT_TYPE_BLE);
                            }
                        } else {
                            //ble 断开
                            Timber.e("更新锁的上报状态：蓝牙断开，" + state);
                            if (state == 1) {
                                mDeviceLists.get(i).setConnectedType(LocalState.DEVICE_CONNECT_TYPE_WIFI);
                            } else {
                                mDeviceLists.get(i).setConnectedType(LocalState.DEVICE_CONNECT_TYPE_DIS);
                                if (null != App.getInstance().getLockAppService()) {
                                    // 主动去连接蓝牙
                                    Timber.e("更新锁的上报状态：蓝牙断开，主动连接：" + state);
                                    checkBleConnect(mDeviceLists.get(i).getMac());
                                }
                            }
                        }
                    }
                } else if (bean.getEventtype().equals("action")) {
                    mDeviceLists.get(i).setMute(eventparams.getVolume() == 1);
                    mDeviceLists.get(i).setOpenDoorSensor(eventparams.getDoorSensor() == 1);
                    mDeviceLists.get(i).setDuress(eventparams.getDuress() == 1);
                }
                if ((null != mDeviceLists.get(i).getMac() && mDeviceLists.get(i).getMac().equals(App.getInstance().getmCurrMac())) ||
                        (null != mDeviceLists.get(i).getEsn() && mDeviceLists.get(i).getEsn().equals(App.getInstance().getmCurrSn()))) {
                    Timber.e("app service setDoorState set BleDeviceLocal");
                    App.getInstance().setBleDeviceLocal(mDeviceLists.get(i));
                }
                break;
            }
        }
    }


    private void processRecord(@NotNull WifiLockOperationEventBean bean) {
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
                setLockState(checkDeviceList(bean.getWfId(), bean.getWfId()), LocalState.LOCK_STATE_CLOSE);
            } else if (eventCode == 2 || eventCode == 0x09 || eventCode == 0x0E) {
                // 开锁
                setLockState(checkDeviceList(bean.getWfId(), bean.getWfId()), LocalState.LOCK_STATE_OPEN);
            }
        } else if (bean.getEventparams().getEventType() == 3) {
           /* int eventCode = bean.getEventparams().getEventCode();
            if (eventCode == 5) {
                // 上锁
                setLockState(getPositionFromWifiId(bean.getWfId()), LocalState.LOCK_STATE_PRIVATE);
            }*/
        } else if (bean.getEventparams().getEventType() == 4) {
            // 传感器上报，门磁
            if (bean.getEventparams().getEventCode() == 1) {
                // 门磁开门
                setDoorState(checkDeviceList(bean.getWfId(), bean.getWfId()), LocalState.DOOR_SENSOR_OPEN);
            } else if (bean.getEventparams().getEventCode() == 2) {
                // 门磁关门
                setDoorState(checkDeviceList(bean.getWfId(), bean.getWfId()), LocalState.DOOR_SENSOR_CLOSE);
            } else if (bean.getEventparams().getEventCode() == 3) {
                // 门磁异常
                Timber.e("processRecord 门磁异常");
                setDoorState(checkDeviceList(bean.getWfId(), bean.getWfId()), LocalState.DOOR_SENSOR_EXCEPTION);
            }
        }
    }

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
        //MQTT 分用户信息命令、设备命令

        if (message.getMessageType() == 2) {
            sendMessage(message, LOCK_MESSAGE_MQTT);
        } else if (message.getMessageType() == 3) {
            sendMessage(message, LOCK_MESSAGE_BLE);
        }
        if (true) {
            return;
        }
        BleDeviceLocal bleDeviceLocal = getDevice(sn, mac);
        if (("".equals(sn) || null == sn) && null != message.getMqttMessage()) {
            sendMessage(message, LOCK_MESSAGE_MQTT);
        } else {
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

    }

    /**
     * 发送数据接口 ，指定发送通道
     *
     * @param message
     * @param MessageType
     */
    public void sendMessage(LockMessage message, int MessageType) {
        Timber.e("msg type:%s", MessageType + "");
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

    private LockMessage nextMessage() {
        LockMessage message = null;
        messageLock.lock();
        if (lockMessageList.size() > 0) {
            message = new LockMessage(lockMessageList.get(0));
            lockMessageList.remove(0);
        }
        messageLock.unlock();
        nextSendMessage();
        return message;
    }

    private LockMessage getCurrMessage() {
        LockMessage lockMessage = null;
        lock.lock();
        if (lockMessageList.size() > 0) {
            lockMessage = new LockMessage(lockMessageList.get(0));
        }
        lock.unlock();
        return lockMessage;
    }

    private LockMessage outTimeNextMessage() {
        //
        return nextMessage();
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
            //addSendMessage(lockMessageList.get(0));
            if (LOCK_MESSAGE_MQTT == lockMessageList.get(0).getMessageType()) {
                sendMessageMQTT(lockMessageList.get(0));
            } else if (LOCK_MESSAGE_BLE == lockMessageList.get(0).getMessageType()) {
                mHandler.obtainMessage(MSG_MESSAGE_SEND_BLE, lockMessageList.get(0)).sendToTarget();
                //sendMessageBle(lockMessageList.get(0));
            }
        }
    }

    public void addSendMessage(LockMessage message) {
        if (null != sendMqttThread) {
            sendMqttThread.addMessage(message);
        }
    }

    private SendMqttThread sendMqttThread;

    private void startSendThread() {
       /* stopSendThread();
        if (null == sendMqttThread) {
            sendMqttThread = new SendMqttThread();
        }
        sendMqttThread.start();*/
    }

    private void stopSendThread() {
        if (null != sendMqttThread) {
            sendMqttThread.isRun = false;
            sendMqttThread.interrupt();
            sendMqttThread = null;
        }
    }

    public class SendMqttThread extends Thread {
        private boolean isRun = true;
        private List<LockMessage> sendMqttList = new ArrayList<>();
        private Lock sendLock = new ReentrantLock();

        public void addMessage(LockMessage message) {
            sendLock.lock();
            sendMqttList.add(message);
            sendLock.unlock();
        }

        public void setRun(boolean run) {
            isRun = run;
        }

        @Override
        public void run() {
            while (isRun) {
                sendLock.lock();
                if (sendMqttList.size() > 0) {
                    if (LOCK_MESSAGE_MQTT == sendMqttList.get(0).getMessageType()) {
                        sendMessageMQTT(sendMqttList.get(0));
                    } else if (LOCK_MESSAGE_BLE == sendMqttList.get(0).getMessageType()) {
                        mHandler.obtainMessage(MSG_MESSAGE_SEND_BLE, sendMqttList.get(0)).sendToTarget();
                        //sendMessageBle(lockMessageList.get(0));
                    }
                    sendMqttList.remove(0);
                }
                sendLock.unlock();
            }
        }
    }

    /***
     * 发送 MQTT
     * @param message
     */
    // Disposable mAppMqttMessage = null;
    private void sendMessageMQTT(LockMessage message) {
        message.addSendFre();
      /* MQTTManager.getInstance().mqttPublish(message.getMqtt_topic(),
                message.getMqttMessage())
                .subscribe(mqttData -> {
                }, Timber::e);
       */
        mHandler.sendEmptyMessageDelayed(MSG_MESSAGE_SEND_OUT_TIME, 3000);

        try {
            MQTTManager.getInstance().mqttPublish(message.getMqtt_topic(),
                    message.getMqttMessage());
        } catch (Exception e) {
            pushErrMessage(message.getMqttMessage().getId() + "", message.getMqtt_message_code());
            e.printStackTrace();
        }

     /*   mAppMqttMessage = MQTTManager.getInstance().mqttPublish(message.getMqtt_topic(),
                message.getMqttMessage())
                .timeout(3, TimeUnit.SECONDS)
                .subscribe(mqttData -> {
                    if (null != mAppMqttMessage) {
                        toDisposable(mAppMqttMessage);
                        mAppMqttMessage = null;
                    }
                }, e -> {
                    pushErrMessage(message.getMqtt_message_code());
                    if (null != mAppMqttMessage) {
                        toDisposable(mAppMqttMessage);
                        mAppMqttMessage = null;
                    }
                    Timber.e(e);


                });
*/

    }

    public void toDisposable(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private void pushErrMessage(String msgId, String messageCode) {
        if (MQttConstant.GET_ALL_BIND_DEVICE.equals(messageCode)) {
            //获取所有绑定的设备接口

        } else if (MQttConstant.SET_MAGNETIC.equals(messageCode)) {
            // 设置门磁
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_SET_MAGNETIC, LockMessageCode.MSG_LOCK_MESSAGE_SET_MAGNETIC, null);
        } else if (MQttConstant.APP_ROACH_OPEN.equals(messageCode)) {
            // 无感开门
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_APP_ROACH_OPEN, LockMessageCode.MSG_LOCK_MESSAGE_APP_ROACH_OPEN, null);
        } else if (MQttConstant.CLOSE_WIFI.equals(messageCode)) {
            // 关闭wifi
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CLOSE_WIFI, LockMessageCode.MSG_LOCK_MESSAGE_CLOSE_WIFI, null);
        } else if (MQttConstant.SET_LOCK.equals(messageCode)) {
            // 开关门指令
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK, null);
        } else if (MQttConstant.CREATE_PWD.equals(messageCode)) {
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CREATE_PWD, LockMessageCode.MSG_LOCK_MESSAGE_CREATE_PWD, null);
        } else if (MQttConstant.ADD_PWD.equals(messageCode)) {
            // 秘钥属性添加
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_ADD_PWD, LockMessageCode.MSG_LOCK_MESSAGE_ADD_PWD, null);
        } else if (MQttConstant.UPDATE_PWD.equals(messageCode)) {
            // 秘钥属性修改
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_UPDATE_PWD, LockMessageCode.MSG_LOCK_MESSAGE_UPDATE_PWD, null);
        } else if (MQttConstant.REMOVE_PWD.equals(messageCode)) {
            // 秘钥属性删除
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_REMOVE_PWD, LockMessageCode.MSG_LOCK_MESSAGE_REMOVE_PWD, null);
        } else if (MQttConstant.GATEWAY_STATE.equals(messageCode)) {
            // 获取网关状态
            // postMessage(LockMessageCode.MSG_LOCK_MESSAGE_GATEWAY_STATE, );
        } else if (MQttConstant.SET_LOCK_ATTR.equals(messageCode)) {
            // 设置门锁属性
            Class t = MqttCommandFactory.sendMessage(msgId, null, 1);
            if (t.getName().equals(WifiLockSetLockAttrAutoRspBean.class.getName())) {
                postMessage(LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTO, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTO, null);
            } else if (t.getName().equals(WifiLockSetLockAttrAutoTimeRspBean.class.getName())) {
                postMessage(LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTOTIME, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTOTIME, null);
            } else if (t.getName().equals(WifiLockSetLockAttrDuressRspBean.class.getName())) {
                postMessage(LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRDURES, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRDURES, null);
            } else if (t.getName().equals(WifiLockSetLockAttrSensitivityRspBean.class.getName())) {
                postMessage(LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRSENSITIVITY, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRSENSITIVITY, null);
            } else if (t.getName().equals(WifiLockSetLockAttrVolumeRspBean.class.getName())) {
                postMessage(LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRVOLUME, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRVOLUME, null);
            }
            MqttCommandFactory.sendMessage(msgId, null, 3);

        } else if (MQttConstant.WF_EVENT.equals(messageCode)) {
            // 操作事件
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_WF_EVEN, LockMessageCode.MSG_LOCK_MESSAGE_WF_EVEN, null);
        } else if (MQttConstant.RECORD.equals(messageCode)) {
            // 记录
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_RECORD, LockMessageCode.MSG_LOCK_MESSAGE_RECORD, null);
        }
    }

    public void postMessage(int resultCode, int messageCode, WifiLockBaseResponseBean bean) {
        LockMessageRes messageRes = new LockMessageRes();
        messageRes.setResultCode(resultCode);//操作完成
        messageRes.setMessgaeType(LockMessageCode.MSG_LOCK_MESSAGE_MQTT);
        messageRes.setMessageCode(messageCode);
        messageRes.setWifiLockBaseResponseBean(bean);
        EventBus.getDefault().post(messageRes);
    }


    /**
     * 发送ble
     *
     * @param message
     */
    private void sendMessageBle(LockMessage message) {
        message.addSendFre();
        BleManager.getInstance().write(message.getBleChr(), message.getMac(), message.getBytes());
        mHandler.sendEmptyMessageDelayed(MSG_MESSAGE_SEND_OUT_TIME, 500);
    }

    private DoorState doorState;

    public void addConnect(String wfid) {
        if (null == doorState) {
            doorState = new DoorState();
            doorState.isRun = true;
            doorState.start();
        }
        doorState.addConnect(wfid);
    }

    public void stopCibDoor() {
        if (null != doorState) {
            doorState.isRun = false;
            doorState.interrupt();
            doorState = null;
        }
    }

    private class DoorState extends Thread {
        private Lock lock = new ReentrantLock();
        private boolean isRun = true;
        private List<String> connectList = new ArrayList<>();

        public void addConnect(String mac) {
            lock.lock();
            if (connectList.indexOf(mac) > 0) {
                return;
            }
            connectList.add(mac);
            lock.unlock();
        }

        private void connect(BleDeviceLocal bleDeviceLoca) {

            if (null != bleDeviceLoca) {
                addDevice(false, bleDeviceLoca);
            }
        }


        @Override
        public void run() {
            while (isRun) {
                lock.lock();
                for (int i = 0; i < connectList.size(); i++) {
                    BleDeviceLocal bleDeviceLoca = getDevice(connectList.get(i), connectList.get(i));
                    if (null == bleDeviceLoca) {
                        continue;
                    }
                    BleBean bleBean = getUserBleBean(bleDeviceLoca.getMac());
                    if (null == bleBean) {
                        //连接
                        connect(bleDeviceLoca);
                    } else {
                        if (bleBean.getBleConning() == 0 || bleBean.getBleConning() == 3) {
                            //连接
                            connect(bleDeviceLoca);
                        } else {
                            connectList.remove(i);
                        }
                    }
                }
                lock.unlock();
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean ping() {
//        try {
//            Runtime runtime = Runtime.getRuntime();
//            Process exec = runtime.exec("ping -c 3 www.baidu.com");
//            int i = exec.waitFor();
//            return i == 0;
//        } catch (InterruptedException | IOException e) {
//            e.printStackTrace();
//        }
//        return false;

        return NetworkUtils.isConnected();
    }
}
