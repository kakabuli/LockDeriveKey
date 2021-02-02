package com.revolo.lock.ble;

import androidx.annotation.Nullable;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.revolo.lock.ble.bean.BleResultBean;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 蓝牙协议的解析
 */
public class BleResultProcess {

    private static final byte CONTROL_ENCRYPTION = 0x01;
    private static final byte CONTROL_NORMAL = 0x00;

    public static void processReceivedData(byte[] receivedData,
                                           byte[] pwd1,
                                           byte[] pwd2Or3,
                                           BLEScanResult bleScanResult) {
        if(receivedData.length != 20) {
            Timber.e("paresReceivedData 接收的数据长度不对，不进行解析 length : %1d", receivedData.length);
            return;
        }
        process(receivedData, pwd1, pwd2Or3, bleScanResult);
    }

    public interface OnReceivedProcess {
        void processResult(BleResultBean bleResultBean);
    }

    private static OnReceivedProcess sOnReceivedProcess;

    public static void setOnReceivedProcess(OnReceivedProcess onReceivedProcess) {
        sOnReceivedProcess = onReceivedProcess;
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8).put(new byte[]{0, 0, 0, 0}).put(bytes);
        buffer.position(0);
        return buffer.getLong();
    }

    public static byte[] longToUnsigned32Bytes(long value)
    {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putLong(value);

        return Arrays.copyOfRange(bytes, 4, 8);
    }

    /**
     *  加密密钥上报
     *  入网时，密钥：Password1+0x00000000 明文：PWD_Type(0x01)+Password2
     *  鉴权成功，密钥：Password1+Password2 明文：PWD_Type(0x02)+Password3
     *  退网时，密钥：Password1+0x00000000 明文：PWD_Type(0x03)+0x00000000
     */

    /**
     * 简单的校验和
     * @param payload 要校验和的数据
     * @return 校验和
     */
    private static byte checksum(byte[] payload) {
        //加密前把数据的校验和算出来
        byte  checkSum = 0;
        for (byte b : payload) {
            checkSum += b;
        }
        return checkSum;
    }

    /**
     * 需要用pwd1和pwd2进行解密
     * @param needDecryptData  需要解密的数据
     * @param pwd1             解密的key pwd1 不足16位需要补0
     * @param pwd2Or3          解密的key pwd2或者pwd3，与pwd1结合生成pwd，可以为null，表示只用pwd1解密
     * @return                 解密后的数据，可能为null
     */
    private static byte[] pwdDecrypt(@NotNull byte[] needDecryptData,
                                     @NotNull byte[] pwd1,
                                     @Nullable byte[] pwd2Or3) {
        if(pwd1.length != 16) {
            Timber.e("pwdDecrypt pwd1 key的长度错误，请检查输入的数据 size: %1d", pwd1.length);
            return null;
        }
        // 只使用pwd1来解密
        if(pwd2Or3 == null) {
            return pwd1Decrypt(needDecryptData, pwd1);
        }
        // pwd1+pwd2或者pwd1+pwd3解密
        if(pwd2Or3.length != 4) {
            Timber.e("pwdDecrypt pwd2 key的长度错误，请检查输入的数据 size: %1d", pwd2Or3.length);
            return null;
        }
        return pwd1n2Or3Decrypt(needDecryptData, pwd1, pwd2Or3);
    }

    /**
     * 使用pwd1+pwd2或者pwd1+pwd3来解密数据
     * @param needDecryptData  需要解密的数据
     * @param pwd1             需要解密的pwd1
     * @param pwd2Or3          需要解密的pwd2或者pwd3
     * @return                 解密后的数据
     */
    private static byte[] pwd1n2Or3Decrypt(@NotNull byte[] needDecryptData,
                                           @NotNull byte[] pwd1,
                                           @NotNull byte[] pwd2Or3) {
        byte[] pwd = new byte[16];
        for (int i=0; i < pwd.length; i++) {
            if(i <= 11) {
                pwd[i]=pwd1[i];
            } else {
                pwd[i]=pwd2Or3[i-12];
            }
        }
        byte[] payload = EncryptUtils.decryptAES(needDecryptData, pwd, "AES/ECB/NoPadding", null);
        Timber.d("pwd1n2Or3Decrypt 解密之前的数据：%1s\n pwd1：%2s\n pwd2: %3s\n pwd: %4s\n 解密后的数据：%5s",
                ConvertUtils.bytes2HexString(needDecryptData),
                ConvertUtils.bytes2HexString(pwd1),
                ConvertUtils.bytes2HexString(pwd2Or3),
                ConvertUtils.bytes2HexString(pwd),
                ConvertUtils.bytes2HexString(payload));
        return payload;
    }

    /**
     * 只使用pwd1来解密
     * @param needDecryptData  需要解密的原始数据
     * @param pwd1             解密需要用到的pwd1
     * @return                 解密后的数据
     */
    private static byte[] pwd1Decrypt(@NotNull byte[] needDecryptData, @NotNull byte[] pwd1) {
        byte[] payload = EncryptUtils.decryptAES(needDecryptData, pwd1, "AES/ECB/NoPadding", null);
        Timber.d("pwd1Decrypt 解密之前的数据：%1s\n pwd1：%2s\n 解密后的数据：%3s",
                ConvertUtils.bytes2HexString(needDecryptData),
                ConvertUtils.bytes2HexString(pwd1),
                ConvertUtils.bytes2HexString(payload));
        return payload;
    }

    /**
     * 处理数据
     * @param receivedData 接收到的数据
     * @param pwd1         解密需要的pwd1
     * @param pwd2Or3      解密需要的pwd2或pwd3
     */
    private static void process(byte[] receivedData, byte[] pwd1, byte[] pwd2Or3, BLEScanResult bleScanResult) {

        if(receivedData.length != 20) {
            Timber.e("getCmd 接收的数据长度不对，不进行解析 length : %1d", receivedData.length);
            return;
        }
        boolean isEncrypt = (receivedData[0]==CONTROL_ENCRYPTION);
        byte[] payload = new byte[16];
        System.arraycopy(receivedData,  4, payload, 0, payload.length);
        byte[] decryptPayload = isEncrypt?pwdDecrypt(payload, pwd1, pwd2Or3):payload;
        byte sum = checksum(decryptPayload);
        if(receivedData[2] != sum) {
            Timber.d("getCmd 校验和失败，接收数据中的校验和：%1s，\n接收数据后计算的校验和：%2s",
                    ConvertUtils.int2HexString(receivedData[2]), ConvertUtils.int2HexString(sum));
            return;
        }
        if(sOnReceivedProcess == null) {
            Timber.e("process sOnReceivedProcess == null");
            return;
        }
        BleResultBean bleResultBean = new BleResultBean(receivedData[0], BleByteUtil.byteToInt(receivedData[1]),
                BleByteUtil.byteToInt(receivedData[3]), payload, decryptPayload, bleScanResult);
        sOnReceivedProcess.processResult(bleResultBean);
    }

}
