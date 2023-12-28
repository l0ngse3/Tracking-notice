package com.kamestudio.noticeappmanager;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import com.google.android.gms.ads.RequestConfiguration;
import com.kamestudio.noticeappmanager.adapter.PackageSetupAdapter;
import com.kamestudio.noticeappmanager.ads.GoogleMobileAdsConsentManager;
import com.kamestudio.noticeappmanager.data.DataStoreUtil;
import com.kamestudio.noticeappmanager.databinding.FragmentFirstBinding;
import com.kamestudio.noticeappmanager.enity.ItemPackage;
import com.kamestudio.noticeappmanager.service.MessageEvent;
import com.kamestudio.noticeappmanager.service.NoticeService;
import com.kamestudio.noticeappmanager.viewmodel.NotificationViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FirstFragment extends Fragment implements Util{
    private static final String TAG = "FirstFragment";
    private FragmentFirstBinding binding;
    private NotificationViewModel viewModel = null;
    private PackageSetupAdapter adapter;


    // Ads config
    private static String AD_UNIT_ID = "";
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(true);
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        AD_UNIT_ID = getResources().getString(R.string.AD_UNIT_ID_TEST);
        adsConfigSettings();
        return binding.getRoot();
    }



    private void adsConfigSettings() {
        adContainerView = binding.adContainer;
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this.getContext());

        googleMobileAdsConsentManager.gatherConsent(
                requireActivity(),
                consentError -> {
                    if (consentError != null) {
                        // Consent not obtained in current session.
                        Log.w(
                                TAG,
                                String.format("%s: %s", consentError.getErrorCode(), consentError.getMessage()));
                    }

                    if (googleMobileAdsConsentManager.canRequestAds()) {
                        initializeMobileAdsSdk();
                    }

                    if (googleMobileAdsConsentManager.isPrivacyOptionsRequired()) {
                        // Regenerate the options menu to include a privacy setting.
                        requireActivity().invalidateOptionsMenu();
                    }
                });
        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk();
        }

        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        adContainerView
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        () -> {
                            if (!initialLayoutComplete.getAndSet(true)
                                    && googleMobileAdsConsentManager.canRequestAds()) {
                                loadBanner();
                            }
                        });

        // Set your test devices. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device."
        MobileAds.setRequestConfiguration(
                //test devices
//                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("446B401A8B1764C39A87843B3BD55F2D")).build());
                // production devices
                new RequestConfiguration.Builder().build());
    }

    private void loadBanner() {
        // Create a new ad view.
        adView = new AdView(this.getContext());
        adView.setAdUnitId(AD_UNIT_ID);
        adView.setAdSize(getAdSize());
//        adView.setAdSize(new AdSize(300, 50));

        // Replace ad container with new ad view.
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        // Start loading the ad in the background.
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(
                this.getContext(),
                initializationStatus -> {});

        // Load an ad.
        if (initialLayoutComplete.get()) {
            loadBanner();
        }
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = requireActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();
        Log.d(TAG, "adContainerView -- width: " + adWidthPixels);

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this.getContext(), adWidth);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<ItemPackage> list = DataStoreUtil.getInstance(getActivity()).getPackages();
        viewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);
        //observable data changing from other fragment
        viewModel.getListMutableLiveData().observe(getViewLifecycleOwner(), itemPackages -> {
            Log.d(TAG, "bindingViewFunction -- observe: updated");
            if(viewModel != null)
                updateUI(itemPackages);
        });

        viewModel.getListMutableLiveData().setValue(list);
        Log.d(TAG, "onViewCreated: DataStore " + list);
        Log.d(TAG, "onViewCreated: " + viewModel.getListMutableLiveData().getValue());
        bindingViewFunction();
        Boolean is_running = Boolean.parseBoolean(DataStoreUtil.
                getInstance(getActivity()).
                getData(NoticeService.IS_RUNNING_STATE_NAME));
        if (is_running){
            startNoticeService();
        }
    }

    private void bindingViewFunction(){
        //start foreground service
        binding.buttonStartService.setOnClickListener(view -> startNoticeService());

        binding.buttonStopService.setOnClickListener(view -> {
            try {
                Toast.makeText(getContext(), "Stop Service", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), NoticeService.class);
                intent.setAction(FOREGROUND_STOP_ACTION);
                getActivity().startService(intent);
                EventBus.getDefault().post(new MessageEvent(NoticeService.ACTION_STOP_SERVICE));
                Log.d(TAG, "onClick: buttonStopService ");
                DataStoreUtil.getInstance(getActivity()).setData(NoticeService.IS_RUNNING_STATE_NAME, false+"");
            }
            catch (Exception ex){
                Log.e(TAG, "onClick: buttonStopService" + ex);
            }
        });

        binding.buttonSettingNotify.setOnClickListener(view -> {
//            Toast.makeText(getContext(), "Stop Notify", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(((MainActivity) getActivity()).ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });

        //create view of item package
        adapter = new PackageSetupAdapter(this);
        adapter.setItemPackageList(viewModel.getListMutableLiveData().getValue());
        Log.d(TAG, "bindingViewFunction: "+viewModel.getListMutableLiveData().getValue());
        binding.packageRecyclerView.setAdapter(adapter);
        binding.packageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void updateUI(List<ItemPackage> itemPackages){
        StringBuilder data = new StringBuilder();
        if(itemPackages != null && itemPackages.size() != 0) {
            for (ItemPackage item : itemPackages) {
                data.append(item.getPackageInfoName()).append("\n");
            }
        }
        else{
            itemPackages = new ArrayList<>();
        }

        adapter.setItemPackageList(itemPackages);
        adapter.notifyDataSetChanged();
        DataStoreUtil.getInstance(this.requireContext()).setPackages(viewModel.getListMutableLiveData().getValue());
        Log.d(TAG, "updateUI: "+data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.resume();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private void startNoticeService(){
        Intent intent = new Intent(getActivity(), NoticeService.class);
        intent.setAction(FOREGROUND_START_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(intent);
//            Toast.makeText(getContext(), "Start Service 1", Toast.LENGTH_SHORT).show();
        }
        else{
            getActivity().startService(intent);
//            Toast.makeText(getContext(), "Start Service 2", Toast.LENGTH_SHORT).show();
        }
        DataStoreUtil.getInstance(getActivity()).setData(NoticeService.IS_RUNNING_STATE_NAME, true+"");
    }

}