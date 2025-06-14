package com.project.rempaudioeditor.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KillNotificationsService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved (Intent rootIntent)  {
        super.onTaskRemoved(rootIntent);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
    }
}