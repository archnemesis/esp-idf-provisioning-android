// Copyright 2020 Espressif Systems (Shanghai) PTE LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.espressif.ui.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.espressif.AppConstants;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.ui.database.GlowsignDatabase;
import com.espressif.ui.database.GlowsignDevice;
import com.espressif.ui.database.GlowsignDeviceAdapter;
import com.espressif.ui.database.GlowsignDeviceViewModel;
import com.espressif.wifi_provisioning.BuildConfig;
import com.espressif.wifi_provisioning.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class EspMainActivity extends AppCompatActivity {

    private static final String TAG = EspMainActivity.class.getSimpleName();

    // Request codes
    private static final int REQUEST_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private ESPProvisionManager provisionManager;
    private FloatingActionButton btnAddDevice;
    private ImageView ivEsp;
    private SharedPreferences sharedPreferences;
    private String deviceType;
    private RecyclerView deviceListView;
    private GlowsignDeviceViewModel viewModel;
    private ActivityResultLauncher<Intent> deviceDiscoverLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esp_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initViews();

        sharedPreferences = getSharedPreferences(AppConstants.ESP_PREFERENCES, Context.MODE_PRIVATE);
        provisionManager = ESPProvisionManager.getInstance(getApplicationContext());

        deviceListView = findViewById(R.id.saved_device_list);
        deviceListView.setLayoutManager(new LinearLayoutManager(this));
        deviceListView.setHasFixedSize(true);
        final GlowsignDeviceAdapter adapter = new GlowsignDeviceAdapter();
        deviceListView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(GlowsignDeviceViewModel.class);
        viewModel.getAllDevices().observe(this, new Observer<List<GlowsignDevice>>() {
            @Override
            public void onChanged(List<GlowsignDevice> glowsignDevices) {
                adapter.submitList(glowsignDevices);
            }
        });

        deviceDiscoverLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.i(TAG, "Finished with the DeviceDiscoverActivity");
                    }
                }
        );

        adapter.setOnItemClickListener(new GlowsignDeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GlowsignDevice device) {
                Intent intent = new Intent(EspMainActivity.this, DeviceDiscoverActivity.class);
                intent.putExtra(DeviceDiscoverActivity.EXTRA_DEVICE_NAME, device.name);
                deviceDiscoverLauncher.launch(intent);
            }
        });

        GlowsignDatabase db = Room.databaseBuilder(getApplicationContext(),
                GlowsignDatabase.class, "glowsign-data").build();

        db.glowsignDeviceDao().getAll().observe(EspMainActivity.this, new Observer<List<GlowsignDevice>>() {
            @Override
            public void onChanged(List<GlowsignDevice> glowsignDevices) {
                Log.i(TAG, String.format("Got %d items", glowsignDevices.size()));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ivEsp.setImageResource(R.drawable.ic_esp_ble);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (BuildConfig.isSettingsAllowed) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_settings, menu);
            return true;
        } else {
            menu.clear();
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOCATION) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                if (isLocationEnabled()) {
                    addDeviceClick();
                }
            }
        }

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth is turned ON, you can provision device now.", Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {

        ivEsp = findViewById(R.id.iv_esp);
        btnAddDevice = findViewById(R.id.addDeviceButton);
        btnAddDevice.setOnClickListener(addDeviceBtnClickListener);
        deviceListView = findViewById(R.id.saved_device_list);

        TextView tvAppVersion = findViewById(R.id.tv_app_version);

        String version = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appVersion = getString(R.string.app_version) + " - v" + version;
        tvAppVersion.setText(appVersion);
    }

    View.OnClickListener addDeviceBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                if (!isLocationEnabled()) {
                    askForLocation();
                    return;
                }
            }
            addDeviceClick();
        }
    };

    private void addDeviceClick() {

        if (BuildConfig.isQrCodeSupported) {

            gotoQrCodeActivity();

        } else {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bleAdapter = bluetoothManager.getAdapter();

            if (!bleAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                startProvisioningFlow();
            }
        }
    }

    private void startProvisioningFlow() {
        provisionManager.createESPDevice(
                ESPConstants.TransportType.TRANSPORT_BLE,
                ESPConstants.SecurityType.SECURITY_1);
        goToBLEProvisionLandingActivity();
    }

    private void askForLocation() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setMessage(R.string.dialog_msg_gps);

        // Set up the buttons
        builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION);
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private boolean isLocationEnabled() {

        boolean gps_enabled = false;
        boolean network_enabled = false;
        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Activity.LOCATION_SERVICE);

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {
        }

        Log.d(TAG, "GPS Enabled : " + gps_enabled + " , Network Enabled : " + network_enabled);

        return gps_enabled || network_enabled;
    }

    private void gotoQrCodeActivity() {
        Intent intent = new Intent(EspMainActivity.this, AddDeviceActivity.class);
        startActivity(intent);
    }

    private void goToBLEProvisionLandingActivity() {

        Intent intent = new Intent(EspMainActivity.this, BLEProvisionLanding.class);
        intent.putExtra(AppConstants.KEY_SECURITY_TYPE, 1);
        startActivity(intent);
    }
}
