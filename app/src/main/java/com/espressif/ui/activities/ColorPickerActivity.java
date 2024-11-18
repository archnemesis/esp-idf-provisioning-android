package com.espressif.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.espressif.wifi_provisioning.R;
import com.skydoves.colorpickerview.ColorPickerView;

public class ColorPickerActivity extends AppCompatActivity {

    public static final String EXTRA_COLOR = "COLOR";

    private static final String TAG = ColorPickerActivity.class.getSimpleName();
    private Button okButton;
    private ColorPickerView colorPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);

        colorPicker = findViewById(R.id.colorPickerView);

        okButton = findViewById(R.id.choose_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent result = new Intent();
                result.putExtra(EXTRA_COLOR, colorPicker.getColor());
                setResult(ColorPickerActivity.RESULT_OK, result);
                finish();
            }
        });
    }
}