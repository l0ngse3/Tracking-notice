package com.kamestudio.noticeappmanager.receiver;

import static com.kamestudio.noticeappmanager.Util.FOREGROUND_START_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.kamestudio.noticeappmanager.Util;
import com.kamestudio.noticeappmanager.service.NoticeService;
import com.kamestudio.noticeappmanager.worker.PeriodicWorker;

public class RestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RestartReceiver", "RestartReceiver - onReceive: ");
        if (Util.isServiceRunning(context)) {
            // invoke periodic work manager request
            Intent intentNoticeService = new Intent(context, NoticeService.class);
            intentNoticeService.setAction(FOREGROUND_START_ACTION);
            context.getApplicationContext().startForegroundService(intentNoticeService);
            Log.d("RestartReceiver", "Starting service ");
        } else {
            Log.d("RestartReceiver", "Service disabled");
        }
    }

}
