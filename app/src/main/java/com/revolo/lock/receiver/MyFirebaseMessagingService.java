package com.revolo.lock.receiver;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.revolo.lock.App;
import com.revolo.lock.bean.request.DeviceTokenBeanReq;
import com.revolo.lock.bean.respone.DeviceTokenBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
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
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Timber.d("**************************   onMessageReceived   **************************");
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
