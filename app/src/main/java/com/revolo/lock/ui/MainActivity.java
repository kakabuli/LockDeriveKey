package com.revolo.lock.ui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;

import com.blankj.utilcode.util.FragmentUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ui.device.DeviceFragment;
import com.revolo.lock.ui.mine.MineFragment;
import com.revolo.lock.ui.user.UserFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;


public class MainActivity extends BaseActivity {

    private final ArrayList<Fragment> mFragments = new ArrayList<>();
    private final String CUR_INDEX = "curIndex";
    private int mCurIndex = 0;

//    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
//        if(item.getItemId() == R.id.navigation_device) {
//            showCurrentFragment(0);
//            return true;
//        }
//        if(item.getItemId() == R.id.navigation_user) {
//            showCurrentFragment(1);
//            return true;
//        }
//        if(item.getItemId() == R.id.navigation_mine) {
//            showCurrentFragment(2);
//            return true;
//        }
//        return false;
//    };

    @Override
    public void initData(@Nullable Bundle bundle) {

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
//        if (savedInstanceState != null) {
//            mCurIndex = savedInstanceState.getInt(CUR_INDEX);
//        }
//        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        mFragments.add(DeviceFragment.newInstance());
//        mFragments.add(UserFragment.newInstance());
//        mFragments.add(MineFragment.newInstance());
//        FragmentUtils.add(getSupportFragmentManager(),
//                mFragments,
//                R.id.nav_host_fragment,
//                new String[]{"DeviceFragment", "UserFragment", "MineFragment"},
//                mCurIndex);

    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }


//    @Override
//    public void onBackPressed() {
//        if(!FragmentUtils.dispatchBackPress(mFragments.get(mCurIndex))) {
//            super.onBackPressed();
//        }
//    }
//
//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
//        super.onSaveInstanceState(outState, outPersistentState);
//        outState.putInt(CUR_INDEX, mCurIndex);
//    }
//
//    private void showCurrentFragment(int index) {
//        mCurIndex = index;
//        FragmentUtils.showHide(index, mFragments);
//    }

}