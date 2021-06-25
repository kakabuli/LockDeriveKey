package com.revolo.lock.ui.mine;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.widget.handPwdUtil.GestureContentView;
import com.revolo.lock.widget.handPwdUtil.GestureDrawline;

/**
 * author :
 * time   : 2021/3/15
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class OpenDrawHandPwdActivity extends BaseActivity {

    private GestureContentView mGestureContentView;
    private boolean mIsFirstInput = true;
    private String mFirstPassword = null;
    private TextView tvDrawTip;

    @Override
    public void initData(@Nullable Bundle bundle) {
        isShowNetState = false;
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_open_draw_hand_pwd;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_change_gesture_password));

        tvDrawTip = findViewById(R.id.tvDrawTip);
        FrameLayout gestureContainer = findViewById(R.id.gesture_container);
        mGestureContentView = new GestureContentView(this, false, "", new GestureDrawline.GestureCallBack() {
            @Override
            public void onGestureCodeInput(String inputCode) {
                if (!isInputPassValidate(inputCode)) {
                    tvDrawTip.setText(getResources().getString(R.string.at_least_4_points_please_redraw));
                    mGestureContentView.clearDrawlineState(0L);
                    return;
                }
                if (mIsFirstInput) {
                    mFirstPassword = inputCode;
                    tvDrawTip.setText(getResources().getString(R.string.draw_the_gesture_code_again));
                    mGestureContentView.clearDrawlineState(0L);
                } else {
                    if (inputCode.equals(mFirstPassword)) {
                        mGestureContentView.clearDrawlineState(0L);
                        User user = App.getInstance().getUser();
                        //缓存手势密码成功的值
                        String code = user.getGestureCode();
                        if (!TextUtils.isEmpty(code)) {
                            user.setGestureCode("");
                        }
                        user.setGestureCode(inputCode);
                        AppDatabase.getInstance(OpenDrawHandPwdActivity.this).userDao().update(user);
                        setResult(RESULT_OK);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 50);

                    } else {
                        tvDrawTip.setText(getResources().getString(R.string.drawing_incorrectly));
                        // 左右移动动画
                        Animation shakeAnimation = AnimationUtils.loadAnimation(OpenDrawHandPwdActivity.this, R.anim.shake);
                        tvDrawTip.startAnimation(shakeAnimation);
                        // 保持绘制的线，1.5秒后清除
                        mGestureContentView.clearDrawlineState(1300L);
                    }
                }
                mIsFirstInput = false;
            }

            @Override
            public void checkedSuccess() {

            }

            @Override
            public void checkedFail() {


            }
        });
        // 设置手势解锁显示到哪个布局里面
        mGestureContentView.setParentView(gestureContainer);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private boolean isInputPassValidate(String inputPassword) {
        if (TextUtils.isEmpty(inputPassword) || inputPassword.length() < 4) {
            return false;
        }
        return true;
    }
}
