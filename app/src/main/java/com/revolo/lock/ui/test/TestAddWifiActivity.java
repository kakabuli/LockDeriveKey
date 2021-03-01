package com.revolo.lock.ui.test;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.ble.bean.WifiSnBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加Wifi
 */
public class TestAddWifiActivity extends BaseActivity {

    private BleBean mBleBean;
    private TextView mTvLog;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_test_add_wifi;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_wifi));
        mTvLog = findViewById(R.id.tvLog);
        mTvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    public void doBusiness() {
        initDevice();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    protected void onDestroy() {
        App.getInstance().clearBleDeviceListener();
        super.onDestroy();
    }

    private void initDevice() {
        mBleBean = App.getInstance().getBleBean();
        if(mBleBean.getOKBLEDeviceImp() != null) {
            App.getInstance().openPairNotify();
            App.getInstance().setOnBleDeviceListener(mOnBleDeviceListener);
            App.getInstance().writePairMsg(BleCommandFactory.wifiListSearchCommand());
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
            BleResultProcess.processReceivedData(value, mBleBean.getPwd1(), null,
                    mBleBean.getOKBLEDeviceImp().getBleScanResult());
        }

        @Override
        public void onWriteValue(String uuid, byte[] value, boolean success) {

        }

        @Override
        public void onAuthSuc() {

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
            addLog(StringUtils.format("total: %1d, no: %2d, value: %3s\n", mWifiTotalNum, no, ConvertUtils.bytes2HexString(payload)));

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
            StringBuilder stringBuilder = new StringBuilder();
            for (String name : mWifiSnList) {
                stringBuilder.append(StringUtils.format("WifiSn: %1s\n", name));
            }
            addLog(stringBuilder.toString());
        }
        // TODO: 2021/1/21 记得清空
    }

    private void addLog(String msg) {
        runOnUiThread(() -> {
            if(mTvLog != null) {
                mTvLog.append(msg);
            }
        });
    }

}
