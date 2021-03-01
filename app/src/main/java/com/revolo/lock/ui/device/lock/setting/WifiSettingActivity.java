package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.WIFI_CONTROL_OPEN;
import static com.revolo.lock.ble.BleProtocolState.CMD_DURESS_PWD_SWITCH;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : wifi设置
 */
public class WifiSettingActivity extends BaseActivity {

    private BleDeviceLocal mBleDeviceLocal;
    private ConstraintLayout clTip;
    private ImageView ivWifiEnable;
    private boolean isWifiConnected = false;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(!intent.hasExtra(Constant.BLE_DEVICE)) {
            // TODO: 2021/2/22 处理
            finish();
            return;
        }
        mBleDeviceLocal = intent.getParcelableExtra(Constant.BLE_DEVICE);
        if(mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_wifi_setting;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_wifi_setting));
        clTip = findViewById(R.id.clTip);
        ivWifiEnable = findViewById(R.id.ivWifiEnable);

        updateUI();
        applyDebouncingClickListener(ivWifiEnable);
    }

    @Override
    public void doBusiness() {
        initBleListener();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivWifiEnable) {
            if(isWifiConnected) {
                // TODO: 2021/2/26 执行关闭wifi
                closeWifi();
            } else {
                // TODO: 2021/2/26 跳转到连接wifi页面
//                Intent intent = new Intent(this, AddWifiActivity.class);
//                intent.putExtra(Constant.LOCK_DETAIL, mBleDeviceLocal);
//                startActivity(intent);
                openWifi();
            }
        }
    }


    private void updateUI() {
        if(mBleDeviceLocal.getConnectedType() == 1) {
            // Wifi
            updateWifiState();
        } else if(mBleDeviceLocal.getConnectedType() == 2) {
            // 蓝牙
            updateBleState();
        } else {
            // TODO: 2021/2/26 do something
        }
    }

    private void openWifi() {
        if(App.getInstance().getBleBean() == null) {
            return;
        }
        byte[] pwd1 = App.getInstance().getBleBean().getPwd1();
        byte[] pwd3 = App.getInstance().getBleBean().getPwd3();
        if(pwd1 == null) {
            return;
        }
        if(pwd3 == null) {
            return;
        }
        App.getInstance().writeControlMsg(BleCommandFactory.wifiSwitch(WIFI_CONTROL_OPEN, pwd1, pwd3));
    }

    private void closeWifi() {
        if(App.getInstance().getBleBean() == null) {
            return;
        }
        byte[] pwd1 = App.getInstance().getBleBean().getPwd1();
        byte[] pwd3 = App.getInstance().getBleBean().getPwd3();
        if(pwd1 == null) {
            return;
        }
        if(pwd3 == null) {
            return;
        }
        App.getInstance().writeControlMsg(BleCommandFactory.wifiSwitch(BleCommandState.WIFI_CONTROL_CLOSE, pwd1, pwd3));
    }

    private void updateWifiState() {
        runOnUiThread(() -> {
            ivWifiEnable.setImageResource(R.drawable.ic_icon_switch_open);
            clTip.setVisibility(View.GONE);
            isWifiConnected = true;
        });
    }

    private void updateBleState() {
        runOnUiThread(() -> {
            ivWifiEnable.setImageResource(R.drawable.ic_icon_switch_close);
            clTip.setVisibility(View.VISIBLE);
            isWifiConnected = false;
        });
    }

    private void initBleListener() {
        App.getInstance().setOnBleDeviceListener(new OnBleDeviceListener() {
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
                BleResultProcess.processReceivedData(value,
                        App.getInstance().getBleBean().getPwd1(),
                        App.getInstance().getBleBean().getPwd3(),
                        App.getInstance().getBleBean().getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(String uuid, byte[] value, boolean success) {

            }

            @Override
            public void onAuthSuc() {

            }
        });
        // TODO: 2021/2/8 查询一下当前设置
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        if(bean.getCMD() == CMD_DURESS_PWD_SWITCH) {
            processWifiSwitch(bean);
        }
    }

    private void processWifiSwitch(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if(state == 0x00) {
            isWifiConnected = !isWifiConnected;
            mBleDeviceLocal.setConnectedType(isWifiConnected?1:2);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
            updateUI();
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

}
