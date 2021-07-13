package com.revolo.lock.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.bean.request.DeviceTokenBeanReq;
import com.revolo.lock.bean.respone.DeviceTokenBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
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
        if (remoteMessage.getNotification() != null && remoteMessage.getNotification().getBody() != null) {
            sendNotification(getApplicationContext(), remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        } else {
            sendNotification(getApplicationContext(), remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    private void sendNotification(Context iContext, String messageTitle, String messageBody) {

        NotificationManager notificationManager = (NotificationManager) iContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MessageListActivity.class); // 接收到通知后，点击通知，启动 MessageActivity

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long[] pattern = {500, 500, 500, 500, 500};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "fcm")
                .setTicker(messageTitle)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Fcm message")
                .setContentText(messageBody)
                .setWhen(System.currentTimeMillis() + 1000)
                .setVibrate(pattern)
                .setLights(Color.BLUE, 1, 1);
        builder.setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(0, builder.build());
    }
}
