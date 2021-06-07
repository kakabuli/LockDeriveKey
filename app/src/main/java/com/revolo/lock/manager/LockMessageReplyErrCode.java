package com.revolo.lock.manager;

/**
 * 通讯中产生的bug
 */
public class LockMessageReplyErrCode {
    /**
     * LockMessageRes message=new LockMessageRes();
     * message.setMessgaeType(MSG_LOCK_MESSAGE_BLE);//蓝牙消息
     * message.setResultCode(MSG_LOCK_MESSAGE_CODE_SUCCESS);//操作成功
     * message.setMessageCode(MSG_LOCK_MESSAGE_ADD_DEVICE_SERVICE);//添加设备到服务端
     * message.setBleResultBea(bleResultBean);
     * message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
     * EventBus.getDefault().post(message);
     */
    //ble通讯
    public static final int LOCK_BLE_ERR_CODE_CONNECT_OUT_TIME = 1;//连接超时
    public static final int LOCK_BLE_ERR_CODE_DATA_CHECK_ERR = 2;//校验失败
    public static final int LOCK_BLE_ERR_CODE_DATA_WRITE_ERR = 3;//写入失败
    public static final int LOCK_BLE_ERR_CODE_DATA_NOTIFY_ERR = 4;//通知失败
    public static final int LOCK_BLE_ERR_CODE_BLE_DIS_ERR = 5;//蓝牙断开失败
    public static final int LOCK_BLE_ERR_CODE_BLE_VALUE_ERR = 6;//蓝牙蓝牙数据错误

    //MQTT
}
