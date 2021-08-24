package com.revolo.lock.ui.device.add;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaTimestamp;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.util.LockEasyPermissions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Time;
import java.util.List;
import java.util.Timer;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * author : yi
 * time   : 20210817
 * E-mail :
 * desc   : 添加设备第一步
 */
public class AddDevice1StepActivity extends BaseActivity {
    private VideoView mVideoView;
    private ImageView mHintImageView;
    private ImageView mPlayStateView;
    private int mPlayState = -1;//0正常播放、1、暂停、-1、初始化
    private int mCurrTime = 0;

    @Override
    public void initData(@Nullable Bundle bundle) {
        isShowNetState = false;
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_device_step1_view;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.add_device_activity_title));
        mVideoView = findViewById(R.id.imageView_voice);
        mHintImageView = findViewById(R.id.imageView);
        mPlayStateView = findViewById(R.id.imageview_play);
        applyDebouncingClickListener(findViewById(R.id.btnNext), mVideoView, mHintImageView, mPlayStateView);
        initVoieo();
    }

    @Override
    public void doBusiness() {

    }

    private void initVoieo() {
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //结束
                clearVoieo();
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onPrepared(MediaPlayer mp) {
                //准备好后
                if (mCurrTime != 0) {
                    mVideoView.seekTo(mCurrTime);
                }
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                clearVoieo();
                return false;
            }
        });
        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearVoieo();
    }

    private void clearVoieo() {
        if (null != mVideoView) {
            mVideoView.setVisibility(View.GONE);
            if (mVideoView.isPlaying()) {
                mVideoView.stopPlayback();
            }
            mVideoView.suspend();
        }
        mPlayState = -1;
        mCurrTime = 0;
        mHintImageView.setVisibility(View.VISIBLE);
        mPlayStateView.setVisibility(View.VISIBLE);
    }

    private void startPlay() {
        mPlayState = 0;
        mPlayStateView.setVisibility(View.GONE);
        mHintImageView.setVisibility(View.INVISIBLE);
        mVideoView.setVisibility(View.VISIBLE);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.smart_voice_1);
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        mVideoView.start();
    }

    private void rePlay() {
        mPlayState = 0;
        mPlayStateView.setVisibility(View.GONE);
        Timber.e("dsagag:" + mCurrTime);
        mVideoView.start();
    }

    private void pausePlay() {
        mPlayState = 1;
        mPlayStateView.setVisibility(View.VISIBLE);
        mCurrTime = mVideoView.getCurrentPosition();
        Timber.e("dsagag:" + mCurrTime);
        mVideoView.pause();
    }

    private void onClickPa() {
        if (mPlayState == -1) {
            //播放
            startPlay();
        } else if (mPlayState == 0) {
            //暂停
            pausePlay();
        } else {
            rePlay();
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnNext) {
            clearVoieo();
            startActivity(new Intent(this, AddDevice2StepActivity.class));
        } else if (view.getId() == mVideoView.getId()) {
            //播放控件
            onClickPa();
        } else if (view.getId() == mHintImageView.getId()) {
            //播放背景
            onClickPa();
        } else if (view.getId() == mPlayStateView.getId()) {
            //播放按键
            onClickPa();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
