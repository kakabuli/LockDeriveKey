package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;

import java.nio.charset.StandardCharsets;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_PARAMETER_CHECK;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门锁信息
 */
public class DoorLockInformationActivity extends BaseActivity {

    private TextView mTvWifiVersion;
    private TextView mTvFirmwareVersion;
    private View mVVersion;
    private DeviceUnbindBeanReq mReq;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.UNBIND_REQ)) {
            mReq = intent.getParcelableExtra(Constant.UNBIND_REQ);
        } else {
            // TODO: 2021/2/6 提示没从上一个页面传递数据过来
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_door_lock_infomation;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_door_lock_information));
        mVVersion = findViewById(R.id.vVersion);
        TextView tvLockSn = findViewById(R.id.tvLockSn);
        mTvWifiVersion = findViewById(R.id.tvWifiVersion);
        mTvFirmwareVersion = findViewById(R.id.tvFirmwareVersion);
        applyDebouncingClickListener(mTvFirmwareVersion, mTvWifiVersion);

        String esn = mReq.getWifiSN();
        tvLockSn.setText(TextUtils.isEmpty(esn)?"":esn);

    }

    @Override
    public void doBusiness() {
        initBleListener();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.tvFirmwareVersion) {
            // TODO: 2021/2/7 检查固件版本，并可能升级
            return;
        }
        if(view.getId() == R.id.tvWifiVersion) {
            // TODO: 2021/2/7 检查固件版本，并可能升级
        }
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        // TODO: 2021/2/7 获取版本信息
        if(bean.getCMD() == CMD_LOCK_PARAMETER_CHECK) {
            if(bean.getPayload()[0] == 0x00) {
                if(bean.getPayload()[1] == 0x03) {
                    // 锁的软件版本
                    runOnUiThread(() -> {
                        byte[] verBytes = new byte[9];
                        System.arraycopy(bean.getPayload(), 2, verBytes, 0, verBytes.length);
                        String verStr = new String(verBytes, StandardCharsets.UTF_8);
                        mTvFirmwareVersion.setText(verStr);
                    });

                } else {
                    // TODO: 2021/2/7 其他的数据处理
                }
            } else {
                // TODO: 2021/2/7 信息失败了的操作
            }
        }
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
        });
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                App.getInstance().writeControlMsg(BleCommandFactory
                        .lockParameterCheckCommand((byte) 0x03,
                                App.getInstance().getBleBean().getPwd1(),
                                App.getInstance().getBleBean().getPwd3()));
            }
        }, 50);
    }

}
