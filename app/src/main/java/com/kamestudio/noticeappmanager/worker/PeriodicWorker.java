package com.kamestudio.noticeappmanager.worker;

import static com.kamestudio.noticeappmanager.Util.FOREGROUND_START_ACTION;
import static com.kamestudio.noticeappmanager.receiver.PeriodicReceiver.ACTION;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.kamestudio.noticeappmanager.data.DataStoreUtil;
import com.kamestudio.noticeappmanager.receiver.PeriodicReceiver;
import com.kamestudio.noticeappmanager.service.NoticeService;

public class PeriodicWorker extends Worker {
    private static String TAG = "PeriodicWorker";
    public PeriodicWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = this.getApplicationContext();
        Intent broadcastIntent = new Intent(context, PeriodicReceiver.class);
        broadcastIntent.setAction(ACTION);
        context.sendBroadcast(broadcastIntent);

//        Intent intentNoticeService = new Intent(context, NoticeService.class);
//        intentNoticeService.setAction(FOREGROUND_START_ACTION);
//        context.getApplicationContext().startForegroundService(intentNoticeService);
        return Result.success();
    }
}
