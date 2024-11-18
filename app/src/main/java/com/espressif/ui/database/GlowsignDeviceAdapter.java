package com.espressif.ui.database;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.espressif.wifi_provisioning.R;


public class GlowsignDeviceAdapter extends ListAdapter<GlowsignDevice, GlowsignDeviceAdapter.GlowsignDeviceViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(GlowsignDevice device);
    }

    private OnItemClickListener listener;

    public GlowsignDeviceAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public GlowsignDeviceAdapter.GlowsignDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.glowsign_device_item, parent, false);
        return new GlowsignDeviceViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull GlowsignDeviceAdapter.GlowsignDeviceViewHolder holder, int position) {
        GlowsignDevice device = getDeviceAt(position);
        holder.bind(device);
    }

    public GlowsignDevice getDeviceAt(int position) {
        return getItem(position);
    }

    private static final DiffUtil.ItemCallback<GlowsignDevice> DIFF_CALLBACK = new DiffUtil.ItemCallback<GlowsignDevice>() {
        @Override
        public boolean areItemsTheSame(@NonNull GlowsignDevice oldItem, @NonNull GlowsignDevice newItem) {
            return oldItem.name.equals(newItem.name);
        }

        @Override
        public boolean areContentsTheSame(@NonNull GlowsignDevice oldItem, @NonNull GlowsignDevice newItem) {
            return oldItem.model.equals(newItem.model);
        }
    };

    public class GlowsignDeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceModelName;

        GlowsignDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceModelName = itemView.findViewById(R.id.device_description);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION)
                        listener.onItemClick(getItem(position));
                }
            });
        }

        void bind(GlowsignDevice device) {
            deviceName.setText(device.name);
            deviceModelName.setText(device.model);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
