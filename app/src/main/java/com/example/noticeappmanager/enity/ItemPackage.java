package com.example.noticeappmanager.enity;

import android.content.pm.PackageInfo;
import android.net.Uri;
import android.util.Log;

import java.util.Comparator;
import java.util.Objects;

public class ItemPackage {
    PackageInfo packageInfo;
    boolean isChoosen;
    String soundPath = "";
    boolean isTurnOn;

    public ItemPackage(PackageInfo packageInfo, boolean isChoosen) {
        this.packageInfo = packageInfo;
        this.isChoosen = isChoosen;
        soundPath = "";
    }

    public boolean isTurnOn() {
        return isTurnOn;
    }

    public void setTurnOn(boolean turnOn) {
        isTurnOn = turnOn;
    }

    public String getSoundPath() {
        return soundPath;
    }

    public void setSoundPath(String soundPath) {
        this.soundPath = soundPath;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public boolean isChoosen() {
        return isChoosen;
    }

    public void setChoosen(boolean choosen) {
        isChoosen = choosen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPackage that = (ItemPackage) o;

        if(!packageInfo.packageName.equals(that.packageInfo.packageName)){
            return false;
        }

        if(isChoosen != that.isChoosen){
            return false;
        }

        if(!soundPath.equals(that.soundPath)){
            return false;
        }

        if(isTurnOn != that.isTurnOn){
            return false;
        }
        return  true;
    }

}
