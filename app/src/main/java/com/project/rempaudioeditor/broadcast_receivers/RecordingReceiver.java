package com.project.rempaudioeditor.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RecordingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("MEDIA_CONTROL").putExtra("action", intent.getAction()));
    }
}
