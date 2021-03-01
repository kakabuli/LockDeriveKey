package com.revolo.lock.ble;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class BleByteUtil {

    /**
     * 把int转换成byte数组
     *
     * @param n 要转换的int值
     * @return 返回的byte数组
     */
    public static byte[] int2BytesArray(int n) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (n >> (24 - i * 8));
        }
        return b;
    }


    /**
     * 把byte数组转换成int类型
     *
     * @param b 源byte数组
     * @return 返回的int值
     */
    public static int byteArray2Int(byte[] b) {
        int a = (((int) b[0]) << 24) + (((int) b[1]) << 16) + (((int) b[2]) << 8) + b[3];
        if (a < 0) {
            a = a + 256;
        }
        return a;
    }

    public static short bytesToShortToLittleEndian(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static short bytesToShortToBigEndianFromLittleEndian(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    //byte 与 int 的相互转换
    public static byte intToByte(int x) {
        return (byte) x;
    }

    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
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

    public static byte[] byteToBit (byte a) {
        byte[] temp= new byte[8];
        for (int i = 7; i >= 0; i--) {
            temp[i] = (byte)((a >> i) & 1);
        }
        return temp;
    }

    public static byte bitToByte(byte[] a) {
        byte temp = (byte) 0;
        for (int i = 0; i < a.length; i++) {
            temp = (byte) (temp | a[i] << i);
        }
        return temp ;
    }

    public static byte[] shortToBytes(short value) {
        byte[] returnByteArray = new byte[2];
        returnByteArray[0] = (byte) (value & 0xff);
        returnByteArray[1] = (byte) ((value >>> 8) & 0xff);
        return returnByteArray;
    }

}
