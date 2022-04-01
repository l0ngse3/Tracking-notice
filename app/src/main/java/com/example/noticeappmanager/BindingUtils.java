package com.example.noticeappmanager;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.example.noticeappmanager.enity.ItemPackage;


public class BindingUtils {

    @BindingAdapter("app:setPackageImage")
    public static void setPackageImage(ImageView view, ItemPackage item) {
        PackageInfo packageInfo = item.getPackageInfo();
        Drawable icon = packageInfo.applicationInfo.loadIcon(view.getContext().getPackageManager());
        view.setImageDrawable(icon);
    }

    @BindingAdapter("android:checked")
    public static void setChecked(CheckBox view, ItemPackage itemPackage){
        view.setChecked(itemPackage.isChoosen());
    }

    @BindingAdapter("android:text")
    public static void setText(TextView view, ItemPackage itemPackage){
        view.setText(itemPackage.getPackageInfo().packageName);
    }

}