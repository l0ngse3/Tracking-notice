package com.kamestudio.noticeappmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.kamestudio.noticeappmanager.Util;
import com.kamestudio.noticeappmanager.worker.PeriodicWorker;

public class PeriodicReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("PeriodicReceiver", "onReceive: ");
        if (Util.isServiceRunning(context)) {
            // invoke periodic work manager request
            WorkManager workManager = WorkManager.getInstance(context);
            OneTimeWorkRequest startServiceRequest = new OneTimeWorkRequest.Builder(PeriodicWorker.class)
                    .build();
            workManager.enqueue(startServiceRequest);
            Log.d("PeriodicReceiver", "Starting service ");
        } else {
            Log.d("PeriodicReceiver", "Service disabled");
        }
    }
}
