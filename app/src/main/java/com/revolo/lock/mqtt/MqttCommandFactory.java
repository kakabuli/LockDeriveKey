package com.revolo.lock.mqtt;

import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.revolo.lock.App;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockAddPwdAttrPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockAddPwdPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockApproachOpenPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockCloseWifiPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockDoorOptPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockEncryptPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockGetAllBindDevicePublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockRemovePasswordPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockSetLockAttrAmModePublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockSetLockAttrAutoLockTimePublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockSetLockAttrDuressPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockSetLockAttrSensitivityPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockSetLockAttrVolumePublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockSetMagneticPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockUpdatePasswordPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.AmModeParams;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.AutoLockTimeParams;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.DuressParams;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.ElecFenceSensitivityParams;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.VolumeParams;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrAutoRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrAutoTimeRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrDuressRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrSensitivityRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrVolumeRspBean;
import com.revolo.lock.util.Rsa;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class MqttCommandFactory {

    private MqttCommandFactory() {
    }

    //MessageId
    public static int MESSAGE_ID = 1;


    public synchronized static int getMessageId() {
        return MESSAGE_ID++;
    }

    private static Map<String, Class> msgs = new HashMap<>();

    /**
     * @param msgId
     * @param c
     * @param type
     * @return
     */
    public synchronized static Class sendMessage(String msgId, Class c, int type) {

        if (type == 0) {
            //添加
            msgs.put(msgId, c);
            return null;
        } else if (type == 1) {
            return msgs.get(msgId);
        } else {
            msgs.remove(msgId);
            return null;
        }
    }


    public static MqttMessage getAllBindDevices(String uid) {
        int messageId = getMessageId();
        WifiLockGetAllBindDevicePublishBean bean = new WifiLockGetAllBindDevicePublishBean(messageId,
                MQttConstant.MSG_TYPE_REQUEST, MQttConstant.GET_ALL_BIND_DEVICE, uid, 2);
        return getMessage(bean, messageId);
    }

    /**
     * 3.App设置门磁
     */
    public static MqttMessage setMagnetic(String wifiID, int mode, byte[] pwd) {
        int messageId = getMessageId();
        WifiLockSetMagneticPublishBean.ParamsBean setMagneticMode = new WifiLockSetMagneticPublishBean.ParamsBean();
        setMagneticMode.setMode(mode);
        WifiLockSetMagneticPublishBean wifiLockSetMagneticPublishBean = new WifiLockSetMagneticPublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.SET_MAGNETIC,
                setMagneticMode,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockSetMagneticPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json);
    }

    /**
     * 无感开门
     */
    public static MqttMessage approachOpen(String wifiID, int broadcastTime, byte[] pwd) {
        int messageId = getMessageId();
        WifiLockApproachOpenPublishBean.ParamsBean approachOpenPublishBean = new WifiLockApproachOpenPublishBean.ParamsBean();
        approachOpenPublishBean.setBroadcast(broadcastTime);
        // 固定设置为0，不开启ibeacon
        approachOpenPublishBean.setIbeacon(0);
        WifiLockApproachOpenPublishBean wifiLockApproachOpenPublishBean = new WifiLockApproachOpenPublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.APP_ROACH_OPEN,
                approachOpenPublishBean,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockApproachOpenPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json);
    }

    /**
     * 关闭wifi
     */
    public static MqttMessage closeWifi(String wifiID, byte[] pwd) {
        int messageId = getMessageId();
        WifiLockCloseWifiPublishBean wifiLockCloseWifiPublishBean = new WifiLockCloseWifiPublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.CLOSE_WIFI,
                new WifiLockCloseWifiPublishBean.ParamsBean(),
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockCloseWifiPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json);
    }

    /**
     * 加密数据发送
     */
    public static MqttMessage sendEncryptData(int messageId, String wifiID, String encrypt) {
        WifiLockEncryptPublishBean wifiLockEncryptPublishBean = new WifiLockEncryptPublishBean(App.getInstance().getUserBean().getUid(),
                wifiID, encrypt);
        return getMessage(wifiLockEncryptPublishBean, messageId, 2);
    }

    /**
     * 加密数据发送
     */
    public static MqttMessage sendEncryptData(int messageId, String wifiID, String encrypt, int qos) {
        WifiLockEncryptPublishBean wifiLockEncryptPublishBean = new WifiLockEncryptPublishBean(App.getInstance().getUserBean().getUid(),
                wifiID, encrypt);
        return getMessage(wifiLockEncryptPublishBean, messageId, qos);
    }

    /**
     * 7.App 下发开门，关门指令
     */
    public static MqttMessage setLock(String wifiID, int dooropt, byte[] pwd, String randomCode, int num) {
        int messageId = getMessageId();
        WifiLockDoorOptPublishBean.ParamsBean setLock = new WifiLockDoorOptPublishBean.ParamsBean();
        setLock.setDooropt(dooropt);
        setLock.setOfflinePwd(getPassword(wifiID, randomCode));
        setLock.setUserNumberId(num);
        WifiLockDoorOptPublishBean mWifiLockDoorOptPublishBean = new WifiLockDoorOptPublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.SET_LOCK, setLock,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, mWifiLockDoorOptPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json, 0);
    }

    private static String getPassword(String wifiEsn, String randomCode) {
        // 有效时间为20秒
        String time = (System.currentTimeMillis() / 1000 / 20) + "";
        Timber.d("--revolo调试--wifiSN  %1s", wifiEsn);
        Timber.d("--revolo调试--randomCode  %1s", randomCode);
        Timber.d("--revolo调试--System.currentTimeMillis()  %1d", System.currentTimeMillis());

        String content = wifiEsn + randomCode + time;

        Timber.d("--revolo--本地数据是  %1s", content);
        byte[] data = content.toUpperCase().getBytes();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            byte[] digest = messageDigest.digest();
            byte[] temp = new byte[4];
            System.arraycopy(digest, 0, temp, 0, 4);
            long l = Rsa.getInt(temp);
            String text = (l % 1000000) + "";
            Timber.e("--revolo--转换之后的数据是     " + l + "    " + Rsa.bytes2Int(temp));
            int offSet = (6 - text.length());
            for (int i = 0; i < offSet; i++) {
                text = "0" + text;
            }
            Timber.d("--revolo--   testSha256 数据是   %1s", Rsa.bytesToHexString(messageDigest.digest()));
            return text;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String getEncryptString(byte[] pwd, Object src) {
        String json = new Gson().toJson(src);
        Timber.d("MQtt 发送数据未加密： %1s", json);
        // 先AES加密
        byte[] aesJson = EncryptUtils.encryptAES(json.getBytes(StandardCharsets.UTF_8), pwd, "AES/ECB/PKCS5Padding", null);
        // 后Base64字符串编码
        return EncodeUtils.base64Encode2String(aesJson);
    }

    public static MqttMessage addPwd(String wifiId, WifiLockAddPwdPublishBean.ParamsBean paramsBean, byte[] pwd) {
        int messageId = getMessageId();
        WifiLockAddPwdPublishBean wifiLockAddPwdPublishBean = new WifiLockAddPwdPublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiId, MQttConstant.CREATE_PWD,
                paramsBean,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockAddPwdPublishBean);
        return sendEncryptData(messageId, wifiId, base64Json);
    }

    /**
     * 秘钥属性添加
     */
    public static MqttMessage addPwdAttr(String wifiID, WifiLockAddPwdAttrPublishBean.ParamsBean addPwd, byte[] pwd) {
        int messageId = getMessageId();
        WifiLockAddPwdAttrPublishBean wifiLockAddPwdAttrPublishBean = new WifiLockAddPwdAttrPublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.ADD_PWD,
                addPwd,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockAddPwdAttrPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json);
    }


    /**
     * 秘钥属性修改
     */
    public static MqttMessage updatePwdAttr(String wifiID, WifiLockUpdatePasswordPublishBean.ParamsBean updatePwd, byte[] pwd) {
        int messageId = getMessageId();
        WifiLockUpdatePasswordPublishBean wifiLockUpdatePasswordPublishBean = new WifiLockUpdatePasswordPublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.UPDATE_PWD,
                updatePwd,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockUpdatePasswordPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json);
    }

    /**
     * 秘钥属性删除
     */
    public static MqttMessage removePwd(String wifiID, int keyType, int keyNum, byte[] pwd) {
        int messageId = getMessageId();
        WifiLockRemovePasswordPublishBean.ParamsBean paramsBean = new WifiLockRemovePasswordPublishBean.ParamsBean();
        paramsBean.setKeyNum(keyNum);
        paramsBean.setKeyType(keyType);
        WifiLockRemovePasswordPublishBean wifiLockRemovePasswordPublishBean = new WifiLockRemovePasswordPublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.REMOVE_PWD,
                paramsBean,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockRemovePasswordPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json);
    }

    /**
     * 设置锁的属性
     */
    public static MqttMessage setLockAttr(String wifiID, AmModeParams bean, byte[] pwd) {
        int messageId = getMessageId();
        sendMessage(messageId + "", WifiLockSetLockAttrAutoRspBean.class, 0);
        WifiLockSetLockAttrAmModePublishBean wifiLockSetLockAttrPublishBean = new WifiLockSetLockAttrAmModePublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.SET_LOCK_ATTR,
                bean,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockSetLockAttrPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json);

    }

    /**
     * 设置锁的属性
     */
    public static MqttMessage setLockAttr(String wifiID, AutoLockTimeParams bean, byte[] pwd) {
        int messageId = getMessageId();
        sendMessage(messageId + "", WifiLockSetLockAttrAutoTimeRspBean.class, 0);
        WifiLockSetLockAttrAutoLockTimePublishBean wifiLockSetLockAttrPublishBean = new WifiLockSetLockAttrAutoLockTimePublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.SET_LOCK_ATTR,
                bean,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockSetLockAttrPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json);

    }

    /**
     * 设置锁的属性
     */
    public static MqttMessage setLockAttr(String wifiID, DuressParams bean, byte[] pwd) {
        int messageId = getMessageId();
        sendMessage(messageId + "", WifiLockSetLockAttrDuressRspBean.class, 0);
        WifiLockSetLockAttrDuressPublishBean wifiLockSetLockAttrPublishBean = new WifiLockSetLockAttrDuressPublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.SET_LOCK_ATTR,
                bean,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockSetLockAttrPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json);

    }

    /**
     * 设置锁的属性
     */
    public static MqttMessage setLockAttr(String wifiID, ElecFenceSensitivityParams bean, byte[] pwd) {
        int messageId = getMessageId();
        sendMessage(messageId + "", WifiLockSetLockAttrSensitivityRspBean.class, 0);
        WifiLockSetLockAttrSensitivityPublishBean wifiLockSetLockAttrPublishBean = new WifiLockSetLockAttrSensitivityPublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.SET_LOCK_ATTR,
                bean,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockSetLockAttrPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json);

    }

    /**
     * 设置锁的属性
     */
    public static MqttMessage setLockAttr(String wifiID, VolumeParams bean, byte[] pwd) {
        int messageId = getMessageId();
        sendMessage(messageId + "", WifiLockSetLockAttrVolumeRspBean.class, 0);
        WifiLockSetLockAttrVolumePublishBean wifiLockSetLockAttrPublishBean = new WifiLockSetLockAttrVolumePublishBean(
                MQttConstant.MSG_TYPE_REQUEST,
                messageId,
                App.getInstance().getUserBean().getUid(),
                wifiID,
                MQttConstant.SET_LOCK_ATTR,
                bean,
                (System.currentTimeMillis() / 1000) + "");
        String base64Json = getEncryptString(pwd, wifiLockSetLockAttrPublishBean);
        return sendEncryptData(messageId, wifiID, base64Json);

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

    public static MqttMessage getMessage(Object o, int messageID, int qos) {
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
