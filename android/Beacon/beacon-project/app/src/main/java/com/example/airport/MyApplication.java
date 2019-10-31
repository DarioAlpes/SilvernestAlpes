package com.example.airport;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;

public class MyApplication extends Application {

    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();

        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                Beacon beacon = getFirstBeacon(list);
                String mensaje = "";
                if (beacon != null){
                    showNotification(
                            "Beacon",
                            "Entrando a la zona del beacon " + beacon.getProximityUUID());
                }
            }
            @Override
            public void onExitedRegion(Region region) {
                if (region != null){
                    showNotification(
                            "Beacon",
                            "Saliendo de  la zona del beacon " + region.getProximityUUID());
                }
            }
        });
        beaconManager.connect(new  BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region("monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 20189, 61086));
//                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 22504, 48827));
            }
        });
    }

    private Beacon getFirstBeacon(List<Beacon> list){
        if (null != list && list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
