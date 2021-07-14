package com.revolo.lock.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DeviceTokenBeanReq;
import com.revolo.lock.bean.respone.DeviceTokenBeanRsp;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockBaseResponseBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.ui.device.DeviceFragment;
import com.revolo.lock.ui.device.add.AddDeviceActivity;
import com.revolo.lock.ui.mine.MessageListActivity;
import com.revolo.lock.ui.mine.MineFragment;
import com.revolo.lock.ui.user.UserFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_MQTT;


public class MainActivity extends BaseActivity {
    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 230;
    private static final int UPDATE_DEVICE_LOCAL = 231;
    private boolean isGotoAddDeviceAct = false;
    private int isMainItemIndex = -1;
    private FragmentManager mSupportFragmentManager;
    private FragmentTransaction mTransaction;
    private final List<Fragment> mFragments = new ArrayList<>();
    private DeviceFragment deviceFragment;
    private UserFragment userFragment;
    private MineFragment mineFragment;
    private BottomNavigationView navView;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.COMMAND)) {
            String command = intent.getStringExtra(Constant.COMMAND);
            isGotoAddDeviceAct = command.equals(Constant.ADD_DEVICE);
        }

        Bundle extras = intent.getExtras();
        if (extras != null) {
            Set<String> strings = extras.keySet();
            for (String s : strings) {
                if (s.equals("type")) {
                    String type = extras.getString(s);
                    if (!type.equals("3")) {
                        startActivity(new Intent(this, MessageListActivity.class));
                    }
                }
            }
        }

        getAlexaIntent(getIntent());

        //onRegisterEventBus();
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
        navView = findViewById(R.id.nav_view);
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
        onRegisterEventBus();
        //NavigationUI.setupWithNavController(navView, navController);
        mSupportFragmentManager = getSupportFragmentManager();
        mTransaction = mSupportFragmentManager.beginTransaction();
        addDeviceFragment();
        if (isGotoAddDeviceAct) {
            startActivity(new Intent(this, AddDeviceActivity.class));
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Timber.d("**********   failed   ************");
                GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(MainActivity.this);
                return;
            }
            String token = task.getResult();
            if (App.getInstance().getUserBean() != null) {
                Timber.d("**************************   set google token to server   **************************");
                DeviceTokenBeanReq req = new DeviceTokenBeanReq();
                req.setType(1);
                req.setDeviceToken(token);
                req.setUid(App.getInstance().getUserBean().getUid());
                Observable<DeviceTokenBeanRsp> deviceTokenBeanRspObservable = HttpRequest.getInstance().deviceToken(App.getInstance().getUserBean().getToken(), req);
                ObservableDecorator.decorate(deviceTokenBeanRspObservable).safeSubscribe(new Observer<DeviceTokenBeanRsp>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull DeviceTokenBeanRsp deviceTokenBeanRsp) {

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage.getMessgaeType() == MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //数据正常
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_ADD_DEVICE://添加到设备到主页
                        Timber.e("getEventBus2");
                        //获取当前用户绑定设备返回
                        checkLocation();
                        break;
                }
            }
        }
    }

    /**
     * 更新电子围栏
     */
    public void updateGeoFence() {
        if (null != App.getInstance().getLockGeoFenceService()) {
            App.getInstance().getLockGeoFenceService().addBleDevice();
        }
    }

    public void checkLocation() {
        if (ContextCompat.checkSelfPermission(App.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Timber.e("Location定位权限开启");
            updateGeoFence();
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show a dialog for displaying why the permission is needed and the ask the permission
                Timber.e("Location定位权限拒绝");
            } else {
                Timber.e("Location定位权限开启开启中");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.e("Location定位权限允许");
                checkLocation();
            }
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

    private void getAlexaIntent(Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
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