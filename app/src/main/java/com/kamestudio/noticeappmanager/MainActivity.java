package com.kamestudio.noticeappmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.kamestudio.noticeappmanager.data.DataStoreUtil;
import com.kamestudio.noticeappmanager.databinding.ActivityMainBinding;
import com.kamestudio.noticeappmanager.enity.ItemPackage;
import com.kamestudio.noticeappmanager.fragment.PopUpDialogFragment;
import com.kamestudio.noticeappmanager.viewmodel.NotificationViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity implements Util{

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE_IN_ACTIVITY = 200120;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 200121;
    private static final int RECEIVE_BOOT_COMPLETED_REQUEST_CODE = 200122;
    private static final int FOREGROUND_SERVICE_REQUEST_CODE = 200123;

    private AlertDialog enableNotificationListenerAlertDialog;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private NotificationViewModel viewModel;
    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //load view model
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        //binding view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //setSupportActionBar(binding.toolbar);

        //setup navigation controller
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        //appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        //load data from share preferences
        //Log.d(TAG, "onCreate: "+DataStoreUtil.getInstance(MainActivity.this).getPackages());
        viewModel.getListMutableLiveData().postValue(DataStoreUtil.getInstance(MainActivity.this).getPackages());
        viewModel.getCurrentPosition().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                Log.d(TAG, "onChanged: value updated "+integer);
                currentPosition = integer;
                if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE);
                    }
                }
                else{
                    getSoundPath();
                }
            }
        });

        viewModel.getListMutableLiveData().observe(this, new Observer<List<ItemPackage>>() {
            @Override
            public void onChanged(List<ItemPackage> itemPackages) {
                DataStoreUtil.getInstance(MainActivity.this).setPackages(itemPackages);
            }
        });

        //set on click
        binding.fab.setOnClickListener(view -> {
            showDialog();
        });

        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE_IN_ACTIVITY);
            }
        }

        if (checkPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}, RECEIVE_BOOT_COMPLETED_REQUEST_CODE);
            }
        }

        if (checkPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.FOREGROUND_SERVICE}, FOREGROUND_SERVICE_REQUEST_CODE);
            }
        }
    }

    void showDialog(){
        DialogFragment dialogFragment = PopUpDialogFragment.newInstance(123);
        dialogFragment.showNow(getSupportFragmentManager(), "My fragment");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
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

    public AlertDialog buildNotificationServiceAlertDialog(){
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
        return(alertDialogBuilder.create());
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

    ActivityResultLauncher<Intent> mGetSound = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        int position = currentPosition;
                        onDataReceive.onReceive(uri, position);
                    }
                }
            });

    private CallBack onDataReceive = new CallBack() {
        @Override
        public void onReceive(Uri data, int position) {
            Log.d(TAG, "onReceive: data -- " + data.getPath());
            String soundPath = Util.getRealPathFromURI(MainActivity.this, data);
            Log.d(TAG, "onReceive: data URI -- " + soundPath);
            viewModel.getListMutableLiveData().getValue().get(position).setSoundPath(soundPath);
            List<ItemPackage> list = viewModel.getListMutableLiveData().getValue();
            viewModel.setListMutableLiveData(list);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case READ_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSoundPath();
                }
                else {
                    Toast.makeText(MainActivity.this, "READ_EXTERNAL_STORAGE Permission Denied", Toast.LENGTH_SHORT) .show();
                }
                break;

            case READ_EXTERNAL_STORAGE_REQUEST_CODE_IN_ACTIVITY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "READ_EXTERNAL_STORAGE Granted", Toast.LENGTH_SHORT) .show();
                }
                else {
                    Toast.makeText(MainActivity.this, "READ_EXTERNAL_STORAGE Permission Denied", Toast.LENGTH_SHORT) .show();
                }
                break;

            case RECEIVE_BOOT_COMPLETED_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "RECEIVE_BOOT_COMPLETED Granted", Toast.LENGTH_SHORT) .show();
                }
                else {
                    Toast.makeText(MainActivity.this, "RECEIVE_BOOT_COMPLETED Permission Denied", Toast.LENGTH_SHORT) .show();
                }
                break;

            case FOREGROUND_SERVICE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "FOREGROUND_SERVICE Granted", Toast.LENGTH_SHORT) .show();
                }
                else {
                    Toast.makeText(MainActivity.this, "FOREGROUND_SERVICE Permission Denied", Toast.LENGTH_SHORT) .show();
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