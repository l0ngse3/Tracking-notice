package com.kamestudio.noticeappmanager.enity;

import android.content.pm.PackageInfo;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ItemPackage {
    String packageInfoName;
    boolean isChosen;
    String soundPath = "";
    boolean isTurnOn;

    public ItemPackage(String packageInfoName, boolean isChosen) {
        this.packageInfoName = packageInfoName;
        this.isChosen = isChosen;
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

    public String getPackageInfoName() {
        return packageInfoName;
    }

    public void setPackageInfo(String packageInfoName) {
        this.packageInfoName = packageInfoName;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPackage that = (ItemPackage) o;

        if (!packageInfoName.equals(that.packageInfoName)) {
            return false;
        }

        if (isChosen != that.isChosen) {
            return false;
        }

        if (!soundPath.equals(that.soundPath)) {
            return false;
        }

        if (isTurnOn != that.isTurnOn) {
            return false;
        }
        return true;
    }
}

