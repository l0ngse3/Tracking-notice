package com.example.noticeappmanager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.noticeappmanager.adapter.PackageSetupAdapter;
import com.example.noticeappmanager.data.DataStoreUtil;
import com.example.noticeappmanager.databinding.FragmentFirstBinding;
import com.example.noticeappmanager.enity.ItemPackage;
import com.example.noticeappmanager.service.NoticeService;
import com.example.noticeappmanager.viewmodel.NotificationViewModel;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment implements Util{

    private FragmentFirstBinding binding;
    private NotificationViewModel viewModel = null;
    private PackageSetupAdapter adapter;
    private FirstFragment fragment;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragment = this;
        List<ItemPackage> list = DataStoreUtil.getInstance(requireActivity()).getPackages();
        viewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);
        //observable data changing from other fragment
        viewModel.getListMutableLiveData().observe(getViewLifecycleOwner(), itemPackages -> {
            Log.d(TAG, "bindingViewFunction -- observe: updated");
            if(viewModel != null)
                updateUI(itemPackages);
        });

        viewModel.setListMutableLiveData(list);
        Log.d(TAG, "onViewCreated: DataStore" + list);
        Log.d(TAG, "onViewCreated: " + viewModel.getListMutableLiveData().getValue());
        bindingViewFunction();
    }

    private void bindingViewFunction(){
        //pushNotificationOnSubChannel("start subchannel");

        Intent intent = new Intent(getActivity(), NoticeService.class);
        //start forceground service
        binding.buttonStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setAction(NoticeService.ACTION_START);
                if(!isServiceRunning(getContext(), NoticeService.class)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        getActivity().startForegroundService(intent);
                    }
                    Toast.makeText(getContext(), "Service not running", Toast.LENGTH_SHORT).show();
                }
                else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        getActivity().startForegroundService(intent);
                    }
                    Toast.makeText(getContext(), "Service running", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.buttonStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Toast.makeText(getContext(), "Stop Service", Toast.LENGTH_SHORT).show();
                    Intent my_intent = new Intent(NoticeService.APP_PACKAGE_NAME);
                    my_intent.putExtra("my_action", NoticeService.ACTION_STOP_SERVICE);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(my_intent);
                    Log.d(TAG, "onClick: buttonStopService -- intent " + my_intent.toString());
                }
                catch (Exception ex){
                    Log.e(TAG, "onClick: buttonStopService" + ex.toString());
                }
            }
        });

        binding.buttonStopNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Stop Notify", Toast.LENGTH_SHORT).show();
                Intent my_intent = new Intent(NoticeService.APP_PACKAGE_NAME);
                my_intent.putExtra("my_action", NoticeService.ACTION_STOP_SOUND);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(my_intent);
                Log.d(TAG, "onClick: buttonStopNotify -- intent " + my_intent.toString());
            }
        });

        //create view of item package
        adapter = new PackageSetupAdapter(this);
        adapter.setItemPackageList(viewModel.getListMutableLiveData().getValue());
        Log.d(TAG, "bindingViewFunction: "+viewModel.getListMutableLiveData().getValue());
        binding.packageRecyclerView.setAdapter(adapter);
        binding.packageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void updateUI(List<ItemPackage> itemPackages){
        String data = "";
        if(itemPackages != null && itemPackages.size() != 0) {
            for (ItemPackage item : itemPackages) {
                data += item.getPackageInfo().packageName + "\n";
            }
        }
        else{
            itemPackages = new ArrayList<>();
        }

        adapter.setItemPackageList(itemPackages);
        adapter.notifyDataSetChanged();
        DataStoreUtil.getInstance(this.requireContext()).setPackages(viewModel.getListMutableLiveData().getValue());
        Log.d("Notify App", "updateUI: "+data);
    }

    @Override
    public void onDestroyView() {
//        DataStoreUtil.getInstance(this.requireContext()).setPackages(viewModel.getListMutableLiveData().getValue());
        super.onDestroyView();
        binding = null;
    }

    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}