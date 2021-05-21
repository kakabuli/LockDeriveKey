package com.revolo.lock.ui.device.lock.setting.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;

import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.room.entity.BleDeviceLocal;

import java.util.List;

import timber.log.Timber;


/**
 * author :
 * time   : 2021/3/15
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class GeoFenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        Timber.d("onReceive:work");

        //Toast.makeText(context, "Geofence triggered.....", Toast.LENGTH_SHORT).show();

        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Timber.d("onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geofenceList) {
            Timber.d("onReceive: %1s", geofence.getRequestId());
        }
//        Location location = geofencingEvent.getTriggeringLocation();
        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_you_have_entered_the_range_of_the_geo_fence);
                notificationHelper.sendHighPriorityNotification(context.getString(R.string.n_geo_fence), context.getString(R.string.t_you_have_entered_the_range_of_the_geo_fence), MapActivity.class);
                BleDeviceLocal deviceLocal = App.getInstance().getBleDeviceLocal();
                if(deviceLocal == null) {
                    return;
                }
                App.getInstance().publishApproachOpen(deviceLocal.getEsn(), deviceLocal.getSetElectricFenceTime());
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                // TODO: 2021/4/23 停留
//                ToastUtils.showShort("GEO_FENCE_TRANSITION_DWELL");
//                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL", "", MapActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_you_have_exited_the_geo_fence);
                notificationHelper.sendHighPriorityNotification(context.getString(R.string.n_geo_fence), context.getString(R.string.t_you_have_exited_the_geo_fence), MapActivity.class);
                break;
        }
    }


}
