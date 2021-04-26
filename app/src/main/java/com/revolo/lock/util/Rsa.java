package com.revolo.lock.util;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

/**
 * @author alun (http://alunblog.duapp.com)
 * @version 1.0
 */
public class Rsa {

    private static final String RSA_PUBLICE = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3//sR2tXw0wrC2DySx8vNGlqt3Y7ldU9+LBLI6e1KS5lfc5jlTGF7KBTSkCHBM3ouEHWqp1ZJ85iJe59aF5gIB2klBd6h4wrbbHA2XE1sq21ykja/Gqx7/IRia3zQfxGv/qEkyGOx+XALVoOlZqDwh76o2n1vP1D+tD3amHsK7QIDAQAB";
    private static final String ALGORITHM = "RSA";

    private Rsa() {}

    /**
     * 得到公钥
     *
     * @param algorithm
     * @param bysKey
     * @return
     */
    private static PublicKey getPublicKeyFromX509(String algorithm,
                                                  String bysKey) throws NoSuchAlgorithmException, Exception {
        byte[] decodedKey = Base64.decode(bysKey, Base64.DEFAULT);
        X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(x509);
    }


    /**
     * 使用公钥加密
     *
     * @param content
     * @return
     */
    public static String encryptByPublic(String content) {
        try {
            PublicKey pubkey = getPublicKeyFromX509(ALGORITHM, RSA_PUBLICE);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);
            byte plaintext[] = content.getBytes(StandardCharsets.UTF_8);
            byte[] output = cipher.doFinal(plaintext);
            return new String(Base64.encode(output, Base64.DEFAULT));
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 使用公钥解密
     *
     * @param content 密文
     * @return 解密后的字符串
     */
    public static String decryptByPublic(String content) {
        try {
            PublicKey pubkey = getPublicKeyFromX509(ALGORITHM, RSA_PUBLICE);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, pubkey);
            InputStream ins = new ByteArrayInputStream(Base64.decode(content, Base64.DEFAULT));
            ByteArrayOutputStream writer = new ByteArrayOutputStream();
            byte[] buf = new byte[128];
            int bufl;
            while ((bufl = ins.read(buf)) != -1) {
                byte[] block;
                if (buf.length == bufl) {
                    block = buf;
                } else {
                    block = new byte[bufl];
                    if (bufl >= 0) System.arraycopy(buf, 0, block, 0, bufl);
                }
                writer.write(cipher.doFinal(block));
            }
            return new String(writer.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return
     */
    public static byte[] encrypt(byte[] content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content;//content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            return cipher.doFinal(byteContent); // 加密
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return
     */
    public static byte[] encryptAES(byte[] content, byte[] password) {
        try {
            if (password == null) {
                System.out.print("Key为空null");
                return null;
            }
            // 判断Key是否为16位
            if (password.length != 16) {
                System.out.print("Key长度不是16位");
                return null;
            }
            // SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            SecretKeySpec key = new SecretKeySpec(password, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            return cipher.doFinal(content); // 加密
        } catch (Exception e) {
            Log.e("walter", "加密异常==" + e.toString());
        }
        return null;
    }

    /**
     * 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return
     */
    public static byte[] encrypt2(byte[] content, byte[] password) {
        try {
            // SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            SecretKeySpec key = new SecretKeySpec(password, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            // byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            return cipher.doFinal(content); // 加密
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("walter 加密异常 : %1s", e.toString());
        }
        return null;
    }


    /**
     * 解密
     *
     * @param content  待解密内容
     * @param password 解密密钥
     * @return
     */
    public static byte[] decrypt(byte[] content, byte[] password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            return cipher.doFinal(content); //
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("解密异常 : %1s", e.toString());
        }
        return null;
    }

    public static void main(String[] args) {
        byte[] content = {'1', '2', '3', '4', '5'};
        byte[] password = {'1', '2', '3', '4', '5', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Timber.d(Rsa.bytesToHexString(decrypt(content, password)));
    }

    /**
     * 将二进制转换为16进制
     * @param buf byte数组
     * @return
     */
    public static String parseByte2HexStr(byte[] buf) {
        StringBuffer buffer = new StringBuffer();
        for (byte b : buf) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            buffer.append(hex.toUpperCase());
        }
        return buffer.toString();
    }

    /**
     * 将16进制转换为二进制
     * @param hexStr 16进制
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1){
            return null;
        }
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0;i< hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    public static String toHexString(byte[] value) {
        StringBuffer mToHexString = new StringBuffer();
        for (int i = 0; i < value.length; i++) {
            mToHexString.append(Integer.toHexString((value[i] & 0xFF)));
            mToHexString.append(" ");
        }
//		Log.e("walter", "读取蓝牙特征值 mToHexString= " + mToHexString);
        return mToHexString.toString();
    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    public static String byteToHexString(byte src) {
        StringBuilder stringBuilder = new StringBuilder("");
        int v = src & 0xFF;
        String hv = Integer.toHexString(v);
        if (hv.length() < 2) {
            stringBuilder.append(0);
        }
        stringBuilder.append(hv);

        return stringBuilder.toString();
    }

    public static String byteTo10(byte[] value) {
        StringBuffer mToHexString = new StringBuffer();
        for (int i = 0; i < value.length; i++) {
            mToHexString.append(Integer.toHexString((value[i] & 0xFF)));
        }

        return mToHexString.toString();
    }

    public static byte[] hex2byte(String hex)
            throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteInt = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = Integer.valueOf(byteInt).byteValue();
        }
        return b;
    }

    public static byte[] hex2byte2(String hex)
            throws IllegalArgumentException {
        hex = hex.replaceAll(" ", "");
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteInt = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = Integer.valueOf(byteInt).byteValue();
        }
        return b;
    }

    public static long bytes2Int(byte[] bt) {
        byte[] b = new byte[4];
        b[3] = bt[0];
        b[2] = bt[1];
        b[1] = bt[2];
        b[0] = bt[3];
        long result = 0;
        for (int i = 0; i < b.length; i++) {
            int tmpVal = (b[i] << (8 * (3 - i)));
            switch (i) {
                case 0:
                    tmpVal = tmpVal & 0xFF000000;
                    break;
                case 1:
                    tmpVal = tmpVal & 0x00FF0000;
                    break;
                case 2:
                    tmpVal = tmpVal & 0x0000FF00;
                    break;
                case 3:
                    tmpVal = tmpVal & 0x000000FF;
                    break;
            }
            result = result | tmpVal;
        }
        return result;
    }

    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    /**
     * 把int转换成byte数组
     *
     * @return 返回的byte数组
     */
    public static byte[] int2BytesArray(int n) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (n >> (24 - i * 8));
        }
        byte[] transitionByte = new byte[4];
        transitionByte[3] = b[0];
        transitionByte[2] = b[1];
        transitionByte[1] = b[2];
        transitionByte[0] = b[3];
        return transitionByte;
    }

    /**
     * 将byte转换为一个长度为8的byte数组，数组每个值代表bit
     */
    public static byte[] byteToBit(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }


    /**
     * 两个字节转int
     */
    public static int bytesToInt(byte[] ary) {
        int value;
        value = (int) ((ary[0] & 0xFF)
                | ((ary[1] << 8) & 0xFF00)
        );
        return value;
    }

    /**
     * 数组转long
     */
    public static long getInt(byte[] bb) {
        long l1 = bb[0]&0xff;
        long l2 = bb[1]&0xff;
        long l3 = bb[2]&0xff;
        long l4 = bb[3]&0xff;
        return (l4<<24)+(l3<<16)+(l2<<8)+l1;
    }

    /**
     * hexString转byteArr
     * <p>例如：</p>
     * hexString2Bytes("00A8") returns { 0, (byte) 0xA8 }
     *
     * @param hexString 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexString2Bytes(String hexString) {
        int len = hexString.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        }
        char[] hexBytes = hexString.toUpperCase().toCharArray();
        byte[] ret = new byte[len >>> 1];
        for (int i = 0; i < len; i += 2) {
            ret[i >> 1] = (byte) (hex2Dec(hexBytes[i]) << 4 | hex2Dec(hexBytes[i + 1]));
        }
        return ret;
    }

    /**
     * hexChar转int
     *
     * @param hexChar hex单个字节
     * @return 0..15
     */
    private static int hex2Dec(char hexChar) {
        if (hexChar >= '0' && hexChar <= '9') {
            return hexChar - '0';
        } else if (hexChar >= 'A' && hexChar <= 'F') {
            return hexChar - 'A' + 10;
        } else {
            throw new IllegalArgumentException();
        }
    }

}

