package com.revolo.lock.manager;

import com.revolo.lock.App;
import com.revolo.lock.LocalState;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockGetAllBindDeviceRspBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

/**
 * 设备处理工具类
 */
public class DeviceContextUtil {
    /**
     * 从服务端获取当前的所有的设备
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
