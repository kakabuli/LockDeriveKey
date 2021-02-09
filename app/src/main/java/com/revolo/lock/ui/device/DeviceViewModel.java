package com.revolo.lock.ui.device;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.bean.showBean.WifiShowBean;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockGetAllBindDeviceRspBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class DeviceViewModel extends ViewModel {

    private final MutableLiveData<List<WifiShowBean>> mWifiShowBean;

    public DeviceViewModel() {

        mWifiShowBean = new MutableLiveData<>();
        new Handler(Looper.getMainLooper()).postDelayed(this::initGetAllBindDevicesFromMQTT, 500);

    }

    public void initGetAllBindDevicesFromMQTT() {

        Timber.d("执行获取设备信息");
        App.getInstance().getMqttService()
                .mqttPublish(MqttConstant.PUBLISH_TO_SERVER,
                        MqttCommandFactory.getAllBindDevices(App.getInstance().getUserBean().getUid()))
                .safeSubscribe(new Observer<MqttData>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull MqttData mqttData) {
                        Gson gson = new Gson();
                        WifiLockGetAllBindDeviceRspBean bean;
                        try {
                            bean = gson.fromJson(mqttData.getPayload(), WifiLockGetAllBindDeviceRspBean.class);
                        } catch (JsonSyntaxException e) {
                            // TODO: 2021/2/6 解析失败的处理
                            Timber.e(e);
                            return;
                        }
                        if(bean == null) {
                            Timber.e("WifiLockGetAllBindDeviceRspBean is null");
                            return;
                        }
                        if(bean.getData() == null) {
                            Timber.e("WifiLockGetAllBindDeviceRspBean.Data is null");
                            return;
                        }
                        if(bean.getData().getWifiList() == null) {
                            Timber.e("WifiLockGetAllBindDeviceRspBean..getData().getWifiList() is null");
                            return;
                        }
                        if(bean.getData().getWifiList().isEmpty()) {
                            Timber.e("WifiLockGetAllBindDeviceRspBean..getData().getWifiList().isEmpty()");
                            return;
                        }
                        List<WifiShowBean> showBeans = new ArrayList<>();
                        for (WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean wifiListBean : bean.getData().getWifiList()) {
                            showBeans.add(new WifiShowBean(2, 1,1, wifiListBean));
                        }
                        updateData(showBeans);
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public LiveData<List<WifiShowBean>> getWifiShowBeans() { return mWifiShowBean; }

    private void updateData(List<WifiShowBean> showBeans) {
        mWifiShowBean.setValue(showBeans);
    }


}