package com.revolo.lock.manager.mqtt;

import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.revolo.lock.App;
import com.revolo.lock.LocalState;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockApproachOpenResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockCloseWifiResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockGetAllBindDeviceRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockRemovePasswordResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrVolumeRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetMagneticResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockUpdatePasswordResponseBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

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
            if (bean == null || TextUtils.isEmpty(bean.getMsgtype()) || !bean.getMsgtype().equals("response") ||
                    bean.getData() == null || bean.getData().getWifiList() == null || bean.getData().getWifiList().isEmpty()) {
                Timber.e("WifiLockGetAllBindDeviceRspBean..getData().getWifiList().isEmpty()");
                return;
            }
            List<WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean> wifilists = bean.getData().getWifiList();
            for (WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean : wifilists) {
                // TODO: 2021/2/26 后期再考虑是否需要多条件合并查询
                BleDeviceLocal bleDeviceLocal = AppDatabase
                        .getInstance(App.getInstance().getApplicationContext()).bleDeviceDao().findBleDeviceFromEsnAndUserId(
                                wifiListBean.getWifiSN(),
                                App.getInstance().getUser().getId());
                if (bleDeviceLocal == null) {
                    Timber.e("updateDataFromNet bleDeviceLocal == null");
                    bleDeviceLocal = createDeviceToLocal(wifiListBean);
                }
                bleDeviceLocal.setName(wifiListBean.getLockNickname());
                String firmwareVer = wifiListBean.getLockFirmwareVersion();
                if (!TextUtils.isEmpty(firmwareVer)) {
                    bleDeviceLocal.setLockVer(firmwareVer);
                }
                String wifiVer = wifiListBean.getWifiVersion();
                if (!TextUtils.isEmpty(wifiVer)) {
                    bleDeviceLocal.setWifiVer(wifiVer);
                }
                Timber.d("wifiESN: %1s, 电量：%2d", wifiListBean.getWifiSN(), wifiListBean.getPower());
                bleDeviceLocal.setLockPower(wifiListBean.getPower());
                // 0 锁端wifi没有与服务器连接   1 锁端wifi与服务器连接成功
                boolean isWifiConnected = (wifiListBean.getWifiStatus().equals("1"));
                bleDeviceLocal.setConnectedType(isWifiConnected ?
                        LocalState.DEVICE_CONNECT_TYPE_WIFI : LocalState.DEVICE_CONNECT_TYPE_BLE);
                bleDeviceLocal.setRandomCode(wifiListBean.getRandomCode());
                AppDatabase.getInstance(App.getInstance().getApplicationContext()).bleDeviceDao().update(bleDeviceLocal);
                if (null != mqttDataLinstener) {
                    mqttDataLinstener.onAddDevice(bleDeviceLocal);
                }
            }
        } else if (MQttConstant.SET_MAGNETIC.equals(mqttData.getFunc())) {
            // 设置门磁
            WifiLockSetMagneticResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetMagneticResponseBean.class);
        } else if (MQttConstant.APP_ROACH_OPEN.equals(mqttData.getFunc())) {
            // 无感开门
            WifiLockApproachOpenResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockApproachOpenResponseBean.class);
        } else if (MQttConstant.CLOSE_WIFI.equals(mqttData.getFunc())) {
            // 关闭wifi
            WifiLockCloseWifiResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockCloseWifiResponseBean.class);
        } else if (MQttConstant.SET_LOCK.equals(mqttData.getFunc())) {
            // 开关门指令
        } else if (MQttConstant.CREATE_PWD.equals(mqttData.getFunc())) {
        } else if (MQttConstant.ADD_PWD.equals(mqttData.getFunc())) {
            // 秘钥属性添加
        } else if (MQttConstant.UPDATE_PWD.equals(mqttData.getFunc())) {
            // 秘钥属性修改
            WifiLockUpdatePasswordResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockUpdatePasswordResponseBean.class);
        } else if (MQttConstant.REMOVE_PWD.equals(mqttData.getFunc())) {
            // 秘钥属性删除
            WifiLockRemovePasswordResponseBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockRemovePasswordResponseBean.class);
        } else if (MQttConstant.GATEWAY_STATE.equals(mqttData.getFunc())) {
            // 获取网关状态
        } else if (MQttConstant.SET_LOCK_ATTR.equals(mqttData.getFunc())) {
            // 设置门锁属性
            WifiLockSetLockAttrVolumeRspBean bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrVolumeRspBean.class);
        } else if (MQttConstant.WF_EVENT.equals(mqttData.getFunc())) {
            // 操作事件
        } else if (MQttConstant.RECORD.equals(mqttData.getFunc())) {
            // 记录
        }
    }

    /**
     * 从服务端获取当前的所有的设备
     *
     * @param wifiListBean
     * @return
     */
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
        long id = AppDatabase.getInstance(App.getInstance().getApplicationContext()).bleDeviceDao().insert(bleDeviceLocal);
        bleDeviceLocal.setId(id);
        return bleDeviceLocal;
    }
}
