package com.revolo.lock.ui.device.add;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.a1anwang.okble.client.core.OKBLEDeviceImp;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.WifiSnBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.popup.WifiListPopup;
import com.revolo.lock.util.LocationUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加Wifi
 */
public class AddWifiActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    private OKBLEDeviceImp mOKBLEDevice;
    private final int RC_ACCESS_COARSE_LOCATION_PERMISSIONS = 1111;
    private WifiListPopup mWifiListPopup;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_wifi;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_wifi));
        EditText etWifiName = findViewById(R.id.etWifiName);
        mWifiListPopup = new WifiListPopup(this);
        mWifiListPopup.setOnItemClickListener((adapter, view, position) -> {
            if(position < 0) {
                return;
            }
            etWifiName.setText((String) adapter.getItem(position));
            mWifiListPopup.dismiss();
        });
        applyDebouncingClickListener(findViewById(R.id.btnNext), findViewById(R.id.ivDropdown));
    }

    @Override
    public void doBusiness() {
        initDevice();
        initWifiList();
        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnNext) {
            startActivity(new Intent(this, WifiConnectActivity.class));
            return;
        }
        if(view.getId() == R.id.ivDropdown) {
            if(mWifiListPopup == null) {
                return;
            }
            if(mWifiListPopup.isShowing()) {
                mWifiListPopup.dismiss();
            } else {
                mWifiListPopup.setPopupGravity(Gravity.BOTTOM);
                mWifiListPopup.showPopupWindow(findViewById(R.id.ivDropdown));
            }
        }
    }

    @Override
    protected void onDestroy() {
        App.getInstance().clearBleDeviceListener();
        unregisterReceiver(mGpsSwitchStateReceiver);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String @NotNull [] permissions,
                                           int @NotNull [] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(perms.isEmpty()) {
            Timber.e("onPermissionsGranted 返回的权限不存在数据 perms size: %1d", perms.size());
            return;
        }
        if(requestCode != RC_ACCESS_COARSE_LOCATION_PERMISSIONS) {
            return;
        }
        if(!perms.get(0).equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return;
        }
        initWifiList();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(perms.get(0).equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Timber.e("onPermissionsDenied 拒绝了搜索WiFi列表需要的位置权限, requestCode: %1d", requestCode);
        }
    }

    private final BroadcastReceiver mGpsSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                // 监听如果打开了GPS，就更新wifi
                if(LocationUtils.isGpsEnabled()) {
                    initWifiList();
                }
            }

        }
    };

    private void initDevice() {
        mOKBLEDevice = App.getInstance().getDevice();
        if (mOKBLEDevice != null) {
            App.getInstance().openPairNotify();
            App.getInstance().setOnBleDeviceListener(mOnBleDeviceListener);
//            App.getInstance().writePairMsg(BleCommandFactory.wifiListSearchCommand());
        }
    }

    @AfterPermissionGranted(RC_ACCESS_COARSE_LOCATION_PERMISSIONS)
    private void rcAccessCoarseLocationPermission() {
        if(!hasAccessCoarseLocation()) {
            // TODO: 2021/1/3 use string
            EasyPermissions.requestPermissions(this, "TODO: location things",
                    RC_ACCESS_COARSE_LOCATION_PERMISSIONS, Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }

    private boolean hasAccessCoarseLocation() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void initWifiList() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            rcAccessCoarseLocationPermission();
            return;
        }
        if(!LocationUtils.isGpsEnabled()) {
            // TODO: 2021/1/22 优化提示语
            ToastUtils.showShort("please open your GPS!");
            return;
        }
        NetworkUtils.WifiScanResults wifiScanResults = NetworkUtils.getWifiScanResult();
        if(wifiScanResults.getFilterResults().isEmpty()) {
            Timber.e("initWifiList wifiScanResults.getFilterResults().isEmpty()");
            return;
        }
        mWifiSnList.clear();
        for (ScanResult scanResult : wifiScanResults.getFilterResults()) {
            Timber.d("wifi sn: %1s", scanResult.SSID);
            mWifiSnList.add(scanResult.SSID);
        }
        if(mWifiListPopup != null) {
            mWifiListPopup.updateWifiList(mWifiSnList);
        }
    }

    private final OnBleDeviceListener mOnBleDeviceListener = new OnBleDeviceListener() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onReceivedValue(String uuid, byte[] value) {
            if(value == null) {
                return;
            }
            BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
            BleResultProcess.processReceivedData(value, BleCommandFactory.sTestPwd1, null, mOKBLEDevice.getBleScanResult());
        }

        @Override
        public void onWriteValue(String uuid, byte[] value, boolean success) {

        }
    };

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        receiveWifiList(bleResultBean);
    };

    private int mWifiTotalNum = 0;
    private final HashMap<Integer, WifiSnBean> mWifiHashMap = new HashMap<>();
    private final List<String> mWifiSnList = new ArrayList<>();

    private void receiveWifiList(BleResultBean bleResultBean) {
//        Timber.d("receiveWifiList 接收信息：%1s", ConvertUtils.int2HexString(bleResultBean.getCMD()));
        if(bleResultBean.getCMD() == BleProtocolState.CMD_WIFI_LIST_CHECK) {
            byte[] payload = bleResultBean.getPayload();
            mWifiTotalNum = payload[0];
            int no = payload[1];
            WifiSnBean wifiSnBean = mWifiHashMap.get(no);
            byte[] data = new byte[11];
            System.arraycopy(payload, 5, data, 0, data.length);
            WifiSnBean.WifiSnBytesBean bytesBean = new WifiSnBean.WifiSnBytesBean(payload[4], payload[3]);
            bytesBean.setBytes(data);
            List<WifiSnBean.WifiSnBytesBean> bytesBeans;
            if(wifiSnBean == null) {
                // 新WifiSn
                bytesBeans = new ArrayList<>();
                bytesBeans.add(bytesBean);
                wifiSnBean = new WifiSnBean();
                wifiSnBean.setRssi(payload[2]);
                wifiSnBean.setWifiIndex(no);
                wifiSnBean.setWifiSnBytesBeans(bytesBeans);
                mWifiHashMap.put(no, wifiSnBean);
            } else {
                bytesBeans = wifiSnBean.getWifiSnBytesBeans();
                if(bytesBeans == null) {
                    bytesBeans = new ArrayList<>();
                }
                bytesBeans.add(bytesBean);
                wifiSnBean.setWifiSnBytesBeans(bytesBeans);
            }
            Timber.d("receiveWifiList total: %1d, no: %2d", mWifiTotalNum, no);
            // TODO: 2021/1/21 有可能缺失的就是最后一包 后期做个超时
            if(no == (mWifiTotalNum-1)) {
                // 最后一个wifi
                boolean isRemain = (bytesBean.getSnLenTotal()%11 != 0);
                int index = bytesBean.getSnLenTotal()/11;
                if(bytesBean.getIndex() == (isRemain?index:index-1)) {
                    processWifi();
                }
            }
        }
    }

    private final List<Integer> mLackNoList = new ArrayList<>();

    private void processWifi() {
        if(!mWifiHashMap.isEmpty()) {
            for (int i=0; i<mWifiTotalNum; i++) {
                WifiSnBean wifiSnBean = mWifiHashMap.get(i);
                if(wifiSnBean != null) {
                    mWifiSnList.add(wifiSnBean.getWifiSn());
                } else {
                    mLackNoList.add(i);
                }
            }
        }
        if(!mLackNoList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<mLackNoList.size(); i++) {
                sb.append(mLackNoList.get(i));
                if(i < mLackNoList.size() - 1) {
                    sb.append(",");
                }
            }
            Timber.d("缺少的序列号为：%1s", sb.toString());
        }
        if(!mWifiSnList.isEmpty()) {
            for (String name : mWifiSnList) {
                Timber.d("WifiSn: %1s", name);
            }
        }
        // TODO: 2021/1/21 记得清空
    }

}
