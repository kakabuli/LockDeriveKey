package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.User;

/**
 * author : Jack
 * time   : 2021/1/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 设置页面
 */
public class SettingActivity extends BaseActivity {

    private ImageView ivGestureCodeEnable, ivEnableTouchIDEnable, ivEnableFaceIDEnable;
    private User mUser;
    private static final int REQUEST_CODE_OPEN_GESTURE_CODE = 1999;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mUser = App.getInstance().getUser();
        if(mUser == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_setting;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_setting));
        ivGestureCodeEnable = findViewById(R.id.ivGestureCodeEnable);
        ivEnableTouchIDEnable = findViewById(R.id.ivEnableTouchIDEnable);
        ivEnableFaceIDEnable = findViewById(R.id.ivEnableFaceIDEnable);
        applyDebouncingClickListener(ivGestureCodeEnable, ivEnableTouchIDEnable, ivEnableFaceIDEnable);
    }

    @Override
    public void doBusiness() {
        refreshUI();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivGestureCodeEnable) {
            if(mUser.isUseGesturePassword()) {
                mUser.setUseGesturePassword(false);
                AppDatabase.getInstance(this).userDao().update(mUser);
                refreshUI();
            } else {
                Intent intent = new Intent(this, OpenDrawHandPwdActivity.class);
                startActivityForResult(intent, REQUEST_CODE_OPEN_GESTURE_CODE);
            }
            return;
        }
        if(view.getId() == R.id.ivEnableTouchIDEnable) {
            return;
        }
        if(view.getId() == R.id.ivEnableFaceIDEnable) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_OPEN_GESTURE_CODE) {
            if(resultCode == RESULT_OK) {
                mUser.setUseGesturePassword(true);
                AppDatabase.getInstance(this).userDao().update(mUser);
                refreshUI();
            }
        }
    }

    private void refreshUI() {
        runOnUiThread(() -> {
            ivGestureCodeEnable.setImageResource(mUser.isUseGesturePassword()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
            ivEnableTouchIDEnable.setImageResource(mUser.isUseTouchId()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
            ivEnableFaceIDEnable.setImageResource(mUser.isUseFaceId()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
        });
    }

}
