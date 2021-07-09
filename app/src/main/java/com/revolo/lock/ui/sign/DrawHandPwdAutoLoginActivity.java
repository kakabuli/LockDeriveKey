package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.ui.TitleBar;
import com.revolo.lock.widget.handPwdUtil.GestureContentView;
import com.revolo.lock.widget.handPwdUtil.GestureDrawline;

import static com.revolo.lock.Constant.REVOLO_SP;

/**
 * author :
 * time   : 2021/3/15
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class DrawHandPwdAutoLoginActivity extends BaseActivity {

    private GestureContentView mGestureContentView;
    private TextView tvDrawTip;

    private int mCount = 3;

    @Override
    public void initData(@Nullable Bundle bundle) {
        isShowNetState = false;
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_draw_hand_pwd_auto_login;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        TitleBar titleBar = useCommonTitleBar("Gesture password");
        titleBar.getIvLeft().setVisibility(View.INVISIBLE);

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

                User user = App.getInstance().getUser();
                // 获取缓存手势密码成功的值
                String code = user.getGestureCode();
                if (inputCode.equals(code)) {
                    setResult(RESULT_OK);
                    autoLogin();
                    // new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 50);
                } else {
                    if (mCount == 0) {
                        setResult(RESULT_CANCELED);
                        Intent intent = new Intent(DrawHandPwdAutoLoginActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        //new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 50);
                    } else {
                        tvDrawTip.setText(getString(R.string.t_text_content_wrong_password));
                        mGestureContentView.clearDrawlineState(0L);
                        mCount--;
                    }

                }
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

        findViewById(R.id.tv_more_unlock).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent(DrawHandPwdAutoLoginActivity.this, SignSelectActivity.class);
            intent.putExtra(Constant.SIGN_SELECT_MODE, "draw");
            startActivity(intent);
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

    private void autoLogin() {
        String loginJson = SPUtils.getInstance(REVOLO_SP).getString(Constant.USER_LOGIN_INFO);
        if (TextUtils.isEmpty(loginJson)) {
            return;
        }
        MailLoginBeanRsp.DataBean dataBean = GsonUtils.fromJson(loginJson, MailLoginBeanRsp.DataBean.class);
        if (dataBean == null) {
            return;
        }
        App.getInstance().setUserBean(dataBean);
        User user = App.getInstance().getUser();
        if (user == null) {
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(DrawHandPwdAutoLoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);

            startActivity(intent);
            finish();
        }, 50);
    }
}
