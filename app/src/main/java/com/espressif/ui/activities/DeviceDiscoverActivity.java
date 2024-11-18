package com.espressif.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.espressif.wifi_provisioning.R;

import java.net.InetAddress;
import java.util.List;

public class DeviceDiscoverActivity extends AppCompatActivity {
    private static final String TAG = DeviceDiscoverActivity.class.getSimpleName();
    private static final String SERVICE_TYPE = "_http._tcp";
    private static final int NUM_RETRIES = 3;

    public static final String EXTRA_DEVICE_NAME = "DEVICE_NAME";

    private ActivityResultLauncher<Intent> deviceControlLauncher;
    private Handler timerHandler = new Handler();
    private NsdManager nsdManager;
    private String deviceName;
    private boolean deviceFound;
    private int retries = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_discover);
        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME);

        startServiceDiscovery();

        deviceControlLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        finish();
                    }
                }
        );
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            stopServiceDiscovery();
        }
    };

    private final NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String s, int i) {
            Log.i(TAG, "Discovery start failed");
        }

        @Override
        public void onStopDiscoveryFailed(String s, int i) {
            Log.i(TAG, "Discovery stop failed");

        }

        @Override
        public void onDiscoveryStarted(String s) {
            Log.i(TAG, "Discovery started");
            timerHandler.postDelayed(timerRunnable, 5000);
        }

        @Override
        public void onDiscoveryStopped(String s) {
            Log.i(TAG, "Discovery stopped");
            if (!deviceFound) {
                Log.i(TAG, "Device not found!");
                if (retries < NUM_RETRIES) {
                    retries++;
                    startServiceDiscovery();
                }
            }
        }

        @Override
        public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
            Log.i(TAG, String.format("Found service: %s", nsdServiceInfo.getServiceName()));

            boolean found = false;
            if (nsdServiceInfo.getServiceName().equals(deviceName)) {
                Log.i(TAG, String.format("Found service %s, resolving...",
                        nsdServiceInfo.getServiceName()));
                nsdManager.resolveService(nsdServiceInfo, resolveListener);
                found = true;
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
            Log.i(TAG, String.format("Lost service: %s", nsdServiceInfo.getServiceName()));
        }
    };

    private final NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {

        }

        @Override
        public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
            Log.i(TAG, String.format("Service %s resolved to %s",
                    nsdServiceInfo.getServiceName(),
                    nsdServiceInfo.getHost().getHostAddress()));
            deviceFound = true;
            timerHandler.removeCallbacks(timerRunnable);
            stopServiceDiscovery();

            Intent intent = new Intent(DeviceDiscoverActivity.this, DeviceControlActivity.class);
            intent.putExtra(DeviceControlActivity.EXTRA_DEVICE_NAME, deviceName);
            intent.putExtra(DeviceControlActivity.EXTRA_DEVICE_ADDRESS, nsdServiceInfo.getHost().getHostAddress());
            deviceControlLauncher.launch(intent);
        }
    };

    private void startServiceDiscovery() {
        nsdManager.discoverServices(
                SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener
        );
    }

    private void stopServiceDiscovery() {
        nsdManager.stopServiceDiscovery(discoveryListener);
    }
}
