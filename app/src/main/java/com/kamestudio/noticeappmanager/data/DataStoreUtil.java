package com.kamestudio.noticeappmanager.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kamestudio.noticeappmanager.Util;
import com.kamestudio.noticeappmanager.enity.ItemPackage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class DataStoreUtil implements Util {

    private Context context;
    public SharedPreferences preferences;
    public static DataStoreUtil instance = null;

    public static DataStoreUtil getInstance(Context context){
        if(instance == null){
            instance = new DataStoreUtil(context);
        }
        return instance;
    }

    private DataStoreUtil(Context _context) {
        this.context = _context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }


    public List<ItemPackage> getPackages(){
        String data_json = preferences.getString("data", "N/A");
        List<ItemPackage> itemPackageList = new ArrayList<>();
        if(!data_json.equals("N/A")){
            Gson gson = new Gson();
            Type type = new TypeToken<List<ItemPackage>>() {}.getType();
            itemPackageList = (List<ItemPackage>) gson.fromJson(data_json, type);
        }
        Log.d(TAG, "getPackages: -- " + data_json);
        return itemPackageList;
    }

    public boolean setPackages(List<ItemPackage> itemPackageList){
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        Type type = new TypeToken<List<ItemPackage>>() {}.getType();
        Log.d(TAG, "setData: "+gson.toJson(itemPackageList, type));
        editor.putString("data", gson.toJson(itemPackageList, type));
        return editor.commit();
    }

    public boolean setData(String key, String data){
        SharedPreferences.Editor editor = preferences.edit();
        Log.d(TAG, "setData: "+ key +" : " + data);
        editor.putString(key, data);
        editor.apply();
        return true;
    }

    public String getData(String key){
        String data_json = preferences.getString(key, "N/A");
        Log.d(TAG, "getData: "+ key +" : " + data_json);
        return data_json;
    }

    public boolean setNotifyChannel(String list_channel){
        SharedPreferences.Editor editor = preferences.edit();
        Log.d(TAG, "setData: "+list_channel);
        editor.putString("notify_list", list_channel);
        return editor.commit();
    }

    public String getNotifyChannel(){
        String data_json = preferences.getString("notify_list", "N/A");
        Log.d(TAG, "getNotifyChannel: -- " + data_json);
        return data_json;
    }

}
