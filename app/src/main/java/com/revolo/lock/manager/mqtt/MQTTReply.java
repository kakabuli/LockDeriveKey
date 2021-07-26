package com.revolo.lock.manager.mqtt;

import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.revolo.lock.App;
import com.revolo.lock.LocalState;
import com.revolo.lock.manager.LockAppService;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.manager.ble.BleManager;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.eventbean.WifiLockOperationEventBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockAddPwdAttrResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockAddPwdRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockApproachOpenResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockBaseResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockCloseWifiResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockDoorOptResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockGetAllBindDeviceRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockRemovePasswordResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrAutoRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrAutoTimeRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrDuressRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrSensitivityRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrVolumeRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetMagneticResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockUpdatePasswordResponseBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import timber.log.Timber;

public class MQTTReply {
    private static MQTTReply mqttReply;
    private MQTTDataLinstener mqttDataLinstener;

    public static MQTTReply getInstance() {
        if (null == mqttReply) {
            mqttReply = new MQTTReply();
        }
        return mqttReply;
    }

    public void setMqttDataLinstener(MQTTDataLinstener dataLinstener) {
        mqttDataLinstener = dataLinstener;
    }

    public void onReply(MqttData mqttData) {
        if (TextUtils.isEmpty(mqttData.getFunc())) {
            return;
        }
        if (MQttConstant.GET_ALL_BIND_DEVICE.equals(mqttData.getFunc())) {
            //获取所有绑定的设备接口
            WifiLockGetAllBindDeviceRspBean bean = null;
            try {
                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockGetAllBindDeviceRspBean.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (bean == null || TextUtils.isEmpty(bean.getMsgtype()) || !bean.getMsgtype().equals("response")) {
                Timber.e("WifiLockGetAllBindDeviceRspBean..getData().getWifiList().isEmpty()");
                return;
            }
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //获取当前本地的设备列表
            List<BleDeviceLocal> locals = AppDatabase.getInstance(App.getInstance()).bleDeviceDao().findBleDevicesFromUserIdByCreateTimeDesc(App.getInstance().getUser().getAdminUid());
            if (bean.getData() == null || bean.getData().getWifiList() == null || bean.getData().getWifiList().isEmpty()) {
                //删除本地数据
                Timber.e("删除本地设备数据");
                if (null != locals && locals.size() > 0) {
                    AppDatabase
                            .getInstance(App.getInstance().getApplicationContext())
                            .bleDeviceDao().delete(locals);
                }
                if (null != mqttDataLinstener) {
                    mqttDataLinstener.onAddDevice(true, null);
                }
                return;
            } else {
                //从服务端返回的设备列表
                List<WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean> wifilists = bean.getData().getWifiList();
                //先判断当前本地的设备是否在在服务器端返回的列表中
                if (locals.size() == 0) {
                    Timber.e("当前本地不存在设备数据，服务端设备个数为:%s", wifilists.size() + "");
                    //当前不存在本地设备
                    for (WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean : wifilists) {
                        // TODO: 2021/2/26 后期再考虑是否需要多条件合并查询
                        BleDeviceLocal bleDeviceLocal = new BleDeviceLocal();
                        long id = AppDatabase.getInstance(App.getInstance().getApplicationContext()).bleDeviceDao().insert(bleDeviceLocal);
                        Timber.e("crate DataFromNet bleDeviceLocal == %s:", id + "");
                        bleDeviceLocal.setId(id);
                        bleDeviceLocal = createDeviceToLocal(wifiListBean, bleDeviceLocal);
                        AppDatabase.getInstance(App.getInstance().getApplicationContext()).bleDeviceDao().update(bleDeviceLocal);
                        if (null != mqttDataLinstener) {
                            mqttDataLinstener.onAddDevice(false, bleDeviceLocal);
                        }
                    }

                } else {
                    //当前存在本地设备
                    Timber.e("当前本地存在设备数据，个数为:%s", locals.size() + "");
                    for (int i = 0; i < locals.size(); i++) {
                        boolean isExistence = false;
                        BleDeviceLocal deviceLocal = locals.get(i);
                        for (WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean : wifilists) {
                            if (deviceLocal.getEsn().equals(wifiListBean.getWifiSN())) {
                                isExistence = true;
                                deviceLocal = createDeviceToLocal(wifiListBean, deviceLocal);
                                Timber.e("当前本地存在设备数据，个数为:%s,更新：%s", locals.size() + "", deviceLocal.toString());
                                AppDatabase.getInstance(App.getInstance().getApplicationContext()).bleDeviceDao().update(deviceLocal);
                                if (null != mqttDataLinstener) {
                                    mqttDataLinstener.onAddDevice(false, deviceLocal);
                                }
                                break;
                            }
                        }
                        if (!isExistence) {
                            Timber.e("服务端不存在此设备数据，删除:%s", deviceLocal.toString());
                            AppDatabase.getInstance(App.getInstance().getApplicationContext()).bleDeviceDao().delete(deviceLocal);
                            if (null != mqttDataLinstener) {
                                mqttDataLinstener.onAddDevice(true, deviceLocal);
                            }
                        }
                    }
                    for (WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean : wifilists) {
                        boolean isExistence = false;
                        for (BleDeviceLocal bleDeviceLocal : locals) {
                            if (bleDeviceLocal.getEsn().equals(wifiListBean.getWifiSN())) {
                                isExistence = true;
                                break;
                            }
                        }
                        if (!isExistence) {

                            BleDeviceLocal bleDeviceLocal = new BleDeviceLocal();
                            long id = AppDatabase.getInstance(App.getInstance().getApplicationContext()).bleDeviceDao().insert(bleDeviceLocal);
                            Timber.e("crate DataFromNet bleDeviceLocal == %s:", id + "");
                            bleDeviceLocal.setId(id);
                            bleDeviceLocal = createDeviceToLocal(wifiListBean, bleDeviceLocal);
                            Timber.e("本地不存在此设备数据，添加:%s", bleDeviceLocal.toString());
                            AppDatabase.getInstance(App.getInstance().getApplicationContext()).bleDeviceDao().update(bleDeviceLocal);
                            if (null != mqttDataLinstener) {
                                mqttDataLinstener.onAddDevice(false, bleDeviceLocal);
                            }
                        }
                    }
                }
            }
        } else if (MQttConstant.SET_MAGNETIC.equals(mqttData.getFunc())) {
            // 设置门磁
            WifiLockSetMagneticResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetMagneticResponseBean.class);
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_SET_MAGNETIC, bean);
        } else if (MQttConstant.APP_ROACH_OPEN.equals(mqttData.getFunc())) {
            // 无感开门
            WifiLockApproachOpenResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockApproachOpenResponseBean.class);
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_APP_ROACH_OPEN, bean);
            if (null != mqttDataLinstener) {
                mqttDataLinstener.onDoorSensorAlignmen(bean.getWfId());
            }
        } else if (MQttConstant.CLOSE_WIFI.equals(mqttData.getFunc())) {
            // 关闭wifi
            WifiLockCloseWifiResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockCloseWifiResponseBean.class);
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_CLOSE_WIFI, bean);
        } else if (MQttConstant.SET_LOCK.equals(mqttData.getFunc())) {
            // 开关门指令
            WifiLockDoorOptResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockDoorOptResponseBean.class);
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK, bean);
        } else if (MQttConstant.CREATE_PWD.equals(mqttData.getFunc())) {
            WifiLockAddPwdRspBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockAddPwdRspBean.class);
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_CREATE_PWD, bean);
        } else if (MQttConstant.ADD_PWD.equals(mqttData.getFunc())) {
            // 秘钥属性添加
            WifiLockAddPwdAttrResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockAddPwdAttrResponseBean.class);
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_ADD_PWD, bean);
        } else if (MQttConstant.UPDATE_PWD.equals(mqttData.getFunc())) {
            // 秘钥属性修改
            WifiLockUpdatePasswordResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockUpdatePasswordResponseBean.class);
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_UPDATE_PWD, bean);
        } else if (MQttConstant.REMOVE_PWD.equals(mqttData.getFunc())) {
            // 秘钥属性删除
            WifiLockRemovePasswordResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockRemovePasswordResponseBean.class);
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_REMOVE_PWD, bean);
        } else if (MQttConstant.GATEWAY_STATE.equals(mqttData.getFunc())) {
            // 获取网关状态
        } else if (MQttConstant.SET_LOCK_ATTR.equals(mqttData.getFunc())) {
            // 设置门锁属性
            Class t = MqttCommandFactory.sendMessage(mqttData.getMessageId() + "", null, 1);
            if (null != t) {
                if (t.getName().equals(WifiLockSetLockAttrAutoRspBean.class.getName())) {
                    WifiLockSetLockAttrAutoRspBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrAutoRspBean.class);
                    postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTO, bean);
                } else if (t.getName().equals(WifiLockSetLockAttrAutoTimeRspBean.class.getName())) {
                    WifiLockSetLockAttrAutoTimeRspBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrAutoTimeRspBean.class);
                    postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTOTIME, bean);
                } else if (t.getName().equals(WifiLockSetLockAttrDuressRspBean.class.getName())) {
                    WifiLockSetLockAttrDuressRspBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrDuressRspBean.class);
                    postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRDURES, bean);
                } else if (t.getName().equals(WifiLockSetLockAttrSensitivityRspBean.class.getName())) {
                    WifiLockSetLockAttrSensitivityRspBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrSensitivityRspBean.class);
                    postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRSENSITIVITY, bean);
                } else if (t.getName().equals(WifiLockSetLockAttrVolumeRspBean.class.getName())) {
                    WifiLockSetLockAttrVolumeRspBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrVolumeRspBean.class);
                    postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRVOLUME, bean);
                }
            }
            MqttCommandFactory.sendMessage(mqttData.getMessageId() + "", null, 3);


        } else if (MQttConstant.WF_EVENT.equals(mqttData.getFunc())) {
            // 操作事件
            WifiLockOperationEventBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockOperationEventBean.class);
            if (null != mqttDataLinstener) {
                mqttDataLinstener.onOperationCallback(LockMessageCode.MSG_LOCK_MESSAGE_WF_EVEN, bean);
            }
            updateLockState(bean);
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_WF_EVEN, bean);

        } else if (MQttConstant.RECORD.equals(mqttData.getFunc())) {
            // 记录
            postMessage(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS, LockMessageCode.MSG_LOCK_MESSAGE_RECORD, null);
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

    private void updateLockState(WifiLockOperationEventBean bean) {
        WifiLockOperationEventBean.EventparamsBean eventparams = bean.getEventparams();
        String wfId = bean.getWfId();
        List<BleDeviceLocal> deviceLists = App.getInstance().getDeviceLists();
        for (BleDeviceLocal bleDeviceLocal : deviceLists) {
            if (bleDeviceLocal.getEsn().equals(wfId) && eventparams != null) {
                if (eventparams.getOperatingMode() == 1) {
                    bleDeviceLocal.setLockState(LocalState.LOCK_STATE_PRIVATE);
                }
                if (bean.getEventtype().equals("wifiState")) {
                    int state = bean.getState();
                    switch (bleDeviceLocal.getConnectedType()) {
                        case LocalState.DEVICE_CONNECT_TYPE_BLE: // 之前是蓝牙
                            if (state == 1) {
                                bleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE);
                            }
                            break;
                        case LocalState.DEVICE_CONNECT_TYPE_DIS: // 之前是断线
                            if (state == 1) {
                                bleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_WIFI);
                            }
                            break;
                        case LocalState.DEVICE_CONNECT_TYPE_WIFI: // 之前是wifi
                            if (state == 0) {
                                bleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_DIS);
                                if (null != App.getInstance().getLockAppService()) {
                                    // 主动去连接蓝牙
                                    App.getInstance().getLockAppService().checkBleConnect(bleDeviceLocal.getMac());
                                }
                            }
                            break;
                        case LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE: //之前是wifi ble双链接
                            if (state == 0) {
                                bleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_BLE);
                            }
                            break;
                    }
                } else if (bean.getEventtype().equals("action")) {
                    bleDeviceLocal.setMute(eventparams.getVolume() == 1);
                    bleDeviceLocal.setOpenDoorSensor(eventparams.getDoorSensor() == 1);
                    bleDeviceLocal.setDuress(eventparams.getDuress() == 1);
                }
            }
        }
    }

    /**
     * 从服务端获取当前的所有的设备
     *
     * @param wifiListBean
     * @return
     */
    private BleDeviceLocal createDeviceToLocal(WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean, BleDeviceLocal bleDeviceLocal) {

        // TODO: 2021/3/16 存储数据
        if (!TextUtils.isEmpty(wifiListBean.getRandomCode()))
            bleDeviceLocal.setRandomCode(wifiListBean.getRandomCode());

        if (!TextUtils.isEmpty(wifiListBean.getLockNickname()))
            bleDeviceLocal.setName(wifiListBean.getLockNickname());
        //门磁状态
        if (wifiListBean.getMagneticStatus() == 1) {
            bleDeviceLocal.setDoorSensor(LocalState.DOOR_SENSOR_OPEN);
        } else if (wifiListBean.getMagneticStatus() == 2) {
            bleDeviceLocal.setDoorSensor(LocalState.DOOR_SENSOR_CLOSE);
        } else if (wifiListBean.getMagneticStatus() == 3) {
            bleDeviceLocal.setDoorSensor(LocalState.DOOR_SENSOR_EXCEPTION);
        }
        //启用门磁
        bleDeviceLocal.setOpenDoorSensor(wifiListBean.getDoorSensor() == 1);
        bleDeviceLocal.setSetAutoLockTime(wifiListBean.getAutoLockTime());
        // TODO: 2021/3/18 修改为从服务器获取数据
        if (!TextUtils.isEmpty(wifiListBean.getWifiStatus())) {
            if (wifiListBean.getWifiStatus().equals("1")) {
                bleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_WIFI);
            } else {
                bleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_DIS);
                if (null != App.getInstance().getLockAppService()) { // 主动去连接蓝牙
                    App.getInstance().getLockAppService().checkBleConnect(wifiListBean.getBleMac());
                }
            }
        }
        bleDeviceLocal.setLockPower(wifiListBean.getPower());
        //锁的wifi模式下开关状态已服务器为准
        Timber.e("设备 服务器 lockState： %s", wifiListBean.getOpenStatus() + "");
        Timber.e("设备 本地 lockState： %s", bleDeviceLocal.getLockState() + "");
        Timber.e("设备 connected： %s", bleDeviceLocal.getConnectedType() + "");
        if (bleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            int eventCode = wifiListBean.getOpenStatus();
            if (eventCode == 0x01 || eventCode == 0x08 || eventCode == 0x0D || eventCode == 0x0A) {
                // 上锁
                bleDeviceLocal.setLockState(LocalState.LOCK_STATE_CLOSE);
            } else if (eventCode == 2 || eventCode == 0x09 || eventCode == 0x0E) {
                // 开锁
                bleDeviceLocal.setLockState(LocalState.LOCK_STATE_OPEN);
            }
            //     bleDeviceLocal.setLockState(wifiListBean.getOpenStatus());
        }

        if (wifiListBean.getOperatingMode() == 1) {
            bleDeviceLocal.setLockState(LocalState.LOCK_STATE_PRIVATE);
        }
        Timber.e("设备 更新后 lockState： %s", bleDeviceLocal.getLockState() + "");

        bleDeviceLocal.setMute(wifiListBean.getVolume() == 1);
        if (null != wifiListBean.getTimeZone() && !"".equals(wifiListBean.getTimeZone()))
            bleDeviceLocal.setTimeZone(wifiListBean.getTimeZone());
        else Timber.e("set time zone=null");

//        bleDeviceLocal.setSetElectricFenceSensitivity();
//        bleDeviceLocal.setSetElectricFenceTime();
//        bleDeviceLocal.setDetectionLock();
        bleDeviceLocal.setAutoLock(wifiListBean.getAmMode() == 0);

        bleDeviceLocal.setDuress(wifiListBean.getDuress() == 1);
        bleDeviceLocal.setDuressEmail(wifiListBean.getDuressEmail());

        if (!TextUtils.isEmpty(wifiListBean.getWifiName()))
            bleDeviceLocal.setConnectedWifiName(wifiListBean.getWifiName());

        bleDeviceLocal.setCreateTime(wifiListBean.getCreateTime());

        if (!TextUtils.isEmpty(wifiListBean.getPassword2()))
            bleDeviceLocal.setPwd2(wifiListBean.getPassword2());

        bleDeviceLocal.setDetectionLock(true);

        if (!TextUtils.isEmpty(wifiListBean.getPassword1()))
            bleDeviceLocal.setPwd1(wifiListBean.getPassword1());

        if (!TextUtils.isEmpty(wifiListBean.getBleMac()))
            bleDeviceLocal.setMac(wifiListBean.getBleMac());

        if (!TextUtils.isEmpty(wifiListBean.getWifiSN()))
            bleDeviceLocal.setEsn(wifiListBean.getWifiSN());

        if (!TextUtils.isEmpty(wifiListBean.getFunctionSet()))
            bleDeviceLocal.setFunctionSet(wifiListBean.getFunctionSet());

        bleDeviceLocal.setUserId(App.getInstance().getUser().getAdminUid());

        if (!TextUtils.isEmpty(wifiListBean.getModel()))
            bleDeviceLocal.setType(wifiListBean.getModel());

        String firmwareVer = wifiListBean.getLockFirmwareVersion();
        if (!TextUtils.isEmpty(firmwareVer)) {
            bleDeviceLocal.setLockVer(firmwareVer);
        }
        bleDeviceLocal.setShareUserType(wifiListBean.getShareUserType());
        bleDeviceLocal.setIsAdmin(wifiListBean.getIsAdmin());

        String wifiVer = wifiListBean.getWifiVersion();
        if (!TextUtils.isEmpty(wifiVer)) {
            bleDeviceLocal.setWifiVer(wifiVer);
        }
        Timber.d("wifiESN: %1s, 电量：%2d", wifiListBean.getWifiSN(), wifiListBean.getPower());
        if (!TextUtils.isEmpty(wifiListBean.getLockNickname()))
            bleDeviceLocal.setLockPower(wifiListBean.getPower());
        // 0 锁端wifi没有与服务器连接   1 锁端wifi与服务器连接成功
        Timber.d("wifi 连接状态: %1s", wifiListBean.getWifiStatus());
        //地理围栏
        //地理围栏是否开启
        Timber.e("电子围栏状态：" + (wifiListBean.getElecFence() == 1 ? "true" : "false"));
        bleDeviceLocal.setOpenElectricFence(wifiListBean.getElecFence() == 1);
        //地理围栏经纬度
        bleDeviceLocal.setLatitude(Double.parseDouble(wifiListBean.getLatitude()));
        bleDeviceLocal.setLongitude(Double.parseDouble(wifiListBean.getLongitude()));
        //地理围栏时间
        bleDeviceLocal.setSetElectricFenceTime(wifiListBean.getElecFenceTime());
        bleDeviceLocal.setSetElectricFenceSensitivity(wifiListBean.getElecFenceSensitivity());
        //地理围栏 从200米外到内
        bleDeviceLocal.setLockElecFenceState(wifiListBean.getElecFenceState() == 0);

        return bleDeviceLocal;
    }
}
