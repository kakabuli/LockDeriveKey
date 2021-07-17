package com.revolo.lock.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.AuthUserDeviceAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import java.util.List;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   :为已分享的用户添加新设备
 */
public class AddDeviceForSharedUserActivity extends BaseActivity {

    private AuthUserDeviceAdapter mAuthUserDeviceAdapter;
    private String mShareUserMail, mShareFirstNameText, mShareLastNameText;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_device_for_shared_user;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_select_authorized_device));
        mShareUserMail = getIntent().getStringExtra(Constant.SHARE_USER_MAIL);
        mShareFirstNameText = getIntent().getStringExtra(Constant.SHARE_USER_FIRST_NAME);
        mShareLastNameText = getIntent().getStringExtra(Constant.SHARE_USER_LAST_NAME);
        RecyclerView rvDevice = findViewById(R.id.rvDevice);
        mAuthUserDeviceAdapter = new AuthUserDeviceAdapter(R.layout.item_auth_user_device_rv);
        rvDevice.setLayoutManager(new LinearLayoutManager(this));
        mAuthUserDeviceAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (position < 0) {
                return;
            }
            Intent intent = new Intent(AddDeviceForSharedUserActivity.this, SelectAuthorizedDeviceActivity.class);
            intent.putExtra(Constant.LOCK_DETAIL, mAuthUserDeviceAdapter.getItem(position));
            intent.putExtra(Constant.SHARE_USER_MAIL, mShareUserMail);
            intent.putExtra(Constant.SHARE_USER_FIRST_NAME, mShareFirstNameText);
            intent.putExtra(Constant.SHARE_USER_LAST_NAME, mShareLastNameText);
            startActivity(intent);
        });
        rvDevice.setAdapter(mAuthUserDeviceAdapter);
    }

    @Override
    public void doBusiness() {
        initData();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initData() {
        List<BleDeviceLocal> list = AppDatabase
                .getInstance(this)
                .bleDeviceDao()
                .findBleDevicesFromUserId(App.getInstance().getUser().getAdminUid());
        if (list == null) {
            Timber.e("initData list == null");
            return;
        }
        if (list.isEmpty()) {
            Timber.e("initData list is empty");
            return;
        }
        mAuthUserDeviceAdapter.setList(list);

    }

}
