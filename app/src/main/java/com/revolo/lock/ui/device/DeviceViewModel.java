package com.revolo.lock.ui.device;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockGetAllBindDeviceRspBean;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

// TODO: 2021/4/20 暂时不使用viewModel层，因为需要提供新的改进方案才开始实现
public class DeviceViewModel extends ViewModel {

    private final MutableLiveData<List<WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean>> mWifiListBean;

    public DeviceViewModel() {

        mWifiListBean = new MutableLiveData<>();
      //  new Handler(Looper.getMainLooper()).postDelayed(this::refreshGetAllBindDevicesFromMQTT, 200);

    }

    /*public void refreshGetAllBindDevicesFromMQTT() {
        if(App.getInstance().getUserBean() == null) {
            return;
        }
        LockMessage message=new LockMessage();
        message.setMessageType(2);
        message.setMqtt_topic(MQttConstant.PUBLISH_TO_SERVER);
        message.setMqttMessage( MqttCommandFactory.getAllBindDevices(App.getInstance().getUserBean().getUid()));
        EventBus.getDefault().post(message);
        Timber.d("执行获取设备信息");
        App.getInstance().getMQttService()
                .mqttPublish(MQttConstant.PUBLISH_TO_SERVER,
                        MqttCommandFactory.getAllBindDevices(App.getInstance().getUserBean().getUid()))
                .safeSubscribe(new Observer<MqttData>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull MqttData mqttData) {
                        if(TextUtils.isEmpty(mqttData.getFunc())) {
                            return;
                        }
                        if(!mqttData.getFunc().equals(MQttConstant.GET_ALL_BIND_DEVICE)) {
                            return;
                        }
                        WifiLockGetAllBindDeviceRspBean bean;
                        try {
                            bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockGetAllBindDeviceRspBean.class);
                        } catch (JsonSyntaxException e) {
                            // TODO: 2021/2/6 解析失败的处理
                            Timber.e(e);
                            return;
                        }
                        if(bean == null) {
                            Timber.e("WifiLockGetAllBindDeviceRspBean is null");
                            return;
                        }
                        if(TextUtils.isEmpty(bean.getMsgtype())) {
                            return;
                        }
                        if(!bean.getMsgtype().equals("response")) {
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
                        updateData(bean.getData().getWifiList());
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }*/

    public LiveData<List<WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean>> getWifiListBeans() { return mWifiListBean; }

    private void updateData(List<WifiLockGetAllBindDeviceRspBean.DataBean.WifiListBean> showBeans) {
        mWifiListBean.setValue(showBeans);
    }


}