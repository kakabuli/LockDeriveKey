package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.ElecFenceSensitivityParams;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrSensitivityRspBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.ble.BleCommandState.KNOCK_DOOR_SENSITIVITY_HIGH;
import static com.revolo.lock.ble.BleCommandState.KNOCK_DOOR_SENSITIVITY_LOW;
import static com.revolo.lock.ble.BleCommandState.KNOCK_DOOR_SENSITIVITY_MEDIUM;
import static com.revolo.lock.ble.BleProtocolState.CMD_KNOCK_DOOR_AND_UNLOCK_TIME;
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

    private GoogleMap mMap;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(!intent.hasExtra(Constant.BLE_DEVICE)) {
            // TODO: 2021/2/22 处理
            finish();
            return;
        }
        mBleDeviceLocal = intent.getParcelableExtra(Constant.BLE_DEVICE);
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

        applyDebouncingClickListener(mIvGeoFenceUnlockEnable);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
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
            // TODO: 2021/2/23 开关电子围栏
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
//            App.getInstance()
//                    .writeControlMsg(BleCommandFactory
//                            .setKnockDoorAndUnlockTime(1,
//                                    mTime,
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
                App.getInstance()
                        .writeControlMsg(BleCommandFactory
                                .setSensitivity(mSensitivity,
                                        App.getInstance().getBleBean().getPwd1(),
                                        App.getInstance().getBleBean().getPwd3()));
            }
        }
    }

    private void initBleListener() {
        App.getInstance().setOnBleDeviceListener(new OnBleDeviceListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onReceivedValue(String uuid, byte[] value) {
                if(value == null) {
                    return;
                }
                BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
                BleResultProcess.processReceivedData(value,
                        App.getInstance().getBleBean().getPwd1(),
                        App.getInstance().getBleBean().getPwd3(),
                        App.getInstance().getBleBean().getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(String uuid, byte[] value, boolean success) {

            }

            @Override
            public void onAuthSuc() {

            }
        });
        // TODO: 2021/2/8 查询一下当前设置
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
                        dismissLoading();
                        if(TextUtils.isEmpty(mqttData.getFunc())) {
                            Timber.e("publishSensitivity mqttData.getFunc() is empty");
                            return;
                        }
                        if(mqttData.getFunc().equals(MqttConstant.SET_LOCK_ATTR)) {
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
