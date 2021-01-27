package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.AdaptScreenUtils;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加密码选择时间页面
 */
public class AddNewPwdSelectActivity extends BaseActivity {

    private ImageView mIvPermanent, mIvSchedule, mIvTemporary;
    private RelativeLayout mRlPermanent, mRlSchedule, mRlTemporary;
    private ConstraintLayout mClSchedule, mClTemporary;
    private Button mBtnNext;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_new_pwd_select;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_password));
        mIvPermanent = findViewById(R.id.ivPermanent);
        mIvSchedule = findViewById(R.id.ivSchedule);
        mIvTemporary = findViewById(R.id.ivTemporary);
        mRlPermanent = findViewById(R.id.rlPermanent);
        mRlSchedule = findViewById(R.id.rlSchedule);
        mRlTemporary = findViewById(R.id.rlTemporary);
        mBtnNext = findViewById(R.id.btnNext);
        mClSchedule = findViewById(R.id.clSchedule);
        mClTemporary = findViewById(R.id.clTemporary);
        applyDebouncingClickListener(mRlPermanent, mRlSchedule, mRlTemporary, mBtnNext);
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.rlPermanent) {
            permanentSwitch();
            showPermanentState();
            return;
        }
        if(view.getId() == R.id.rlSchedule) {
            scheduleSwitch();
            showSchedule();
            return;
        }
        if(view.getId() == R.id.rlTemporary) {
            temporarySwitch();
            showTemporary();
            return;
        }
        if(view.getId() == R.id.btnNext) {
            startActivity(new Intent(this, AddNewPwdNameActivity.class));
        }
    }

    private void showPermanentState() {
        mClSchedule.setVisibility(View.GONE);
        mClTemporary.setVisibility(View.GONE);
        changeBtnNext(128);
    }

    private void showSchedule() {
        mClSchedule.setVisibility(View.VISIBLE);
        mClTemporary.setVisibility(View.GONE);
        changeBtnNext(34);
    }

    private void showTemporary() {
        mClSchedule.setVisibility(View.GONE);
        mClTemporary.setVisibility(View.VISIBLE);
        changeBtnNext(128);
    }

    private void changeBtnNext(int bottomMargin) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mBtnNext.getLayoutParams();
        params.bottomMargin = AdaptScreenUtils.pt2Px(bottomMargin);
        mBtnNext.setLayoutParams(params);
    }


    private void permanentSwitch() {
        mIvPermanent.setImageResource(R.drawable.ic_home_password_icon_selected);
        mIvSchedule.setImageResource(R.drawable.ic_home_password_icon_default);
        mIvTemporary.setImageResource(R.drawable.ic_home_password_icon_default);
    }

    private void scheduleSwitch() {
        mIvPermanent.setImageResource(R.drawable.ic_home_password_icon_default);
        mIvSchedule.setImageResource(R.drawable.ic_home_password_icon_selected);
        mIvTemporary.setImageResource(R.drawable.ic_home_password_icon_default);
    }

    private void temporarySwitch() {
        mIvPermanent.setImageResource(R.drawable.ic_home_password_icon_default);
        mIvSchedule.setImageResource(R.drawable.ic_home_password_icon_default);
        mIvTemporary.setImageResource(R.drawable.ic_home_password_icon_selected);
    }

}
