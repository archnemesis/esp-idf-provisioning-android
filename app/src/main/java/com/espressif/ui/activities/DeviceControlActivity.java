package com.espressif.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.espressif.ui.views.ColorChooser;
import com.espressif.wifi_provisioning.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceControlActivity extends AppCompatActivity {
    private static final String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRA_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRA_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String deviceName;
    private String deviceAddress;
    private ColorChooser colorChooser1;
    private ColorChooser colorChooser2;
    private ColorChooser colorChooser3;
    private ActivityResultLauncher<Intent> colorPickerLauncher;
    private int color1 = 0xFF0000;
    private int color2 = 0x00FF00;
    private int color3 = 0x0000FF;
    private int requestedColor;
    private Spinner presetSpinner;
    private boolean userInteractionOnSpinner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);

        colorChooser1 = findViewById(R.id.color_chooser_1);
        colorChooser1.setColor(0xFF000000 | color1);
        colorChooser1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked on the color chooser!");
                requestedColor = 1;
                Intent intent = new Intent(DeviceControlActivity.this, ColorPickerActivity.class);
                colorPickerLauncher.launch(intent);
            }
        });

        colorChooser2 = findViewById(R.id.color_chooser_2);
        colorChooser2.setColor(0xFF000000 | color2);
        colorChooser2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked on the color chooser!");
                requestedColor = 2;
                Intent intent = new Intent(DeviceControlActivity.this, ColorPickerActivity.class);
                colorPickerLauncher.launch(intent);
            }
        });

        colorChooser3 = findViewById(R.id.color_chooser_3);
        colorChooser3.setColor(0xFF000000 | color3);
        colorChooser3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked on the color chooser!");
                requestedColor = 3;
                Intent intent = new Intent(DeviceControlActivity.this, ColorPickerActivity.class);
                colorPickerLauncher.launch(intent);
            }
        });

        presetSpinner = findViewById(R.id.preset_spinner);
        ;
        String[] items = new String[]{
                "Pentagram",
                "Solid Color",
                "Rainbow",
                "Scan",
                "Breathe"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        presetSpinner.setAdapter(adapter);
        presetSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                userInteractionOnSpinner = true;
                return false;
            }
        });
        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (userInteractionOnSpinner) {
                    userInteractionOnSpinner = false;
                    commandDevicePreset(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        colorPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.i(TAG, "ColorPickerActivity done");
                        Intent intent = result.getData();

                        switch (requestedColor) {
                            case 1:
                                color1 = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR, color1 & 0xFFFFFF);
                                colorChooser1.setColor(color1);
                                break;
                            case 2:
                                color2 = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR, color2 & 0xFFFFFF);
                                colorChooser2.setColor(color2);
                                break;
                            case 3:
                                color3 = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR, color3 & 0xFFFFFF);
                                colorChooser3.setColor(color3);
                                break;
                            default:
                                break;
                        }

                        commandDevicePreset(presetSpinner.getSelectedItemPosition());
                    }
                }
        );

        getDevicePreset();
    }

    private void getDevicePreset()
    {
        Request request = new Request.Builder()
                .url(String.format("http://%s/api/v1/preset", deviceAddress))
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(presetRequestCallback);
    }

    private void commandDevicePreset(int preset) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("preset", preset);
            jsonObject.put("color1", color1);
            jsonObject.put("color2", color2);
            jsonObject.put("color3", color3);
            jsonObject.put("speed", 100);
        }
        catch (JSONException jsonException) {
            return;
        }

        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                MediaType.get("application/json"));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(String.format("http://%s/api/v1/preset", deviceAddress))
                .post(body)
                .build();

        client.newCall(request).enqueue(commandRequestCallback);
    }

    final private Callback commandRequestCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {

        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            DeviceControlActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            DeviceControlActivity.this,
                            "Success!",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    final private Callback presetRequestCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {

        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (response.body() != null) {
                String responseData = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    int preset = jsonObject.getInt("preset");
                    int color1 = jsonObject.getInt("color1");
                    int color2 = jsonObject.getInt("color2");
                    int color3 = jsonObject.getInt("color3");

                    DeviceControlActivity.this.color1 = color1;
                    DeviceControlActivity.this.color2 = color2;
                    DeviceControlActivity.this.color3 = color3;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            colorChooser1.setColor(color1);
                            colorChooser2.setColor(color2);
                            colorChooser3.setColor(color3);
                            presetSpinner.setSelection(preset);
                            Toast.makeText(
                                    DeviceControlActivity.this,
                                    String.format("Connected to %s", deviceName),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException e) {
                    // todo: show some kind of error message
                    return;
                }
            }
        }
    };
}