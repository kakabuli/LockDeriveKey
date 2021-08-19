package com.revolo.lock.ui.device.lock.setting;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.PostNotDisturbModeBeanReq;
import com.revolo.lock.bean.respone.NotDisturbModeBeanRsp;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * 勿扰模式
 */
public class DisturbModeActivity extends BaseActivity {
    private BleDeviceLocal mBleDeviceLocal;
    private ConstraintLayout mDisturbStateLayout;
    private ImageView mDisturbImage;
    private TextView mDisturbWhatLayout, mDisturbWhatContent;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.disturb_mode_activity_activity;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        onRegisterEventBus();
        useCommonTitleBar(getString(R.string.disturb_mode_activity_title));
        mDisturbStateLayout = findViewById(R.id.disturb_mode_state_layout);
        mDisturbImage = findViewById(R.id.disturb_mode_state_icon);
        mDisturbWhatLayout = findViewById(R.id.disturb_mode_what_layout);
        mDisturbWhatContent = findViewById(R.id.disturb_mode_what_content);
        applyDebouncingClickListener(mDisturbStateLayout, mDisturbWhatLayout);
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        mDisturbImage.setImageResource(mBleDeviceLocal.isDoNotDisturbMode() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息

        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                Timber.e("%s", lockMessage.toString());

            } else {
                switch (lockMessage.getResultCode()) {
                    // 超时或者其他错误
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTO:
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTOTIME:
                        dismissLoading();
                        break;
                }

            }
        }
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (mDisturbWhatLayout.getId() == view.getId()) {
            if (mDisturbWhatContent.getVisibility() == View.GONE) {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_icon_more_close);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mDisturbWhatLayout.setCompoundDrawables(null, null, drawable, null);
                mDisturbWhatContent.setVisibility(View.VISIBLE);
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_icon_more_open);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mDisturbWhatLayout.setCompoundDrawables(null, null, drawable, null);
                mDisturbWhatContent.setVisibility(View.GONE);
            }
        } else if (mDisturbStateLayout.getId() == view.getId()) {
            openOrCloseNotification();
        }
    }
    private void openOrCloseNotification() {

        String token = App.getInstance().getUserBean().getToken();
        String uid = App.getInstance().getUserBean().getUid();
        PostNotDisturbModeBeanReq req = new PostNotDisturbModeBeanReq();
        req.setOpenlockPushSwitch(mBleDeviceLocal.isDoNotDisturbMode());
        req.setUid(uid);
        Observable<NotDisturbModeBeanRsp> notDisturbModeBeanRspObservable = HttpRequest.getInstance().postPushSwitch(token, req);
        ObservableDecorator.decorate(notDisturbModeBeanRspObservable).safeSubscribe(new Observer<NotDisturbModeBeanRsp>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull NotDisturbModeBeanRsp notDisturbModeBeanRsp) {
                if (notDisturbModeBeanRsp.getCode().equals("200")) {
                    mBleDeviceLocal.setDoNotDisturbMode(!mBleDeviceLocal.isDoNotDisturbMode());
                    App.getInstance().setBleDeviceLocal(mBleDeviceLocal);
                    AppDatabase.getInstance(DisturbModeActivity.this).bleDeviceDao().update(mBleDeviceLocal);
                    mDisturbImage.setImageResource(mBleDeviceLocal.isDoNotDisturbMode() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
                } else if (notDisturbModeBeanRsp.equals("444")) {
                    App.getInstance().logout(true, DisturbModeActivity.this);
                }
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}

