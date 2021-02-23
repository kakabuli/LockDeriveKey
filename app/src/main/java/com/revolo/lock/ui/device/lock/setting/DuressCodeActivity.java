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

import static com.revolo.lock.ble.BleProtocolState.CMD_DURESS_PWD_SWITCH;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 胁迫密码
 */
public class DuressCodeActivity extends BaseActivity {
    // TODO: 2021/2/22 所有发送指令都要做超时

    private ConstraintLayout mClInputEmail;
    private ImageView mIvDuressCodeEnable;
    private BleDeviceLocal mBleDeviceLocal;

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
        return R.layout.activity_duress_code;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_duress_code));
        mClInputEmail = findViewById(R.id.clInputEmail);
        mIvDuressCodeEnable = findViewById(R.id.ivDuressCodeEnable);
        initUI();
        applyDebouncingClickListener(findViewById(R.id.ivDuressCodeEnable));
    }

    @Override
    public void doBusiness() {
        initBleListener();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivDuressCodeEnable) {
            openOrCloseDuressPwd();
        }
    }

    private void openOrCloseDuressPwd() {
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
        int control = mBleDeviceLocal.isDuress()? BleCommandState.DURESS_PWD_OPEN:BleCommandState.DURESS_PWD_CLOSE;
        App.getInstance().writeControlMsg(BleCommandFactory.duressPwdSwitch(control, pwd1, pwd3));
    }

    private void initUI() {
        runOnUiThread(() -> {
            mIvDuressCodeEnable.setImageResource(mBleDeviceLocal.isDuress()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
            mClInputEmail.setVisibility(mBleDeviceLocal.isDuress()?View.VISIBLE:View.GONE);
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
            processDuress(bean);
        }
    }

    private void processDuress(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if(state == 0x00) {
            mBleDeviceLocal.setDuress(!mBleDeviceLocal.isDuress());
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
            initUI();
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

}
