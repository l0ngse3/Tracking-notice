package com.kamestudio.noticeappmanager.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kamestudio.noticeappmanager.enity.ItemPackage;

import java.util.ArrayList;
import java.util.List;

public class NotificationViewModel extends ViewModel {
    private MutableLiveData<List<ItemPackage>> listMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> currentPosition = new MutableLiveData<>();
    private MutableLiveData<String> manual_stop = new MutableLiveData<>();

    public MutableLiveData<String> getManual_stop() {
        return manual_stop;
    }

    public void setManual_stop(MutableLiveData<String> manual_stop) {
        this.manual_stop = manual_stop;
    }

    public MutableLiveData<Integer> getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Integer currentPosition) {
        this.currentPosition.postValue(currentPosition);
    }

    public void addItem(ItemPackage item) {
        listMutableLiveData.getValue().add(item);

    }

    public MutableLiveData<List<ItemPackage>> getListMutableLiveData() {
        if (listMutableLiveData.getValue() == null) {
            listMutableLiveData.postValue(new ArrayList<>());
        }
        return listMutableLiveData;
    }

    public void setListMutableLiveData(List<ItemPackage> listIem) {
        this.listMutableLiveData.postValue(listIem);
        this.listMutableLiveData.setValue(listIem);
    }
}
