package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.WifiSnBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.popup.WifiListPopup;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_INFO;
import static com.revolo.lock.ble.BleProtocolState.CMD_WIFI_LIST_CHECK;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加Wifi
 */
public class AddWifiActivity extends BaseActivity {

    private BleBean mBleBean;
    private WifiListPopup mWifiListPopup;
    private EditText mEtWifiName, mEtPwd;
    private boolean isShowPwd = true;

    private BleDeviceLocal mBleDeviceLocal;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if(mBleDeviceLocal == null) {
            // TODO: 2021/2/22 做处理
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_wifi;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_wifi));
        mEtWifiName = findViewById(R.id.etWifiName);
        mEtPwd = findViewById(R.id.etPwd);
        mWifiListPopup = new WifiListPopup(this);
        mWifiListPopup.setOnItemClickListener((adapter, view, position) -> {
            if(position < 0) {
                return;
            }
            mEtWifiName.setText((String) adapter.getItem(position));
            mWifiListPopup.dismiss();
        });
        applyDebouncingClickListener(findViewById(R.id.btnNext), findViewById(R.id.ivDropdown),
                findViewById(R.id.ivEye), findViewById(R.id.tvSkip));
        initLoading("Loading...");
    }

    @Override
    public void doBusiness() {
        initDevice();
        finishPreAct();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnNext) {
            gotoWifiConnectAct();
            return;
        }
        if(view.getId() == R.id.ivDropdown) {
            showOrDismissWifiList();
            return;
        }
        if(view.getId() == R.id.ivEye) {
            openOrClosePwdEye();
            return;
        }
        if(view.getId() == R.id.tvSkip) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if(App.getInstance().isWifiSettingNeedToCloseBle()) {
            if(mBleBean != null && mBleBean.getOKBLEDeviceImp() != null) {
                mBleBean.getOKBLEDeviceImp().disConnect(false);
            }
        }
        super.onDestroy();
    }

    private void finishPreAct() {
        ActivityUtils.finishActivity(AddDeviceActivity.class);
        ActivityUtils.finishActivity(AddDeviceStep1Activity.class);
        ActivityUtils.finishActivity(AddDeviceQRCodeStep2Activity.class);
        ActivityUtils.finishActivity(DoorSensorCheckActivity.class);
    }

    private void gotoWifiConnectAct() {
        String wifiSn = mEtWifiName.getText().toString().trim();
        if(TextUtils.isEmpty(wifiSn)) {
            // TODO: 2021/1/22 调整提示语
            ToastUtils.showShort("Please input wifi name!");
            return;
        }
        String wifiPwd = mEtPwd.getText().toString();
        if(TextUtils.isEmpty(wifiPwd)) {
            wifiPwd = "";
        }
        Intent intent = new Intent(this, WifiConnectActivity.class);
        intent.putExtra(Constant.WIFI_NAME, wifiSn);
        intent.putExtra(Constant.WIFI_PWD, wifiPwd);
        startActivity(intent);
    }

    private void showOrDismissWifiList() {
        runOnUiThread(() -> {
            if(mWifiListPopup == null) {
                return;
            }
            if(mWifiListPopup.isShowing()) {
                mWifiListPopup.dismiss();
            } else {
                mWifiListPopup.setPopupGravity(Gravity.BOTTOM);
                mWifiListPopup.showPopupWindow(findViewById(R.id.ivDropdown));
            }
        });

    }

    private void openOrClosePwdEye() {
        ImageView ivEye = findViewById(R.id.ivEye);
        ivEye.setImageResource(isShowPwd?R.drawable.ic_login_icon_display:R.drawable.ic_login_icon_hide);
        mEtPwd.setInputType(isShowPwd?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                :(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD));
        isShowPwd = !isShowPwd;
    }

    private void initDevice() {
        mBleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        // TODO: 2021/3/7 初始化有问题做处理
        if(mBleBean == null) {
            Timber.e("initDevice mBleBean == null");
            return;
        }
        if (mBleBean.getOKBLEDeviceImp() != null) {
            App.getInstance().openPairNotify(mBleBean.getOKBLEDeviceImp());
            mBleBean.setOnBleDeviceListener(mOnBleDeviceListener);
            checkBattery();
        }
    }

    private final OnBleDeviceListener mOnBleDeviceListener = new OnBleDeviceListener() {
        @Override
        public void onConnected(@NotNull String mac) {

        }

        @Override
        public void onDisconnected(@NotNull String mac) {

        }

        @Override
        public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
            if(value == null) {
                Timber.e("initBleListener value == null");
                return;
            }
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if(bleBean == null) {
                Timber.e("initBleListener bleBean == null");
                return;
            }
            if(bleBean.getOKBLEDeviceImp() == null) {
                Timber.e("initBleListener bleBean.getOKBLEDeviceImp() == null");
                return;
            }
            if(bleBean.getPwd1() == null) {
                Timber.e("initBleListener bleBean.getPwd1() == null");
                return;
            }
            if(bleBean.getPwd3() == null) {
                Timber.e("initBleListener bleBean.getPwd3() == null");
                return;
            }
            BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
            BleResultProcess.processReceivedData(value, mBleBean.getPwd1(), mBleBean.getPwd3(),
                    mBleBean.getOKBLEDeviceImp().getBleScanResult());
        }

        @Override
        public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

        }

        @Override
        public void onAuthSuc(@NotNull String mac) {

        }

    };

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        if(bleResultBean.getCMD() == CMD_LOCK_INFO) {
            receiveLockBaseInfo(bleResultBean);
        } else if(bleResultBean.getCMD() == CMD_WIFI_LIST_CHECK) {
            receiveWifiList(bleResultBean);
        }
    };

    private void checkBattery() {
        if(mBleBean == null) {
            Timber.e("checkBattery mBleBean == null");
            return;
        }
        showLoading();
        // wifi配网前记得查询电量
        App.getInstance().writeControlMsg(BleCommandFactory
                        .checkLockBaseInfoCommand(mBleBean.getPwd1(), mBleBean.getPwd3()),
                mBleBean.getOKBLEDeviceImp());
        // 临时加一个6秒后执行取消loading
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissLoading();
            }
        }, 6000);
    }

    private void receiveLockBaseInfo(BleResultBean bleResultBean) {
        int power = BleByteUtil.byteToInt(bleResultBean.getPayload()[11]);
        Timber.d("receiveLockBaseInfo battery power: %1d", power);
        mBleDeviceLocal.setLockPower(power);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        if(power > 20) {
            getWifiList();
        } else {
            dismissLoading();
            // TODO: 2021/3/2 不允许wifi配网
            ToastUtils.showShort("Low Battery! Can't Pair Wifi!");
        }
    }

    private void getWifiList() {
        if(mBleBean == null) {
            dismissLoading();
            Timber.e("getWifiList mBleBean == null");
            return;
        }
        if(mBleBean.getOKBLEDeviceImp() == null) {
            dismissLoading();
            Timber.e("getWifiList mBleBean.getOKBLEDeviceImp() == null");
            return;
        }
        App.getInstance().writePairMsg(BleCommandFactory.wifiListSearchCommand(), mBleBean.getOKBLEDeviceImp());
    }

    private int mWifiTotalNum = 0;
    private final HashMap<Integer, WifiSnBean> mWifiHashMap = new HashMap<>();
    private final List<String> mWifiSnList = new ArrayList<>();

    private void receiveWifiList(BleResultBean bleResultBean) {
//        Timber.d("receiveWifiList 接收信息：%1s", ConvertUtils.int2HexString(bleResultBean.getCMD()));
        if(bleResultBean.getCMD() == CMD_WIFI_LIST_CHECK) {
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
        dismissLoading();
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
        runOnUiThread(() -> {
            if(mWifiListPopup != null) {
                mWifiListPopup.updateWifiList(mWifiSnList);
                if(mWifiSnList.isEmpty()) {
                    return;
                }mEtWifiName.setText(mWifiSnList.get(0));
            }
        });

        // TODO: 2021/1/21 记得清空
    }

}
