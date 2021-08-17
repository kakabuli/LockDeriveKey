package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;


/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加新的密码
 */
public class AddInputNewPwdActivity extends BaseActivity {

    private EditText mEtPwd;
    private TextView mTvIntroduceTitle, mTvIntroduceContent;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_input_new_pwd;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_password));
        mEtPwd = findViewById(R.id.etPwd);
        mTvIntroduceTitle = findViewById(R.id.tvTextThree);
        mTvIntroduceContent = findViewById(R.id.tvTextFour);
        applyDebouncingClickListener(findViewById(R.id.btnNext), mTvIntroduceTitle);
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnNext) {
            String pwd = mEtPwd.getText().toString().trim();
            if (RegexUtils.isMatch("(?:0(?=1)|1(?=2)|2(?=3)|3(?=4)|4(?=5)|5(?=6)|6(?=7)|7(?=8)|8(?=9)){3,10}\\d", pwd)
                    || RegexUtils.isMatch("(?:9(?=8)|8(?=7)|7(?=6)|6(?=5)|5(?=4)|4(?=3)|3(?=2)|2(?=1)|1(?=0)){3,10}\\d", pwd)
                    || RegexUtils.isMatch("([\\d])\\1{2,}", pwd)) {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_dont_enter_a_simple_pwd);
                return;
            }
            if (pwd.startsWith("911")) {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.tip_password_start_with_911);
                return;
            }
            if (pwd.length() >= 4 && pwd.length() <= 12) {
                Intent intent = new Intent(this, AddNewPwdSelectActivity.class);
                intent.putExtra(Constant.USER_PWD, pwd);
                startActivity(intent);
                finish();
            } else {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_right_pwd);
            }
        } else if (view.getId() == R.id.tvTextThree) {
            if (mTvIntroduceContent.getVisibility() == View.INVISIBLE) {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_icon_more_close);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mTvIntroduceTitle.setCompoundDrawables(null, null, drawable, null);
                mTvIntroduceContent.setVisibility(View.VISIBLE);
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_icon_more_open);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mTvIntroduceTitle.setCompoundDrawables(null, null, drawable, null);
                mTvIntroduceContent.setVisibility(View.INVISIBLE);
            }
        }
    }
}
