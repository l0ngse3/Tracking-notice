package com.kamestudio.noticeappmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.kamestudio.noticeappmanager.data.DataStoreUtil;
import com.kamestudio.noticeappmanager.databinding.ActivityMainBinding;
import com.kamestudio.noticeappmanager.enity.ItemPackage;
import com.kamestudio.noticeappmanager.fragment.PopUpDialogFragment;
import com.kamestudio.noticeappmanager.viewmodel.NotificationViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity implements Util{

    private static final String TAG = "MainActivity";
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE_IN_ACTIVITY = 200120;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 200121;
    private static final int READ_MEDIA_AUDIO_CODE = 2001211;

    private AlertDialog enableNotificationListenerAlertDialog;

    private ActivityMainBinding binding;

    private ActivityResultLauncher<Intent> mGetSound;

    private NotificationViewModel viewModel;
    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //binding view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setup ViewModel
        viewModelSetup();
        //set on click
        binding.fab.setOnClickListener(view -> showDialog());
        permissionGranting();
        //register
        prepareRegisterForActivityResult();
    }


    void viewModelSetup(){
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        viewModel.getListMutableLiveData().setValue(DataStoreUtil.getInstance(MainActivity.this).getPackages());
        viewModel.getCurrentPosition().observe(this, integer -> {
            Log.d(TAG, "onChanged: value updated "+integer);
            currentPosition = integer;
            if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE_IN_ACTIVITY);
                    }
                }
                else{
                    if (checkPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, READ_MEDIA_AUDIO_CODE);
                    }
                }
            }
            getSoundPath();
        });
        viewModel.getListMutableLiveData().observe(this, itemPackages -> DataStoreUtil.getInstance(MainActivity.this).setPackages(itemPackages));
    }

    void permissionGranting(){
        if(!isNotificationServiceEnabled()){
            //Show a dialog alert user grant permission for Notification listener
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Need turn on notification settings");
            alertDialogBuilder.setMessage("Turn it on, please!");
            alertDialogBuilder.setPositiveButton("Yes",
                    (dialog, id) -> startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)));
            alertDialogBuilder.setNegativeButton("No",
                (dialog, id) -> {
                    // If you choose to not enable the notification listener
                    // the app. will not work as expected
                });
            enableNotificationListenerAlertDialog = alertDialogBuilder.create();
            enableNotificationListenerAlertDialog.show();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2) {
            if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE_IN_ACTIVITY);
            }
        }
        else{
            Log.d(TAG, "Request permission: READ_MEDIA_AUDIO, POST_NOTIFICATIONS");
            if (checkPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_DENIED ||
                    checkPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS}, READ_MEDIA_AUDIO_CODE);
            }
        }
    }

    void showDialog(){
        DialogFragment dialogFragment = PopUpDialogFragment.newInstance(123);
        dialogFragment.showNow(getSupportFragmentManager(), "My fragment");
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: "+viewModel);
        DataStoreUtil.getInstance(this).setPackages(viewModel.getListMutableLiveData().getValue());
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: "+viewModel);
        DataStoreUtil.getInstance(this).setPackages(viewModel.getListMutableLiveData().getValue());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        DataStoreUtil.getInstance(this).setPackages(viewModel.getListMutableLiveData().getValue());
        super.onDestroy();
    }

    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case READ_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSoundPath();
                }
                else {
                    Log.d(TAG, "READ_EXTERNAL_STORAGE Permission Denied");
//                    Toast.makeText(MainActivity.this, "READ_EXTERNAL_STORAGE Permission Denied", Toast.LENGTH_SHORT) .show();
                }
                break;

            case READ_EXTERNAL_STORAGE_REQUEST_CODE_IN_ACTIVITY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "READ_EXTERNAL_STORAGE Granted");
//                    Toast.makeText(MainActivity.this, "READ_EXTERNAL_STORAGE Granted", Toast.LENGTH_SHORT) .show();
                }
                else {
                    Log.d(TAG, "READ_EXTERNAL_STORAGE Permission Denied");
//                    Toast.makeText(MainActivity.this, "READ_EXTERNAL_STORAGE Permission Denied", Toast.LENGTH_SHORT) .show();
                }
                break;

            case READ_MEDIA_AUDIO_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "READ_MEDIA_AUDIO Granted");
//                    Toast.makeText(MainActivity.this, "READ_MEDIA_AUDIO Granted", Toast.LENGTH_SHORT) .show();
                }
                else {
                    Log.d(TAG, "READ_MEDIA_AUDIO Permission Denied");
//                    Toast.makeText(MainActivity.this, "READ_MEDIA_AUDIO Permission Denied", Toast.LENGTH_SHORT) .show();
                }

                if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "POST_NOTIFICATIONS Granted");
//                    Toast.makeText(MainActivity.this, "POST_NOTIFICATIONS Granted", Toast.LENGTH_SHORT) .show();
                }
                else {
                    Log.d(TAG, "POST_NOTIFICATIONS Permission Denied");
//                    Toast.makeText(MainActivity.this, "POST_NOTIFICATIONS Permission Denied", Toast.LENGTH_SHORT) .show();
                }
                break;
        }
    }

    public int checkPermission(String permission)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            return PackageManager.PERMISSION_DENIED;
        }
        else {
            return PackageManager.PERMISSION_GRANTED;
        }
    }

    void prepareRegisterForActivityResult(){
        //prepare callback user for later
        CallBack onDataReceive = (data, position) -> {
            Log.d(TAG, "onReceive: data -- " + data.getPath());
            String soundPath = Util.getRealPathFromURI(MainActivity.this, data);
            Log.d(TAG, "onReceive: data URI -- " + soundPath);
            viewModel.getListMutableLiveData().getValue().get(position).setSoundPath(soundPath);
            List<ItemPackage> list = viewModel.getListMutableLiveData().getValue();
            viewModel.setListMutableLiveData(list);
        };

        //register to get uri path after got audio file
        mGetSound = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    Uri uri = data.getData();
                    int position = currentPosition;
                    onDataReceive.onReceive(uri, position);
                }
            });
    }

    public void getSoundPath(){
        Intent soundPicker = new Intent(Intent.ACTION_GET_CONTENT);
        soundPicker.setType("audio/*");
        mGetSound.launch(soundPicker);
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
        }
        return result;
    }
}