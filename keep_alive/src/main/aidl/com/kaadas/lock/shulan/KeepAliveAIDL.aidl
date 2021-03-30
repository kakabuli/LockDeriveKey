// KeepAliveAIDL.aidl
package com.kaadas.lock.shulan;

// Declare any non-default types here with import statements

interface KeepAliveAIDL {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    //相互唤醒服务
    void wakeUp(String title, String discription, int iconRes);
}
