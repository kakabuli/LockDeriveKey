package com.revolo.lock.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ui.device.add.AddDeviceActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import timber.log.Timber;


public class MainActivity extends BaseActivity {

    private boolean isGotoAddDeviceAct = false;
    
    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.COMMAND)) {
            String command = intent.getStringExtra(Constant.COMMAND);
            isGotoAddDeviceAct = command.equals(Constant.ADD_DEVICE);
        }
        getAlexaIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getAlexaIntent(intent);
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
        App.getInstance().setMainActivity(this);
        if(isGotoAddDeviceAct) {
            startActivity(new Intent(this, AddDeviceActivity.class));
        }
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void getAlexaIntent(Intent intent) {
        String action = intent.getAction();
        if(Intent.ACTION_VIEW.equals(action)) {
            Uri data = getIntent().getData();

            if (data != null) {
                String code = data.getQueryParameter("code");
                String scope = data.getQueryParameter("scope");
                String state = data.getQueryParameter("state");
                Timber.d("code: %1s, scope: %2s, state: %3s", code, scope, state);
            }
        }
    }

}