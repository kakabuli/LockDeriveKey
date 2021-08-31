package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.ui.device.lock.setting.ChangeLockNameActivity;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_MQTT;


/**
 * author :
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class AddWifiSucActivity extends BaseActivity {

    private boolean booleanExtra = true;

    @Override
    public void initData(@Nullable Bundle bundle) {
        isShowNetState = false;
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_wifi_suc;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_connect_wifi));
        applyDebouncingClickListener(findViewById(R.id.btnReDistribution));
    }

    @Override
    public void doBusiness() {

        booleanExtra = getIntent().getBooleanExtra(Constant.WIFI_SETTING_TO_ADD_WIFI, true);

        finishPreAct();
        refreshGetAllBindDevicesFromMQTT();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnReDistribution) {
            if (booleanExtra) {
                startActivity(new Intent(this, ChangeLockNameActivity.class));
            }
            finish();
        }
    }

    /**
     * 获取当前用户绑定的设备
     */
    public void refreshGetAllBindDevicesFromMQTT() {
        if (App.getInstance().getUserBean() == null) {
            Timber.e("refreshGetAllBindDevicesFromMQTT getActivity() == null");
            return;
        }
        Timber.e("执行获取设备信息");
        LockMessage lockMessage = new LockMessage();
        lockMessage.setMessageType(2);
        lockMessage.setMqtt_topic(MQttConstant.PUBLISH_TO_SERVER);
        lockMessage.setMqtt_message_code(MQttConstant.GET_ALL_BIND_DEVICE);
        lockMessage.setMqttMessage(MqttCommandFactory.getAllBindDevices(App.getInstance().getUserBean().getUid()));
        lockMessage.setSn("");
        lockMessage.setMessageType(MSG_LOCK_MESSAGE_MQTT);
        lockMessage.setBytes(null);
        pushMessage(lockMessage);
    }

    public void pushMessage(LockMessage message) {
        EventBus.getDefault().post(message);
    }

    private void finishPreAct() {
        ActivityUtils.finishActivity(WifiConnectActivity.class);
        ActivityUtils.finishActivity(AddWifiActivity.class);
        ActivityUtils.finishActivity(InputESNActivity.class);
    }
}
