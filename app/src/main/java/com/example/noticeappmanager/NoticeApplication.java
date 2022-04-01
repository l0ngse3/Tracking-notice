package com.example.noticeappmanager;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.noticeappmanager.viewmodel.NotificationViewModel;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class NoticeApplication extends Application {

    private static AppOpenManager appOpenManager;
    private static NoticeApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
            }
        });
        appOpenManager = new AppOpenManager(this);
    }

    public NoticeApplication getInstance(){
        if(instance == null){
            instance = new NoticeApplication();
        }
        return instance;
    }
}
