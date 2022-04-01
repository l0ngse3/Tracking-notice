package com.example.noticeappmanager.fragment;

import android.content.ClipData;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.collection.ArraySet;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.example.noticeappmanager.Util;
import com.example.noticeappmanager.adapter.PackageAdapter;
import com.example.noticeappmanager.data.DataStoreUtil;
import com.example.noticeappmanager.databinding.FragmentPopupBinding;
import com.example.noticeappmanager.enity.ItemPackage;
import com.example.noticeappmanager.viewmodel.NotificationViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PopUpDialogFragment extends DialogFragment implements Util {

    PackageAdapter packageAdapter = new PackageAdapter();
    List<ItemPackage> itemPackageFullList = new ArrayList<>();
    List<ItemPackage> itemPackageListView = new ArrayList<>();
    PopUpDialogFragment context = null;
    private FragmentPopupBinding binding = null;
    NotificationViewModel viewModel = null;
    private String queryText = "";

    public static PopUpDialogFragment newInstance(int title) {
        PopUpDialogFragment frag = new PopUpDialogFragment();
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queryText = "";
        viewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);
    }

    private void setOnClick() {
        if(binding != null){

            binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    //packageAdapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    packageAdapter.getFilter().filter(newText);
                    queryText = newText;
                    return false;
                }
            });

            binding.okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "okButton.setOnClickListener -- view model: " + viewModel.getListMutableLiveData().getValue());
                    if(viewModel.getListMutableLiveData().getValue() != null) {
                        List<ItemPackage> choosenList = new ArrayList<>();
                        List<ItemPackage> viewModelData = viewModel.getListMutableLiveData().getValue();

                        for (ItemPackage item : packageAdapter.getItemPackageList()) {
                            if(item.isChoosen()){
                                choosenList.add(item);
                            }
                        }

                        for (ItemPackage item : packageAdapter.getItemPackageFullList()) {
                            if(item.isChoosen() && !choosenList.contains(item)){
                                choosenList.add(item);
                            }
                        }

                        for(ItemPackage item : viewModelData){
                            for(ItemPackage item1 : choosenList){
                                if(item.getPackageInfo().packageName.equals(item1.getPackageInfo().packageName)){
                                    item1.setTurnOn(item.isTurnOn());
                                    item1.setSoundPath(item.getSoundPath());
                                }
                            }
                        }

                        viewModel.setListMutableLiveData(choosenList);
                    }
                    context.dismiss();
                }
            });

            binding.cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "cancelButton.setOnClickListener");
                    context.dismiss();
                }
            });
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.setCancelable(false);
        binding = FragmentPopupBinding.inflate(inflater, container, false);

        List<ItemPackage> choosenPackage = viewModel.getListMutableLiveData().getValue();
        List<PackageInfo> packageInfoList = getContext().getPackageManager().getInstalledPackages(0);

        for(PackageInfo item : packageInfoList){
            boolean isChoosen = false;
            for(ItemPackage itemPackage : choosenPackage){
                if(itemPackage.getPackageInfo().packageName.equals(item.packageName)){
                    isChoosen = true;
                }
            }
            itemPackageListView.add(new ItemPackage(item, isChoosen));
            itemPackageFullList.add(new ItemPackage(item, isChoosen));
        }
        packageAdapter.setPackageInfoArrayList(itemPackageFullList);

        binding.packageRecyclerView.setAdapter(packageAdapter);
        binding.packageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        context = this;
        setOnClick();

        return binding.getRoot();
    }
}
