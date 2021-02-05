package com.revolo.lock.mqtt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.revolo.lock.App;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockAddPasswordPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockApproachOpenPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockCloseWifiPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockDoorOptPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockEncryptPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockRemovePasswordPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockSetMagneticPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockUpdatePasswordPublishBean;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttCommandFactory {


    //MessageId
    public static int MESSAGE_ID = 0;


    public synchronized static int getMessageId() {
        return MESSAGE_ID++;
    }


    /**
     * 3.App设置门磁
     */
    public static MqttMessage setMagnetic(String wifiID,int mode){
        int messageId = getMessageId();
        WifiLockSetMagneticPublishBean.ParamsBean setMagneticMode = new WifiLockSetMagneticPublishBean.ParamsBean();
        setMagneticMode.setMode(mode);
        WifiLockSetMagneticPublishBean mWifiLockSetMagneticPublishBean = new WifiLockSetMagneticPublishBean(MqttConstant.MSG_TYPE_REQUEST,messageId,
                App.getInstance().getUserBean().getUid(),wifiID,MqttConstant.SET_MAGNETIC,setMagneticMode,System.currentTimeMillis() + "");
        return getMessage(mWifiLockSetMagneticPublishBean, messageId);
    }

    /**
     * 4.无感开门
     */
    public static MqttMessage approachOpen(String wifiID,int mode){
        int messageId = getMessageId();
        WifiLockApproachOpenPublishBean.ParamsBean mApproachOpen = new WifiLockApproachOpenPublishBean.ParamsBean();
        mApproachOpen.setBroadcast(mode);
        WifiLockApproachOpenPublishBean mWifiLockApproachOpenPublishBean = new WifiLockApproachOpenPublishBean(MqttConstant.MSG_TYPE_REQUEST,messageId,
                App.getInstance().getUserBean().getUid(),wifiID,MqttConstant.APP_ROACHOPEN,mApproachOpen,System.currentTimeMillis() + "");
        return getMessage(mWifiLockApproachOpenPublishBean, messageId);
    }

    /**
     *  关闭wifi
     */
    public static MqttMessage closeWifi(String wifiID){
        int messageId = getMessageId();
        WifiLockCloseWifiPublishBean mWifiLockCloseWifiPublishBean = new WifiLockCloseWifiPublishBean(MqttConstant.MSG_TYPE_REQUEST,messageId,
                App.getInstance().getUserBean().getUid(),wifiID,MqttConstant.CLOSE_WIFI,new WifiLockCloseWifiPublishBean.ParamsBean(),System.currentTimeMillis() + "");
        return getMessage(mWifiLockCloseWifiPublishBean, messageId);
    }

    /**
     *  加密数据发送
     */
    public static MqttMessage SendEncryptData(String wifiID,String encrypt){
        int messageId = getMessageId();
        WifiLockEncryptPublishBean wifiLockEncryptPublishBean = new WifiLockEncryptPublishBean(App.getInstance().getUserBean().getUid(),
                wifiID,encrypt);
        return getMessage(wifiLockEncryptPublishBean, messageId);
    }

    /**
     * 7.App 下发开门，关门指令
     */
    public static MqttMessage setLock(String wifiID,int dooropt){
        int messageId = getMessageId();
        WifiLockDoorOptPublishBean.ParamsBean setLock = new WifiLockDoorOptPublishBean.ParamsBean();
        setLock.setDooropt(dooropt);
        WifiLockDoorOptPublishBean mWifiLockDoorOptPublishBean = new WifiLockDoorOptPublishBean(MqttConstant.MSG_TYPE_REQUEST,messageId,
                App.getInstance().getUserBean().getUid(),wifiID,MqttConstant.SET_LOCK,setLock,System.currentTimeMillis() + "");
        return getMessage(mWifiLockDoorOptPublishBean, messageId);
    }

    /**
     * 8.秘钥属性添加
     */
    public static MqttMessage addPwd(String wifiID,WifiLockAddPasswordPublishBean.ParamsBean addPwd){
        int messageId = getMessageId();
        WifiLockAddPasswordPublishBean mWifiLockAddPasswordPublishBean = new WifiLockAddPasswordPublishBean(MqttConstant.MSG_TYPE_REQUEST,messageId,
                App.getInstance().getUserBean().getUid(),wifiID,MqttConstant.ADD_PWD,addPwd,System.currentTimeMillis() + "");
        return getMessage(mWifiLockAddPasswordPublishBean, messageId);
    }


    /**
     * 9.秘钥属性修改
     */
    public static MqttMessage updatePwd(String wifiID,WifiLockUpdatePasswordPublishBean.ParamsBean updatePwd){
        int messageId = getMessageId();
        WifiLockUpdatePasswordPublishBean mWifiLockUpdatePasswordPublishBean = new WifiLockUpdatePasswordPublishBean(MqttConstant.MSG_TYPE_REQUEST,messageId,
                App.getInstance().getUserBean().getUid(),wifiID,MqttConstant.UPDATE_PWD,updatePwd,System.currentTimeMillis() + "");
        return getMessage(mWifiLockUpdatePasswordPublishBean, messageId);
    }

    /**
     * 10.秘钥属性删除
     */
    public static MqttMessage removePwd(String wifiID,int keyType,int keyNum){
        int messageId = getMessageId();
        WifiLockRemovePasswordPublishBean.ParamsBean paramsBean = new WifiLockRemovePasswordPublishBean.ParamsBean();
        paramsBean.setKeyNum(keyNum);
        paramsBean.setKeyType(keyType);
        WifiLockRemovePasswordPublishBean mWifiLockRemovePasswordPublishBean = new WifiLockRemovePasswordPublishBean(MqttConstant.MSG_TYPE_REQUEST,messageId,
                App.getInstance().getUserBean().getUid(),wifiID,MqttConstant.REMOVE_PWD,paramsBean,System.currentTimeMillis() + "");
        return getMessage(mWifiLockRemovePasswordPublishBean, messageId);
    }

    /**
     * mqtt公用方法
     *
     * @param o
     * @param messageID
     * @return
     */

    public static MqttMessage getMessage(Object o, int messageID) {
        String payload = new Gson().toJson(o);
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(2);
        mqttMessage.setRetained(false);
        mqttMessage.setId(messageID);
        mqttMessage.setPayload(payload.getBytes());
        return mqttMessage;
    }

    public static MqttMessage getMessage(Object o, int messageID,int qos) {
        String payload = new Gson().toJson(o);
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(false);
        mqttMessage.setId(messageID);
        mqttMessage.setPayload(payload.getBytes());
        return mqttMessage;
    }

    public static MqttMessage getMe(Object o, int messageID) {
        GsonBuilder gb = new GsonBuilder();
        gb.disableHtmlEscaping();
        String payload = gb.create().toJson(o);
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(2);
        mqttMessage.setRetained(false);
        mqttMessage.setId(messageID);
        mqttMessage.setPayload(payload.getBytes());
        return mqttMessage;
    }

}
