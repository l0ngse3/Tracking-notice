package com.kamestudio.noticeappmanager.service;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class RestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Notify App", "onReceive: ");
        if (!isServiceRunning(context, NoticeService.class)) {
            Intent intentService = new Intent(context, NoticeService.class);
            context.startService(intentService);
            Toast.makeText(context, "Service not running", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Service running", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
