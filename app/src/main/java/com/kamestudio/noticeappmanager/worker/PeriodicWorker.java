package com.kamestudio.noticeappmanager.worker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.kamestudio.noticeappmanager.data.DataStoreUtil;
import com.kamestudio.noticeappmanager.service.NoticeService;

public class PeriodicWorker extends Worker {
    private static String TAG = "PeriodicWorker";
    public PeriodicWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Boolean is_running = Boolean.parseBoolean(DataStoreUtil.
                getInstance(this.getApplicationContext()).
                getData(NoticeService.IS_RUNNING_STATE_NAME));

        if(is_running){
            Log.d(TAG, "starting service from doWork");
            Intent intent = new Intent(this.getApplicationContext(), NoticeService.class);
            ContextCompat.startForegroundService(this.getApplicationContext(), intent);
        }
        return Result.success();
    }
}
