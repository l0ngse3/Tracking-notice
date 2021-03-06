package com.kamestudio.noticeappmanager.adapter;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.kamestudio.noticeappmanager.R;
import com.kamestudio.noticeappmanager.databinding.CardViewShowPackageAndSetupLayoutBinding;
import com.kamestudio.noticeappmanager.enity.ItemPackage;

public class SetupViewHolder extends RecyclerView.ViewHolder {

    private CardViewShowPackageAndSetupLayoutBinding binding;

    public static SetupViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_view_show_package_and_setup_layout, parent, false);
        CardViewShowPackageAndSetupLayoutBinding _binding = CardViewShowPackageAndSetupLayoutBinding.bind(view);
        return new SetupViewHolder(_binding);
    }

    private SetupViewHolder(CardViewShowPackageAndSetupLayoutBinding _binding) {
        super(_binding.getRoot());
        binding = _binding;
    }

    public void bind(ItemPackage itemPackage, View.OnClickListener onClick) {
        PackageInfo packageInfo = itemPackage.getPackageInfo();
        Drawable icon = packageInfo.applicationInfo.loadIcon(binding.getRoot().getContext().getPackageManager());
        binding.packageImageView.setImageDrawable(icon);
        binding.packageTextView.setText(packageInfo.packageName);

        if (itemPackage.getSoundPath() != null) {
            if (itemPackage.getSoundPath().length() > 0) {
                String[] soundNameArr = itemPackage.getSoundPath().split("/");
                String soundName = soundNameArr[soundNameArr.length - 1];
                binding.soundNameTextView.setText(soundName);
            } else {
                binding.soundNameTextView.setText("");
            }
        } else {
            binding.soundNameTextView.setText("");
        }
        binding.soundSwitch.setChecked(itemPackage.isTurnOn());
        binding.soundSwitch.setOnClickListener(onClick);
        binding.soundChangeImageView.setOnClickListener(onClick);
    }


}