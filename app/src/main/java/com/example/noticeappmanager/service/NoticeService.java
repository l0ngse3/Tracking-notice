package com.example.noticeappmanager.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.noticeappmanager.CallBack;
import com.example.noticeappmanager.CallBackReceiver;
import com.example.noticeappmanager.MainActivity;
import com.example.noticeappmanager.R;
import com.example.noticeappmanager.Util;
import com.example.noticeappmanager.data.DataStoreUtil;
import com.example.noticeappmanager.enity.ItemPackage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class NoticeService extends NotificationListenerService implements Util {
    public static String APP_PACKAGE_NAME = "com.example.noticeappmanager";
    public static String MAIN_CHANNEL = "Notice manager";
    public static String SUB_CHANNEL = "Notice manager Alerting";
    private List<ItemPackage> packageListChoosen;
    private NotificationManager manager;

    private String channelNameMain = "Listen notification channel";
    private String channelNameSub = "Notify channel";

    private int FOREGROUND_CHANNEL_ID = 123456;
    private int SUB_CHANNEL_ID = 1234568;
    private Uri soundUri;

    private NotificationChannel serviceChannel, notifyChannel;

    //handle turn off notice on sub channel
    public static String ACTION_STOP_SERVICE = "com.noticeappmanager.ACTION_STOP_SERVICE";
    public static String ACTION_STOP_SOUND = "com.noticeappmanager.ACTION_STOP_SOUND";
    public static String ACTION_START = "com.noticeappmanager.ACTION_START";
    public boolean isStopSubChannel = true;
    MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
//        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = (NotificationManager) getSystemService(NotificationManager.class);
        }
        packageListChoosen = DataStoreUtil.getInstance(this).getPackages();

        EventBus.getDefault().register(this);

        String APP_PACKAGE_NAME = getApplicationContext().getString(R.string.app_package_name);
        startNotificationService(APP_PACKAGE_NAME);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String notificationPackageName = getNotificationPackageName(sbn);
        //Log.d("Notify App", notificationPackageName + ": " + sbn.getPackageName().equals(APP_PACKAGE_NAME));
        packageListChoosen = DataStoreUtil.getInstance(this).getPackages();
        Log.d(TAG, "onNotificationPosted: " + notificationPackageName);
        if (!notificationPackageName.equals(APP_PACKAGE_NAME)) {
            boolean isChoosen = false;

            isChoosen = isPackageChoosen(notificationPackageName);

            if(isChoosen) {
                isStopSubChannel = false;

                if(mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(this, R.raw.sound);
                    mediaPlayer.setLooping(true); // Set looping
                    mediaPlayer.setVolume(100, 100);
                }

                Toast.makeText(this, "Start sound", Toast.LENGTH_LONG);
                mediaPlayer.start();
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public void startCustomForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        serviceChannel = new NotificationChannel(MAIN_CHANNEL, channelNameMain, NotificationManager.IMPORTANCE_HIGH);
        serviceChannel.setLightColor(Color.BLUE);
        serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        serviceChannel.setShowBadge(true);

        assert manager != null;
        manager.createNotificationChannel(serviceChannel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, MAIN_CHANNEL);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Notice from main channel: " + APP_PACKAGE_NAME)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SYSTEM)
                .setSmallIcon(R.drawable.ic_baseline_android_24)
                .setAutoCancel(false)
                .build();
        //manager.notify(FOREGROUND_CHANNEL_ID, notification);
        startForeground(FOREGROUND_CHANNEL_ID, notification);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String notificationPackageName = getNotificationPackageName(sbn);
        StatusBarNotification[] activeNotifications = this.getActiveNotifications();

        Toast.makeText(this, notificationPackageName+": removed", Toast.LENGTH_LONG).show();

        boolean isChoosen = false;
        isChoosen = isPackageChoosen(notificationPackageName);
        if(isChoosen && mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Log.d("Notify App", "onNotificationRemoved: " + notificationPackageName);
    }

    private String getNotificationPackageName(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        return packageName;
    }

    private void startNotificationService(String notificationPackageName) {
        Log.d("Notify App", "onNotificationPosted: ");
        Toast.makeText(this, notificationPackageName + ": coming", Toast.LENGTH_LONG).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startCustomForeground();
            Log.d("Notify App", "startMyOwnForeground: ");
        } else {
            startForeground(FOREGROUND_CHANNEL_ID, new Notification());
            Log.d("Notify App", "startForeground: ");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void pushNotificationOnSubChannel(String notificationPackageName, ItemPackage itemPackage){
        Log.d("Notify App", "pushNotificationOnSubChannel: "+notificationPackageName);
        Toast.makeText(this, notificationPackageName, Toast.LENGTH_SHORT).show();

        String old_channel = DataStoreUtil.getInstance(getApplicationContext()).getNotifyChannel();
        manager.deleteNotificationChannel(old_channel);


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Log.d(TAG, "pushNotificationOnSubChannel: "+ Build.VERSION.SDK_INT);
        channelNameSub = notificationPackageName;

        soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.sound);
        Log.d(TAG, soundUri.toString());
//        Uri soundUri = Uri.parse("file://" + itemPackage.getSoundPath());
        SUB_CHANNEL = Util.getRandomChannelName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifyChannel = new NotificationChannel(SUB_CHANNEL, channelNameSub, NotificationManager.IMPORTANCE_HIGH);
            notifyChannel.setLightColor(Color.BLUE);
            notifyChannel.enableVibration(true);
            notifyChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notifyChannel.setShowBadge(true);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            notifyChannel.setSound(soundUri, audioAttributes);

            assert manager != null;
            manager.createNotificationChannel(notifyChannel);
            DataStoreUtil.getInstance(getApplicationContext()).setNotifyChannel(SUB_CHANNEL);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, SUB_CHANNEL);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Notice from sub channel: " + notificationPackageName)
                .setContentText(notificationPackageName)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setSmallIcon(R.drawable.ic_baseline_android_24)
                .setAutoCancel(true)
                .setOngoing(false)
//                .setSound(soundUri)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(SUB_CHANNEL_ID, notification);
        //manager.notify(SUB_CHANNEL_ID, notification);
    }

    private boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NoticeService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPackageChoosen(String pkg){
        boolean isChoosen = false;
        for(ItemPackage item : packageListChoosen){
            if(pkg.equals(item.getPackageInfo().packageName) && item.isTurnOn()){
                isChoosen = true;
                break;
            }
        }
        return isChoosen;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        stopForeground(true);
        stopSelf();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void handleEvent(MessageEvent event){
        Log.d(TAG, "handleEvent: " + event.message);
    }
}