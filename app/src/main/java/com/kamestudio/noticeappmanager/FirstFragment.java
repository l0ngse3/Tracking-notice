package com.kamestudio.noticeappmanager;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kamestudio.noticeappmanager.adapter.PackageSetupAdapter;
import com.kamestudio.noticeappmanager.data.DataStoreUtil;
import com.kamestudio.noticeappmanager.databinding.FragmentFirstBinding;
import com.kamestudio.noticeappmanager.enity.ItemPackage;
import com.kamestudio.noticeappmanager.service.MessageEvent;
import com.kamestudio.noticeappmanager.service.NoticeService;
import com.kamestudio.noticeappmanager.viewmodel.NotificationViewModel;

import org.greenrobot.eventbus.EventBus;

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
        startNoticeService();
    }

    private void bindingViewFunction(){
        //start forceground service
        binding.buttonStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNoticeService();
            }
        });

        binding.buttonStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Toast.makeText(getContext(), "Stop Service", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new MessageEvent(NoticeService.ACTION_STOP_SERVICE));
                    Log.d(TAG, "onClick: buttonStopService ");
                }
                catch (Exception ex){
                    Log.e(TAG, "onClick: buttonStopService" + ex.toString());
                }
            }
        });

        binding.buttonSettingNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Stop Notify", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(((MainActivity) getActivity()).ACTION_NOTIFICATION_LISTENER_SETTINGS));
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

    private void startNoticeService(){
        Intent intent = new Intent(getActivity(), NoticeService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(intent);
            Toast.makeText(getContext(), "Start Service 1", Toast.LENGTH_SHORT).show();
        }
        else{
            getActivity().startService(intent);
            Toast.makeText(getContext(), "Start Service 2", Toast.LENGTH_SHORT).show();
        }
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