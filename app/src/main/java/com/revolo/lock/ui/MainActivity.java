package com.revolo.lock.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.AppUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LockAppManager;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DeviceTokenBeanReq;
import com.revolo.lock.bean.request.GetVersionBeanReq;
import com.revolo.lock.bean.respone.DeviceTokenBeanRsp;
import com.revolo.lock.bean.respone.GetVersionBeanRsp;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.dialog.PrivacyPolicyDialog;
import com.revolo.lock.dialog.UpdateVersionDialog;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
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

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getAlexaIntent(intent);
        if (intent.hasExtra(Constant.SHOW_SHARE_DIALOG_TITLE) && isMainItemIndex != R.id.navigation_device) {
            String title = intent.getStringExtra(Constant.SHOW_SHARE_DIALOG_TITLE);
            if (TextUtils.isEmpty(title)) {
                MessageDialog messageDialog = new MessageDialog(LockAppManager.getAppManager().currentActivity());
                messageDialog.setMessage(title);
                messageDialog.setOnListener(v -> {
                    addDeviceFragment();
                    navView.setSelectedItemId(R.id.navigation_device);
                    messageDialog.dismiss();
                });
                messageDialog.show();
            }
        } else {
            addDeviceFragment();
            navView.setSelectedItemId(R.id.navigation_device);
        }
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
                App.getInstance().deleteDeviceToken();
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

        getServerAppVersion();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_USER) {
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //数据正常
                if (lockMessage.getMessageCode() == LockMessageCode.MSG_LOCK_MESSAGE_OPEN_PERMISSION) {
                    checkLocation();
                }
            }  //数据异常
        } else if (lockMessage.getMessgaeType() == MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //数据正常
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_ADD_DEVICE://添加到设备到主页
                        Timber.e("getEventBus2");
                        //获取当前用户绑定设备返回
                        updateGeoFence();
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
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show a dialog for displaying why the permission is needed and the ask the permission
                Timber.e("Location定位权限拒绝");
            } else {
                Timber.e("Location定位权限开启开启中");
                onPrivacyPolicyDialog();
            }
        }
    }

    private PrivacyPolicyDialog privacyPolicyDialog;

    private void onPrivacyPolicyDialog() {
        if (privacyPolicyDialog == null) {
            privacyPolicyDialog = new PrivacyPolicyDialog(this);
        } else {
            privacyPolicyDialog.dismiss();
        }
        privacyPolicyDialog.setOnConfirmListener(v -> {
            privacyPolicyDialog.dismiss();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        });
        privacyPolicyDialog.setOnCancelClickListener(v -> {
            privacyPolicyDialog.dismiss();
            App.getInstance().logout(true, this);
        });
        privacyPolicyDialog.show();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.e("Location定位权限允许");
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
        mTransaction.commitAllowingStateLoss();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void getServerAppVersion() {
        if (!checkNetConnectFail()) {
            return;
        }
        MailLoginBeanRsp.DataBean userBean = App.getInstance().getUserBean();
        if (userBean == null) {
            return;
        }

        String token = userBean.getToken();
        String uid = userBean.getUid();
        GetVersionBeanReq req = new GetVersionBeanReq();
        req.setUid(uid);
        req.setPhoneSysType("0");
        Observable<GetVersionBeanRsp> version = HttpRequest.getInstance().getVersion(token, req);
        ObservableDecorator.decorate(version).safeSubscribe(new Observer<GetVersionBeanRsp>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull GetVersionBeanRsp getVersionBeanRsp) {
                if (getVersionBeanRsp.getCode().equals("200")) {
                    if (getVersionBeanRsp.getData() != null) {
                        String appVersions = getVersionBeanRsp.getData().getAppVersions();
                        // 版本号不一致
                        if (appVersions(appVersions, AppUtils.getAppVersionName())) {
                            if (getVersionBeanRsp.getData() != null) {
                                GetVersionBeanRsp.DataBean data = getVersionBeanRsp.getData();
                                UpdateVersionDialog updateVersionDialog = new UpdateVersionDialog(MainActivity.this);
                                updateVersionDialog.setContent(data.getForceFlag(), data.getVersionDesc());
                                updateVersionDialog.setOnConfirmListener(v -> {
                                    launchAppDetail("com.revolo.lock", "com.android.vending");
                                });
                                updateVersionDialog.setOnCancelClickListener(v -> {
                                    updateVersionDialog.dismiss();
                                });
                                updateVersionDialog.show();
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                Constant.isNewAppVersion = false;
            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 服务器版本大于当前版本
     *
     * @param newAppVersions
     * @param oldAppVersions
     * @return
     */
    private boolean appVersions(String newAppVersions, String oldAppVersions) {
        try {
            int newIndex = newAppVersions.contains("V") ? newAppVersions.lastIndexOf("V") : newAppVersions.contains("v") ? newAppVersions.lastIndexOf("v") : 0;
            int oldIndex = oldAppVersions.contains("V") ? oldAppVersions.lastIndexOf("V") : oldAppVersions.contains("v") ? oldAppVersions.lastIndexOf("v") : 0;

            String[] newSplit = newAppVersions.substring(newIndex).replace("V", "").replace("v", "").split("\\.");
            String[] oldSplit = oldAppVersions.substring(oldIndex).replace("V", "").replace("v", "").split("\\.");
            if (newSplit != null && oldSplit != null) {
                for (int i = 0; i < newSplit.length; i++) {
                    if (newSplit[i] != null && oldSplit[i] != null) {
                        int newInt = Integer.parseInt(newSplit[i]);
                        int oldInt = Integer.parseInt(oldSplit[i]);
                        if (newInt < oldInt) {
                            return false;
                        } else if (newInt > oldInt) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 参数名： app包名以及google play包名。
     */
    public void launchAppDetail(String appPkg, String marketPkg) {
        try {
            if (TextUtils.isEmpty(appPkg)) return;

            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}