package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.util.LinkClickableSpan;


/**
 * author : Jack
 * time   : 2020/12/23
 * E-mail : wengmaowei@kaadas.com
 * desc   : 注册页面
 */
public class RegisterActivity extends BaseActivity {

    private boolean isSelected = false;
    private boolean isShowPwd = false;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_register;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.register));
        applyDebouncingClickListener(findViewById(R.id.btnStartCreating),
                findViewById(R.id.ivEye),
                findViewById(R.id.ivSelect));

        TextView tvAgreement = findViewById(R.id.tvAgreement);
        String agreementStr = getString(R.string.terms_of_use);
        SpannableString spannableString = new SpannableString(agreementStr);
        LinkClickableSpan span = new LinkClickableSpan() {
            // TODO: 2021/1/11 跳转到协议界面
        };
        spannableString.setSpan(span, 0, agreementStr.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tvAgreement.append(getString(R.string.i_agree_to));
        tvAgreement.append(spannableString);

    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnStartCreating) {
            startActivity(new Intent(this, RegisterInputNameActivity.class));
            return;
        }
        if(view.getId() == R.id.ivEye) {
            ImageView ivEye = findViewById(R.id.ivEye);
            ivEye.setImageResource(isShowPwd?R.drawable.ic_login_icon_display:R.drawable.ic_login_icon_hide);
            EditText etPwd = findViewById(R.id.etPwd);
            etPwd.setInputType(isShowPwd?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    :(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD));
            isShowPwd = !isShowPwd;
            return;
        }
        if(view.getId() == R.id.ivSelect) {
            ImageView ivSelect = findViewById(R.id.ivSelect);
            ivSelect.setImageResource(isSelected?R.drawable.ic_sign_in_icon_selected:R.drawable.ic_sign_in_icon_default);
            isSelected = !isSelected;
        }
    }
}
