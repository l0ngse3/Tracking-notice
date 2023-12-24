package com.kamestudio.noticeappmanager;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.kamestudio.noticeappmanager.enity.ItemPackage;


public class BindingUtils {

    @BindingAdapter("app:setPackageImage")
    public static void setPackageImage(ImageView view, ItemPackage item) {
        String packageInfoName = item.getPackageInfoName();
        Drawable icon = null;
        try {
            icon = view.getContext().getPackageManager().getApplicationIcon(packageInfoName);
            view.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @BindingAdapter("android:checked")
    public static void setChecked(CheckBox view, ItemPackage itemPackage){
        view.setChecked(itemPackage.isChosen());
    }

    @BindingAdapter("android:text")
    public static void setText(TextView view, ItemPackage itemPackage){
        view.setText(itemPackage.getPackageInfoName());
    }

}