package com.revolo.lock.receiver;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.revolo.lock.App;
import com.revolo.lock.bean.request.DeviceTokenBeanReq;
import com.revolo.lock.bean.respone.DeviceTokenBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.ui.device.lock.setting.geofence.NotificationHelper;
import com.revolo.lock.ui.mine.MessageListActivity;

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
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        if (remoteMessage.getNotification() != null && remoteMessage.getNotification().getBody() != null) {
            Map<String, String> data = remoteMessage.getData();
            if (data != null) {
                Timber.d("*****************    data = " + data.toString() + "   ********************");
                String type = data.get("type");
                if (TextUtils.isEmpty(type) || !type.equals("3")) {
                    notificationHelper.sendHighPriorityNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), MessageListActivity.class);
                }
            }
        } else {
            notificationHelper.sendHighPriorityNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), MessageListActivity.class);
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
