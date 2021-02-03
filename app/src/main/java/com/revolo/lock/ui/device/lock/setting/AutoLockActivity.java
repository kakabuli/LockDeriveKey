package com.revolo.lock.ui.device.lock.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @Override
    public void initData(@Nullable Bundle bundle) {

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
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

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
