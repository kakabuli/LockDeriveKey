package com.revolo.lock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ui.device.DeviceFragment;
import com.revolo.lock.ui.device.add.AddDeviceActivity;
import com.revolo.lock.ui.mine.MineFragment;
import com.revolo.lock.ui.user.UserFragment;
import com.revolo.lock.ui.view.MainViewNavController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.Navigation;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity {

    private boolean isGotoAddDeviceAct = false;
    private int isMainItemIndex = -1;
    private FragmentManager mSupportFragmentManager;
    private FragmentTransaction mTransaction;
    private List<Fragment> mFragments = new ArrayList<>();
    private DeviceFragment deviceFragment;
    private UserFragment userFragment;
    private MineFragment mineFragment;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.COMMAND)) {
            String command = intent.getStringExtra(Constant.COMMAND);
            isGotoAddDeviceAct = command.equals(Constant.ADD_DEVICE);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navView.setOnNavigationItemSelectedListener(item -> {
            if (isMainItemIndex != item.getItemId()) {
                isMainItemIndex = item.getItemId();
                if (item.getItemId() == R.id.navigation_device) {
                    addDeviceFragment();
                } else if (item.getItemId() == R.id.navigation_user) {
                    addUserFragment();
                } else if (item.getItemId() == R.id.navigation_mine) {
                    addMineFragment();
                }
            }
            return true;
        });
        //NavigationUI.setupWithNavController(navView, navController);
        mSupportFragmentManager = getSupportFragmentManager();
        mTransaction = mSupportFragmentManager.beginTransaction();
        App.getInstance().setMainActivity(this);
        addDeviceFragment();
        if (isGotoAddDeviceAct) {
            startActivity(new Intent(this, AddDeviceActivity.class));
        }
    }

    private void addDeviceFragment() {

        if (deviceFragment == null) {
            deviceFragment = new DeviceFragment();
            mFragments.add(deviceFragment);
            hideOthersFragment(deviceFragment, true);
        } else {
            hideOthersFragment(deviceFragment, false);
        }
    }

    private void addUserFragment() {

        if (userFragment == null) {
            userFragment = new UserFragment();
            mFragments.add(userFragment);
            hideOthersFragment(userFragment, true);
        } else {
            hideOthersFragment(userFragment, false);
        }
    }

    private void addMineFragment() {
        if (mineFragment == null) {
            mineFragment = new MineFragment();
            mFragments.add(mineFragment);
            hideOthersFragment(mineFragment, true);
        } else {
            hideOthersFragment(mineFragment, false);
        }
    }

    private void hideOthersFragment(Fragment showFragment, boolean add) {
        mTransaction = mSupportFragmentManager.beginTransaction();
        if (add) {
            mTransaction.add(R.id.nav_host_fragment, showFragment);
        }

        for (Fragment fragment : mFragments) {
            if (showFragment.equals(fragment)) {
                mTransaction.show(fragment);
            } else {
                mTransaction.hide(fragment);
            }
        }
        mTransaction.commit();
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

}