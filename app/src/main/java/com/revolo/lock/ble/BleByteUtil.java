package com.revolo.lock.ble;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class BleByteUtil {

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



}
