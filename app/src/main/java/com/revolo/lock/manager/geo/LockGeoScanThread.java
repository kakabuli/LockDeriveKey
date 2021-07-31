package com.revolo.lock.manager.geo;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.a1anwang.okble.client.scan.DeviceScanCallBack;
import com.a1anwang.okble.client.scan.OKBLEScanManager;
import com.revolo.lock.App;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * 定位设备搜索
 */
public class LockGeoScanThread {
    private static LockGeoScanThread lockGeoScanThread;
    private LockGeoScanLinstener linstener;
    private List<String> devices;
    private Map<String, Integer> msgs = new HashMap<>();

    public static LockGeoScanThread getInstance() {
        if (null == lockGeoScanThread) {
            lockGeoScanThread = new LockGeoScanThread();
        }
        return lockGeoScanThread;
    }

    public interface LockGeoScanLinstener {
        void onBLEDeviceScan(BLEScanResult bleScanResult, int i);
    }

    public void setLockGeoScanLinstener(LockGeoScanLinstener lockGeoScanLinstener) {
        linstener = lockGeoScanLinstener;
    }

    public int addDevice(String mac, LockGeoScanLinstener lockGeoScanLinstener) {
        if (null == linstener) {
            linstener = lockGeoScanLinstener;
        }
        Timber.e(" 添加设备开始搜索：" + mac);
        int isAdd = -1;
        if (null == devices) {
            devices = new ArrayList<>();
        }
        if (checkMac(mac) < 0) {
            devices.add(mac);
            isAdd = devices.size() - 1;
        }
        if (devices.size() > 0) {
            //开始搜索
            startScan();
        } else {
            //结束搜索
            stopScan();
            return -1;
        }
        if (isAdd != -1) {
            msgs.put(mac, isAdd);
        }
        return isAdd;
    }

    public int getDeviceMap(String mac) {
        Timber.e("getDevice：" + mac);
        if (msgs.size() == 0) {
            return -1;
        }
        return msgs.get(mac);
    }

    public int checkMac(String mac) {
        int index = -1;
        if (null != devices) {
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).equals(mac)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }


    public void clearDevice(String mac) {
        Timber.e("clear device：" + mac);
        if (null != devices) {
            int index = 0;
            if ((index = checkMac(mac)) > -1) {
                devices.remove(index);
                msgs.remove(mac);
            }
            if (devices.size() > 0) {
                //开始搜索
                startScan();
            } else {
                //结束搜索
                stopScan();
            }
        } else {
            //结束搜索
            stopScan();
        }
    }

    private OKBLEScanManager mScanManager;

    public OKBLEScanManager getScanManager() {
        if (mScanManager == null) {
            mScanManager = new OKBLEScanManager(App.getInstance().getApplicationContext());
            mScanManager.setScanCallBack(deviceScanCallBack);
            mScanManager.setScanDuration(8 * 1000);
        }
        return mScanManager;
    }

    private DeviceScanCallBack deviceScanCallBack = new DeviceScanCallBack() {
        @Override
        public void onBLEDeviceScan(BLEScanResult bleScanResult, int i) {
            if (null != bleScanResult) {
                Timber.e("搜索到的设备：" + bleScanResult.getMacAddress());
                for (int index = 0; index < devices.size(); index++) {
                    if (bleScanResult.getMacAddress().toUpperCase().equals(devices.get(index).toUpperCase())) {
                        Timber.e("搜索到的目标设备：" + bleScanResult.getMacAddress());
                        if (null != linstener) {
                            linstener.onBLEDeviceScan(bleScanResult, i);
                        }
                        devices.remove(index);
                        break;
                    }
                }
                if (devices.size() == 0) {
                    stopScan();
                }
            }

        }

        @Override
        public void onFailed(int i) {
            Timber.e("搜索 onFailed:" + i);
            stopScan();
        }

        @Override
        public void onStartSuccess() {
            Timber.e("搜索 onStartSuccess");
        }
    };

    private void startScan() {
        stopScan();
        if (null == scanThread) {
            scanThread = new ScanThread();
        }
        scanThread.start();

    }

    private void stopScan() {
        Timber.e("停止搜索");
        if (null != scanThread) {
            scanThread.interrupt();
            scanThread = null;
        }
        if (null != getScanManager()) {
            getScanManager().stopScan();
        }

    }

    private ScanThread scanThread;

    private class ScanThread extends Thread {
        @Override
        public void run() {
            Timber.e("开始搜索");
            getScanManager().startScan();
            try {
                ScanThread.sleep(8 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopScan();
        }
    }


}
