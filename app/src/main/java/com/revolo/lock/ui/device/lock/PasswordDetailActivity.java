package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DelKeyBeanReq;
import com.revolo.lock.bean.respone.DelKeyBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockRemovePasswordResponseBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.DevicePwd;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_TIME_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_WEEK_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_OPTION_DEL;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_TYPE_PWD;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ATTRIBUTES_SET;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 密码详情页面
 */
public class PasswordDetailActivity extends BaseActivity {

    private TextView mTvPwdName, mTvPwd, mTvPwdCharacteristic, mTvCreationDate;
    private CustomerLoadingDialog mLoadingDialog;
    private long mPwdId;
    private DevicePwd mDevicePwd;
    private String mESN;
    private BleDeviceLocal mBleDeviceLocal;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.PWD_ID)) {
            mPwdId = intent.getLongExtra(Constant.PWD_ID, -1L);
        }
        if(mPwdId == -1) {
            finish();
        }
        mDevicePwd = AppDatabase.getInstance(this).devicePwdDao().findDevicePwdFromId(mPwdId);
        if(mDevicePwd == null) {
            finish();
        }
        if(intent.hasExtra(Constant.LOCK_ESN)) {
            mESN = intent.getStringExtra(Constant.LOCK_ESN);
            Timber.d("initData Device Esn: %1s", mESN);
        }
        if(TextUtils.isEmpty(mESN)) {
            // TODO: 2021/2/24 无法获取esn来处理问题
            finish();
        }
        mBleDeviceLocal = AppDatabase.getInstance(this).bleDeviceDao().findBleDeviceFromId(mDevicePwd.getDeviceId());
        if(mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_password_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_pwd_details));
        applyDebouncingClickListener(findViewById(R.id.ivEditPwdName), findViewById(R.id.btnDeletePwd));
        mTvPwdName = findViewById(R.id.tvPwdName);
        mTvPwd = findViewById(R.id.tvPwd);
        mTvCreationDate = findViewById(R.id.tvCreationDate);
        mTvPwdCharacteristic  = findViewById(R.id.tvPwdCharacteristic);
        // TODO: 2021/1/29 抽离英文
        mLoadingDialog = new CustomerLoadingDialog.Builder(this)
                .setMessage("Deleting")
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
    }

    @Override
    public void doBusiness() {
        initDetail();
        if(mBleDeviceLocal.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            initBleListener();
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivEditPwdName) {
            Intent intent = new Intent(this, ChangePwdNameActivity.class);
            intent.putExtra(Constant.PWD_ID, mPwdId);
            intent.putExtra(Constant.LOCK_ESN, mESN);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.btnDeletePwd) {
            showDelDialog();
        }
    }

    private void initDetail() {
        if(mDevicePwd != null) {
            mTvPwdName.setText(mDevicePwd.getPwdName());
            mTvPwd.setText("***********");
            mTvPwdCharacteristic.setText(getPwdCharacteristic(mDevicePwd));
            mTvCreationDate.setText(TimeUtils.millis2String(mDevicePwd.getCreateTime()*1000, "MM,dd,yyyy HH:mm:ss"));
        }
    }

    private String getPwdCharacteristic(DevicePwd devicePwd) {
        int attribute = devicePwd.getAttribute();
        String detail = "";
        if(attribute == KEY_SET_ATTRIBUTE_ALWAYS) {
            detail = "Permanent";
        } else if(attribute == KEY_SET_ATTRIBUTE_TIME_KEY) {
            long startTimeMill = devicePwd.getStartTime()*1000;
            long endTimeMill = devicePwd.getEndTime()*1000;
            detail = TimeUtils.millis2String(startTimeMill, "MM,dd,yyyy   HH:mm")
                    + "-" + TimeUtils.millis2String(endTimeMill, "MM,dd,yyyy   HH:mm");
        } else if(attribute == KEY_SET_ATTRIBUTE_WEEK_KEY) {
            byte[] weekBytes = BleByteUtil.byteToBit(devicePwd.getWeekly());
            String weekly = "";
            if(weekBytes[0] == 0x01) {
                weekly += "Sun";
            }
            if(weekBytes[1] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Mon":"、Mon";
            }
            if(weekBytes[2] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Tues":"、Tues";
            }
            if(weekBytes[3] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Wed":"、Wed";
            }
            if(weekBytes[4] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Thur":"、Thur";
            }
            if(weekBytes[5] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Fri":"、Fri";
            }
            if(weekBytes[6] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Sat":"、Sat";
            }
            weekly += "\n";
            long startTimeMill = devicePwd.getStartTime()*1000;
            long endTimeMill = devicePwd.getEndTime()*1000;
            detail = weekly
                    + TimeUtils.millis2String(startTimeMill, "HH:mm")
                    + " - "
                    + TimeUtils.millis2String(endTimeMill, "HH:mm");
        }
        return detail;
    }

    private void showDelDialog() {
        SelectDialog dialog = new SelectDialog(this);
        dialog.setMessage(getString(R.string.dialog_tip_please_approach_the_door_lock_to_delete_password));
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            delPwd();
        });
        dialog.show();
    }

    private void publishDelPwd(String wifiId, int num) {
        App.getInstance().getMqttService().mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.removePwd(
                        wifiId,
                        1,
                        num,
                        BleCommandFactory.getPwd(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()), ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .timeout(10, TimeUnit.SECONDS).safeSubscribe(new Observer<MqttData>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull MqttData mqttData) {
                dismissLoading();
                if(TextUtils.isEmpty(mqttData.getFunc())) {
                    return;
                }
                if(mqttData.getFunc().equals(MqttConstant.REMOVE_PWD)) {
                    Timber.d("删除密码信息: %1s", mqttData);
                    WifiLockRemovePasswordResponseBean bean;
                    try {
                        bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockRemovePasswordResponseBean.class);
                    } catch (JsonSyntaxException e) {
                        Timber.e(e);
                        return;
                    }
                    if(bean == null) {
                        Timber.e("publishDelPwd bean == null");
                        return;
                    }
                    if(bean.getParams() == null) {
                        Timber.e("publishDelPwd bean.getParams() == null");
                        return;
                    }
                    if(bean.getCode() != 200) {
                        Timber.e("publishDelPwd code : %1d", bean.getCode());
                        return;
                    }
                    delKeyFromService();
                }
                Timber.d("publishDelPwd %1s", mqttData.toString());
            }

            @Override
            public void onError(@NotNull Throwable e) {
                dismissLoading();
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void showLoading() {
        runOnUiThread(() -> {
            if(mLoadingDialog != null) {
                mLoadingDialog.show();
            }
        });
    }

    private void delPwd() {
        showLoading();
        if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            publishDelPwd(mBleDeviceLocal.getEsn(), mDevicePwd.getPwdNum());
        } else {
            App.getInstance().writeControlMsg(BleCommandFactory
                    .keyAttributesSet(KEY_SET_KEY_OPTION_DEL,
                            KEY_SET_KEY_TYPE_PWD,
                            (byte)mDevicePwd.getPwdNum(),
                            KEY_SET_ATTRIBUTE_WEEK_KEY,
                            (byte)0x00,
                            (byte)0x00,
                            (byte)0x00,
                            App.getInstance().getBleBean().getPwd1(),
                            App.getInstance().getBleBean().getPwd3()));
        }
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        if(bleResultBean.getCMD() == CMD_KEY_ATTRIBUTES_SET) {
            delServiceAndLocalKey(bleResultBean);
        }
    };

    private void delServiceAndLocalKey(BleResultBean bleResultBean) {
        runOnUiThread(() -> {
            if(bleResultBean.getPayload()[0] == 0x00) {
                // 锁端删除成功，执行服务器和本地数据库删除
                delKeyFromService();
            } else {
                // 锁端删除失败
                dismissLoading();
                showFailMessage();
            }
        });
    }

    private void dismissLoadingAndShowFailMessage() {
        dismissLoading();
        showFailMessage();
    }

    private void dismissLoadingAndShowSucMessage() {
        dismissLoading();
        showSucMessage();
    }

    private void showSucMessage() {
        runOnUiThread(() -> {
            MessageDialog messageDialog = new MessageDialog(PasswordDetailActivity.this);
            messageDialog.setMessage(getString(R.string.dialog_tip_password_deleted));
            messageDialog.setOnListener(v -> {
                messageDialog.dismiss();
                finish();
            });
            messageDialog.show();
        });

    }

    private void showFailMessage() {
        runOnUiThread(() -> {
            MessageDialog messageDialog = new MessageDialog(PasswordDetailActivity.this);
            messageDialog.setMessage(getString(R.string.dialog_tip_deletion_failed_door_lock_bluetooth_is_not_found));
            messageDialog.setOnListener(v -> messageDialog.dismiss());
            messageDialog.show();
        });

    }

    private void dismissLoading() {
        runOnUiThread(() -> {
            if(mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        });

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
    }

    private void delKeyFromService() {

        List<DelKeyBeanReq.PwdListBean> listBeans = new ArrayList<>();
        DelKeyBeanReq.PwdListBean pwdListBean = new DelKeyBeanReq.PwdListBean();
        pwdListBean.setNum(mDevicePwd.getPwdNum());
        pwdListBean.setPwdType(1);
        listBeans.add(pwdListBean);

        // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除，下面所有
        BleDeviceLocal bleDeviceLocal = AppDatabase.getInstance(this).bleDeviceDao().findBleDeviceFromId(mDevicePwd.getDeviceId());
        if(bleDeviceLocal == null) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService bleDeviceLocal == null");
            return;
        }
        String esn = bleDeviceLocal.getEsn();
        if(TextUtils.isEmpty(esn)) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService esn is Empty");
            return;
        }
        if(App.getInstance().getUserBean() == null) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService uid is Empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService token is Empty");
            return;
        }
        DelKeyBeanReq req = new DelKeyBeanReq();
        req.setPwdList(listBeans);
        req.setSn(esn);
        req.setUid(uid);

        Observable<DelKeyBeanRsp> observable = HttpRequest.getInstance().delKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<DelKeyBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull DelKeyBeanRsp delKeyBeanRsp) {
                if(TextUtils.isEmpty(delKeyBeanRsp.getCode())) {
                    // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除
                    dismissLoadingAndShowFailMessage();
                    Timber.e("delKeyBeanRsp.getCode() is Empty");
                    return;
                }
                if(!delKeyBeanRsp.getCode().equals("200")) {
                    // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除
                    dismissLoadingAndShowFailMessage();
                    Timber.e("");
                    return;
                }
                AppDatabase.getInstance(getApplicationContext()).devicePwdDao().delete(mDevicePwd);
                new Handler(Looper.getMainLooper()).postDelayed(() -> dismissLoadingAndShowSucMessage(), 50);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                dismissLoadingAndShowFailMessage();
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

}
