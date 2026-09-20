package com.falcon.car.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.falcon.car.R;
import com.falcon.car.data.model.AdapterProfile;
import com.falcon.car.data.model.Transport;
import com.falcon.car.ui.UiUtils;

import java.util.ArrayList;
import java.util.List;

/** Rows for the adapter catalogue, showing each family's real reach. */
public class AdapterCatalogAdapter
        extends RecyclerView.Adapter<AdapterCatalogAdapter.ProfileHolder> {

    public interface OnProfileSelected {
        void onProfileSelected(AdapterProfile profile);
    }

    private final List<AdapterProfile> profiles = new ArrayList<>();
    private final OnProfileSelected listener;

    public AdapterCatalogAdapter(OnProfileSelected listener) {
        this.listener = listener;
    }

    public void submit(List<AdapterProfile> newProfiles) {
        profiles.clear();
        profiles.addAll(newProfiles);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_adapter, parent, false);
        return new ProfileHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileHolder holder, int position) {
        holder.bind(profiles.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /** "Bluetooth, Bluetooth LE, USB OTG" for a profile's transport set. */
    public static String transportSummary(Context context, AdapterProfile profile) {
        StringBuilder builder = new StringBuilder();
        for (Transport transport : Transport.values()) {
            if (!profile.getTransports().contains(transport)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(context.getString(transport.getLabelRes()));
        }
        return builder.toString();
    }

    static class ProfileHolder extends RecyclerView.ViewHolder {

        private final TextView brandTag;
        private final TextView name;
        private final TextView models;
        private final TextView transports;
        private final TextView support;

        ProfileHolder(@NonNull View itemView) {
            super(itemView);
            brandTag = itemView.findViewById(R.id.adapter_brand_tag);
            name = itemView.findViewById(R.id.adapter_name);
            models = itemView.findViewById(R.id.adapter_models);
            transports = itemView.findViewById(R.id.adapter_transports);
            support = itemView.findViewById(R.id.adapter_support);
        }

        void bind(AdapterProfile profile, OnProfileSelected listener) {
            Context context = itemView.getContext();

            brandTag.setText(shortTag(profile.getBrand()));
            name.setText(profile.getBrand());
            models.setText(join(profile.getModels()));
            transports.setText(transportSummary(context, profile));

            support.setText(profile.getOverallSupport().getLabelRes());
            UiUtils.stylePill(support, profile.getOverallSupport().getPillRes(),
                    profile.getOverallSupport().getColorRes());

            itemView.setOnClickListener(v -> listener.onProfileSelected(profile));
        }

        private String shortTag(String brand) {
            return brand.length() <= 5 ? brand : brand.substring(0, 5);
        }

        private String join(List<String> values) {
            StringBuilder builder = new StringBuilder();
            for (String value : values) {
                if (builder.length() > 0) {
                    builder.append(" \u00B7 ");
                }
                builder.append(value);
            }
            return builder.toString();
        }
    }
}
