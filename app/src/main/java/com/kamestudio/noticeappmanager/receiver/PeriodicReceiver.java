package com.kamestudio.noticeappmanager.receiver;

import static com.kamestudio.noticeappmanager.Util.FOREGROUND_START_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.kamestudio.noticeappmanager.Util;
import com.kamestudio.noticeappmanager.service.NoticeService;
import com.kamestudio.noticeappmanager.worker.PeriodicWorker;

public class PeriodicReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.kamestudio.noticeappmanager.PeriodicReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("PeriodicReceiver", "onReceive: ");
//        if (Util.isServiceRunning(context)) {
//            Intent intentNoticeService = new Intent(context, NoticeService.class);
//            intentNoticeService.setAction(FOREGROUND_START_ACTION);
//            context.getApplicationContext().startForegroundService(intentNoticeService);
//
//            Log.d("PeriodicReceiver", "Starting service ");
//        } else {
//            Log.d("PeriodicReceiver", "Service disabled");
//        }

        Intent intentNoticeService = new Intent(context, NoticeService.class);
        intentNoticeService.setAction(FOREGROUND_START_ACTION);
        context.getApplicationContext().startForegroundService(intentNoticeService);
    }
}
