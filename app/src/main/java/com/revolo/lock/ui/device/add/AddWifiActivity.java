package com.revolo.lock.ui.device.add;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.a1anwang.okble.client.core.OKBLEOperation;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.ble.bean.WifiSnBean;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.popup.WifiListPopup;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_INFO;
import static com.revolo.lock.ble.BleProtocolState.CMD_WIFI_LIST_CHECK;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加Wifi
 */
public class AddWifiActivity extends BaseActivity {

    private BleBean mBleBean;
    private WifiListPopup mWifiListPopup;
    private EditText mEtWifiName, mEtPwd;
    private boolean isShowPwd = true;

    private BleDeviceLocal mBleDeviceLocal;
    private String mDefaultName = "";
    private boolean booleanExtra = true;
    private WifiManager wifiManager;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            // TODO: 2021/2/22 做处理
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_wifi;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_wifi_setting));
        mEtWifiName = findViewById(R.id.etWifiName);
        mEtPwd = findViewById(R.id.etPwd);
        mWifiListPopup = new WifiListPopup(this);
        mWifiListPopup.setBackgroundColor(Color.TRANSPARENT);
        mWifiListPopup.setOnItemClickListener((adapter, view, position) -> {
            if (position < 0) {
                return;
            }
            mEtWifiName.setText((String) adapter.getItem(position));
            mWifiListPopup.dismiss();
        });
        applyDebouncingClickListener(findViewById(R.id.btnNext), findViewById(R.id.ivDropdown),
                findViewById(R.id.ivEye), findViewById(R.id.tvSkip));
        initLoading(getString(R.string.t_load_content_loading));

        mDefaultName = getIntent().getStringExtra(Constant.CONNECT_WIFI_NAME);
        booleanExtra = getIntent().getBooleanExtra(Constant.WIFI_SETTING_TO_ADD_WIFI, true);
        onRegisterEventBus();
    }

    /**
     * 获取当前WiFi
     *
     * @return
     */
    private String getCurrWifi() {
        if (null == wifiManager) {
            wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        }
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (null != connectionInfo) {
            return connectionInfo.getSSID();
        }
        return "";
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
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_USER) {
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {

            } else {
                switch (lockMessage.getResultCode()) {

                }
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            if (null != lockMessage.getBleResultBea()) {
                if (lockMessage.getBleResultBea().getCMD() == CMD_LOCK_INFO) {
                    receiveLockBaseInfo(lockMessage.getBleResultBea());
                } else if (lockMessage.getBleResultBea().getCMD() == CMD_WIFI_LIST_CHECK) {
                    receiveWifiList(lockMessage.getBleResultBea());
                }
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                }
            } else {
                dismissLoading();
                switch (lockMessage.getResultCode()) {
                }
            }
        } else {

        }
    }

    @Override
    public void doBusiness() {
        initDevice();
        finishPreAct();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnNext) {
            gotoWifiConnectAct();
            return;
        }
        if (view.getId() == R.id.ivDropdown) {
            showOrDismissWifiList();
            return;
        }
        if (view.getId() == R.id.ivEye) {
            openOrClosePwdEye();
            return;
        }
        if (view.getId() == R.id.tvSkip) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
       /* if (App.getInstance().isWifiSettingNeedToCloseBle()) {
            if (mBleBean != null && mBleBean.getOKBLEDeviceImp() != null) {
                mBleBean.getOKBLEDeviceImp().disConnect(false);
            }
        }*/
        super.onDestroy();
    }

    private void finishPreAct() {
        ActivityUtils.finishActivity(AddDeviceActivity.class);
        ActivityUtils.finishActivity(AddDeviceStep1Activity.class);
        ActivityUtils.finishActivity(AddDevice1StepActivity.class);
        ActivityUtils.finishActivity(AddDevice2StepActivity.class);
        ActivityUtils.finishActivity(AddDeviceQRCodeStep2Activity.class);
        ActivityUtils.finishActivity(DoorSensorCheckActivity.class);
    }

    private void gotoWifiConnectAct() {
        String wifiSn = mEtWifiName.getText().toString().trim();
        if (TextUtils.isEmpty(wifiSn)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_wifi_name);
            return;
        }
        String wifiPwd = mEtPwd.getText().toString();
        if (TextUtils.isEmpty(wifiPwd)) {
            wifiPwd = "";
        }
        Intent intent = new Intent(this, WifiConnectActivity.class);
        intent.putExtra(Constant.WIFI_NAME, wifiSn);
        intent.putExtra(Constant.WIFI_PWD, wifiPwd);
        intent.putExtra(Constant.WIFI_SETTING_TO_ADD_WIFI, booleanExtra);
        startActivity(intent);
    }

    private void showOrDismissWifiList() {
        runOnUiThread(() -> {
            if (mWifiListPopup == null) {
                return;
            }
            if (mWifiSnList == null || mWifiSnList.isEmpty()) {
                Timber.e("get WiFi List is Empty!");
                return;
            }
            if (mWifiListPopup.isShowing()) {
                mWifiListPopup.dismiss();
            } else {
                mWifiListPopup.setPopupGravity(Gravity.BOTTOM);
                mWifiListPopup.showPopupWindow(findViewById(R.id.ivDropdown));
            }
        });
    }

    private void openOrClosePwdEye() {
        ImageView ivEye = findViewById(R.id.ivEye);
        ivEye.setImageResource(isShowPwd ? R.drawable.ic_login_icon_display_blue : R.drawable.ic_login_icon_hide_blue);
        mEtPwd.setInputType(isShowPwd ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        isShowPwd = !isShowPwd;
    }

    private void initDevice() {
        mBleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        /*替换
        mBleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        */// TODO: 2021/3/7 初始化有问题做处理
        if (mBleBean == null) {
            Timber.e("initDevice mBleBean == null");
            return;
        }
        if (mBleBean.getOKBLEDeviceImp() != null) {
            mBleBean.getOKBLEDeviceImp().addNotifyOrIndicateOperation("FFC6", true, mNotifyOrIndicateOperationListener);
            // App.getInstance().openPairNotify(mBleBean.getOKBLEDeviceImp(), mNotifyOrIndicateOperationListener);
            checkBattery();
        }

    }

    private final OKBLEOperation.NotifyOrIndicateOperationListener mNotifyOrIndicateOperationListener = new OKBLEOperation.NotifyOrIndicateOperationListener() {
        @Override
        public void onNotifyOrIndicateComplete() {
            Timber.d("openControlNotify onNotifyOrIndicateComplete 打开配网通知成功");
        }

        @Override
        public void onFail(int code, String errMsg) {
            Timber.e("openControlNotify onFail errMsg: %1s", errMsg);
            // TODO 暂定
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("WiFi Setting Failed");
            finish();
        }

        @Override
        public void onExecuteSuccess(OKBLEOperation.OperationType type) {

        }
    };

    private void checkBattery() {
        if (mBleBean == null || null == mBleBean.getOKBLEDeviceImp()) {
            Timber.e("checkBattery mBleBean == null");
            return;
        }
        showLoading();
        // wifi配网前记得查询电量
        LockMessage lockMessage = new LockMessage();
        lockMessage.setMessageType(3);
        lockMessage.setBytes(BleCommandFactory
                .checkLockBaseInfoCommand(mBleBean.getPwd1(), mBleBean.getPwd3()));
        lockMessage.setMac(mBleBean.getOKBLEDeviceImp().getMacAddress());
        EventBus.getDefault().post(lockMessage);

        /*替换WiFi配网前记得查询电量
        App.getInstance().writeControlMsg(BleCommandFactory
                        .checkLockBaseInfoCommand(mBleBean.getPwd1(), mBleBean.getPwd3()),
                mBleBean.getOKBLEDeviceImp());*/

        // 临时加一个6秒后执行取消loading
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                dismissLoading();
//            }
//        }, 6000);
    }

    private void receiveLockBaseInfo(BleResultBean bleResultBean) {
        int power = BleByteUtil.byteToInt(bleResultBean.getPayload()[11]);
        Timber.d("receiveLockBaseInfo battery power: %1d", power);
        mBleDeviceLocal.setLockPower(power);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        if (power > 20) {
            getWifiList();
        } else {
            dismissLoading();
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_low_battery_cant_pair_wifi);
        }
    }

    private void getWifiList() {
        if (mBleBean == null) {
            dismissLoading();
            Timber.e("getWifiList mBleBean == null");
            return;
        }
        if (mBleBean.getOKBLEDeviceImp() == null) {
            dismissLoading();
            Timber.e("getWifiList mBleBean.getOKBLEDeviceImp() == null");
            return;
        }
        LockMessage lockMessage = new LockMessage();
        lockMessage.setBytes(BleCommandFactory.wifiListSearchCommand());
        lockMessage.setMac(mBleBean.getOKBLEDeviceImp().getMacAddress());
        lockMessage.setMessageType(3);
        lockMessage.setBleChr(1);
        EventBus.getDefault().post(lockMessage);
        /*替换
        App.getInstance().writePairMsg(BleCommandFactory.wifiListSearchCommand(), mBleBean.getOKBLEDeviceImp());*/
    }

    private int mWifiTotalNum = 0;
    private final HashMap<Integer, WifiSnBean> mWifiHashMap = new HashMap<>();
    private final List<String> mWifiSnList = new ArrayList<>();

    private void receiveWifiList(BleResultBean bleResultBean) {
        Timber.d("receiveWifiList 接收信息：%1s", ConvertUtils.int2HexString(bleResultBean.getCMD()));
        if (bleResultBean.getCMD() == CMD_WIFI_LIST_CHECK) {
            byte[] payload = bleResultBean.getPayload();
            mWifiTotalNum = payload[0];
            int no = payload[1];
            WifiSnBean wifiSnBean = mWifiHashMap.get(no);
            byte[] data = new byte[11];
            System.arraycopy(payload, 5, data, 0, data.length);
            WifiSnBean.WifiSnBytesBean bytesBean = new WifiSnBean.WifiSnBytesBean(payload[4], payload[3]);
            bytesBean.setBytes(data);
            List<WifiSnBean.WifiSnBytesBean> bytesBeans;
            if (wifiSnBean == null) {
                // 新WifiSn
                bytesBeans = new ArrayList<>();
                bytesBeans.add(bytesBean);
                wifiSnBean = new WifiSnBean();
                wifiSnBean.setRssi(payload[2]);
                wifiSnBean.setWifiIndex(no);
                wifiSnBean.setWifiSnBytesBeans(bytesBeans);
                mWifiHashMap.put(no, wifiSnBean);
            } else {
                bytesBeans = wifiSnBean.getWifiSnBytesBeans();
                if (bytesBeans == null) {
                    bytesBeans = new ArrayList<>();
                }
                bytesBeans.add(bytesBean);
                wifiSnBean.setWifiSnBytesBeans(bytesBeans);
            }
            Timber.d("receiveWifiList total: %1d, no: %2d", mWifiTotalNum, no);
            // TODO: 2021/1/21 有可能缺失的就是最后一包 后期做个超时
            if (no == (mWifiTotalNum - 1)) {
                // 最后一个wifi
                boolean isRemain = (bytesBean.getSnLenTotal() % 11 != 0);
                int index = bytesBean.getSnLenTotal() / 11;
                if (bytesBean.getIndex() == (isRemain ? index : index - 1)) {
                    processWifi();
                }
            }
            if (!TextUtils.isEmpty(mDefaultName)) mEtWifiName.setText(mDefaultName);
        }
        dismissLoading();
    }

    private final List<Integer> mLackNoList = new ArrayList<>();

    private void processWifi() {
        if (!mWifiHashMap.isEmpty()) {
            mWifiSnList.clear();
            for (int i = 0; i < mWifiTotalNum; i++) {
                WifiSnBean wifiSnBean = mWifiHashMap.get(i);
                if (wifiSnBean != null) {
                    mWifiSnList.add(wifiSnBean.getWifiSn());
                } else {
                    mLackNoList.add(i);
                }
            }
        }
        if (!mLackNoList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mLackNoList.size(); i++) {
                sb.append(mLackNoList.get(i));
                if (i < mLackNoList.size() - 1) {
                    sb.append(",");
                }
            }
            Timber.d("缺少的序列号为：%1s", sb.toString());
        }
        if (!mWifiSnList.isEmpty()) {
            String currSsid = getCurrWifi();
            int index = -1;
            if (null != currSsid && !"".equals(currSsid)) {
                currSsid=currSsid.trim().replace("\"","");
                Timber.d("currSsid: %1s",currSsid);
                for (int i = 0; i < mWifiSnList.size(); i++) {
                    if (null != mWifiSnList.get(i) && !"".equals(mWifiSnList.get(i))) {
                        if (mWifiSnList.get(i).toUpperCase().equals(currSsid.toUpperCase())) {
                            index = i;
                        }
                    }
                    Timber.d("WifiSn: %1s", mWifiSnList.get(i));
                }
                if (index != -1) {
                    mDefaultName = mWifiSnList.get(index);
                    mWifiSnList.remove(index);
                    mWifiSnList.add(0, mDefaultName);

                }
            }
        }
        runOnUiThread(() -> {
            if (mWifiListPopup != null) {
                mWifiListPopup.updateWifiList(mWifiSnList);
                if (mWifiSnList.isEmpty()) {
                    return;
                }
                if (TextUtils.isEmpty(mDefaultName)) {
                    mEtWifiName.setText(mWifiSnList.get(0));
                } else {
                    mEtWifiName.setText(mDefaultName);
                }
            }
        });
    }
}
