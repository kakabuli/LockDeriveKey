package com.revolo.lock.ui.device.lock.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.SPUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleCommandFactory;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 自动上锁设置
 */
public class AutoLockActivity extends BaseActivity {

    private SeekBar mSeekBar;
    private TextView mTvTime;
    private int mTime = 0;
    private ImageView ivDetectionLockEnable, ivAutoLockEnable;
    boolean isOpenAutoLock= false;
    private ConstraintLayout clSetLockTime;
    // TODO: 2021/2/8 存在锁的数据列表里
    private boolean isShowSetLockTime;
    // TODO: 2021/2/8 临时的bool值来判断是否开启自动上锁功能，后续需要通过查询状态来实现功能
    private final String TEST_SET_LOCK_TIME = "TestSetLockTime";
    private final String TEST_AUTO_LOCK = "TestAutoLock";

    @Override
    public void initData(@Nullable Bundle bundle) {
        isShowSetLockTime = SPUtils.getInstance().getBoolean(TEST_SET_LOCK_TIME, false);
        isOpenAutoLock = SPUtils.getInstance().getBoolean(TEST_AUTO_LOCK);
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
        clSetLockTime = findViewById(R.id.clSetLockTime);
        ivAutoLockEnable = findViewById(R.id.ivAutoLockEnable);
        ivDetectionLockEnable = findViewById(R.id.ivDetectionLockEnable);
        applyDebouncingClickListener(ivAutoLockEnable, ivDetectionLockEnable);
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

        clSetLockTime.setVisibility(isShowSetLockTime?View.VISIBLE:View.GONE);
        ivDetectionLockEnable.setImageResource(isShowSetLockTime?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivAutoLockEnable) {
            openOrCloseAutoLock();
            return;
        }
        if(view.getId() == R.id.ivDetectionLockEnable) {
            isShowSetLockTime = !isShowSetLockTime;
            SPUtils.getInstance().put(TEST_SET_LOCK_TIME, isShowSetLockTime);
            clSetLockTime.setVisibility(isShowSetLockTime?View.VISIBLE:View.GONE);
            ivDetectionLockEnable.setImageResource(isShowSetLockTime?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
        }
    }

    // TODO: 2021/2/8 要接收回调处理
    private void openOrCloseAutoLock() {
        byte[] value = new byte[1];
        value[0] = (byte) (isOpenAutoLock?0x01:0x00);
        isOpenAutoLock = !isOpenAutoLock;
        ivAutoLockEnable.setImageResource(isOpenAutoLock?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
        SPUtils.getInstance().put(TEST_AUTO_LOCK, isOpenAutoLock);
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

}
