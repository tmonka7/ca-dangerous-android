package com.falcon.car.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.falcon.car.R;
import com.falcon.car.data.model.AdapterProfile;
import com.falcon.car.data.model.DiscoveredDevice;
import com.falcon.car.data.model.Protocol;
import com.falcon.car.data.model.SupportLevel;
import com.falcon.car.ui.UiUtils;
import com.falcon.car.ui.widget.SignalBarsView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Rows for the devices a scan turned up on the selected transport. */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceHolder> {

    public interface OnDeviceSelected {
        void onDeviceSelected(DiscoveredDevice device);
    }

    private final List<DiscoveredDevice> devices = new ArrayList<>();
    private final OnDeviceSelected listener;

    public DeviceAdapter(OnDeviceSelected listener) {
        this.listener = listener;
    }

    public void submit(List<DiscoveredDevice> newDevices) {
        devices.clear();
        devices.addAll(newDevices);
        notifyDataSetChanged();
    }

    public int getCount() {
        return devices.size();
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        holder.bind(devices.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceHolder extends RecyclerView.ViewHolder {

        private final ImageView icon;
        private final TextView name;
        private final TextView meta;
        private final TextView state;
        private final SignalBarsView signal;

        DeviceHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.device_icon);
            name = itemView.findViewById(R.id.device_name);
            meta = itemView.findViewById(R.id.device_meta);
            state = itemView.findViewById(R.id.device_state);
            signal = itemView.findViewById(R.id.device_signal);
        }

        void bind(DiscoveredDevice device, OnDeviceSelected listener) {
            UiUtils.setIcon(icon, device.getTransport().getIconRes(), R.color.redline_red);
            name.setText(device.getName());
            meta.setText(metaLine(device));
            signal.setLevel(device.getSignalLevel());

            boolean connected = device.getState() == DiscoveredDevice.State.CONNECTED;
            state.setText(connected ? R.string.status_connected : R.string.device_available);
            state.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    connected ? R.color.state_good : R.color.text_tertiary));

            itemView.setOnClickListener(v -> listener.onDeviceSelected(device));
        }

        /** Transport plus how many protocols this family actually covers. */
        private String metaLine(DiscoveredDevice device) {
            String transport = itemView.getContext().getString(device.getTransport().getLabelRes());
            AdapterProfile profile = device.getProfile();
            if (profile == null) {
                return transport;
            }
            int supported = 0;
            for (Map.Entry<Protocol, SupportLevel> entry : profile.getProtocols().entrySet()) {
                if (entry.getValue() == SupportLevel.SUPPORTED) {
                    supported++;
                }
            }
            return transport + " \u00B7 " + profile.getBrand() + " \u00B7 " + supported + " "
                    + itemView.getContext().getString(R.string.label_protocols);
        }
    }
}
