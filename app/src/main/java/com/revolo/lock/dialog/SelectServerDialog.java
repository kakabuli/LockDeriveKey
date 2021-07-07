package com.revolo.lock.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import com.revolo.lock.R;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.net.HttpRequest;

import timber.log.Timber;

/**
 * author : zhougm
 * time   : 2021/7/7
 * E-mail : zhouguimin@kaadas.com
 * desc   : 选择服务器
 */
public class SelectServerDialog extends Dialog {


    public SelectServerDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_server);
        RadioGroup radioGroup = findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_button_249) {
                HttpRequest.HTTP_BASE_HOST = HttpRequest.LOCAL_HOST_249;
                MQttConstant.MQTT_BASE_URL = MQttConstant.MQTT_URL_249;
                Timber.d("##########    http = " + HttpRequest.LOCAL_HOST_249 + " mqtt = " + MQttConstant.MQTT_URL_249 + "   ##########");
                dismiss();
            } else if (checkedId == R.id.radio_button_248) {
                HttpRequest.HTTP_BASE_HOST = HttpRequest.LOCAL_HOST_248;
                MQttConstant.MQTT_BASE_URL = MQttConstant.MQTT_URL_248;
                Timber.d("##########    http = " + HttpRequest.LOCAL_HOST_248 + " mqtt = " + MQttConstant.MQTT_URL_248 + "   ##########");
                dismiss();
            } else if (checkedId == R.id.radio_button_alpha) {
                HttpRequest.HTTP_BASE_HOST = HttpRequest.LOCAL_HOST_ALPHA;
                MQttConstant.MQTT_BASE_URL = MQttConstant.MQTT_URL_ALPHA;
                Timber.d("##########    http = " + HttpRequest.LOCAL_HOST_ALPHA + " mqtt = " + MQttConstant.MQTT_URL_ALPHA + "   ##########");
                dismiss();
            } else if (checkedId == R.id.radio_button_abroad) {
                HttpRequest.HTTP_BASE_HOST = HttpRequest.LOCAL_HOST_ABROAD;
                MQttConstant.MQTT_BASE_URL = MQttConstant.MQTT_URL_ABROAD;
                Timber.d("##########    http = " + HttpRequest.LOCAL_HOST_ABROAD + " mqtt = " + MQttConstant.MQTT_URL_ABROAD + "   ##########");
                dismiss();
            }
        });
    }
}
