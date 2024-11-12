package com.espressif.ui.database;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.espressif.wifi_provisioning.R;


public class GlowsignDeviceAdapter extends ListAdapter<GlowsignDevice, GlowsignDeviceAdapter.ViewHolder> {
    GlowsignDeviceAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public GlowsignDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull GlowsignDeviceAdapter.ViewHolder holder, int position) {

    }

    private static final DiffUtil.ItemCallback<GlowsignDevice> DIFF_CALLBACK = new DiffUtil.ItemCallback<GlowsignDevice>() {
        @Override
        public boolean areItemsTheSame(@NonNull GlowsignDevice oldItem, @NonNull GlowsignDevice newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull GlowsignDevice oldItem, @NonNull GlowsignDevice newItem) {
            return false;
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceModelName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
        }
    }

}
