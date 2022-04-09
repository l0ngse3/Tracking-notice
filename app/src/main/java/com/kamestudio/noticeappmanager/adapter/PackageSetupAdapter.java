package com.kamestudio.noticeappmanager.adapter;


import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.kamestudio.noticeappmanager.FirstFragment;
import com.kamestudio.noticeappmanager.R;
import com.kamestudio.noticeappmanager.Util;
import com.kamestudio.noticeappmanager.enity.ItemPackage;
import com.kamestudio.noticeappmanager.service.MessageEvent;
import com.kamestudio.noticeappmanager.service.NoticeService;
import com.kamestudio.noticeappmanager.viewmodel.NotificationViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class PackageSetupAdapter extends ListAdapter<ItemPackage, SetupViewHolder> implements Util {

    private List<ItemPackage> itemPackageList;
    private FirstFragment mFragment;
    private int currentPosition = 0;
    private NotificationViewModel viewModel;

    public static final DiffUtil.ItemCallback<ItemPackage> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ItemPackage>() {
                @Override
                public boolean areItemsTheSame(ItemPackage oldItem, ItemPackage newItem) {
                    return oldItem.equals(newItem);
                }
                @Override
                public boolean areContentsTheSame(ItemPackage oldItem, ItemPackage newItem) {
                    return false;
                }
            };

    public PackageSetupAdapter(FirstFragment fragment) {
        super(DIFF_CALLBACK);
        this.mFragment = fragment;
    }

    public List<ItemPackage> getItemPackageList() {
        return itemPackageList;
    }

    public void setItemPackageList(List<ItemPackage> itemPackageList) {
        this.itemPackageList = itemPackageList;
        submitList(itemPackageList);
    }

    @NonNull
    @Override
    public SetupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        viewModel = new ViewModelProvider(mFragment.requireActivity()).get(NotificationViewModel.class);
        return SetupViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull SetupViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if(position+1 > itemPackageList.size())
            return;
        ItemPackage itemPackage = itemPackageList.get(position);
        if(itemPackage != null) {
            holder.bind(itemPackage, view -> {
                        switch (view.getId()) {
                            case R.id.soundChangeImageView:
                                currentPosition = holder.getAdapterPosition();
                                viewModel.setCurrentPosition(currentPosition);
                                Log.d("Notify App", "onClick: Select content");
                                Toast.makeText(mFragment.getContext(), "Select content", Toast.LENGTH_LONG).show();
                                break;

                            case R.id.soundSwitch:
                                ItemPackage item = viewModel.getListMutableLiveData().getValue().get(position);
                                Switch switchView = (Switch) view;
                                List<ItemPackage> list = viewModel.getListMutableLiveData().getValue();
                                list.get(position).setTurnOn(!list.get(position).isTurnOn());

                                if (item.getSoundPath().length() > 0 && !item.getSoundPath().equals("default")) {
                                    if(!switchView.isChecked()) {
                                        EventBus.getDefault().post(new MessageEvent(NoticeService.ACTION_STOP_SOUND));
                                    }
                                } else {
                                    list.get(position).setSoundPath("default");
                                    if(!switchView.isChecked()) {
                                        EventBus.getDefault().post(new MessageEvent(NoticeService.ACTION_STOP_SOUND));
                                    }
//                                    Switch switchView = (Switch) view;
//                                    switchView.setChecked(false);
//                                    Toast.makeText(mFragment.getContext(), "Please change sound", Toast.LENGTH_LONG).show();
                                }
                                viewModel.setListMutableLiveData(list);
                                Log.d("Notify App", "onClick -- soundSwitch");
                                break;
                        }
                    });
        }
    }

}
