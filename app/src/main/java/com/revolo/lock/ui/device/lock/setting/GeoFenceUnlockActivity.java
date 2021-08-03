package com.revolo.lock.ui.device.lock.setting;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.revolo.lock.App;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.UpdateLocalBeanReq;
import com.revolo.lock.bean.respone.UpdateLocalBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.ElecFenceSensitivityParams;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockApproachOpenResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrSensitivityRspBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.setting.geofence.MapActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.KNOCK_DOOR_SENSITIVITY_HIGH;
import static com.revolo.lock.ble.BleCommandState.KNOCK_DOOR_SENSITIVITY_LOW;
import static com.revolo.lock.ble.BleCommandState.KNOCK_DOOR_SENSITIVITY_MEDIUM;
import static com.revolo.lock.ble.BleProtocolState.CMD_SET_SENSITIVITY;

/**
 * author : Jack
 * time   : 2021/1/19
 * E-mail : wengmaowei@kaadas.com
 * desc   : 地理围栏设置页面
 */
public class GeoFenceUnlockActivity extends BaseActivity implements OnMapReadyCallback {

    private ImageView mIvGeoFenceUnlockEnable;
    private TextView mTvTime, mTvSensitivity, mTvIntroduceTitle, mTvIntroduceContent;
    private BleDeviceLocal mBleDeviceLocal;
    private SeekBar mSeekBarTime, mSeekBarSensitivity;
    private ConstraintLayout mConstraintLayout;
    private boolean isNextUpdate = false;
    private GoogleMap mMap;
    public float GEO_FENCE_RADIUS = 200;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_geo_fence_unlock;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_geo_fence_unlock));
        mIvGeoFenceUnlockEnable = findViewById(R.id.ivGeoFenceUnlockEnable);
        mTvTime = findViewById(R.id.tvTime);
        mTvSensitivity = findViewById(R.id.tvSensitivity);
        mSeekBarTime = findViewById(R.id.seekBarTime);
        mTvIntroduceContent = findViewById(R.id.tvIntroduceContent);
        mTvIntroduceTitle = findViewById(R.id.tvIntroduceTitle);
        mSeekBarSensitivity = findViewById(R.id.seekBarSensitivity);
        mConstraintLayout = findViewById(R.id.constraintLayout);
        initLoading(getString(R.string.t_load_content_setting));
        mSeekBarTime.setMax(300);
        mSeekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                processChangeTimeFromSeekBar(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                processStopTimeFromSeekBar(seekBar, true);
            }
        });
        mSeekBarSensitivity.setMax(100);
        mSeekBarSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                processChangeSensitivityFromSeekBar(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                processStopSensitivityFromSeekBar(seekBar, true);
            }
        });

        applyDebouncingClickListener(mIvGeoFenceUnlockEnable, findViewById(R.id.clDistanceRangeSetting));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        if (mBleDeviceLocal.isOpenElectricFence()) {
                            double la = mBleDeviceLocal.getLatitude();
                            double lo = mBleDeviceLocal.getLongitude();
                            if (mMap != null) {
                                mMap.clear();
                                LatLng latLng = new LatLng(la, lo);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                                addMarker(latLng);
                                addCircle(latLng, GEO_FENCE_RADIUS);
                            }
                        }
                    }
                });
        onRegisterEventBus();

        mTvIntroduceTitle.setOnClickListener(v -> {
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
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            if (null != lockMessage.getBleResultBea()) {
                processBleResult(lockMessage.getBleResultBea());
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRSENSITIVITY:
                        processSensitivity((WifiLockSetLockAttrSensitivityRspBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_APP_ROACH_OPEN:
                        if (isNextUpdate) {
                            isNextUpdate = false;
                            checkSetTime((WifiLockApproachOpenResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        }
                        break;
                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRSENSITIVITY:
                        dismissLoading();
                        break;
                }
            }
        } else {

        }
    }


    @Override
    public void doBusiness() {
        initDefaultValue();
        initTimeNSensitivityDataUI();
        mIvGeoFenceUnlockEnable.setImageResource(mBleDeviceLocal.isOpenElectricFence() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.ivGeoFenceUnlockEnable) {
            if (mBleDeviceLocal.isOpenElectricFence()) {
                mBleDeviceLocal.setOpenElectricFence(false);
                AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
                mIvGeoFenceUnlockEnable.setImageResource(mBleDeviceLocal.isOpenElectricFence() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
                // 更新电子围栏状态
                if (null != App.getInstance().getLockGeoFenceService()) {
                    App.getInstance().getLockGeoFenceService().clearBleDevice(mBleDeviceLocal.getEsn());
                }
                if (null != App.getInstance().getLockAppService()) {
                    App.getInstance().getLockAppService().updateDeviceGeoState(mBleDeviceLocal.getMac(), mBleDeviceLocal);
                }
                pushService();

            } else {
                if (mBleDeviceLocal.getLongitude() == 0 && mBleDeviceLocal.getLatitude() == 0) {
                    Intent intent = new Intent(this, MapActivity.class);
                    startActivity(intent);
                } else {
                    mBleDeviceLocal.setOpenElectricFence(true);
                    AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
                    mIvGeoFenceUnlockEnable.setImageResource(mBleDeviceLocal.isOpenElectricFence() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
                    if (null != App.getInstance().getLockAppService()) {
                        App.getInstance().getLockAppService().updateDeviceGeoState(mBleDeviceLocal.getMac(), mBleDeviceLocal);
                    }
                    if (null != App.getInstance().getLockGeoFenceService()) {
                        App.getInstance().getLockGeoFenceService().addBleDevice();
                    }
                    pushService();
                }

            }
        }
        if (view.getId() == R.id.clDistanceRangeSetting) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 上传给服务器
     */
    private void pushService() {
        showLoading();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                UpdateLocalBeanReq lockLocal = new UpdateLocalBeanReq();
                lockLocal.setSn(mBleDeviceLocal.getEsn());
                lockLocal.setElecFence(mBleDeviceLocal.isOpenElectricFence() ? 1 : 0);
                lockLocal.setElecFenceSensitivity(mBleDeviceLocal.getSetElectricFenceSensitivity());
                lockLocal.setElecFenceTime(mBleDeviceLocal.getSetElectricFenceTime());
                lockLocal.setLatitude(mBleDeviceLocal.getLatitude() + "");
                lockLocal.setLongitude(mBleDeviceLocal.getLongitude() + "");
                lockLocal.setElecFenceState(mBleDeviceLocal.getElecFenceState() ? 0 : 1);
                String token = App.getInstance().getUserBean().getToken();
                Observable<UpdateLocalBeanRsp> observable = HttpRequest.getInstance().updateockeLecfence(token, lockLocal);
                ObservableDecorator.decorate(observable).safeSubscribe(new Observer<UpdateLocalBeanRsp>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull UpdateLocalBeanRsp changeKeyNickBeanRsp) {
                        dismissLoading();
                        String code = changeKeyNickBeanRsp.getCode();
                        if (TextUtils.isEmpty(code)) {
                            Timber.e("changeKeyNickBeanRsp.getCode() is Empty");
                            return;
                        }
                        String msg = changeKeyNickBeanRsp.getMsg();
                        Timber.e("code: %1s, msg: %2s", changeKeyNickBeanRsp.getCode(), msg);
                        if (!code.equals("200")) {
                            if (code.equals("444")) {
                                App.getInstance().logout(true, GeoFenceUnlockActivity.this);
                                return;
                            }
                            if (!TextUtils.isEmpty(msg)) {
                                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                            }
                            return;
                        } else {
                            if (!TextUtils.isEmpty(msg)) {
                                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                            }
                            return;
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e);

                        dismissLoading();

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        });

    }

    private void pushMessage(String wifiID, int broadcastTime) {
        BleDeviceLocal deviceLocal = App.getInstance().getBleDeviceLocal();
        if (deviceLocal == null) {
            Timber.e("publishApproachOpen deviceLocal == null");
            return;
        }
        isNextUpdate = true;
        LockMessage lockMessage = new LockMessage();
        lockMessage.setMqttMessage(MqttCommandFactory.approachOpen(wifiID, broadcastTime,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(deviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(deviceLocal.getPwd2())), 1, 4));
        lockMessage.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        lockMessage.setMessageType(2);
        lockMessage.setMqtt_message_code(MQttConstant.APP_ROACH_OPEN);
        EventBus.getDefault().post(lockMessage);

    }

    // TODO: 2021/2/24 超时或者失败处理需要恢复为原来的显示
    private int mTime;
    private String mTimeStr;

    private @BleCommandState.KnockDoorSensitivity
    int mSensitivity;
    private String mSensitivityStr;

    private void initDefaultValue() {
        mTime = mBleDeviceLocal.getSetElectricFenceTime();
        mSensitivity = mBleDeviceLocal.getSetElectricFenceSensitivity();
        boolean isNeedSave = false;
        if (mTime == 0) {
            mTime = 10;
            mBleDeviceLocal.setSetElectricFenceTime(mTime);
            isNeedSave = true;
        }
        if (mSensitivity == 0) {
            mSensitivity = KNOCK_DOOR_SENSITIVITY_MEDIUM;
            mBleDeviceLocal.setSetElectricFenceSensitivity(mSensitivity);
            isNeedSave = true;
        }
        if (mBleDeviceLocal.isOpenElectricFence()) {
            double la = mBleDeviceLocal.getLatitude();
            double lo = mBleDeviceLocal.getLongitude();
            if (mMap != null) {
                mMap.clear();
                LatLng latLng = new LatLng(la, lo);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                addMarker(latLng);
                addCircle(latLng, GEO_FENCE_RADIUS);
            }
        }
        if (isNeedSave) {
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        }
    }

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();

        circleOptions.center(latLng)
                .radius(radius)
                .strokeColor(Color.argb(255, 255, 0, 0))
                .fillColor(Color.argb(64, 255, 0, 0))
                .strokeWidth(4);

        mMap.addCircle(circleOptions);
    }

    private void initTimeNSensitivityDataUI() {
        mTimeStr = mTime + "min";
        mTvTime.setText(mTimeStr);
        mSeekBarTime.setProgress(mTime * 10);
        if (mSensitivity == KNOCK_DOOR_SENSITIVITY_LOW) {
            mSensitivityStr = getString(R.string.low);
        } else if (mSensitivity == KNOCK_DOOR_SENSITIVITY_MEDIUM) {
            mSensitivityStr = getString(R.string.medium);
        } else if (mSensitivity == KNOCK_DOOR_SENSITIVITY_HIGH) {
            mSensitivityStr = getString(R.string.high);
        }
        mTvSensitivity.setText(mSensitivityStr);
        processStopSensitivityFromSeekBar(mSeekBarSensitivity, false);
    }

    private void processChangeTimeFromSeekBar(int process) {
        mTimeStr = (process / 10) + "min";
        mTime = (process / 10);
        mTvTime.setText(mTimeStr);
    }

    private void processStopTimeFromSeekBar(SeekBar seekBar, boolean isNeedToSave) {
        mTvTime.setText(mTimeStr);
        seekBar.setProgress(mTime * 10);
        //先保存  下发地理围栏时
        saveElectricFenceTimeToLocal(mTime);
        pushService();

        //pushMessage(mBleDeviceLocal.getEsn(), mTime);
    }

    private void processChangeSensitivityFromSeekBar(int process) {
        if (process <= 30) {
            mSensitivity = KNOCK_DOOR_SENSITIVITY_LOW;
            mSensitivityStr = getString(R.string.low);
        } else if (process <= 70) {
            mSensitivity = KNOCK_DOOR_SENSITIVITY_MEDIUM;
            mSensitivityStr = getString(R.string.medium);
        } else {
            mSensitivity = KNOCK_DOOR_SENSITIVITY_HIGH;
            mSensitivityStr = getString(R.string.high);
        }
        mTvSensitivity.setText(mSensitivityStr);
    }

    private void processStopSensitivityFromSeekBar(SeekBar seekBar, boolean isSendCommand) {
        mTvSensitivity.setText(mSensitivityStr);
        if (mSensitivity == KNOCK_DOOR_SENSITIVITY_LOW) {
            seekBar.setProgress(0);
        } else if (mSensitivity == KNOCK_DOOR_SENSITIVITY_MEDIUM) {
            seekBar.setProgress(50);
        } else if (mSensitivity == KNOCK_DOOR_SENSITIVITY_HIGH) {
            seekBar.setProgress(100);
        }
        if (isSendCommand) {
            if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
                publishSensitivity(mBleDeviceLocal.getEsn(), mSensitivity);
            } else {
                setSensitivityFromBle();
            }
        }
    }

    private void setSensitivityFromBle() {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (bleBean == null) {
            Timber.e("setSensitivityFromBle bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("setSensitivityFromBle bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if (bleBean.getPwd1() == null) {
            Timber.e("setSensitivityFromBle bleBean.getPwd1() == null");
            return;
        }
        if (bleBean.getPwd3() == null) {
            Timber.e("setSensitivityFromBle bleBean.getPwd3() == null");
            return;
        }
        LockMessage message = new LockMessage();
        message.setMessageType(3);
        message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        message.setBytes(BleCommandFactory
                .setSensitivity(mSensitivity, bleBean.getPwd1(), bleBean.getPwd3()));
        EventBus.getDefault().post(message);

    }


    private void processBleResult(BleResultBean bean) {
        if (bean.getCMD() == CMD_SET_SENSITIVITY) {
            processSetSensitivity(bean);
        }
    }

    private void saveElectricFenceTimeToLocal(int time) {
        mBleDeviceLocal.setSetElectricFenceTime(time);
        if (null != App.getInstance().getLockGeoFenceService()) {
            App.getInstance().getLockGeoFenceService().setFenceTime(mBleDeviceLocal.getEsn(), time);
        }
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);

        if (null != App.getInstance().getLockAppService()) {
            App.getInstance().getLockAppService().updateDeviceGeoState(mBleDeviceLocal.getMac(), mBleDeviceLocal);
        }
    }

    private void processSetSensitivity(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if (state == 0x00) {
            saveSensitivityToLocal();
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

    private void saveSensitivityToLocal() {
        mBleDeviceLocal.setSetElectricFenceSensitivity(mSensitivity);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        if (null != App.getInstance().getLockAppService()) {
            App.getInstance().getLockAppService().updateDeviceGeoState(mBleDeviceLocal.getMac(), mBleDeviceLocal);
        }
        pushService();
    }

    // private Disposable mSensitivityDisposable;

    private void publishSensitivity(String wifiID, int sensitivity) {

        showLoading();
        ElecFenceSensitivityParams autoLockTimeParams = new ElecFenceSensitivityParams();
        autoLockTimeParams.setElecFenceSensitivity(sensitivity);
        LockMessage lockMessage = new LockMessage();
        lockMessage.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        lockMessage.setMqtt_message_code(MQttConstant.SET_LOCK_ATTR);
        lockMessage.setMessageType(2);
        lockMessage.setMqttMessage(MqttCommandFactory.setLockAttr(wifiID, autoLockTimeParams,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        EventBus.getDefault().post(lockMessage);

    }

    private void processSensitivity(WifiLockSetLockAttrSensitivityRspBean bean) {
        dismissLoading();
        if (bean == null) {
            Timber.e("publishSensitivity bean == null");
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("publishSensitivity bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishSensitivity code : %1d", bean.getCode());
            if (bean.getCode() == 201) {
                // 设置失败了
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_setting_sensitivity_fail);
                initDefaultValue();
                initTimeNSensitivityDataUI();
            }
            return;
        }
        saveSensitivityToLocal();
        pushService();
    }

    /**
     * 保存地理围栏时间设置
     *
     * @param bean
     */
    private void checkSetTime(WifiLockApproachOpenResponseBean bean) {
        dismissLoading();
        if (bean == null) {
            Timber.e("publishSensitivity bean == null");
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("publishSensitivity bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishSensitivity code : %1d", bean.getCode());
            if (bean.getCode() == 201) {
                // 设置失败了
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_setting_sensitivity_fail);
                initDefaultValue();
                initTimeNSensitivityDataUI();
            }
            return;
        }
        saveElectricFenceTimeToLocal(mTime);
        pushService();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * <p>
     * If Google Play services is not installed on the device, the user will be prompted to install`
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
