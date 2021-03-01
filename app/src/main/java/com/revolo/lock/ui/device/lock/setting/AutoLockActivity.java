package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

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
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_PARAMETER_CHANGED;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 自动上锁设置
 */
public class AutoLockActivity extends BaseActivity {

    private SeekBar mSeekBar;
    private TextView mTvTime, mTvDetectionLock;
    private int mTime = 0;
    private ImageView mIvDetectionLockEnable, mIvAutoLockEnable;
    private ConstraintLayout mClSetLockTime;
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
        return R.layout.activity_auto_lock;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_auto_lock));
        mSeekBar = findViewById(R.id.seekBar);
        mTvTime = findViewById(R.id.tvTime);
        mTvDetectionLock = findViewById(R.id.tvDetectionLock);
        mClSetLockTime = findViewById(R.id.clSetLockTime);
        mIvAutoLockEnable = findViewById(R.id.ivAutoLockEnable);
        mIvDetectionLockEnable = findViewById(R.id.ivDetectionLockEnable);
        applyDebouncingClickListener(mIvAutoLockEnable, mIvDetectionLockEnable);
        mSeekBar.setMax(140);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChange(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                stopTrackingTouch(seekBar);
            }
        });

        initUI();
    }

    @Override
    public void doBusiness() {
        initBleListener();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivAutoLockEnable) {
            openOrCloseAutoLock();
            return;
        }
        if(view.getId() == R.id.ivDetectionLockEnable) {
            openOrCloseDetectionLock();
        }
    }

    private void initUI() {
        runOnUiThread(() -> {
            mIvAutoLockEnable.setImageResource(mBleDeviceLocal.isAutoLock()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
            mTvDetectionLock.setVisibility(mBleDeviceLocal.isAutoLock()?View.VISIBLE:View.GONE);
            mIvDetectionLockEnable.setVisibility(mBleDeviceLocal.isAutoLock()?View.VISIBLE:View.GONE);
            mClSetLockTime.setVisibility(mBleDeviceLocal.isAutoLock()?View.VISIBLE:View.GONE);
            mIvDetectionLockEnable
                    .setImageResource(mBleDeviceLocal.isDetectionLock()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
        });

    }

    private void openOrCloseDetectionLock() {
        // TODO: 2021/2/22 服务器开启，或者本地开关 2021/2/22 开启服务通知
        mBleDeviceLocal.setDetectionLock(!mBleDeviceLocal.isDetectionLock());
        initUI();
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    // TODO: 2021/2/8 要接收回调处理
    private void openOrCloseAutoLock() {
        byte[] value = new byte[1];
        value[0] = (byte) (mBleDeviceLocal.isAutoLock()?0x01:0x00);
        App.getInstance().writeControlMsg(BleCommandFactory
                .lockParameterModificationCommand((byte) 0x04, (byte) 0x01, value,
                        App.getInstance().getBleBean().getPwd1(),
                        App.getInstance().getBleBean().getPwd3()));
    }

    private void stopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if(progress >= 0 && progress < 10) {
            mSeekBar.setProgress(0);
            mTvTime.setText("0s");
            mTime = 0;
        } else if(progress >= 10 && progress < 20) {
            mSeekBar.setProgress(10);
            mTvTime.setText("5s");
            mTime = 5;
        } else if(progress >= 20 && progress < 30) {
            mSeekBar.setProgress(20);
            mTvTime.setText("10s");
            mTime = 10;
        } else if(progress >= 30 && progress < 40) {
            mSeekBar.setProgress(30);
            mTvTime.setText("15s");
            mTime = 15;
        } else if(progress >= 40 && progress < 50) {
            mSeekBar.setProgress(40);
            mTvTime.setText("20s");
            mTime = 20;
        } else if(progress >= 50 && progress < 60) {
            mSeekBar.setProgress(50);
            mTvTime.setText("25s");
            mTime = 25;
        } else if(progress >= 60 && progress < 70) {
            mSeekBar.setProgress(60);
            mTvTime.setText("30s");
            mTime = 30;
        } else if(progress >= 70 && progress < 80) {
            mSeekBar.setProgress(70);
            mTvTime.setText("1min");
            mTime = 60;
        } else if(progress >= 80 && progress < 90) {
            mSeekBar.setProgress(80);
            mTvTime.setText("2min");
            mTime = 120;
        } else if(progress >= 90 && progress < 100) {
            mSeekBar.setProgress(90);
            mTvTime.setText("5min");
            mTime = 300;
        } else if(progress >= 100 && progress < 110) {
            mSeekBar.setProgress(100);
            mTvTime.setText("10min");
            mTime = 600;
        } else if(progress >= 110 && progress < 120) {
            mSeekBar.setProgress(110);
            mTvTime.setText("15min");
            mTime = 900;
        } else if(progress >= 120 && progress < 130) {
            mSeekBar.setProgress(130);
            mTvTime.setText("20min");
            mTime = 1200;
        } else if(progress >= 130 && progress < 140) {
            mSeekBar.setProgress(130);
            mTvTime.setText("30min");
            mTime = 1800;
        }
        App.getInstance().writeControlMsg(BleCommandFactory
                .setAutoLockTime(mTime,
                        App.getInstance().getBleBean().getPwd1(),
                        App.getInstance().getBleBean().getPwd3()));
    }

    private void progressChange(int progress) {
        if(progress == 0) {
            mTvTime.setText("0s");
        } else if(progress == 10) {
            mTvTime.setText("5s");
        } else if(progress == 20) {
            mTvTime.setText("10s");
        } else if(progress == 30) {
            mTvTime.setText("15s");
        } else if(progress == 40) {
            mTvTime.setText("20s");
        } else if(progress == 50) {
            mTvTime.setText("25s");
        } else if(progress == 60) {
            mTvTime.setText("30s");
        } else if(progress == 70) {
            mTvTime.setText("1min");
        } else if(progress == 80) {
            mTvTime.setText("2min");
        } else if(progress == 90) {
            mTvTime.setText("5min");
        } else if(progress == 100) {
            mTvTime.setText("10min");
        } else if(progress == 110) {
            mTvTime.setText("15min");
        } else if(progress == 120) {
            mTvTime.setText("20min");
        } else if(progress == 140) {
            mTvTime.setText("30min");
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
        if(bean.getCMD() == CMD_LOCK_PARAMETER_CHANGED) {
            processAutoLock(bean);
        }
    }

    private void processAutoLock(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if(state == 0x00) {
            mBleDeviceLocal.setAutoLock(!mBleDeviceLocal.isAutoLock());
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
            initUI();
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

}
