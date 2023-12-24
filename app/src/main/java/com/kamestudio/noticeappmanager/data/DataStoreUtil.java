package com.kamestudio.noticeappmanager.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.kamestudio.noticeappmanager.Util;
import com.kamestudio.noticeappmanager.enity.ItemPackage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class DataStoreUtil implements Util {
    private static final String TAG = "DataStoreUtil";
    private Context context;
    public SharedPreferences preferences;
    public static DataStoreUtil instance = null;
    private static Gson gson = null;

    public static DataStoreUtil getInstance(Context context){
        if(instance == null){
            instance = new DataStoreUtil(context);
        }
        return instance;
    }

    private static Gson getGsonInstance(){
        if(gson == null){
//            GsonBuilder gsonBuilder = new GsonBuilder();
//            gsonBuilder.registerTypeAdapter(ItemPackage.class, new ItemPackageInstanceCreator());
//            gson = gsonBuilder.create();
            gson = new Gson();
        }
        return gson;
    }

    private DataStoreUtil(Context _context) {
        this.context = _context;
        preferences = context.getSharedPreferences("default", Context.MODE_PRIVATE);
    }


    public List<ItemPackage> getPackages(){
        List<ItemPackage> itemPackageList = new ArrayList<>();
        try {
            String data_json = preferences.getString("data", "N/A");
            if(!data_json.equals("N/A")){
                Log.d(TAG, "getPackages: -- " + data_json);
                Gson gson = getGsonInstance();
                Type type = new TypeToken<List<ItemPackage>>(){}.getType();
                itemPackageList = gson.fromJson(data_json, type);
            }
            return itemPackageList;
        }
        catch (Exception ex){
            ex.printStackTrace(System.out);
            return itemPackageList;
        }
    }

    public boolean setPackages(List<ItemPackage> itemPackageList){
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = getGsonInstance();
        Type type = new TypeToken<List<ItemPackage>>() {}.getType();
        editor.putString("data", gson.toJson(itemPackageList, type));
        Log.d(TAG, "setData: "+gson.toJson(itemPackageList, type));
        Log.d(TAG, "setDataStatus: " + editor.commit());
        return true;
    }

    public boolean setData(String key, String data){
        SharedPreferences.Editor editor = preferences.edit();
        Log.d(TAG, "setData: "+ key +" : " + data);
        editor.putString(key, data);
        Log.d(TAG, "setDataStatus: " + editor.commit());
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
        editor.apply();
        return true;
    }

    public String getNotifyChannel(){
        String data_json = preferences.getString("notify_list", "N/A");
        Log.d(TAG, "getNotifyChannel: -- " + data_json);
        return data_json;
    }

}
