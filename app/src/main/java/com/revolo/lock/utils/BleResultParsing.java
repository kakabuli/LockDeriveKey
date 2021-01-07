package com.revolo.lock.utils;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncryptUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 蓝牙协议的解析
 */
public class BleResultParsing {

    private static final byte CONTROL_ENCRYPTION = 0x01;
    private static final byte CONTROL_NORMAL = 0x00;
    private static final byte[] sTestPwd1 = new byte[] {0x53,0x48,0x45,
            (byte) 0xB3, (byte) 0xEB, (byte) 0xF3,
            0x01, (byte) 0xBC,0x1D,
            0x0F,0x68, (byte) 0x8F,
            0x00,0x00,0x00,0x00};
    
    private static byte[] mPwd2Or3 = new byte[4];
    private static byte[] mPwd = new byte[16];
    private static byte mTSN;
    private static boolean isUsePwd2 = false;

    public static int paresReceivedData(byte[] receivedData) {
        if(receivedData.length != 20) {
            Timber.e("paresReceivedData 接收的数据长度不对，不进行解析 length : %1d", receivedData.length);
            return 0;
        }
        byte cmd =  receivedData[3];
        if(cmd == 0x1b) {
            byte status = receivedData[4];
            if(status == 0x00) {
                return 1;
            }
        } else if(cmd == 0x08) {
            byte[] payload = new byte[16];
            System.arraycopy(receivedData, 4, payload, 0, payload.length);
            byte[] data = EncryptUtils.decryptAES(payload, isUsePwd2?mPwd:sTestPwd1, "AES/ECB/NoPadding", null);
            Timber.d("解密前的数据：%1s，解密后的数据：%2s, 使用的%3s加密",
                    ConvertUtils.bytes2HexString(payload),ConvertUtils.bytes2HexString(data), isUsePwd2?"pwd1+pwd2":"pwd1");
            if(data[0] == 0x01 || data[0] == 0x02) {
                // 入网时
                isUsePwd2 = true;
                System.arraycopy(data, 1, mPwd2Or3, 0, mPwd2Or3.length);
//                mPwd2Or3 = ByteBuffer.wrap(mPwd2Or3).order(ByteOrder.LITTLE_ENDIAN).array();
                mTSN = receivedData[1];
                for (int i=0; i < mPwd.length; i++) {
                    if(i <= 11) {
                        mPwd[i]=sTestPwd1[i];
                    } else {
                        mPwd[i]= mPwd2Or3[i-12];
                    }
                }
                return data[0] == 0x01?2:4;
            }
            return 3;
        }
        return 0;
    }

    public static byte[] getPwd2Or3() {
        return mPwd2Or3;
    }

    public static byte getTSN() {
        return mTSN;
    }

}
