package com.revolo.lock.ui.device.lock.setting;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.ElecFenceSensitivityParams;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockApproachOpenResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrSensitivityRspBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.setting.geofence.MapActivity;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
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
    private TextView mTvTime, mTvSensitivity;
    private BleDeviceLocal mBleDeviceLocal;
    private SeekBar mSeekBarTime, mSeekBarSensitivity;

    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if(mBleDeviceLocal == null) {
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
        mSeekBarSensitivity = findViewById(R.id.seekBarSensitivity);
        initLoading("Setting...");
        mSeekBarTime.setMax(230);
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

        mIvGeoFenceUnlockEnable.setImageResource(mBleDeviceLocal.isOpenElectricFence()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);

        applyDebouncingClickListener(mIvGeoFenceUnlockEnable, findViewById(R.id.clDistanceRangeSetting));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            if(mMap != null) {
                                // Add a marker in Sydney and move the camera 23.795158587414274, 90.39920794033618
                                LatLng dhaka = new LatLng(location.getLatitude(), location.getLongitude());
                                // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dhaka, 16));

                            }
                        }
                    }
                });
    }

    @Override
    public void doBusiness() {
        initDefaultValue();
        initTimeNSensitivityDataUI();
        if(mBleDeviceLocal.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            initBleListener();
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivGeoFenceUnlockEnable) {
            // TODO: 2021/2/23 开关电子围栏 TEST使用开启地理围栏开门
            publishApproachOpen(mBleDeviceLocal.getEsn(), mBleDeviceLocal.getSetElectricFenceTime());
            return;
        }
        if(view.getId() == R.id.clDistanceRangeSetting) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        }
    }

    // TODO: 2021/2/24 超时或者失败处理需要恢复为原来的显示
    private int mTime;
    private String mTimeStr;

    private @BleCommandState.KnockDoorSensitivity int mSensitivity;
    private String mSensitivityStr;

    private void initDefaultValue() {
        mTime = mBleDeviceLocal.getSetElectricFenceTime();
        mSensitivity = mBleDeviceLocal.getSetElectricFenceSensitivity();
        boolean isNeedSave = false;
        if(mTime == 0) {
            mTime = 10*60;
            mBleDeviceLocal.setSetElectricFenceTime(mTime);
            isNeedSave = true;
        }
        if(mSensitivity == 0) {
            mSensitivity = KNOCK_DOOR_SENSITIVITY_MEDIUM;
            mBleDeviceLocal.setSetElectricFenceSensitivity(mSensitivity);
            isNeedSave = true;
        }
        if(isNeedSave) {
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        }
    }
    
    private void initTimeNSensitivityDataUI() {
        for (int i=3; i<=30; i++) {
            if(mTime == i*60) {
                mTimeStr = i+"min";
                break;
            }
        }
        processStopTimeFromSeekBar(mSeekBarTime, false);
        if(mSensitivity == KNOCK_DOOR_SENSITIVITY_LOW) {
            mSensitivityStr = getString(R.string.low);
        } else if(mSensitivity == KNOCK_DOOR_SENSITIVITY_MEDIUM) {
            mSensitivityStr = getString(R.string.medium);
        } else if(mSensitivity == KNOCK_DOOR_SENSITIVITY_HIGH) {
            mSensitivityStr = getString(R.string.high);
        }
        mTvSensitivity.setText(mSensitivityStr);
        processStopSensitivityFromSeekBar(mSeekBarSensitivity, false);
    }

    private void processChangeTimeFromSeekBar(int process) {
        for (int i=1; i<=23; i++) {
            if(process <= i*10) {
                if(i==1) {
                    mTimeStr = "3min";
                    mTime = 3*60;
                } else if(i==2) {
                    mTimeStr = "5min";
                    mTime = 5*60;
                } else {
                    mTimeStr = (i+7) + "min";
                    mTime = (i+7)*60;
                }
                break;
            }
        }
        mTvTime.setText(mTimeStr);
    }

    private void processStopTimeFromSeekBar(SeekBar seekBar, boolean isNeedToSave) {
        mTvTime.setText(mTimeStr);
        for (int i=3; i<=30; i++) {
            if(mTime == i*60) {
                if(i == 3) {
                    seekBar.setProgress(0);
                } else if(i == 5) {
                    seekBar.setProgress(11);
                } else if(i == 30) {
                    seekBar.setProgress(230);
                } else {
                    seekBar.setProgress(((i-8)*10)+1);
                }
                break;
            }
        }
        if(isNeedToSave) {
            mBleDeviceLocal.setSetElectricFenceTime(mTime);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        }
        // TODO: 2021/3/1 做这个操作意味着进行开门了
//        if(isSendCommand) {
//            // 固定3分钟
//            App.getInstance()
//                    .writeControlMsg(BleCommandFactory
//                            .setKnockDoorAndUnlockTime(1,
//                                    App.getInstance().getBleBean().getPwd1(),
//                                    App.getInstance().getBleBean().getPwd3()));
//        }
    }

    private void processChangeSensitivityFromSeekBar(int process) {
        if(process <= 30) {
            mSensitivity = KNOCK_DOOR_SENSITIVITY_LOW;
            mSensitivityStr = getString(R.string.low);
        } else if(process <= 70) {
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
        if(mSensitivity == KNOCK_DOOR_SENSITIVITY_LOW) {
            seekBar.setProgress(0);
        } else if(mSensitivity == KNOCK_DOOR_SENSITIVITY_MEDIUM) {
            seekBar.setProgress(50);
        } else if(mSensitivity == KNOCK_DOOR_SENSITIVITY_HIGH) {
            seekBar.setProgress(100);
        }
        if(isSendCommand) {
            if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                publishSensitivity(mBleDeviceLocal.getEsn(), mSensitivity);
            } else {
                setSensitivityFromBle();
            }
        }
    }

    private void setSensitivityFromBle() {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(bleBean == null) {
            Timber.e("setSensitivityFromBle bleBean == null");
            return;
        }
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("setSensitivityFromBle bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if(bleBean.getPwd1() == null) {
            Timber.e("setSensitivityFromBle bleBean.getPwd1() == null");
            return;
        }
        if(bleBean.getPwd3() == null) {
            Timber.e("setSensitivityFromBle bleBean.getPwd3() == null");
            return;
        }
        App.getInstance()
                .writeControlMsg(BleCommandFactory
                        .setSensitivity(mSensitivity, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
    }

    private void initBleListener() {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(bleBean != null) {
            bleBean.setOnBleDeviceListener(new OnBleDeviceListener() {
                @Override
                public void onConnected(@NotNull String mac) {

                }

                @Override
                public void onDisconnected(@NotNull String mac) {

                }

                @Override
                public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
                    if(value == null) {
                        Timber.e("initBleListener value == null");
                        return;
                    }
                    BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
                    if(bleBean == null) {
                        Timber.e("initBleListener bleBean == null");
                        return;
                    }
                    if(bleBean.getOKBLEDeviceImp() == null) {
                        Timber.e("initBleListener bleBean.getOKBLEDeviceImp() == null");
                        return;
                    }
                    if(bleBean.getPwd1() == null) {
                        Timber.e("initBleListener bleBean.getPwd1() == null");
                        return;
                    }
                    if(bleBean.getPwd3() == null) {
                        Timber.e("initBleListener bleBean.getPwd3() == null");
                        return;
                    }
                    BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
                    BleResultProcess.processReceivedData(value, bleBean.getPwd1(), bleBean.getPwd3(),
                            bleBean.getOKBLEDeviceImp().getBleScanResult());
                }

                @Override
                public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

                }

                @Override
                public void onAuthSuc(@NotNull String mac) {

                }

            });
            // TODO: 2021/2/8 查询一下当前设置
        }
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        if(bean.getCMD() == CMD_SET_SENSITIVITY) {
            processSetSensitivity(bean);
        }
//        else if(bean.getCMD() == CMD_KNOCK_DOOR_AND_UNLOCK_TIME) {
//            processKnockUnlockTime(bean);
//        }
    }

    private void processKnockUnlockTime(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if(state == 0x00) {
            mBleDeviceLocal.setSetElectricFenceTime(mTime);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

    private void processSetSensitivity(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if(state == 0x00) {
            saveSensitivityToLocal();
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

    private void saveSensitivityToLocal() {
        mBleDeviceLocal.setSetElectricFenceSensitivity(mSensitivity);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    private void publishSensitivity(String wifiID, int sensitivity) {
        showLoading();
        ElecFenceSensitivityParams autoLockTimeParams = new ElecFenceSensitivityParams();
        autoLockTimeParams.setElecFenceSensitivity(sensitivity);
        App.getInstance().getMqttService().mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLockAttr(wifiID, autoLockTimeParams,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .safeSubscribe(new Observer<MqttData>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull MqttData mqttData) {
                        if(TextUtils.isEmpty(mqttData.getFunc())) {
                            Timber.e("publishSensitivity mqttData.getFunc() is empty");
                            return;
                        }
                        if(mqttData.getFunc().equals(MqttConstant.SET_LOCK_ATTR)) {
                            dismissLoading();
                            Timber.d("publishSensitivity 设置属性: %1s", mqttData);
                            WifiLockSetLockAttrSensitivityRspBean bean;
                            try {
                                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrSensitivityRspBean.class);
                            } catch (JsonSyntaxException e) {
                                Timber.e(e);
                                return;
                            }
                            if(bean == null) {
                                Timber.e("publishSensitivity bean == null");
                                return;
                            }
                            if(bean.getParams() == null) {
                                Timber.e("publishSensitivity bean.getParams() == null");
                                return;
                            }
                            if(bean.getCode() != 200) {
                                Timber.e("publishSensitivity code : %1d", bean.getCode());
                                return;
                            }
                            saveSensitivityToLocal();
                        }
                        Timber.d("publishSensitivity %1s", mqttData.toString());
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        // TODO: 2021/3/3 错误处理
                        // 超时或者其他错误
                        dismissLoading();
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void publishApproachOpen(String wifiID, int broadcastTime) {
        showLoading();
        App.getInstance().getMqttService().mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.approachOpen(wifiID, broadcastTime,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .safeSubscribe(new Observer<MqttData>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull MqttData mqttData) {
                        if(TextUtils.isEmpty(mqttData.getFunc())) {
                            Timber.e("publishApproachOpen mqttData.getFunc() is empty");
                            return;
                        }
                        if(mqttData.getFunc().equals(MqttConstant.APP_ROACH_OPEN)) {
                            dismissLoading();
                            Timber.d("publishApproachOpen 无感开门: %1s", mqttData);
                            WifiLockApproachOpenResponseBean bean;
                            try {
                                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockApproachOpenResponseBean.class);
                            } catch (JsonSyntaxException e) {
                                Timber.e(e);
                                return;
                            }
                            if(bean == null) {
                                Timber.e("publishApproachOpen bean == null");
                                return;
                            }
                            if(bean.getParams() == null) {
                                Timber.e("publishApproachOpen bean.getParams() == null");
                                return;
                            }
                            if(bean.getCode() != 200) {
                                Timber.e("publishApproachOpen code : %1d", bean.getCode());
                                return;
                            }
                            // TODO: 2021/3/5 开启成功，然后开启蓝牙并不断搜索设备
                        }
                        Timber.d("publishApproachOpen %1s", mqttData.toString());
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        // TODO: 2021/3/3 错误处理
                        // 超时或者其他错误
                        dismissLoading();
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     *
     * If Google Play services is not installed on the device, the user will be prompted to install`
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }
}
