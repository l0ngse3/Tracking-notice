package com.kamestudio.noticeappmanager.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import com.kamestudio.noticeappmanager.MainActivity;
import com.kamestudio.noticeappmanager.R;
import com.kamestudio.noticeappmanager.Util;
import com.kamestudio.noticeappmanager.data.DataStoreUtil;
import com.kamestudio.noticeappmanager.enity.ItemPackage;
import com.kamestudio.noticeappmanager.receiver.PeriodicReceiver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

public class NoticeService extends NotificationListenerService implements Util {
    private static final String TAG = "NoticeService";
    public static String APP_PACKAGE_NAME = "com.kamestudio.noticeappmanager";
    public static String MAIN_CHANNEL = "Notice manager";
    public static String IS_RUNNING_STATE_NAME = "is_running";
    private List<ItemPackage> packageListChoosen;
    private NotificationManager manager;
    private String channelNameMain = "Listen notification channel";
    private String channelNameSub = "Notify channel";

    private int FOREGROUND_CHANNEL_ID = 123456;

    private NotificationChannel serviceChannel;

    //handle turn off notice on sub channel
    public static String ACTION_STOP_SERVICE = "com.noticeappmanager.ACTION_STOP_SERVICE";
    public static String ACTION_STOP_SOUND = "com.noticeappmanager.ACTION_STOP_SOUND";
    public static String ACTION_START_SERVICE = "com.noticeappmanager.ACTION_START";
    public boolean isStopSubChannel = true;
    MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
//        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        packageListChoosen = DataStoreUtil.getInstance(this).getPackages();

        EventBus.getDefault().register(this);
        Log.d(TAG, "onCreate: " + Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String notificationPackageName = getNotificationPackageName(sbn);
        //Log.d("Notify App", notificationPackageName + ": " + sbn.getPackageName().equals(APP_PACKAGE_NAME));
        packageListChoosen = DataStoreUtil.getInstance(this).getPackages();
        Log.d(TAG, "onNotificationPosted: " + notificationPackageName);
        if (!notificationPackageName.equals(APP_PACKAGE_NAME)) {
            ItemPackage itemPackage = isPackageChoosen(notificationPackageName);
//            ItemPackage itemPackage = new ItemPackage("", true);

            if (itemPackage != null) {
                isStopSubChannel = false;
                // com.google.android.apps.messaging
                // com.google.android.dialer

                if (mediaPlayer == null) {
//                    mediaPlayer = MediaPlayer.create(this, R.raw.sound);
                    String soundPath = itemPackage.getSoundPath();
                    File file = new File(soundPath);
                    Log.d(TAG, "onNotificationPosted: File existed -- " + file.exists() + ", File can read -- " + file.canRead());

                    if (file.exists() && file.canRead()) {
                        mediaPlayer = MediaPlayer.create(this, Uri.parse(soundPath));
                    }
                    else{
                        mediaPlayer = MediaPlayer.create(this, R.raw.sound);
                    }
                    Log.d(TAG, "onNotificationPosted: sound path -- " + soundPath);
                    mediaPlayer.setLooping(true); // Set looping
                    mediaPlayer.setVolume(100, 100);
                }

                Toast.makeText(this, "Start sound", Toast.LENGTH_LONG);
                mediaPlayer.start();
            }
        }
    }


    public void startCustomForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

        serviceChannel = new NotificationChannel(MAIN_CHANNEL, channelNameMain, NotificationManager.IMPORTANCE_HIGH);
        serviceChannel.setLightColor(Color.BLUE);
        serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        serviceChannel.setShowBadge(true);

        assert manager != null;
        manager.createNotificationChannel(serviceChannel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, MAIN_CHANNEL);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Tracking notice")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SYSTEM)
                .setSmallIcon(R.drawable.icon_app)
                .setAutoCancel(false)
                .build();
        //manager.notify(FOREGROUND_CHANNEL_ID, notification);
        ServiceCompat.startForeground(this,
                FOREGROUND_CHANNEL_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals(FOREGROUND_START_ACTION)){
            String APP_PACKAGE_NAME = getApplicationContext().getString(R.string.app_package_name);
            startNotificationService(APP_PACKAGE_NAME);
        }
        else{
            onDestroy();
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String notificationPackageName = getNotificationPackageName(sbn);
//        StatusBarNotification[] activeNotifications = this.getActiveNotifications();
        ItemPackage itemPackage = isPackageChoosen(notificationPackageName);
        if (itemPackage != null && mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Log.d(TAG, "onNotificationRemoved: " + notificationPackageName);
    }

    private String getNotificationPackageName(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        return packageName;
    }

    private void startNotificationService(String notificationPackageName) {
//        Toast.makeText(this, notificationPackageName + ": coming", Toast.LENGTH_LONG).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startCustomForeground();
            Log.d(TAG, "startMyOwnForeground: ");
        } else {
            startForeground(FOREGROUND_CHANNEL_ID, new Notification());
            Log.d(TAG, "startForeground: ");
        }
    }

    private ItemPackage isPackageChoosen(String pkg) {
        ItemPackage returnItem = null;
        for (ItemPackage item : packageListChoosen) {
            if (pkg.equals(item.getPackageInfoName()) && item.isTurnOn()) {
                returnItem = item;
                break;
            }
        }
        return returnItem;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopForeground(true);
        stopSelf();
        EventBus.getDefault().unregister(this);

        // reschedule service if it still running
        if (Util.isServiceRunning(this)){
            // call boardcast receiver for trigger restart service
            Intent broadcastIntent = new Intent(this, PeriodicReceiver.class);
            sendBroadcast(broadcastIntent);
        }
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void handleEvent(MessageEvent event) {
        Log.d(TAG, "handleEvent: " + event.message);
        if (event.message.equals(ACTION_STOP_SERVICE)) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
            onDestroy();
        } else if (event.message.equals(ACTION_STOP_SOUND)) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
        else if (event.message.equals(ACTION_START_SERVICE)){
            onCreate();
        }
    }
}